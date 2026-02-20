package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.SoundFinishedMessage;

/** Converter for client-to-server sound-finished messages. */
public final class SoundFinishedConverter
    implements MessageConverter<SoundFinishedMessage, core.network.proto.c2s.SoundFinishedMessage> {
  private static final byte WIRE_TYPE_ID = 6;

  @Override
  public core.network.proto.c2s.SoundFinishedMessage toProto(SoundFinishedMessage message) {
    return core.network.proto.c2s.SoundFinishedMessage.newBuilder()
        .setSoundInstanceId(message.soundInstanceId())
        .build();
  }

  @Override
  public SoundFinishedMessage fromProto(core.network.proto.c2s.SoundFinishedMessage proto) {
    return new SoundFinishedMessage(proto.getSoundInstanceId());
  }

  @Override
  public Class<SoundFinishedMessage> domainType() {
    return SoundFinishedMessage.class;
  }

  @Override
  public Class<core.network.proto.c2s.SoundFinishedMessage> protoType() {
    return core.network.proto.c2s.SoundFinishedMessage.class;
  }

  @Override
  public Parser<core.network.proto.c2s.SoundFinishedMessage> parser() {
    return core.network.proto.c2s.SoundFinishedMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
