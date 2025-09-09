package core.network.codec;

import io.netty.buffer.ByteBuf;
import java.io.*;

public final class NetworkCodec {
  private NetworkCodec() {}

  public static byte[] serialize(Object obj) throws IOException {
    if (!(obj instanceof Serializable)) {
      throw new NotSerializableException(
        "Object not serializable: " + obj);
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    return bos.toByteArray();
  }

  public static Object deserialize(ByteBuf buf)
    throws IOException, ClassNotFoundException {
    int len = buf.readableBytes();
    byte[] array = new byte[len];
    buf.getBytes(buf.readerIndex(), array);
    try (ObjectInputStream ois =
           new ObjectInputStream(new ByteArrayInputStream(array))) {
      return ois.readObject();
    }
  }
}
