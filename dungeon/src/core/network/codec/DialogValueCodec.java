package core.network.codec;

import java.io.Serializable;

/**
 * Codec for encoding and decoding custom dialog attribute and payload types to bytes.
 *
 * <p>Subprojects implement this interface for each custom {@link Serializable} type they need to
 * send through dialog context attributes ({@link
 * core.network.codec.converters.s2c.DialogShowConverter}) or dialog response payloads ({@link
 * core.network.codec.converters.c2s.DialogResponseConverter}).
 *
 * <p>Register implementations via {@link DialogValueCodecRegistry#register(DialogValueCodec)}.
 *
 * @param <T> the custom type, must be {@link Serializable}
 */
public interface DialogValueCodec<T extends Serializable> {

  /**
   * Unique string discriminator written to the protobuf {@code CustomValue.type_id} field.
   *
   * <p>Must be stable across versions; changing it breaks wire compatibility.
   *
   * @return the type identifier
   */
  String typeId();

  /**
   * The concrete Java class this codec handles.
   *
   * <p>Used for encode-side dispatch with exact-class matching.
   *
   * @return the handled class
   */
  Class<T> type();

  /**
   * Encodes a value to a byte array for protobuf transport.
   *
   * @param value the value to encode
   * @return the encoded bytes
   */
  byte[] encode(T value);

  /**
   * Decodes a value from a byte array received over protobuf.
   *
   * @param data the encoded bytes
   * @return the decoded value
   */
  T decode(byte[] data);
}
