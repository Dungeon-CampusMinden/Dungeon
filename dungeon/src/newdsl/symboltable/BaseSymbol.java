package newdsl.symboltable;

import newdsl.common.SourceLocation;

public abstract class BaseSymbol implements Symbol {
    protected final String name;
    protected final SymbolTable.SymbolType type;
    protected final SourceLocation sourceLocation;

    public BaseSymbol(String name, SymbolTable.SymbolType type, SourceLocation sourceLocation) {
        this.name = name;
        this.type = type;
        this.sourceLocation = sourceLocation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SymbolTable.SymbolType getType() {
        return type;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }
}
