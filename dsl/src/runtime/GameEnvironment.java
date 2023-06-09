package runtime;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;

import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;

import dslToGame.QuestConfig;

import runtime.nativefunctions.NativeInstantiate;
import runtime.nativefunctions.NativePrint;

import semanticanalysis.*;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameEnvironment implements IEvironment {
    // TODO: the type builder should also be part of some 'type factory' to
    //  avoid having only one builder for game Environments
    protected final TypeBuilder typeBuilder;

    // TODO: also make HashMaps
    protected final ArrayList<IType> BUILT_IN_TYPES;
    protected final ArrayList<Symbol> NATIVE_FUNCTIONS;

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
        this.typeBuilder = new TypeBuilder();
        this.globalScope = new Scope();
        this.symbolTable = new SymbolTable(this.globalScope);

        // create built in types and native functions
        this.BUILT_IN_TYPES = buildBuiltInTypes();
        this.NATIVE_FUNCTIONS = buildNativeFunctions();

        bindBuiltIns();
        registerDefaultTypeAdapters();
    }

    protected void registerDefaultTypeAdapters() {
        /* The DrawComponent was fundamentally refactort and the DSL is not yet updated.
         * see https://github.com/Programmiermethoden/Dungeon/pull/687 for more information*/
        // typeBuilder.registerTypeAdapter(AnimationBuilder.class, Scope.NULL);
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
    public void loadTypes(IType... types) {
        loadTypes(Arrays.stream(types).toList());
    }

    @Override
    public void loadTypes(List<IType> types) {
        for (IType type : types) {
            if (!(type instanceof IType)) {
                continue;
            }
            if (loadedTypes.containsKey(type.getName())) {
                throw new RuntimeException(
                        "A type with the name '"
                                + type.getName()
                                + "' is already loaded in the environment!");
            }
            loadedTypes.put(type.getName(), type);
            this.globalScope.bind((Symbol) type);
        }
    }

    @Override
    public void loadFunctions(ScopedSymbol... functions) {
        loadFunctions(Arrays.stream(functions).toList());
    }

    @Override
    public void loadFunctions(List<ScopedSymbol> functions) {
        for (var func : functions) {
            if (!(func instanceof ICallable)) {
                continue;
            }
            if (loadedFunctions.containsKey(func.getName())) {
                continue;
            }
            loadedFunctions.put(func.getName(), func);
            this.globalScope.bind(func);
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

    @Override
    public HashMap<Class<?>, IType> javaTypeToDSLTypeMap() {
        return typeBuilder.getJavaTypeToDSLTypeMap();
    }

    private ArrayList<IType> buildBuiltInTypes() {
        ArrayList<IType> types = new ArrayList<>();

        types.add(BuiltInType.noType);
        types.add(BuiltInType.intType);
        types.add(BuiltInType.floatType);
        types.add(BuiltInType.stringType);
        types.add(BuiltInType.graphType);
        types.add(EntityType.ENTITY_TYPE);
        //types.add(BuiltInType.funcType);

        registerDefaultTypeAdapters();

        var questConfigType = typeBuilder.createTypeFromClass(Scope.NULL, QuestConfig.class);
        var entityComponentType = typeBuilder.createTypeFromClass(Scope.NULL, Entity.class);
        var positionComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, PositionComponent.class);
        /* The DrawComponent was fundamentally refactort and the DSL is not yet updated.
         * see https://github.com/Programmiermethoden/Dungeon/pull/687 for more information*/
        // var animationComponentType =
        //      typeBuilder.createTypeFromClass(Scope.NULL, DrawComponent.class);
        var velocityComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, VelocityComponent.class);
        var aiComponentType = typeBuilder.createTypeFromClass(Scope.NULL, AIComponent.class);
        var hitboxComponentType =
                typeBuilder.createTypeFromClass(Scope.NULL, CollideComponent.class);
        types.add(questConfigType);
        types.add(entityComponentType);
        types.add(positionComponentType);
        /* The DrawComponent was fundamentally refactort and the DSL is not yet updated.
         * see https://github.com/Programmiermethoden/Dungeon/pull/687 for more information*/
        // types.add(animationComponentType);
        types.add(velocityComponentType);
        types.add(aiComponentType);
        types.add(hitboxComponentType);

        return types;
    }

    private static ArrayList<Symbol> buildNativeFunctions() {
        ArrayList<Symbol> nativeFunctions = new ArrayList<>();
        nativeFunctions.add(NativePrint.func);
        nativeFunctions.add(NativeInstantiate.func);
        return nativeFunctions;
    }
}
