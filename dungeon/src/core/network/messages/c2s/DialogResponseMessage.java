package core.network.messages.c2s;

import core.network.messages.NetworkMessage;
import java.util.Objects;

/**
 * Represents a client-to-server message that reports a user interaction with a dialog.
 *
 * <p>This message is sent by network callbacks created by {@link
 * contrib.hud.dialogs.DialogCallbackResolver} when the user clicks a button, submits input, or
 * closes a dialog.
 *
 * <p>The server validates this message against {@link core.network.server.DialogTracker} to ensure
 * the client is authorized to respond and, for shared dialogs, only the first response triggers a
 * callback.
 *
 * @param dialogId The unique identifier of the dialog being responded to.
 * @param callbackKey The callback key to execute, or {@code null} when the dialog was closed.
 * @param payload The optional callback payload, or {@code null} when no payload is provided.
 * @see core.network.messages.s2c.DialogShowMessage
 * @see core.network.server.DialogTracker
 */
public record DialogResponseMessage(String dialogId, String callbackKey, Payload payload)
    implements NetworkMessage {

  /**
   * Creates a dialog response message with a required dialog identifier.
   *
   * <p>The callback key and payload may be {@code null} depending on the interaction type.
   *
   * @param dialogId The unique identifier of the dialog being responded to.
   * @param callbackKey The callback key to execute, or {@code null} when the dialog was closed.
   * @param payload The optional callback payload, or {@code null} when no payload is provided.
   * @throws NullPointerException If {@code dialogId} is {@code null}.
   */
  public DialogResponseMessage {
    Objects.requireNonNull(dialogId, "dialogId");
  }

  /**
   * Returns the payload cast to the requested payload type.
   *
   * <p>This method fails fast when no payload is present or when the payload type does not match
   * the requested type.
   *
   * @param type The expected payload class.
   * @param <T> The expected payload subtype.
   * @return The payload as the requested type.
   * @throws NullPointerException If {@code type} is {@code null}.
   * @throws IllegalArgumentException If {@code payload} is {@code null} or not assignable to {@code
   *     type}.
   */
  public <T extends Payload> T payloadAs(Class<T> type) {
    Objects.requireNonNull(type, "type");
    if (payload == null) {
      throw new IllegalArgumentException("DialogResponseMessage payload is null");
    }
    if (!type.isInstance(payload)) {
      throw new IllegalArgumentException(
          "Expected payload of type "
              + type.getSimpleName()
              + " but got "
              + payload.getClass().getSimpleName());
    }
    return type.cast(payload);
  }

  /**
   * Marks all supported dialog response payload types.
   *
   * <p>Implementations represent concrete values transferred with a dialog response callback.
   *
   * @see DialogResponseMessage
   */
  public sealed interface Payload
      permits StringValue,
          IntValue,
          LongValue,
          FloatValue,
          DoubleValue,
          BoolValue,
          StringList,
          IntList {}

  /**
   * Represents a string payload for dialog responses.
   *
   * <p>Use this payload when callbacks require textual input from a client.
   *
   * @param value The string value sent as payload.
   */
  public record StringValue(String value) implements Payload {
    /**
     * Creates a string payload with a required value.
     *
     * <p>The value must be non-null.
     *
     * @param value The string value sent as payload.
     * @throws NullPointerException If {@code value} is {@code null}.
     */
    public StringValue {
      Objects.requireNonNull(value, "value");
    }
  }

  /**
   * Represents an integer payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require whole-number input.
   *
   * @param value The integer value sent as payload.
   */
  public record IntValue(int value) implements Payload {}

  /**
   * Represents a long payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require large whole-number input.
   *
   * @param value The long value sent as payload.
   */
  public record LongValue(long value) implements Payload {}

  /**
   * Represents a float payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require single-precision decimal input.
   *
   * @param value The float value sent as payload.
   */
  public record FloatValue(float value) implements Payload {}

  /**
   * Represents a double payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require double-precision decimal input.
   *
   * @param value The double value sent as payload.
   */
  public record DoubleValue(double value) implements Payload {}

  /**
   * Represents a boolean payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require true or false decisions.
   *
   * @param value The boolean value sent as payload.
   */
  public record BoolValue(boolean value) implements Payload {}

  /**
   * Represents a string-array payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require multiple textual values.
   *
   * @param values The string array sent as payload.
   */
  public record StringList(String[] values) implements Payload {
    /**
     * Creates a string-array payload with required values.
     *
     * <p>The array reference must be non-null.
     *
     * @param values The string array sent as payload.
     * @throws NullPointerException If {@code values} is {@code null}.
     */
    public StringList {
      Objects.requireNonNull(values, "values");
    }
  }

  /**
   * Represents an int-array payload for dialog responses.
   *
   * <p>Use this payload for callbacks that require multiple whole-number values.
   *
   * @param values The int array sent as payload.
   */
  public record IntList(int[] values) implements Payload {
    /**
     * Creates an int-array payload with required values.
     *
     * <p>The array reference must be non-null.
     *
     * @param values The int array sent as payload.
     * @throws NullPointerException If {@code values} is {@code null}.
     */
    public IntList {
      Objects.requireNonNull(values, "values");
    }
  }
}
