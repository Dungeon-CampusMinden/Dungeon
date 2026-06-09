package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.InitialWorldReady;

/** Converter for client-to-server initial-world-ready messages. */
public final class InitialWorldReadyConverter
    implements MessageConverter<InitialWorldReady, core.network.proto.c2s.InitialWorldReady> {
  private static final byte WIRE_TYPE_ID = 27;

  @Override
  public core.network.proto.c2s.InitialWorldReady toProto(InitialWorldReady message) {
    return core.network.proto.c2s.InitialWorldReady.newBuilder().build();
  }

  @Override
  public InitialWorldReady fromProto(core.network.proto.c2s.InitialWorldReady proto) {
    return new InitialWorldReady();
  }

  @Override
  public Class<InitialWorldReady> domainType() {
    return InitialWorldReady.class;
  }

  @Override
  public Class<core.network.proto.c2s.InitialWorldReady> protoType() {
    return core.network.proto.c2s.InitialWorldReady.class;
  }

  @Override
  public Parser<core.network.proto.c2s.InitialWorldReady> parser() {
    return core.network.proto.c2s.InitialWorldReady.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
