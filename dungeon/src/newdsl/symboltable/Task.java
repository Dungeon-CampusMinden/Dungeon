package newdsl.symboltable;

import newdsl.common.SourceLocation;

import java.util.HashMap;

public class Task extends SymbolWithScope {

    public Task(Scope enclosingScope, HashMap<String, Symbol> symbols, String name, SourceLocation sourceLocation) {
        super(enclosingScope, SymbolTable.SymbolType.TASK, symbols, name, sourceLocation);

    }

    @Override
    public Symbol resolve(String name) {
        Symbol memberSymbol = resolveMember(name);
        if (memberSymbol != null) {
            return memberSymbol;
        }

        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }

        return null;
    }

    public Symbol resolveMember(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }


        return null;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return this.sourceLocation;
    }
}
