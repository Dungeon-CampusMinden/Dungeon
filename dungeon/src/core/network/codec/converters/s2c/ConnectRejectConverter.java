package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.ConnectReject;

/** Converter for server-to-client connection rejection messages. */
public final class ConnectRejectConverter
    implements MessageConverter<ConnectReject, core.network.proto.s2c.ConnectReject> {
  private static final byte WIRE_TYPE_ID = 8;

  @Override
  public core.network.proto.s2c.ConnectReject toProto(ConnectReject message) {
    ConnectReject.Reason reason = ConnectReject.Reason.fromCode(message.reason());
    return core.network.proto.s2c.ConnectReject.newBuilder().setReason(toProto(reason)).build();
  }

  @Override
  public ConnectReject fromProto(core.network.proto.s2c.ConnectReject proto) {
    return new ConnectReject(fromProto(proto.getReason()));
  }

  @Override
  public Class<ConnectReject> domainType() {
    return ConnectReject.class;
  }

  @Override
  public Class<core.network.proto.s2c.ConnectReject> protoType() {
    return core.network.proto.s2c.ConnectReject.class;
  }

  @Override
  public Parser<core.network.proto.s2c.ConnectReject> parser() {
    return core.network.proto.s2c.ConnectReject.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static core.network.proto.s2c.ConnectReject.RejectReason toProto(
      ConnectReject.Reason reason) {
    return switch (reason) {
      case INVALID_NAME ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INVALID_NAME;
      case INCOMPATIBLE_VERSION ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INCOMPATIBLE_VERSION;
      case NO_SESSION_FOUND ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_NO_SESSION_FOUND;
      case INVALID_SESSION_TOKEN ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INVALID_SESSION_TOKEN;
      case OTHER -> core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_OTHER;
    };
  }

  private static ConnectReject.Reason fromProto(
      core.network.proto.s2c.ConnectReject.RejectReason reason) {
    return switch (reason) {
      case REJECT_REASON_INVALID_NAME -> ConnectReject.Reason.INVALID_NAME;
      case REJECT_REASON_INCOMPATIBLE_VERSION -> ConnectReject.Reason.INCOMPATIBLE_VERSION;
      case REJECT_REASON_NO_SESSION_FOUND -> ConnectReject.Reason.NO_SESSION_FOUND;
      case REJECT_REASON_INVALID_SESSION_TOKEN -> ConnectReject.Reason.INVALID_SESSION_TOKEN;
      case REJECT_REASON_OTHER, REJECT_REASON_UNSPECIFIED, UNRECOGNIZED ->
          ConnectReject.Reason.OTHER;
    };
  }
}
