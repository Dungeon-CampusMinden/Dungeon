package wizard.authoring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import engine.utils.logging.DungeonLogger;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import tools.jackson.databind.JsonNode;

/** Local standalone browser host for private Wizard authoring. */
public final class WizardAuthoringApplication {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(WizardAuthoringApplication.class);
  private static final String API_PREFIX = "/api/v1";
  private static final String UI_PREFIX = "wizard/authoring-ui/";
  private static final String TEMPLATE_RESOURCE = "wizard/template/WizardRoomTemplate.jar";
  private static final int PORT = 27_777;
  private static final int MAX_REQUEST_BYTES = 96 * 1024 * 1024;
  private static final String ORIGIN = "http://127.0.0.1:" + PORT;
  private static final Set<String> BUNDLED_ASSETS = bundledAssets();

  private WizardAuthoringApplication() {}

  /**
   * Starts the fixed loopback-only host and opens the system browser.
   *
   * @param arguments must be empty
   */
  public static void main(final String[] arguments) throws IOException {
    if (arguments.length != 0) {
      throw new IllegalArgumentException("DungeonWizard.jar does not accept arguments");
    }
    HttpServer server;
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
    } catch (BindException exception) {
      throw new IllegalStateException(
          "Dungeon Wizard cannot start because 127.0.0.1:" + PORT + " is already in use",
          exception);
    }
    CandidateProjectService projects = new CandidateProjectService();
    server.createContext("/", exchange -> handle(exchange, projects));
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    server.setExecutor(executor);
    AtomicBoolean shutdownComplete = new AtomicBoolean();
    Runnable shutdown = () -> shutdown(server, executor, shutdownComplete);
    Runtime.getRuntime().addShutdownHook(new Thread(shutdown, "wizard-authoring-shutdown"));
    server.start();
    System.out.println("Dungeon Wizard is available at " + ORIGIN + "/");
    openBrowser(URI.create(ORIGIN + "/"));
    boolean interrupted = false;
    try {
      new CountDownLatch(1).await();
    } catch (InterruptedException exception) {
      interrupted = true;
    } finally {
      shutdown.run();
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static void shutdown(
      final HttpServer server,
      final ExecutorService executor,
      final AtomicBoolean shutdownComplete) {
    synchronized (shutdownComplete) {
      if (shutdownComplete.get()) {
        return;
      }
      server.stop(0);
      executor.close();
      shutdownComplete.set(true);
    }
  }

  private static void handle(final HttpExchange exchange, final CandidateProjectService projects) {
    try {
      if (exchange.getRequestURI().getPath().startsWith(API_PREFIX)) {
        handleApi(exchange, projects);
      } else {
        serveStatic(exchange);
      }
    } catch (IllegalArgumentException exception) {
      sendError(exchange, 400, concise(exception));
    } catch (Exception exception) {
      LOGGER.error("Unexpected Wizard authoring request failure", exception);
      sendError(exchange, 500, "The local Wizard operation failed");
    } finally {
      exchange.close();
    }
  }

  private static void handleApi(final HttpExchange exchange, final CandidateProjectService projects)
      throws IOException {
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    if (method.equals("GET") && path.equals(API_PREFIX + "/status")) {
      sendJson(exchange, 200, Map.of("apiVersion", "1", "mode", "native"));
      return;
    }
    if (method.equals("POST") && path.equals(API_PREFIX + "/validate")) {
      requireMutation(exchange);
      sendJson(exchange, 200, projects.validate(readJsonBody(exchange)).json());
      return;
    }
    if (method.equals("POST") && path.equals(API_PREFIX + "/package")) {
      requireMutation(exchange);
      JsonNode request = readJsonBody(exchange);
      CandidateProjectService.PackageResponse result;
      try (InputStream template = templateResource()) {
        result = projects.packageProject(request, template);
      }
      byte[] jar = result.jarBytes();
      if (jar == null) {
        sendJson(exchange, 200, result.json());
      } else {
        sendJar(exchange, jar);
      }
      return;
    }
    sendError(exchange, 404, "API route does not exist");
  }

  private static void requireMutation(final HttpExchange exchange) {
    if (!ORIGIN.equals(exchange.getRequestHeaders().getFirst("Origin"))) {
      throw new IllegalArgumentException("Mutation request origin is not the local Wizard host");
    }
    if (!"application/json".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
      throw new IllegalArgumentException("Request Content-Type must be application/json");
    }
  }

  private static JsonNode readJsonBody(final HttpExchange exchange) throws IOException {
    return AuthoringJson.parse(readBody(exchange, MAX_REQUEST_BYTES));
  }

  private static byte[] readBody(final HttpExchange exchange, final int limit) throws IOException {
    String header = exchange.getRequestHeaders().getFirst("Content-Length");
    if (header != null) {
      long length;
      try {
        length = Long.parseLong(header);
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException("Content-Length is invalid", exception);
      }
      if (length < 0 || length > limit) {
        throw new IllegalArgumentException("Request body is too large");
      }
    }
    try (InputStream input = exchange.getRequestBody();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int total = 0;
      int read;
      while ((read = input.read(buffer)) >= 0) {
        total += read;
        if (total > limit) {
          throw new IllegalArgumentException("Request body is too large");
        }
        output.write(buffer, 0, read);
      }
      return output.toByteArray();
    }
  }

  private static void serveStatic(final HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    if (!method.equals("GET") && !method.equals("HEAD")) {
      sendError(exchange, 405, "Static resources support GET and HEAD only");
      return;
    }
    String path = exchange.getRequestURI().getPath();
    String resource;
    if (path.startsWith("/bundled-assets/")) {
      String relative = path.substring("/bundled-assets/".length());
      requireSafeResourcePath(relative);
      if (!BUNDLED_ASSETS.contains(relative)) {
        sendError(exchange, 404, "Bundled asset does not exist");
        return;
      }
      resource = relative;
    } else {
      String relative = path.equals("/") ? "index.html" : path.substring(1);
      requireSafeResourcePath(relative);
      resource = UI_PREFIX + relative;
    }
    byte[] bytes = resource(resource);
    if (bytes == null && uiRoute(path)) {
      bytes = resource(UI_PREFIX + "index.html");
      resource = UI_PREFIX + "index.html";
    }
    if (bytes == null) {
      sendError(exchange, 404, "Static resource does not exist");
      return;
    }
    send(exchange, 200, mediaType(resource), bytes, method.equals("HEAD"));
  }

  private static boolean uiRoute(final String path) {
    return !path.startsWith(API_PREFIX)
        && !path.startsWith("/bundled-assets/")
        && !path.substring(path.lastIndexOf('/') + 1).contains(".");
  }

  private static void requireSafeResourcePath(final String path) {
    if (path.isBlank()
        || path.startsWith("/")
        || path.contains("\\")
        || path.contains(":")
        || List.of(path.split("/", -1)).stream()
            .anyMatch(
                segment -> segment.isBlank() || segment.equals(".") || segment.equals(".."))) {
      throw new IllegalArgumentException("Static resource path is invalid");
    }
  }

  private static byte[] resource(final String name) throws IOException {
    ClassLoader loader = WizardAuthoringApplication.class.getClassLoader();
    try (InputStream input = loader.getResourceAsStream(name)) {
      return input == null ? null : input.readAllBytes();
    }
  }

  private static Set<String> bundledAssets() {
    Set<String> result = new HashSet<>();
    try {
      var resources =
          WizardAuthoringApplication.class.getClassLoader().getResources("internal_assets.txt");
      while (resources.hasMoreElements()) {
        try (InputStream input = resources.nextElement().openStream()) {
          result.addAll(
              new String(input.readAllBytes(), StandardCharsets.UTF_8)
                  .lines()
                  .filter(line -> !line.isBlank())
                  .toList());
        }
      }
    } catch (IOException exception) {
      throw new ExceptionInInitializerError(exception);
    }
    if (result.isEmpty()) {
      throw new ExceptionInInitializerError("Packaged internal asset index is missing");
    }
    return Set.copyOf(result);
  }

  private static InputStream templateResource() {
    InputStream input =
        WizardAuthoringApplication.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE);
    if (input == null) {
      throw new IllegalStateException("Packaged Wizard room template is missing");
    }
    return input;
  }

