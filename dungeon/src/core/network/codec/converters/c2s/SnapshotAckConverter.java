package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.SnapshotAck;

/** Converter for client-to-server snapshot acknowledgements. */
public final class SnapshotAckConverter
    implements MessageConverter<SnapshotAck, core.network.proto.c2s.SnapshotAck> {
  private static final byte WIRE_TYPE_ID = 22;

  @Override
  public core.network.proto.c2s.SnapshotAck toProto(SnapshotAck message) {
    return core.network.proto.c2s.SnapshotAck.newBuilder()
        .setServerTick(message.serverTick())
        .build();
  }

  @Override
  public SnapshotAck fromProto(core.network.proto.c2s.SnapshotAck proto) {
    return new SnapshotAck(proto.getServerTick());
  }

  @Override
  public Class<SnapshotAck> domainType() {
    return SnapshotAck.class;
  }

  @Override
  public Class<core.network.proto.c2s.SnapshotAck> protoType() {
    return core.network.proto.c2s.SnapshotAck.class;
  }

  @Override
  public Parser<core.network.proto.c2s.SnapshotAck> parser() {
    return core.network.proto.c2s.SnapshotAck.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
