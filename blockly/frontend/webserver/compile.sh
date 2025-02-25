#!/bin/bash

# Compile for linux x86_64
deno compile --target x86_64-unknown-linux-gnu --allow-net -output blockly_x86_64.bin --allow-read --allow-run --no-npm webserver.ts
# Compile for linux arm64
deno compile --target aarch64-unknown-linux-gnu --allow-net --output blockly_arm64.bin --allow-read --allow-run --no-npm webserver.ts
