package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.InitialPresentationReady;

/** Converter for {@link InitialPresentationReady} messages. */
public final class InitialPresentationReadyConverter
    implements MessageConverter<
        InitialPresentationReady, engine.network.proto.s2c.InitialPresentationReady> {
  private static final byte WIRE_TYPE_ID = 37;

  @Override
  public engine.network.proto.s2c.InitialPresentationReady toProto(
      InitialPresentationReady message) {
    return engine.network.proto.s2c.InitialPresentationReady.newBuilder().build();
  }

  @Override
  public InitialPresentationReady fromProto(
      engine.network.proto.s2c.InitialPresentationReady proto) {
    return new InitialPresentationReady();
  }

  @Override
  public Class<InitialPresentationReady> domainType() {
    return InitialPresentationReady.class;
  }

  @Override
  public Class<engine.network.proto.s2c.InitialPresentationReady> protoType() {
    return engine.network.proto.s2c.InitialPresentationReady.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.InitialPresentationReady> parser() {
    return engine.network.proto.s2c.InitialPresentationReady.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
