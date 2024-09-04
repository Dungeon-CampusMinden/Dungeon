package newdsl.symboltable;

import newdsl.common.SourceLocation;

public class Variable extends BaseSymbol {
    public Variable(String name, SymbolTable.SymbolType type, SourceLocation sourceLocation) {
        super(name, type, sourceLocation);
    }
}
