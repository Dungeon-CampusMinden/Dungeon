package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.DialogCloseMessage;

/** Converter for server-to-client dialog close messages. */
public final class DialogCloseConverter
    implements MessageConverter<DialogCloseMessage, engine.network.proto.s2c.DialogCloseMessage> {
  private static final byte WIRE_TYPE_ID = 10;

  @Override
  public engine.network.proto.s2c.DialogCloseMessage toProto(DialogCloseMessage message) {
    return engine.network.proto.s2c.DialogCloseMessage.newBuilder()
        .setDialogId(message.dialogId())
        .build();
  }

  @Override
  public DialogCloseMessage fromProto(engine.network.proto.s2c.DialogCloseMessage proto) {
    return new DialogCloseMessage(proto.getDialogId());
  }

  @Override
  public Class<DialogCloseMessage> domainType() {
    return DialogCloseMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.DialogCloseMessage> protoType() {
    return engine.network.proto.s2c.DialogCloseMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.DialogCloseMessage> parser() {
    return engine.network.proto.s2c.DialogCloseMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
