package dsl.runtime;

import dsl.semanticanalysis.IScope;
import dsl.semanticanalysis.ScopedSymbol;
import dsl.semanticanalysis.Symbol;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.types.BuiltInType;
import dsl.semanticanalysis.types.IType;
import dsl.semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO: this is more of a semantic analysis kind of concept -> put it there
public interface IEnvironment {

    TypeBuilder getTypeBuilder();

    /**
     * @return all available types of the environment
     */
    default IType[] getTypes() {
        ArrayList<IType> types = new ArrayList<>();

        for (Symbol symbol : this.getGlobalScope().getSymbols()) {
            if (symbol instanceof IType) {
                types.add((IType) symbol);
            }
        }
        return types.toArray(new IType[0]);
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

    default HashMap<Type, IType> javaTypeToDSLTypeMap() {
        return new HashMap<>();
    }

    default IType getDSLTypeForClass(Class<?> clazz) {
        IType dslType = BuiltInType.noType;
        String dslTypeName = TypeBuilder.getDSLTypeName(clazz);
        Symbol dslTypeSymbol = this.getGlobalScope().resolve(dslTypeName);
        if (dslTypeSymbol != Symbol.NULL) {
            dslType = (IType) dslTypeSymbol;
        }
        return dslType;
    }

    default Symbol resolveInGlobalScope(String name) {
        return this.getGlobalScope().resolve(name);
    }

    RuntimeObjectTranslator getRuntimeObjectTranslator();
}
