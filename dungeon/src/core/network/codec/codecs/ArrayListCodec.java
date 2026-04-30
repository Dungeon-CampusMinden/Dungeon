package core.network.codec.codecs;

import core.network.codec.DialogValueCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Built-in codec for {@link ArrayList}.
 *
 * <p>Uses standard Java serialization. All elements stored in the list must therefore implement
 * {@link java.io.Serializable}. This is the case for the dialog payload types currently produced by
 * {@code DialogFactory} (e.g. {@code DialogEntry}, {@code ChoiceOption}).
 *
 * <p>The codec is registered for the exact class {@link ArrayList} (matches the encoder's
 * exact-class lookup in {@link core.network.codec.converters.s2c.DialogShowConverter}). Other
 * {@link java.util.List} implementations would need their own codec or to be wrapped in an {@code
 * ArrayList} before being put into a {@code DialogContext}.
 */
@SuppressWarnings("rawtypes")
public final class ArrayListCodec implements DialogValueCodec<ArrayList> {

  @Override
  public String typeId() {
    return "ArrayList";
  }

  @Override
  public Class<ArrayList> type() {
    return ArrayList.class;
  }

  @Override
  public byte[] encode(ArrayList value) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(value);
      oos.flush();
      return baos.toByteArray();
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Failed to serialize ArrayList for dialog attribute. Ensure all elements are Serializable.",
          e);
    }
  }

  @Override
  public ArrayList decode(byte[] data) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object obj = ois.readObject();
      if (!(obj instanceof ArrayList<?> list)) {
        throw new IllegalArgumentException(
            "Decoded dialog attribute was not an ArrayList: "
                + (obj == null ? "null" : obj.getClass().getName()));
      }
      return list;
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("Failed to deserialize ArrayList dialog attribute.", e);
    }
  }
}
