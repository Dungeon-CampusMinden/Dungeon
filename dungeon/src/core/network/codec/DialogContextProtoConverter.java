package core.network.codec;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.utils.components.showImage.TransitionSpeed;
import core.network.proto.s2c.DialogAttribute;
import core.network.proto.s2c.DialogShowMessage;
import core.network.proto.s2c.IntList;
import core.network.proto.s2c.StringList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Converts between {@link DialogContext} and {@link DialogShowMessage}.
 *
 * <p>All dialog-specific data is encoded as typed attributes. Only primitives, strings, and arrays
 * are supported.
 */
public final class DialogContextProtoConverter {

  private DialogContextProtoConverter() {}

  /**
   * Converts a dialog context to a protobuf show message.
   *
   * @param context the dialog context to convert
   * @param canBeClosed whether the dialog can be closed without selection
   * @return the protobuf dialog show message
   */
  public static DialogShowMessage toProto(DialogContext context, boolean canBeClosed) {
    Objects.requireNonNull(context, "context");
    DialogShowMessage.Builder builder =
        DialogShowMessage.newBuilder()
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

  /**
   * Converts a protobuf dialog show message to a dialog context.
   *
   * @param proto the protobuf dialog show message
   * @return the dialog context
   */
  public static DialogContext fromProto(DialogShowMessage proto) {
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
