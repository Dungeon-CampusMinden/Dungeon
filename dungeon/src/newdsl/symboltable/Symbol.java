package newdsl.symboltable;

import newdsl.common.SourceLocation;

public interface Symbol {
    String getName();

    SymbolTable.SymbolType getType();

    SourceLocation getSourceLocation();

}
