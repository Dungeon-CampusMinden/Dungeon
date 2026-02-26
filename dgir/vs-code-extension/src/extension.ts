import * as vscode from "vscode";
import * as childProcess from "child_process";
import * as fs from "fs";
import * as net from "net";
import * as path from "path";

interface DgirDebugConfiguration extends vscode.DebugConfiguration {
  program?: string;
  jarPath?: string;
  javaPath?: string;
  dapPort?: number;
  host?: string;
  startServer?: boolean;
  stopOnEntry?: boolean;
  autoBasenameSourceMap?: boolean;
  sourceFileMap?: Record<string, string>;
  useTerminal?: boolean;
  stopServerOnExit?: boolean;
}

const DEFAULT_DAP_PORT = 4711;
const DEFAULT_HOST = "127.0.0.1";

const sessionProcesses = new Map<string, childProcess.ChildProcess>();
const sessionOutputs = new Map<string, vscode.OutputChannel>();
const sessionTerminating = new Set<string>();
let sharedTerminal: vscode.Terminal | undefined;

export function activate(context: vscode.ExtensionContext) {
  context.subscriptions.push(
    vscode.debug.registerDebugConfigurationProvider(
      "dgir",
      new DgirDebugConfigurationProvider()
    )
  );

  context.subscriptions.push(
    vscode.debug.registerDebugAdapterDescriptorFactory(
      "dgir",
      new DgirDebugAdapterDescriptorFactory()
    )
  );

  context.subscriptions.push(
    vscode.debug.registerDebugAdapterTrackerFactory(
      "dgir",
      new DgirDebugAdapterTrackerFactory()
    )
  );

  context.subscriptions.push(
    vscode.commands.registerCommand("dgir.debugCurrentFile", async () => {
      const editor = vscode.window.activeTextEditor;
      if (!editor) {
        vscode.window.showErrorMessage("No active editor found.");
        return;
      }

      const config = await DgirDebugConfigurationProvider.buildFromEditor(editor.document);
      if (!config) {
        vscode.window.showErrorMessage("Unable to create DGIR debug configuration.");
        return;
      }

      await vscode.debug.startDebugging(undefined, config);
    })
  );

  context.subscriptions.push(
    vscode.debug.onDidTerminateDebugSession((session) => {
      sessionTerminating.add(session.id);
      const child = sessionProcesses.get(session.id);
      if (child) {
        child.kill();
        sessionProcesses.delete(session.id);
      }
      const config = session.configuration as DgirDebugConfiguration;
      const useTerminal = config.useTerminal ?? true;
      const stopServerOnExit = config.stopServerOnExit ?? true;
      if (useTerminal && stopServerOnExit && sharedTerminal) {
        sharedTerminal.sendText("\u0003", false);
      }
      // Keep terminal open for inspection after the run ends.
    })
  );
}

export function deactivate() {
  for (const child of sessionProcesses.values()) {
    child.kill();
  }
  sessionProcesses.clear();

  for (const output of sessionOutputs.values()) {
    output.dispose();
  }
  sessionOutputs.clear();

  sharedTerminal?.dispose();
  sharedTerminal = undefined;
  sessionTerminating.clear();
}

class DgirDebugConfigurationProvider implements vscode.DebugConfigurationProvider {
  async resolveDebugConfiguration(
    folder: vscode.WorkspaceFolder | undefined,
    config: DgirDebugConfiguration
  ): Promise<vscode.DebugConfiguration | null | undefined> {
    if (!config.type) {
      config.type = "dgir";
    }
    if (!config.request) {
      config.request = "launch";
    }
    if (!config.name) {
      config.name = "Debug DGIR Program";
    }

    if (!config.program) {
      const editor = vscode.window.activeTextEditor;
      if (editor) {
        const inferred = await DgirDebugConfigurationProvider.buildFromEditor(editor.document);
        if (inferred?.program) {
          config.program = inferred.program;
        }
      }
    }

    if (!config.program) {
      vscode.window.showErrorMessage("DGIR: Missing 'program' (DGIR JSON path).");
      return null;
    }

    config.host = config.host ?? DEFAULT_HOST;
    config.dapPort = config.dapPort ?? DEFAULT_DAP_PORT;
    config.startServer = config.startServer ?? true;
    config.autoBasenameSourceMap = config.autoBasenameSourceMap ?? true;
    config.stopServerOnExit = config.stopServerOnExit ?? true;

    if (!config.javaPath) {
      config.javaPath = "java";
    }

    if (!config.jarPath) {
      const workspaceRoot = folder?.uri.fsPath ?? vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
      if (workspaceRoot) {
        config.jarPath = path.join(
          workspaceRoot,
          "dgir",
          "vm",
          "build",
          "libs",
          "dgir-vm-dap.jar"
        );
      }
    }

    if (config.useTerminal ?? true) {
      if (!config.internalConsoleOptions) {
        config.internalConsoleOptions = "neverOpen";
      }
    }

    return config;
  }

