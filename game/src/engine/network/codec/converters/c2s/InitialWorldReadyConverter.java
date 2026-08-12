package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.InitialWorldReady;

/** Converter for client-to-server initial-world-ready messages. */
public final class InitialWorldReadyConverter
    implements MessageConverter<InitialWorldReady, engine.network.proto.c2s.InitialWorldReady> {
  private static final byte WIRE_TYPE_ID = 27;

  @Override
  public engine.network.proto.c2s.InitialWorldReady toProto(InitialWorldReady message) {
    return engine.network.proto.c2s.InitialWorldReady.newBuilder().build();
  }

  @Override
  public InitialWorldReady fromProto(engine.network.proto.c2s.InitialWorldReady proto) {
    return new InitialWorldReady();
  }

  @Override
  public Class<InitialWorldReady> domainType() {
    return InitialWorldReady.class;
  }

  @Override
  public Class<engine.network.proto.c2s.InitialWorldReady> protoType() {
    return engine.network.proto.c2s.InitialWorldReady.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.InitialWorldReady> parser() {
    return engine.network.proto.c2s.InitialWorldReady.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
