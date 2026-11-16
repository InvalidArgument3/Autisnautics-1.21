// No natural mobs in the overworld starter island.
//
// Center and radius are horizontal only. Y is ignored, so the column covers the
// full world height. Live world spawn is 62, 489, 41, about 74 blocks from the
// origin, which is why a 64-block circle around getSharedSpawnPos() never covered
// the island. Do not point this back at the shared spawn.

let SPAWN_SAFE_CENTER_X = 0
let SPAWN_SAFE_CENTER_Z = 0
let SPAWN_SAFE_RADIUS = 128
let SPAWN_SAFE_RADIUS_SQ = SPAWN_SAFE_RADIUS * SPAWN_SAFE_RADIUS

// NeoForge 21.1.265 posts FinalizeSpawnEvent only from EventHooks.finalizeMobSpawn,
// and the patched server calls that for BaseSpawner and TrialSpawner. Natural
// spawns, chunk generation, phantoms, patrols, raids, and structure placement call
// Mob.finalizeSpawn directly and then addFreshEntity. KubeJS checkSpawn never sees
// them, and NaturalSpawner does not read Mob.isSpawnCancelled(). The join listener
// below is what actually stops those.
//
// Spawn type is Mob.getSpawnType(), set inside finalizeSpawn before the entity is
// added. A mob added with addFreshEntity and no finalizeSpawn has a null type and
// is treated as natural.
//
// Kept: spawners, trial spawners, spawn eggs, commands, dispensers, buckets,
// breeding, conversion, mob-summoned (golems, withers, vexes), and triggered
// (silverfish broken out of a block). Cobblemon is exempt: wild spawns use
// NATURAL, and cancelling that would stop pastures. Entities loaded from a
// chunk are left where they are.

let BLOCKED_SPAWN_TYPES = {
    natural: true,
    chunk_generation: true,
    structure: true,
    patrol: true,
    event: true,
    jockey: true,
    reinforcement: true
}

let ALLOWED_SPAWN_TYPES = {
    spawner: true,
    trial_spawner: true,
    spawn_egg: true,
    command: true,
    breeding: true,
    bucket: true,
    dispenser: true,
    conversion: true,
    mob_summoned: true,
    triggered: true
}

let $SpawnSafeMob = Java.loadClass('net.minecraft.world.entity.Mob')
let $SpawnSafeJoin = Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent')

function spawnDimensionId(level) {
    try {
        return String(level.dimension().location())
    } catch (error) {
        try {
            return String(level.dimension)
        } catch (error2) {
            return ''
        }
    }
}

function spawnTypeName(type) {
    try {
        if (!type) return ''
        return String(type.name()).toLowerCase()
    } catch (error) {
        return String(type).toLowerCase()
    }
}

function entityTypeId(entity) {
    try {
        let encoded = entity.getEncodeId()
        if (encoded) return String(encoded)
    } catch (error) {}
    try {
        return String(entity.type)
    } catch (error2) {
        return ''
    }
}

function isCobblemon(entity) {
    return entityTypeId(entity).indexOf('cobblemon:') === 0
}

function inSpawnSafeZone(level, x, z) {
    if (spawnDimensionId(level).indexOf('minecraft:overworld') < 0) return false
    let dx = x - SPAWN_SAFE_CENTER_X
    let dz = z - SPAWN_SAFE_CENTER_Z
    return dx * dx + dz * dz <= SPAWN_SAFE_RADIUS_SQ
}

function freshMobBlocked(entity) {
    if (!(entity instanceof $SpawnSafeMob)) return false
    if (isCobblemon(entity)) return false
    let typeName = ''
    try {
        typeName = spawnTypeName(entity.getSpawnType())
    } catch (error) {
        typeName = ''
    }
    if (ALLOWED_SPAWN_TYPES[typeName]) return false
    return true
}

EntityEvents.checkSpawn(function (event) {
    if (!BLOCKED_SPAWN_TYPES[spawnTypeName(event.type)]) return
    if (isCobblemon(event.entity)) return
    if (inSpawnSafeZone(event.level, event.x, event.z)) event.cancel()
})

NativeEvents.onEvent($SpawnSafeJoin, function (event) {
    try {
        if (event.loadedFromDisk()) return
    } catch (error) {
        return
    }
    let level = event.getLevel()
    if (level.isClientSide()) return
    let entity = event.getEntity()
    if (!inSpawnSafeZone(level, entity.getX(), entity.getZ())) return
    if (!freshMobBlocked(entity)) return
    event.setCanceled(true)
})

ServerEvents.loaded(function (event) {
    console.info('[Autisnautics] Overworld natural mobs blocked within ' + SPAWN_SAFE_RADIUS
        + ' blocks of X=' + SPAWN_SAFE_CENTER_X + ' Z=' + SPAWN_SAFE_CENTER_Z
        + ' (full height). Spawners, eggs, commands, breeding, and buckets still work. Cobblemon is exempt. Mobs already saved in chunks are left in place.')
})
