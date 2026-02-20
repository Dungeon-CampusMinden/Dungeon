package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.utils.components.showImage.TransitionSpeed;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.DialogShowMessage;
import core.network.proto.common.IntList;
import core.network.proto.common.StringList;
import core.network.proto.s2c.DialogAttribute;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/** Converter for server-to-client dialog show messages. */
public final class DialogShowConverter
    implements MessageConverter<DialogShowMessage, core.network.proto.s2c.DialogShowMessage> {
  private static final byte WIRE_TYPE_ID = 9;

  @Override
  public core.network.proto.s2c.DialogShowMessage toProto(DialogShowMessage message) {
    return toProto(message.context(), message.canBeClosed());
  }

  @Override
  public DialogShowMessage fromProto(core.network.proto.s2c.DialogShowMessage proto) {
    return new DialogShowMessage(fromDialogProto(proto), proto.getCanBeClosed());
  }

  @Override
  public Class<DialogShowMessage> domainType() {
    return DialogShowMessage.class;
  }

  @Override
  public Class<core.network.proto.s2c.DialogShowMessage> protoType() {
    return core.network.proto.s2c.DialogShowMessage.class;
  }

  @Override
  public Parser<core.network.proto.s2c.DialogShowMessage> parser() {
    return core.network.proto.s2c.DialogShowMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static core.network.proto.s2c.DialogShowMessage toProto(
      DialogContext context, boolean canBeClosed) {
    Objects.requireNonNull(context, "context");
    core.network.proto.s2c.DialogShowMessage.Builder builder =
        core.network.proto.s2c.DialogShowMessage.newBuilder()
            .setDialogId(context.dialogId())
            .setDialogType(context.dialogType().type())
            .setCenter(context.center())
            .setCanBeClosed(canBeClosed);

    for (Map.Entry<String, Serializable> entry : context.attributes().entrySet()) {
      DialogAttribute attribute = toAttribute(entry.getKey(), entry.getValue());
      if (attribute != null) {
        builder.addAttributes(attribute);
      }
    }

    return builder.build();
  }

  private static DialogContext fromDialogProto(core.network.proto.s2c.DialogShowMessage proto) {
    Objects.requireNonNull(proto, "proto");
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(resolveDialogType(proto.getDialogType()))
            .center(proto.getCenter());

    if (!proto.getDialogId().isEmpty()) {
      builder.dialogId(proto.getDialogId());
    }

    for (DialogAttribute attribute : proto.getAttributesList()) {
      Serializable value = fromAttribute(attribute.getKey(), attribute);
      if (value != null) {
        builder.put(attribute.getKey(), value);
      }
    }

    return builder.build();
  }

  private static DialogAttribute toAttribute(String key, Serializable value) {
    if (value == null) {
      return null;
    }
    DialogAttribute.Builder builder = DialogAttribute.newBuilder().setKey(key);

    switch (value) {
      case String stringValue -> builder.setStringValue(stringValue);
      case Integer intValue -> builder.setIntValue(intValue);
      case Long longValue -> builder.setLongValue(longValue);
      case Float floatValue -> builder.setFloatValue(floatValue);
      case Double doubleValue -> builder.setDoubleValue(doubleValue);
      case Boolean boolValue -> builder.setBoolValue(boolValue);
      case String[] stringArray ->
          builder.setStringList(StringList.newBuilder().addAllValues(Arrays.asList(stringArray)));
      case int[] intArray -> {
        IntList.Builder intList = IntList.newBuilder();
        for (int item : intArray) {
          intList.addValues(item);
        }
        builder.setIntList(intList);
      }
      case TransitionSpeed speed when DialogContextKeys.IMAGE_TRANSITION_SPEED.equals(key) ->
          builder.setStringValue(speed.name());
      default ->
          throw new IllegalArgumentException(
              "Unsupported dialog attribute type for key '"
                  + key
                  + "': "
                  + value.getClass().getName());
    }

    return builder.build();
  }

  private static Serializable fromAttribute(String key, DialogAttribute attribute) {
    return switch (attribute.getValueCase()) {
      case STRING_VALUE -> transitionSpeedOrString(key, attribute.getStringValue());
      case INT_VALUE -> attribute.getIntValue();
      case LONG_VALUE -> attribute.getLongValue();
      case FLOAT_VALUE -> attribute.getFloatValue();
      case DOUBLE_VALUE -> attribute.getDoubleValue();
      case BOOL_VALUE -> attribute.getBoolValue();
      case STRING_LIST -> attribute.getStringList().getValuesList().toArray(new String[0]);
      case INT_LIST -> attribute.getIntList().getValuesList().stream().mapToInt(i -> i).toArray();
      case VALUE_NOT_SET -> null;
    };
  }

  private static Serializable transitionSpeedOrString(String key, String value) {
    if (!DialogContextKeys.IMAGE_TRANSITION_SPEED.equals(key)) {
      return value;
    }
    try {
      return TransitionSpeed.valueOf(value);
    } catch (IllegalArgumentException ignored) {
      return value;
    }
  }

  private static DialogType resolveDialogType(String typeString) {
    for (DialogType.DefaultTypes type : DialogType.DefaultTypes.values()) {
      if (type.type().equals(typeString)) {
        return type;
      }
    }
    return new DialogTypeWrapper(typeString);
  }

  private record DialogTypeWrapper(String type) implements DialogType {

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof DialogType other)) {
        return false;
      }
      return Objects.equals(type, other.type());
    }
  }
}
