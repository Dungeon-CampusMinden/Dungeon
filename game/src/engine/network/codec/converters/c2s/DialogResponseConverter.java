package engine.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import engine.network.codec.DialogValueCodec;
import engine.network.codec.DialogValueCodecRegistry;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.proto.common.CustomValue;
import engine.network.proto.common.IntList;
import engine.network.proto.common.StringList;
import java.io.Serializable;
import java.util.Arrays;

/** Converter for client-to-server dialog response messages. */
public final class DialogResponseConverter
    implements MessageConverter<
        DialogResponseMessage, engine.network.proto.c2s.DialogResponseMessage> {
  private static final String DIALOG_CLOSED_KEY = "CLOSED";
  private static final byte WIRE_TYPE_ID = 3;

  @Override
  public engine.network.proto.c2s.DialogResponseMessage toProto(DialogResponseMessage message) {
    String callbackKey = message.callbackKey();
    if (callbackKey == null) {
      callbackKey = DIALOG_CLOSED_KEY;
    }
    engine.network.proto.c2s.DialogResponseMessage.Builder builder =
        engine.network.proto.c2s.DialogResponseMessage.newBuilder()
            .setDialogId(message.dialogId())
            .setCallbackKey(callbackKey);
    DialogResponseMessage.Payload payload = message.payload();
    if (payload != null) {
      setDialogPayload(builder, payload);
    }
    return builder.build();
  }

  @Override
  public DialogResponseMessage fromProto(engine.network.proto.c2s.DialogResponseMessage proto) {
    String callbackKey = proto.getCallbackKey();
    if (DIALOG_CLOSED_KEY.equals(callbackKey)) {
      callbackKey = null;
    }
    DialogResponseMessage.Payload payload = parseDialogPayload(proto);
    return new DialogResponseMessage(proto.getDialogId(), callbackKey, payload);
  }

  @Override
  public Class<DialogResponseMessage> domainType() {
    return DialogResponseMessage.class;
  }

  @Override
  public Class<engine.network.proto.c2s.DialogResponseMessage> protoType() {
    return engine.network.proto.c2s.DialogResponseMessage.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.DialogResponseMessage> parser() {
    return engine.network.proto.c2s.DialogResponseMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static void setDialogPayload(
      engine.network.proto.c2s.DialogResponseMessage.Builder builder,
      DialogResponseMessage.Payload payload) {
    switch (payload) {
      case DialogResponseMessage.StringValue(String value5) -> builder.setStringValue(value5);
      case DialogResponseMessage.IntValue(int value4) -> builder.setIntValue(value4);
      case DialogResponseMessage.LongValue(long value3) -> builder.setLongValue(value3);
      case DialogResponseMessage.FloatValue(float value2) -> builder.setFloatValue(value2);
      case DialogResponseMessage.DoubleValue(double value1) -> builder.setDoubleValue(value1);
      case DialogResponseMessage.BoolValue(boolean value1) -> builder.setBoolValue(value1);
      case DialogResponseMessage.StringList(String[] stringArray) ->
          builder.setStringList(StringList.newBuilder().addAllValues(Arrays.asList(stringArray)));
      case DialogResponseMessage.IntList(int[] intArray) -> {
        IntList.Builder listBuilder = IntList.newBuilder();
        for (int value : intArray) {
          listBuilder.addValues(value);
        }
        builder.setIntList(listBuilder);
      }
      default -> {
        if (payload instanceof Serializable serializablePayload) {
          @SuppressWarnings("unchecked")
          DialogValueCodec<Serializable> codec =
              (DialogValueCodec<Serializable>)
                  DialogValueCodecRegistry.global()
                      .byType(payload.getClass())
                      .orElseThrow(
                          () ->
                              new IllegalArgumentException(
                                  "Unsupported dialog response payload type: "
                                      + payload.getClass().getName()
                                      + ". Register a DialogValueCodec for this type."));
          builder.setCustomValue(
              CustomValue.newBuilder()
                  .setTypeId(codec.typeId())
                  .setData(ByteString.copyFrom(codec.encode(serializablePayload))));
        } else {
          throw new IllegalArgumentException(
              "Unsupported dialog response payload type: "
                  + payload.getClass().getName()
                  + ". Register a DialogValueCodec for this type.");
        }
      }
    }
  }

  private static DialogResponseMessage.Payload parseDialogPayload(
      engine.network.proto.c2s.DialogResponseMessage proto) {
    return switch (proto.getPayloadCase()) {
      case STRING_VALUE -> new DialogResponseMessage.StringValue(proto.getStringValue());
      case INT_VALUE -> new DialogResponseMessage.IntValue(proto.getIntValue());
      case LONG_VALUE -> new DialogResponseMessage.LongValue(proto.getLongValue());
      case FLOAT_VALUE -> new DialogResponseMessage.FloatValue(proto.getFloatValue());
      case DOUBLE_VALUE -> new DialogResponseMessage.DoubleValue(proto.getDoubleValue());
      case BOOL_VALUE -> new DialogResponseMessage.BoolValue(proto.getBoolValue());
      case STRING_LIST ->
          new DialogResponseMessage.StringList(
              proto.getStringList().getValuesList().toArray(new String[0]));
      case INT_LIST ->
          new DialogResponseMessage.IntList(
              proto.getIntList().getValuesList().stream().mapToInt(i -> i).toArray());
      case CUSTOM_VALUE -> {
        CustomValue custom = proto.getCustomValue();
        DialogValueCodec<?> codec =
            DialogValueCodecRegistry.global()
                .byTypeId(custom.getTypeId())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "No DialogValueCodec registered for typeId '"
                                + custom.getTypeId()
                                + "'"));
        Serializable decoded = codec.decode(custom.getData().toByteArray());
        if (decoded instanceof DialogResponseMessage.Payload payload) {
          yield payload;
        }
        yield new DialogResponseMessage.CustomPayload(decoded);
      }
      case PAYLOAD_NOT_SET -> null;
    };
  }
}
