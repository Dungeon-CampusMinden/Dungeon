package dsl.error;

import org.antlr.v4.runtime.*;

public class MissingTokenException extends RecognitionException {
  public MissingTokenException(String message, Parser recognizer) {
    super(message, recognizer, recognizer.getInputStream(), recognizer.getContext());
  }
}
