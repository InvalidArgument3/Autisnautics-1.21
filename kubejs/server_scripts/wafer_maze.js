// Wafer Gate holds one ruined wafer. Right-click enters that cell. Death sends you back with your items. The same wafer stays seated for another try.

const WAFER_DIM = "autisnautics:wafer"
const MAZE = 17
const FLOOR = 64
const ROOM = 4

function rngOf(seed) {
    let state = seed | 0
    return () => {
        state = (state * 1664525 + 1013904223) | 0
        return (state >>> 0) / 4294967296
    }
}

function carveMaze(random) {
    let wall = []
    for (let y = 0; y < MAZE; y++) {
        wall[y] = []
        for (let x = 0; x < MAZE; x++) wall[y][x] = true
    }
    let visit = (cx, cy) => {
        wall[cy][cx] = false
        let dirs = [[1, 0], [-1, 0], [0, 1], [0, -1]]
        for (let i = dirs.length - 1; i > 0; i--) {
            let j = Math.floor(random() * (i + 1))
            let swap = dirs[i]
            dirs[i] = dirs[j]
            dirs[j] = swap
        }
        dirs.forEach(dir => {
            let nx = cx + dir[0] * 2
            let ny = cy + dir[1] * 2
            if (nx > 0 && ny > 0 && nx < MAZE - 1 && ny < MAZE - 1 && wall[ny][nx]) {
                wall[cy + dir[1]][cx + dir[0]] = false
                visit(nx, ny)
            }
        })
    }
    visit(1, 1)
    return wall
}

function paint(level, x, y, z, block) {
    level.getBlock(x, y, z).set(block)
}

function buildCell(level, cell, wall) {
    let originX = 32 + cell * 64
    let originZ = 32
    for (let dz = 0; dz < MAZE; dz++) {
        for (let dx = 0; dx < MAZE; dx++) {
            let x = originX + dx
            let z = originZ + dz
            paint(level, x, FLOOR, z, "minecraft:smooth_quartz")
            paint(level, x, FLOOR + ROOM + 1, z, "minecraft:crying_obsidian")
            for (let dy = 1; dy <= ROOM; dy++) {
                paint(level, x, FLOOR + dy, z, wall[dz][dx] ? "minecraft:polished_deepslate" : "minecraft:air")
            }
            if (!wall[dz][dx] && ((dx + dz) % 5 == 0)) paint(level, x, FLOOR + 1, z, "minecraft:candle")
        }
    }
    paint(level, originX + 1, FLOOR + 1, originZ + 1, "minecraft:air")
    return { x: originX, z: originZ }
}

function openTiles(wall) {
    let tiles = []
    for (let z = 0; z < MAZE; z++) {
        for (let x = 0; x < MAZE; x++) {
            if (!wall[z][x]) tiles.push({ x: x, z: z })
        }
    }
    return tiles
}

function summon(server, id, x, y, z, cell, name) {
    let nbt = `{Tags:["wafer_demon","wafer_cell_${cell}"],PersistenceRequired:1b,CustomName:'{"text":"${name}"}'}`
    server.runCommandSilent(`execute in ${WAFER_DIM} run summon ${id} ${x} ${y} ${z} ${nbt}`)
}

function chamber(wall, tile) {
    for (let dz = -1; dz <= 1; dz++) {
        for (let dx = -1; dx <= 1; dx++) {
            let x = tile.x + dx
            let z = tile.z + dz
            if (x > 0 && z > 0 && x < MAZE - 1 && z < MAZE - 1) wall[z][x] = false
        }
    }
}

