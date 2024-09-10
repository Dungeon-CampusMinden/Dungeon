import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DiagnosticsVisitor extends DungeonDiagnosticsBaseVisitor<Object> {
    // Stack zur Nachverfolgung von Variablen-Scope (jede Funktion oder Block hat einen eigenen Scope)
    private Stack<Map<String, Boolean>> scopes = new Stack<>();

    // Liste zur Speicherung von Fehlern
    public ArrayList<ErrorInfo> errors = new ArrayList<>();

    // Konstruktor, um den globalen Scope zu initialisieren
    public DiagnosticsVisitor() {
        // Globalen Scope erstellen
        scopes.push(new HashMap<>());
    }


    // Hilfsmethode zum Hinzufügen eines Fehlers in die errors-Liste
    private void addError(String message, int line, int column) {
        errors.add(new ErrorInfo( line, column, message));
    }

    // Getter für die Fehlerliste
    public ArrayList<ErrorInfo> getErrors() {
        return errors;
    }


}
