package core.network.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping protobuf message types to wire type identifiers.
 *
 * <p>This registry is shared by client and server to ensure consistent type IDs.
 */
public final class MessageRegistry {
  private static final byte CONNECT_REQUEST = 1;
  private static final byte INPUT_MESSAGE = 2;
  private static final byte DIALOG_RESPONSE_MESSAGE = 3;
  private static final byte REGISTER_UDP = 4;
  private static final byte REQUEST_ENTITY_SPAWN = 5;
  private static final byte SOUND_FINISHED_MESSAGE = 6;
  private static final byte CONNECT_ACK = 7;
  private static final byte CONNECT_REJECT = 8;
  private static final byte DIALOG_SHOW_MESSAGE = 9;
  private static final byte DIALOG_CLOSE_MESSAGE = 10;
  private static final byte ENTITY_SPAWN_EVENT = 11;
  private static final byte ENTITY_SPAWN_BATCH = 12;
  private static final byte ENTITY_DESPAWN_EVENT = 13;
  private static final byte ENTITY_STATE = 14;
  private static final byte GAME_OVER_EVENT = 15;
  private static final byte LEVEL_CHANGE_EVENT = 16;
  private static final byte REGISTER_ACK = 17;
  private static final byte SNAPSHOT_MESSAGE = 18;
  private static final byte SOUND_PLAY_MESSAGE = 19;
  private static final byte SOUND_STOP_MESSAGE = 20;

  private static final Map<Byte, Parser<? extends Message>> PARSERS = new HashMap<>();
  private static final Map<Class<? extends Message>, Byte> TYPE_IDS = new HashMap<>();

  static {
    register(
        CONNECT_REQUEST,
        core.network.proto.c2s.ConnectRequest.class,
        core.network.proto.c2s.ConnectRequest.parser());
    register(
        INPUT_MESSAGE,
        core.network.proto.c2s.InputMessage.class,
        core.network.proto.c2s.InputMessage.parser());
    register(
        DIALOG_RESPONSE_MESSAGE,
        core.network.proto.c2s.DialogResponseMessage.class,
        core.network.proto.c2s.DialogResponseMessage.parser());
    register(
        REGISTER_UDP,
        core.network.proto.c2s.RegisterUdp.class,
        core.network.proto.c2s.RegisterUdp.parser());
    register(
        REQUEST_ENTITY_SPAWN,
        core.network.proto.c2s.RequestEntitySpawn.class,
        core.network.proto.c2s.RequestEntitySpawn.parser());
    register(
        SOUND_FINISHED_MESSAGE,
        core.network.proto.c2s.SoundFinishedMessage.class,
        core.network.proto.c2s.SoundFinishedMessage.parser());
    register(
        CONNECT_ACK,
        core.network.proto.s2c.ConnectAck.class,
        core.network.proto.s2c.ConnectAck.parser());
    register(
        CONNECT_REJECT,
        core.network.proto.s2c.ConnectReject.class,
        core.network.proto.s2c.ConnectReject.parser());
    register(
        DIALOG_SHOW_MESSAGE,
        core.network.proto.s2c.DialogShowMessage.class,
        core.network.proto.s2c.DialogShowMessage.parser());
    register(
        DIALOG_CLOSE_MESSAGE,
        core.network.proto.s2c.DialogCloseMessage.class,
        core.network.proto.s2c.DialogCloseMessage.parser());
    register(
        ENTITY_SPAWN_EVENT,
        core.network.proto.s2c.EntitySpawnEvent.class,
        core.network.proto.s2c.EntitySpawnEvent.parser());
    register(
        ENTITY_SPAWN_BATCH,
        core.network.proto.s2c.EntitySpawnBatch.class,
        core.network.proto.s2c.EntitySpawnBatch.parser());
    register(
        ENTITY_DESPAWN_EVENT,
        core.network.proto.s2c.EntityDespawnEvent.class,
        core.network.proto.s2c.EntityDespawnEvent.parser());
    register(
        ENTITY_STATE,
        core.network.proto.s2c.EntityState.class,
        core.network.proto.s2c.EntityState.parser());
    register(
        GAME_OVER_EVENT,
        core.network.proto.s2c.GameOverEvent.class,
        core.network.proto.s2c.GameOverEvent.parser());
    register(
        LEVEL_CHANGE_EVENT,
        core.network.proto.s2c.LevelChangeEvent.class,
        core.network.proto.s2c.LevelChangeEvent.parser());
    register(
        REGISTER_ACK,
        core.network.proto.s2c.RegisterAck.class,
        core.network.proto.s2c.RegisterAck.parser());
    register(
        SNAPSHOT_MESSAGE,
        core.network.proto.s2c.SnapshotMessage.class,
        core.network.proto.s2c.SnapshotMessage.parser());
    register(
        SOUND_PLAY_MESSAGE,
        core.network.proto.s2c.SoundPlayMessage.class,
        core.network.proto.s2c.SoundPlayMessage.parser());
    register(
        SOUND_STOP_MESSAGE,
        core.network.proto.s2c.SoundStopMessage.class,
        core.network.proto.s2c.SoundStopMessage.parser());
  }

  private MessageRegistry() {}

  /**
   * Returns the parser registered for the given type identifier.
   *
   * @param typeId the wire type identifier
   * @return the registered protobuf parser
   * @throws IllegalArgumentException if the type ID is unknown
   */
  public static Parser<? extends Message> parser(byte typeId) {
    Parser<? extends Message> parser = PARSERS.get(typeId);
    if (parser == null) {
      throw new IllegalArgumentException("Unknown message type id: " + typeId);
    }
    return parser;
  }

  /**
   * Returns the registered type identifier for the given protobuf message instance.
   *
   * @param message the protobuf message
   * @return the registered type identifier
   * @throws IllegalArgumentException if the message type is unknown
   */
  public static byte typeId(Message message) {
    Byte typeId = TYPE_IDS.get(message.getClass());
    if (typeId == null) {
      throw new IllegalArgumentException(
          "Unregistered message type: " + message.getClass().getName());
    }
    return typeId;
  }

  /**
   * Parses a protobuf message based on the given type identifier.
   *
   * @param typeId the message type identifier
   * @param data the protobuf payload bytes
   * @return the parsed message
   * @throws InvalidProtocolBufferException if parsing fails
   */
  public static Message parse(byte typeId, byte[] data) throws InvalidProtocolBufferException {
    return parser(typeId).parseFrom(data);
  }

  private static <T extends Message> void register(byte typeId, Class<T> clazz, Parser<T> parser) {
    Parser<? extends Message> existingParser = PARSERS.putIfAbsent(typeId, parser);
    if (existingParser != null) {
      throw new IllegalStateException("Duplicate type id registration: " + typeId);
    }
    Byte existingType = TYPE_IDS.putIfAbsent(clazz, typeId);
    if (existingType != null) {
      throw new IllegalStateException("Duplicate type registration for " + clazz.getName());
    }
  }
}