  static async buildFromEditor(
    document: vscode.TextDocument
  ): Promise<DgirDebugConfiguration | null> {
    const filePath = document.uri.fsPath;
    const ext = path.extname(filePath).toLowerCase();

    if (ext === ".json" || ext === ".dgir") {
      return {
        type: "dgir",
        name: "Debug DGIR Program",
        request: "launch",
        program: filePath
      };
    }

    if (ext === ".java") {
      const baseName = path.basename(filePath, ".java");
      const jsonCandidate = path.join(path.dirname(filePath), `${baseName}.json`);
      if (fs.existsSync(jsonCandidate)) {
        return {
          type: "dgir",
          name: "Debug DGIR Program",
          request: "launch",
          program: jsonCandidate
        };
      }
    }

    return null;
  }
}

class DgirDebugAdapterDescriptorFactory
  implements vscode.DebugAdapterDescriptorFactory {
  async createDebugAdapterDescriptor(
    session: vscode.DebugSession
  ): Promise<vscode.DebugAdapterDescriptor> {
    const config = session.configuration as DgirDebugConfiguration;
    const host = config.host ?? DEFAULT_HOST;
    const port = config.dapPort ?? DEFAULT_DAP_PORT;
    const useTerminal = config.useTerminal ?? true;

    if (config.startServer !== false) {
      if (!config.jarPath || !fs.existsSync(config.jarPath)) {
        throw new Error(
          `DGIR: jar not found at ${config.jarPath ?? "(undefined)"}. Build with ./gradlew :dgir:vm:dapJar`
        );
      }

      const args: string[] = ["-jar", config.jarPath, "--dap-port", String(port)];
      if (config.program) {
        args.push("--program", config.program);
      }

      if (useTerminal) {
        const terminal = getOrCreateTerminal();
        terminal.show(true);
        // Ensure any prior server in the shared terminal is stopped before starting a new one.
        terminal.sendText("\u0003", false);
        await waitForPortClose(host, port, 3000);
        await delay(200);
        terminal.sendText(
          [shellQuote(config.javaPath ?? "java"), ...args.map(shellQuote)].join(" "),
          true
        );
      } else {
        const child = childProcess.spawn(config.javaPath ?? "java", args, {
          stdio: "pipe"
        });

        const output = vscode.window.createOutputChannel(`DGIR VM (${session.name})`);
        sessionOutputs.set(session.id, output);
        output.show(true);

        child.stdout?.on("data", (chunk: Buffer | string) => {
          output.append(chunk.toString());
        });
        child.stderr?.on("data", (chunk: Buffer | string) => {
          output.append(chunk.toString());
        });

        child.on("exit", (code, signal) => {
          const terminatedByUser =
            signal === "SIGTERM" || code === 143 || sessionTerminating.has(session.id);
          if (code !== 0 && !terminatedByUser) {
            vscode.window.showErrorMessage(`DGIR DAP server exited with code ${code}.`);
          }
        });

        sessionProcesses.set(session.id, child);
      }

      await waitForPort(host, port, 6000);
    }

    return new vscode.DebugAdapterServer(port, host);
  }
}

class DgirDebugAdapterTrackerFactory implements vscode.DebugAdapterTrackerFactory {
  createDebugAdapterTracker(session: vscode.DebugSession): vscode.DebugAdapterTracker {
    const config = session.configuration as DgirDebugConfiguration;
    const sourceFileMap = config.sourceFileMap ?? {};
    const reverseMap = new Map<string, string>();

    for (const [key, value] of Object.entries(sourceFileMap)) {
      reverseMap.set(value, key);
    }

    const autoMap = config.autoBasenameSourceMap ?? true;

    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
    const programDir = config.program ? path.dirname(config.program) : undefined;

    return {
      onWillReceiveMessage: (message) => {
        if (message?.command === "setBreakpoints" || message?.command === "breakpointLocations") {
          mapSourcePathToDgir(message?.arguments, sourceFileMap, autoMap, config.program);
        }
      },
      onDidSendMessage: (message) => {
        mapSourcesInResponse(message, reverseMap, programDir, workspaceRoot);
      }
    };
  }
}

