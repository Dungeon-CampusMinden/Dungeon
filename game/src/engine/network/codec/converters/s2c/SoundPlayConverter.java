package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.SoundPlayMessage;

/** Converter for server-to-client sound play messages. */
public final class SoundPlayConverter
    implements MessageConverter<SoundPlayMessage, engine.network.proto.s2c.SoundPlayMessage> {
  private static final byte WIRE_TYPE_ID = 19;

  @Override
  public engine.network.proto.s2c.SoundPlayMessage toProto(SoundPlayMessage message) {
    return engine.network.proto.s2c.SoundPlayMessage.newBuilder()
        .setEntityId(message.entityId())
        .setSpec(CommonProtoConverters.toProto(message.soundSpec()))
        .build();
  }

  @Override
  public SoundPlayMessage fromProto(engine.network.proto.s2c.SoundPlayMessage proto) {
    return new SoundPlayMessage(
        proto.getEntityId(), CommonProtoConverters.fromProto(proto.getSpec()));
  }

  @Override
  public Class<SoundPlayMessage> domainType() {
    return SoundPlayMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.SoundPlayMessage> protoType() {
    return engine.network.proto.s2c.SoundPlayMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.SoundPlayMessage> parser() {
    return engine.network.proto.s2c.SoundPlayMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
