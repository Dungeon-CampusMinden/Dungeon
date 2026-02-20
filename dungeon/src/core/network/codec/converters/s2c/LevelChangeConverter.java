package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.LevelChangeEvent;

/** Converter for server-to-client level-change event messages. */
public final class LevelChangeConverter
    implements MessageConverter<LevelChangeEvent, core.network.proto.s2c.LevelChangeEvent> {
  private static final byte WIRE_TYPE_ID = 16;

  @Override
  public core.network.proto.s2c.LevelChangeEvent toProto(LevelChangeEvent message) {
    return core.network.proto.s2c.LevelChangeEvent.newBuilder()
        .setLevelName(message.levelName())
        .setLevelData(message.levelData())
        .build();
  }

  @Override
  public LevelChangeEvent fromProto(core.network.proto.s2c.LevelChangeEvent proto) {
    return new LevelChangeEvent(proto.getLevelName(), proto.getLevelData());
  }

  @Override
  public Class<LevelChangeEvent> domainType() {
    return LevelChangeEvent.class;
  }

  @Override
  public Class<core.network.proto.s2c.LevelChangeEvent> protoType() {
    return core.network.proto.s2c.LevelChangeEvent.class;
  }

  @Override
  public Parser<core.network.proto.s2c.LevelChangeEvent> parser() {
    return core.network.proto.s2c.LevelChangeEvent.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
