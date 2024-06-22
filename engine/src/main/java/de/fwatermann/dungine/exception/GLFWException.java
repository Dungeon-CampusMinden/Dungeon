package de.fwatermann.dungine.exception;

/**
 * Represents a GLFW exception.
 * This class extends the RuntimeException class and is used to signal that an error has occurred in the GLFW library.
 */
public class GLFWException extends RuntimeException {

    /**
     * Constructs a new GLFWException with the specified detail message.
     *
     * @param message the detail message
     */
    public GLFWException(String message) {
        super(message);
    }

    /**
     * Constructs a new GLFWException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public GLFWException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new GLFWException with no detail message.
     */
    public GLFWException() {
        super();
    }
}
