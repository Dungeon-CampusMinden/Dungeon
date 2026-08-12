package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.SnapshotAck;

/** Converter for client-to-server snapshot acknowledgements. */
public final class SnapshotAckConverter
    implements MessageConverter<SnapshotAck, engine.network.proto.c2s.SnapshotAck> {
  private static final byte WIRE_TYPE_ID = 22;

  @Override
  public engine.network.proto.c2s.SnapshotAck toProto(SnapshotAck message) {
    engine.network.proto.c2s.SnapshotAck.Builder builder =
        engine.network.proto.c2s.SnapshotAck.newBuilder().setServerTick(message.serverTick());
    if (message.resyncRequested()) {
      builder
          .setResyncRequested(true)
          .setMissingBaseTick(message.missingBaseTick())
          .setDeltaTick(message.deltaTick());
    }
    return builder.build();
  }

  @Override
  public SnapshotAck fromProto(engine.network.proto.c2s.SnapshotAck proto) {
    return new SnapshotAck(
        proto.getServerTick(),
        proto.getResyncRequested(),
        proto.getMissingBaseTick(),
        proto.getDeltaTick());
  }

  @Override
  public Class<SnapshotAck> domainType() {
    return SnapshotAck.class;
  }

  @Override
  public Class<engine.network.proto.c2s.SnapshotAck> protoType() {
    return engine.network.proto.c2s.SnapshotAck.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.SnapshotAck> parser() {
    return engine.network.proto.c2s.SnapshotAck.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
