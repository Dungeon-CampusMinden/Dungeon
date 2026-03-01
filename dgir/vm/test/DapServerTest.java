import core.Dialect;
import core.debug.Location;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dap.DapServer;
import dgir.vm.dialect.io.PrintRunner;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static dialect.arith.ArithOps.ConstantOp;
import static dialect.builtin.BuiltinOps.ProgramOp;
import static dialect.func.FuncOps.FuncOp;
import static dialect.func.FuncOps.ReturnOp;
import static dialect.io.IoOps.PrintOp;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DapServer}.
 *
 * <p>Each test starts a real {@link DapServer} bound on a random OS-assigned port (port {@code 0}),
 * connects a real lsp4j DAP client over a TCP loopback socket, drives the full Debug Adapter
 * Protocol exchange, and asserts on the events and responses received back from the adapter.
 *
 * <p>Unlike the unit-level {@code DapAdapterTest}, these tests exercise the entire stack:
 * <ol>
 *   <li>TCP socket accept loop inside {@link DapServer}</li>
 *   <li>lsp4j JSON-RPC framing / marshalling (Content-Length–framed messages)</li>
 *   <li>{@link dgir.vm.dap.DapAdapter} request handling and event dispatch</li>
 *   <li>The {@link VM} execution engine running DGIR operations</li>
 * </ol>
 *
 * <h2>Test structure</h2>
 * Tests are grouped into the following sections:
 * <ul>
 *   <li><b>Server lifecycle</b> – port binding, {@code stop()}, and {@code getBoundPort()} behaviour</li>
 *   <li><b>End-to-end: run to completion</b> – launching a program and waiting for {@code exited} /
 *       {@code terminated} events</li>
 *   <li><b>End-to-end: breakpoints</b> – setting a source breakpoint and verifying the
 *       {@code stopped} event fires at the correct location</li>
 *   <li><b>End-to-end: stepping</b> – step-in and next commands, verifying {@code stopped("step")}
 *       events</li>
 *   <li><b>End-to-end: threads / stackTrace / scopes / variables</b> – introspection requests
 *       while the VM is paused</li>
 *   <li><b>End-to-end: setExceptionBreakpoints</b> – verifying the adapter accepts the request
 *       without error</li>
 *   <li><b>Multiple sequential clients</b> – verifying the server calls the VM factory once per
 *       connection</li>
 * </ul>
 *
 * <h2>Timeout policy</h2>
 * Every blocking call uses a {@code 5-second} timeout so a hung VM never stalls the test suite.
 *
 * <h2>Expected log noise</h2>
 * Every test that calls {@link ClientSession#close()} (i.e. all end-to-end tests) will produce

 * two INFO log lines from the server side after the socket is closed:
 * <pre>
 * INFO: Socket closed
 *   java.net.SocketException: Socket closed
 *       at ...StreamMessageProducer.listen(...)
 *       ...
 * INFO: DAP session closed.
 * </pre>
 * These are <b>not errors</b>. Closing the client socket is the normal teardown path; lsp4j's
 * {@code StreamMessageProducer} catches the resulting {@code SocketException} and logs it at INFO
 * before marking the JSON-RPC stream as finished. The server's cleanup thread then closes its
 * side of the socket and logs {@code "DAP session closed."}. Both messages can safely be ignored.
 */
class DapServerTest extends VmTestBase {

  /** A well-known source location constant used for operations that do not require a real location. */
  static final Location LOC = Location.UNKNOWN;

  /**
   * The {@link DapServer} under test. Created in individual tests (or in {@link #connect}) and
   * shut down in {@link #stopServer()} after each test.
   */
  DapServer server;

  /**
   * One-time JUnit setup: registers all DGIR dialects with the {@link Dialect} registry and all
   * operation runners with {@link OpRunnerRegistry}, then wires {@link PrintRunner} output to
   * {@link System#out}.
   *
   * <p>Must run before any test because {@link VM#init} relies on both registries being populated.
   */
  @BeforeAll
  static void registerDialects() {
    Dialect.registerAllDialects();
    OpRunnerRegistry.registerAllRunners();
    PrintRunner.out = System.out;
  }

  /**
   * Ensures the server is stopped after every test, regardless of test outcome.
   * Calling {@link DapServer#stop()} on an already-stopped or never-started server is safe.
   */
  @AfterEach
  void stopServer() {
    if (server != null) server.stop();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Builds a minimal DGIR program that prints a single string and returns.
   *
   * <p>The resulting IR is equivalent to:
   * <pre>{@code
   * program {
   *   func main() {
   *     %c = constant $text
   *     print(%c)
   *     return
   *   }
   * }
   * }</pre>
   *
   * <p>All operations are assigned {@link Location#UNKNOWN} because source locations are not
   * relevant to the tests that use this helper (those tests only care about program completion,
   * not about breakpoints or stepping on specific lines).
   *
   * @param text the literal string value that the {@code print} operation will output
   * @return a fully-constructed {@link ProgramOp} ready to be passed to {@link VM#init}
   */
  static ProgramOp simplePrintProgram(String text) {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    var c = main.addOperation(new ConstantOp(LOC, text), 0);
    main.addOperation(new PrintOp(LOC, c.getValue()), 0);
    main.addOperation(new ReturnOp(LOC), 0);
    return prog;
  }

  /**
   * Builds a multi-line DGIR program with explicit source locations suitable for breakpoint and
   * stepping tests.
   *
   * <p>The program is rooted in a virtual source file {@code "test.dgir"} and is structured as:
   * <pre>{@code
   * // test.dgir
   * line 1: program {
   * line 2:   func main() {
   * line 3:     %a = constant "A"
   * line 4:     print(%a)
   * line 5:     %b = constant "B"
   * line 6:     print(%b)
   * line 7:     return
   *           }
   *         }
   * }</pre>
   *
   * <p>Each operation is tagged with a distinct line number so that:
   * <ul>
   *   <li>A breakpoint set on line 4 will fire when the {@code print("A")} operation is about to
   *       execute.</li>
   *   <li>Stepping through the program advances through lines 1–7 in order, allowing
   *       {@code stopped("step")} events to be counted and verified.</li>
   * </ul>
   *
   * @return a fully-constructed {@link ProgramOp} with per-operation source locations
   */
  static ProgramOp multiLinePrintProgram() {
    ProgramOp prog = new ProgramOp(new Location("test.dgir", 1, 1));
    FuncOp main =
        prog.addOperation(new FuncOp(new Location("test.dgir", 2, 1), "main"));
    var a = main.addOperation(new ConstantOp(new Location("test.dgir", 3, 1), "A"), 0);
    main.addOperation(new PrintOp(new Location("test.dgir", 4, 1), a.getValue()), 0);
    var b = main.addOperation(new ConstantOp(new Location("test.dgir", 5, 1), "B"), 0);
    main.addOperation(new PrintOp(new Location("test.dgir", 6, 1), b.getValue()), 0);
    main.addOperation(new ReturnOp(new Location("test.dgir", 7, 1)), 0);
    return prog;
  }

  /**
   * Starts a {@link DapServer} for {@code prog} and connects a lsp4j DAP client to it, returning
   * a {@link ClientSession} that bundles the raw socket and the collecting client.
   *
   * <p>The server is started on port {@code 0} so the OS picks a free port. The method:
   * <ol>
   *   <li>Creates and starts the server (assigns {@link #server} so {@link #stopServer()} can
   *       clean it up).</li>
   *   <li>Reads the OS-assigned port via {@link DapServer#getBoundPort()}.</li>
   *   <li>Opens a loopback TCP socket to that port.</li>
   *   <li>Creates a {@link CollectingClient} and wires it up with a lsp4j
   *       {@link DSPLauncher#createClientLauncher client launcher}.</li>
   *   <li>Starts the launcher's listening thread and returns the session.</li>
   * </ol>
   *
   * @param prog the DGIR program to debug; wrapped in a {@link VM} and passed to {@link VM#init}
   *             by the server's VM factory
   * @return a {@link ClientSession} representing the established debug session
   * @throws IOException if the server cannot bind or the socket cannot connect
   */
  ClientSession connect(ProgramOp prog) throws IOException {
    server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(prog);
              return vm;
            });
    server.start();

    int port = server.getBoundPort();
    assertTrue(port > 0, "Server should be bound to a port");

    Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
    CollectingClient collectingClient = new CollectingClient();

    Launcher<IDebugProtocolServer> launcher =
        DSPLauncher.createClientLauncher(
            collectingClient, socket.getInputStream(), socket.getOutputStream());

    collectingClient.remoteServer = launcher.getRemoteProxy();
    @SuppressWarnings("unused")
    var listeningFuture = launcher.startListening();

    return new ClientSession(socket, collectingClient);
  }

  /**
   * Holds the TCP socket and {@link CollectingClient} for one debug session.
   *
   * <p>Use {@link #remote()} to send DAP requests to the server, and {@link #client()} to
   * access the event queues. Call {@link #close()} at the end of every test to release the socket.
   *
   * @param socket the open loopback socket connected to the {@link DapServer}
   * @param client the DAP client that collects asynchronous events from the server
   */
  record ClientSession(Socket socket, CollectingClient client) {
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
    void close() throws IOException {
      socket.close();
    }

    /**
     * Returns the lsp4j remote proxy that can be used to send DAP requests
     * (e.g. {@code initialize}, {@code launch}, {@code continue_}, etc.) to the server.
     *
     * @return the remote {@link IDebugProtocolServer} proxy
     */
    IDebugProtocolServer remote() {
      return client.remoteServer;
    }
  }

  /**
   * A DAP client implementation that collects all asynchronous events it receives into
   * {@link BlockingQueue}s, enabling test code to wait for specific events with a timeout.
   *
   * <p>The queues are intentionally unbounded so that spurious or out-of-order events do not
   * cause the client to block on the lsp4j dispatch thread. Test methods drain specific
   * queues via the {@code await*} helper methods.
   */
  static class CollectingClient implements IDebugProtocolClient {
    /** The lsp4j remote-server proxy through which DAP requests are sent. */
    IDebugProtocolServer remoteServer;

    /** Collects every {@code stopped} event sent by the adapter. */
    final BlockingQueue<StoppedEventArguments> stopped = new LinkedBlockingQueue<>();
    /** Collects every {@code exited} event sent by the adapter. */
    final BlockingQueue<ExitedEventArguments> exited = new LinkedBlockingQueue<>();
    /** Collects every {@code terminated} event sent by the adapter. */
    final BlockingQueue<TerminatedEventArguments> terminated = new LinkedBlockingQueue<>();
    /** Collects the single {@code initialized} notification sent by the adapter. */
    final BlockingQueue<Object> initialized = new LinkedBlockingQueue<>();

    /** {@inheritDoc} */
    @Override
    public void stopped(StoppedEventArguments args) {
      stopped.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void exited(ExitedEventArguments args) {
      exited.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void terminated(TerminatedEventArguments args) {
      terminated.add(args);
    }

    /** {@inheritDoc} */
    @Override
    public void initialized() {
      initialized.add(true);
    }

    /**
     * Blocks until a {@code stopped} event arrives or 5 seconds elapse.
     *
     * @return the next {@link StoppedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    StoppedEventArguments awaitStopped() throws Exception {
      StoppedEventArguments e = stopped.poll(5, TimeUnit.SECONDS);
      assertNotNull(e, "Expected stopped event within 5 s");
      return e;
    }

    /**
     * Blocks until an {@code exited} event arrives or 5 seconds elapse.
     *
     * @return the next {@link ExitedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    ExitedEventArguments awaitExited() throws Exception {
      ExitedEventArguments e = exited.poll(5, TimeUnit.SECONDS);
      assertNotNull(e, "Expected exited event within 5 s");
      return e;
    }

    /**
     * Blocks until a {@code terminated} event arrives or 5 seconds elapse.
     *
     * @return the next {@link TerminatedEventArguments} from the queue
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    TerminatedEventArguments awaitTerminated() throws Exception {
      TerminatedEventArguments e = terminated.poll(5, TimeUnit.SECONDS);
      assertNotNull(e, "Expected terminated event within 5 s");
      return e;
    }

    /**
     * Blocks until the {@code initialized} notification arrives or 5 seconds elapse.
     *
     * @throws Exception if the wait is interrupted or the assertion fails on timeout
     */
    void awaitInitialized() throws Exception {
      Object e = initialized.poll(5, TimeUnit.SECONDS);
      assertNotNull(e, "Expected initialized event within 5 s");
    }
  }

  /**
   * Performs the standard DAP handshake required before execution can start:
   * <ol>
   *   <li>{@code initialize} – negotiates capabilities; waits for the {@code initialized}
   *       notification back from the server.</li>
   *   <li>{@code launch} – tells the adapter to start (or prepare to start) the program;
   *       {@code stopOnEntry} controls whether the VM pauses before its very first operation.</li>
   *   <li>{@code configurationDone} – signals that all configuration (breakpoints, etc.) has been
   *       sent; the VM begins execution immediately after this call returns on the server side.</li>
   * </ol>
   *
   * <p>All three requests time out after 5 seconds. A non-null {@link Capabilities} response is
   * asserted after {@code initialize}.
   *
   * @param session     the active {@link ClientSession} whose {@link ClientSession#remote() remote}
   *                    proxy and {@link ClientSession#client() client} queues are used
   * @param stopOnEntry when {@code true}, the launch arguments include {@code {"stopOnEntry": true}},
   *                    causing the adapter to fire a {@code stopped("entry")} event before the first
   *                    operation executes
   * @throws Exception if any request times out or an assertion fails
   */
  static void fullHandshake(ClientSession session, boolean stopOnEntry) throws Exception {
    IDebugProtocolServer remote = session.remote();

    InitializeRequestArguments initArgs = new InitializeRequestArguments();
    initArgs.setClientID("test");
    Capabilities caps = remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
    assertNotNull(caps);

    session.client().awaitInitialized();

    Map<String, Object> launchArgs = stopOnEntry ? Map.of("stopOnEntry", true) : Map.of();
    remote.launch(launchArgs).get(5, TimeUnit.SECONDS);
    remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
  }

  // =========================================================================
  // Server lifecycle
  // =========================================================================

  /**
   * Verifies that {@link DapServer#getBoundPort()} returns {@code -1} before {@link DapServer#start()}
   * is called, and a valid ephemeral port ({@code 1–65535}) after the server has successfully bound
   * its listening socket.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Create a server on port {@code 0} (OS-assigned).</li>
   *   <li>Assert that {@code getBoundPort() == -1} before starting.</li>
   *   <li>Call {@link DapServer#start()}.</li>
   *   <li>Assert that {@code getBoundPort()} is in the range {@code [1, 65535]}.</li>
   * </ol>
   */
  @Test
  void server_bindsToRandomPort() throws IOException {
    server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            });
    assertEquals(-1, server.getBoundPort(), "Port should be -1 before start");

    server.start();
    int port = server.getBoundPort();
    assertTrue(port > 0 && port <= 65535, "Should have a valid bound port: " + port);
  }

  /**
   * Verifies that {@link DapServer#stop()} closes the server's listening socket so that
   * subsequent TCP connection attempts to the same port are refused.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Start a server and record its bound port.</li>
   *   <li>Call {@link DapServer#stop()}.</li>
   *   <li>Sleep 50 ms to allow the OS to reclaim the port.</li>
   *   <li>Assert that opening a new {@link Socket} to that port throws an {@link IOException}.</li>
   * </ol>
   */
  @Test
  void server_stop_closesListeningSocket() throws IOException, InterruptedException {
    server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            });
    server.start();
    int port = server.getBoundPort();
    server.stop();

    // After stop, connections should be refused
    java.lang.Thread.sleep(50);
    assertThrows(
        IOException.class,
        () -> new Socket(java.net.InetAddress.getLoopbackAddress(), port).close(),
        "Server should refuse connections after stop");
  }

  /**
   * Verifies that {@link DapServer#getBoundPort()} returns {@code -1} again after
   * {@link DapServer#stop()} is called.
   *
   * <p>This is the companion check to {@link #server_bindsToRandomPort}: together they cover the
   * full port-reporting lifecycle (unstarted → started → stopped).
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Start the server and assert a positive port is reported.</li>
   *   <li>Stop the server.</li>
   *   <li>Assert that {@code getBoundPort() == -1}.</li>
   * </ol>
   */
  @Test
  void server_getBoundPort_returns_minusOne_afterStop() throws IOException {
    server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            });
    server.start();
    assertTrue(server.getBoundPort() > 0);
    server.stop();
    assertEquals(-1, server.getBoundPort());
  }

  // =========================================================================
  // End-to-end: run to completion
  // =========================================================================

  /**
   * End-to-end smoke test: launches a simple program without stopping on entry and verifies that
   * the adapter sends {@code exited(exitCode=0)} followed by {@code terminated}.
   *
   * <p>This is the most basic "happy path" scenario — the program runs uninterrupted from start
   * to finish over a real TCP connection.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect to a server running {@link #simplePrintProgram}.</li>
   *   <li>Perform the full DAP handshake with {@code stopOnEntry=false}.</li>
   *   <li>Wait for the {@code exited} event and assert {@code exitCode == 0}.</li>
   *   <li>Wait for the {@code terminated} event.</li>
   * </ol>
   */
  @Test
  void endToEnd_simpleProgram_runsToCompletion() throws Exception {
    ClientSession session = connect(simplePrintProgram("hello"));
    fullHandshake(session, false);

    ExitedEventArguments exited = session.client().awaitExited();
    assertEquals(0, exited.getExitCode());

    session.client().awaitTerminated();
    session.close();
  }

  /**
   * Verifies that the {@code stopOnEntry} launch option pauses execution before the first
   * operation, and that resuming via {@code continue} allows the program to finish normally.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Assert a {@code stopped} event arrives with {@code reason == "entry"}.</li>
   *   <li>Send a {@code continue} request.</li>
   *   <li>Wait for {@code exited} and {@code terminated} events confirming the program completed.</li>
   * </ol>
   */
  @Test
  void endToEnd_stopOnEntry_thenContinue_runsToCompletion() throws Exception {
    ClientSession session = connect(simplePrintProgram("stop-on-entry"));
    fullHandshake(session, true);

    StoppedEventArguments stopped = session.client().awaitStopped();
    assertEquals("entry", stopped.getReason());

    // Continue
    ContinueArguments contArgs = new ContinueArguments();
    session.remote().continue_(contArgs).get(5, TimeUnit.SECONDS);

    session.client().awaitExited();
    session.client().awaitTerminated();
    session.close();
  }

  // =========================================================================
  // End-to-end: breakpoints
  // =========================================================================

  /**
   * Verifies that a source breakpoint set on a specific line of {@code test.dgir} causes the
   * adapter to pause execution at that line and fire a {@code stopped("breakpoint")} event.
   *
   * <p>The test uses {@link #multiLinePrintProgram()}, which maps operations to lines 1–7 of
   * {@code test.dgir}. A breakpoint is placed on line 4, which corresponds to the
   * {@code print("A")} operation.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect to a server and perform the {@code initialize} + {@code initialized} handshake
   *       (but <em>not</em> {@code launch} yet, so breakpoints can be set before execution).</li>
   *   <li>Send {@code setBreakpoints} for {@code test.dgir} line 4. Assert the response contains
   *       exactly one verified breakpoint.</li>
   *   <li>Send {@code launch} (no {@code stopOnEntry}) and {@code configurationDone}.</li>
   *   <li>Wait for a {@code stopped} event; assert {@code reason == "breakpoint"} and
   *       {@code threadId == 1}.</li>
   *   <li>Resume with {@code continue}; wait for {@code exited} and {@code terminated}.</li>
   *   <li>Close the session. The server logs a {@code SocketException: Socket closed} and
   *       {@code "DAP session closed."} — both are expected; see the class-level note on
   *       <em>Expected log noise</em>.</li>
   * </ol>
   */
  @Test
  void endToEnd_breakpointHit_pausesExecution() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());

    InitializeRequestArguments initArgs = new InitializeRequestArguments();
    initArgs.setClientID("test");
    session.remote().initialize(initArgs).get(5, TimeUnit.SECONDS);
    session.client().awaitInitialized();

    // Set a breakpoint on line 4
    Source src = new Source();
    src.setPath("test.dgir");
    SourceBreakpoint sb = new SourceBreakpoint();
    sb.setLine(4);
    SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
    bpArgs.setSource(src);
    bpArgs.setBreakpoints(new SourceBreakpoint[] {sb});
    SetBreakpointsResponse bpResp =
        session.remote().setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);
    assertEquals(1, bpResp.getBreakpoints().length);
    assertTrue(bpResp.getBreakpoints()[0].isVerified());

    session.remote().launch(Map.of()).get(5, TimeUnit.SECONDS);
    session.remote().configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);

    StoppedEventArguments stopped = session.client().awaitStopped();
    assertEquals("breakpoint", stopped.getReason());
    assertEquals(1, stopped.getThreadId());

    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.client().awaitTerminated();
    session.close();
  }

  // =========================================================================
  // End-to-end: stepping
  // =========================================================================

  /**
   * Verifies that a sequence of {@code next} commands each produce exactly one
   * {@code stopped("step")} event per distinct source line, and that the program completes
   * normally once {@code continue} is sent.
   *
   * <p>Stepping is <em>line-granular</em>: a single {@code next} advances past all IR operations
   * that share the same source line and only fires one {@code stopped("step")} event when the VM
   * reaches the first operation on a different line. This means the number of step events equals
   * the number of distinct source lines traversed, not the number of raw IR operations.
   *
   * <p>The test uses {@link #multiLinePrintProgram()}, which assigns every IR operation its own
   * distinct line (1–7), so each {@code next} advances exactly one IR operation here. Three
   * consecutive {@code next} commands should therefore fire three {@code stopped("step")} events.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Await and verify the initial {@code stopped("entry")} event.</li>
   *   <li>Issue three {@code next} commands, asserting a {@code stopped("step")} event after
   *       each one.</li>
   *   <li>Send {@code continue}; wait for {@code exited} and {@code terminated}.</li>
   * </ol>
   */
  @Test
  void endToEnd_stepThroughProgram_firesStepEvents() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());
    fullHandshake(session, true);

    StoppedEventArguments first = session.client().awaitStopped();
    assertEquals("entry", first.getReason());

    // Step through three distinct source lines
    for (int i = 0; i < 3; i++) {
      if (i == 0) session.remote().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
      else session.remote().next(new NextArguments()).get(5, TimeUnit.SECONDS);
      StoppedEventArguments stepped = session.client().awaitStopped();
      assertEquals("step", stepped.getReason());
    }

    // Continue the rest
    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.client().awaitTerminated();
    session.close();
  }

  /**
   * Verifies that the {@code stepIn} DAP command is line-granular and behaves identically to
   * {@code next} in a flat DGIR program (no callee to step into), firing exactly one
   * {@code stopped("step")} event per distinct source line.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Drain the initial {@code stopped("entry")} event.</li>
   *   <li>Issue a single {@code stepIn} command and assert a {@code stopped("step")} event.</li>
   *   <li>Resume with {@code continue}; wait for {@code exited}.</li>
   * </ol>
   */
  @Test
  void endToEnd_stepIn_behavesLikeNext() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());
    fullHandshake(session, true);

    session.client().awaitStopped(); // entry

    session.remote().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
    StoppedEventArguments stepped = session.client().awaitStopped();
    assertEquals("step", stepped.getReason());

    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.close();
  }

  // =========================================================================
  // End-to-end: threads / stackTrace / scopes / variables
  // =========================================================================

  /**
   * Verifies that the {@code threads} request returns exactly one thread named {@code "main"}
   * while the VM is paused on entry.
   *
   * <p>The DGIR VM is single-threaded; the adapter always exposes a single logical thread with
   * ID {@code 1} and name {@code "main"}.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Await the entry-stop event.</li>
   *   <li>Send a {@code threads} request and assert exactly one thread named {@code "main"}.</li>
   *   <li>Resume and wait for {@code exited}.</li>
   * </ol>
   */
  @Test
  void endToEnd_threads_returnsSingleThread() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());
    fullHandshake(session, true);

    session.client().awaitStopped(); // entry

    ThreadsResponse threads = session.remote().threads().get(5, TimeUnit.SECONDS);
    assertEquals(1, threads.getThreads().length);
    assertEquals("main", threads.getThreads()[0].getName());

    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.close();
  }

  /**
   * Verifies that the {@code stackTrace} request returns at least one frame with a non-null
   * {@link Source} and a positive frame ID while the VM is paused on entry.
   *
   * <p>The presence of a {@code Source} object on every frame is required by VS Code to enable
   * source highlighting in the editor when the debugger pauses.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Await the entry-stop event.</li>
   *   <li>Send {@code stackTrace} for thread ID {@code 1}.</li>
   *   <li>Assert {@code totalFrames > 0}, at least one stack frame is returned, every frame has
   *       a non-null {@code source}, and every frame has a positive {@code id}.</li>
   *   <li>Resume and wait for {@code exited}.</li>
   * </ol>
   */
  @Test
  void endToEnd_stackTrace_whilePaused_returnsFrames() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());
    fullHandshake(session, true);

    session.client().awaitStopped(); // entry

    StackTraceArguments stArgs = new StackTraceArguments();
    stArgs.setThreadId(1);
    StackTraceResponse st = session.remote().stackTrace(stArgs).get(5, TimeUnit.SECONDS);

    assertTrue(st.getTotalFrames() > 0);
    assertTrue(st.getStackFrames().length > 0);
    for (StackFrame f : st.getStackFrames()) {
      assertNotNull(f.getSource());
      assertTrue(f.getId() > 0);
    }

    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.close();
  }

  /**
   * Verifies that the {@code scopes} and {@code variables} requests return meaningful data after
   * at least one operation has executed and produced a binding.
   *
   * <p>After the entry stop, two step commands are issued: the first enters the {@code main}
   * function (executes the first op in {@code main}, {@code ConstantOp("A")}), and the second
   * executes the {@code PrintOp} on the next line.
   * that produces the first visible local binding. At that point the scopes/variables
   * introspection should be populated.
   *
   * <p><b>Expected scopes:</b> exactly one scope named {@code "Locals"}.
   *
   * <p><b>Expected variables:</b> at least one variable, each with a non-null name and value.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.</li>
   *   <li>Drain the entry-stop event.</li>
   *   <li>Send {@code stepIn} (executes {@code ConstantOp("A")}); drain the resulting
   *       {@code stopped("step")} event.</li>
   *   <li>Send {@code next} (executes {@code PrintOp("A")}); drain the resulting
   *       {@code stopped("step")} event.</li>
   *   <li>Send {@code scopes} for frame ID {@code 1}; assert one scope named {@code "Locals"}.</li>
   *   <li>Send {@code variables} for the locals scope; assert at least one variable with
   *       non-null name and value.</li>
   *   <li>Resume and wait for {@code exited}.</li>
   * </ol>
   */
  @Test
  void endToEnd_scopesAndVariables_whilePaused() throws Exception {
    ClientSession session = connect(multiLinePrintProgram());
    fullHandshake(session, true);

    session.client().awaitStopped(); // entry pause

    // Step into one to execute ConstantOp("A") and produce the first binding.
    session.remote().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitStopped(); // step

    // Step again — executes PrintOp("A"), creating the first visible binding.
    session.remote().next(new NextArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitStopped(); // step

    // Scopes
    ScopesArguments scopesArgs = new ScopesArguments();
    scopesArgs.setFrameId(1);
    ScopesResponse scopes = session.remote().scopes(scopesArgs).get(5, TimeUnit.SECONDS);
    assertEquals(1, scopes.getScopes().length);
    assertEquals("Locals", scopes.getScopes()[0].getName());

    // Variables
    VariablesArguments varArgs = new VariablesArguments();
    varArgs.setVariablesReference(scopes.getScopes()[0].getVariablesReference());
    VariablesResponse vars = session.remote().variables(varArgs).get(5, TimeUnit.SECONDS);
    assertNotNull(vars.getVariables());
    assertTrue(vars.getVariables().length >= 1, "Expected at least one variable after stepping");

    for (Variable v : vars.getVariables()) {
      assertNotNull(v.getName());
      assertNotNull(v.getValue());
    }

    session.remote().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.close();
  }

  // =========================================================================
  // End-to-end: setExceptionBreakpoints is always accepted
  // =========================================================================

  /**
   * Verifies that the {@code setExceptionBreakpoints} DAP request is accepted by the adapter and
   * returns a non-null response, even when an empty filter array is supplied.
   *
   * <p>VS Code always sends this request during the initialization sequence regardless of whether
   * the adapter advertises support for it. Returning a valid (non-error) response prevents the
   * client from surfacing a protocol error to the user.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Connect and perform the {@code initialize} + {@code initialized} handshake.</li>
   *   <li>Send {@code setExceptionBreakpoints} with an empty {@code filters} array.</li>
   *   <li>Assert the response future completes with a non-null value (no exception thrown).</li>
   *   <li>Complete the handshake with {@code launch} + {@code configurationDone}; wait for
   *       {@code exited}.</li>
   * </ol>
   */
  @Test
  void endToEnd_setExceptionBreakpoints_isAccepted() throws Exception {
    ClientSession session = connect(simplePrintProgram("x"));

    InitializeRequestArguments initArgs = new InitializeRequestArguments();
    session.remote().initialize(initArgs).get(5, TimeUnit.SECONDS);
    session.client().awaitInitialized();

    SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
    args.setFilters(new String[0]);
    // Should not throw
    assertNotNull(session.remote().setExceptionBreakpoints(args).get(5, TimeUnit.SECONDS));

    session.remote().launch(Map.of()).get(5, TimeUnit.SECONDS);
    session.remote().configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
    session.client().awaitExited();
    session.close();
  }

  // =========================================================================
  // Multiple sequential clients (factory called per session)
  // =========================================================================

  /**
   * Verifies that the {@link DapServer} creates a fresh {@link VM} for each client connection
   * by calling the VM factory once per accepted session, and that each session completes
   * independently with exit code {@code 0}.
   *
   * <p>The server is shared across two sequential client connections. An internal counter tracks
   * how many times the factory lambda is invoked. Each client connection goes through the full
   * DAP handshake and waits for {@code exited} and {@code terminated} events before the next
   * connection is opened.
   *
   * <p><b>Steps:</b>
   * <ol>
   *   <li>Create a server with a factory that increments a counter on every call and returns a
   *       new {@link VM} running a program named after the current call count.</li>
   *   <li>Connect client 1, perform the full handshake, wait for {@code exited(exitCode=0)} and
   *       {@code terminated}.</li>
   *   <li>Connect client 2, repeat the same handshake and assertions.</li>
   *   <li>Assert the factory was called exactly twice.</li>
   * </ol>
   */
  @Test
  void server_handlesMultipleSequentialClients() throws Exception {
    final int[] callCount = {0};
    server =
        new DapServer(
            0,
            () -> {
              callCount[0]++;
              VM vm = new VM();
              vm.init(simplePrintProgram("session-" + callCount[0]));
              return vm;
            });
    server.start();

    for (int i = 1; i <= 2; i++) {
      try (Socket socket =
          new Socket(java.net.InetAddress.getLoopbackAddress(), server.getBoundPort())) {
        CollectingClient client = new CollectingClient();
        Launcher<IDebugProtocolServer> launcher =
            DSPLauncher.createClientLauncher(
                client, socket.getInputStream(), socket.getOutputStream());
        client.remoteServer = launcher.getRemoteProxy();
        @SuppressWarnings("unused")
        var listeningFuture = launcher.startListening();

        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        client.remoteServer.initialize(initArgs).get(5, TimeUnit.SECONDS);
        client.awaitInitialized();

        client.remoteServer.launch(Map.of()).get(5, TimeUnit.SECONDS);
        client
            .remoteServer
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);

        ExitedEventArguments exited = client.awaitExited();
        assertEquals(0, exited.getExitCode(), "Session " + i + " should exit cleanly");
        client.awaitTerminated();
      }
    }

    assertEquals(2, callCount[0], "vmFactory should have been called twice");
  }
}
