package core.network.messages.c2s;

import core.network.messages.NetworkMessage;
import java.util.Objects;

/**
 * Client-to-server message reporting a user's interaction with a dialog.
 *
 * <p>Sent by network callbacks created by {@link contrib.hud.dialogs.DialogCallbackResolver} when
 * the user interacts with a dialog (clicks a button, submits input, or closes the dialog).
 *
 * <p>The server validates this message against {@link core.network.server.DialogTracker} to ensure:
 *
 * <ul>
 *   <li>The client was authorized to see this dialog
 *   <li>For shared dialogs, only the first responder's callback is executed
 * </ul>
 *
 * @param dialogId the unique identifier of the dialog being responded to
 * @param callbackKey the callback key to execute (e.g., "onConfirm", "craft"), null for CLOSED
 * @param payload optional custom payload for the callback, may be null
 * @see core.network.messages.s2c.DialogShowMessage
 * @see core.network.server.DialogTracker
 */
public record DialogResponseMessage(String dialogId, String callbackKey, Payload payload)
    implements NetworkMessage {

  /**
   * Creates a new dialog response message.
   *
   * @param dialogId the dialog identifier
   * @param callbackKey the callback key to execute, or null for CLOSED
   * @param payload the optional payload for the callback
   */
  public DialogResponseMessage {
    Objects.requireNonNull(dialogId, "dialogId");
  }

  /**
   * Returns the payload casted to the requested type.
   *
   * @param type the payload type
   * @param <T> the payload type
   * @return the payload as the requested type
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

  /** Marker interface for dialog response payloads. */
  public sealed interface Payload
      permits StringValue,
          IntValue,
          LongValue,
          FloatValue,
          DoubleValue,
          BoolValue,
          StringList,
          IntList {}

  /** Payload for string values. */
  public record StringValue(String value) implements Payload {
    public StringValue {
      Objects.requireNonNull(value, "value");
    }
  }

  /** Payload for int values. */
  public record IntValue(int value) implements Payload {}

  /** Payload for long values. */
  public record LongValue(long value) implements Payload {}

  /** Payload for float values. */
  public record FloatValue(float value) implements Payload {}

  /** Payload for double values. */
  public record DoubleValue(double value) implements Payload {}

  /** Payload for boolean values. */
  public record BoolValue(boolean value) implements Payload {}

  /** Payload for string list values. */
  public record StringList(String[] values) implements Payload {
    public StringList {
      Objects.requireNonNull(values, "values");
    }
  }

  /** Payload for int list values. */
  public record IntList(int[] values) implements Payload {
    public IntList {
      Objects.requireNonNull(values, "values");
    }
  }
}
