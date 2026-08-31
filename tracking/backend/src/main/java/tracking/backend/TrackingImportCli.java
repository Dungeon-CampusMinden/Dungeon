package tracking.backend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import tracking.core.TrackingAck;
import tracking.core.TrackingBatch;
import tracking.core.TrackingEvent;
import tracking.core.TrackingEventType;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlReadResult;
import tracking.core.TrackingParticipant;

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

    List<TrackingParticipant> participants = reconstructParticipants(outbox.events());
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    TrackingAck lastAck;
    if (outbox.events().isEmpty()) {
      lastAck =
          upload(
              client,
              options,
              new TrackingBatch(
                  outbox.session().schemaVersion(), outbox.session(), participants, List.of()));
    } else {
      int firstEnd = Math.min(options.batchSize(), outbox.events().size());
      lastAck =
          upload(
              client,
              options,
              new TrackingBatch(
                  outbox.session().schemaVersion(),
                  outbox.session(),
                  participants,
                  outbox.events().subList(0, firstEnd)));
      for (int start = firstEnd; start < outbox.events().size(); start += options.batchSize()) {
        int end = Math.min(start + options.batchSize(), outbox.events().size());
        lastAck =
            upload(
                client,
                options,
                new TrackingBatch(
                    outbox.session().schemaVersion(),
                    outbox.session(),
                    participants,
                    outbox.events().subList(start, end)));
      }
    }
    if (outbox.finish().isPresent()) {
      lastAck =
          validateAcknowledgement(
              post(
                  client,
                  options,
                  outbox.session().sessionId(),
                  "finish",
                  TrackingJson.write(outbox.finish().orElseThrow())),
              outbox.session().sessionId(),
              outbox.finish().orElseThrow().finalSequence());
    }
    System.out.println(
        "Imported "
            + outbox.events().size()
            + " events; backend acknowledgement is sequence "
            + lastAck.lastPersistedSequence());
  }

  private static List<TrackingParticipant> reconstructParticipants(
      final List<TrackingEvent> events) {
    Map<UUID, TrackingParticipant> participants = new LinkedHashMap<>();
    for (TrackingEvent event : events) {
      if (event.eventType() == TrackingEventType.PARTICIPANT_JOINED) {
        UUID participantId = event.participantId().orElseThrow();
        boolean roomPlayedBefore = roomPlayedBefore(event);
        TrackingParticipant previous = participants.get(participantId);
        if (previous == null) {
          participants.put(
              participantId,
              new TrackingParticipant(
                  event.sessionId(),
                  participantId,
                  roomPlayedBefore,
                  event.occurredAt(),
                  Optional.empty()));
        } else {
          if (previous.leftAt().isEmpty()) {
            throw new IllegalArgumentException(
                "Participant joined while already active at sequence " + event.sessionSequence());
          }
          if (previous.roomPlayedBefore() != roomPlayedBefore) {
            throw new IllegalArgumentException(
                "Participant rejoin changes roomPlayedBefore at sequence "
                    + event.sessionSequence());
          }
          participants.put(
              participantId,
              new TrackingParticipant(
                  previous.sessionId(),
                  participantId,
                  previous.roomPlayedBefore(),
                  previous.joinedAt(),
                  Optional.empty()));
        }
      } else if (event.eventType() == TrackingEventType.PARTICIPANT_LEFT) {
        UUID participantId = event.participantId().orElseThrow();
        TrackingParticipant previous = participants.get(participantId);
        if (previous == null) {
          throw new IllegalArgumentException(
              "Participant left before joining at sequence " + event.sessionSequence());
        }
        if (previous.leftAt().isPresent()) {
          throw new IllegalArgumentException(
              "Participant left repeatedly at sequence " + event.sessionSequence());
        }
        participants.put(
            participantId,
            new TrackingParticipant(
                previous.sessionId(),
                participantId,
                previous.roomPlayedBefore(),
                previous.joinedAt(),
                Optional.of(event.occurredAt())));
      } else if (event.eventType() == TrackingEventType.ANSWER_SUBMITTED
          || event.eventType() == TrackingEventType.HINT_USED) {
        UUID participantId = event.participantId().orElseThrow();
        TrackingParticipant participant = participants.get(participantId);
        if (participant == null || participant.leftAt().isPresent()) {
          throw new IllegalArgumentException(
              "Participant-attributed event occurred while inactive at sequence "
                  + event.sessionSequence());
        }
      }
    }
    return List.copyOf(participants.values());
  }

  private static boolean roomPlayedBefore(final TrackingEvent event) {
    var value = event.payload().get("roomPlayedBefore");
    if (value == null || !value.isBoolean()) {
      throw new IllegalArgumentException(
          "Participant join lacks boolean roomPlayedBefore at sequence " + event.sessionSequence());
    }
    return value.booleanValue();
  }

  private static TrackingAck upload(
      final HttpClient client, final Arguments options, final TrackingBatch batch)
      throws IOException, InterruptedException {
    long lastSequence = batch.events().isEmpty() ? 0 : batch.events().getLast().sessionSequence();
    return validateAcknowledgement(
        post(client, options, batch.session().sessionId(), "events", TrackingJson.write(batch)),
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
      final HttpClient client,
      final Arguments options,
      final UUID sessionId,
      final String operation,
      final String json)
      throws IOException, InterruptedException {
    String base = options.baseUri().toString().replaceAll("/+$", "");
    URI endpoint = URI.create(base + "/tracking/sessions/" + sessionId + "/" + operation);
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
