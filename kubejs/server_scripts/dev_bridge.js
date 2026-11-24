// Reusable KubeJS server-side dev bridge using built-in JsonIO.
// Polls testing/bridge/server_req.json every 5 ticks; writes response to testing/bridge/server_res.json.
;(function () {
    const BlockPos = Java.loadClass('net.minecraft.core.BlockPos')

    const REQ_PATH = 'testing/bridge/server_req.json'
    const RES_PATH = 'testing/bridge/server_res.json'
    let tickCount = 0
    let lastId = ''

    function str(v, def) {
        return (v === null || v === undefined) ? (def || '') : String(v)
    }

    function num(v, def) {
        if (v === null || v === undefined) return def
        let n = Number(v)
        return isNaN(n) ? def : n
    }

    function getTargetPlayer(server, name) {
        let players = server.getPlayerList().getPlayers()
        if (players.isEmpty()) return null
        if (!name) return players.get(0)
        for (let i = 0; i < players.size(); i++) {
            let pl = players.get(i)
            if (String(pl.getGameProfile().getName()) === name) return pl
        }
        return players.get(0)
    }

    function isOpen(level, x, y, z, allowFluid) {
        let state = level.getBlockState(new BlockPos(x, y, z))
        if (state.isAir()) return true
        if (allowFluid && !state.getFluidState().isEmpty()) return true
        return false
    }

    function rayDistance(level, x, y, z, dx, dy, dz, maxDist, allowFluid) {
        for (let d = 1; d <= maxDist; d++) {
            if (!isOpen(level, Math.round(x + dx * d), Math.round(y + dy * d), Math.round(z + dz * d), allowFluid)) {
                return d - 1
            }
        }
        return maxDist
    }

    function findBestView(level, cx, cy, cz, yMin, yMax, radius, step, allowFluid) {
        let dirs = [
            { yaw: 0, dx: 0, dz: 1 },
            { yaw: 45, dx: -0.707, dz: 0.707 },
            { yaw: 90, dx: -1, dz: 0 },
            { yaw: 135, dx: -0.707, dz: -0.707 },
            { yaw: 180, dx: 0, dz: -1 },
            { yaw: -135, dx: 0.707, dz: -0.707 },
            { yaw: -90, dx: 1, dz: 0 },
            { yaw: -45, dx: 0.707, dz: 0.707 }
        ]
        let best = null
        let r = radius || 48
        let s = step || 8
        for (let x = cx - r; x <= cx + r; x += s) {
            for (let z = cz - r; z <= cz + r; z += s) {
                for (let y = yMin; y <= yMax; y += 6) {
                    if (!isOpen(level, x, y, z, allowFluid)) continue
                    if (!isOpen(level, x, y + 1, z, allowFluid)) continue
                    if (!isOpen(level, x, y + 2, z, allowFluid)) continue
                    for (let i = 0; i < dirs.length; i++) {
                        let d = dirs[i]
                        let fwd = rayDistance(level, x, y + 1, z, d.dx, -0.1, d.dz, 44, allowFluid)
                        if (fwd < 6) continue
                        let up = rayDistance(level, x, y + 1, z, 0, 1, 0, 14, allowFluid)
                        let down = rayDistance(level, x, y + 1, z, 0, -1, 0, 14, allowFluid)
                        let score = fwd * 3 + Math.min(up, 10) + Math.min(down, 10)
                        if (!best || score > best.score) {
                            best = { x: x + 0.5, y: y + 0.5, z: z + 0.5, yaw: d.yaw, pitch: 8, fwd: fwd, score: score }
                        }
                    }
                }
            }
        }
        return best
    }

    function dimOf(p) {
        let lvl = p.level
        if (typeof lvl.dimension === 'function') return String(lvl.dimension().location())
        return String(lvl.dimension)
    }

    function handle(server, req) {
        let cmd = str(req.cmd)
        let p, name, dim, level, res
        if (cmd === 'ping') {
            let players = []
            let list = server.getPlayerList().getPlayers()
            for (let i = 0; i < list.size(); i++) {
                p = list.get(i)
                players.push({
                    name: String(p.getGameProfile().getName()),
                    dim: dimOf(p),
                    x: Number(Number(p.x).toFixed(2)),
                    y: Number(Number(p.y).toFixed(2)),
                    z: Number(Number(p.z).toFixed(2)),
                    yaw: Number(Number(p.yaw).toFixed(1)),
                    pitch: Number(Number(p.pitch).toFixed(1))
                })
            }
            return { ok: true, tick: tickCount, players: players }
        }
        if (cmd === 'command') {
            res = server.runCommandSilent(str(req.command))
            return { ok: true, result: res }
        }
        if (cmd === 'tp') {
            p = getTargetPlayer(server, str(req.player))
            if (!p) return { ok: false, error: 'no player online' }
            name = String(p.getGameProfile().getName())
            dim = str(req.dim, dimOf(p))
            let tx = num(req.x, Number(p.x))
            let ty = num(req.y, Number(p.y))
            let tz = num(req.z, Number(p.z))
            let tyaw = num(req.yaw, Number(p.yaw))
            let tpitch = num(req.pitch, Number(p.pitch))
            let c = 'execute in ' + dim + ' run tp ' + name + ' ' + tx + ' ' + ty + ' ' + tz + ' ' + tyaw + ' ' + tpitch
            res = server.runCommandSilent(c)
            return { ok: true, command: c, result: res }
        }
        if (cmd === 'findView') {
            p = getTargetPlayer(server, str(req.player))
            if (!p) return { ok: false, error: 'no player online' }
            level = p.level
            let cx = Math.floor(num(req.x, Number(p.x)))
            let cy = Math.floor(num(req.y, Number(p.y)))
            let cz = Math.floor(num(req.z, Number(p.z)))
            let yMin = num(req.yMin, Math.max(level.getMinBuildHeight() + 8, cy - 30))
            let yMax = num(req.yMax, Math.min(level.getMaxBuildHeight() - 8, cy + 30))
            let best = findBestView(level, cx, cy, cz, yMin, yMax, num(req.radius, 48), num(req.step, 8), !!req.allowFluid)
            if (!best) return { ok: false, error: 'no open view found', dim: dimOf(p) }
            if (req.apply !== false) {
                name = String(p.getGameProfile().getName())
                dim = dimOf(p)
                server.runCommandSilent('execute in ' + dim + ' run tp ' + name + ' ' + best.x + ' ' + best.y + ' ' + best.z + ' ' + best.yaw + ' ' + best.pitch)
            }
            return { ok: true, dim: dimOf(p), view: best }
        }
        return { ok: false, error: 'unknown cmd: ' + cmd }
    }

    ServerEvents.tick(event => {
        tickCount++
        if (tickCount % 5 !== 0) return
        let req = null
        try {
            req = JsonIO.read(REQ_PATH)
        } catch (e) {
            return
        }
        if (!req || !req.id) return
        let id = String(req.id)
        if (id === lastId) return
        lastId = id
        let res
        try {
            res = handle(event.server, req)
        } catch (err) {
            res = { ok: false, error: String(err) }
        }
        res.id = id
        try {
            JsonIO.write(RES_PATH, res)
        } catch (err) {
            console.warn('[DevBridge] write failed: ' + err)
        }
    })
})()
