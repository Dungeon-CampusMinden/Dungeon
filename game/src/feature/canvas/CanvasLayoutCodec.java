package feature.canvas;

import engine.network.codec.DialogValueCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Transports a {@link CanvasLayout} through the dialog attribute channel. */
public final class CanvasLayoutCodec implements DialogValueCodec<CanvasLayout> {

  /** Stable wire discriminator. */
  public static final String TYPE_ID = "canvas.layout";

  @Override
  public String typeId() {
    return TYPE_ID;
  }

  @Override
  public Class<CanvasLayout> type() {
    return CanvasLayout.class;
  }

  @Override
  public byte[] encode(CanvasLayout value) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(value);
      out.flush();
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Could not encode CanvasLayout", e);
    }
  }

  @Override
  public CanvasLayout decode(byte[] data) {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
      return (CanvasLayout) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Could not decode CanvasLayout", e);
    }
  }
}
