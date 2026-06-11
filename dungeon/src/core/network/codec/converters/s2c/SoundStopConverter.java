package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.SoundStopMessage;

/** Converter for server-to-client sound stop messages. */
public final class SoundStopConverter
    implements MessageConverter<SoundStopMessage, core.network.proto.s2c.SoundStopMessage> {
  private static final byte WIRE_TYPE_ID = 20;

  @Override
  public core.network.proto.s2c.SoundStopMessage toProto(SoundStopMessage message) {
    return core.network.proto.s2c.SoundStopMessage.newBuilder()
        .setSoundInstanceId(message.soundInstanceId())
        .build();
  }

  @Override
  public SoundStopMessage fromProto(core.network.proto.s2c.SoundStopMessage proto) {
    return new SoundStopMessage(proto.getSoundInstanceId());
  }

  @Override
  public Class<SoundStopMessage> domainType() {
    return SoundStopMessage.class;
  }

  @Override
  public Class<core.network.proto.s2c.SoundStopMessage> protoType() {
    return core.network.proto.s2c.SoundStopMessage.class;
  }

  @Override
  public Parser<core.network.proto.s2c.SoundStopMessage> parser() {
    return core.network.proto.s2c.SoundStopMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
