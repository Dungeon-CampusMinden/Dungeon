package dgir.vm.dap;

import dgir.vm.api.VM;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static dgir.dialect.builtin.BuiltinOps.ProgramOp;

/**
 * TCP server that accepts DAP client connections and wires each one to a {@link VM} via a {@link
 * DapAdapter}.
 *
 * <p>The server uses lsp4j's {@link DSPLauncher} to handle the DAP wire protocol (JSON-RPC over a
 * Content-Length–framed stream). Each accepted connection gets its own {@link DapAdapter} / {@link
 * VM} pair produced by the supplied factory.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * DapServer server = new DapServer(4711, () -> {
 *     VM vm = new VM();
 *     vm.init(buildMyProgram());
 *     return vm;
 * });
 * server.start();   // non-blocking; each connection is handled on its own daemon threads
 * }</pre>
 *
 * <h2>Connecting from VS Code</h2>
 *
 * Add an entry to {@code .vscode/launch.json}:
 *
 * <pre>{@code
 * {
 *   "type": "dgir",
 *   "request": "attach",
 *   "name": "Debug DGIR program",
 *   "port": 4711,
 *   "stopOnEntry": true
 * }
 * }</pre>
 */
public class DapServer implements AutoCloseable {

  private static final Logger LOG = Logger.getLogger(DapServer.class.getName());

  /** Conventional DAP port used by VS Code when no port is explicitly specified. */
  public static final int DEFAULT_PORT = 4711;

  private final int port;
  private final @NotNull Supplier<VM> vmFactory;
  private final @NotNull AtomicReference<ServerSocket> serverSocketRef = new AtomicReference<>();

  /**
   * The {@link DapAdapter} for the most recently accepted DAP client connection, or {@code null} if
   * no client has connected yet. Used by {@link #reloadProgram} to push a new program into an
   * active debug session without closing it.
   */
  private final @NotNull AtomicReference<DapAdapter> currentAdapter = new AtomicReference<>();

  /**
   * Create a server that listens on the given port.
   *
   * @param port the TCP port to listen on.
   * @param vmFactory factory that creates a fully {@link VM#init}-ed VM per debug session.
   */
  public DapServer(int port, @NotNull Supplier<VM> vmFactory) {
    this.port = port;
    this.vmFactory = vmFactory;
  }

  /**
   * Create a server on the {@link #DEFAULT_PORT default port} (4711).
   *
   * @param vmFactory factory that creates a fully {@link VM#init}-ed VM per debug session.
   */
  public DapServer(@NotNull Supplier<VM> vmFactory) {
    this(DEFAULT_PORT, vmFactory);
  }

  // =========================================================================
  // Lifecycle
  // =========================================================================

