package core.network.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import core.network.messages.NetworkMessage;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for network message converters.
 *
 * <p>This registry unifies message conversion dispatch and wire type ID lookup.
 */
public final class ConverterRegistry {
  private static volatile ConverterRegistry globalInstance;

  private final Map<Class<?>, MessageConverter<?, ?>> byDomainType = new ConcurrentHashMap<>();
  private final Map<Class<?>, MessageConverter<?, ?>> byProtoType = new ConcurrentHashMap<>();
  private final Map<Byte, MessageConverter<?, ?>> byWireId = new ConcurrentHashMap<>();

  /** Creates an empty converter registry. */
  public ConverterRegistry() {}

  /**
   * Returns the shared global converter registry instance.
   *
   * @return the global registry
   */
  public static ConverterRegistry global() {
    ConverterRegistry instance = globalInstance;
    if (instance != null) {
      return instance;
    }
    synchronized (ConverterRegistry.class) {
      instance = globalInstance;
      if (instance == null) {
        instance = new ConverterRegistry();
        CoreConverters.registerAll(instance);
        globalInstance = instance;
      }
    }
    return instance;
  }

  /**
   * Registers a converter.
   *
   * @param converter the converter to register
   * @param <D> the domain message type
   * @param <P> the protobuf message type
   */
  public <D extends NetworkMessage, P extends Message> void register(
      MessageConverter<D, P> converter) {
    Objects.requireNonNull(converter, "converter");

    validateWireTypeId(converter.wireTypeId());

    MessageConverter<?, ?> existingDomain =
        byDomainType.putIfAbsent(converter.domainType(), converter);
    if (existingDomain != null) {
      throw new IllegalStateException(
          "Duplicate domain converter registration for "
              + converter.domainType().getName()
              + " (existing converter: "
              + existingDomain.getClass().getName()
              + ").");
    }

    MessageConverter<?, ?> existingProto =
        byProtoType.putIfAbsent(converter.protoType(), converter);
    if (existingProto != null) {
      byDomainType.remove(converter.domainType(), converter);
      throw new IllegalStateException(
          "Duplicate proto converter registration for "
              + converter.protoType().getName()
              + " (existing converter: "
              + existingProto.getClass().getName()
              + ").");
    }

    MessageConverter<?, ?> existingWire = byWireId.putIfAbsent(converter.wireTypeId(), converter);
    if (existingWire != null) {
      byDomainType.remove(converter.domainType(), converter);
      byProtoType.remove(converter.protoType(), converter);
      throw new IllegalStateException(
          "Duplicate wire type registration for "
              + Byte.toUnsignedInt(converter.wireTypeId())
              + " (existing converter: "
              + existingWire.getClass().getName()
              + ").");
    }
  }

  /**
   * Converts a domain message to protobuf.
   *
   * @param message the domain message
   * @return the protobuf representation
   */
  public Message toProto(NetworkMessage message) {
    Objects.requireNonNull(message, "message");
    MessageConverter<NetworkMessage, Message> converter =
        converterByDomainType(message.getClass(), message);
    return converter.toProto(message);
  }

  /**
   * Converts a protobuf message to a domain message.
   *
   * @param proto the protobuf message
   * @return the domain representation
   */
  public NetworkMessage fromProto(Message proto) {
    Objects.requireNonNull(proto, "proto");
    MessageConverter<NetworkMessage, Message> converter =
        converterByProtoType(proto.getClass(), proto);
    return converter.fromProto(proto);
  }

  /**
   * Returns the wire type identifier for a protobuf message.
   *
   * @param proto the protobuf message
   * @return the wire type ID
   */
  public byte typeId(Message proto) {
    Objects.requireNonNull(proto, "proto");
    MessageConverter<?, ?> converter = byProtoType.get(proto.getClass());
    if (converter == null) {
      throw new IllegalArgumentException(
          "Unsupported proto message type: " + proto.getClass().getName());
    }
    return converter.wireTypeId();
  }

  /**
   * Parses wire bytes into a protobuf message.
   *
   * @param typeId the wire type identifier
   * @param data the protobuf payload bytes
   * @return the parsed protobuf message
   * @throws InvalidProtocolBufferException if parsing fails
   */
  public Message parse(byte typeId, byte[] data) throws InvalidProtocolBufferException {
    Objects.requireNonNull(data, "data");
    MessageConverter<?, ?> converter = byWireId.get(typeId);
    if (converter == null) {
      throw new IllegalArgumentException("Unknown message type id: " + Byte.toUnsignedInt(typeId));
    }
    return converter.parser().parseFrom(data);
  }

  @SuppressWarnings("unchecked")
  private MessageConverter<NetworkMessage, Message> converterByDomainType(
      Class<?> messageClass, NetworkMessage message) {
    MessageConverter<?, ?> converter = byDomainType.get(messageClass);
    if (converter == null) {
      throw new IllegalArgumentException(
          "Unsupported network message type: " + message.getClass().getName());
    }
    return (MessageConverter<NetworkMessage, Message>) converter;
  }

  @SuppressWarnings("unchecked")
  private MessageConverter<NetworkMessage, Message> converterByProtoType(
      Class<?> protoClass, Message proto) {
    MessageConverter<?, ?> converter = byProtoType.get(protoClass);
    if (converter == null) {
      throw new IllegalArgumentException(
          "Unsupported proto message type: " + proto.getClass().getName());
    }
    return (MessageConverter<NetworkMessage, Message>) converter;
  }

  private static void validateWireTypeId(byte wireTypeId) {
    if (wireTypeId == 0) {
      throw new IllegalArgumentException("wireTypeId 0 is reserved.");
    }
  }
}
