// Wafer Gate holds one ruined wafer. Right-click enters the wafer dimension. The maze is worldgen, not built on click.

const WAFER_DIM = "autisnautics:wafer"
const MAZE_ANCHOR = { x: 8002, y: 64, z: 8002 }
const SPOTS = [[24, 12], [28, 0], [28, 8], [12, 4], [8, 4], [16, 4]]
const BOSS = [8, 0]

function summon(server, id, x, y, z, cell, name) {
    let nbt = `{Tags:["wafer_demon","wafer_cell_${cell}"],PersistenceRequired:1b,CustomName:'{"text":"${name}"}'}`
    server.runCommandSilent(`execute in ${WAFER_DIM} run summon ${id} ${x} ${y} ${z} ${nbt}`)
}

function fighters() {
    let list = []
    if (Platform.isLoaded("quark")) {
        list.push(["quark:wraith", "Lattice Wraith"])
        list.push(["quark:wraith", "Lattice Wraith"])
        list.push(["quark:wraith", "Lattice Wraith"])
        list.push(["quark:wraith", "Lattice Wraith"])
        list.push(["quark:foxhound", "Lattice Hound"])
        list.push(["quark:foxhound", "Lattice Hound"])
    } else {
        list.push(["minecraft:wither_skeleton", "Lattice Demon"])
        list.push(["minecraft:wither_skeleton", "Lattice Demon"])
        list.push(["minecraft:blaze", "Lattice Demon"])
        list.push(["minecraft:blaze", "Lattice Demon"])
        list.push(["minecraft:vex", "Lattice Imp"])
        list.push(["minecraft:vex", "Lattice Imp"])
    }
    if (Platform.isLoaded("friendsandfoes")) list.push(["friendsandfoes:wildfire", "Lattice Wildfire"])
    return list
}

function rawLevel(level) {
    if (typeof level.getChunkSource == "function") return level
    if (level.minecraftLevel) return level.minecraftLevel
    return level
}

function placeStructure(level, id, x, y, z) {
    let ResourceLocation = Java.loadClass("net.minecraft.resources.ResourceLocation")
    let BlockPos = Java.loadClass("net.minecraft.core.BlockPos")
    let StructurePlaceSettings = Java.loadClass("net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings")
    let RandomSource = Java.loadClass("net.minecraft.util.RandomSource")
    let template = rawLevel(level).getStructureManager().getOrCreate(ResourceLocation.parse(id))
    if (!template || template.getSize().getX() == 0) return false
    let pos = new BlockPos(x, y, z)
    template.placeInWorld(rawLevel(level), pos, pos, new StructurePlaceSettings(), RandomSource.create(1), 2)
    return true
}

function anchorOf(server) {
    let data = server.persistentData
    if (!data.getBoolean("wafer_maze_ready")) return { x: MAZE_ANCHOR.x, y: MAZE_ANCHOR.y, z: MAZE_ANCHOR.z }
    return { x: data.getInt("wafer_maze_x"), y: data.getInt("wafer_maze_y"), z: data.getInt("wafer_maze_z") }
}

function rememberAnchor(server, anchor) {
    let data = server.persistentData
    data.putBoolean("wafer_maze_ready", true)
    data.putInt("wafer_maze_x", anchor.x)
    data.putInt("wafer_maze_y", anchor.y)
    data.putInt("wafer_maze_z", anchor.z)
}

function stockMaze(server, cell, anchor) {
    let mobs = fighters()
    let alive = 0
    for (let i = 0; i < mobs.length; i++) {
        let offset = i == mobs.length - 1 ? BOSS : SPOTS[i % SPOTS.length]
        summon(server, mobs[i][0], anchor.x + offset[0] + 0.5, anchor.y + 1, anchor.z + offset[1] + 0.5, cell, mobs[i][1])
        alive++
    }
    return alive
}

function findOwner(server, cell) {
    let id = server.persistentData.getString("wafer_owner_" + cell)
    let name = server.persistentData.getString("wafer_owner_name_" + cell)
    let found = null
    server.getPlayers().forEach(player => {
        let uuid = String(player.uuid)
        let username = String(player.username)
        if (id && (uuid == id || username == id)) found = player
        if (name && username == name) found = player
    })
    return found
}

function demonsLeft(level, cell) {
    let alive = 0
    let tag = "wafer_cell_" + cell
    level.getAllEntities().forEach(entity => {
        if (!entity.isAlive()) return
        let tags = entity.getTags()
        if (tags.contains("wafer_demon") && tags.contains(tag)) alive++
    })
    return alive
}

