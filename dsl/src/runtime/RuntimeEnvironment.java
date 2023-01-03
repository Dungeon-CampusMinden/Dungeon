package runtime;

import java.util.HashMap;
import semanticAnalysis.IScope;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTable;

// this extends the normal IEnvironment definition by storing AggregateTypeWithDefault-types
// which are basically evaluated type definitions (of game objects)
public class RuntimeEnvironment implements IEvironment {
    private final SymbolTable symbolTable;
    private final HashMap<String, Symbol> functions;
    private final HashMap<String, Symbol> types;
    private final HashMap<String, AggregateTypeWithDefaults> typesWithDefaults;

    public RuntimeEnvironment(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.functions = new HashMap<>();
        this.types = new HashMap<>();
        this.typesWithDefaults = new HashMap<>();
    }

    // construct a new runtime environment from an existing environment and
    // add all type definitions to stored types
    public RuntimeEnvironment(IEvironment other) {
        this.symbolTable = other.getSymbolTable();

        var functions = other.getFunctions();
        this.functions = new HashMap<>();
        for (var function : functions) {
            this.functions.put(function.getName(), function);
        }

        var types = other.getTypes();
        this.types = new HashMap<>();
        for (var type : types) {
            this.types.put(type.getName(), type);
        }

        this.typesWithDefaults = new HashMap<>();
    }

    public AggregateTypeWithDefaults lookupTypeWithDefaults(String name) {
        return this.typesWithDefaults.getOrDefault(name, AggregateTypeWithDefaults.NONE);
    }

    public boolean addTypeWithDefaults(AggregateTypeWithDefaults type) {
        if (this.typesWithDefaults.containsKey(type.getName())) {
            return false;
        } else {
            this.typesWithDefaults.put(type.getName(), type);
            return true;
        }
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    @Override
    public IScope getGlobalScope() {
        return this.symbolTable.getGlobalScope();
    }
}
