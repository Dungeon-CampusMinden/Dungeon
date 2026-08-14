package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.InitialWorldComplete;

/** Converter for server-to-client initial-world-complete messages. */
public final class InitialWorldCompleteConverter
    implements MessageConverter<
        InitialWorldComplete, engine.network.proto.s2c.InitialWorldComplete> {
  private static final byte WIRE_TYPE_ID = 28;

  @Override
  public engine.network.proto.s2c.InitialWorldComplete toProto(InitialWorldComplete message) {
    return engine.network.proto.s2c.InitialWorldComplete.newBuilder().build();
  }

  @Override
  public InitialWorldComplete fromProto(engine.network.proto.s2c.InitialWorldComplete proto) {
    return new InitialWorldComplete();
  }

  @Override
  public Class<InitialWorldComplete> domainType() {
    return InitialWorldComplete.class;
  }

  @Override
  public Class<engine.network.proto.s2c.InitialWorldComplete> protoType() {
    return engine.network.proto.s2c.InitialWorldComplete.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.InitialWorldComplete> parser() {
    return engine.network.proto.s2c.InitialWorldComplete.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