function finishClear(player) {
    let data = player.persistentData
    if (!data.getBoolean("wafer_active")) return
    let cell = data.getInt("wafer_cell")
    let server = player.server
    server.persistentData.putBoolean("wafer_closed_" + cell, true)
    server.persistentData.putInt("wafer_left_" + cell, 0)
    wipe(server, cell)
    let dim = data.getString("wafer_return_dim")
    let x = data.getDouble("wafer_x")
    let y = data.getDouble("wafer_y")
    let z = data.getDouble("wafer_z")
    data.putBoolean("wafer_active", false)
    if (!setGateItem(server, data, "kubejs:silicon_wafer")) player.give("kubejs:silicon_wafer")
    if (dim) player.teleportTo(dim, x, y, z, 0, 0)
    player.tell(Text.green("The lattice is clean. The wafer in the gate is blank. Sneak-click the gate to take it."))
}

function wipe(server, cell) {
    server.runCommandSilent(`execute in ${WAFER_DIM} run kill @e[tag=wafer_cell_${cell}]`)
}

function stackId(stack) {
    if (!stack || stack.isEmpty()) return ""
    return String(Item.of(stack).id)
}

function cellKey(block) {
    let dim = String(block.level.dimension).replace(":", "_")
    return "wafer_seat_" + dim + "_" + block.x + "_" + block.y + "_" + block.z
}

function ensureCell(server, block) {
    let key = cellKey(block)
    let stored = server.persistentData.getInt(key)
    if (stored > 0) return stored - 1
    let cell = server.persistentData.getInt("wafer_next_cell")
    server.persistentData.putInt("wafer_next_cell", cell + 1)
    server.persistentData.putInt(key, cell + 1)
    return cell
}

function setGateItem(server, data, itemId) {
    let dim = data.getString("wafer_gate_dim")
    let level = dim ? server.getLevel(dim) : null
    if (!level) return false
    let block = level.getBlock(data.getInt("wafer_gate_x"), data.getInt("wafer_gate_y"), data.getInt("wafer_gate_z"))
    let inv = block.getInventory()
    if (!inv) return false
    inv.setStackInSlot(0, Item.of(itemId))
    inv.setChanged()
    return true
}

function goHome(player, message) {
    let data = player.persistentData
    if (!data.getBoolean("wafer_active")) return
    let cell = data.getInt("wafer_cell")
    let dim = data.getString("wafer_return_dim")
    let x = data.getDouble("wafer_x")
    let y = data.getDouble("wafer_y")
    let z = data.getDouble("wafer_z")
    player.server.persistentData.putInt("wafer_left_" + cell, -1)
    wipe(player.server, cell)
    data.putBoolean("wafer_active", false)
    data.putBoolean("wafer_building", false)
    if (dim) player.teleportTo(dim, x, y, z, 0, 0)
    if (message) player.tell(message)
}

function enterCell(server, player, block) {
    let level = server.getLevel(WAFER_DIM)
    if (!level) {
        player.tell(Text.red("The wafer dimension is not loaded."))
        return
    }
    let anchor = anchorOf(server)
    rememberAnchor(server, anchor)
    if (!server.persistentData.getBoolean("wafer_maze_placed")) {
        if (!placeStructure(level, "autisnautics:wafer/maze", anchor.x - 2, anchor.y, anchor.z - 2)) {
            player.tell(Text.red("The lattice template is not loaded. Restart the instance once."))
            return
        }
        server.persistentData.putBoolean("wafer_maze_placed", true)
    }
    let cell = ensureCell(server, block)
    let data = player.persistentData
    data.putBoolean("wafer_active", true)
    data.putBoolean("wafer_building", false)
    data.putInt("wafer_cell", cell)
    data.putInt("wafer_enter_age", player.age | 0)
    data.putString("wafer_return_dim", String(player.level.dimension))
    data.putDouble("wafer_x", player.x)
    data.putDouble("wafer_y", player.y)
    data.putDouble("wafer_z", player.z)
    data.putString("wafer_gate_dim", String(block.level.dimension))
    data.putInt("wafer_gate_x", block.x)
    data.putInt("wafer_gate_y", block.y)
    data.putInt("wafer_gate_z", block.z)
    server.persistentData.putString("wafer_owner_" + cell, String(player.uuid))
    server.persistentData.putString("wafer_owner_name_" + cell, String(player.username))
    server.persistentData.putBoolean("wafer_closed_" + cell, false)
    let count = stockMaze(server, cell, anchor)
    server.persistentData.putInt("wafer_left_" + cell, count)
    player.teleportTo(WAFER_DIM, anchor.x + 0.5, anchor.y + 1, anchor.z + 0.5, 0, 0)
    player.tell(Text.gold("The wafer stays in the gate. " + count + " demons are in the lattice."))
}

