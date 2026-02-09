#!/usr/bin/env sh
# Run tests with a hard timeout so the run never hangs.
# Usage: ./test-with-timeout.sh [timeout_seconds]   (default: 120)

TIMEOUT="${1:-120}"
./gradlew test --rerun-tasks &
PID=$!
# Kill test process after TIMEOUT seconds if still running
( sleep "$TIMEOUT"; kill -9 "$PID" 2>/dev/null ) &
KILLER=$!
wait "$PID"
EXIT=$?
kill $KILLER 2>/dev/null
if [ $EXIT -eq 137 ]; then
  echo "Tests timed out after ${TIMEOUT}s" 1>&2
  exit 124
fi
exit $EXIT
