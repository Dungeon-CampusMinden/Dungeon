package dsl.error;

import java.util.BitSet;
import java.util.logging.Logger;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class ErrorListener extends BaseErrorListener {
  private static final Logger LOGGER = Logger.getLogger("error");
  static ErrorListener INSTANCE = new ErrorListener();

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {
    String warning =
        String.format(
            "Syntax error, recognizer: '%s', offendingSymbol: '%s', line: %x, charPosition: %x, msg: '%s', exception: '%s'",
            recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    LOGGER.warning(warning);
  }

  @Override
  public void reportAmbiguity(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      boolean exact,
      BitSet ambigAlts,
      ATNConfigSet configs) {
    String warning =
        String.format(
            "Ambiguity, recognizer: '%s', dfa: '%s', startIndex: %x, stopIndex: %x",
            recognizer, dfa, startIndex, stopIndex);
    LOGGER.warning(warning);
  }

  @Override
  public void reportAttemptingFullContext(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      BitSet conflictingAlts,
      ATNConfigSet configs) {
    String warning =
        String.format(
            "Ambiguity full context, recognizer: '%s', dfa: '%s', startIndex: %x, stopIndex: %x",
            recognizer, dfa, startIndex, stopIndex);
    LOGGER.warning(warning);
  }

  @Override
  public void reportContextSensitivity(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      int prediction,
      ATNConfigSet configs) {
    String warning =
        String.format(
            "Context sensitivity, recognizer: '%s', dfa: '%s', startIndex: %x, stopIndex: %x",
            recognizer, dfa, startIndex, stopIndex);
    LOGGER.warning(warning);
  }
}