BlockEvents.rightClicked("kubejs:wafer_gate", event => {
    if (String(event.hand) != "MAIN_HAND") return
    let player = event.player
    if (!player) return
    let inv = event.block.getInventory()
    if (!inv) {
        player.tell(Text.red("This gate has no seat. Break it and place a new one."))
        return
    }
    let held = stackId(inv.getStackInSlot(0))
    let hand = String(event.item.id)
    let sneaking = player.isShiftKeyDown()
    if (player.persistentData.getBoolean("wafer_active") && String(player.level.dimension) != WAFER_DIM) {
        let cell = player.persistentData.getInt("wafer_cell")
        player.server.persistentData.putInt("wafer_left_" + cell, -1)
        wipe(player.server, cell)
        player.persistentData.putBoolean("wafer_active", false)
    }
    if (player.persistentData.getBoolean("wafer_active") && !sneaking) {
        player.tell(Text.red("You are already inside a wafer cell."))
        event.cancel()
        return
    }
    if (sneaking || (held == "kubejs:silicon_wafer" && hand == "minecraft:air")) {
        if (!held) {
            player.tell(Text.gray("The gate is empty."))
        } else {
            player.give(Item.of(inv.getStackInSlot(0).copy()))
            inv.setStackInSlot(0, Item.of("minecraft:air"))
            inv.setChanged()
            player.tell(Text.gray(held == "kubejs:silicon_wafer" ? "The blank wafer comes free." : "You take the wafer back."))
        }
        event.cancel()
        return
    }
    if (!held && hand == "kubejs:ruined_wafer") {
        inv.setStackInSlot(0, Item.of("kubejs:ruined_wafer"))
        event.item.shrink(1)
        inv.setChanged()
        player.tell(Text.gold("The gate is holding the wafer. Right-click with an empty hand to enter."))
        event.cancel()
        return
    }
    if (held == "kubejs:ruined_wafer" && hand == "minecraft:air") {
        enterCell(event.server, player, event.block)
        event.cancel()
        return
    }
    player.tell(held ? Text.gray("The gate is already holding a wafer. Sneak-click to take it.") : Text.gray("The gate wants a ruined wafer."))
    event.cancel()
})

EntityEvents.death(event => {
    let entity = event.entity
    if (entity.isPlayer() && String(entity.level.dimension) == WAFER_DIM && entity.persistentData.getBoolean("wafer_active")) {
        entity.setHealth(entity.getMaxHealth())
        entity.setRemainingFireTicks(0)
        goHome(entity, Text.red("You fall out of the lattice. Your items stay with you. The wafer is still in the gate."))
        event.cancel()
        return
    }
    let tags = String(entity.tags)
    if (!tags.includes("wafer_demon")) return
    let match = tags.match(/wafer_cell_(\d+)/)
    if (!match) return
    let cell = match[1]
    let server = event.server
    let left = server.persistentData.getInt("wafer_left_" + cell)
    if (left <= 0) return
    left--
    server.persistentData.putInt("wafer_left_" + cell, left)
    if (left > 0) return
    let player = findOwner(server, cell)
    if (!player) return
    finishClear(player)
})

PlayerEvents.tick(event => {
    let player = event.player
    let data = player.persistentData
    if (!data.getBoolean("wafer_active")) return
    if (String(player.level.dimension) != WAFER_DIM) return
    if (player.age % 20 != 0) return
    let server = player.server
    let cell = data.getInt("wafer_cell")
    let left = server.persistentData.getInt("wafer_left_" + cell)
    let since = (player.age | 0) - data.getInt("wafer_enter_age")
    if (left < 0) {
        if (since < 20) return
        let alive = demonsLeft(player.level, cell)
        if (alive > 0) {
            server.persistentData.putInt("wafer_left_" + cell, alive)
            return
        }
        server.persistentData.putInt("wafer_left_" + cell, stockMaze(server, cell, anchorOf(server)))
        return
    }
    if (since < 40) return
    if (demonsLeft(player.level, cell) > 0) return
    finishClear(player)
})

PlayerEvents.respawned(event => {
    let player = event.player
    if (!player.persistentData.getBoolean("wafer_active")) return
    goHome(player, Text.red("You fall out of the lattice. The wafer is still in the gate."))
})

PlayerEvents.loggedIn(event => {
    let player = event.player
    if (!player.persistentData.getBoolean("wafer_active")) return
    if (String(player.level.dimension) == WAFER_DIM) return
    goHome(player, Text.red("The cell closed while you were gone. The wafer is still in the gate."))
})
