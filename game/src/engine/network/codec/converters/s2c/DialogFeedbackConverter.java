package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.DialogFeedbackMessage;

/** Converter for server-to-client live dialog feedback messages. */
public final class DialogFeedbackConverter
    implements MessageConverter<
        DialogFeedbackMessage, engine.network.proto.s2c.DialogFeedbackMessage> {
  private static final byte WIRE_TYPE_ID = 30;

  @Override
  public engine.network.proto.s2c.DialogFeedbackMessage toProto(DialogFeedbackMessage message) {
    return engine.network.proto.s2c.DialogFeedbackMessage.newBuilder()
        .setDialogId(message.dialogId())
        .setMessageKey(message.messageKey())
        .setSuccessful(message.successful())
        .setTargetTabKey(message.targetTabKey())
        .setSourceFingerprint(message.sourceFingerprint())
        .build();
  }

  @Override
  public DialogFeedbackMessage fromProto(engine.network.proto.s2c.DialogFeedbackMessage proto) {
    return new DialogFeedbackMessage(
        proto.getDialogId(),
        proto.getTargetTabKey(),
        proto.getSourceFingerprint(),
        proto.getMessageKey(),
        proto.getSuccessful());
  }

  @Override
  public Class<DialogFeedbackMessage> domainType() {
    return DialogFeedbackMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.DialogFeedbackMessage> protoType() {
    return engine.network.proto.s2c.DialogFeedbackMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.DialogFeedbackMessage> parser() {
    return engine.network.proto.s2c.DialogFeedbackMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
