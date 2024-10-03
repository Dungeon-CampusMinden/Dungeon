import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class CustomErrorListener extends BaseErrorListener {

    // Liste zur Speicherung von Fehlerinformationen
    private final List<ErrorInfo> errors = new ArrayList<>();

    // Methode wird aufgerufen, wenn ein Syntaxfehler auftritt
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        // Speichere den Fehler als benutzerdefiniertes ParseError-Objekt
        errors.add(new ErrorInfo(line, charPositionInLine, msg));
    }

    // Methode, um die gesammelten Fehler zurückzugeben
    public List<ErrorInfo> getErrors() {
        return errors;
    }


}

