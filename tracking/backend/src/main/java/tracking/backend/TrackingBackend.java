package tracking.backend;

import java.util.concurrent.CountDownLatch;

/** Starts the self-hosted Dungeon tracking reference backend. */
public final class TrackingBackend {
  private TrackingBackend() {}

  /**
   * Loads runtime configuration and serves until the process stops.
   *
   * @param arguments unused command-line arguments
   */
  public static void main(final String[] arguments) throws Exception {
    BackendConfig config = BackendConfig.load();
    Database database = new Database(config);
    TrackingRepository repository = new TrackingRepository(database);
    TrackingHttpServer server = new TrackingHttpServer(config, repository);
    CountDownLatch stopped = new CountDownLatch(1);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  server.close();
                  stopped.countDown();
                },
                "tracking-backend-shutdown"));
    server.start();
    System.out.println(
        "Dungeon tracking backend listening on " + config.bindHost() + ":" + config.port());
    stopped.await();
  }
}
