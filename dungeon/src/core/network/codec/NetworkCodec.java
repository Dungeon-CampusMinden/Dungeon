package core.network.codec;

import io.netty.buffer.ByteBuf;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility class for serializing and deserializing objects over the network.
 *
 * <p>This class provides methods to serialize objects into byte arrays and deserialize byte arrays
 * back into objects. It ensures that the objects being serialized are `Serializable` and handles
 * the conversion using Java's built-in serialization mechanisms.
 *
 * <p>TODO: Replace with more efficient and secure serialization.
 */
public final class NetworkCodec {

  // Private constructor to prevent instantiation of this utility class
  private NetworkCodec() {}

  /**
   * Serializes a given object into a byte array.
   *
   * @param obj the object to serialize; must implement {@link Serializable}
   * @return a byte array representing the serialized object
   * @throws IOException if an I/O error occurs during serialization
   * @throws NotSerializableException if the object does not implement {@link Serializable}
   */
  public static byte[] serialize(Object obj) throws IOException {
    if (!(obj instanceof Serializable)) {
      throw new NotSerializableException("Object not serializable: " + obj);
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    return bos.toByteArray();
  }

  /**
   * Deserializes an object from a {@link ByteBuf}.
   *
   * @param buf the {@link ByteBuf} containing the serialized object data
   * @return the deserialized object
   * @throws IOException if an I/O error occurs during deserialization
   * @throws ClassNotFoundException if the class of the deserialized object cannot be found
   */
  public static Object deserialize(ByteBuf buf) throws IOException, ClassNotFoundException {
    int len = buf.readableBytes();
    byte[] array = new byte[len];
    buf.getBytes(buf.readerIndex(), array);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array))) {
      return ois.readObject();
    }
  }
}
