package runtime;

import dslToGame.QuestConfig;
import java.util.ArrayList;
import java.util.HashMap;
import runtime.nativeFunctions.NativePrint;
import semanticAnalysis.IScope;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTable;
import semanticAnalysis.types.BuiltInType;
import semanticAnalysis.types.IType;
import semanticAnalysis.types.TypeBuilder;

public class GameEnvironment implements IEvironment {
    // TODO: also make HashMaps
    protected static final ArrayList<Symbol> builtInTypes = buildBuiltInTypes();
    protected static final ArrayList<Symbol> nativeFunctions = buildNativeFunctions();

    protected final HashMap<String, Symbol> loadedTypes = new HashMap<>();
    protected final HashMap<String, Symbol> loadedFunctions = new HashMap<>();
    protected final SymbolTable symbolTable;
    protected final Scope globalScope;

    /**
     * Constructor. Creates fresh global scope and symbol table and binds built in types and native
     * functions
     */
    public GameEnvironment() {
        this.globalScope = new Scope();
        this.symbolTable = new SymbolTable(this.globalScope);

        bindBuiltIns();
    }

    protected void bindBuiltIns() {
        for (Symbol type : builtInTypes) {
            globalScope.bind(type);
        }

        for (Symbol func : nativeFunctions) {
            globalScope.bind(func);
        }
    }

    @Override
    public Symbol[] getTypes() {
        var typesArray = new Symbol[builtInTypes.size() + loadedTypes.size()];
        var combinedList = new ArrayList<Symbol>();
        combinedList.addAll(builtInTypes);
        combinedList.addAll(loadedTypes.values());
        return combinedList.toArray(typesArray);
    }

    @Override
    public Symbol[] getFunctions() {
        var funcArray = new Symbol[nativeFunctions.size() + loadedFunctions.size()];
        var combinedList = new ArrayList<Symbol>();
        combinedList.addAll(nativeFunctions);
        combinedList.addAll(loadedFunctions.values());
        return combinedList.toArray(funcArray);
    }

    @Override
    public void loadTypes(Symbol[] types) {
        for (Symbol type : types) {
            if (!(type instanceof IType)) {
                continue;
            }
            if (loadedTypes.containsKey(type.getName())) {
                continue;
            }
            loadedTypes.put(type.getName(), type);
            this.globalScope.bind(type);
        }
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    @Override
    public IScope getGlobalScope() {
        return this.globalScope;
    }

    private static ArrayList<Symbol> buildBuiltInTypes() {
        ArrayList<Symbol> types = new ArrayList<>();

        types.add(BuiltInType.intType);
        types.add(BuiltInType.stringType);
        types.add(BuiltInType.graphType);
        types.add(BuiltInType.funcType);

        TypeBuilder tb = new TypeBuilder();
        var questConfigType = tb.createTypeFromClass(Scope.NULL, QuestConfig.class);
        types.add(questConfigType);

        return types;
    }

    private static ArrayList<Symbol> buildNativeFunctions() {
        ArrayList<Symbol> nativeFunctions = new ArrayList<>();
        nativeFunctions.add(NativePrint.func);
        return nativeFunctions;
    }
}
