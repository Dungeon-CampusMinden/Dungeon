package wizard.authoring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import tools.jackson.databind.JsonNode;
import wizard.authoring.CandidateProjectService.FinalizationRecoveredException;
import wizard.authoring.CandidateProjectService.MissingDraftException;
import wizard.authoring.DraftStore.FinalizationIdentityConflictException;
import wizard.authoring.DraftStore.ProjectOwnershipConflictException;
import wizard.authoring.DraftStore.ProjectOwnershipUnavailableException;
import wizard.authoring.DraftStore.RevisionConflictException;
import wizard.runner.contract.ContractCapabilities;

/** Local standalone browser host for private Wizard authoring. */
public final class WizardAuthoringApplication {
  private static final String API_PREFIX = "/api/v1";
  private static final String UI_PREFIX = "wizard/authoring-ui/";
  private static final String TEMPLATE_RESOURCE = "wizard/template/WizardRoomTemplate.jar";
  private static final int MAX_REQUEST_BYTES = ContractCapabilities.MAX_DEER_BYTES * 4;
  private static final Set<String> BUNDLED_ASSETS = bundledAssets();

  private WizardAuthoringApplication() {}

  /**
   * Starts the loopback-only host and opens the system browser.
   *
   * @param arguments must be empty
   */
  public static void main(final String[] arguments) throws IOException {
    if (arguments.length != 0) {
      throw new IllegalArgumentException("DungeonWizard.jar does not accept arguments");
    }
    Path dataRoot = DraftStore.defaultDataRoot();
    DataRootLock dataRootLock = DataRootLock.acquire(dataRoot);
    DraftStore drafts = new DraftStore(dataRoot);
    CandidateProjectService projects = new CandidateProjectService(drafts);
    projects.reconcileAll();
    Path template = materializeTemplate();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    String origin = "http://127.0.0.1:" + server.getAddress().getPort();
    server.createContext("/", exchange -> handle(exchange, drafts, projects, template, origin));
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  server.stop(0);
                  try {
                    Files.deleteIfExists(template);
                  } catch (IOException ignored) {
                    // The operating system can reclaim an abandoned temporary template.
                  }
                  dataRootLock.close();
                },
                "wizard-authoring-shutdown"));
    server.start();
    System.out.println("Dungeon Wizard is available at " + origin + "/");
    openBrowser(URI.create(origin + "/"));
    try {
      new CountDownLatch(1).await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    } finally {
      server.stop(0);
      Files.deleteIfExists(template);
      dataRootLock.close();
    }
  }

  private static void handle(
      final HttpExchange exchange,
      final DraftStore drafts,
      final CandidateProjectService projects,
      final Path template,
      final String origin) {
    try {
      if (exchange.getRequestURI().getPath().startsWith(API_PREFIX)) {
        handleApi(exchange, drafts, projects, template, origin);
      } else {
        serveStatic(exchange);
      }
    } catch (RevisionConflictException exception) {
      sendJson(
          exchange,
          409,
          Map.of(
              "error",
              exception.getMessage(),
              "code",
              "REVISION_CONFLICT",
              "expectedRevision",
              exception.expectedRevision(),
              "actualRevision",
              exception.actualRevision()));
    } catch (FinalizationRecoveredException exception) {
      sendJson(
          exchange,
          409,
          Map.of(
              "error",
              exception.getMessage(),
              "code",
              "FINALIZATION_RECOVERED",
              "revision",
              exception.revision()));
    } catch (FinalizationIdentityConflictException exception) {
      sendConflict(exchange, "FINALIZATION_IDENTITY_MISMATCH", exception.getMessage());
    } catch (ProjectOwnershipConflictException exception) {
      sendConflict(exchange, "PROJECT_DIRECTORY_OWNED", exception.getMessage());
    } catch (ProjectOwnershipUnavailableException exception) {
      sendConflict(exchange, "PROJECT_OWNERSHIP_UNAVAILABLE", exception.getMessage());
    } catch (MissingDraftException exception) {
      sendError(exchange, 404, exception.getMessage());
    } catch (IllegalArgumentException exception) {
      sendError(exchange, 400, concise(exception));
    } catch (Exception exception) {
      sendError(exchange, 500, "The local Wizard operation failed");
    } finally {
      exchange.close();
    }
  }

  private static void handleApi(
      final HttpExchange exchange,
      final DraftStore drafts,
      final CandidateProjectService projects,
      final Path template,
      final String origin)
      throws IOException {
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    if (method.equals("GET") && path.equals(API_PREFIX + "/status")) {
      sendJson(exchange, 200, Map.of("apiVersion", "1", "mode", "native"));
      return;
    }
    if (method.equals("GET") && path.equals(API_PREFIX + "/drafts")) {
      sendJson(exchange, 200, drafts.list());
      return;
    }
    if (method.equals("POST") && path.equals(API_PREFIX + "/choose-project-directory")) {
      requireMutation(exchange, origin, null);
      requireEmptyBody(exchange);
      Optional<Path> chosen = chooseDirectory();
      if (chosen.isEmpty()) {
        exchange.sendResponseHeaders(204, -1);
      } else {
        sendJson(exchange, 200, Map.of("projectDirectory", chosen.orElseThrow().toString()));
      }
      return;
    }

    List<String> segments = segments(path.substring(API_PREFIX.length()));
    if (segments.size() < 2 || !segments.get(0).equals("drafts")) {
      sendError(exchange, 404, "API route does not exist");
      return;
    }
    String draftId = segments.get(1);
    DraftStore.requireDraftId(draftId);

    if (segments.size() == 2 && method.equals("GET")) {
      JsonNode draft =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      sendJson(exchange, 200, draft);
      return;
    }
    if (segments.size() == 2 && method.equals("PUT")) {
      requireMutation(exchange, origin, "application/json");
      sendJson(exchange, 200, drafts.save(draftId, readBody(exchange, MAX_REQUEST_BYTES)));
      return;
    }
    if (segments.size() == 2 && method.equals("DELETE")) {
      requireMutation(exchange, origin, "application/json");
      drafts.delete(draftId, AuthoringJson.parse(readBody(exchange, MAX_REQUEST_BYTES)));
      exchange.sendResponseHeaders(204, -1);
      return;
    }
    if (segments.size() == 3
        && segments.get(2).equals("finalization-status")
        && method.equals("GET")) {
      sendJson(exchange, 200, projects.finalizationStatus(draftId));
      return;
    }
    if (segments.size() >= 3 && segments.get(2).equals("uploads")) {
      handleUploads(exchange, drafts, draftId, segments, origin);
      return;
    }
    if (segments.size() == 3 && method.equals("POST")) {
      requireMutation(exchange, origin, "application/json");
      JsonNode request = AuthoringJson.parse(readBody(exchange, MAX_REQUEST_BYTES));
      switch (segments.get(2)) {
        case "validate" -> sendJson(exchange, 200, projects.validate(draftId, request).json());
        case "finalize" ->
            sendJson(exchange, 200, projects.finalizeProject(draftId, request).json());
        case "package" ->
            sendJson(exchange, 200, projects.packageProject(draftId, request, template));
        default -> sendError(exchange, 404, "API route does not exist");
      }
      return;
    }
    sendError(exchange, 405, "HTTP method is not allowed for this API route");
  }

  private static void handleUploads(
      final HttpExchange exchange,
      final DraftStore drafts,
      final String draftId,
      final List<String> segments,
      final String origin)
      throws IOException {
    if (drafts.loadOptional(draftId).isEmpty()) {
      throw new MissingDraftException(draftId);
    }
    String method = exchange.getRequestMethod();
    if (segments.size() == 3 && method.equals("POST")) {
      String mediaType = exchange.getRequestHeaders().getFirst("Content-Type");
      if (!"image/png".equals(mediaType) && !"image/jpeg".equals(mediaType)) {
        throw new IllegalArgumentException("Upload Content-Type must be image/png or image/jpeg");
      }
      requireMutation(exchange, origin, mediaType);
      String name = queryName(exchange.getRequestURI().getRawQuery());
      DraftStore.Upload upload =
          drafts.putUpload(
              draftId, name, mediaType, readBody(exchange, ContractCapabilities.MAX_ASSET_BYTES));
      sendJson(
          exchange,
          200,
          Map.of("storageKey", upload.storageKey(), "originalName", upload.originalName()));
      return;
    }
    if (segments.size() == 3 && method.equals("GET")) {
      sendJson(exchange, 200, Map.of("storageKeys", drafts.uploadKeys(draftId)));
      return;
    }
    if (segments.size() == 4 && method.equals("GET")) {
      Optional<DraftStore.Upload> storedUpload = drafts.upload(draftId, segments.get(3));
      if (storedUpload.isEmpty()) {
        sendError(exchange, 404, "Upload does not exist");
        return;
      }
      DraftStore.Upload upload = storedUpload.orElseThrow();
      send(exchange, 200, upload.mediaType(), upload.bytes(), false);
      return;
    }
    sendError(exchange, 405, "HTTP method is not allowed for this upload route");
  }

  private static void requireMutation(
      final HttpExchange exchange, final String origin, final String contentType) {
    if (!origin.equals(exchange.getRequestHeaders().getFirst("Origin"))) {
      throw new IllegalArgumentException("Mutation request origin is not the local Wizard host");
    }
    if (contentType != null
        && !contentType.equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
      throw new IllegalArgumentException("Request Content-Type is invalid");
    }
  }

  private static void requireEmptyBody(final HttpExchange exchange) throws IOException {
    if (readBody(exchange, 1).length != 0) {
      throw new IllegalArgumentException("Request body must be empty");
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

  private static Path materializeTemplate() throws IOException {
    byte[] bytes = resource(TEMPLATE_RESOURCE);
    if (bytes == null) {
      throw new IllegalStateException("Packaged Wizard room template is missing");
    }
    Path temporary = Files.createTempFile("WizardRoomTemplate-", ".jar");
    Files.write(temporary, bytes);
    return temporary;
  }

  private static Optional<Path> chooseDirectory() {
    final Path[] result = new Path[1];
    final RuntimeException[] failure = new RuntimeException[1];
    try {
      SwingUtilities.invokeAndWait(
          () -> {
            try {
              UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
              JFileChooser chooser = new JFileChooser();
              chooser.setDialogTitle("Wizard-Projektordner wählen");
              chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
              chooser.setAcceptAllFileFilterUsed(false);
              if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                result[0] = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
              }
            } catch (Exception exception) {
              failure[0] = new IllegalStateException("Project directory chooser failed", exception);
            }
          });
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Project directory chooser was interrupted", exception);
    } catch (java.lang.reflect.InvocationTargetException exception) {
      throw new IllegalStateException("Project directory chooser failed", exception);
    }
    if (failure[0] != null) {
      throw failure[0];
    }
    return Optional.ofNullable(result[0]);
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

  private static List<String> segments(final String path) {
    return List.of(path.split("/", -1)).stream().filter(segment -> !segment.isEmpty()).toList();
  }

  private static String queryName(final String rawQuery) {
    if (rawQuery == null || !rawQuery.startsWith("name=") || rawQuery.indexOf('&') >= 0) {
      throw new IllegalArgumentException("Upload requires exactly one name query parameter");
    }
    String name = URLDecoder.decode(rawQuery.substring(5), StandardCharsets.UTF_8);
    if (name.isBlank()
        || name.length() > 255
        || name.codePoints().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException("Upload original name is invalid");
    }
    return name;
  }

  private static byte[] readBody(final HttpExchange exchange, final int limit) throws IOException {
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

  private static void sendRawJson(
      final HttpExchange exchange, final int status, final String value) {
    send(
        exchange,
        status,
        "application/json; charset=utf-8",
        value.getBytes(StandardCharsets.UTF_8),
        false);
  }

  private static void sendError(
      final HttpExchange exchange, final int status, final String message) {
    sendJson(exchange, status, Map.of("error", message));
  }

  private static void sendConflict(
      final HttpExchange exchange, final String code, final String message) {
    sendJson(exchange, 409, Map.of("error", message, "code", code));
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
