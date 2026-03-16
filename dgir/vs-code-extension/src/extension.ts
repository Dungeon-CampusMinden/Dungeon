import * as vscode from "vscode";
import * as childProcess from "child_process";
import * as fs from "fs";
import * as net from "net";
import * as path from "path";

/**
 * Debug configuration shape accepted by the DGIR debug extension.
 *
 * <p>These keys are surfaced in package.json and are read/filled by the configuration provider.
 * Missing fields are given sensible defaults at launch time.
 */
interface DgirDebugConfiguration extends vscode.DebugConfiguration {
  program?: string;
  jarPath?: string;
  javaPath?: string;
  dapPort?: number;
  host?: string;
  startServer?: boolean;
  stopOnEntry?: boolean;
  sourceFileMap?: Record<string, string>;
  useTerminal?: boolean;
  stopServerOnExit?: boolean;
}

/** Default DAP port the DGIR VM server listens on. */
const DEFAULT_DAP_PORT = 4711;
/** Default host for the DAP TCP server. */
const DEFAULT_HOST = "127.0.0.1";

/**
 * Track child processes when the server is launched without a terminal.
 * The key is the VS Code debug session ID.
 */
const sessionProcesses = new Map<string, childProcess.ChildProcess>();
/** Output channels tied to background server processes. */
const sessionOutputs = new Map<string, vscode.OutputChannel>();
/** Guard against reporting non-zero exits when the user stops a session. */
const sessionTerminating = new Set<string>();
/** Shared terminal for interactive runs (stdin/stdout). */
let sharedTerminal: vscode.Terminal | undefined;

/** Extension activation entry point. Registers providers and commands. */
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

      // Build a debug configuration from the current editor file.
      const config = await DgirDebugConfigurationProvider.buildFromEditor(editor.document);
      if (!config) {
        vscode.window.showErrorMessage("Unable to create DGIR debug configuration.");
        return;
      }

      // Start a DGIR debug session with the generated configuration.
      await vscode.debug.startDebugging(undefined, config);
    })
  );

  context.subscriptions.push(
    vscode.debug.onDidTerminateDebugSession((session) => {
      // Mark as user-initiated termination to avoid spurious error messages.
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
        // Send Ctrl+C to stop the shared terminal server process.
        sharedTerminal.sendText("\u0003", false);
      }
      // Keep terminal open for inspection after the run ends.
    })
  );
}

/** Extension deactivation: ensures any spawned processes are cleaned up. */
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

/**
 * Debug configuration provider: fills missing defaults and validates configuration values.
 *
 * <p>This runs every time a debug session starts and can adjust the configuration before
 * VS Code passes it to the debug adapter.
 */
class DgirDebugConfigurationProvider implements vscode.DebugConfigurationProvider {
  async resolveDebugConfiguration(
    folder: vscode.WorkspaceFolder | undefined,
    config: DgirDebugConfiguration
  ): Promise<vscode.DebugConfiguration | null | undefined> {
    // Ensure the required DAP fields are set so VS Code knows which adapter to use.
    if (!config.type) {
      config.type = "dgir";
    }
    if (!config.request) {
      config.request = "launch";
    }
    if (!config.name) {
      config.name = "Debug DGIR Program";
    }

    // If the user did not set a program, try to infer it from the active editor file.
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

    // Apply defaults for connection and server lifecycle options.
    config.host = config.host ?? DEFAULT_HOST;
    config.dapPort = config.dapPort ?? DEFAULT_DAP_PORT;
    config.startServer = config.startServer ?? true;
    config.stopServerOnExit = config.stopServerOnExit ?? true;

    // Default java executable if not provided.
    if (!config.javaPath) {
      config.javaPath = "java";
    }

    // Infer the jar location if omitted (assumes standard build layout).
    if (!config.jarPath) {
      const workspaceRoot = folder?.uri.fsPath ?? vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
      if (workspaceRoot) {
        config.jarPath = path.join(
          workspaceRoot,
          "dgir-vm-dap.jar"
        );
      }
    }

    return config;
  }

  /**
   * Build a minimal launch configuration from the active editor document.
   * Supports direct DGIR JSON files and Java files with a matching JSON neighbor.
   */
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

/**
 * Debug adapter descriptor factory: starts or attaches to the DGIR DAP server.
 *
 * <p>When startServer is true, we spawn the server either in a terminal (interactive)
 * or in the background with an output channel.
 */
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

      // Build the Java command line for the DAP server.
      const args: string[] = ["-jar", config.jarPath, "--dap-port", String(port)];
      if (config.program) {
        args.push("--program", config.program);
      }

      if (useTerminal) {
        // Run in a terminal for interactive stdin/stdout.
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
        // Run in the background and stream output to a VS Code output channel.
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

      // Wait until the server is reachable before attaching the debug adapter.
      await waitForPort(host, port, 6000);
    }

    // VS Code will connect to the DAP server over TCP.
    return new vscode.DebugAdapterServer(port, host);
  }
}

