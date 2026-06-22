package core.game;

import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Launches and supervises a dedicated server in a separate JVM.
 *
 * <p>Used by the {@link MainMenu} "Host Game" flow: a child process is started from the current
 * JVM's classpath, the menu waits until the server's network port becomes reachable, and only then
 * does the client transition into the game and connect to {@code 127.0.0.1}.
 *
 * <p>The spawned process is bound to the lifetime of the hosting client: a JVM shutdown hook
 * destroys it when the client exits, so closing the game also stops the hosted server.
 */
public final class ServerProcess {

  /** Default argument used to put an explicit project's entry point into server mode. */
  public static final String SERVER_ARGUMENT = "--server";

  /**
   * System property the host passes to the server child process to communicate the port to bind.
   *
   * <p>An explicit project's server entry point can read it via {@link Integer#getInteger(String,
   * Integer)} to honor a non-default port.
   */
  public static final String PORT_PROPERTY = "dungeon.server.port";

  /**
   * System property set on server child processes started by a hosting client (the main menu's
   * "Host Game" option). Standalone server launches do not set it, so a server starter can use it
   * to hide UI that is only useful for standalone servers (e.g. a status window).
   */
  public static final String MANAGED_PROPERTY = "dungeon.server.managed";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerProcess.class);
  private static final String LOCALHOST = "127.0.0.1";
  private static final int CONNECT_PROBE_TIMEOUT_MS = 500;
  private static final long POLL_INTERVAL_MS = 200;

  private final Process process;

  private ServerProcess(Process process) {
    this.process = process;
  }

  /**
   * Starts the given main class in a new JVM, reusing the current process' {@code java} executable
   * and classpath.
   *
   * <p>The configured {@code port} is forwarded both as the {@link #PORT_PROPERTY} system property
   * and is the port {@link #awaitReady(int, Duration)} probes for readiness.
   *
   * @param mainClass the server main class to launch
   * @param port the port the server is expected to listen on
   * @param args extra program arguments (e.g. {@code --server})
   * @return a handle to the started server process
   * @throws IOException if the child process could not be started
   */
  public static ServerProcess start(Class<?> mainClass, int port, String... args)
      throws IOException {
    List<String> command = buildCommand(mainClass, port, args);
    LOGGER.info("Starting server process: {}", String.join(" ", command));
    Process process = new ProcessBuilder(command).inheritIO().start();
    ServerProcess server = new ServerProcess(process);
    Runtime.getRuntime()
        .addShutdownHook(new Thread(server::stop, "server-process-shutdown-" + process.pid()));
    return server;
  }

  private static List<String> buildCommand(Class<?> mainClass, int port, String... args) {
    String javaHome = System.getProperty("java.home");
    boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    String javaBin = Path.of(javaHome, "bin", windows ? "java.exe" : "java").toString();

    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add("-D" + PORT_PROPERTY + "=" + port);
    command.add("-D" + MANAGED_PROPERTY + "=true");
    command.add(mainClass.getName());
    if (args != null) {
      Collections.addAll(command, args);
    }
    return command;
  }

  /**
   * Blocks until the server's port is reachable on localhost or the timeout elapses.
   *
   * @param port the port to probe
   * @param timeout the maximum time to wait
   * @return {@code true} if the server became reachable and the process is still alive
   */
  public boolean awaitReady(int port, Duration timeout) {
    long deadlineNanos = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadlineNanos) {
      if (!process.isAlive()) {
        LOGGER.warn("Server process exited before becoming reachable.");
        return false;
      }
      if (isReachable(port)) {
        return true;
      }
      try {
        Thread.sleep(POLL_INTERVAL_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }
    return false;
  }

  private boolean isReachable(int port) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(LOCALHOST, port), CONNECT_PROBE_TIMEOUT_MS);
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  /**
   * Returns whether the underlying process is still running.
   *
   * @return {@code true} if the process is alive
   */
  public boolean isAlive() {
    return process.isAlive();
  }

  /** Stops the server process if it is still running. */
  public void stop() {
    if (process.isAlive()) {
      LOGGER.info("Stopping server process (pid={}).", process.pid());
      process.destroy();
    }
  }
}
