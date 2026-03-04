import static org.junit.jupiter.api.Assertions.*;

import dgir.core.Dialect;
import dgir.core.serialization.Utils;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end integration tests for the external {@code dgir-vm-dap.jar} process.
 *
 * <p>These tests use real compiled fixture programs from {@code test_assets/DapTestFiles} and
 * derive test expectations from the JSON itself (source path, lines, variables), instead of
 * hand-constructing synthetic programs.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DapJarIntegrationTest {
  static class DapSession implements AutoCloseable {
    final Process serverProcess;
    final Socket socket;
    final CollectingClient client;
    final IDebugProtocolServer remote;

    DapSession(
        Process serverProcess,
        Socket socket,
        CollectingClient client,
        IDebugProtocolServer remote) {
      this.serverProcess = serverProcess;
      this.socket = socket;
      this.client = client;
      this.remote = remote;
    }

    @Override
    public void close() throws Exception {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
      if (serverProcess != null && serverProcess.isAlive()) {
        serverProcess.destroyForcibly();
      }
    }
  }

  static class CollectingClient implements IDebugProtocolClient {

    IDebugProtocolServer remoteServer;

    final BlockingQueue<StoppedEventArguments> stopped = new LinkedBlockingQueue<>();
    final BlockingQueue<ExitedEventArguments> exited = new LinkedBlockingQueue<>();
    final BlockingQueue<TerminatedEventArguments> terminated = new LinkedBlockingQueue<>();
    final BlockingQueue<Object> initialized = new LinkedBlockingQueue<>();

    @Override
    public void stopped(StoppedEventArguments args) {
      stopped.add(args);
    }

    @Override
    public void exited(ExitedEventArguments args) {
      exited.add(args);
    }

    @Override
    public void terminated(TerminatedEventArguments args) {
      terminated.add(args);
    }

    @Override
    public void initialized() {
      initialized.add(Boolean.TRUE);
    }

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

  static final ObjectMapper MAPPER = Utils.getMapper(false);
  static Path dapJar;
  static Path testProgramsDir;
  static List<FixtureProgram> fixtures;

  record FixtureProgram(
      Path jsonPath,
      Path javaPath,
      String sourceName,
      List<Integer> breakpointLines,
      Set<String> debugNames) {}

  @BeforeAll
  static void setup() throws Exception {
    Dialect.registerAllDialects();

    dapJar = TestUtils.findDapJar().orElse(Path.of("blasdl;akmwd"));
    if (!Files.exists(dapJar)) {
      Path workspaceRoot = TestUtils.findWorkspaceRoot();
      ProcessBuilder buildPb =
          new ProcessBuilder(workspaceRoot.resolve("gradlew").toString(), ":dgir:vm:dapJar");
      buildPb.directory(workspaceRoot.toFile());
      buildPb.inheritIO();
      int exit = buildPb.start().waitFor();
      assertEquals(0, exit, "Gradle dapJar build failed with exit code " + exit);
    }

    Assumptions.assumeTrue(
        Files.exists(dapJar), "Skipping DapJarIntegrationTest: fat jar not found at " + dapJar);

    testProgramsDir = TestUtils.findTestProgramsDir().orElse(null);
    Assumptions.assumeTrue(
        testProgramsDir != null, "Skipping DapJarIntegrationTest: test_assets folder not found");
  }

  static int findFreePort() throws IOException {
    try (java.net.ServerSocket ss = new java.net.ServerSocket(0)) {
      ss.setReuseAddress(true);
      return ss.getLocalPort();
    }
  }

  static void waitForPort(int port, long timeoutMs) throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      try (Socket probe = new Socket(InetAddress.getLoopbackAddress(), port)) {
        return;
      } catch (ConnectException ignored) {
        java.lang.Thread.sleep(100);
      }
    }
    fail("DAP server did not become ready on port " + port + " within " + timeoutMs + " ms");
  }

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
    throw new IOException("Could not connect to DAP server on port " + port, last);
  }

  static DapSession openSession(Path programJson) throws Exception {
    int port = findFreePort();
    ProcessBuilder pb =
        new ProcessBuilder(
            "java",
            "-jar",
            dapJar.toAbsolutePath().toString(),
            "--program",
            programJson.toAbsolutePath().toString(),
            "--dap-port",
            String.valueOf(port));
    pb.redirectErrorStream(true);
    pb.inheritIO();
    Process process = pb.start();

    waitForPort(port, 10_000);

    Socket socket = connectWithRetry(port, 5_000);
    CollectingClient client = new CollectingClient();
    Launcher<IDebugProtocolServer> launcher =
        DSPLauncher.createClientLauncher(client, socket.getInputStream(), socket.getOutputStream());
    IDebugProtocolServer remote = launcher.getRemoteProxy();
    client.remoteServer = remote;
    launcher.startListening();

    return new DapSession(process, socket, client, remote);
  }

  static Capabilities initializeOnly(IDebugProtocolServer remote, CollectingClient client)
      throws Exception {
    InitializeRequestArguments initArgs = new InitializeRequestArguments();
    initArgs.setClientID("jar-integration-test");
    Capabilities caps = remote.initialize(initArgs).get(5, TimeUnit.SECONDS);
    assertNotNull(caps, "initialize must return Capabilities");
    assertTrue(caps.getSupportsConfigurationDoneRequest(), "configurationDone must be supported");
    client.awaitInitialized();
    return caps;
  }

  static void handshake(IDebugProtocolServer remote, CollectingClient client, boolean stopOnEntry)
      throws Exception {
    initializeOnly(remote, client);
    Map<String, Object> launchArgs = stopOnEntry ? Map.of("stopOnEntry", true) : Map.of();
    remote.launch(launchArgs).get(5, TimeUnit.SECONDS);
    remote.configurationDone(new ConfigurationDoneArguments()).get(5, TimeUnit.SECONDS);
  }

  static SetBreakpointsResponse setSingleBreakpoint(
      IDebugProtocolServer remote, String sourcePath, int line, Integer column) throws Exception {
    Source src = new Source();
    src.setPath(sourcePath);

    SourceBreakpoint sb = new SourceBreakpoint();
    sb.setLine(line);
    if (column != null && column > 0) {
      sb.setColumn(column);
    }

    SetBreakpointsArguments args = new SetBreakpointsArguments();
    args.setSource(src);
    args.setBreakpoints(new SourceBreakpoint[] {sb});
    return remote.setBreakpoints(args).get(5, TimeUnit.SECONDS);
  }

  static Variable[] readLocals(IDebugProtocolServer remote) throws Exception {
    StackTraceArguments stArgs = new StackTraceArguments();
    stArgs.setThreadId(1);
    StackTraceResponse st = remote.stackTrace(stArgs).get(5, TimeUnit.SECONDS);
    assertTrue(st.getStackFrames().length > 0, "Expected stack frames while paused");

    ScopesArguments scopesArgs = new ScopesArguments();
    scopesArgs.setFrameId(st.getStackFrames()[0].getId());
    ScopesResponse scopes = remote.scopes(scopesArgs).get(5, TimeUnit.SECONDS);
    assertNotNull(scopes.getScopes());
    assertTrue(scopes.getScopes().length > 0, "Expected at least one scope");

    int localsRef = -1;
    for (Scope scope : scopes.getScopes()) {
      if ("Locals".equals(scope.getName())) {
        localsRef = scope.getVariablesReference();
        break;
      }
    }
    if (localsRef < 0) {
      localsRef = scopes.getScopes()[0].getVariablesReference();
    }

    VariablesArguments varArgs = new VariablesArguments();
    varArgs.setVariablesReference(localsRef);
    VariablesResponse vars = remote.variables(varArgs).get(5, TimeUnit.SECONDS);
    assertNotNull(vars.getVariables());
    return vars.getVariables();
  }
}
