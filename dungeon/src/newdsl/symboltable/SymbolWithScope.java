package newdsl.symboltable;

import newdsl.common.SourceLocation;

import java.util.HashMap;

public abstract class SymbolWithScope extends BaseScope implements Symbol {
    protected final String name;
    protected final SymbolTable.SymbolType type;

    public SymbolWithScope(Scope enclosingScope, SymbolTable.SymbolType type, HashMap<String, Symbol> symbols, String name, SourceLocation sourceLocation) {
        super(enclosingScope, symbols, sourceLocation);
        this.name = name;
        this.type = type;
    }

    public Scope getScope() {
        return enclosingScope;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SymbolTable.SymbolType getType() {
        return type;
    }
}
