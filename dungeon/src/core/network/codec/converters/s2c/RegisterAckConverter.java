package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.RegisterAck;

/** Converter for server-to-client UDP registration ack messages. */
public final class RegisterAckConverter
    implements MessageConverter<RegisterAck, core.network.proto.s2c.RegisterAck> {
  private static final byte WIRE_TYPE_ID = 17;

  @Override
  public core.network.proto.s2c.RegisterAck toProto(RegisterAck message) {
    return core.network.proto.s2c.RegisterAck.newBuilder().setOk(message.ok()).build();
  }

  @Override
  public RegisterAck fromProto(core.network.proto.s2c.RegisterAck proto) {
    return new RegisterAck(proto.getOk());
  }

  @Override
  public Class<RegisterAck> domainType() {
    return RegisterAck.class;
  }

  @Override
  public Class<core.network.proto.s2c.RegisterAck> protoType() {
    return core.network.proto.s2c.RegisterAck.class;
  }

  @Override
  public Parser<core.network.proto.s2c.RegisterAck> parser() {
    return core.network.proto.s2c.RegisterAck.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
