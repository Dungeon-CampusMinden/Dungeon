package core.network.messages;

import core.utils.Point;
import java.io.Serial;

/**
 * Client→server: compact input action message (UDP-friendly).
 *
 * <p>Expected max size: tiny (<= 64 bytes per message).
 *
 * @param clientId the client id (assigned by server; client stamps after ConnectAck)
 * @param action the action (e.g., move, move path, cast skill, interact)
 * @param point the point (e.g., move path, cast skill, interact)
 */
public record InputMessage(int clientId, Action action, Point point)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  public InputMessage(Action action, Point point) {
    this(0, action, point);
  }

  public enum Action {
    MOVE(0),
    MOVE_PATH(1),
    CAST_SKILL(2),
    INTERACT(3),
    ;

    private final byte value;

    Action(int value) {
      this.value = (byte) value;
    }

    public byte value() {
      return value;
    }
  }
}
