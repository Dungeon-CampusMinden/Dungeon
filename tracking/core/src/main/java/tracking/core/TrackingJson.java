package tracking.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.exc.UnexpectedEndOfInputException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Stable JSON and JSONL codec for the public tracking records. */
public final class TrackingJson {
  private static final ObjectMapper MAPPER =
      JsonMapper.builder(
              JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .build();

  private TrackingJson() {}

  /**
   * Creates an empty structured payload.
   *
   * @return mutable JSON object
   */
  public static ObjectNode object() {
    return MAPPER.createObjectNode();
  }

  /**
   * Parses an arbitrary JSON object for use as an event payload.
   *
   * @param json JSON object text
   * @return mutable JSON object
   */
  public static ObjectNode object(final String json) {
    JsonNode node = parse(json);
    if (!(node instanceof ObjectNode object)) {
      throw new IllegalArgumentException("JSON value must be an object");
    }
    return object;
  }

  /**
   * Encodes any public tracking record with its stable field order.
   *
   * @param value public tracking record
   * @return compact JSON
   */
  public static String write(final Object value) {
    return switch (value) {
      case TrackingEvent event -> event(event);
      case TrackingBatch batch -> batch(batch);
      case TrackingSessionDescriptor session -> session(session);
      case TrackingParticipant participant -> participant(participant);
      case TrackingAck ack -> ack(ack);
      case TrackingSessionFinish finish -> finish(finish);
      default ->
          throw new IllegalArgumentException("Unsupported tracking type: " + value.getClass());
    };
  }

  /**
   * Decodes one public tracking record of the requested type.
   *
   * @param json compact JSON
   * @param type requested tracking record class
   * @param <T> tracking record type
   * @return decoded record
   */
  public static <T> T read(final String json, final Class<T> type) {
    Object value;
    if (type == TrackingEvent.class) {
      value = event(json);
    } else if (type == TrackingBatch.class) {
      value = batch(json);
    } else if (type == TrackingSessionDescriptor.class) {
      value = session(json);
    } else if (type == TrackingParticipant.class) {
      value = participant(json);
    } else if (type == TrackingAck.class) {
      value = ack(json);
    } else if (type == TrackingSessionFinish.class) {
      value = finish(json);
    } else {
      throw new IllegalArgumentException("Unsupported tracking type: " + type);
    }
    return type.cast(value);
  }

  // Keep field insertion order stable for JSONL diffs.
  private static String event(final TrackingEvent event) {
    return encode(eventNode(event));
  }

  private static TrackingEvent event(final String json) {
    return event(requireObject(parse(json), "event"));
  }

  private static String batch(final TrackingBatch batch) {
    ObjectNode root = object();
    root.put("schemaVersion", batch.schemaVersion());
    root.set("session", sessionNode(batch.session()));
    ArrayNode participants = root.putArray("participants");
    batch.participants().forEach(value -> participants.add(participantNode(value)));
    ArrayNode events = root.putArray("events");
    batch.events().forEach(value -> events.add(eventNode(value)));
    return encode(root);
  }

  private static TrackingBatch batch(final String json) {
    ObjectNode root = requireObject(parse(json), "batch");
    TrackingSessionDescriptor session =
        session(requireObject(required(root, "session"), "session"));
    List<TrackingParticipant> participants = new ArrayList<>();
    for (JsonNode item : requiredArray(root, "participants")) {
      participants.add(participant(requireObject(item, "participant")));
    }
    List<TrackingEvent> events = new ArrayList<>();
    for (JsonNode item : requiredArray(root, "events")) {
      events.add(event(requireObject(item, "event")));
    }
    return new TrackingBatch(requiredInt(root, "schemaVersion"), session, participants, events);
  }

  private static String session(final TrackingSessionDescriptor session) {
    return encode(sessionNode(session));
  }

  private static TrackingSessionDescriptor session(final String json) {
    return session(requireObject(parse(json), "session"));
  }

  private static String participant(final TrackingParticipant participant) {
    return encode(participantNode(participant));
  }

  private static TrackingParticipant participant(final String json) {
    return participant(requireObject(parse(json), "participant"));
  }

  private static String ack(final TrackingAck ack) {
    ObjectNode root = object();
    root.put("schemaVersion", ack.schemaVersion());
    root.put("sessionId", ack.sessionId().toString());
    root.put("lastPersistedSequence", ack.lastPersistedSequence());
    root.put("acceptedEventCount", ack.acceptedEventCount());
    return encode(root);
  }

  private static TrackingAck ack(final String json) {
    ObjectNode root = requireObject(parse(json), "ack");
    return new TrackingAck(
        requiredInt(root, "schemaVersion"),
        requiredUuid(root, "sessionId"),
        requiredLong(root, "lastPersistedSequence"),
        requiredInt(root, "acceptedEventCount"));
  }

  private static String finish(final TrackingSessionFinish finish) {
    ObjectNode root = object();
    root.put("schemaVersion", finish.schemaVersion());
    root.put("sessionId", finish.sessionId().toString());
    root.put("finalSequence", finish.finalSequence());
    root.put("status", finish.status().name());
    root.put("endedAt", finish.endedAt().toString());
    root.put("elapsedMonotonicMs", finish.elapsedMonotonicMs());
    finish.abortedAtPuzzleId().ifPresent(value -> root.put("abortedAtPuzzleId", value));
    return encode(root);
  }

  private static TrackingSessionFinish finish(final String json) {
    ObjectNode root = requireObject(parse(json), "finish");
    return new TrackingSessionFinish(
        requiredInt(root, "schemaVersion"),
        requiredUuid(root, "sessionId"),
        requiredLong(root, "finalSequence"),
        requiredEnum(root, "status", TrackingSessionStatus.class),
        requiredInstant(root, "endedAt"),
        requiredLong(root, "elapsedMonotonicMs"),
        optionalText(root, "abortedAtPuzzleId"));
  }

  /**
   * Appends events as one compact JSON object per line.
   *
   * @param path destination outbox
   * @param events ordered events to append
   * @throws IOException when the outbox cannot be written
   */
  public static void appendEventsJsonl(final Path path, final List<TrackingEvent> events)
      throws IOException {
    Path absolute = path.toAbsolutePath();
    if (absolute.getParent() != null) {
      Files.createDirectories(absolute.getParent());
    }
    try (BufferedWriter writer =
        Files.newBufferedWriter(
            absolute,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND)) {
      for (TrackingEvent event : events) {
        writer.write(event(event));
        writer.newLine();
      }
    }
  }

  /**
   * Reads a JSONL event file in order. Blank lines are ignored.
   *
   * @param path source outbox
   * @return immutable events in file order
   * @throws IOException when the outbox cannot be read
   */
  public static List<TrackingEvent> readEventsJsonl(final Path path) throws IOException {
    return readEventsJsonl(path, false).events();
  }

  /**
   * Reads complete events and ignores only an incomplete or invalid UTF-8 unterminated tail.
   * Earlier corruption and invalid terminated lines remain errors. A valid unterminated event is
   * returned.
   *
   * @param path source outbox
   * @return immutable events and whether a truncated tail was ignored
   * @throws IOException when the outbox cannot be read
   */
  public static TrackingJsonlReadResult readEventsJsonlRecoveringTruncatedTail(final Path path)
      throws IOException {
    return readEventsJsonl(path, true);
  }

  private static TrackingJsonlReadResult readEventsJsonl(
      final Path path, final boolean recoverTruncatedTail) throws IOException {
    byte[] contents = Files.readAllBytes(path);
    List<TrackingEvent> events = new ArrayList<>();
    int completedPrefixLength = completedPrefixLength(contents);
    parseCompleteLines(decodeUtf8(contents, 0, completedPrefixLength), events);
    boolean truncatedTailIgnored = false;
    if (completedPrefixLength < contents.length) {
      try {
        String tail =
            decodeUtf8(contents, completedPrefixLength, contents.length - completedPrefixLength);
        if (!tail.isBlank()) {
          try {
            events.add(event(tail));
          } catch (TruncatedJsonException exception) {
            if (!recoverTruncatedTail) {
              throw invalidJsonl(events.size() + 1, exception);
            }
            truncatedTailIgnored = true;
          } catch (IllegalArgumentException exception) {
            throw invalidJsonl(events.size() + 1, exception);
          }
        }
      } catch (InvalidUtf8Exception exception) {
        if (!recoverTruncatedTail) {
          throw exception;
        }
        truncatedTailIgnored = true;
      }
    }
    return new TrackingJsonlReadResult(events, truncatedTailIgnored);
  }

  private static void parseCompleteLines(
      final String completedPrefix, final List<TrackingEvent> events) throws IOException {
    try (var reader = new java.io.BufferedReader(new StringReader(completedPrefix))) {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.isBlank()) {
          continue;
        }
        try {
          events.add(event(line));
        } catch (IllegalArgumentException exception) {
          throw invalidJsonl(lineNumber, exception);
        }
      }
    }
  }

  private static int completedPrefixLength(final byte[] contents) {
    for (int index = contents.length - 1; index >= 0; index--) {
      if (contents[index] == '\n' || contents[index] == '\r') {
        return index + 1;
      }
    }
    return 0;
  }

  private static String decodeUtf8(final byte[] contents, final int offset, final int length)
      throws InvalidUtf8Exception {
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(contents, offset, length))
          .toString();
    } catch (CharacterCodingException exception) {
      throw new InvalidUtf8Exception("Invalid UTF-8 in tracking JSONL", exception);
    }
  }

  private static IllegalArgumentException invalidJsonl(
      final int lineNumber, final IllegalArgumentException cause) {
    return new IllegalArgumentException("Invalid tracking JSONL at line " + lineNumber, cause);
  }

  private static ObjectNode sessionNode(final TrackingSessionDescriptor session) {
    ObjectNode root = object();
    root.put("schemaVersion", session.schemaVersion());
    root.put("sessionId", session.sessionId().toString());
    root.put("roomId", session.roomId());
    root.put("startedAt", session.startedAt().toString());
    return root;
  }

  private static TrackingSessionDescriptor session(final ObjectNode root) {
    return new TrackingSessionDescriptor(
        requiredInt(root, "schemaVersion"),
        requiredUuid(root, "sessionId"),
        requiredText(root, "roomId"),
        requiredInstant(root, "startedAt"));
  }

  private static ObjectNode participantNode(final TrackingParticipant participant) {
    ObjectNode root = object();
    root.put("sessionId", participant.sessionId().toString());
    root.put("participantId", participant.participantId().toString());
    root.put("roomPlayedBefore", participant.roomPlayedBefore());
    root.put("joinedAt", participant.joinedAt().toString());
    participant.leftAt().ifPresent(value -> root.put("leftAt", value.toString()));
    return root;
  }

  private static TrackingParticipant participant(final ObjectNode root) {
    return new TrackingParticipant(
        requiredUuid(root, "sessionId"),
        requiredUuid(root, "participantId"),
        requiredBoolean(root, "roomPlayedBefore"),
        requiredInstant(root, "joinedAt"),
        optionalInstant(root, "leftAt"));
  }

  private static ObjectNode eventNode(final TrackingEvent event) {
    ObjectNode root = object();
    root.put("schemaVersion", event.schemaVersion());
    root.put("sessionId", event.sessionId().toString());
    root.put("sessionSequence", event.sessionSequence());
    root.put("eventId", event.eventId());
    event.participantId().ifPresent(value -> root.put("participantId", value.toString()));
    root.put("roomId", event.roomId());
    root.put("eventType", event.eventType().name());
    event.puzzleId().ifPresent(value -> root.put("puzzleId", value));
    event.objectId().ifPresent(value -> root.put("objectId", value));
    event.outcome().ifPresent(value -> root.put("outcome", value.name()));
    root.put("elapsedMonotonicMs", event.elapsedMonotonicMs());
    root.put("occurredAt", event.occurredAt().toString());
    root.set("payload", event.payload());
    return root;
  }

  private static TrackingEvent event(final ObjectNode root) {
    return new TrackingEvent(
        requiredInt(root, "schemaVersion"),
        requiredUuid(root, "sessionId"),
        requiredLong(root, "sessionSequence"),
        requiredText(root, "eventId"),
        optionalUuid(root, "participantId"),
        requiredText(root, "roomId"),
        requiredEnum(root, "eventType", TrackingEventType.class),
        optionalText(root, "puzzleId"),
        optionalText(root, "objectId"),
        optionalEnum(root, "outcome", TrackingOutcome.class),
        requiredLong(root, "elapsedMonotonicMs"),
        requiredInstant(root, "occurredAt"),
        requireObject(required(root, "payload"), "payload"));
  }

  private static JsonNode parse(final String json) {
    if (json == null) {
      throw new NullPointerException("json");
    }
    try {
      JsonNode node = MAPPER.readTree(json);
      if (node == null) {
        throw new IllegalArgumentException("JSON must not be empty");
      }
      return node;
    } catch (JacksonException exception) {
      if (exception instanceof UnexpectedEndOfInputException) {
        throw new TruncatedJsonException("Truncated JSON", exception);
      }
      throw new IllegalArgumentException("Invalid JSON", exception);
    }
  }

  private static String encode(final JsonNode node) {
    try {
      return MAPPER.writeValueAsString(node);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Value cannot be encoded as JSON", exception);
    }
  }

  private static JsonNode required(final ObjectNode root, final String field) {
    JsonNode value = root.get(field);
    if (value == null || value.isNull()) {
      throw new IllegalArgumentException("Missing field: " + field);
    }
    return value;
  }

  private static ArrayNode requiredArray(final ObjectNode root, final String field) {
    JsonNode value = required(root, field);
    if (!(value instanceof ArrayNode array)) {
      throw new IllegalArgumentException(field + " must be an array");
    }
    return array;
  }

  private static ObjectNode requireObject(final JsonNode value, final String name) {
    if (!(value instanceof ObjectNode object)) {
      throw new IllegalArgumentException(name + " must be an object");
    }
    return object;
  }

  private static String requiredText(final ObjectNode root, final String field) {
    JsonNode value = required(root, field);
    if (!value.isString()) {
      throw new IllegalArgumentException(field + " must be a string");
    }
    return value.stringValue();
  }

  private static int requiredInt(final ObjectNode root, final String field) {
    JsonNode value = required(root, field);
    if (!value.isIntegralNumber() || !value.canConvertToInt()) {
      throw new IllegalArgumentException(field + " must be a 32-bit integer");
    }
    return value.intValue();
  }

  private static long requiredLong(final ObjectNode root, final String field) {
    JsonNode value = required(root, field);
    if (!value.isIntegralNumber() || !value.canConvertToLong()) {
      throw new IllegalArgumentException(field + " must be a 64-bit integer");
    }
    return value.longValue();
  }

  private static boolean requiredBoolean(final ObjectNode root, final String field) {
    JsonNode value = required(root, field);
    if (!value.isBoolean()) {
      throw new IllegalArgumentException(field + " must be a boolean");
    }
    return value.booleanValue();
  }

  private static UUID requiredUuid(final ObjectNode root, final String field) {
    try {
      return UUID.fromString(requiredText(root, field));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(field + " must be a UUID", exception);
    }
  }

  private static Optional<UUID> optionalUuid(final ObjectNode root, final String field) {
    return optionalText(root, field).map(UUID::fromString);
  }

  private static Instant requiredInstant(final ObjectNode root, final String field) {
    try {
      return Instant.parse(requiredText(root, field));
    } catch (DateTimeException exception) {
      throw new IllegalArgumentException(field + " must be a UTC instant", exception);
    }
  }

  private static Optional<Instant> optionalInstant(final ObjectNode root, final String field) {
    try {
      return optionalText(root, field).map(Instant::parse);
    } catch (DateTimeException exception) {
      throw new IllegalArgumentException(field + " must be a UTC instant", exception);
    }
  }

  private static Optional<String> optionalText(final ObjectNode root, final String field) {
    JsonNode value = root.get(field);
    if (value == null || value.isNull()) {
      return Optional.empty();
    }
    if (!value.isString()) {
      throw new IllegalArgumentException(field + " must be a string when present");
    }
    return Optional.of(value.stringValue());
  }

  private static <E extends Enum<E>> E requiredEnum(
      final ObjectNode root, final String field, final Class<E> type) {
    try {
      return Enum.valueOf(type, requiredText(root, field));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(field + " has an unsupported value", exception);
    }
  }

  private static <E extends Enum<E>> Optional<E> optionalEnum(
      final ObjectNode root, final String field, final Class<E> type) {
    return optionalText(root, field).map(value -> Enum.valueOf(type, value));
  }

  private static final class TruncatedJsonException extends IllegalArgumentException {
    private TruncatedJsonException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  private static final class InvalidUtf8Exception extends IOException {
    private InvalidUtf8Exception(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
