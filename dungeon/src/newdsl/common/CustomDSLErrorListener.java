package newdsl.common;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.List;

public class CustomDSLErrorListener extends BaseErrorListener {
    List<DSLError> errors;

    public CustomDSLErrorListener(List<DSLError> errors) {
        super();
        this.errors = errors;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errors.add(new DSLError(msg, new SourceLocation(line, charPositionInLine, ((CommonToken) offendingSymbol).getTokenSource().getSourceName())));
    }

}
