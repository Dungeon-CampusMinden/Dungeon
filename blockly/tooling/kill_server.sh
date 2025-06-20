#!/bin/bash

# Dieses Skript beendet Prozesse auf den Ports 8080 und 8081, zuerst mit SIGTERM und nach einer Wartezeit mit SIGKILL, falls sie noch laufen.

ports=(8080 8081)
pids_to_kill=()
timeout_seconds=5 # Wartezeit in Sekunden, bevor SIGKILL gesendet wird

for port in "${ports[@]}"; do
  pids=$(lsof -t -i:"$port" 2>/dev/null)
  if [ -n "$pids" ]; then
    for pid in $pids; do
      pids_to_kill+=("$pid")
    done
  fi
done

unique_pids=($(echo "${pids_to_kill[@]}" | tr ' ' '\n' | sort -u | tr '\n' ' '))

if [ ${#unique_pids[@]} -eq 0 ]; then
  echo "Keine Prozesse auf Port 8080 oder 8081 gefunden."
  exit 0
fi

echo "Versuche, folgende PIDs ordentlich zu beenden (SIGTERM): ${unique_pids[*]}"
kill "${unique_pids[@]}"

# Warte und prüfe, ob die Prozesse beendet wurden
sleep "$timeout_seconds"

still_running_pids=()
for pid in "${unique_pids[@]}"; do
  # Prüfe, ob der Prozess noch existiert
  if ps -p "$pid" > /dev/null 2>&1; then
    still_running_pids+=("$pid")
  fi
done

if [ ${#still_running_pids[@]} -gt 0 ]; then
  echo "Folgende PIDs haben nicht auf SIGTERM reagiert und werden nun mit SIGKILL beendet: ${still_running_pids[*]}"
  kill -9 "${still_running_pids[@]}"
  echo "Prozesse mit SIGKILL beendet."
else
  echo "Alle Prozesse wurden erfolgreich mit SIGTERM beendet."
fi
