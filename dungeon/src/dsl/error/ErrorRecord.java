package dsl.error;

import dsl.IndexGenerator;
import dsl.programmanalyzer.Relatable;
import org.antlr.v4.runtime.*;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class ErrorRecord implements Relatable {
  private static long g_documentVersion = 0;

  public static void setDocumentVersion(long version) {
    g_documentVersion = version;
  }

  @Property private final String msg;
  @Transient private final CommonToken offendingSymbol;
  @Property private final int line;
  @Property private final int charPositionInLine;
  @Property private final ErrorType errorType;
  @Property private final long documentVersion;
  @Transient private final RecognitionException exception;
  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();

  // @Id public final long id = IndexGenerator.getIdx();

  @Override
  public Long getId() {
    return this.internalId;
  }

  public enum ErrorType {
    none,
    lexerNoViableAlternative,
    inputMismatch,
    unwantedToken,
    missingToken,
    noViableAlternative,
    failedPredicate,
    specificErrorAlternative // TODO: specify, which one
  ;

    @Override
    public String toString() {
      return switch (this) {
        case none -> "no error type";
        case lexerNoViableAlternative -> "Lexer error";
        case inputMismatch -> "Input mismatch";
        case unwantedToken -> "Unexpected token";
        case missingToken -> "Missing token";
        case noViableAlternative -> "No viable alternative";
        case failedPredicate -> "Failed predicate";
        case specificErrorAlternative -> "Specific error";
      };
    }
  }

  public String msg() {
    return msg;
  }

  public CommonToken offendingSymbol() {
    return offendingSymbol;
  }

  public RecognitionException exception() {
    return exception;
  }

  public ErrorType errorType() {
    return this.errorType;
  }

  public int line() {
    return line;
  }

  public int charPositionInLine() {
    return charPositionInLine;
  }

  public ErrorRecord() {
    this.documentVersion = g_documentVersion;
    this.msg = "";
    this.errorType = ErrorType.none;
    this.line = -1;
    this.charPositionInLine = -1;
    this.exception = null;
    this.offendingSymbol = null;
  }

  public ErrorRecord(
      String msg,
      CommonToken offendingSymbol,
      int line,
      int charPositionInLine,
      ErrorType errorType,
      RecognitionException exception) {
    this.documentVersion = g_documentVersion;
    this.msg = msg;
    this.errorType = errorType;
    this.offendingSymbol = offendingSymbol;
    this.line = line;
    this.charPositionInLine = charPositionInLine;
    this.exception = exception;
  }

  public static ErrorType exceptionToType(RecognitionException exception) {
    ErrorType errorType = ErrorType.none;
    if (exception instanceof MissingTokenException) {
      errorType = ErrorType.missingToken;
    } else if (exception instanceof UnwantedTokenException) {
      errorType = ErrorType.unwantedToken;
    } else if (exception instanceof InputMismatchException) {
      errorType = ErrorType.inputMismatch;
    } else if (exception instanceof NoViableAltException) {
      errorType = ErrorType.noViableAlternative;
    } else if (exception instanceof LexerNoViableAltException) {
      errorType = ErrorType.lexerNoViableAlternative;
    }
    return errorType;
  }

  public static ErrorRecord fromRecognitionException(RecognitionException exception) {
    var offendingToken = (CommonToken) exception.getOffendingToken();
    ErrorType errorType = exceptionToType(exception);

    return // new ErrorRecord(
    ErrorRecordFactory.instance.errorRecord(
        exception.getMessage(),
        offendingToken,
        offendingToken.getLine(),
        offendingToken.getCharPositionInLine(),
        errorType,
        exception);
  }

  // TODO: which type to put here?
  public static ErrorRecord fromOffendingSymbol(CommonToken offendingSymbol) {
    String msg = "Found offending symbol " + offendingSymbol.toString();

    return // new ErrorRecord(
    ErrorRecordFactory.instance.errorRecord(
        msg,
        offendingSymbol,
        offendingSymbol.getLine(),
        offendingSymbol.getCharPositionInLine(),
        null);
  }
}
