#!/usr/bin/env bash
# Throwaway singleplayer smoke. The world lives in this mod's run/ directory.
# It is not a player world and it is never copied out of run/.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

WORLD="${1:-smoke}"
RUN="$ROOT/run"
SAVES="$RUN/saves/$WORLD"
LOG_DIR="${SMOKE_LOG_DIR:-/tmp/neepmeat-smoke}"
mkdir -p "$LOG_DIR"
SERVER_LOG="$LOG_DIR/server-$WORLD.log"
CLIENT_LOG="$LOG_DIR/client-$WORLD.log"

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "xvfb-run is not installed" >&2
  exit 1
fi

wait_for() {
  local file="$1" pattern="$2" seconds="$3" pid="$4"
  local i
  for ((i = 0; i < seconds; i++)); do
    if grep -a -q "$pattern" "$file" 2>/dev/null; then
      return 0
    fi
    if ! kill -0 "$pid" 2>/dev/null; then
      return 1
    fi
    sleep 1
  done
  return 1
}

stop_tree() {
  local pid="$1"
  # Gradle's game is a grandchild. Kill the dev launch and the wrapper together.
  pkill -TERM -P "$pid" 2>/dev/null || true
  if kill -0 "$pid" 2>/dev/null; then
    kill -INT "$pid" 2>/dev/null || true
  fi
  local i
  for ((i = 0; i < 15; i++)); do
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  pkill -KILL -P "$pid" 2>/dev/null || true
  kill -KILL "$pid" 2>/dev/null || true
  pkill -KILL -f 'net.neoforged.devlaunch.Main' 2>/dev/null || true
}

if [[ ! -f "$SAVES/level.dat" ]]; then
  echo "Generating throwaway world '$WORLD' with the dedicated server"
  : >"$SERVER_LOG"
  ./gradlew runServer --offline -PsmokeLevel="$WORLD" >"$SERVER_LOG" 2>&1 &
  server_pid=$!
  if wait_for "$SERVER_LOG" "Done (" 150 "$server_pid"; then
    echo "Server finished preparing spawn"
  else
    echo "Server did not report Done; see $SERVER_LOG" >&2
  fi
  stop_tree "$server_pid"
  wait "$server_pid" 2>/dev/null || true
  mkdir -p "$RUN/saves"
  if [[ -f "$RUN/$WORLD/level.dat" ]]; then
    rm -rf "$SAVES"
    cp -a "$RUN/$WORLD" "$SAVES"
  fi
fi

if [[ ! -f "$SAVES/level.dat" ]]; then
  echo "No level.dat at $SAVES" >&2
  exit 1
fi

if [[ ! -f "$RUN/options.txt" ]]; then
  printf 'fullscreen:false\noverrideWidth:854\noverrideHeight:480\n' >"$RUN/options.txt"
fi
export vblank_mode=0
export __GL_SYNC_TO_VBLANK=0

echo "Launching the client into '$WORLD'"
: >"$CLIENT_LOG"
xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runClient --offline -PquickPlaySingleplayer="$WORLD" >"$CLIENT_LOG" 2>&1 &
client_pid=$!
joined=0
if wait_for "$CLIENT_LOG" "logged in with entity id" 180 "$client_pid"; then
  joined=1
  echo "Player joined; waiting 15s for ticks"
  sleep 15
fi
stop_tree "$client_pid"
wait "$client_pid" 2>/dev/null || true

echo "----- smoke log: $CLIENT_LOG -----"
if [[ "$joined" -ne 1 ]]; then
  echo "Player did not join" >&2
  rg -n "Exception|ERROR|Caused by|logged in|Fatal" "$CLIENT_LOG" | tail -40 || true
  exit 1
fi

join_line="$(grep -a -n "logged in with entity id" "$CLIENT_LOG" | head -1 | cut -d: -f1)"
if tail -n +"$join_line" "$CLIENT_LOG" | grep -a -E "Exception caught|Exception in thread|Internal Exception|Unexpected error" | grep -v "Failed to open OpenAL device"; then
  echo "Client log contains an exception after the player joined" >&2
  exit 1
fi

echo "Joined '$WORLD' and stayed up. Log: $CLIENT_LOG"
rg -n "logged in with entity id|neoforge\\.[0-9]+ loaded|Turning off sounds" "$CLIENT_LOG" | head -20
