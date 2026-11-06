// Autisnautics: enforce 50 km × 50 km Overworld worldborder (±25000 from 0,0).
// Vanilla WorldBorder.setSize(50000) is the full diameter (min = center - size/2).
// Silent Java API — no chat spam on /reload or server start.
ServerEvents.loaded(event => {
    const server = event.server
    const level = server.getLevel('minecraft:overworld')
    if (!level) return

    const wb = level.worldBorder
    const target = 50000.0
    const cx = wb.centerX
    const cz = wb.centerZ
    const size = wb.size

    let changed = false
    if (Math.abs(cx) > 0.01 || Math.abs(cz) > 0.01) {
        wb.setCenter(0.0, 0.0)
        changed = true
    }
    if (Math.abs(size - target) > 0.5) {
        wb.setSize(target)
        changed = true
    }
    if (changed) {
        console.info('[Autisnautics] Overworld worldborder set to 50000 (50 km × 50 km) centered at 0,0')
    }
})
