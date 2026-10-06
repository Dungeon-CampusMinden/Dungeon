package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.PauseStateMessage;

/** Converter for PauseStateMessage. */
public final class PauseStateMessageConverter
    implements MessageConverter<PauseStateMessage, engine.network.proto.c2s.PauseStateMessage> {
  @Override
  public engine.network.proto.c2s.PauseStateMessage toProto(PauseStateMessage message) {
    return engine.network.proto.c2s.PauseStateMessage.newBuilder()
        .setPaused(message.paused())
        .build();
  }

  @Override
  public PauseStateMessage fromProto(engine.network.proto.c2s.PauseStateMessage proto) {
    return new PauseStateMessage(proto.getPaused());
  }

  @Override
  public Class<PauseStateMessage> domainType() {
    return PauseStateMessage.class;
  }

  @Override
  public Class<engine.network.proto.c2s.PauseStateMessage> protoType() {
    return engine.network.proto.c2s.PauseStateMessage.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.PauseStateMessage> parser() {
    return engine.network.proto.c2s.PauseStateMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return 33;
  }
}
