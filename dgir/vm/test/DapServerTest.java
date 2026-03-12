import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.debug.ValueDebugInfo;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dap.DapServer;
import dgir.vm.dialect.io.IoRunners;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.io.IoOps.PrintOp;
import static dgir.vm.api.DapServerUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DapServer}.
 *
 * <p>Each test starts a real {@link DapServer} bound on a random OS-assigned port (port {@code 0}),
 * connects a real lsp4j DAP client over a TCP loopback socket, drives the full Debug Adapter
 * Protocol exchange, and asserts on the events and responses received back from the adapter.
 *
 * <p>Unlike the unit-level {@code DapAdapterTest}, these tests exercise the entire stack:
 *
 * <ol>
 *   <li>TCP socket accept loop inside {@link DapServer}
 *   <li>lsp4j JSON-RPC framing / marshalling (Content-Length–framed messages)
 *   <li>{@link dgir.vm.dap.DapAdapter} request handling and event dispatch
 *   <li>The {@link VM} execution engine running DGIR operations
 * </ol>
 *
 * <h2>Test structure</h2>
 *
 * Tests are grouped into the following sections:
 *
 * <ul>
 *   <li><b>Server lifecycle</b> – port binding, {@code stop()}, and {@code getBoundPort()}
 *       behaviour
 *   <li><b>End-to-end: run to completion</b> – launching a program and waiting for {@code exited} /
 *       {@code terminated} events
 *   <li><b>End-to-end: breakpoints</b> – setting a source breakpoint and verifying the {@code
 *       stopped} event fires at the correct location
 *   <li><b>End-to-end: stepping</b> – step-in and next commands, verifying {@code stopped("step")}
 *       events
 *   <li><b>End-to-end: threads / stackTrace / scopes / variables</b> – introspection requests while
 *       the VM is paused
 *   <li><b>End-to-end: setExceptionBreakpoints</b> – verifying the adapter accepts the request
 *       without error
 *   <li><b>Multiple sequential clients</b> – verifying the server calls the VM factory once per
 *       connection
 * </ul>
 *
 * <h2>Timeout policy</h2>
 *
 * Every blocking call uses a {@code 5-second} timeout so a hung VM never stalls the test suite.
 */
class DapServerTest extends VmTestBase {

  /**
   * A well-known source location constant used for operations that do not require a real location.
   */
  static final Location LOC = Location.UNKNOWN;

