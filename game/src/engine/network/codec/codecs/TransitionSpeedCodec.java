package engine.network.codec.codecs;

import engine.network.codec.DialogValueCodec;
import feature.presentation.TransitionSpeed;
import java.nio.charset.StandardCharsets;

/** Built-in codec for {@link TransitionSpeed}. */
public final class TransitionSpeedCodec implements DialogValueCodec<TransitionSpeed> {

  @Override
  public String typeId() {
    return "TransitionSpeed";
  }

  @Override
  public Class<TransitionSpeed> type() {
    return TransitionSpeed.class;
  }

  @Override
  public byte[] encode(TransitionSpeed value) {
    return value.name().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public TransitionSpeed decode(byte[] data) {
    return TransitionSpeed.valueOf(new String(data, StandardCharsets.UTF_8));
  }
}
