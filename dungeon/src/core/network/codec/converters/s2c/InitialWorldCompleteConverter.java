package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.InitialWorldComplete;

/** Converter for server-to-client initial-world-complete messages. */
public final class InitialWorldCompleteConverter
    implements MessageConverter<InitialWorldComplete, core.network.proto.s2c.InitialWorldComplete> {
  private static final byte WIRE_TYPE_ID = 28;

  @Override
  public core.network.proto.s2c.InitialWorldComplete toProto(InitialWorldComplete message) {
    return core.network.proto.s2c.InitialWorldComplete.newBuilder().build();
  }

  @Override
  public InitialWorldComplete fromProto(core.network.proto.s2c.InitialWorldComplete proto) {
    return new InitialWorldComplete();
  }

  @Override
  public Class<InitialWorldComplete> domainType() {
    return InitialWorldComplete.class;
  }

  @Override
  public Class<core.network.proto.s2c.InitialWorldComplete> protoType() {
    return core.network.proto.s2c.InitialWorldComplete.class;
  }

  @Override
  public Parser<core.network.proto.s2c.InitialWorldComplete> parser() {
    return core.network.proto.s2c.InitialWorldComplete.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
