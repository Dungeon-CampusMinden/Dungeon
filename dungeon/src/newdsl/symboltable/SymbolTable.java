package newdsl.symboltable;

import java.util.HashMap;

public class SymbolTable {

    private Scope currentScope;

    public SymbolTable() {
        this.currentScope = new GlobalScope(null, new HashMap<>());
        HashMap<String, Symbol> keywords = bindKeywords();

        this.currentScope.setSymbols(keywords);
    }

    private HashMap<String, Symbol> bindKeywords() {
        String[] keywords = {"import", "task", "variant", "with", "grade", "pass", "scenario", "solution", "select", "from", "and", "or", "as", "multiple-choice", "multiple", "single-choice", "single", "fill-in-the-blank", "blank", "matching", "match", "crafting", "craft", "calculation", "calc"};
        HashMap<String, Symbol> keywordMap = new HashMap<>();

        for (String type : keywords) {
            BuiltIn keyword = new BuiltIn(type, SymbolType.BUILTIN, null);
            keywordMap.put(type, keyword);
        }
        return keywordMap;
    }

    public void addSymbol(Symbol symbol) {
        this.currentScope.bind(symbol);
    }

    public void setScope(Scope scope) {
        this.currentScope = scope;
    }

    public void popScope() {
        this.currentScope = currentScope.getEnclosingScope();
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public boolean isDefined(String name) {
        return this.currentScope.resolveInScope(name) != null;
    }

    public void enterScope(String name) {
        Symbol symbol = currentScope.resolveInScope(name);

        if (symbol instanceof SymbolWithScope) {
            this.currentScope = (SymbolWithScope) symbol;
        } else {
            System.err.println("Cannot enter scope: " + name);
        }
    }

    public enum SymbolType {
        TASK, TASK_COMPOSITION, TASK_CONFIGURATION, ALIAS, OPTIONAL_CONTENT, BUILTIN, PARAMETER;

        @Override
        public String toString() {
            return switch (this) {
                case TASK -> "Task";
                case TASK_COMPOSITION -> "Task Composition";
                case TASK_CONFIGURATION -> "Task Configuration";
                case ALIAS -> "Alias";
                case OPTIONAL_CONTENT -> "Optional Content";
                case BUILTIN -> "Builtin";
                case PARAMETER -> "Parameter";
            };
        }
    }

}
