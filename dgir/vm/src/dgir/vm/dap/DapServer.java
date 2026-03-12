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
 * TCP server that accepts DAP client connections and wires each one to a single, shared {@link VM}
 * via a persistent {@link DapAdapter}.
 *
 * <p>The server maintains <em>one</em> adapter/VM pair for the lifetime of a program. The VM is
 * started (headlessly) by the first {@link #reloadProgram} call, or by the first DAP client that
 * sends a {@code launch}/{@code attach} request. A debugger can attach to the running VM at any
 * time; when it disconnects the VM continues executing. If a second client tries to connect while
 * one is already attached, the new connection is rejected immediately.
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li>Call {@link #start()} to open the listening socket.
 *   <li>(Optional) Call {@link #reloadProgram} to load and start a program without a debugger.
 *   <li>A DAP client (e.g. VS Code) connects and attaches to the running VM.
 *   <li>When the client disconnects the VM resumes freely.
 *   <li>Call {@link #reloadProgram} again to replace the program at any time.
 * </ol>
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
   * The single, persistent {@link DapAdapter} that wraps the shared VM. Created lazily on the first
   * {@link #reloadProgram} call or the first client connection, and then reused for all subsequent
   * operations.
   */
  private final @NotNull AtomicReference<DapAdapter> currentAdapter = new AtomicReference<>();

  /**
   * Create a server that listens on the given port.
   *
   * @param port the TCP port to listen on.
   * @param vmFactory factory that creates a blank (or pre-initialised) {@link VM}. Called exactly
   *     once to create the shared VM instance.
   */
  public DapServer(int port, @NotNull Supplier<VM> vmFactory) {
    this.port = port;
    this.vmFactory = vmFactory;
  }

  /**
   * Create a server on the {@link #DEFAULT_PORT default port} (4711).
   *
   * @param vmFactory factory that creates a blank (or pre-initialised) {@link VM}.
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
   * Handles a newly accepted client {@link Socket}.
   *
   * <p>The server enforces a <em>single-attachment</em> policy:
   *
   * <ul>
   *   <li>If another client is already attached, the socket is closed immediately.
   *   <li>If the VM has already finished running (and no {@link #reloadProgram} has been issued),
   *       the socket is closed immediately.
   * </ul>
   *
   * <p>Otherwise the socket is wired to the shared {@link DapAdapter} via lsp4j's {@link
   * DSPLauncher}. A daemon cleanup thread waits for the listening future to complete, calls {@link
   * DapAdapter#onSessionEnded()} to clear the client reference and resume the VM, and then closes
   * the socket.
   *
   * @param socket the accepted TCP socket; ownership is transferred to the cleanup thread
   */
  private void handleSession(@NotNull Socket socket) {
    // Lazily create the shared adapter on the very first connection (before any reloadProgram).
    DapAdapter adapter = currentAdapter.get();
    if (adapter == null) {
      VM vm = vmFactory.get();
      DapAdapter fresh = new DapAdapter(vm);
      if (currentAdapter.compareAndSet(null, fresh)) {
        adapter = fresh;
      } else {
        adapter = currentAdapter.get();
      }
    }

    // Reject if another debugger is already attached.
    if (adapter.hasActiveClient()) {
      LOG.warning("DAP client rejected: a debugger is already attached.");
      closeQuietly(socket);
      return;
    }

    // Reject if the program has already finished (and no reload has been issued).
    if (adapter.isVmFinished()) {
      LOG.warning("DAP client rejected: the program has already finished running.");
      closeQuietly(socket);
      return;
    }

    Launcher<IDebugProtocolClient> launcher;
    try {
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

    // Clean-up thread: notify the adapter and close the socket once the session ends.
    Thread cleanup = getCleanupThread(socket, listening, adapter);
    cleanup.start();
  }

  /** Closes a socket silently, logging only at FINE level. */
  private static void closeQuietly(@NotNull Socket socket) {
    try {
      socket.close();
    } catch (IOException e) {
      LOG.fine("Error closing rejected socket: " + e.getMessage());
    }
  }

  /**
   * Builds a daemon thread that waits for the lsp4j listening future to complete, calls {@link
   * DapAdapter#onSessionEnded()} to clear the client reference and resume the VM, and then closes
   * the client socket.
   *
   * @param socket the socket to close when the session ends
   * @param listening the {@link Future} returned by {@code launcher.startListening()}
   * @param adapter the adapter to notify when the session ends
   * @return a configured daemon thread; the caller is responsible for calling {@link
   *     Thread#start()}
   */
  private static @NotNull Thread getCleanupThread(
      @NotNull Socket socket, Future<Void> listening, @NotNull DapAdapter adapter) {
    Thread cleanup =
        new Thread(
            () -> {
              try {
                listening.get();
              } catch (Exception e) {
                LOG.fine("DAP session ended: " + e.getMessage());
              }
              // Clear the client reference and resume the VM so it can continue running.
              adapter.onSessionEnded();
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
   * <h2>When a DAP client is connected</h2>
   *
   * Delegates to {@link DapAdapter#reloadProgram(dgir.dialect.builtin.BuiltinOps.ProgramOp,
   * boolean)}. The adapter stops the current VM, re-initialises it, fires an {@code initialized}
   * event to the client, and starts a new VM thread that waits for the client to re-send
   * breakpoints and {@code configurationDone}. The VS Code debug session remains open throughout.
   *
   * <h2>When no client is connected</h2>
   *
   * The program is started headlessly on a daemon thread. Any previously running headless VM is
   * stopped first (via the adapter's reload logic), so at most one VM run is active at any time.
   *
   * @param newProgram the DGIR program to execute next
   * @param stopOnEntry when {@code true} and a DAP client is connected, the VM pauses before the
   *     first operation of {@code main} so the user can inspect state immediately
   * @throws InterruptedException if the thread join on the old VM thread is interrupted
   */
  public void reloadProgram(@NotNull ProgramOp newProgram, boolean stopOnEntry)
      throws InterruptedException {
    // Ensure the shared adapter exists (create it lazily on first use).
    DapAdapter adapter = currentAdapter.get();
    if (adapter == null) {
      VM vm = vmFactory.get();
      DapAdapter fresh = new DapAdapter(vm);
      if (currentAdapter.compareAndSet(null, fresh)) {
        adapter = fresh;
      } else {
        adapter = currentAdapter.get();
      }
    }
    adapter.reloadProgram(newProgram, stopOnEntry);
  }

  /**
   * Convenience overload that reloads the program without pausing on entry.
   *
   * @param newProgram the DGIR program to execute next
   * @throws InterruptedException if the thread join on the old VM thread is interrupted
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
