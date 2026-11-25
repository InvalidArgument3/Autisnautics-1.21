# KubeJS Dev Bridge & Headless Testing Guide

Lightweight file-queue bridge and test harness for driving a running Minecraft client/singleplayer world from scripts, tests, or AI agents.

## Files

- `kubejs/server_scripts/dev_bridge.js` — polls `testing/bridge/server_req.json` every 5 server ticks and writes `testing/bridge/server_res.json`.
- `kubejs/client_scripts/dev_bridge_client.js` — polls `testing/bridge/client_req.json` every 5 client ticks and writes `testing/bridge/client_res.json`.
- `testing/bridge.ps1` — PowerShell CLI helper that stamps a request `id`, writes the request file, and waits for the matching response.
- `testing/launch-sp.ps1` — launches Prism into `AutisTest` with `-Headless` (muted + no Prism window) or `-ShowWindow` (normal UI + audio).
- `testing/run-smoke.ps1` — end-to-end smoke test runner (`-Headless`, `-ShowWindow`, `-KeepRunning`).
- `testing/stop-game.ps1` — stops Minecraft/Prism and restores `soundCategory_master` in `options.txt` if `-Headless` muted it.
- `testing/SparkReader.java` — parses local Spark `.sparkprofile` and `.sparkheap` protobuf dumps without uploading.

## Export Exclusion (Dev-Only)

This bridge is **dev/repo-only** and does not ship in exported client or server packs:
- `pakku.json` excludes `!kubejs/server_scripts/dev_bridge.js` and `!kubejs/client_scripts/dev_bridge_client.js` from `overrides`.
- `testing/` and `docs/` are not included in `overrides`, `client_overrides`, or `server_overrides`.
- `testing/bridge/` (runtime `*_req.json` / `*_res.json` / `prev_master_vol.txt` files) and `config/spark/*.sparkprofile` / `*.sparkheap` / `*.hprof` are gitignored.

When idle (no request file or no new `id`), both scripts do a single `JsonIO.read` check every 5 ticks.

## Launching Tests (Headless vs Windowed)

```powershell
# Headless (muted audio via options.txt + no Prism launcher window):
.\testing\launch-sp.ps1 -Headless

# Windowed (normal Prism window + audio):
.\testing\launch-sp.ps1 -ShowWindow

# Smoke test with headless mode:
.\testing\run-smoke.ps1 -Headless

# Always stop with stop-game.ps1 so soundCategory_master is restored:
.\testing\stop-game.ps1
```

### What `-Headless` Does
1. Saves the current `soundCategory_master` value from `options.txt` to `testing/bridge/prev_master_vol.txt` and sets `soundCategory_master:0.0` so the game is silent from startup (including loading/title screens).
2. Omits `--show-window` when invoking Prism Launcher.
3. Ensures `OverrideCommands=false` in `../instance.cfg` if `autis/updater.jar` is absent (dev instances bypass the updater).
4. `.\testing\stop-game.ps1` restores `soundCategory_master` from `prev_master_vol.txt` when done.

You can also mute/unmute live at runtime via the client bridge:
```powershell
.\testing\bridge.ps1 client '{"cmd":"mute","muted":true}'
.\testing\bridge.ps1 client '{"cmd":"mute","muted":false,"volume":1.0}'
```

## Quick Start (In-World)

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
| `worlds` | — | Returns loaded chunk counts per dimension. |

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
| `mute` | `muted` (bool), `volume` (number, optional) | Mutes/unmutes `SoundSource.MASTER` live. |
| `clientCommand` | `command` (string) | Runs a client-side command (e.g. `sparkc profiler start`) via `mc.player.connection.sendCommand(...)`. |

## Spark Profiling & Local Analysis

Spark (`spark-1.10.124-neoforge`) is included in the pack. Use the bridge to capture local `.sparkprofile` / `.sparkheap` snapshots without uploading, then parse them with `testing/SparkReader.java`:

```powershell
# Note: config/spark/config.json sets backgroundProfiler: true.
# Cancel any active background profiler first if starting a custom/alloc run:
.\testing\bridge.ps1 server '{"cmd":"command","command":"spark profiler cancel"}'
.\testing\bridge.ps1 server '{"cmd":"command","command":"spark profiler start --thread *"}'
# wait 25-60s while playing/idling
.\testing\bridge.ps1 server '{"cmd":"command","command":"spark profiler stop --save-to-file"}'

# Heap summary (writes config/spark/heapsummary-*.sparkheap):
.\testing\bridge.ps1 server '{"cmd":"command","command":"spark heapsummary --save-to-file"}'

# Parse locally (CPU profile filters out idle park/wait frames):
java -cp mods\spark-1.10.124-neoforge.jar testing\SparkReader.java config\spark\<file.sparkprofile> 25
java -cp mods\spark-1.10.124-neoforge.jar testing\SparkReader.java config\spark\<file.sparkheap> 30
```

## Agent Notes & Gotchas

- **Process lifetime in automated shells:** Launching Prism/Minecraft from a short-lived shell command may be terminated when that command exits (Windows job object cleanup). Keep the launch, wait loop, bridge commands, and `stop-game.ps1` inside one long-running shell invocation when automating end-to-end.
- **Dev updater bypass:** `autis_updater_setup-1.0.2.jar` patches `../instance.cfg` (`OverrideCommands=true`, `PreLaunchCommand=.../autis/updater.jar`) on game exit, but `autis/updater.jar` is not present in the dev repo. `launch-sp.ps1` automatically resets `OverrideCommands=false` when `autis/updater.jar` is missing.
- **Unpaused singleplayer:** If `server` commands time out while `client` responds, check `client ping` — singleplayer pauses `ServerEvents.tick` whenever `PauseScreen` is open. Send `pauseOnLostFocus: false` + `closeScreen`.
- **Fresh test world:** `ensure-test-world.ps1` clones `New World` if present; if a save lacks `level.dat`, delete `saves/AutisTest` and seed `saves/AutisTest/level.dat` from a valid save (without `region/`/`DIM*` for fresh worldgen).
- **KubeJS 7 / Rhino class filter:** `java.nio.file`, `java.io`, and `java.lang.Runtime` are blocked by KubeJS's class filter. Use `JsonIO.read` / `JsonIO.write`, and use `var` inside `handle()` (Rhino hoists `const`/`let` across sibling `if` blocks).
- **Hot-reloading scripts:** Edit either bridge script while running and POST to KubeJS's local web server (`kubejs/config/web_server.json`):
  ```powershell
  $cfg = Get-Content kubejs/config/web_server.json | ConvertFrom-Json
  $hdr = @{ Authorization = "Bearer $($cfg.auth)" }
  Invoke-RestMethod -Uri "http://127.0.0.1:$($cfg.port)/api/reload/client" -Method Post -Headers $hdr
  Invoke-RestMethod -Uri "http://127.0.0.1:$($cfg.port)/api/reload/server" -Method Post -Headers $hdr
  ```
