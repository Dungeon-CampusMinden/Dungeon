package engine.tracking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/** Performs the non-blocking backend and database health check used by operator UI. */
final class TrackingBackendHealth {
  private static final Duration TIMEOUT = Duration.ofSeconds(2);
  private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

  private TrackingBackendHealth() {}

  static CompletableFuture<Boolean> check(URI endpoint) {
    String base = endpoint.toString().replaceAll("/+$", "");
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(base + "/health")).timeout(TIMEOUT).GET().build();
    return CLIENT
        .sendAsync(request, HttpResponse.BodyHandlers.discarding())
        .thenApply(response -> response.statusCode() == 200)
        .exceptionally(ignored -> false);
  }
}
