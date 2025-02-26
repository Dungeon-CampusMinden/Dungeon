REM Compile for windows x86_64
deno compile --target x86_64-pc-windows-msvc --allow-net --output blockly_x86_64.exe --allow-read --allow-run --no-npm --icon ./content/favicon.ico webserver.ts
