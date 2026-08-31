package feature.canvas;

import engine.network.codec.DialogValueCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Transports a {@link CanvasSnapshot} through the dialog attribute channel.
 *
 * <p>This is what lets the server hand its current default node set to the client when a canvas
 * dialog is opened. The codec is registered as a built-in in {@link
 * engine.network.codec.DialogValueCodecRegistry}, so it is available on both sides before any
 * dialog message is decoded.
 */
public final class CanvasSnapshotCodec implements DialogValueCodec<CanvasSnapshot> {

  /** Stable wire discriminator; changing it breaks compatibility with older clients. */
  public static final String TYPE_ID = "canvas.snapshot";

  @Override
  public String typeId() {
    return TYPE_ID;
  }

  @Override
  public Class<CanvasSnapshot> type() {
    return CanvasSnapshot.class;
  }

  @Override
  public byte[] encode(CanvasSnapshot value) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(value);
      out.flush();
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Could not encode CanvasSnapshot", e);
    }
  }

  @Override
  public CanvasSnapshot decode(byte[] data) {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
      return (CanvasSnapshot) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Could not decode CanvasSnapshot", e);
    }
  }
}
