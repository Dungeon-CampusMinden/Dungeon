package tracking.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import tracking.core.TrackingAck;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlReadResult;
import tracking.core.TrackingOutboxUpload;

/** Imports one self-contained local JSONL outbox through the tracking HTTP API. */
public final class TrackingImportCli {
  private static final String API_KEY_ENVIRONMENT_VARIABLE = "DUNGEON_TRACKING_API_KEY";

  private TrackingImportCli() {}

  /**
   * Imports the outbox named by the command-line options.
   *
   * @param arguments importer command-line options
   */
  public static void main(final String[] arguments) throws Exception {
    Arguments options = Arguments.parse(arguments);
    TrackingJsonlReadResult outbox =
        TrackingJson.readJsonlRecoveringTruncatedTail(options.outboxFile());
    if (outbox.truncatedTailIgnored()) {
      System.err.println(
          "Warning: ignored an incomplete final record in "
              + options.outboxFile().toAbsolutePath()
              + "; complete preceding records will be imported.");
    }

    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    TrackingAck lastAck =
        TrackingOutboxUpload.upload(
            client, options.baseUri(), options.apiKey(), outbox, options.batchSize());
    System.out.println(
        "Imported "
            + outbox.events().size()
            + " events; backend acknowledgement is sequence "
            + lastAck.lastPersistedSequence());
  }

  private record Arguments(URI baseUri, Path outboxFile, Optional<String> apiKey, int batchSize) {
    static Arguments parse(final String[] arguments) {
      URI baseUri = null;
      Path outboxFile = null;
      int batchSize = 500;
      for (int index = 0; index < arguments.length; index += 2) {
        if (index + 1 >= arguments.length) {
          throw usage();
        }
        String value = arguments[index + 1];
        switch (arguments[index]) {
          case "--url" -> baseUri = URI.create(value);
          case "--outbox" -> outboxFile = Path.of(value);
          case "--batch-size" -> batchSize = Integer.parseInt(value);
          default -> throw usage();
        }
      }
      if (baseUri == null || outboxFile == null || batchSize < 1) {
        throw usage();
      }
      return new Arguments(baseUri, outboxFile, environmentApiKey(), batchSize);
    }

    private static Optional<String> environmentApiKey() {
      String value = System.getenv(API_KEY_ENVIRONMENT_VARIABLE);
      if (value == null) {
        return Optional.empty();
      }
      value = value.strip();
      if (value.isEmpty()) {
        throw new IllegalArgumentException(API_KEY_ENVIRONMENT_VARIABLE + " is empty");
      }
      return Optional.of(value);
    }

    private static IllegalArgumentException usage() {
      return new IllegalArgumentException(
          "Usage: --url URL --outbox SESSION.jsonl [--batch-size N]");
    }
  }
}