function stockCell(server, cell, origin, tiles, far) {
    let fighters = []
    if (Platform.isLoaded("quark")) {
        fighters.push(["quark:wraith", "Lattice Wraith"])
        fighters.push(["quark:wraith", "Lattice Wraith"])
        fighters.push(["quark:wraith", "Lattice Wraith"])
        fighters.push(["quark:wraith", "Lattice Wraith"])
        fighters.push(["quark:foxhound", "Lattice Hound"])
        fighters.push(["quark:foxhound", "Lattice Hound"])
    } else {
        fighters.push(["minecraft:wither_skeleton", "Lattice Demon"])
        fighters.push(["minecraft:wither_skeleton", "Lattice Demon"])
        fighters.push(["minecraft:blaze", "Lattice Demon"])
        fighters.push(["minecraft:blaze", "Lattice Demon"])
        fighters.push(["minecraft:vex", "Lattice Imp"])
        fighters.push(["minecraft:vex", "Lattice Imp"])
    }
    if (Platform.isLoaded("friendsandfoes")) fighters.push(["friendsandfoes:wildfire", "Lattice Wildfire"])

    let spots = tiles.filter(tile => tile.x != 1 || tile.z != 1)
    fighters.forEach((mob, index) => {
        let spot = index == fighters.length - 1 && Platform.isLoaded("friendsandfoes") ? far : spots[index % spots.length]
        summon(server, mob[0], origin.x + spot.x + 0.5, FLOOR + 1, origin.z + spot.z + 0.5, cell, mob[1])
    })
    return fighters.length
}

function findPlayer(server, name) {
    if (!name) return null
    return server.getPlayer(name)
}

function wipe(server, cell) {
    server.runCommandSilent(`execute in ${WAFER_DIM} run kill @e[tag=wafer_cell_${cell}]`)
}

function closeRun(server, cell) {
    let data = server.persistentData
    if (data.getBoolean("wafer_closed_" + cell)) return null
    data.putBoolean("wafer_closed_" + cell, true)
    data.putInt("wafer_left_" + cell, -1)
    let player = findPlayer(server, data.getString("wafer_owner_" + cell))
    wipe(server, cell)
    return player
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
    if (dim) player.teleportTo(dim, x, y, z, 0, 0)
    if (message) player.tell(message)
}

function enterCell(server, player, block) {
    let level = server.getLevel(WAFER_DIM)
    if (!level) {
        player.tell(Text.red("The wafer dimension is not loaded."))
        return
    }
    let cell = ensureCell(server, block)
    let wall = carveMaze(rngOf((cell + 1) * 0x5bd1e995))
    let tiles = openTiles(wall)
    let far = tiles[0]
    tiles.forEach(tile => {
        if (tile.x + tile.z > far.x + far.z) far = tile
    })
    chamber(wall, far)
    let origin = buildCell(level, cell, wall)
    let count = stockCell(server, cell, origin, openTiles(wall), far)
    let data = player.persistentData
    data.putBoolean("wafer_active", true)
    data.putInt("wafer_cell", cell)
    data.putString("wafer_return_dim", String(player.level.dimension))
    data.putDouble("wafer_x", player.x)
    data.putDouble("wafer_y", player.y)
    data.putDouble("wafer_z", player.z)
    data.putString("wafer_gate_dim", String(block.level.dimension))
    data.putInt("wafer_gate_x", block.x)
    data.putInt("wafer_gate_y", block.y)
    data.putInt("wafer_gate_z", block.z)
    server.persistentData.putString("wafer_owner_" + cell, player.username)
    server.persistentData.putInt("wafer_left_" + cell, count)
    server.persistentData.putBoolean("wafer_closed_" + cell, false)
    player.teleportTo(WAFER_DIM, origin.x + 1.5, FLOOR + 1, origin.z + 1.5, 0, 0)
    player.tell(Text.gold("The wafer stays in the gate. Kill every demon. If you die, you come back here."))
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

    let player = closeRun(server, cell)
    if (!player) return
    let data = player.persistentData
    let dim = data.getString("wafer_return_dim")
    let x = data.getDouble("wafer_x")
    let y = data.getDouble("wafer_y")
    let z = data.getDouble("wafer_z")
    data.putBoolean("wafer_active", false)
    if (!setGateItem(server, data, "kubejs:silicon_wafer")) player.give("kubejs:silicon_wafer")
    if (dim) player.teleportTo(dim, x, y, z, 0, 0)
    player.tell(Text.green("The lattice is clean. The wafer in the gate is blank. Sneak-click the gate to take it."))
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
