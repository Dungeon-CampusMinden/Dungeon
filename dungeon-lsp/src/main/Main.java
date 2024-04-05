package main;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

public class Main {
  private static int socketPort = -1;

  public static Logger LOGGER;

  public static void main(String[] args) {
    // init logger
    LOGGER = Logger.getLogger("log");
    FileHandler fh;
    try {
      // This block configure the logger with handler and formatter
      Path path = Path.of("/home/malt_r/dev/lsp-server/log/my_log.log").toAbsolutePath();
      System.out.println("Path:" + path);
      Path cwd = Path.of(".").toAbsolutePath();
      System.out.println("cwd: " + cwd);

      fh = new FileHandler(path.toString());
      LOGGER.addHandler(fh);
      LOGGER.setLevel(Level.FINER);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);

      LOGGER.info("CWD: '" + cwd + "'");
    } catch (SecurityException | IOException e) {
      e.printStackTrace();
    }

    // TODO: extend to support pipe connection, reject stdio connection
    Pattern socketPattern = Pattern.compile("--socket=(\\d{1,5})");
    for (String arg : args) {
      LOGGER.info("Arg: '" + arg + "'");
      var match = socketPattern.matcher(arg);
      if (match.matches()) {
        socketPort = Integer.parseInt(match.group(1));
        LOGGER.info("Parsed port number '" + socketPort + "'");
      }
    }

    if (socketPort == -1) {
      throw new RuntimeException("No port number specified!");
    }

    try {
      var server = new LspServer(socketPort);

      LOGGER.info("Waiting for server to finish...");
      server.getServerListening().get();
    } catch (IOException | InterruptedException | ExecutionException exc) {
      LOGGER.log(Level.SEVERE, "Exception", exc);
    }

    LOGGER.info("EXITING SERVER");
  }
}
