// Autisnautics: 50 km x 50 km worldborder in every loaded dimension.
// Diameter 50000, center 0,0, in that dimension's own block coordinates (bounds ±25000).
// Silent Java API. One log line per dimension per script load. /reload runs this again.
//
// Minecraft 1.21.1 WorldBorder scales only getCenterX/getCenterZ when the dimension
// type's coordinate_scale is not 1. setCenter and setSize store raw values. getMinX is
// the scaled center minus size/2, so center 0 and size 50000 is still ±25000 when the
// Nether's scale is 8. Do not multiply the diameter by the scale.
//
// Levels created at startup other than the overworld get a one-way
// DelegateBorderChangeListener on the overworld border. That listener copies the
// overworld's raw size and raw center. DerivedLevelData.setWorldBorder is a no-op, so
// those borders are not saved; the next boot copies the saved overworld border back
// through the listeners. Dimensions created after startup are not in that listener
// list, so each level is set directly as well.

let targetDiameter = 50000.0
let seen = {}

function applyBorder(level) {
    let id = String(level.dimension)
    if (seen[id]) return
    seen[id] = true

    let wb = level.worldBorder
    wb.setCenter(0.0, 0.0)
    wb.setSize(targetDiameter)

    let minX = wb.getMinX()
    let maxX = wb.getMaxX()
    let minZ = wb.getMinZ()
    let maxZ = wb.getMaxZ()
    let span = maxX - minX
    if (Math.abs(span - targetDiameter) > 0.5 && Math.abs(span) > 0.5) {
        wb.setSize(targetDiameter * (wb.getSize() / span))
        minX = wb.getMinX()
        maxX = wb.getMaxX()
        minZ = wb.getMinZ()
        maxZ = wb.getMaxZ()
        span = maxX - minX
    }

    let scale = 1.0
    try {
        scale = level.dimensionType().coordinateScale()
    } catch (error) {
        scale = -1
    }

    console.info('[Autisnautics] Worldborder ' + id
        + ' diameter ' + span
        + ' center ' + wb.getCenterX() + ',' + wb.getCenterZ()
        + ' bounds x[' + minX + ',' + maxX + '] z[' + minZ + ',' + maxZ + ']'
        + ' coordinate_scale ' + scale)
}

LevelEvents.loaded(function (event) {
    applyBorder(event.level)
})

ServerEvents.loaded(function (event) {
    let it = event.server.getAllLevels().iterator()
    while (it.hasNext()) applyBorder(it.next())
})
