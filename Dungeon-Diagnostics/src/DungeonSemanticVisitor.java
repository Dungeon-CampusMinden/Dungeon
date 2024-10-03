import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DungeonSemanticVisitor extends SemanticAnalysisBaseVisitor<Object> {
    // Stack für Scopes: Jede Ebene enthält eine Map für Variablen und ihre Typen/Informationen
    private Stack<Map<String, String>> scopeStack = new Stack<>();

    // Aktuelle Scope-Tiefe
    private int scopeLevel = 0;

    public DungeonSemanticVisitor() {
        // Initialer globaler Scope
        scopeStack.push(new HashMap<>());
    }

    // Prüfe eine Variablendeklaration
    @Override
    public Object visitVar_decl_assignment(SemanticAnalysisParser.Var_decl_assignmentContext ctx) {
        String varName = ctx.id.getText();  // Variablenname
        Map<String, String> currentScope = scopeStack.peek();  // Hole den aktuellen Scope

        if (currentScope.containsKey(varName)) {
            // Fehler: Variable wurde bereits im aktuellen Scope deklariert
            System.out.println("Fehler: Variable '" + varName + "' wurde bereits deklariert in diesem Scope.");
        } else {
            // Deklariere die Variable im aktuellen Scope
            currentScope.put(varName, "assigned");
            System.out.println("Variable '" + varName + "' deklariert in Scope Level: " + scopeLevel);
        }

        return null;
    }

    @Override
    public Object visitOtherCode(SemanticAnalysisParser.OtherCodeContext ctx) {
        String text = ctx.getText();  // Hole den Text des Tokens

        // Prüfe den Inhalt auf { oder }
        for (char c : text.toCharArray()) {
            if (c == '{') {
                // Neuer Scope: Neue Map auf den Stack
                scopeStack.push(new HashMap<>());
                scopeLevel++;
                System.out.println("Scope geöffnet. Aktuelle Tiefe: " + scopeLevel);
            } else if (c == '}') {
                // Scope wird geschlossen: Map entfernen
                if (!scopeStack.isEmpty()) {
                    scopeStack.pop();
                    scopeLevel--;
                } else {
                    throw new RuntimeException("Fehler: Zu viele geschlossene Scopes!");
                }
                System.out.println("Scope geschlossen. Aktuelle Tiefe: " + scopeLevel);
            }
        }

        return null;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

}

class Scope {
    Map<String, String> variables = new HashMap<>();  // Variablenname -> Typ
}

class SymbolTable {
    Stack<Scope> scopeStack = new Stack<>();

    public void enterScope() {
        scopeStack.push(new Scope());
    }

    public void exitScope() {
        scopeStack.pop();
    }

    public boolean declareVariable(String name, String type) {
        if (scopeStack.peek().variables.containsKey(name)) {
            return false; // Variable bereits in diesem Scope deklariert
        }
        scopeStack.peek().variables.put(name, type);
        return true;
    }

    public String lookupVariable(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).variables.containsKey(name)) {
                return scopeStack.get(i).variables.get(name);  // Rückgabe des Typs
            }
        }
        return null;  // Variable nicht gefunden
    }

    // Neue Methode: Überprüft, ob die Variable im aktuellen Scope existiert
    public boolean variableExistsInCurrentScope(String name) {
        if (scopeStack.isEmpty()) {
            return false;  // Kein Scope vorhanden
        }
        return scopeStack.peek().variables.containsKey(name);  // Überprüfung im aktuellen Scope
    }
}
