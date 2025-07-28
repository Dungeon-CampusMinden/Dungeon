package core.network;

/** Exception thrown by the NetworkInterface for network-related errors. */
public class NetworkException extends Exception {
  public NetworkException(String message) {
    super(message);
  }

  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }
}
