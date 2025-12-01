package contrib.hud.dialogs;

/** Runtime exception thrown when a dialog cannot be constructed via {@link DialogFactory}. */
class DialogCreationException extends RuntimeException {

  DialogCreationException(String message) {
    super(message);
  }

  DialogCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
