/*
* A compilation exception.
 */

package blockly.compiler.api;

/** Exception type thrown when a source file cannot be compiled to IR. */
public class CompilationException extends Exception {
  public CompilationException(String message) {
    super(message);
  }

  public CompilationException(String message, Throwable cause) {
    super(message, cause);
  }
}
