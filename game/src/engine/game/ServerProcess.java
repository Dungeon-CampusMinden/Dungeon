package engine.game;

import engine.utils.logging.DungeonLogger;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Launches and supervises a dedicated server in a separate JVM.
 *
 * <p>The main menu waits until the child exposes its network port before connecting to it.
 */
public final class ServerProcess {

  /** Default argument used to put an explicit project's entry point into server mode. */
  public static final String SERVER_ARGUMENT = "--server";

  /** System property used to communicate the server port to the child process. */
  public static final String PORT_PROPERTY = "dungeon.server.port";

  /** System property identifying a server process managed by a hosting client. */
  public static final String MANAGED_PROPERTY = "dungeon.server.managed";

  /** Internal loopback port used by a managed server to report terminal status to its host. */
  public static final String STATUS_PORT_PROPERTY = "dungeon.server.statusPort";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerProcess.class);
  private static final String LOCALHOST = "127.0.0.1";
  private static final int CONNECT_PROBE_TIMEOUT_MS = 500;
  private static final long POLL_INTERVAL_MS = 200;
  private static final long TERMINATION_TIMEOUT_SECONDS = 15;
  private static final long STATUS_DRAIN_TIMEOUT_MILLIS = 500;
  private final Process process;
  private final ServerSocket statusServer;
  private final Consumer<String> statusConsumer;
  private final CountDownLatch statusListenerComplete = new CountDownLatch(1);

  private ServerProcess(
      final Process process,
      final ServerSocket statusServer,
      final Consumer<String> statusConsumer) {
    this.process = process;
    this.statusServer = statusServer;
    this.statusConsumer = statusConsumer;
    startStatusListener();
    process.onExit().thenRun(this::drainAndCloseStatusServer);
  }

  /**
   * Starts the given main class in a direct child JVM.
   *
   * @param mainClass the server main class to launch
   * @param port the port the server is expected to listen on
   * @param environmentOverrides environment values supplied to the child process
   * @param statusConsumer callback for one opaque terminal status sent by the managed child
   * @param args extra program arguments such as {@code --server}
   * @return a handle to the started server process
   * @throws IOException if the child process could not be started
   */
  public static ServerProcess start(
      final Class<?> mainClass,
      final int port,
      final Map<String, String> environmentOverrides,
      final Consumer<String> statusConsumer,
      final String... args)
      throws IOException {
    Map<String, String> childEnvironment =
        Map.copyOf(Objects.requireNonNull(environmentOverrides, "environmentOverrides"));
    Consumer<String> childStatusConsumer = Objects.requireNonNull(statusConsumer, "statusConsumer");
    ServerSocket statusServer = new ServerSocket();
    statusServer.bind(new InetSocketAddress(LOCALHOST, 0));
    Process process;
    try {
      ProcessBuilder processBuilder =
          new ProcessBuilder(buildCommand(mainClass, port, statusServer.getLocalPort(), args))
              .redirectOutput(ProcessBuilder.Redirect.INHERIT)
              .redirectError(ProcessBuilder.Redirect.INHERIT);
      processBuilder.environment().putAll(childEnvironment);
      process = processBuilder.start();
    } catch (IOException exception) {
      statusServer.close();
      throw exception;
    }
    ServerProcess server = new ServerProcess(process, statusServer, childStatusConsumer);
    Runtime.getRuntime()
        .addShutdownHook(new Thread(server::stop, "server-process-shutdown-" + process.pid()));
    return server;
  }

  private static List<String> buildCommand(
      final Class<?> mainClass, final int port, final int statusPort, final String... args) {
    String javaHome = System.getProperty("java.home");
    boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    List<String> command = new ArrayList<>();
    command.add(javaExecutable(javaHome, windows));
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add("-D" + PORT_PROPERTY + "=" + port);
    command.add("-D" + MANAGED_PROPERTY + "=true");
    command.add("-D" + STATUS_PORT_PROPERTY + "=" + statusPort);
    command.add(mainClass.getName());
    if (args != null) {
      Collections.addAll(command, args);
    }
    return List.copyOf(command);
  }

  private static String javaExecutable(final String javaHome, final boolean windows) {
    if (!windows) {
      return Path.of(javaHome, "bin", "java").toString();
    }
    String executableName =
        ProcessHandle.current()
            .info()
            .command()
            .map(Path::of)
            .map(Path::getFileName)
            .map(Path::toString)
            .filter(name -> name.equalsIgnoreCase("javaw") || name.equalsIgnoreCase("javaw.exe"))
            .map(ignored -> "javaw.exe")
            .orElse("java.exe");
    return Path.of(javaHome, "bin", executableName).toString();
  }

  /**
   * Blocks until the server's port is reachable on localhost or the timeout elapses.
   *
   * @param port the port to probe
   * @param timeout the maximum time to wait
   * @return {@code true} if the server became reachable and the process is still alive
   */
  public boolean awaitReady(final int port, final Duration timeout) {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      if (!process.isAlive()) {
        LOGGER.warn("Server process exited before becoming reachable.");
        return false;
      }
      if (isReachable(port)) {
        return true;
      }
      try {
        Thread.sleep(POLL_INTERVAL_MS);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
    return false;
  }

  private static boolean isReachable(final int port) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(LOCALHOST, port), CONNECT_PROBE_TIMEOUT_MS);
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  /**
   * Returns whether the direct server child is still running.
   *
   * @return {@code true} while the server child is alive
   */
  public boolean isAlive() {
    return process.isAlive();
  }

  /** Requests a normal managed stop and waits briefly for the child to exit. */
  public synchronized void stop() {
    try {
      if (process.isAlive()) {
        LOGGER.info("Stopping server process (pid={}).", process.pid());
        requestGracefulStop();
        if (!process.waitFor(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          process.destroy();
          process.waitFor(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      process.destroy();
    } finally {
      drainAndCloseStatusServer();
    }
  }

  private void startStatusListener() {
    Thread.ofPlatform()
        .daemon()
        .name("server-process-status-" + process.pid())
        .start(
            () -> {
              try (Socket socket = statusServer.accept();
                  DataInputStream input = new DataInputStream(socket.getInputStream())) {
                statusConsumer.accept(input.readUTF());
              } catch (IOException | RuntimeException exception) {
                if (!statusServer.isClosed()) {
                  LOGGER.warn("Cannot receive managed-server status.", exception);
                }
              } finally {
                statusListenerComplete.countDown();
                closeStatusServer();
              }
            });
  }

  private void drainAndCloseStatusServer() {
    try {
      statusListenerComplete.await(STATUS_DRAIN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    } finally {
      closeStatusServer();
    }
  }

  private void closeStatusServer() {
    try {
      statusServer.close();
    } catch (IOException exception) {
      LOGGER.warn("Cannot close managed-server status channel.", exception);
    }
  }

  private void requestGracefulStop() {
    try (OutputStream childInput = process.getOutputStream()) {
      childInput.write(0);
      childInput.flush();
    } catch (IOException exception) {
      LOGGER.warn("Cannot request graceful hosted-server stop.");
    }
  }
}