  /**
   * Open the listening socket and start the accept loop on a daemon thread.
   *
   * @throws IOException if the server socket cannot be bound.
   */
  public void start() throws IOException {
    ServerSocket ss = new ServerSocket(port);
    serverSocketRef.set(ss);
    LOG.info("DAP server listening on port " + ss.getLocalPort());

    Thread acceptThread =
        new Thread(
            () -> {
              try (ServerSocket ignored = ss) {
                while (!ss.isClosed()) {
                  Socket client;
                  try {
                    client = ss.accept();
                  } catch (IOException e) {
                    if (!ss.isClosed()) LOG.warning("DAP accept error: " + e.getMessage());
                    break;
                  }
                  LOG.info("DAP client connected from " + client.getRemoteSocketAddress());
                  handleSession(client);
                }
              } catch (IOException e) {
                LOG.fine("DAP server socket closed: " + e.getMessage());
              }
            },
            "dap-accept");
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  /** Stop accepting new connections. Active sessions continue until the client disconnects. */
  public void stop() {
    ServerSocket ss = serverSocketRef.getAndSet(null);
    if (ss != null) {
      try {
        ss.close();
      } catch (IOException e) {
        LOG.fine("Error closing server socket: " + e.getMessage());
      }
    }
  }

  /**
   * The port the server is currently bound to, or {@code -1} if not running. Useful when the OS
   * assigned the port (e.g. {@code new DapServer(0, factory)}).
   */
  public int getBoundPort() {
    ServerSocket ss = serverSocketRef.get();
    return ss != null ? ss.getLocalPort() : -1;
  }

  // =========================================================================
  // Session wiring
  // =========================================================================

  /**
   * Wires a newly accepted client {@link Socket} to a fresh {@link VM} / {@link DapAdapter} pair.
   *
   * <p>A lsp4j {@code DSPLauncher} is used to build the JSON-RPC message pump. The adapter receives
   * the remote client proxy via {@link DapAdapter#setClient} so it can fire events back. A daemon
   * cleanup thread waits for the listening future to complete and then closes the socket.
   *
   * @param socket the accepted TCP socket; ownership is transferred to the cleanup thread
   */
  private void handleSession(@NotNull Socket socket) {
    VM vm = vmFactory.get();
    DapAdapter adapter = new DapAdapter(vm);
    // Expose the adapter so reloadProgram() can reach it from outside the session thread.
    currentAdapter.set(adapter);
    Launcher<IDebugProtocolClient> launcher;
    try {
      // Let lsp4j build the JSON-RPC / DAP message pump.
      launcher =
          DSPLauncher.createServerLauncher(
              adapter, socket.getInputStream(), socket.getOutputStream());
    } catch (IOException e) {
      LOG.warning("Failed to create DAP session: " + e.getMessage());
      return;
    }

    // Give the adapter a reference to the remote client proxy so it can fire events.
    adapter.setClient(launcher.getRemoteProxy());

    // Start listening for incoming messages on a daemon thread.
    Future<Void> listening = launcher.startListening();

    // Clean-up thread: close the socket once the session ends.
    Thread cleanup = getCleanupThread(socket, listening);
    cleanup.start();
  }

  /**
   * Builds a daemon thread that waits for the lsp4j listening future to complete and then closes
   * the client socket.
   *
   * <p>This ensures that every accepted socket is released even if the client disconnects without
   * sending an explicit {@code disconnect} request. The thread is named {@code
   * "dap-session-cleanup"}.
   *
   * @param socket the socket to close when the session ends
   * @param listening the {@link Future} returned by {@code launcher.startListening()}
   * @return a configured daemon thread; the caller is responsible for calling {@link
   *     Thread#start()}
   */
  private static @NotNull Thread getCleanupThread(@NotNull Socket socket, Future<Void> listening) {
    Thread cleanup =
        new Thread(
            () -> {
              try {
                listening.get();
              } catch (Exception e) {
                LOG.fine("DAP session ended: " + e.getMessage());
              }
              try {
                socket.close();
              } catch (IOException e) {
                LOG.fine("Error closing DAP socket: " + e.getMessage());
              }
              LOG.info("DAP session closed.");
            },
            "dap-session-cleanup");
    cleanup.setDaemon(true);
    return cleanup;
  }

  // =========================================================================
  // Live reload
  // =========================================================================

  /**
   * Replaces the running program with {@code newProgram}, keeping the DAP server and any active
   * debugger session alive.
   *
   * <h2>When a DAP client (e.g. VS Code) is connected</h2>
   *
   * Delegates to {@link DapAdapter#reloadProgram(ProgramOp, boolean)}. The adapter stops the
   * current VM, re-initialises it, fires an {@code initialized} event to the client, and starts a
   * new VM thread that waits for the client to re-send breakpoints and {@code configurationDone}.
   * The VS Code debug session remains open throughout.
   *
   * <h2>When no client is connected (headless / blockly-only)</h2>
   *
   * No adapter is available, so a standalone {@link VM} is created from the {@link #vmFactory},
   * re-initialised with {@code newProgram}, and executed on a daemon thread without any DAP events.
   * This allows the blockly frontend to run programs even before a debugger attaches.
   *
   * @param newProgram the DGIR program to execute next
   * @param stopOnEntry when {@code true} and a DAP client is connected, the VM pauses before the
   *     first operation of {@code main} so the user can inspect state immediately
   * @throws InterruptedException if the thread join inside the adapter is interrupted
   */
  public void reloadProgram(@NotNull ProgramOp newProgram, boolean stopOnEntry)
      throws InterruptedException {
    DapAdapter adapter = currentAdapter.get();
    if (adapter != null) {
      adapter.reloadProgram(newProgram, stopOnEntry);
    } else {
      // No DAP client connected — run headlessly on a daemon thread.
      Thread t =
          new Thread(
              () -> {
                VM vm = vmFactory.get();
                vm.init(newProgram);
                vm.run();
              },
              "dap-vm-headless");
      t.setDaemon(true);
      t.start();
    }
  }

  /**
   * Convenience overload that reloads the program without pausing on entry.
   *
   * @param newProgram the DGIR program to execute next
   * @throws InterruptedException if the thread join inside the adapter is interrupted
   * @see #reloadProgram(ProgramOp, boolean)
   */
  public void reloadProgram(@NotNull ProgramOp newProgram) throws InterruptedException {
    reloadProgram(newProgram, false);
  }

  /** Calls {@link #stop()}, implementing {@link AutoCloseable} for use in try-with-resources. */
  @Override
  public void close() {
    stop();
  }
}
