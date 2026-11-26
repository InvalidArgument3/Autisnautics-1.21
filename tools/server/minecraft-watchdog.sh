#!/bin/bash
# Restarts the Autisnautics server when a crash-time shutdown hangs.
#
# The minecraft unit is Type=forking: screen stays up while java is wedged inside
# "Stopping server" / "waiting for N chunks", so Restart=always never runs.
# TimeoutStopSec only applies when systemd itself is stopping the unit.
#
# Thresholds (override in the environment):
#   STOP_MINUTES   default 8   "Stopping server" and java still alive
#   START_MINUTES  default 5   unit active, java never printed Done (startup is 1-2 min)
#   OOM_MINUTES    default 1   OutOfMemoryError after the last Done, java still alive
#
# A quiet healthy server does not write latest.log. Silence after "Done" is not a hang.

set -u

UNIT="${UNIT:-minecraft}"
LOG="${LOG:-/opt/minecraft/server/logs/latest.log}"
PORT="${PORT:-25565}"
STOP_MINUTES="${STOP_MINUTES:-8}"
START_MINUTES="${START_MINUTES:-5}"
OOM_MINUTES="${OOM_MINUTES:-1}"

log() {
    logger -t minecraft-watchdog -- "$*"
    printf '%s\n' "$*"
}

if ! systemctl is-active --quiet "$UNIT"; then
    exit 0
fi

active_stamp="$(systemctl show "$UNIT" -p ActiveEnterTimestamp --value || true)"
now="$(date +%s)"
active_epoch="$(date -d "$active_stamp" +%s 2>/dev/null || echo "$now")"
active_for="$(( now - active_epoch ))"

java_alive=0
if pgrep -u minecraft -x java >/dev/null 2>&1; then
    java_alive=1
fi

force_restart() {
    local reason="$1"
    log "restarting ${UNIT}: ${reason}"
    systemctl kill --kill-who=all -s SIGKILL "$UNIT" || true
    sleep 2
    if ! systemctl is-active --quiet "$UNIT"; then
        systemctl reset-failed "$UNIT" || true
        systemctl start "$UNIT" || log "systemctl start ${UNIT} failed"
    fi
}

# screen without a java child: the forking unit looks active and will not restart itself
if [[ "$java_alive" -eq 0 && "$active_for" -ge $(( START_MINUTES * 60 )) ]]; then
    force_restart "unit active for ${active_for}s and no java process"
    exit 0
fi

if [[ ! -f "$LOG" ]]; then
    if [[ "$active_for" -ge $(( START_MINUTES * 60 )) ]]; then
        force_restart "no ${LOG} after ${active_for}s"
    fi
    exit 0
fi

# Last "Done (" is the successful boot marker. Anything after it belongs to this session.
done_line="$(grep -n 'Done (' "$LOG" | tail -n 1 | cut -d: -f1 || true)"
if [[ -z "$done_line" ]]; then
    done_line=0
fi

line_epoch() {
    local line="$1"
    local stamp
    stamp="$(printf '%s\n' "$line" | sed -n \
        -e 's/^\([0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\} [0-9:.]\{8,\}\).*/\1/p' \
        -e 's/^\[\([0-9]\{1,2\}[A-Za-z]\{3\}[0-9]\{4\} [0-9:.]\{8,\}\)\].*/\1/p' \
        -e 's/^\[\([0-9]\{2\}:[0-9]\{2\}:[0-9]\{2\}\)\].*/\1/p' \
        | head -n 1)"
    if [[ -z "$stamp" ]]; then
        echo 0
        return
    fi
    stamp="${stamp%%.*}"
    local parsed
    parsed="$(date -d "$stamp" +%s 2>/dev/null || echo 0)"
    if [[ "$parsed" -gt "$(date +%s)" ]]; then
        parsed="$(( parsed - 86400 ))"
    fi
    echo "$parsed"
}

listening=0
if ss -tln 2>/dev/null | grep -qE ":${PORT}[[:space:]]"; then
    listening=1
fi

if [[ "$done_line" -eq 0 && "$listening" -eq 0 && "$active_for" -ge $(( START_MINUTES * 60 )) ]]; then
    force_restart "startup produced no Done line after ${active_for}s"
    exit 0
fi

if [[ "$java_alive" -eq 1 ]]; then
    stop_line="$(awk -v start="$done_line" 'NR>start && /Stopping server/ { line=$0 } END { print line }' "$LOG")"
    if [[ -n "$stop_line" ]]; then
        stop_at="$(line_epoch "$stop_line")"
        if [[ "$stop_at" -gt 0 && $(( now - stop_at )) -ge $(( STOP_MINUTES * 60 )) ]]; then
            force_restart "Stopping server for $(( now - stop_at ))s and java is still alive"
            exit 0
        fi
    fi

    oom_line="$(awk -v start="$done_line" 'NR>start && /OutOfMemoryError/ { line=$0 } END { print line }' "$LOG")"
    if [[ -n "$oom_line" ]]; then
        oom_at="$(line_epoch "$oom_line")"
        if [[ "$oom_at" -eq 0 || $(( now - oom_at )) -ge $(( OOM_MINUTES * 60 )) ]]; then
            force_restart "OutOfMemoryError in the current session and java is still alive"
            exit 0
        fi
    fi
fi

exit 0
