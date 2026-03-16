# DGIR Debugger VS Code Extension

Debug DGIR programs (JSON IR) using the `DgirDebugServer` shipped with the DGIR VM.

## Features

- Start `DgirDebugServer` automatically for a DGIR JSON program.
- Attach VS Code to the DAP server on a configurable port.
- Optional source mapping to debug Java source files (e.g., `HelloWorld.java`).

## Prerequisites

1. Build the DGIR DAP server jar:

```bash
./gradlew :dgir:vm:dapJar
```

2. Make sure you have a DGIR JSON program (e.g., `dgir/vm/test_assets/HelloWorld.json`).

## Quick Start

Create `.vscode/launch.json` in your workspace:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "dgir",
      "name": "Debug DGIR Program",
      "request": "launch",
      "program": "${workspaceFolder}/dgir/vm/test_assets/HelloWorld.json",
      "jarPath": "${workspaceFolder}/dgir/vm/build/libs/dgir-vm-dap.jar",
      "dapPort": 4711,
      "stopOnEntry": true
    }
  ]
}
```

Then press F5.

## Configuration

- `program`: Path to the compiled DGIR JSON program (required).
- `jarPath`: Path to `dgir-vm-dap.jar` (built via `:dgir:vm:dapJar`).
- `javaPath`: Java executable to launch the server (default: `java`).
- `dapPort`: Port used by the DAP server (default: `4711`).
- `host`: DAP host (default: `127.0.0.1`).
- `startServer`: Start `DgirDebugServer` automatically (default: `true`).
- `useTerminal`: Run the server in a VS Code terminal for interactive stdin/stdout (default: `true`).
- `stopServerOnExit`: Send Ctrl+C to stop the server when the debug session ends (default: `true`).
- `internalConsoleOptions`: Set to `neverOpen` to prevent the Debug Console from opening (default when `useTerminal` is `true`).
- `sourceFileMap`: Map Java source paths to DGIR source paths.
- `autoBasenameSourceMap`: If true, auto-map `Foo.java` to `Foo.dgir` when needed.

## Source Mapping Notes

By default, the DGIR JSON produced by the compiler uses Java filenames in `loc` (e.g.,
`HelloWorld.java:3:8`). The extension resolves these relative filenames to absolute paths
based on the program directory or workspace root so VS Code can open the file.

If your DGIR `loc` values use different filenames, set `sourceFileMap` explicitly.

## VM Console I/O

- Output: When `useTerminal` is `true`, stdout/stderr appear in the `DGIR VM (<session name>)` terminal.
- Input: When `useTerminal` is `true`, type directly into that terminal for `ConsoleIn`.
- Output (non-terminal): When `useTerminal` is `false`, stdout/stderr stream to the Output panel.
- Input (non-terminal): Not available when `useTerminal` is `false` (terminal mode is recommended for input).

## Development

```bash
npm install
npm run compile
```

## Troubleshooting

If breakpoints do not bind, ensure the DGIR source locations in the JSON match the mapped paths.
