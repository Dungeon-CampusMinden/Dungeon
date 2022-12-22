package runtime;

import semanticAnalysis.FunctionSymbol;
import semanticAnalysis.IScope;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTable;

public interface IEvironment {

    /**
     * @return all available types of the environment
     */
    default Symbol[] getTypes() {
        return new Symbol[0];
    }

    /**
     * @return all available function definitions
     */
    default Symbol[] getFunctions() {
        return new Symbol[0];
    }

    /**
     * @param types AggregateTypes to load into the environment
     */
    default void loadTypes(Symbol[] types) {}

    /**
     * @param functionDefinitions FunctionSymbols to load into the environment
     */
    default void loadFunctions(FunctionSymbol[] functionDefinitions) {}

    /**
     * @return symbol table of this environment
     */
    SymbolTable getSymbolTable();

    /**
     * @return global scope of this environment
     */
    IScope getGlobalScope();
}