  private static void openBrowser(final URI uri) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(uri);
        return;
      }
    } catch (IOException | RuntimeException exception) {
      // Fall through to the visible manual address.
    }
    System.out.println("Open this address in a browser: " + uri);
  }

  private static String mediaType(final String resource) {
    String lower = resource.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".html")) return "text/html; charset=utf-8";
    if (lower.endsWith(".js")) return "text/javascript; charset=utf-8";
    if (lower.endsWith(".css")) return "text/css; charset=utf-8";
    if (lower.endsWith(".json")) return "application/json; charset=utf-8";
    if (lower.endsWith(".svg")) return "image/svg+xml";
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".woff2")) return "font/woff2";
    return "application/octet-stream";
  }

  private static void sendJson(final HttpExchange exchange, final int status, final Object value) {
    send(exchange, status, "application/json; charset=utf-8", AuthoringJson.encode(value), false);
  }

  private static void sendJar(final HttpExchange exchange, final byte[] jar) {
    exchange
        .getResponseHeaders()
        .set("Content-Disposition", "attachment; filename=\"WizardRoom.jar\"");
    send(exchange, 200, "application/java-archive", jar, false);
  }

  private static void sendError(
      final HttpExchange exchange, final int status, final String message) {
    sendJson(exchange, status, Map.of("error", message));
  }

  private static void send(
      final HttpExchange exchange,
      final int status,
      final String contentType,
      final byte[] bytes,
      final boolean head) {
    try {
      exchange.getResponseHeaders().set("Content-Type", contentType);
      exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
      exchange.getResponseHeaders().set("Cache-Control", "no-store");
      exchange.sendResponseHeaders(status, head ? -1 : bytes.length);
      if (!head) {
        exchange.getResponseBody().write(bytes);
      }
    } catch (IOException ignored) {
      // The browser may have disconnected before receiving the local response.
    }
  }

  private static String concise(final RuntimeException exception) {
    String message = exception.getMessage();
    return message == null || message.isBlank() ? "Request is invalid" : message;
  }
}
