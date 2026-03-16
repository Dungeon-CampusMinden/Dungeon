package server;

import coderunner.BlocklyCodeRunner;
import com.sun.net.httpserver.*;
import core.utils.logging.DungeonLogger;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

/** This class is hosting the html files for the blockly dungeon. */
public class FrontendServer {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BlocklyCodeRunner.class);
  private static HttpServer server;

  /**
   * Starts the frontend server.
   *
   * @throws IOException if textures can not be loaded.
   */
  public static void run() throws IOException {
    server = HttpServer.create(new InetSocketAddress(8081), 0);
    LOGGER.debug(new File(".").getAbsolutePath());

    server.createContext(
        "/",
        exchange -> {
          String uriPath = exchange.getRequestURI().getPath();

          // Default auf index.html
          if (uriPath.equals("/")) {
            uriPath = "/index.html";
          }

          String resourcePath = "assets" + uriPath;

          InputStream is = loadAsset(resourcePath);

          // Fallback
          if (is == null) {
            resourcePath = "assets/index.html";
            is = loadAsset(resourcePath);
          }

          // SPA fallback
          if (is == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
          }

          byte[] bytes = is.readAllBytes();
          is.close();

          exchange.getResponseHeaders().add("Content-Type", guessMime(resourcePath));
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });

    server.setExecutor(null);
    server.start();
  }

  private static String guessMime(String name) {
    if (name.endsWith(".html")) return "text/html";
    if (name.endsWith(".js")) return "application/javascript";
    if (name.endsWith(".css")) return "text/css";
    if (name.endsWith(".png")) return "image/png";
    if (name.endsWith(".svg")) return "image/svg+xml";
    if (name.endsWith(".json")) return "application/json";
    return "application/octet-stream";
  }

  private static InputStream loadAsset(String path) throws IOException {
    // load assets from jar asset path
    InputStream is = FrontendServer.class.getClassLoader().getResourceAsStream(path);

    if (is != null) return is;

    // fallback: loading assets from dev system
    File file = new File(path.replaceFirst("^assets", "blockly/frontend/dist"));

    if (file.exists()) {
      return new FileInputStream(file);
    }

    return null;
  }

  /** Stops the frontend server. */
  public static void stopServer() {
    if (server != null) {

      server.stop(0);
      System.out.println("Server gestoppt.");
    }
  }
}
