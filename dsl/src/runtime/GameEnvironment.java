package runtime;

import dslToGame.AnimationBuilder;
import dslToGame.QuestConfig;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
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
    // TODO: the type builder should also be part of some 'type factory' to
    //  avoid having only one builder for game Environments
    protected static final TypeBuilder typeBuilder = new TypeBuilder();

    // TODO: also make HashMaps
    protected static final ArrayList<IType> BUILT_IN_TYPES = buildBuiltInTypes();
    protected static final ArrayList<Symbol> NATIVE_FUNCTIONS = buildNativeFunctions();

    protected final HashMap<String, IType> loadedTypes = new HashMap<>();
    protected final HashMap<String, Symbol> loadedFunctions = new HashMap<>();
    protected final SymbolTable symbolTable;
    protected final Scope globalScope;

    public TypeBuilder getTypeBuilder() {
        return typeBuilder;
    }

    /**
     * Constructor. Creates fresh global scope and symbol table and binds built in types and native
     * functions
     */
    public GameEnvironment() {
        this.globalScope = new Scope();
        this.symbolTable = new SymbolTable(this.globalScope);

        bindBuiltIns();
        registerDefaultTypeAdapters();
    }

    protected static void registerDefaultTypeAdapters() {
        typeBuilder.registerTypeAdapter(AnimationBuilder.class, Scope.NULL);
    }

    protected void bindBuiltIns() {
        for (IType type : BUILT_IN_TYPES) {
            globalScope.bind((Symbol) type);
        }

        for (Symbol func : NATIVE_FUNCTIONS) {
            globalScope.bind(func);
        }
    }

    @Override
    public IType[] getTypes() {
        var typesArray = new IType[BUILT_IN_TYPES.size() + loadedTypes.size()];
        var combinedList = new ArrayList<IType>();
        combinedList.addAll(BUILT_IN_TYPES);
        combinedList.addAll(loadedTypes.values());
        return combinedList.toArray(typesArray);
    }

    @Override
    public Symbol[] getFunctions() {
        var funcArray = new Symbol[NATIVE_FUNCTIONS.size() + loadedFunctions.size()];
        var combinedList = new ArrayList<Symbol>();
        combinedList.addAll(NATIVE_FUNCTIONS);
        combinedList.addAll(loadedFunctions.values());
        return combinedList.toArray(funcArray);
    }

    @Override
    public void loadTypes(IType[] types) {
        for (IType type : types) {
            if (!(type instanceof IType)) {
                continue;
            }
            if (loadedTypes.containsKey(type.getName())) {
                continue;
            }
            loadedTypes.put(type.getName(), type);
            this.globalScope.bind((Symbol) type);
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

    private static ArrayList<IType> buildBuiltInTypes() {
        ArrayList<IType> types = new ArrayList<>();

        types.add(BuiltInType.intType);
        types.add(BuiltInType.floatType);
        types.add(BuiltInType.stringType);
        types.add(BuiltInType.graphType);
        types.add(BuiltInType.funcType);

        registerDefaultTypeAdapters();

        var questConfigType = typeBuilder.createTypeFromClass(Scope.NULL, QuestConfig.class);
        var entityComponentType = typeBuilder.createTypeFromClass(Scope.NULL, Entity.class);
        var positionComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, PositionComponent.class);
        var animationComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, AnimationComponent.class);
        var velocityComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, VelocityComponent.class);
        var aiComponentType = typeBuilder.createTypeFromClass(Scope.NULL, AIComponent.class);
        var hitboxComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, HitboxComponent.class);
        types.add(questConfigType);
        types.add(entityComponentType);
        types.add(positionComponentType);
        types.add(animationComponentType);
        types.add(velocityComponentType);
        types.add(aiComponentType);
        types.add(hitboxComponentType);

        return types;
    }

    private static ArrayList<Symbol> buildNativeFunctions() {
        ArrayList<Symbol> nativeFunctions = new ArrayList<>();
        nativeFunctions.add(NativePrint.func);
        return nativeFunctions;
    }
}
