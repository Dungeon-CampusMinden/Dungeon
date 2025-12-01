#!/bin/bash

# Kill background jobs when script exits
cleanup() {
    echo "Stopping servers..."
    kill $(jobs -p) 2>/dev/null
    echo "All servers stopped."
}
trap cleanup EXIT

read -p "Web Version (yes/no)? " answer

if [[ "$answer" == "yes" || "$answer" == "y" ]]; then
    echo "Starting Web Version..."
    ./gradlew runBlockly -Pweb=true &
    gradle_pid=$!

    echo "Starting Blockly frontend..."
    cd blockly/frontend || exit
    npm run dev &
    npm_pid=$!

    echo "Waiting 5 seconds before opening browser..."
    sleep 5
    xdg-open http://localhost:5173/ 2>/dev/null || open http://localhost:5173/

    echo "Press CTRL+C to stop everything"
    wait
else
    echo "Starting Java Dungeon..."
    ./gradlew runBlockly
fi
