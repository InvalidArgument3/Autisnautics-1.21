if (handlers.length !== 2) throw new Error('expected chunk and tick handlers, got ' + handlers.length)
var onChunk = handlers[0]
var onTick = handlers[1]
var ServerLevel = classes['net.minecraft.server.level.ServerLevel']
var blockReads = 0

function makeLevel(dim, chunkNow) {
    var value = new ServerLevel()
    value.dimension = dim
    value.persistentData = {
        getBoolean: function () { return false },
        putBoolean: function () {},
        remove: function () {}
    }
    value.getMinBuildHeight = function () { return -64 }
    value.getMaxBuildHeight = function () { return 320 }
    value.setBlock = function () {}
    value.getFluidTicks = function () { return { clearArea: function () {} } }
    value.getChunkSource = function () {
        return {
            getChunkNow: function () { return chunkNow },
            randomState: function () {
                return { router: function () { return { finalDensity: function () { return density } } } }
            }
        }
    }
    return value
}

function chunkEvent(dim, x, z, chunkNow) {
    return {
        getLevel: function () { return makeLevel(dim, chunkNow) },
        isNewChunk: function () { return true },
        getChunk: function () { return { getPos: function () { return { x: x, z: z } } } }
    }
}

// Face 0 (anchor 127) is inside only one step east of the column, so that column is an outer ring.
// Face 1 is never inside, so the overlap loop must visit both faces before it returns.
var density = {
    compute: function (point) {
        if (point.y === 127 && point.x === 1 && point.z === 0) return -2001
        if (point.y === 127 && point.x === 64 && point.z === 64) return -2001
        return 0
    }
}

onChunk(chunkEvent('minecraft:overworld', 0, 0, null))
onChunk(chunkEvent('minecraft:overworld', 1, 0, null))
onTick({ server: {} })

var section = { hasOnlyAir: function () { return false } }
var loaded = {
    getSection: function () { return section },
    getSectionIndex: function () { return 0 },
    getBlockState: function () {
        blockReads++
        return {
            isAir: function () { return true },
            getFluidState: function () { return { isEmpty: function () { return true } } },
            getBlock: function () { return { id: 'minecraft:air' } }
        }
    },
    getFluidTicks: function () { return { count: function () { return 0 } } }
}
onChunk(chunkEvent('infinite_abyss:first_layer', 0, 0, loaded))
var guard = 0
while (guard++ < 80) onTick({ server: {} })
if (blockReads < 1) throw new Error('overlap-face loop never ran; blockReads=' + blockReads)

// A loaded-looking event whose chunk is not yet readable must keep its marker and retry.
var removed = 0
var absent = makeLevel('infinite_abyss:first_layer', null)
absent.persistentData.remove = function () { removed++ }
onChunk({
    getLevel: function () { return absent },
    isNewChunk: function () { return true },
    getChunk: function () { return { getPos: function () { return { x: 4, z: 4 } } } }
})
var spins = 0
while (spins++ < 12) onTick({ server: {} })
if (removed !== 0) throw new Error('marker removed while getChunkNow stayed null')
var readsBeforeResume = blockReads
absent.getChunkSource = function () {
    return {
        getChunkNow: function () { return loaded },
        randomState: function () {
            return { router: function () { return { finalDensity: function () { return density } } } }
        }
    }
}
var resumed = 0
while (resumed++ < 40) onTick({ server: {} })
if (blockReads <= readsBeforeResume) throw new Error('retry did not resume a chunk that became readable without a second load event')
