package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.DialogCloseMessage;

/** Converter for server-to-client dialog close messages. */
public final class DialogCloseConverter
    implements MessageConverter<DialogCloseMessage, core.network.proto.s2c.DialogCloseMessage> {
  private static final byte WIRE_TYPE_ID = 10;

  @Override
  public core.network.proto.s2c.DialogCloseMessage toProto(DialogCloseMessage message) {
    return core.network.proto.s2c.DialogCloseMessage.newBuilder()
        .setDialogId(message.dialogId())
        .build();
  }

  @Override
  public DialogCloseMessage fromProto(core.network.proto.s2c.DialogCloseMessage proto) {
    return new DialogCloseMessage(proto.getDialogId());
  }

  @Override
  public Class<DialogCloseMessage> domainType() {
    return DialogCloseMessage.class;
  }

  @Override
  public Class<core.network.proto.s2c.DialogCloseMessage> protoType() {
    return core.network.proto.s2c.DialogCloseMessage.class;
  }

  @Override
  public Parser<core.network.proto.s2c.DialogCloseMessage> parser() {
    return core.network.proto.s2c.DialogCloseMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
