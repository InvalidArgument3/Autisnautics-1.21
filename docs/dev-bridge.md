# KubeJS Dev Bridge

Lightweight file-queue bridge for driving a running Minecraft client/singleplayer world from scripts, tests, or AI agents.

## Files

- `kubejs/server_scripts/dev_bridge.js` — polls `testing/bridge/server_req.json` every 5 server ticks and writes `testing/bridge/server_res.json`.
- `kubejs/client_scripts/dev_bridge_client.js` — polls `testing/bridge/client_req.json` every 5 client ticks and writes `testing/bridge/client_res.json`.
- `testing/bridge.ps1` — PowerShell CLI helper that stamps a request `id`, writes the request file, and waits for the matching response.

## Export Exclusion (Dev-Only)

This bridge is **dev/repo-only** and does not ship in exported client or server packs:
- `pakku.json` excludes `!kubejs/server_scripts/dev_bridge.js` and `!kubejs/client_scripts/dev_bridge_client.js` from `overrides`.
- `testing/` and `docs/` are not included in `overrides`, `client_overrides`, or `server_overrides`.
- `testing/bridge/` (runtime `*_req.json` / `*_res.json` files) is gitignored.

When idle (no request file or no new `id`), both scripts do a single `JsonIO.read` check every 5 ticks.

## Quick Start

From the instance `minecraft/` directory while the game is running in a world:

```powershell
# Check client & server state
.\testing\bridge.ps1 client '{"cmd":"ping"}'
.\testing\bridge.ps1 server '{"cmd":"ping"}'

# Prevent singleplayer from pausing when the game window is unfocused
.\testing\bridge.ps1 client '{"cmd":"pauseOnLostFocus","value":false}'
.\testing\bridge.ps1 client '{"cmd":"closeScreen"}'

# Teleport across dimensions, hide HUD, and take a screenshot
.\testing\bridge.ps1 server '{"cmd":"command","command":"gamemode spectator @p"}'
.\testing\bridge.ps1 server '{"cmd":"tp","dim":"minecraft:the_nether","x":0,"y":70,"z":0,"yaw":45,"pitch":10}'
.\testing\bridge.ps1 server '{"cmd":"findView","yMin":36,"yMax":108,"radius":48,"step":8}'
.\testing\bridge.ps1 client '{"cmd":"hideHud","hide":true}'
.\testing\bridge.ps1 client '{"cmd":"screenshot","file":"nether_test.png"}'
.\testing\bridge.ps1 client '{"cmd":"hideHud","hide":false}'
```

Screenshots are saved to `screenshots/<file>` via `net.minecraft.client.Screenshot.grab`.

## Supported Commands

### Server (`.\testing\bridge.ps1 server '<json>'`)

| `cmd` | Parameters | Description |
|---|---|---|
| `ping` | — | Returns server tick and online players (`name`, `dim`, `x`, `y`, `z`, `yaw`, `pitch`). |
| `command` | `command` (string) | Runs a server command silently via `server.runCommandSilent(...)`. |
| `tp` | `dim`, `x`, `y`, `z`, `yaw`, `pitch`, `player` (all optional) | Cross-dimension teleport via `execute in <dim> run tp ...`. |
| `findView` | `x`, `y`, `z`, `yMin`, `yMax`, `radius` (48), `step` (8), `allowFluid` (false), `apply` (true) | Scans around `(x, y, z)` for an open pocket with the longest unobstructed forward sightline and teleports the player there. Useful for cave/underground dimensions. |

### Client (`.\testing\bridge.ps1 client '<json>'`)

| `cmd` | Parameters | Description |
|---|---|---|
| `ping` | — | Returns `inWorld`, `dim`, coords, rotation, `hideGui`, `paused`, `pauseOnLostFocus`, `screen`, window `width`/`height`. |
| `hideHud` | `hide` (bool) | Toggles `mc.options.hideGui` (same as F1). |
| `pauseOnLostFocus` | `value` (bool) | Sets `mc.options.pauseOnLostFocus` so background automation doesn't pause singleplayer. |
| `closeScreen` | — | Calls `mc.setScreen(null)` to dismiss pause/menu screens. |
| `fov` | `value` (number) | Sets `mc.options.fov()`. |
| `look` | `yaw`, `pitch` (numbers) | Sets client player rotation. |
| `screenshot` | `file` (string) | Captures the main render target to `screenshots/<file>`. |

## Hot-Reloading Without Restarting

If you edit either bridge script while Minecraft is running, you can hot-reload via KubeJS's built-in local web server (configured in `kubejs/config/web_server.json`):

```powershell
$cfg = Get-Content kubejs/config/web_server.json | ConvertFrom-Json
$hdr = @{ Authorization = "Bearer $($cfg.auth)" }
Invoke-RestMethod -Uri "http://127.0.0.1:$($cfg.port)/api/reload/client" -Method Post -Headers $hdr
Invoke-RestMethod -Uri "http://127.0.0.1:$($cfg.port)/api/reload/server" -Method Post -Headers $hdr
```

Client reload takes ~1–2s; server reload takes ~10–15s on the full pack.

## Extending the Bridge (KubeJS 7 / Rhino Notes)

When adding new commands to `handle()` in either script:
- **No `java.nio.file` or `java.io`:** KubeJS's class filter blocks them. Use KubeJS's built-in `JsonIO.read` / `JsonIO.write` (and `mc.gameDirectory` if a method needs a `File`).
- **Use `var` inside `handle()`:** Rhino hoists `const`/`let` across sibling `if` blocks in the same function and throws `redeclaration of var` if two branches declare the same name.
- **Player properties:** On KubeJS 7 (MC 1.21.1), use `p.level`, `p.x`, `p.y`, `p.z`, `p.yaw`, `p.pitch` rather than `p.level()` / `p.getYRot()`.
- **Unpaused singleplayer:** If `server` commands time out while `client` responds, check `client ping` — singleplayer pauses `ServerEvents.tick` whenever `PauseScreen` is open. Send `pauseOnLostFocus: false` + `closeScreen`.