function mapSourcePathToDgir(
  args: { source?: { path?: string } } | undefined,
  sourceFileMap: Record<string, string>,
  autoMap: boolean,
  program?: string
) {
  if (!args?.source?.path || args.source.path === "<unknown>") return;
  const srcPath = path.resolve(args.source.path);

  if (sourceFileMap[srcPath]) {
    args.source.path = sourceFileMap[srcPath];
    return;
  }

  if (autoMap) {
    const baseName = path.basename(srcPath, path.extname(srcPath));
    const javaName = `${baseName}.java`;
    args.source.path = javaName;
    sourceFileMap[srcPath] = javaName;
    return;
  }

  if (program) {
    args.source.path = path.basename(program, path.extname(program)) + ".dgir";
  }
}

function mapSourcesInResponse(
  message: any,
  reverseMap: Map<string, string>,
  programDir?: string,
  workspaceRoot?: string
) {
  if (!message) return;

  if (message.type === "response" && message.command === "stackTrace") {
    const frames = message.body?.stackFrames ?? [];
    for (const frame of frames) {
      mapSource(frame?.source, reverseMap, programDir, workspaceRoot);
    }
  }

  if (message.type === "response" && message.command === "setBreakpoints") {
    const bps = message.body?.breakpoints ?? [];
    for (const bp of bps) {
      mapSource(bp?.source, reverseMap, programDir, workspaceRoot);
    }
  }

  if (message.type === "response" && message.command === "breakpointLocations") {
    const bps = message.body?.breakpoints ?? [];
    for (const bp of bps) {
      mapSource(bp?.source, reverseMap, programDir, workspaceRoot);
    }
  }
}

function mapSource(
  source: { path?: string; name?: string } | undefined,
  reverseMap: Map<string, string>,
  programDir?: string,
  workspaceRoot?: string
) {
  if (!source?.path) return;
  if (source.path === "<unknown>") {
    // Avoid VS Code trying to load a synthetic path that can't exist.
    delete source.path;
    source.name = source.name ?? "<unknown>";
    return;
  }

  const mapped = reverseMap.get(source.path);
  if (mapped) {
    source.path = mapped;
    return;
  }

  if (!path.isAbsolute(source.path)) {
    const resolved = resolveRelativeSource(source.path, programDir, workspaceRoot);
    if (resolved) {
      source.path = resolved;
    }
  }
}

function resolveRelativeSource(
  relativePath: string,
  programDir?: string,
  workspaceRoot?: string
): string | undefined {
  if (programDir) {
    const fromProgram = path.join(programDir, relativePath);
    if (fs.existsSync(fromProgram)) return fromProgram;
  }
  if (workspaceRoot) {
    const fromWorkspace = path.join(workspaceRoot, relativePath);
    if (fs.existsSync(fromWorkspace)) return fromWorkspace;
  }
  return undefined;
}

function waitForPort(host: string, port: number, timeoutMs: number): Promise<void> {
  const start = Date.now();

  return new Promise((resolve, reject) => {
    const tryConnect = () => {
      const socket = new net.Socket();
      socket.setTimeout(1000);

      socket.once("error", () => {
        socket.destroy();
        if (Date.now() - start > timeoutMs) {
          reject(new Error(`Timed out waiting for ${host}:${port}`));
          return;
        }
        setTimeout(tryConnect, 200);
      });

      socket.once("timeout", () => {
        socket.destroy();
        if (Date.now() - start > timeoutMs) {
          reject(new Error(`Timed out waiting for ${host}:${port}`));
          return;
        }
        setTimeout(tryConnect, 200);
      });

      socket.connect(port, host, () => {
        socket.end();
        resolve();
      });
    };

    tryConnect();
  });
}

async function waitForPortClose(host: string, port: number, timeoutMs: number): Promise<void> {
  const start = Date.now();
  while (true) {
    const open = await canConnect(host, port, 500);
    if (!open) return;
    if (Date.now() - start > timeoutMs) {
      throw new Error(`Port ${port} is still in use on ${host}`);
    }
    await delay(200);
  }
}

function canConnect(host: string, port: number, timeoutMs: number): Promise<boolean> {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    socket.setTimeout(timeoutMs);

    socket.once("error", () => {
      socket.destroy();
      resolve(false);
    });

    socket.once("timeout", () => {
      socket.destroy();
      resolve(false);
    });

    socket.connect(port, host, () => {
      socket.end();
      resolve(true);
    });
  });
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function shellQuote(value: string): string {
  if (/^[A-Za-z0-9_./:-]+$/.test(value)) return value;
  return `"${value.replace(/"/g, "\\\"")}"`;
}

function getOrCreateTerminal(): vscode.Terminal {
  if (sharedTerminal) return sharedTerminal;
  sharedTerminal = vscode.window.createTerminal({
    name: "DGIR VM"
  });
  return sharedTerminal;
}
