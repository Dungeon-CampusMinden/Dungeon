package tracking.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Uploads one self-contained JSONL outbox through the tracking HTTP API.
 *
 * <p>The importer CLI and the game use it for outboxes whose live upload did not complete. The
 * upload repeats the whole session, which the API accepts for data it already stores.
 */
public final class TrackingOutboxUpload {
  private TrackingOutboxUpload() {}

  /**
   * Sends the session, its events in batches, and its finish record when the outbox has one.
   *
   * @param client HTTP client used for all requests
   * @param baseUri backend base URI
   * @param apiKey optional bearer credential
   * @param outbox records read from the outbox file
   * @param batchSize maximum number of events per request
   * @return the last acknowledgement; it confirms the finish when the outbox has one
   * @throws IOException when a request fails or the backend rejects or under-acknowledges it
   * @throws InterruptedException when the calling thread is interrupted during a request
   */
  public static TrackingAck upload(
      final HttpClient client,
      final URI baseUri,
      final Optional<String> apiKey,
      final TrackingJsonlReadResult outbox,
      final int batchSize)
      throws IOException, InterruptedException {
    List<TrackingParticipant> participants = reconstructParticipants(outbox.events());
    UUID sessionId = outbox.session().sessionId();
    TrackingAck lastAck;
    int start = 0;
    // A session without events still uploads its descriptor in one empty batch.
    do {
      int end = Math.min(start + batchSize, outbox.events().size());
      List<TrackingEvent> events = outbox.events().subList(start, end);
      TrackingBatch batch =
          new TrackingBatch(
              outbox.session().schemaVersion(), outbox.session(), participants, events);
      lastAck =
          validateAcknowledgement(
              post(client, baseUri, apiKey, sessionId, "events", TrackingJson.write(batch)),
              sessionId,
              events.isEmpty() ? 0 : events.getLast().sessionSequence());
      start = end;
    } while (start < outbox.events().size());
    if (outbox.finish().isPresent()) {
      TrackingSessionFinish finish = outbox.finish().orElseThrow();
      lastAck =
          validateAcknowledgement(
              post(client, baseUri, apiKey, sessionId, "finish", TrackingJson.write(finish)),
              sessionId,
              finish.finalSequence());
    }
    return lastAck;
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
          || event.eventType() == TrackingEventType.INTERACTION_RECORDED
          || event.eventType() == TrackingEventType.HINT_USED) {
        UUID participantId = event.participantId().orElseThrow();
        TrackingParticipant participant = participants.get(participantId);
        if (participant == null
            || (participant.leftAt().isPresent()
                && event.eventType() != TrackingEventType.ANSWER_SUBMITTED)) {
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
      final URI baseUri,
      final Optional<String> apiKey,
      final UUID sessionId,
      final String operation,
      final String json)
      throws IOException, InterruptedException {
    String base = baseUri.toString().replaceAll("/+$", "");
    URI endpoint = URI.create(base + "/tracking/sessions/" + sessionId + "/" + operation);
    HttpRequest.Builder request =
        HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
    apiKey.ifPresent(value -> request.header("Authorization", "Bearer " + value));
    HttpResponse<String> response =
        client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() != 200) {
      throw new IOException("Backend rejected import with HTTP " + response.statusCode());
    }
    return TrackingJson.read(response.body(), TrackingAck.class);
  }
}
