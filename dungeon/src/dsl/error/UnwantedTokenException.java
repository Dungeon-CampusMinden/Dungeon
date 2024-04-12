package dsl.error;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

public class UnwantedTokenException extends RecognitionException {
  public UnwantedTokenException(String message, Parser recognizer) {
    super(message, recognizer, recognizer.getInputStream(), recognizer.getContext());
  }
}
