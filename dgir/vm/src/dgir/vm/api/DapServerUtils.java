package dgir.vm.api;

import dgir.dialect.builtin.BuiltinOps;
import dgir.vm.dap.DapServer;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Utility façade for creating and connecting to a {@link DapServer}.
 *
 * <p>The {@link CollectingClient} inner class and the {@link ClientSession} record are designed to
 * be used together: {@code CollectingClient} queues asynchronous DAP events, and the {@code await*}
 * methods block until the expected event arrives (with a configurable timeout).
 *
 * <p>The handshake helpers ({@link #fullHandshake}, {@link #initializeHandshake}, {@link
 * #launchAndConfigDone}) cover the standard three-step DAP startup sequence that every debug
 * session must perform before execution can begin.
 */
public class DapServerUtils {
  /**
   * A DAP client implementation that collects all asynchronous events it receives into {@link
   * BlockingQueue}s, enabling test code to wait for specific events with a timeout.
   *
   * <p>The queues are intentionally unbounded so that spurious or out-of-order events do not cause
   * the client to block on the lsp4j dispatch thread. Drain specific queues via the {@code await*}
   * helper methods.
   */
  public static class CollectingClient implements IDebugProtocolClient {
    private final BlockingQueue<StoppedEventArguments> stoppedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ExitedEventArguments> exitedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<TerminatedEventArguments> terminatedQueue =
        new LinkedBlockingQueue<>();
    private final BlockingQueue<Object> initializedQueue = new LinkedBlockingQueue<>();
    private final long timeoutMillis;

    /** Creates a {@link CollectingClient} with the default timeout of 5 000 ms. */
    public CollectingClient() {
      this(5000);
    }

    /**
     * Creates a {@link CollectingClient} with a custom timeout.
     *
     * @param timeoutSeconds maximum number of seconds the {@code await*} methods will block before
     *     asserting failure
     */
    public CollectingClient(float timeoutSeconds) {
      this.timeoutMillis = (long) (timeoutSeconds * 1000);
    }

    /** {@inheritDoc} */
    @Override
    public void stopped(StoppedEventArguments args) {
      stoppedQueue.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void exited(ExitedEventArguments args) {
      exitedQueue.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void terminated(TerminatedEventArguments args) {
      terminatedQueue.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void initialized() {
      initializedQueue.add(true);
    }

    /**
     * Blocks until a {@code stopped} event arrives or {@link CollectingClient#timeoutMillis}
     * seconds elapse.
     *
     * @return the next {@link StoppedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    public StoppedEventArguments awaitStopped() throws Exception {
      StoppedEventArguments e = stoppedQueue.poll(5, TimeUnit.MILLISECONDS);
      assert e != null : "Expected stopped event within " + timeoutMillis + " s";
      return e;
    }

    /**
     * Blocks until an {@code exited} event arrives or {@link CollectingClient#timeoutMillis}
     * seconds elapse.
     *
     * @return the next {@link ExitedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    public ExitedEventArguments awaitExited() throws Exception {
      ExitedEventArguments e = exitedQueue.poll(5, TimeUnit.MILLISECONDS);
      assert e != null : "Expected exited event within " + timeoutMillis + " s";
      return e;
    }

    /**
     * Blocks until a {@code terminated} event arrives or {@link CollectingClient#timeoutMillis}
     * seconds elapse.
     *
     * @return the next {@link TerminatedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    public TerminatedEventArguments awaitTerminated() throws Exception {
      TerminatedEventArguments e = terminatedQueue.poll(5, TimeUnit.MILLISECONDS);
      assert e != null : "Expected terminated event within " + timeoutMillis + " s";
      return e;
    }

    /**
     * Blocks until the {@code initialized} notification arrives or {@link
     * CollectingClient#timeoutMillis} seconds elapse.
     *
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    public void awaitInitialized() throws Exception {
      Object e = initializedQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
      assert e != null : "Expected initialized event within " + timeoutMillis + " s";
    }
  }

  /**
   * Holds the TCP socket and a {@link IDebugProtocolClient} for one debug session.
   *
   * <p>Use {@link #server()} to send DAP requests to the server, and {@link #client()} to access
   * the callback events. Call {@link #close()} at the end to release the socket.
   *
   * @param socket the open loopback socket connected to the {@link DapServer}
   * @param client the DAP client that collects asynchronous events from the server
   */
  public record ClientSession<T extends CollectingClient>(
      @NotNull Socket socket,
      @NotNull T client,
      @NotNull IDebugProtocolServer server,
      @NotNull Future<Void> listeningFuture)
      implements AutoCloseable {
    /**
     * Closes the underlying TCP socket, signalling end-of-stream to the server.
     *
     * <p>When this method is called, the server-side lsp4j {@code StreamMessageProducer} detects
     * the closed stream and logs a {@code SocketException: Socket closed} at INFO level. This is
     * <b>expected and harmless</b>: it is the normal shutdown path for a JSON-RPC stream that ends
     * without an explicit {@code disconnect} request. The server's cleanup thread then closes its
     * side of the socket and logs {@code "DAP session closed."}.
     *
     * @throws IOException if the socket cannot be closed
     */
    @Override
    public void close() throws IOException {
      socket.close();
    }
  }

  /**
   * Creates a {@link DapServer} on the {@link DapServer#DEFAULT_PORT default port} (4711) using an
   * un-initialised {@link VM} factory. The server is <em>not</em> started by this call.
   *
   * @return a new, unstarted {@link DapServer}
   */
  public static DapServer createServer() {
    return new DapServer(VM::new);
  }

  /**
   * Creates a {@link DapServer} bound to the given port, using a default (un-initialised) {@link
   * VM} factory. The server is <em>not</em> started by this call.
   *
   * @param port the TCP port to listen on; {@code 0} lets the OS assign a free port
   * @return a new, unstarted {@link DapServer}
   */
  public static DapServer createServer(int port) {
    return new DapServer(port, VM::new);
  }

  /**
   * Creates a {@link DapServer} on the {@link DapServer#DEFAULT_PORT default port} (4711) whose
   * {@link VM} factory creates and initialises a VM with the supplied program. The server is
   * <em>not</em> started by this call.
   *
   * @param prog the DGIR program to load into every VM produced by the factory
   * @return a new, unstarted {@link DapServer}
   */
  public static DapServer createServerAndInit(BuiltinOps.ProgramOp prog) {
    return new DapServer(
        () -> {
          VM vm = new VM();
          vm.init(prog);
          return vm;
        });
  }

  /**
   * Creates a {@link DapServer} on the given port whose {@link VM} factory creates and initialises
   * a VM with the supplied program. The server is <em>not</em> started by this call.
   *
   * @param port the TCP port to listen on; {@code 0} lets the OS assign a free port
   * @param prog the DGIR program to load into every VM produced by the factory
   * @return a new, unstarted {@link DapServer}
   */
  public static DapServer createServerAndInit(int port, BuiltinOps.ProgramOp prog) {
    return new DapServer(
        port,
        () -> {
          VM vm = new VM();
          vm.init(prog);
          return vm;
        });
  }

  /**
   * Creates and starts a {@link DapServer} on the default port and immediately connects a {@link
   * CollectingClient} to it.
   *
   * <p>Equivalent to calling {@link #createServerAndInit(BuiltinOps.ProgramOp)}, {@link
   * DapServer#start()}, and {@link #connect(DapServer)}.
   *
   * @param prog the DGIR program each session's VM will execute
   * @return a pair of the running {@link DapServer} and the connected {@link ClientSession}
   * @throws IOException if the server socket cannot be bound or the client socket cannot connect
   */
  public static Pair<DapServer, ClientSession<CollectingClient>> createServerAndConnect(
      BuiltinOps.ProgramOp prog) throws IOException {
    DapServer server = createServerAndInit(prog);
    server.start();
    return Pair.of(server, connect(server));
  }

  /**
   * Creates and starts a {@link DapServer} on the given port and immediately connects a {@link
   * CollectingClient} to it.
   *
   * @param port the TCP port to listen on; {@code 0} lets the OS assign a free port
   * @param prog the DGIR program each session's VM will execute
   * @return a pair of the running {@link DapServer} and the connected {@link ClientSession}
   * @throws IOException if the server socket cannot be bound or the client socket cannot connect
   */
  public static Pair<DapServer, ClientSession<CollectingClient>> createServerAndConnect(
      int port, BuiltinOps.ProgramOp prog) throws IOException {
    DapServer server = createServerAndInit(port, prog);
    server.start();
    return Pair.of(server, connect(server));
  }

  /**
   * Creates and starts a {@link DapServer} on the default port and immediately connects the
   * supplied {@link CollectingClient} subclass to it.
   *
   * <p>Use this overload when the test needs a custom {@link CollectingClient} subclass that
   * captures additional events.
   *
   * @param <T> concrete {@link CollectingClient} subtype
   * @param prog the DGIR program each session's VM will execute
   * @param client the client instance to use for the session
   * @return a pair of the running {@link DapServer} and the connected {@link ClientSession}
   * @throws IOException if the server socket cannot be bound or the client socket cannot connect
   */
  public static <T extends CollectingClient>
      Pair<DapServer, ClientSession<T>> createServerAndConnect(BuiltinOps.ProgramOp prog, T client)
          throws IOException {
    DapServer server = createServerAndInit(prog);
    server.start();
    return Pair.of(server, connect(server, client));
  }

  /**
   * Creates and starts a {@link DapServer} on the given port and immediately connects the supplied
   * {@link CollectingClient} subclass to it.
   *
   * @param <T> concrete {@link CollectingClient} subtype
   * @param port the TCP port to listen on; {@code 0} lets the OS assign a free port
   * @param prog the DGIR program each session's VM will execute
   * @param client the client instance to use for the session
   * @return a pair of the running {@link DapServer} and the connected {@link ClientSession}
   * @throws IOException if the server socket cannot be bound or the client socket cannot connect
   */
  public static <T extends CollectingClient>
      Pair<DapServer, ClientSession<T>> createServerAndConnect(
          int port, BuiltinOps.ProgramOp prog, T client) throws IOException {
    DapServer server = createServerAndInit(port, prog);
    server.start();
    return Pair.of(server, connect(server, client));
  }

  /**
   * Connects a default {@link CollectingClient} to an already-started {@link DapServer}.
   *
   * @param server the running server to connect to
   * @return a {@link ClientSession} ready for the DAP handshake
   * @throws IOException if the loopback socket cannot be opened
   */
  public static ClientSession<CollectingClient> connect(DapServer server) throws IOException {
    return connect(server, new CollectingClient());
  }

  /**
   * Connects the given {@link CollectingClient} subclass to an already-started {@link DapServer}
   * and starts the lsp4j listening loop.
   *
   * @param <T> concrete {@link CollectingClient} subtype
   * @param server the running server to connect to; must have been {@link DapServer#start()}ed
   * @param client the client instance that will receive DAP events
   * @return a {@link ClientSession} wrapping the socket, client, remote proxy, and listener future
   * @throws IOException if the loopback socket cannot be opened
   * @throws AssertionError if the server is not yet bound to a port
   */
  public static <T extends CollectingClient> ClientSession<T> connect(DapServer server, T client)
      throws IOException {
    int port = server.getBoundPort();
    assert port > 0 : "Server should be bound to a port";

    Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);

    Launcher<IDebugProtocolServer> launcher =
        DSPLauncher.createClientLauncher(client, socket.getInputStream(), socket.getOutputStream());

    return new ClientSession<>(
        socket, client, launcher.getRemoteProxy(), launcher.startListening());
  }

  /**
   * Performs the standard DAP handshake required before execution can start:
   *
   * <ol>
   *   <li>{@code initialize} – negotiates capabilities; waits for the {@code initialized}
   *       notification back from the server.
   *   <li>{@code launch} – tells the adapter to start (or prepare to start) the program; {@code
   *       stopOnEntry} controls whether the VM pauses before its very first operation.
   *   <li>{@code configurationDone} – signals that all configuration (breakpoints, etc.) has been
   *       sent; the VM begins execution immediately after this call returns on the server side.
   * </ol>
   *
   * <p>All three requests time out after 5 seconds. A non-null {@link Capabilities} response is
   * asserted after {@code initialize}.
   *
   * @param session the active {@link ClientSession} whose {@link ClientSession#server() remote}
   *     proxy and {@link ClientSession#client() client} queues are used
   * @param stopOnEntry when {@code true}, the launch arguments include {@code {"stopOnEntry":
   *     true}}, causing the adapter to fire a {@code stopped("entry")} event before the first
   *     operation executes
   * @throws Exception if any request times out or an assertion fails
   */
  public static void fullHandshake(ClientSession<?> session, boolean stopOnEntry) throws Exception {
    initializeHandshake(session);
    launchAndConfigDone(session, stopOnEntry);
  }

  /**
   * Sends the {@code initialize} request and waits for the {@code initialized} notification.
   *
   * <p>This is the first half of the standard DAP handshake. It may be called separately from
   * {@link #launchAndConfigDone} when a test needs to set breakpoints between the two steps.
   *
   * @param session the active {@link ClientSession}
   * @throws Exception if the request times out or an assertion fails
   */
  public static void initializeHandshake(ClientSession<?> session) throws Exception {
    InitializeRequestArguments initArgs = new InitializeRequestArguments();
    initArgs.setClientID("test");
    Capabilities caps = session.server().initialize(initArgs).get(5, TimeUnit.SECONDS);
    assert caps != null : "Expected non-null capabilities from initialize response";

    session.client().awaitInitialized();
  }

  /**
   * Sends {@code launch} (with an optional {@code stopOnEntry} flag) followed by {@code
   * configurationDone}, completing the standard DAP startup sequence.
   *
   * <p>This is the second half of the handshake. After this method returns on the caller side, the
   * server-side VM thread is unblocked and execution begins.
   *
   * @param session the active {@link ClientSession}
   * @param stopOnEntry when {@code true}, passes {@code {"stopOnEntry": true}} in the launch
   *     arguments so the adapter pauses before the first operation
   * @throws Exception if any request times out or an assertion fails
   */
  public static void launchAndConfigDone(ClientSession<?> session, boolean stopOnEntry)
      throws Exception {
    Map<String, Object> launchArgs = stopOnEntry ? Map.of("stopOnEntry", true) : Map.of();
    session.server().launch(launchArgs).get(5, TimeUnit.SECONDS);
    session.server().configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
  }

  /**
   * Sends a {@code setBreakpoints} request for the given source file and line numbers, waiting for
   * the response and asserting that every requested breakpoint is verified.
   *
   * @param server the remote DAP server proxy
   * @param sourcePath the file path to set breakpoints in (e.g. {@code "test.dgir"})
   * @param lines one or more 1-based line numbers to break on
   * @throws Exception if the request times out or a breakpoint is not verified
   */
  public static void setBreakpointsOnLines(
      IDebugProtocolServer server, String sourcePath, int... lines) throws Exception {
    Source src = new Source();
    src.setPath(sourcePath);

    SourceBreakpoint[] sourceBreakpoints = new SourceBreakpoint[lines.length];
    for (int i = 0; i < lines.length; i++) {
      SourceBreakpoint sb = new SourceBreakpoint();
      sb.setLine(lines[i]);
      sourceBreakpoints[i] = sb;
    }

    SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
    bpArgs.setSource(src);
    bpArgs.setBreakpoints(sourceBreakpoints);

    SetBreakpointsResponse bpResp = server.setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);
    assert lines.length == bpResp.getBreakpoints().length;
    for (var bp : bpResp.getBreakpoints()) {
      assert bp.isVerified() : "Breakpoint should be verified";
    }
  }

  /**
   * Queries the {@code stackTrace} for thread 1 and returns the topmost frame.
   *
   * @param server the remote DAP server proxy
   * @return the first (innermost) {@link StackFrame} in the current call stack
   * @throws Exception if the request times out or the call stack is empty
   */
  public static StackFrame topFrame(IDebugProtocolServer server) throws Exception {
    StackTraceArguments stArgs = new StackTraceArguments();
    stArgs.setThreadId(1);
    StackTraceResponse st = server.stackTrace(stArgs).get(5, TimeUnit.SECONDS);
    assert st.getStackFrames().length > 0 : "Expected at least one frame";
    return st.getStackFrames()[0];
  }

  /**
   * Returns the source line number of the topmost stack frame.
   *
   * <p>Convenience shorthand for {@code topFrame(server).getLine()}.
   *
   * @param server the remote DAP server proxy
   * @return the 1-based source line of the innermost frame
   * @throws Exception if the underlying {@link #topFrame} call fails
   */
  public static int topLine(IDebugProtocolServer server) throws Exception {
    return topFrame(server).getLine();
  }
}
