package server;
import coderunner.BlocklyCodeRunner;
import com.sun.net.httpserver.*;
import core.utils.logging.DungeonLogger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class FrontendServer {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BlocklyCodeRunner.class);



  public static void run() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
    LOGGER.debug(new File(".").getAbsolutePath());

    // Immer nur index.html ausliefern
    server.createContext("/", exchange -> {
      String uriPath = exchange.getRequestURI().getPath();
      System.out.println(exchange.getRequestMethod() + " " + uriPath);

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
    System.out.println("Server läuft auf http://localhost:8081/");
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
    // 1️⃣ Versuch: aus JAR
    InputStream is = FrontendServer.class
      .getClassLoader()
      .getResourceAsStream(path);

    if (is != null) return is;

    System.out.println("asset cannot be loaded with input stream path " + path);
    System.out.println("loading from frontend dist assets stream path " + path);
    // 2️⃣ Fallback: Dev-Filesystem
    File file = new File(
      path.replaceFirst("^assets", "blockly/frontend/dist")
    );
    System.out.println(file.toPath());

    if (file.exists()) {
      return new FileInputStream(file);
    }

    System.out.println("asset could not be found");

    return null;
  }
}
