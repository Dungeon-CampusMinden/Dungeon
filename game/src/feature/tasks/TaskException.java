package feature.tasks;

/** Exception thrown when an invalid operation is performed on a task. */
public class TaskException extends RuntimeException {

  /**
   * Creates a new task exception with the specified message.
   *
   * @param message the detail message describing the reason for the exception
   */
  public TaskException(String message) {
    super(message);
  }
}
