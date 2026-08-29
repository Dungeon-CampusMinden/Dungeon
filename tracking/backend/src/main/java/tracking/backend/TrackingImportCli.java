package tracking.backend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import tracking.core.TrackingAck;
import tracking.core.TrackingBatch;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlReadResult;
import tracking.core.TrackingParticipant;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;

/** Imports a local JSONL outbox into the same HTTP API used by the game. */
public final class TrackingImportCli {
  private static final String API_KEY_ENVIRONMENT_VARIABLE = "DUNGEON_TRACKING_API_KEY";

  private TrackingImportCli() {}

  /**
   * Imports an outbox described by the command-line options documented in the tracking README.
   *
   * @param arguments importer options
   */
  public static void main(final String[] arguments) throws Exception {
    Arguments options = Arguments.parse(arguments);
    TrackingSessionDescriptor session =
        TrackingJson.read(
            Files.readString(options.sessionFile(), StandardCharsets.UTF_8),
            TrackingSessionDescriptor.class);
    List<TrackingParticipant> participants = new ArrayList<>();
    for (Path file : options.participantFiles()) {
      participants.add(
          TrackingJson.read(
              Files.readString(file, StandardCharsets.UTF_8), TrackingParticipant.class));
    }
    TrackingJsonlReadResult outbox =
        TrackingJson.readEventsJsonlRecoveringTruncatedTail(options.eventsFile());
    List<TrackingEvent> events = outbox.events();
    if (outbox.truncatedTailIgnored()) {
      System.err.println(
          "Warning: ignored an incomplete final line in "
              + options.eventsFile().toAbsolutePath()
              + "; complete preceding events will be imported.");
    }
    validateOutbox(session, events);

    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    TrackingAck lastAck = null;
    if (events.isEmpty()) {
      lastAck =
          upload(
              client,
              options,
              new TrackingBatch(session.schemaVersion(), session, participants, List.of()));
    } else {
      for (int start = 0; start < events.size(); start += options.batchSize()) {
        int end = Math.min(start + options.batchSize(), events.size());
        lastAck =
            upload(
                client,
                options,
                new TrackingBatch(
                    session.schemaVersion(), session, participants, events.subList(start, end)));
      }
    }
    if (options.finishFile().isPresent()) {
      TrackingSessionFinish finish =
          TrackingJson.read(
              Files.readString(options.finishFile().orElseThrow(), StandardCharsets.UTF_8),
              TrackingSessionFinish.class);
      if (!finish.sessionId().equals(session.sessionId())) {
        throw new IllegalArgumentException("Finish and session files have different session IDs");
      }
      lastAck =
          validateAcknowledgement(
              post(client, options, "finish", TrackingJson.write(finish)),
              session.sessionId(),
              finish.finalSequence());
    }
    System.out.println(
        "Imported "
            + events.size()
            + " events; backend acknowledgement is sequence "
            + lastAck.lastPersistedSequence());
  }

  private static TrackingAck upload(
      final HttpClient client, final Arguments options, final TrackingBatch batch)
      throws IOException, InterruptedException {
    long lastSequence = batch.events().isEmpty() ? 0 : batch.events().getLast().sessionSequence();
    return validateAcknowledgement(
        post(client, options, "events", TrackingJson.write(batch)),
        batch.session().sessionId(),
        lastSequence);
  }

  private static TrackingAck validateAcknowledgement(
      final TrackingAck acknowledgement, final UUID expectedSessionId, final long minimumSequence)
      throws IOException {
    if (!acknowledgement.sessionId().equals(expectedSessionId)) {
      throw new IOException("Backend acknowledged a different session");
    }
    if (acknowledgement.lastPersistedSequence() < minimumSequence) {
      throw new IOException("Backend acknowledgement is behind the imported data");
    }
    return acknowledgement;
  }