  /**
   * One-time JUnit setup: registers all DGIR dialects with the {@link Dialect} registry and all
   * operation runners with {@link OpRunnerRegistry}, then wires {@link IoRunners.PrintRunner}
   * output to {@link System#out}.
   *
   * <p>Must run before any test because {@link VM#init} relies on both registries being populated.
   */
  @BeforeAll
  static void registerDialects() {
    IoRunners.PrintRunner.out = System.out;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Builds a minimal DGIR program that prints a single string and returns.
   *
   * <p>The resulting IR is equivalent to:
   *
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
   * relevant to the tests that use this helper (those tests only care about program completion, not
   * about breakpoints or stepping on specific lines).
   *
   * @param text the literal string value that the {@code print} operation will output
   * @return a fully-constructed {@link ProgramOp} ready to be passed to {@link VM#init}
   */
  static ProgramOp simplePrintProgram(String text) {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    var c = main.addOperation(new ConstantOp(LOC, text), 0);
    main.addOperation(new PrintOp(LOC, c.getResult()), 0);
    main.addOperation(new ReturnOp(LOC), 0);
    return prog;
  }

  /**
   * Builds a multi-line DGIR program with explicit source locations suitable for breakpoint and
   * stepping tests.
   *
   * <p>The program is rooted in a virtual source file {@code "test.dgir"} and is structured as:
   *
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
   *
   * <ul>
   *   <li>A breakpoint set on line 4 will fire when the {@code print("A")} operation is about to
   *       execute.
   *   <li>Stepping through the program advances through lines 1–7 in order, allowing {@code
   *       stopped("step")} events to be counted and verified.
   * </ul>
   *
   * @return a fully-constructed {@link ProgramOp} with per-operation source locations
   */
  static ProgramOp multiLinePrintProgram() {
    ProgramOp prog = new ProgramOp(new Location("test.dgir", 1, 1));
    FuncOp main = prog.addOperation(new FuncOp(new Location("test.dgir", 2, 1), "main"));
    var a = main.addOperation(new ConstantOp(new Location("test.dgir", 3, 1), "A"), 0);
    a.getResult().setDebugInfo(new ValueDebugInfo(a.getLocation(), "a"));
    main.addOperation(new PrintOp(new Location("test.dgir", 4, 1), a.getResult()), 0);
    var b = main.addOperation(new ConstantOp(new Location("test.dgir", 5, 1), "B"), 0);
    b.getResult().setDebugInfo(new ValueDebugInfo(b.getLocation(), "b"));
    main.addOperation(new PrintOp(new Location("test.dgir", 6, 1), b.getResult()), 0);
    main.addOperation(new ReturnOp(new Location("test.dgir", 7, 1)), 0);
    return prog;
  }

  /**
   * Loads a DGIR program from the test resource {@code "functionCallWithOverload.json"}, which
   * contains a simple program that calls an overloaded function.
   *
   * <pre>{@code
   * public class functionCallWithOverload {
   *   public static void main() {
   *     int result = add(5, 10);
   *     float resultfloat = add(5f, 10f);
   *   }
   *
   *   public static int add(int a, int b) {
   *     return a + b;
   *   }
   *
   *   public static float add(float a, float b) {
   *     return a + b;
   *   }
   * }
   * }</pre>
   *
   * @return a fully-constructed {@link ProgramOp} loaded from the JSON resource file
   * @throws RuntimeException if the resource file cannot be found or parsed
   */
  static ProgramOp functionCallsWithOverload() {
    return TestUtils.loadProgram("functionCallWithOverload.json")
        .orElseThrow(() -> new RuntimeException("Failed to load test program"));
  }

  // =========================================================================
  // Server lifecycle
  // =========================================================================

  /**
   * Verifies that {@link DapServer#getBoundPort()} returns {@code -1} before {@link
   * DapServer#start()} is called, and a valid ephemeral port ({@code 1–65535}) after the server has
   * successfully bound its listening socket.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Create a server on port {@code 0} (OS-assigned).
   *   <li>Assert that {@code getBoundPort() == -1} before starting.
   *   <li>Call {@link DapServer#start()}.
   *   <li>Assert that {@code getBoundPort()} is in the range {@code [1, 65535]}.
   * </ol>
   */
  @Test
  void server_bindsToRandomPort() throws IOException {
    try (DapServer server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            })) {

      assertEquals(-1, server.getBoundPort(), "Port should be -1 before start");

      server.start();
      int port = server.getBoundPort();
      assertTrue(port > 0 && port <= 65535, "Should have a valid bound port: " + port);
    }
  }

