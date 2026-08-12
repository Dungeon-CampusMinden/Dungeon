package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.SoundStopMessage;

/** Converter for server-to-client sound stop messages. */
public final class SoundStopConverter
    implements MessageConverter<SoundStopMessage, engine.network.proto.s2c.SoundStopMessage> {
  private static final byte WIRE_TYPE_ID = 20;

  @Override
  public engine.network.proto.s2c.SoundStopMessage toProto(SoundStopMessage message) {
    return engine.network.proto.s2c.SoundStopMessage.newBuilder()
        .setSoundInstanceId(message.soundInstanceId())
        .build();
  }

  @Override
  public SoundStopMessage fromProto(engine.network.proto.s2c.SoundStopMessage proto) {
    return new SoundStopMessage(proto.getSoundInstanceId());
  }

  @Override
  public Class<SoundStopMessage> domainType() {
    return SoundStopMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.SoundStopMessage> protoType() {
    return engine.network.proto.s2c.SoundStopMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.SoundStopMessage> parser() {
    return engine.network.proto.s2c.SoundStopMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
