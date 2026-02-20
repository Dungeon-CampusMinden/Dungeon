package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: reject connection attempt with reason.
 *
 * <p>Sent by the server when a client attempts to connect but is rejected for some reason (e.g.,
 * server full, invalid player name, incompatible version).
 *
 * @param reason The reason for the connection rejection.
 */
public record ConnectReject(byte reason) implements NetworkMessage {
  /**
   * @param reason The reason for the connection rejection.
   */
  public ConnectReject(Reason reason) {
    this(reason.getCode());
  }

  /** Enumeration of possible connection rejection reasons. */
  public enum Reason {
    /** Invalid player name (e.g., already taken, invalid characters). */
    INVALID_NAME,
    /** Invalid protocol version (client and server versions do not match). */
    INCOMPATIBLE_VERSION,
    /** The given session was not found on the server. */
    NO_SESSION_FOUND,
    /** The given session token was invalid. */
    INVALID_SESSION_TOKEN,
    /** Unspecified connection rejection reason. */
    OTHER;

    /**
     * Get the code corresponding to this Reason.
     *
     * @return the code
     * @throws IllegalStateException if there are too many Reason entries to fit in a byte
     */
    public byte getCode() {
      if (values().length > Byte.MAX_VALUE + 1) {
        throw new IllegalStateException(
            "Too many Reason enum entries for byte encoding: " + values().length);
      }
      return (byte) this.ordinal();
    }

    /**
     * Get Reason from its code.
     *
     * @param code the code
     * @return the corresponding Reason, or OTHER if code is invalid
     */
    public static Reason fromCode(byte code) {
      for (Reason reason : values()) {
        if (reason.ordinal() == code) {
          return reason;
        }
      }
      return OTHER;
    }

    @Override
    public String toString() {
      return switch (this) {
        case INVALID_NAME -> "Invalid Name";
        case INCOMPATIBLE_VERSION -> "Incompatible Version";
        case NO_SESSION_FOUND -> "No Session Found";
        case INVALID_SESSION_TOKEN -> "Invalid Session Token";
        case OTHER -> "Other";
      };
    }
  }
}
