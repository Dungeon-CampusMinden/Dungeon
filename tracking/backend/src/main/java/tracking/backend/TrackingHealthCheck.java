package tracking.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Container health check that needs no shell network utility. */
public final class TrackingHealthCheck {
  private TrackingHealthCheck() {}

  /**
   * Exits unsuccessfully unless the local backend and PostgreSQL are healthy.
   *
   * @param arguments unused command-line arguments
   * @throws Exception when the request cannot be completed
   */
  public static void main(final String[] arguments) throws Exception {
    String port = System.getenv().getOrDefault("DUNGEON_TRACKING_PORT", "8088");
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health"))
            .timeout(Duration.ofSeconds(3))
            .GET()
            .build();
    HttpResponse<Void> response =
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
    if (response.statusCode() != 200) {
      throw new IllegalStateException("Tracking backend is unhealthy");
    }
  }
}
