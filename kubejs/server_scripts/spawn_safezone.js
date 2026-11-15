// No natural mobs within 64 blocks (horizontal) of the overworld world spawn.
//
// KubeJS EntityEvents.checkSpawn is NeoForge FinalizeSpawnEvent. event.cancel()
// is interruptFalse, which setSpawnCancelled(true) and cancels the event, so the
// mob is never added. Distance ignores height. The point is ServerLevel's shared
// spawn (the /setworldspawn point), not a bed.
//
// Cancelled: natural, chunk generation, structure, patrol, raid/event, jockey,
// and reinforcement. Those are the world filling itself in.
//
// Left alone: spawners, trial spawners, spawn eggs, commands, dispensers,
// buckets, breeding, conversion, mob-summoned (golems, withers, vexes), and
// triggered (silverfish broken out of a block).
//
// Cobblemon wild spawns call Mob.finalizeSpawn with MobSpawnType.NATURAL, so a
// blanket natural cancel would also stop pastures and the species spawner.
// /spawnpokemon uses COMMAND and would survive, but the spawner would not.
// cobblemon: entities are exempt. Abyss Creatures are not special-cased: the
// radius is overworld-only, and a natural FinalizeSpawn in that circle is
// cancelled like any other mob.

var SPAWN_SAFE_RADIUS = 64
var SPAWN_SAFE_RADIUS_SQ = SPAWN_SAFE_RADIUS * SPAWN_SAFE_RADIUS

var BLOCKED_SPAWN_TYPES = {
    natural: true,
    chunk_generation: true,
    structure: true,
    patrol: true,
    event: true,
    jockey: true,
    reinforcement: true
}

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

EntityEvents.checkSpawn(function (event) {
    if (spawnDimensionId(event.level).indexOf('minecraft:overworld') < 0) return
    if (!BLOCKED_SPAWN_TYPES[spawnTypeName(event.type)]) return

    let id = entityTypeId(event.entity)
    if (id.indexOf('cobblemon:') === 0) return

    let spawn
    try {
        spawn = event.level.getSharedSpawnPos()
    } catch (error) {
        return
    }
    if (!spawn) return

    let dx = event.x - spawn.getX()
    let dz = event.z - spawn.getZ()
    if (dx * dx + dz * dz <= SPAWN_SAFE_RADIUS_SQ) {
        event.cancel()
    }
})

ServerEvents.loaded(function (event) {
    let it = event.server.getAllLevels().iterator()
    while (it.hasNext()) {
        let level = it.next()
        let dimension = spawnDimensionId(level)
        if (dimension.indexOf('minecraft:overworld') < 0) continue
        let spawn = level.getSharedSpawnPos()
        console.info('[Autisnautics] Overworld ' + dimension
            + ' shared spawn ' + spawn.getX() + ' ' + spawn.getY() + ' ' + spawn.getZ()
            + '; natural mobs blocked inside ' + SPAWN_SAFE_RADIUS + ' blocks horizontally. Cobblemon entities are exempt.')
    }
})
