package audience.relay;

/** Starts the relay between a game in audience mode and the devices of its audience. */
public final class AudienceRelay {
  private AudienceRelay() {}

  /**
   * Loads the configuration and serves until the process stops; the server's dispatcher thread
   * keeps the process alive.
   *
   * @param arguments unused command-line arguments
   */
  public static void main(String[] arguments) throws Exception {
    RelayConfig config = RelayConfig.load();
    RelayHttpServer server = new RelayHttpServer(config);
    Runtime.getRuntime().addShutdownHook(new Thread(server::close, "audience-relay-shutdown"));
    server.start();
    System.out.println("Audience relay listening on " + config.bindHost() + ":" + config.port());
    if (config.createKey().isEmpty()) {
      System.out.println("No DUNGEON_RELAY_CREATE_KEY set: any game can create sessions");
    }
  }
}
