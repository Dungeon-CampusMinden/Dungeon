package newdsl.symboltable;

import newdsl.common.SourceLocation;

public class BuiltIn extends BaseSymbol implements Symbol {

    public BuiltIn(String name, SymbolTable.SymbolType type, SourceLocation sourceLocation) {
        super(name, type, sourceLocation);
    }

}
