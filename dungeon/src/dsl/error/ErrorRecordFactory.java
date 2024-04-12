package dsl.error;

import dsl.IndexGenerator;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;

import java.util.ArrayList;

public class ErrorRecordFactory {
  public static ErrorRecordFactory instance = new ErrorRecordFactory();

  private ArrayList<ErrorRecord> records = new ArrayList<>();

  private ErrorRecordFactory() {}

  public void clear() {
    this.records.clear();
  }

  public ArrayList<ErrorRecord> get() {
    return records;
  }

  public ErrorRecord errorRecord(String msg, CommonToken offendingSymbol, int line, int charPositionInLine, RecognitionException exception) {
    ErrorRecord.ErrorType errorType = ErrorRecord.exceptionToType(exception);

    var record = new ErrorRecord(msg, offendingSymbol, line, charPositionInLine, errorType, exception);
    records.add(record);
    return record;
  }

  public ErrorRecord errorRecord(String msg, CommonToken offendingSymbol, int line, int charPositionInLine, ErrorRecord.ErrorType type, RecognitionException exception) {

    var record = new ErrorRecord(msg, offendingSymbol, line, charPositionInLine, type, exception);
    records.add(record);
    return record;
  }

  public ErrorRecord errorRecord(RecognitionException ex) {
    var record = ErrorRecord.fromRecognitionException(ex);
    records.add(record);
    return record;
  }
}