  /**
   * Verifies that {@link DapServer#stop()} closes the server's listening socket so that subsequent
   * TCP connection attempts to the same port are refused.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Start a server and record its bound port.
   *   <li>Call {@link DapServer#stop()}.
   *   <li>Sleep 50 ms to allow the OS to reclaim the port.
   *   <li>Assert that opening a new {@link Socket} to that port throws an {@link IOException}.
   * </ol>
   */
  @Test
  void server_stop_closesListeningSocket() throws IOException, InterruptedException {
    try (DapServer server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            })) {
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
  }

  /**
   * Verifies that {@link DapServer#getBoundPort()} returns {@code -1} again after {@link
   * DapServer#stop()} is called.
   *
   * <p>This is the companion check to {@link #server_bindsToRandomPort}: together they cover the
   * full port-reporting lifecycle (unstarted → started → stopped).
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Start the server and assert a positive port is reported.
   *   <li>Stop the server.
   *   <li>Assert that {@code getBoundPort() == -1}.
   * </ol>
   */
  @Test
  void server_getBoundPort_returns_minusOne_afterStop() throws IOException {
    try (DapServer server =
        new DapServer(
            0,
            () -> {
              VM vm = new VM();
              vm.init(simplePrintProgram("x"));
              return vm;
            })) {
      server.start();
      assertTrue(server.getBoundPort() > 0);
      server.stop();
      assertEquals(-1, server.getBoundPort());
    }
  }

  // =========================================================================
  // End-to-end: run to completion
  // =========================================================================

  /**
   * End-to-end smoke test: launches a simple program without stopping on entry and verifies that
   * the adapter sends {@code exited(exitCode=0)} followed by {@code terminated}.
   *
   * <p>This is the most basic "happy path" scenario — the program runs uninterrupted from start to
   * finish over a real TCP connection.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect to a server running {@link #simplePrintProgram}.
   *   <li>Perform the full DAP handshake with {@code stopOnEntry=false}.
   *   <li>Wait for the {@code exited} event and assert {@code exitCode == 0}.
   *   <li>Wait for the {@code terminated} event.
   * </ol>
   */
  @Test
  void endToEnd_simpleProgram_runsToCompletion() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("hello"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {

        fullHandshake(session, false);

        ExitedEventArguments exited = session.client().awaitExited();
        assertEquals(0, exited.getExitCode());

        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that the {@code stopOnEntry} launch option pauses execution before the first
   * operation, and that resuming via {@code continue} allows the program to finish normally.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Assert a {@code stopped} event arrives with {@code reason == "entry"}.
   *   <li>Send a {@code continue} request.
   *   <li>Wait for {@code exited} and {@code terminated} events confirming the program completed.
   * </ol>
   */
  @Test
  void endToEnd_stopOnEntry_thenContinue_runsToCompletion() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("stop-on-entry"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        StoppedEventArguments stopped = session.client().awaitStopped();
        assertEquals("entry", stopped.getReason());

        // Continue
        ContinueArguments contArgs = new ContinueArguments();
        session.server().continue_(contArgs).get(5, TimeUnit.SECONDS);

        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  // =========================================================================
  // End-to-end: breakpoints
  // =========================================================================

  /**
   * Verifies that a source breakpoint set on a specific line of {@code test.dgir} causes the
   * adapter to pause execution at that line and fire a {@code stopped("breakpoint")} event.
   *
   * <p>The test uses {@link #multiLinePrintProgram()}, which maps operations to lines 1–7 of {@code
   * test.dgir}. A breakpoint is placed on line 4, which corresponds to the {@code print("A")}
   * operation.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect to a server and perform the {@code initialize} + {@code initialized} handshake
   *       (but <em>not</em> {@code launch} yet, so breakpoints can be set before execution).
   *   <li>Send {@code setBreakpoints} for {@code test.dgir} line 4. Assert the response contains
   *       exactly one verified breakpoint.
   *   <li>Send {@code launch} (no {@code stopOnEntry}) and {@code configurationDone}.
   *   <li>Wait for a {@code stopped} event; assert {@code reason == "breakpoint"} and {@code
   *       threadId == 1}.
   *   <li>Resume with {@code continue}; wait for {@code exited} and {@code terminated}.
   *   <li>Close the session. The server logs a {@code SocketException: Socket closed} and {@code
   *       "DAP session closed."} — both are expected; see the class-level note on <em>Expected log
   *       noise</em>.
   * </ol>
   */
  @Test
  void endToEnd_breakpointHit_pausesExecution() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("test");
        session.server().initialize(initArgs).get(5, TimeUnit.SECONDS);
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
            session.server().setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);
        assertEquals(1, bpResp.getBreakpoints().length);
        assertTrue(bpResp.getBreakpoints()[0].isVerified());

        session.server().launch(Map.of()).get(5, TimeUnit.SECONDS);
        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);

        StoppedEventArguments stopped = session.client().awaitStopped();
        assertEquals("breakpoint", stopped.getReason());
        assertEquals(1, stopped.getThreadId());

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  // =========================================================================
  // End-to-end: stepping
  // =========================================================================

  /**
   * Verifies that a sequence of {@code next} commands each produce exactly one {@code
   * stopped("step")} event per distinct source line, and that the program completes normally once
   * {@code continue} is sent.
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
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Await and verify the initial {@code stopped("entry")} event.
   *   <li>Issue three {@code next} commands, asserting a {@code stopped("step")} event after each
   *       one.
   *   <li>Send {@code continue}; wait for {@code exited} and {@code terminated}.
   * </ol>
   */
  @Test
  void endToEnd_stepThroughProgram_firesStepEvents() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        StoppedEventArguments first = session.client().awaitStopped();
        assertEquals("entry", first.getReason());

        // Step through three distinct source lines
        for (int i = 0; i < 3; i++) {
          if (i == 0) session.server().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
          else session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
          StoppedEventArguments stepped = session.client().awaitStopped();
          assertEquals("step", stepped.getReason());
        }

        // Continue the rest
        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that the {@code stepIn} DAP command is line-granular and behaves identically to {@code
   * next} in a flat DGIR program (no callee to step into), firing exactly one {@code
   * stopped("step")} event per distinct source line.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Drain the initial {@code stopped("entry")} event.
   *   <li>Issue a single {@code stepIn} command and assert a {@code stopped("step")} event.
   *   <li>Resume with {@code continue}; wait for {@code exited}.
   * </ol>
   */
  @Test
  void endToEnd_stepIn_behavesLikeNext() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        session.client().awaitStopped(); // entry

        session.server().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments stepped = session.client().awaitStopped();
        assertEquals("step", stepped.getReason());

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
      }
    }
  }

  // =========================================================================
  // End-to-end: threads / stackTrace / scopes / variables
  // =========================================================================

  /**
   * Verifies that the {@code threads} request returns exactly one thread named {@code "main"} while
   * the VM is paused on entry.
   *
   * <p>The DGIR VM is single-threaded; the adapter always exposes a single logical thread with ID
   * {@code 1} and name {@code "main"}.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Await the entry-stop event.
   *   <li>Send a {@code threads} request and assert exactly one thread named {@code "main"}.
   *   <li>Resume and wait for {@code exited}.
   * </ol>
   */
  @Test
  void endToEnd_threads_returnsSingleThread() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        session.client().awaitStopped(); // entry

        ThreadsResponse threads = session.server().threads().get(5, TimeUnit.SECONDS);
        assertEquals(1, threads.getThreads().length);
        assertEquals("main", threads.getThreads()[0].getName());

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
      }
    }
  }

  /**
   * Verifies that the {@code stackTrace} request returns at least one frame with a non-null {@link
   * Source} and a positive frame ID while the VM is paused on entry.
   *
   * <p>The presence of a {@code Source} object on every frame is required by VS Code to enable
   * source highlighting in the editor when the debugger pauses.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Await the entry-stop event.
   *   <li>Send {@code stackTrace} for thread ID {@code 1}.
   *   <li>Assert {@code totalFrames > 0}, at least one stack frame is returned, every frame has a
   *       non-null {@code source}, and every frame has a positive {@code id}.
   *   <li>Resume and wait for {@code exited}.
   * </ol>
   */
  @Test
  void endToEnd_stackTrace_whilePaused_returnsFrames() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        session.client().awaitStopped(); // entry

        StackTraceArguments stArgs = new StackTraceArguments();
        stArgs.setThreadId(1);
        StackTraceResponse st = session.server().stackTrace(stArgs).get(5, TimeUnit.SECONDS);

        assertTrue(st.getTotalFrames() > 0);
        assertTrue(st.getStackFrames().length > 0);
        for (StackFrame f : st.getStackFrames()) {
          assertNotNull(f.getSource());
          assertTrue(f.getId() > 0);
        }

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
      }
    }
  }

  /**
   * Verifies that the {@code scopes} and {@code variables} requests return meaningful data after at
   * least one operation has executed and produced a binding.
   *
   * <p>After the entry stop, two step commands are issued: the first enters the {@code main}
   * function (executes the first op in {@code main}, {@code ConstantOp("A")}), and the second
   * executes the {@code PrintOp} on the next line. that produces the first visible local binding.
   * At that point the scopes/variables introspection should be populated.
   *
   * <p><b>Expected scopes:</b> exactly one scope named {@code "Locals"}.
   *
   * <p><b>Expected variables:</b> at least one variable, each with a non-null name and value.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Drain the entry-stop event.
   *   <li>Send {@code stepIn} (executes {@code ConstantOp("A")}); drain the resulting {@code
   *       stopped("step")} event.
   *   <li>Send {@code next} (executes {@code PrintOp("A")}); drain the resulting {@code
   *       stopped("step")} event.
   *   <li>Send {@code scopes} for frame ID {@code 1}; assert one scope named {@code "Locals"}.
   *   <li>Send {@code variables} for the locals scope; assert at least one variable with non-null
   *       name and value.
   *   <li>Resume and wait for {@code exited}.
   * </ol>
   */
  @Test
  void endToEnd_scopesAndVariables_whilePaused() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        session.client().awaitStopped(); // entry pause

        // Step into one to execute ConstantOp("A") and produce the first binding.
        session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitStopped(); // step

        // Step again — executes PrintOp("A"), creating the first visible binding.
        session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitStopped(); // step

        // Scopes
        ScopesArguments scopesArgs = new ScopesArguments();
        scopesArgs.setFrameId(1);
        ScopesResponse scopes = session.server().scopes(scopesArgs).get(5, TimeUnit.SECONDS);
        assertEquals(1, scopes.getScopes().length);
        assertEquals("Locals", scopes.getScopes()[0].getName());

        // Variables
        VariablesArguments varArgs = new VariablesArguments();
        varArgs.setVariablesReference(scopes.getScopes()[0].getVariablesReference());
        VariablesResponse vars = session.server().variables(varArgs).get(5, TimeUnit.SECONDS);
        assertNotNull(vars.getVariables());
        assertTrue(
            vars.getVariables().length >= 1, "Expected at least one variable after stepping");

        for (Variable v : vars.getVariables()) {
          assertNotNull(v.getName());
          assertNotNull(v.getValue());
        }

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
      }
    }
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
   *
   * <ol>
   *   <li>Connect and perform the {@code initialize} + {@code initialized} handshake.
   *   <li>Send {@code setExceptionBreakpoints} with an empty {@code filters} array.
   *   <li>Assert the response future completes with a non-null value (no exception thrown).
   *   <li>Complete the handshake with {@code launch} + {@code configurationDone}; wait for {@code
   *       exited}.
   * </ol>
   */
  @Test
  void endToEnd_setExceptionBreakpoints_isAccepted() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("x"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {

        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        session.server().initialize(initArgs).get(5, TimeUnit.SECONDS);
        session.client().awaitInitialized();

        SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
        args.setFilters(new String[0]);
        // Should not throw
        assertNotNull(session.server().setExceptionBreakpoints(args).get(5, TimeUnit.SECONDS));

        session.server().launch(Map.of()).get(5, TimeUnit.SECONDS);
        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
      }
    }
  }

  // =========================================================================
  // Multiple sequential clients (factory called per session)
  // =========================================================================

  /**
   * Verifies that the {@link DapServer} creates a fresh {@link VM} for each client connection by
   * calling the VM factory once per accepted session, and that each session completes independently
   * with exit code {@code 0}.
   *
   * <p>The server is shared across two sequential client connections. An internal counter tracks
   * how many times the factory lambda is invoked. Each client connection goes through the full DAP
   * handshake and waits for {@code exited} and {@code terminated} events before the next connection
   * is opened.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Create a server with a factory that increments a counter on every call and returns a new
   *       {@link VM} running a program named after the current call count.
   *   <li>Connect client 1, perform the full handshake, wait for {@code exited(exitCode=0)} and
   *       {@code terminated}.
   *   <li>Connect client 2, repeat the same handshake and assertions.
   *   <li>Assert the factory was called exactly twice.
   * </ol>
   */
  @Test
  void server_handlesMultipleSequentialClients() throws Exception {
    final int[] callCount = {0};
    try (DapServer server =
        new DapServer(
            0,
            () -> {
              callCount[0]++;
              VM vm = new VM();
              vm.init(simplePrintProgram("session-" + callCount[0]));
              return vm;
            })) {
      server.start();

      for (int i = 1; i <= 2; i++) {

        CollectingClient client = new CollectingClient();
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), server.getBoundPort());
        Launcher<IDebugProtocolServer> launcher =
            DSPLauncher.createClientLauncher(
                client, socket.getInputStream(), socket.getOutputStream());
        try (ClientSession<CollectingClient> session =
            new ClientSession<>(
                socket, client, launcher.getRemoteProxy(), launcher.startListening())) {
          InitializeRequestArguments initArgs = new InitializeRequestArguments();
          session.server().initialize(initArgs).get(5, TimeUnit.SECONDS);
          client.awaitInitialized();

          session.server().launch(Map.of()).get(5, TimeUnit.SECONDS);
          session
              .server()
              .configurationDone(new ConfigurationDoneArguments())
              .get(5, TimeUnit.SECONDS);

          ExitedEventArguments exited = client.awaitExited();
          assertEquals(0, exited.getExitCode(), "Session " + i + " should exit cleanly");
          client.awaitTerminated();
        }
      }
    }
    assertEquals(2, callCount[0], "vmFactory should have been called twice");
  }

  // =========================================================================
  // Overload program DAP tests
  // =========================================================================

  /**
   * The virtual source file path used by the overload test program ({@code
   * "functionCallWithOverload.java"}). Breakpoints in the overload tests are set against this path
   * so that the DAP {@code setBreakpoints} request and the IR operation locations agree.
   */
  static final String OVERLOAD_SOURCE = "functionCallWithOverload.java";

  /**
   * Verifies that the overload program runs to completion when started with the stop-on-entry
   * option, and that stepping over the first three lines reaches the fourth line.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect to a server running the overload program.
   *   <li>Perform the full DAP handshake with {@code stopOnEntry=true}.
   *   <li>Wait for the {@code stopped("entry")} event and assert the top line is 2.
   *   <li>Send two {@code next} requests and assert the top line advances to 4.
   *   <li>Send {@code continue}; wait for {@code exited} and {@code terminated}.
   * </ol>
   */
  @Test
  void endToEnd_overload_stopOnEntry_stepOver_reachesFourthLine() throws Exception {
    var serverAndSession = createServerAndConnect(functionCallsWithOverload());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        StoppedEventArguments entry = session.client().awaitStopped();
        assertEquals("entry", entry.getReason());
        assertEquals(3, topLine(session.server()));

        session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments firstStep = session.client().awaitStopped();
        assertEquals("step", firstStep.getReason());
        assertEquals(4, topLine(session.server()));

        session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments secondStep = session.client().awaitStopped();
        assertEquals("step", secondStep.getReason());
        assertEquals(5, topLine(session.server()));

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that a breakpoint set on line 3 of the overload program causes the adapter to pause
   * execution at that line, and that stepping over it then reaches line 4.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect to a server running the overload program.
   *   <li>Perform the initialize handshake.
   *   <li>Set a breakpoint on line 3 and launch the program with stop-on-entry.
   *   <li>Wait for the {@code stopped("entry")} event.
   *   <li>Send a {@code continue} request; wait for the {@code stopped("breakpoint")} event at line
   *       3.
   *   <li>Send a {@code next} request; assert the top line advances to 4.
   *   <li>Send {@code continue}; wait for {@code exited} and {@code terminated}.
   * </ol>
   */
  @Test
  void endToEnd_overload_breakpointLine3_thenStepOver_reachesLine4() throws Exception {
    var serverAndSession = createServerAndConnect(functionCallsWithOverload());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        initializeHandshake(session);
        setBreakpointsOnLines(session.server(), OVERLOAD_SOURCE, 3);
        launchAndConfigDone(session, true);

        StoppedEventArguments entry = session.client().awaitStopped();
        assertEquals("entry", entry.getReason());
        assertEquals(3, topLine(session.server()));

        session.server().next(new NextArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments stepped = session.client().awaitStopped();
        assertEquals("step", stepped.getReason());
        assertEquals(4, topLine(session.server()));

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that breakpoints set on lines 3 and 8 of the overload program are hit in sequence when
   * stepping into the {@code add(int, int)} callee and then stepping out again.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect to a server running the overload program.
   *   <li>Perform the initialize handshake.
   *   <li>Set breakpoints on lines 3 and 8, then launch the program.
   *   <li>Wait for the breakpoint at line 3.
   *   <li>Send a {@code stepIn} request; wait for the breakpoint at line 8 or a step event.
   *   <li>Send a {@code stepOut} request; wait for the next step event.
   *   <li>Send {@code continue}; wait for {@code exited} and {@code terminated}.
   * </ol>
   */
  @Test
  void endToEnd_overload_breakpoints3and8_stepIn_and_stepOut() throws Exception {
    var serverAndSession = createServerAndConnect(functionCallsWithOverload());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        initializeHandshake(session);
        setBreakpointsOnLines(session.server(), OVERLOAD_SOURCE, 3, 8);
        launchAndConfigDone(session, false);

        StoppedEventArguments bp3 = session.client().awaitStopped();
        assertEquals("breakpoint", bp3.getReason());
        assertEquals(3, topLine(session.server()));

        session.server().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments inCallee = session.client().awaitStopped();
        assertEquals("step", inCallee.getReason(), "stepIn into add(int,int) should pause by step");
        assertEquals(8, topLine(session.server()));

        session.server().stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments breakpoint8 = session.client().awaitStopped();
        assertEquals(
            "breakpoint", breakpoint8.getReason(), "step should pause at breakpoint line 8");
        assertEquals(8, topLine(session.server()));

        session.server().stepOut(new StepOutArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments outToCaller = session.client().awaitStopped();
        assertEquals("step", outToCaller.getReason());
        assertEquals(4, topLine(session.server()));

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  // =========================================================================
  // Live reload
  // =========================================================================

  /**
   * Verifies that {@link DapServer#reloadProgram} replaces a program that is paused on entry and
   * runs the new program to completion, firing {@code exited(exitCode=0)} and {@code terminated}.
   *
   * <p>This is the primary "run again" scenario: the user (or blockly frontend) submits a new
   * program while the previous one is still paused at the DAP entry-stop.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Drain the {@code stopped("entry")} event — VM is now paused.
   *   <li>Call {@link DapServer#reloadProgram} with a different program.
   *   <li>Wait for the {@code initialized} event that signals the adapter is ready for new config.
   *   <li>Send {@code configurationDone} to unblock the new VM thread.
   *   <li>Wait for {@code exited(0)} and {@code terminated}.
   * </ol>
   */
  @Test
  void reload_whilePausedOnEntry_newProgramRunsToCompletion() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("first"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);

        StoppedEventArguments entry = session.client().awaitStopped();
        assertEquals("entry", entry.getReason(), "Should stop at entry of first program");

        // Reload while the VM is paused — replaces execution entirely.
        server.reloadProgram(simplePrintProgram("second"));

        // Adapter fires 'initialized' so the client can re-register breakpoints.
        session.client().awaitInitialized();

        // Complete the DAP configuration handshake for the new program.
        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);

        ExitedEventArguments exited = session.client().awaitExited();
        assertEquals(0, exited.getExitCode(), "Reloaded program should exit cleanly");
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that {@link DapServer#reloadProgram} does <em>not</em> fire a {@code terminated} event
   * to the connected DAP client, ensuring the VS Code debug session remains open.
   *
   * <p>A {@code terminated} event from the adapter signals VS Code to close the debug panel. For
   * the blockly use case the session must survive across multiple program reloads.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true}.
   *   <li>Drain the entry-stop event.
   *   <li>Call {@link DapServer#reloadProgram}.
   *   <li>Wait for the {@code initialized} event confirming the reload has completed.
   *   <li>Assert that <em>no</em> {@code terminated} event arrived before {@code initialized}.
   *   <li>Clean up by sending {@code configurationDone} and draining the remaining events.
   * </ol>
   */
  @Test
  void reload_doesNotFireTerminatedEvent_sessionStaysAlive() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("first"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);
        session.client().awaitStopped(); // drain entry stop

        server.reloadProgram(simplePrintProgram("second"));

        // Poll for terminated with a short window — it must NOT have been sent.
        TerminatedEventArguments spurious = session.client().tryAwaitTerminated(200);
        assertNull(spurious, "reloadProgram() must not fire 'terminated' to the DAP client");

        // Confirm reload completed by checking that 'initialized' did arrive.
        session.client().awaitInitialized();

        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);
        session.client().awaitExited();
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that {@link DapServer#reloadProgram} correctly replaces a program that is paused at a
   * source breakpoint, and that the new program runs to completion.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect, initialize, set a breakpoint on line 4 of {@code test.dgir}, then launch.
   *   <li>Wait for the {@code stopped("breakpoint")} event — VM is paused at the breakpoint.
   *   <li>Call {@link DapServer#reloadProgram} with a new program.
   *   <li>Wait for {@code initialized}, send {@code configurationDone}.
   *   <li>Wait for {@code exited(0)} and {@code terminated}.
   * </ol>
   */
  @Test
  void reload_whilePausedAtBreakpoint_newProgramRunsToCompletion() throws Exception {
    var serverAndSession = createServerAndConnect(multiLinePrintProgram());
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        initializeHandshake(session);
        setBreakpointsOnLines(session.server(), "test.dgir", 4);
        launchAndConfigDone(session, false);

        StoppedEventArguments bp = session.client().awaitStopped();
        assertEquals("breakpoint", bp.getReason(), "Should pause at the breakpoint");

        // Replace the paused program.
        server.reloadProgram(simplePrintProgram("after-breakpoint-reload"));

        session.client().awaitInitialized();
        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);

        ExitedEventArguments exited = session.client().awaitExited();
        assertEquals(0, exited.getExitCode());
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that reloading with {@code stopOnEntry=true} causes the <em>new</em> program to pause
   * before its first operation, delivering a {@code stopped("entry")} event.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and perform the full handshake with {@code stopOnEntry=true} for program 1.
   *   <li>Drain the first entry-stop.
   *   <li>Call {@link DapServer#reloadProgram} with program 2, passing {@code stopOnEntry=true}.
   *   <li>Wait for {@code initialized} and send {@code configurationDone}.
   *   <li>Assert that a new {@code stopped("entry")} event fires for program 2.
   *   <li>Resume and wait for {@code exited(0)} and {@code terminated}.
   * </ol>
   */
  @Test
  void reload_withStopOnEntry_pausesNewProgramAtEntry() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("first"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);
        session.client().awaitStopped(); // drain entry stop for first program

        // Reload with stopOnEntry so the new program also pauses.
        server.reloadProgram(multiLinePrintProgram(), /* stopOnEntry= */ true);

        session.client().awaitInitialized();
        session
            .server()
            .configurationDone(new ConfigurationDoneArguments())
            .get(5, TimeUnit.SECONDS);

        StoppedEventArguments secondEntry = session.client().awaitStopped();
        assertEquals(
            "entry",
            secondEntry.getReason(),
            "Reloaded program must deliver a stopped('entry') event when stopOnEntry=true");

        session.server().continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        ExitedEventArguments exited = session.client().awaitExited();
        assertEquals(0, exited.getExitCode());
        session.client().awaitTerminated();
      }
    }
  }

  /**
   * Verifies that three consecutive reloads each run a distinct program to completion, with the DAP
   * session remaining open throughout all reloads.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Connect and pause program 1 on entry.
   *   <li>Reload with program 2; complete the handshake; assert {@code exited(0)} and {@code
   *       terminated}.
   *   <li>Reload with program 3 immediately after; complete the handshake; assert {@code exited(0)}
   *       and {@code terminated}.
   *   <li>Reload with program 4 immediately after; complete the handshake; assert {@code exited(0)}
   *       and {@code terminated}.
   * </ol>
   */
  @Test
  void reload_multipleSequential_eachRunsToCompletion() throws Exception {
    var serverAndSession = createServerAndConnect(simplePrintProgram("prog-0"));
    try (DapServer server = serverAndSession.getLeft()) {
      try (ClientSession<CollectingClient> session = serverAndSession.getRight()) {
        fullHandshake(session, true);
        session.client().awaitStopped(); // entry stop for prog-0

        for (int i = 1; i <= 3; i++) {
          server.reloadProgram(simplePrintProgram("prog-" + i));

          session.client().awaitInitialized();
          session
              .server()
              .configurationDone(new ConfigurationDoneArguments())
              .get(5, TimeUnit.SECONDS);

          ExitedEventArguments exited = session.client().awaitExited();
          assertEquals(0, exited.getExitCode(), "prog-" + i + " should exit cleanly");
          session.client().awaitTerminated();
        }
      }
    }
  }

  /**
   * Verifies that {@link DapServer#reloadProgram} executes a program headlessly when no DAP client
   * is connected, without throwing any exceptions.
   *
   * <p>When no VS Code (or other DAP client) has attached yet, the server must still be able to run
   * programs on behalf of the blockly frontend. The program is executed on a daemon thread without
   * any DAP protocol events.
   *
   * <p>A custom VM subclass is used to detect when {@link dgir.vm.api.VM#run()} completes so the
   * test can await completion without relying on a fixed sleep.
   *
   * <p><b>Steps:</b>
   *
   * <ol>
   *   <li>Create and start a server whose factory produces instrumented VMs.
   *   <li>Call {@link DapServer#reloadProgram} <em>before</em> any client connects.
   *   <li>Await the completion latch (max 5 s).
   *   <li>Assert the program ran to completion ({@code exitOk == true}).
   * </ol>
   */
  @Test
  void reload_headless_noClientConnected_programRuns() throws Exception {
    CountDownLatch done = new CountDownLatch(1);
    boolean[] exitOk = {false};

    // Instrumented VM that signals the latch when run() finishes.
    VM instrumentedVm =
        new VM() {
          @Override
          public boolean run() {
            boolean ok = super.run();
            exitOk[0] = ok;
            done.countDown();
            return ok;
          }
        };

    try (DapServer server = new DapServer(0, () -> instrumentedVm)) {
      server.start();

      // No client has connected — reloadProgram must run headlessly.
      server.reloadProgram(simplePrintProgram("headless-run"));

      assertTrue(done.await(5, TimeUnit.SECONDS), "Headless VM should complete within 5 s");
      assertTrue(exitOk[0], "Headless program should exit successfully");
    }
  }
}
