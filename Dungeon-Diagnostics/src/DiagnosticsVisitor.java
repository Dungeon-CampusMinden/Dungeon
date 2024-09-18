import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DiagnosticsVisitor extends DungeonDiagnosticsBaseVisitor<Object> {
//    SymbolTable symbolTable = new SymbolTable();
//
//    @Override
//    public Void visitStmt_block(DungeonDiagnosticsParser.Stmt_blockContext ctx) {
//        symbolTable.enterScope();
//        super.visitStmt_block(ctx);
//        symbolTable.exitScope();
//        return null;
//    }
//
//    @Override
//    public Void visitVar_decl_assignment(DungeonDiagnosticsParser.Var_decl_assignmentContext ctx) {
//        String varName = ctx.id.getText();
//
//        if (!symbolTable.variableExistsInCurrentScope(varName)) {
//            System.out.println("Variable " + varName + " bereits im aktuellen Scope deklariert");
//        }
//        return null;
//    }
//
//    @Override
//    public Void visitVar_decl_type_decl(DungeonDiagnosticsParser.Var_decl_type_declContext ctx) {
//        String varName = ctx.id.getText();
//        String varType = ctx.type_decl().getText();
//
//        if (!symbolTable.declareVariable(varName, varType)) {
//            System.out.println("Variable " + varName + " bereits im aktuellen Scope deklariert");
//        }
//        return null;
//    }
//
//    @Override
//    public Void visitAssignment(DungeonDiagnosticsParser.AssignmentContext ctx) {
//        String varName = ctx.assignee().getText();
//
//        String declaredType = symbolTable.lookupVariable(varName);
//        if (declaredType == null) {
//            System.out.println("Variable " + varName + " wurde nicht deklariert");
//        }
//        return null;
//    }




//    // Hilfsmethode zum Hinzufügen eines Fehlers in die errors-Liste
//    private void addError(String message, int line, int column) {
//        errors.add(new ErrorInfo( line, column, message));
//    }
//
//    // Getter für die Fehlerliste
//    public ArrayList<ErrorInfo> getErrors() {
//        return errors;
//    }


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
