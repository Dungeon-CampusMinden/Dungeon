package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.PlayClockMessage;

/** Converter for PlayClockMessage. */
public final class PlayClockMessageConverter
    implements MessageConverter<PlayClockMessage, engine.network.proto.s2c.PlayClockMessage> {
  @Override
  public engine.network.proto.s2c.PlayClockMessage toProto(PlayClockMessage message) {
    return engine.network.proto.s2c.PlayClockMessage.newBuilder()
        .setActiveMs(message.activeMs())
        .setRunning(message.running())
        .build();
  }

  @Override
  public PlayClockMessage fromProto(engine.network.proto.s2c.PlayClockMessage proto) {
    return new PlayClockMessage(proto.getActiveMs(), proto.getRunning());
  }

  @Override
  public Class<PlayClockMessage> domainType() {
    return PlayClockMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.PlayClockMessage> protoType() {
    return engine.network.proto.s2c.PlayClockMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.PlayClockMessage> parser() {
    return engine.network.proto.s2c.PlayClockMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return 34;
  }
}
