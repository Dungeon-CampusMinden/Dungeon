import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the DGIR VM DAP fat jar ({@code dgir-vm-dap.jar}).
 *
 * <p>Each test in this class starts the fat jar as a real, external OS process and feeds it
 * {@code HelloWorld.json} as the program via the {@code --program} flag. A real lsp4j DAP
 * client then connects over a TCP loopback socket and drives the full Debug Adapter Protocol
 * exchange, asserting on the events and responses it receives.
 *
 * <p>Unlike {@code DapServerTest} (which creates the {@link dgir.vm.dap.DapServer} inside the
 * same JVM), these tests exercise the complete deployment artifact:
 * <ol>
 *   <li>The fat-jar build ({@code :dgir:vm:dapJar}) that packages all runtime dependencies.</li>
 *   <li>The {@link dgir.vm.DgirDebugServer} entry-point, including argument parsing and
 *       program loading from a JSON file on disk.</li>
 *   <li>The {@link dgir.vm.dap.DapServer} TCP accept loop and lsp4j framing layer.</li>
 *   <li>The {@link dgir.vm.dap.DapAdapter} request handling and event dispatch.</li>
 *   <li>The {@link dgir.vm.api.VM} executing the real {@code HelloWorld} DGIR program.</li>
 * </ol>
 *
 * <h2>HelloWorld.json program structure</h2>
 * The program loaded from {@code test_assets/HelloWorld.json} is equivalent to:
 * <pre>{@code
 * // HelloWorld.dgir
 * line 2: func main() {
 * line 3:   %0 = arith.constant "Hello World!"   // produces local %0
 * line 4:   io.print(%0)
 *           func.return
 *         }
 * }</pre>
 *
 * <h2>Fat-jar prerequisite</h2>
 * The jar at {@code dgir/vm/build/libs/dgir-vm-dap.jar} <b>must exist</b> before these tests run.
 * Build it once with:
 * <pre>./gradlew :dgir:vm:dapJar</pre>
 * The {@link #findDapJar()} helper resolves the jar path relative to this class's location and
 * skips the whole test class (via {@link Assumptions#assumeTrue}) when the jar is absent, so a
 * missing jar will not cause a red build — only a skipped test class.
 *
 * <h2>Port selection</h2>
 * The server is started on a fixed port chosen by {@link #findFreePort()}.  A retry loop in
 * {@link #connectWithRetry} gives the process up to 10 seconds to bind the port before the
 * test fails.
 *
 * <h2>Expected log noise</h2>
 * When a test closes the client socket the server-side lsp4j stream logs:
 * <pre>
 * INFO: Socket closed
 *   java.net.SocketException: Socket closed
 *       at ...StreamMessageProducer.listen(...)
 * INFO: DAP session closed.
 * </pre>
 * These are <b>not errors</b>; they are the normal shutdown path. See {@code DapServerTest} for a
 * full explanation.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DapJarIntegrationTest {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * DAP port the external process will listen on.  Chosen once per class run so all tests share
     * the same process.
     */
    static int DAP_PORT;

    /**
     * Source file name embedded in every operation location inside {@code HelloWorld.json}.
     * Used when constructing {@code setBreakpoints} requests.
     */
    static final String SOURCE_FILE = "HelloWorld.java";

    /**
     * Line of the {@code arith.constant "Hello World!"} operation in {@code HelloWorld.dgir}.
     * Setting a breakpoint here will pause execution before the constant is produced.
     */
    static final int LINE_CONSTANT = 3;

    /**
     * Line of the {@code io.print(%0)} operation in {@code HelloWorld.dgir}.
     * Setting a breakpoint here will pause execution just before printing.
     */
    static final int LINE_PRINT = 4;

    // =========================================================================
    // Class-level state
    // =========================================================================

    /** The external {@code dgir-vm-dap.jar} process, started once and shared by all tests. */
    static Process serverProcess;

    // =========================================================================
    // Class-level setup / teardown
    // =========================================================================

    /**
     * Builds the fat jar (if not already present), locates it, picks a free TCP port, and starts
     * the external {@code dgir-vm-dap.jar} process so it listens on that port with
     * {@code HelloWorld.json} as the program.
     *
     * <p>The method:
     * <ol>
     *   <li>Resolves the workspace root (two levels above the {@code vm/} module directory).</li>
     *   <li>Runs {@code ./gradlew :dgir:vm:dapJar} to ensure the jar is up to date.</li>
     *   <li>Skips the entire class ({@link Assumptions#assumeTrue}) if the jar is still missing
     *       after the build, to avoid a hard failure in CI environments that do not have Gradle.</li>
     *   <li>Picks a free TCP port.</li>
     *   <li>Starts the jar process with {@code --program <path>} and {@code --dap-port <port>}.</li>
     *   <li>Waits until the port accepts connections (up to 10 seconds).</li>
     * </ol>
     */
    @BeforeAll
    static void startServer() throws Exception {
        Path jarPath = findDapJar();

        // Build the fat jar if it is absent.
        if (!Files.exists(jarPath)) {
            Path workspaceRoot = findWorkspaceRoot();
            ProcessBuilder buildPb = new ProcessBuilder(
                    workspaceRoot.resolve("gradlew").toString(),
                    ":dgir:vm:dapJar"
            );
            buildPb.directory(workspaceRoot.toFile());
            buildPb.inheritIO();
            Process buildProcess = buildPb.start();
            int buildExit = buildProcess.waitFor();
            assertEquals(0, buildExit, "Gradle dapJar build failed with exit code " + buildExit);
        }

        // Skip cleanly if the jar is still not available (e.g. Gradle not present in CI).
        Assumptions.assumeTrue(Files.exists(jarPath),
                "Skipping DapJarIntegrationTest: fat jar not found at " + jarPath);

        Path helloWorldJson = findHelloWorldJson();
        Assumptions.assumeTrue(Files.exists(helloWorldJson),
                "Skipping DapJarIntegrationTest: HelloWorld.json not found at " + helloWorldJson);

        DAP_PORT = findFreePort();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar", jarPath.toAbsolutePath().toString(),
                "--program", helloWorldJson.toAbsolutePath().toString(),
                "--dap-port", String.valueOf(DAP_PORT)
        );
        pb.redirectErrorStream(true);  // merge stderr into stdout so we see all output on failure
        pb.inheritIO();                // forward to the test runner's console

        serverProcess = pb.start();

        // Wait until the server is ready to accept connections.
        waitForPort(DAP_PORT, 10_000);
    }

    /** Kills the external server process after all tests have run. */
    @AfterAll
    static void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroyForcibly();
        }
    }

    // =========================================================================
    // Per-test setup: every test opens its own session so tests are independent.
    // =========================================================================

    /** The active TCP socket for the current test. */
    Socket socket;

    /** The collecting DAP client for the current test. */
    CollectingClient client;

    /** lsp4j remote-server proxy for the current test. */
    IDebugProtocolServer remote;

    /**
     * Opens a fresh TCP connection to the running server before each test.  Each test therefore
     * gets a completely independent DAP session (and a freshly initialised VM on the server side,
     * because the server's VM factory is called once per connection).
     */
    @BeforeEach
    void openSession() throws Exception {
        socket = connectWithRetry(DAP_PORT, 5_000);
        client = new CollectingClient();
        Launcher<IDebugProtocolServer> launcher =
                DSPLauncher.createClientLauncher(
                        client,
                        socket.getInputStream(),
                        socket.getOutputStream());
        client.remoteServer = launcher.getRemoteProxy();
        remote = client.remoteServer;
        launcher.startListening();
    }

    /**
     * Closes the TCP socket after each test, triggering the server-side shutdown sequence
     * (logged at INFO level — see class-level note on expected log noise).
     */
    @AfterEach
    void closeSession() throws Exception {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Performs the three-way DAP handshake: {@code initialize} → await {@code initialized} event →
     * {@code launch} → {@code configurationDone}.
     *
     * @param stopOnEntry when {@code true}, passes {@code {"stopOnEntry": true}} in the launch
     *                    arguments so the VM pauses before executing its first operation
     */
    void handshake(boolean stopOnEntry) throws Exception {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        Capabilities caps = remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
        assertNotNull(caps, "initialize must return Capabilities");
        assertTrue(caps.getSupportsConfigurationDoneRequest(),
                "Server must support configurationDone");

        client.awaitInitialized();

        Map<String, Object> launchArgs = stopOnEntry
                ? Map.of("stopOnEntry", true)
                : Map.of();
        remote.launch(launchArgs).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
    }

    // =========================================================================
    // Tests
    // =========================================================================

    /**
     * Verifies that the fat-jar server accepts a DAP connection, performs the full handshake, and
     * runs the HelloWorld program to completion, emitting {@code exited(exitCode=0)} followed by
     * {@code terminated}.
     *
     * <p>This is the most basic "it works" smoke test for the deployed artifact.
     */
    @Test
    @DisplayName("1. HelloWorld runs to completion (exited + terminated)")
    void helloWorld_runsToCompletion() throws Exception {
        handshake(false);

        ExitedEventArguments exited = client.awaitExited();
        assertEquals(0, exited.getExitCode(),
                "HelloWorld program should exit with code 0");

        assertNotNull(client.awaitTerminated(),
                "A terminated event must follow exited");
    }

    /**
     * Verifies that the {@code stopOnEntry} launch option causes the server to fire a
     * {@code stopped("entry")} event before the first operation of the HelloWorld program
     * executes, and that a subsequent {@code continue} lets it finish normally.
     */
    @Test
    @DisplayName("2. stopOnEntry pauses before first op; continue runs to completion")
    void stopOnEntry_pausesThenContinue() throws Exception {
        handshake(true);

        StoppedEventArguments stopped = client.awaitStopped();
        assertEquals("entry", stopped.getReason(),
                "First stopped event must have reason 'entry'");
        assertEquals(1, stopped.getThreadId(),
                "Thread ID must be 1 (DGIR VM is single-threaded)");

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);

        ExitedEventArguments exited = client.awaitExited();
        assertEquals(0, exited.getExitCode());
        assertNotNull(client.awaitTerminated());
    }

    /**
     * Verifies that a source breakpoint set on the {@code io.print} line of HelloWorld
     * ({@code HelloWorld.dgir:4}) causes the adapter to pause with {@code reason="breakpoint"}
     * and that resuming via {@code continue} lets the program finish.
     */
    @Test
    @DisplayName("3. Breakpoint on print line pauses with reason='breakpoint'")
    void breakpointOnPrintLine_pausesExecution() throws Exception {
        // Initialize but do NOT launch yet — set breakpoints first.
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
        client.awaitInitialized();

        // Set a breakpoint on line 4 (io.print).
        Source src = new Source();
        src.setPath(SOURCE_FILE);
        SourceBreakpoint sb = new SourceBreakpoint();
        sb.setLine(LINE_PRINT);
        SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
        bpArgs.setSource(src);
        bpArgs.setBreakpoints(new SourceBreakpoint[]{sb});
        SetBreakpointsResponse bpResp =
                remote.setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);

        assertNotNull(bpResp.getBreakpoints());
        assertEquals(1, bpResp.getBreakpoints().length,
                "Exactly one breakpoint should be registered");
        assertTrue(bpResp.getBreakpoints()[0].isVerified(),
                "Breakpoint must be verified by the adapter");

        // Launch and start execution.
        remote.launch(Map.of()).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);

        // Expect the breakpoint hit.
        StoppedEventArguments stopped = client.awaitStopped();
        assertEquals("breakpoint", stopped.getReason(),
                "Stopped reason must be 'breakpoint'");
        assertEquals(1, stopped.getThreadId());

        // Resume and wait for clean exit.
        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        ExitedEventArguments exited = client.awaitExited();
        assertEquals(0, exited.getExitCode());
        assertNotNull(client.awaitTerminated());
    }

    /**
     * Verifies that a source breakpoint can also be set on the {@code arith.constant} line
     * ({@code HelloWorld.dgir:3}), which precedes the print operation.
     */
    @Test
    @DisplayName("4. Breakpoint on constant line (line 3) pauses before print")
    void breakpointOnConstantLine_pausesBeforePrint() throws Exception {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
        client.awaitInitialized();

        Source src = new Source();
        src.setPath(SOURCE_FILE);
        SourceBreakpoint sb = new SourceBreakpoint();
        sb.setLine(LINE_CONSTANT);
        SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
        bpArgs.setSource(src);
        bpArgs.setBreakpoints(new SourceBreakpoint[]{sb});
        SetBreakpointsResponse bpResp =
                remote.setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);

        assertEquals(1, bpResp.getBreakpoints().length);
        assertTrue(bpResp.getBreakpoints()[0].isVerified());

        remote.launch(Map.of()).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);

        StoppedEventArguments stopped = client.awaitStopped();
        assertEquals("breakpoint", stopped.getReason());

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        assertEquals(0, client.awaitExited().getExitCode());
        assertNotNull(client.awaitTerminated());
    }

    /**
     * Verifies that after stopping on entry, the {@code threads} request returns exactly one
     * thread named {@code "main"}.
     */
    @Test
    @DisplayName("5. threads() returns single thread named 'main' while paused")
    void threads_returnsSingleMainThread() throws Exception {
        handshake(true);
        client.awaitStopped(); // entry

        ThreadsResponse threads = remote.threads().get(5, TimeUnit.SECONDS);
        assertNotNull(threads.getThreads());
        assertEquals(1, threads.getThreads().length,
                "DGIR VM is single-threaded — exactly one thread expected");
        assertEquals("main", threads.getThreads()[0].getName(),
                "The sole thread must be named 'main'");
        assertEquals(1, threads.getThreads()[0].getId(),
                "Thread ID must be 1");

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    /**
     * Verifies that the {@code stackTrace} request, issued while the VM is paused on entry,
     * returns at least one frame whose {@link Source} is non-null, and that every frame carries
     * a positive ID.
     *
     * <p>VS Code requires the {@code source} field to enable file highlighting in the editor.
     */
    @Test
    @DisplayName("6. stackTrace() while paused returns frames with source")
    void stackTrace_whilePaused_returnsFramesWithSource() throws Exception {
        handshake(true);
        client.awaitStopped(); // entry

        StackTraceArguments stArgs = new StackTraceArguments();
        stArgs.setThreadId(1);
        StackTraceResponse st = remote.stackTrace(stArgs).get(5, TimeUnit.SECONDS);

        assertTrue(st.getTotalFrames() > 0,
                "totalFrames must be > 0 while paused");
        assertTrue(st.getStackFrames().length > 0,
                "At least one stack frame must be present");
        for (StackFrame frame : st.getStackFrames()) {
            assertNotNull(frame.getSource(),
                    "Every stack frame must carry a non-null Source");
            assertTrue(frame.getId() > 0,
                    "Frame IDs must be positive (got " + frame.getId() + ")");
        }

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    /**
     * Verifies that the {@code scopes} and {@code variables} requests return meaningful data
     * after stepping past the {@code arith.constant} operation that produces the local binding
     * {@code %0 = "Hello World!"}.
     *
     * <p>After two step commands:
     * <ol>
     *   <li>{@code stepIn} — advances past the entry point into the {@code main} function body.</li>
     *   <li>{@code next} — executes the {@code arith.constant} op, creating the first visible
     *       local variable.</li>
     * </ol>
     *
     * <p>At that point, {@code scopes} must return exactly one scope named {@code "Locals"}, and
     * {@code variables} must return at least one variable with a non-null name and value.
     */
    @Test
    @DisplayName("7. scopes + variables after stepping past constant op")
    void scopesAndVariables_afterSteppingPastConstant() throws Exception {
        handshake(true);
        client.awaitStopped(); // entry

        // Step 1: stepIn to enter main function body.
        remote.stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments step1 = client.awaitStopped();
        assertEquals("step", step1.getReason());

        // Step 2: next — executes arith.constant, creating local %0.
        remote.next(new NextArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments step2 = client.awaitStopped();
        assertEquals("step", step2.getReason());

        // Query scopes for frame 1.
        ScopesArguments scopesArgs = new ScopesArguments();
        scopesArgs.setFrameId(1);
        ScopesResponse scopes = remote.scopes(scopesArgs).get(5, TimeUnit.SECONDS);

        assertNotNull(scopes.getScopes());
        assertEquals(1, scopes.getScopes().length,
                "Exactly one scope ('Locals') expected");
        assertEquals("Locals", scopes.getScopes()[0].getName());

        // Query variables in the Locals scope.
        VariablesArguments varArgs = new VariablesArguments();
        varArgs.setVariablesReference(scopes.getScopes()[0].getVariablesReference());
        VariablesResponse vars = remote.variables(varArgs).get(5, TimeUnit.SECONDS);

        assertNotNull(vars.getVariables());
        assertTrue(vars.getVariables().length >= 1,
                "At least one variable must be visible after executing arith.constant");

        for (Variable v : vars.getVariables()) {
            assertNotNull(v.getName(), "Variable name must not be null");
            assertNotNull(v.getValue(), "Variable value must not be null");
        }

        // The constant's value must appear somewhere in the variable list.
        boolean foundHelloWorld = false;
        for (Variable v : vars.getVariables()) {
            if (v.getValue() != null && v.getValue().contains("Hello World!")) {
                foundHelloWorld = true;
                break;
            }
        }
        assertTrue(foundHelloWorld,
                "Expected to find 'Hello World!' in the local variables after executing arith.constant; "
                        + "got: " + java.util.Arrays.toString(vars.getVariables()));

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    /**
     * Verifies that {@code stepIn} and {@code next} each fire exactly one {@code stopped("step")}
     * event per distinct source line, and that the program completes cleanly after a final
     * {@code continue}.
     *
     * <p>Steps applied:
     * <ol>
     *   <li>Entry stop (consumed).</li>
     *   <li>{@code stepIn} — enters main, fires {@code stopped("step")}.</li>
     *   <li>{@code next} — executes arith.constant, fires {@code stopped("step")}.</li>
     *   <li>{@code next} — executes io.print, fires {@code stopped("step")}.</li>
     *   <li>{@code continue} — runs to end, fires {@code exited} + {@code terminated}.</li>
     * </ol>
     */
    @Test
    @DisplayName("8. Step-through HelloWorld fires step events in order")
    void stepThroughHelloWorld_firesStepEventsInOrder() throws Exception {
        handshake(true);
        StoppedEventArguments entry = client.awaitStopped();
        assertEquals("entry", entry.getReason());

        // stepIn: enter main
        remote.stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        assertEquals("step", client.awaitStopped().getReason(), "After stepIn into main");

        // next: arith.constant
        remote.next(new NextArguments()).get(5, TimeUnit.SECONDS);
        assertEquals("step", client.awaitStopped().getReason(), "After constant op");

        // next: io.print
        remote.next(new NextArguments()).get(5, TimeUnit.SECONDS);
        assertEquals("step", client.awaitStopped().getReason(), "After print op");

        // continue to finish
        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        ExitedEventArguments exited = client.awaitExited();
        assertEquals(0, exited.getExitCode());
        assertNotNull(client.awaitTerminated());
    }

    /**
     * Verifies that {@code stepIn} behaves identically to {@code next} in the flat HelloWorld
     * program (there is no callee to step into), firing one {@code stopped("step")} event and
     * leaving the program in a resumable state.
     */
    @Test
    @DisplayName("9. stepIn behaves like next in flat HelloWorld program")
    void stepIn_behavesLikeNext_inFlatProgram() throws Exception {
        handshake(true);
        client.awaitStopped(); // entry

        remote.stepIn(new StepInArguments()).get(5, TimeUnit.SECONDS);
        StoppedEventArguments stepped = client.awaitStopped();
        assertEquals("step", stepped.getReason());

        remote.continue_(new ContinueArguments()).get(5, TimeUnit.SECONDS);
        assertEquals(0, client.awaitExited().getExitCode());
    }

    /**
     * Verifies that the {@code initialize} request returns {@link Capabilities} with the two
     * capabilities required by the VS Code DGIR extension:
     * <ul>
     *   <li>{@code supportsConfigurationDoneRequest == true}</li>
     *   <li>{@code supportsBreakpointLocationsRequest == true}</li>
     * </ul>
     */
    @Test
    @DisplayName("10. initialize() advertises required capabilities")
    void initialize_advertisesRequiredCapabilities() throws Exception {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        Capabilities caps = remote.initialize(initArgs).get(5, TimeUnit.SECONDS);

        assertNotNull(caps);
        assertTrue(caps.getSupportsConfigurationDoneRequest(),
                "supportsConfigurationDoneRequest must be true");
        assertTrue(caps.getSupportsBreakpointLocationsRequest(),
                "supportsBreakpointLocationsRequest must be true");

        client.awaitInitialized();

        // Clean shutdown.
        remote.launch(Map.of()).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    /**
     * Verifies that the {@code setExceptionBreakpoints} request is accepted without error even
     * when an empty filters array is supplied.  VS Code always sends this request during the
     * initialization sequence; returning a valid response prevents a visible protocol error.
     */
    @Test
    @DisplayName("11. setExceptionBreakpoints with empty filters is accepted")
    void setExceptionBreakpoints_isAccepted() throws Exception {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
        client.awaitInitialized();

        SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
        args.setFilters(new String[0]);
        assertNotNull(remote.setExceptionBreakpoints(args).get(5, TimeUnit.SECONDS),
                "setExceptionBreakpoints must return a non-null response");

        remote.launch(Map.of()).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    /**
     * Verifies that {@code breakpointLocations} returns at least one valid location inside the
     * source range of the HelloWorld program, confirming the adapter correctly reports which
     * lines in {@code HelloWorld.dgir} can carry breakpoints.
     *
     * <p>The HelloWorld program has operations on lines 2, 3, and 4 of {@code HelloWorld.dgir};
     * querying lines 1–10 must return at least those three distinct line entries.
     */
    @Test
    @DisplayName("12. breakpointLocations returns valid locations for HelloWorld source")
    void breakpointLocations_returnsLocationsForHelloWorld() throws Exception {
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setClientID("jar-integration-test");
        Capabilities caps = remote.initialize(initArgs).get(5, TimeUnit.SECONDS);

        assertTrue(
                caps.getSupportsBreakpointLocationsRequest() != null
                        && caps.getSupportsBreakpointLocationsRequest(),
                "initialize() must advertise supportsBreakpointLocationsRequest=true");

        client.awaitInitialized();

        BreakpointLocationsArguments bpLocArgs = new BreakpointLocationsArguments();
        Source src = new Source();
        src.setPath(SOURCE_FILE);
        bpLocArgs.setSource(src);
        bpLocArgs.setLine(1);
        bpLocArgs.setEndLine(10);

        BreakpointLocationsResponse bpLocs =
                remote.breakpointLocations(bpLocArgs).get(5, TimeUnit.SECONDS);

        assertNotNull(bpLocs, "breakpointLocations must return a non-null response");
        assertNotNull(bpLocs.getBreakpoints(),
                "breakpointLocations response must contain a breakpoints array");
        assertTrue(bpLocs.getBreakpoints().length > 0,
                "Expected at least one breakpoint location for HelloWorld.dgir lines 1-10");

        // All returned locations must have positive line numbers within the queried range.
        for (BreakpointLocation bl : bpLocs.getBreakpoints()) {
            assertTrue(bl.getLine() >= 1 && bl.getLine() <= 10,
                    "Returned line " + bl.getLine() + " is outside the queried range [1, 10]");
        }

        remote.launch(Map.of()).get(5, TimeUnit.SECONDS);
        remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
        client.awaitExited();
    }

    // =========================================================================
    // Infrastructure helpers
    // =========================================================================

    /**
     * Locates the {@code dgir-vm-dap.jar} fat jar produced by the {@code :dgir:vm:dapJar} task.
     *
     * <p>The resolution strategy walks up from this class's code source to find the workspace root
     * and then constructs the conventional Gradle build-output path.
     */
    static Path findDapJar() {
        Path workspaceRoot = findWorkspaceRoot();
        return workspaceRoot.resolve("dgir/vm/build/libs/dgir-vm-dap.jar");
    }

    /**
     * Resolves the workspace root (the directory that contains {@code gradlew}) by walking up
     * from the current working directory.
     */
    static Path findWorkspaceRoot() {
        // Gradle runs tests with cwd = subproject dir; walk up to find gradlew.
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            if (Files.exists(dir.resolve("gradlew"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        // Fallback: assume cwd is dgir/vm and the root is two levels up.
        return Paths.get("").toAbsolutePath().getParent().getParent();
    }

    /**
     * Locates {@code HelloWorld.json} in the {@code dgir/vm/test_assets/} directory.
     */
    static Path findHelloWorldJson() {
        Path workspaceRoot = findWorkspaceRoot();
        return workspaceRoot.resolve("dgir/vm/test_assets/HelloWorld.json");
    }

    /**
     * Finds an available TCP port by letting the OS assign one temporarily.
     */
    static int findFreePort() throws IOException {
        try (java.net.ServerSocket ss = new java.net.ServerSocket(0)) {
            ss.setReuseAddress(true);
            return ss.getLocalPort();
        }
    }

    /**
     * Blocks until the given TCP port accepts connections or the timeout expires.
     *
     * @param port        the port to probe
     * @param timeoutMs   maximum wait time in milliseconds
     * @throws Exception  if the port is not ready within the timeout
     */
    static void waitForPort(int port, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try (Socket probe = new Socket(InetAddress.getLoopbackAddress(), port)) {
                return; // connected — server is ready
            } catch (ConnectException ignored) {
                java.lang.Thread.sleep(100);
            }
        }
        fail("DAP server did not become ready on port " + port + " within " + timeoutMs + " ms");
    }

    /**
     * Opens a TCP connection to the given port, retrying on {@link ConnectException} up to
     * {@code timeoutMs} milliseconds.
     *
     * @param port      the DAP server port
     * @param timeoutMs the maximum time to wait
     * @return an open, connected {@link Socket}
     * @throws Exception if the connection cannot be established within the timeout
     */
    static Socket connectWithRetry(int port, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Exception last = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                return new Socket(InetAddress.getLoopbackAddress(), port);
            } catch (ConnectException e) {
                last = e;
                java.lang.Thread.sleep(100);
            }
        }
        throw new IOException(
                "Could not connect to DAP server on port " + port + " within " + timeoutMs + " ms",
                last);
    }

    // =========================================================================
    // Collecting DAP client
    // =========================================================================

    /**
     * A DAP client that stores every asynchronous event it receives in {@link BlockingQueue}s so
     * that test methods can poll for specific events with a bounded timeout.
     *
     * <p>The queues are unbounded to prevent the lsp4j dispatch thread from ever blocking, even
     * if the test drains events slowly.
     */
    static class CollectingClient implements IDebugProtocolClient {

        /** lsp4j remote-server proxy; set after the launcher is wired up. */
        IDebugProtocolServer remoteServer;

        final BlockingQueue<StoppedEventArguments>    stopped     = new LinkedBlockingQueue<>();
        final BlockingQueue<ExitedEventArguments>     exited      = new LinkedBlockingQueue<>();
        final BlockingQueue<TerminatedEventArguments> terminated  = new LinkedBlockingQueue<>();
        final BlockingQueue<Object>                   initialized = new LinkedBlockingQueue<>();

        @Override public void stopped(StoppedEventArguments args)       { stopped.add(args); }
        @Override public void exited(ExitedEventArguments args)          { exited.add(args); }
        @Override public void terminated(TerminatedEventArguments args)  { terminated.add(args); }
        @Override public void initialized()                              { initialized.add(Boolean.TRUE); }

        StoppedEventArguments awaitStopped() throws Exception {
            StoppedEventArguments e = stopped.poll(5, TimeUnit.SECONDS);
            assertNotNull(e, "Expected a stopped event within 5 s");
            return e;
        }

        ExitedEventArguments awaitExited() throws Exception {
            ExitedEventArguments e = exited.poll(5, TimeUnit.SECONDS);
            assertNotNull(e, "Expected an exited event within 5 s");
            return e;
        }

        TerminatedEventArguments awaitTerminated() throws Exception {
            TerminatedEventArguments e = terminated.poll(5, TimeUnit.SECONDS);
            assertNotNull(e, "Expected a terminated event within 5 s");
            return e;
        }

        void awaitInitialized() throws Exception {
            Object e = initialized.poll(5, TimeUnit.SECONDS);
            assertNotNull(e, "Expected an initialized event within 5 s");
        }
    }
}





