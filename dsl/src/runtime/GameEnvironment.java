package runtime;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;

import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;

import dslToGame.EntityTranslator;
import dslToGame.QuestConfig;
import dslToGame.taskdsltypes.MultipleChoiceTask;
import dslToGame.taskdsltypes.SingleChoiceDescriptionProperty;
import dslToGame.taskdsltypes.SingleChoiceTask;

import runtime.nativefunctions.NativeInstantiate;
import runtime.nativefunctions.NativePrint;

import semanticanalysis.*;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import task.Task;
import task.quizquestion.Quiz;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameEnvironment implements IEvironment {
    // TODO: the TypeBuilder should be completely encapsulated, so that types can only
    //  be created via the environment, so that the global scope of the environment is
    //  always passed correctly to the TypeBuilder; this has some major implications for
    //  RuntimeEnvironment and GameEnvironment and depends on the implementation of
    //  typechecking, which is not yet implemented
    protected final TypeBuilder typeBuilder;

    // TODO: also make HashMaps
    protected final ArrayList<Symbol> NATIVE_FUNCTIONS;

    protected final HashMap<String, IType> loadedTypes = new HashMap<>();
    protected final HashMap<String, Symbol> loadedFunctions = new HashMap<>();
    protected final SymbolTable symbolTable;
    protected final Scope globalScope;
    protected final RuntimeObjectTranslator runtimeObjectTranslator = new RuntimeObjectTranslator();

    /* The DrawComponent was fundamentally refactort and the DSL is not yet updated.
     * see https://github.com/Programmiermethoden/Dungeon/pull/687 for more information*/
    // var animationComponentType =
    //      typeBuilder.createTypeFromClass(Scope.NULL, DrawComponent.class);
    public Class<?>[] getBuiltInAggregateTypeClasses() {
        return (Class<?>[])
                new Class[] {
                    QuestConfig.class,
                    Entity.class,
                    PositionComponent.class,
                    VelocityComponent.class,
                    AIComponent.class,
                    CollideComponent.class,
                    Task.class,
                    // SingleChoiceTask.class,
                    Quiz.Content.class
                };
    }

    public List<IDSLTypeProperty<?, ?>> getBuiltInProperties() {
        ArrayList<IDSLTypeProperty<?, ?>> properties = new ArrayList<>();
        properties.add(SingleChoiceDescriptionProperty.instance);
        return properties;
    }

    @Override
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
        this.NATIVE_FUNCTIONS = buildNativeFunctions();

        bindBuiltInTypes();

        registerDefaultTypeAdapters();
        registerDefaultRuntimeObjectTranslators();
        bindBuiltInAggregateTypes();

        bindBuiltInProperties();

        bindNativeFunctions();
    }

    protected void registerDefaultTypeAdapters() {
        /* The DrawComponent was fundamentally refactort and the DSL is not yet updated.
         * see https://github.com/Programmiermethoden/Dungeon/pull/687 for more information*/
        // typeBuilder.registerTypeAdapter(AnimationBuilder.class, Scope.NULL);
        typeBuilder.registerTypeAdapter(SingleChoiceTask.class, this.globalScope);
        typeBuilder.registerTypeAdapter(MultipleChoiceTask.class, this.globalScope);
    }

    protected void registerDefaultRuntimeObjectTranslators() {
        this.runtimeObjectTranslator.loadObjectToValueTranslator(
                Entity.class, EntityTranslator.instance);
    }

    protected void bindNativeFunctions() {
        for (Symbol func : NATIVE_FUNCTIONS) {
            globalScope.bind(func);
        }
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
    public HashMap<Type, IType> javaTypeToDSLTypeMap() {
        return typeBuilder.getJavaTypeToDSLTypeMap();
    }

    @Override
    public RuntimeObjectTranslator getRuntimeObjectTranslator() {
        return this.runtimeObjectTranslator;
    }

    protected void bindBuiltInAggregateTypes() {
        for (Class<?> clazz : getBuiltInAggregateTypeClasses()) {
            this.typeBuilder.createDSLTypeForJavaTypeInScope(this.globalScope, clazz);
        }
    }

    protected void bindBuiltInProperties() {
        for (IDSLTypeProperty<?, ?> property : getBuiltInProperties()) {
            this.typeBuilder.registerProperty(this.globalScope, property);
        }
    }

    private void bindBuiltInTypes() {
        this.globalScope.bind(BuiltInType.noType);
        this.globalScope.bind(BuiltInType.boolType);
        this.globalScope.bind(BuiltInType.intType);
        this.globalScope.bind(BuiltInType.floatType);
        this.globalScope.bind(BuiltInType.stringType);
        this.globalScope.bind(BuiltInType.graphType);
        this.globalScope.bind(Prototype.PROTOTYPE);
    }

    private static ArrayList<Symbol> buildNativeFunctions() {
        ArrayList<Symbol> nativeFunctions = new ArrayList<>();
        nativeFunctions.add(NativePrint.func);
        nativeFunctions.add(NativeInstantiate.func);
        return nativeFunctions;
    }
}