  private static TrackingAck post(
      final HttpClient client, final Arguments options, final String operation, final String json)
      throws IOException, InterruptedException {
    String base = options.baseUri().toString().replaceAll("/+$", "");
    URI endpoint = URI.create(base + "/tracking/sessions/" + options.sessionId() + "/" + operation);
    HttpRequest.Builder request =
        HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
    options.apiKey().ifPresent(value -> request.header("Authorization", "Bearer " + value));
    HttpResponse<String> response =
        client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() != 200) {
      throw new IOException("Backend rejected import with HTTP " + response.statusCode());
    }
    return TrackingJson.read(response.body(), TrackingAck.class);
  }

  private static void validateOutbox(
      final TrackingSessionDescriptor session, final List<TrackingEvent> events) {
    for (TrackingEvent event : events) {
      if (!event.sessionId().equals(session.sessionId())) {
        throw new IllegalArgumentException("Outbox contains an event from another session");
      }
    }
  }

  private record Arguments(
      URI baseUri,
      Path sessionFile,
      Path eventsFile,
      List<Path> participantFiles,
      Optional<Path> finishFile,
      Optional<String> apiKey,
      int batchSize,
      String sessionId) {
    static Arguments parse(final String[] arguments) {
      URI baseUri = null;
      Path sessionFile = null;
      Path eventsFile = null;
      List<Path> participants = new ArrayList<>();
      Optional<Path> finish = Optional.empty();
      Optional<Path> apiKeyFile = Optional.empty();
      int batchSize = 500;
      for (int index = 0; index < arguments.length; index += 2) {
        if (index + 1 >= arguments.length) {
          throw usage();
        }
        String value = arguments[index + 1];
        switch (arguments[index]) {
          case "--url" -> baseUri = URI.create(value);
          case "--session" -> sessionFile = Path.of(value);
          case "--events", "--outbox" -> eventsFile = Path.of(value);
          case "--participant" -> participants.add(Path.of(value));
          case "--finish" -> finish = Optional.of(Path.of(value));
          case "--api-key-file" -> apiKeyFile = Optional.of(Path.of(value));
          case "--batch-size" -> batchSize = Integer.parseInt(value);
          default -> throw usage();
        }
      }
      if (baseUri == null || eventsFile == null || batchSize < 1) {
        throw usage();
      }
      Optional<String> apiKey = Optional.empty();
      if (apiKeyFile.isPresent()) {
        try {
          String value = Files.readString(apiKeyFile.orElseThrow(), StandardCharsets.UTF_8).strip();
          if (value.isEmpty()) {
            throw new IllegalArgumentException("API-key file is empty");
          }
          apiKey = Optional.of(value);
        } catch (IOException exception) {
          throw new IllegalArgumentException("Cannot read API-key file", exception);
        }
      } else {
        apiKey = environmentApiKey();
      }
      String inferredSessionId = inferSessionId(eventsFile);
      Path sidecarDirectory = eventsFile.toAbsolutePath().getParent();
      if (sessionFile == null) {
        sessionFile = sidecarDirectory.resolve(inferredSessionId + ".session.json");
      }
      TrackingSessionDescriptor session;
      try {
        session =
            TrackingJson.read(
                Files.readString(sessionFile, StandardCharsets.UTF_8),
                TrackingSessionDescriptor.class);
      } catch (IOException exception) {
        throw new IllegalArgumentException("Cannot read session file", exception);
      }
      if (!session.sessionId().toString().equals(inferredSessionId)) {
        throw new IllegalArgumentException("Outbox and session sidecar have different session IDs");
      }
      if (participants.isEmpty()) {
        participants.addAll(findParticipantSidecars(sidecarDirectory, inferredSessionId));
      }
      if (finish.isEmpty()) {
        Path inferredFinish = sidecarDirectory.resolve(inferredSessionId + ".finish.json");
        if (Files.isRegularFile(inferredFinish)) {
          finish = Optional.of(inferredFinish);
        }
      }
      return new Arguments(
          baseUri,
          sessionFile,
          eventsFile,
          List.copyOf(participants),
          finish,
          apiKey,
          batchSize,
          session.sessionId().toString());
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
          "Usage: --url URL --outbox EVENTS.jsonl [--session FILE] [--participant FILE] "
              + "[--finish FILE] [--api-key-file FILE] [--batch-size N]");
    }

    private static String inferSessionId(final Path eventsFile) {
      try {
        List<TrackingEvent> events =
            TrackingJson.readEventsJsonlRecoveringTruncatedTail(eventsFile).events();
        if (!events.isEmpty()) {
          return events.getFirst().sessionId().toString();
        }
      } catch (IOException exception) {
        throw new IllegalArgumentException("Cannot read outbox file", exception);
      }
      String fileName = eventsFile.getFileName().toString();
      String candidate =
          fileName.endsWith(".events.jsonl")
              ? fileName.substring(0, fileName.length() - ".events.jsonl".length())
              : fileName.endsWith(".jsonl")
                  ? fileName.substring(0, fileName.length() - ".jsonl".length())
                  : fileName;
      try {
        return UUID.fromString(candidate).toString();
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException(
            "An empty outbox filename must start with its session UUID", exception);
      }
    }

    private static List<Path> findParticipantSidecars(
        final Path directory, final String sessionId) {
      String prefix = sessionId + ".participant-";
      try (Stream<Path> files = Files.list(directory)) {
        return files
            .filter(Files::isRegularFile)
            .filter(
                path -> {
                  String name = path.getFileName().toString();
                  return name.startsWith(prefix) && name.endsWith(".json");
                })
            .sorted(Comparator.comparing(path -> path.getFileName().toString()))
            .toList();
      } catch (IOException exception) {
        throw new IllegalArgumentException("Cannot list participant sidecars", exception);
      }
    }
  }
}
