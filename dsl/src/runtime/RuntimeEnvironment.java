package runtime;

import java.util.HashMap;
import semanticAnalysis.IScope;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTable;
import semanticAnalysis.types.IType;

// this extends the normal IEnvironment definition by storing prototypes
// which are basically evaluated type definitions (of game objects)
public class RuntimeEnvironment implements IEvironment {
    private final SymbolTable symbolTable;
    private final HashMap<String, Symbol> functions;
    private final HashMap<String, IType> types;
    private final HashMap<String, Prototype> prototypes;

    public RuntimeEnvironment(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.functions = new HashMap<>();
        this.types = new HashMap<>();
        this.prototypes = new HashMap<>();
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

        this.prototypes = new HashMap<>();
    }

    public Prototype lookupPrototype(String name) {
        return this.prototypes.getOrDefault(name, Prototype.NONE);
    }

    public boolean addPrototype(Prototype type) {
        if (this.prototypes.containsKey(type.getName())) {
            return false;
        } else {
            this.prototypes.put(type.getName(), type);
            return true;
        }
    }

    @Override
    public IType[] getTypes() {
        return this.types.values().toArray(new IType[0]);
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
