// Reusable KubeJS client-side dev bridge using built-in JsonIO.
// Polls testing/bridge/client_req.json every 5 ticks; writes response to testing/bridge/client_res.json.
;(function () {
    const Minecraft = Java.loadClass('net.minecraft.client.Minecraft')
    const Screenshot = Java.loadClass('net.minecraft.client.Screenshot')

    const REQ_PATH = 'testing/bridge/client_req.json'
    const RES_PATH = 'testing/bridge/client_res.json'
    let tickCount = 0
    let lastId = ''

    function str(v, def) {
        return (v === null || v === undefined) ? (def || '') : String(v)
    }

    function handle(mc, req) {
        let cmd = str(req.cmd)
        let p = mc.player
        if (cmd === 'ping') {
            return {
                ok: true,
                tick: tickCount,
                inWorld: !!p,
                dim: p ? String(p.level.dimension) : null,
                x: p ? Number(Number(p.x).toFixed(2)) : null,
                y: p ? Number(Number(p.y).toFixed(2)) : null,
                z: p ? Number(Number(p.z).toFixed(2)) : null,
                yaw: p ? Number(Number(p.yaw).toFixed(1)) : null,
                pitch: p ? Number(Number(p.pitch).toFixed(1)) : null,
                hideGui: !!mc.options.hideGui,
                paused: !!mc.isPaused(),
                pauseOnLostFocus: !!mc.options.pauseOnLostFocus,
                screen: mc.screen ? String(mc.screen) : null,
                width: mc.getWindow().getWidth(),
                height: mc.getWindow().getHeight()
            }
        }
        if (cmd === 'pauseOnLostFocus') {
            mc.options.pauseOnLostFocus = !!req.value
            return { ok: true, pauseOnLostFocus: !!mc.options.pauseOnLostFocus }
        }
        if (cmd === 'hideHud') {
            mc.options.hideGui = !!req.hide
            return { ok: true, hideGui: !!mc.options.hideGui }
        }
        if (cmd === 'closeScreen') {
            mc.setScreen(null)
            return { ok: true }
        }
        if (cmd === 'fov') {
            mc.options.fov().set(Number(req.value || 70))
            return { ok: true, fov: Number(mc.options.fov().get()) }
        }
        if (cmd === 'look') {
            if (!p) return { ok: false, error: 'no client player' }
            if (req.yaw !== null && req.yaw !== undefined) {
                p.yaw = Number(req.yaw)
            }
            if (req.pitch !== null && req.pitch !== undefined) {
                p.pitch = Number(req.pitch)
            }
            return { ok: true, yaw: Number(p.yaw), pitch: Number(p.pitch) }
        }
        if (cmd === 'screenshot') {
            let fileName = str(req.file, 'cap_' + Date.now() + '.png')
            Screenshot.grab(mc.gameDirectory, fileName, mc.getMainRenderTarget(), function () {})
            return { ok: true, path: 'screenshots/' + fileName }
        }
        if (cmd === 'clientCommand') {
            if (!p || !mc.player.connection) return { ok: false, error: 'no client connection' }
            let c = str(req.command).replace(/^\//, '')
            mc.player.connection.sendCommand(c)
            return { ok: true, command: c }
        }
        if (cmd === 'mute') {
            let SoundSource = Java.loadClass('net.minecraft.sounds.SoundSource')
            let muted = req.muted !== false
            let vol = muted ? 0.0 : (req.volume !== undefined && req.volume !== null ? Number(req.volume) : 1.0)
            mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(vol)
            mc.getSoundManager().updateSourceVolume(SoundSource.MASTER, vol)
            return { ok: true, muted: muted, masterVolume: vol }
        }
        return { ok: false, error: 'unknown cmd: ' + cmd }
    }

    ClientEvents.tick(event => {
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
            res = handle(Minecraft.getInstance(), req)
        } catch (err) {
            res = { ok: false, error: String(err) }
        }
        res.id = id
        try {
            JsonIO.write(RES_PATH, res)
        } catch (err) {
            console.warn('[DevBridge] client write failed: ' + err)
        }
    })
})()
