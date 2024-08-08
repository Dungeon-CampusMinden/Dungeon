package autocompletion;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/** ANTLR Error Listener that throws a runtime exception on syntax errors. */
public class ThrowErrorListener extends BaseErrorListener {
  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {
    throw new RuntimeException("line " + line + ":" + charPositionInLine + " " + msg);
  }
}
