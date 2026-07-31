package core.game;

import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerProcess.class);
  private static final String LOCALHOST = "127.0.0.1";
  private static final int CONNECT_PROBE_TIMEOUT_MS = 500;
  private static final long POLL_INTERVAL_MS = 200;
  private static final long TERMINATION_TIMEOUT_SECONDS = 5;

  private final Process process;

  private ServerProcess(final Process process) {
    this.process = process;
  }

  /**
   * Starts the given main class in a direct child JVM.
   *
   * @param mainClass the server main class to launch
   * @param port the port the server is expected to listen on
   * @param args extra program arguments such as {@code --server}
   * @return a handle to the started server process
   * @throws IOException if the child process could not be started
   */
  public static ServerProcess start(final Class<?> mainClass, final int port, final String... args)
      throws IOException {
    Process process =
        new ProcessBuilder(buildCommand(mainClass, port, args))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();
    ServerProcess server = new ServerProcess(process);
    Runtime.getRuntime()
        .addShutdownHook(new Thread(server::stop, "server-process-shutdown-" + process.pid()));
    return server;
  }

  private static List<String> buildCommand(
      final Class<?> mainClass, final int port, final String... args) {
    String javaHome = java.lang.System.getProperty("java.home");
    boolean windows =
        java.lang.System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    List<String> command = new ArrayList<>();
    command.add(javaExecutable(javaHome, windows));
    command.add("-cp");
    command.add(java.lang.System.getProperty("java.class.path"));
    command.add("-D" + PORT_PROPERTY + "=" + port);
    command.add("-D" + MANAGED_PROPERTY + "=true");
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
    long deadline = java.lang.System.nanoTime() + timeout.toNanos();
    while (java.lang.System.nanoTime() < deadline) {
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