/**
 * Debug adapter tracker: rewrites source paths to and from the DAP server.
 *
 * <p>The server uses paths from debug info. We assume those are workspace-relative.
 * This tracker maps breakpoint requests into relative paths and resolves response
 * paths back to absolute workspace paths for VS Code.
 */
class DgirDebugAdapterTrackerFactory implements vscode.DebugAdapterTrackerFactory {
  createDebugAdapterTracker(session: vscode.DebugSession): vscode.DebugAdapterTracker {
    const config = session.configuration as DgirDebugConfiguration;
    const sourceFileMap = config.sourceFileMap ?? {};
    const reverseMap = new Map<string, string>();

    // Create reverse mapping (DGIR -> user source) for response rewrites.
    for (const [key, value] of Object.entries(sourceFileMap)) {
      reverseMap.set(value, key);
    }

    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;

    return {
      onWillReceiveMessage: (message) => {
        if (message?.command === "setBreakpoints" || message?.command === "breakpointLocations") {
          // Convert VS Code absolute paths to workspace-relative paths for the server.
          mapSourcePathToDgir(message?.arguments, sourceFileMap, workspaceRoot);
        }
      },
      onDidSendMessage: (message) => {
        // Convert server paths (relative) back into absolute paths for VS Code UI.
        mapSourcesInResponse(message, reverseMap, workspaceRoot);
      }
    };
  }
}

/**
 * Map incoming breakpoint requests from VS Code to workspace-relative paths for the server.
 */
function mapSourcePathToDgir(
  args: { source?: { path?: string } } | undefined,
  sourceFileMap: Record<string, string>,
  workspaceRoot?: string
) {
  if (!args?.source?.path || args.source.path === "<unknown>") return;
  const rawPath = args.source.path;
  const srcPath = path.isAbsolute(rawPath)
    ? rawPath
    : workspaceRoot
      ? path.join(workspaceRoot, rawPath)
      : rawPath;

  // Respect explicit mappings, if present.
  if (sourceFileMap[srcPath]) {
    args.source.path = sourceFileMap[srcPath];
    return;
  }

  // Default: if the path is under the workspace, send a relative path to the server.
  if (workspaceRoot && path.isAbsolute(srcPath) && srcPath.startsWith(workspaceRoot + path.sep)) {
    const rel = path.relative(workspaceRoot, srcPath);
    args.source.path = rel;
    sourceFileMap[srcPath] = rel;
  }
}

/**
 * Rewrite paths in DAP responses (stack frames, breakpoints, locations).
 */
function mapSourcesInResponse(
  message: any,
  reverseMap: Map<string, string>,
  workspaceRoot?: string
) {
  if (!message) return;

  if (message.type === "response" && message.command === "stackTrace") {
    const frames = message.body?.stackFrames ?? [];
    for (const frame of frames) {
      mapSource(frame?.source, reverseMap, workspaceRoot);
    }
  }

  if (message.type === "response" && message.command === "setBreakpoints") {
    const bps = message.body?.breakpoints ?? [];
    for (const bp of bps) {
      mapSource(bp?.source, reverseMap, workspaceRoot);
    }
  }

  if (message.type === "response" && message.command === "breakpointLocations") {
    const bps = message.body?.breakpoints ?? [];
    for (const bp of bps) {
      mapSource(bp?.source, reverseMap, workspaceRoot);
    }
  }
}

/**
 * Resolve a single DAP source entry to an absolute path (if possible).
 */
function mapSource(
  source: { path?: string; name?: string } | undefined,
  reverseMap: Map<string, string>,
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
    const resolved = resolveRelativeSource(source.path, workspaceRoot);
    if (resolved) {
      source.path = resolved;
    }
  }
}

/**
 * Resolve a workspace-relative path to an absolute path on disk.
 */
function resolveRelativeSource(
  relativePath: string,
  workspaceRoot?: string
): string | undefined {
  if (workspaceRoot) {
    const fromWorkspace = path.join(workspaceRoot, relativePath);
    if (fs.existsSync(fromWorkspace)) return fromWorkspace;
  }
  return undefined;
}

/** Attempt to connect to the DAP server until it becomes reachable or times out. */
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

/** Wait for the DAP port to close, used to avoid races when reusing the terminal. */
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

/** Probe the DAP port; returns true if a TCP connection can be established. */
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

/** Promise-based delay helper for polling. */
function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Quote a shell argument for a simple POSIX-style terminal invocation. */
function shellQuote(value: string): string {
  if (/^[A-Za-z0-9_./:-]+$/.test(value)) return value;
  return `"${value.replace(/"/g, "\\\"")}"`;
}

/** Create or reuse the shared terminal used for the DGIR server. */
function getOrCreateTerminal(): vscode.Terminal {
  if (sharedTerminal) return sharedTerminal;
  sharedTerminal = vscode.window.createTerminal({
    name: "DGIR VM"
  });
  return sharedTerminal;
}
