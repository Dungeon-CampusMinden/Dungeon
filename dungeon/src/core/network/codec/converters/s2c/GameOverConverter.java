package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.GameOverEvent;

/** Converter for server-to-client game-over event messages. */
public final class GameOverConverter
    implements MessageConverter<GameOverEvent, core.network.proto.s2c.GameOverEvent> {
  private static final byte WIRE_TYPE_ID = 15;

  @Override
  public core.network.proto.s2c.GameOverEvent toProto(GameOverEvent message) {
    return core.network.proto.s2c.GameOverEvent.newBuilder().setReason(message.reason()).build();
  }

  @Override
  public GameOverEvent fromProto(core.network.proto.s2c.GameOverEvent proto) {
    return new GameOverEvent(proto.getReason());
  }

  @Override
  public Class<GameOverEvent> domainType() {
    return GameOverEvent.class;
  }

  @Override
  public Class<core.network.proto.s2c.GameOverEvent> protoType() {
    return core.network.proto.s2c.GameOverEvent.class;
  }

  @Override
  public Parser<core.network.proto.s2c.GameOverEvent> parser() {
    return core.network.proto.s2c.GameOverEvent.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
