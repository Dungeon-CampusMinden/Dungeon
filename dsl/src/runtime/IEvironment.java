package runtime;

import semanticanalysis.*;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.util.HashMap;
import java.util.List;

// TODO: this is more of a semantic analysis kind of concept -> put it there
// TODO: add getTypeBuilder
public interface IEvironment {

    TypeBuilder getTypeBuilder();

    /**
     * @return all available types of the environment
     */
    default IType[] getTypes() {
        return new IType[0];
    }

    // default Symbol lookupType(String name) { return Symbol.NULL; }

    /**
     * @return all available function definitions
     */
    default Symbol[] getFunctions() {
        return new Symbol[0];
    }

    // default Symbol lookupFunction(String name) { return Symbol.NULL; }
    /**
     * @param types AggregateTypes to load into the environment
     */
    default void loadTypes(IType... types) {}

    /**
     * @param types AggregateTypes to load into the environment
     */
    default void loadTypes(List<IType> types) {}

    /**
     * @param functionDefinitions FunctionSymbols to load into the environment
     */
    default void loadFunctions(ScopedSymbol... functionDefinitions) {}

    /**
     * @param functionDefinitions FunctionSymbols to load into the environment
     */
    default void loadFunctions(List<ScopedSymbol> functionDefinitions) {}

    /**
     * @return symbol table of this environment
     */
    SymbolTable getSymbolTable();

    /**
     * @return global scope of this environment
     */
    IScope getGlobalScope();

    default HashMap<Class<?>, IType> javaTypeToDSLTypeMap() {
        return new HashMap<>();
    }

    RuntimeObjectTranslator getRuntimeObjectTranslator();
}
