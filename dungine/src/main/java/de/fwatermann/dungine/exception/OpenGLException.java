package de.fwatermann.dungine.exception;

/**
 * Represents a OpenGL exception. This class extends the RuntimeException class and is used to
 * signal that an error has occurred in OpenGL.
 */
public class OpenGLException extends RuntimeException {

  /**
   * Constructs a new OpenGLException with the specified detail message.
   *
   * @param message the detail message
   */
  public OpenGLException(String message) {
    super(message);
  }

  /**
   * Constructs a new OpenGLException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public OpenGLException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Constructs a new OpenGLException with no detail message. */
  public OpenGLException() {
    super();
  }
}
