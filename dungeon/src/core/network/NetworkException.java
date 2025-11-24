package core.network;

/** Exception thrown by the NetworkInterface for network-related errors. */
public class NetworkException extends Exception {

  /**
   * Constructor for NetworkException.
   *
   * @param message The error message.
   */
  public NetworkException(String message) {
    super(message);
  }

  /**
   * Constructor for NetworkException with a cause.
   *
   * @param message The error message.
   * @param cause The underlying cause of the exception.
   */
  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }
}
