package runtime;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;

import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;

import dslnativefunction.NativeBuildQuestItem;
import dslnativefunction.NativeInstantiate;

import dsltypeadapters.DrawComponentAdapter;

import dsltypeadapters.QuestItemAdapter;
import dsltypeproperties.EntityExtension;

import dsltypeproperties.QuestItemExtension;
import dungeonFiles.DungeonConfig;

import interpreter.DSLInterpreter;
import parser.ast.Node;
import runtime.nativefunctions.NativeFunction;
import runtime.nativefunctions.NativePrint;

import semanticanalysis.*;
import semanticanalysis.types.*;

import task.QuestItem;
import task.Quiz;
import task.Task;
import task.components.TaskComponent;
import task.taskdsltypes.MultipleChoiceTask;
import task.taskdsltypes.SingleChoiceDescriptionProperty;
import task.taskdsltypes.SingleChoiceTask;

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

    public Class<?>[] getBuiltInAggregateTypeClasses() {
        return (Class<?>[])
                new Class[] {
                    DungeonConfig.class,
                    Entity.class,
                    QuestItem.class,
                    PositionComponent.class,
                    VelocityComponent.class,
                    AIComponent.class,
                    CollideComponent.class,
                    DrawComponent.class,
                    TaskComponent.class,
                    Task.class,
                    // SingleChoiceTask.class,
                    Quiz.Content.class,
                    Tile.Direction.class
                };
    }

    public List<IDSLTypeProperty<?, ?>> getBuiltInProperties() {
        ArrayList<IDSLTypeProperty<?, ?>> properties = new ArrayList<>();
        properties.add(SingleChoiceDescriptionProperty.instance);

        properties.add(EntityExtension.VelocityComponentProperty.instance);
        properties.add(EntityExtension.PositionComponentProperty.instance);
        properties.add(EntityExtension.DrawComponentProperty.instance);
        properties.add(EntityExtension.TaskComponentProperty.instance);
        properties.add(TaskComponent.TaskProperty.instance);
        properties.add(QuestItemExtension.TaskContentComponentProperty.instance);

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

        bindBuiltInTypes();

        registerDefaultTypeAdapters();
        registerDefaultRuntimeObjectTranslators();
        bindBuiltInAggregateTypes();

        bindBuiltInProperties();
        bindBuiltInMethods();

        // create built in types and native functions
        this.NATIVE_FUNCTIONS = buildNativeFunctions();
        bindNativeFunctions();
    }

    protected void registerDefaultTypeAdapters() {
        typeBuilder.registerTypeAdapter(DrawComponentAdapter.class, this.globalScope);

        typeBuilder.registerTypeAdapter(SingleChoiceTask.class, this.globalScope);
        typeBuilder.registerTypeAdapter(MultipleChoiceTask.class, this.globalScope);
        typeBuilder.registerTypeAdapter(QuestItemAdapter.class, this.globalScope);
    }

    protected void registerDefaultRuntimeObjectTranslators() {}

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
            this.typeBuilder.bindProperty(this.globalScope, property);
        }
    }

    protected void bindBuiltInMethods() {
        for (IDSLExtensionMethod<?, ?> method : getBuiltInMethods()) {
            this.typeBuilder.bindMethod(this.globalScope, method);
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
        this.globalScope.bind(Prototype.ITEM_PROTOTYPE);
    }

    private ArrayList<Symbol> buildNativeFunctions() {
        ArrayList<Symbol> nativeFunctions = new ArrayList<>();
        nativeFunctions.add(NativePrint.func);
        nativeFunctions.add(NativeInstantiate.func);
        nativeFunctions.add(NativeBuildQuestItem.func);

        // build functions with dependency on specific non-builtin types
        IType questItemType = (IType) this.globalScope.resolve("quest_item");
        IType entityType = (IType) this.globalScope.resolve("entity");
        IType entitySetType = new SetType(entityType, this.globalScope);

        NativeFunction placeQuestItem = new NativePlaceQuestItem(Scope.NULL, questItemType, entitySetType);
        nativeFunctions.add(placeQuestItem);

        return nativeFunctions;
    }

    // region native functions with dependency on specific types

    private static class NativePlaceQuestItem extends NativeFunction {
        /**
         * Constructor
         *
         * @param parentScope parent scope of this function
         */
        public NativePlaceQuestItem(IScope parentScope, IType questItemType, IType entitySetType) {
            super(
                "place_quest_item",
                parentScope,
                new FunctionType(BuiltInType.noType, questItemType, entitySetType));

            // bind parameters
            Symbol param = new Symbol("", this, questItemType);
            this.bind(param);
        }

        // TODO: finish
        @Override
        public Object call(DSLInterpreter interpreter, List<Node> parameters) {
            assert parameters != null && parameters.size() > 0;

            RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
            Value questItemValue = (Value) parameters.get(0).accept(interpreter);
            SetValue entitySetValue = (SetValue) parameters.get(1).accept(interpreter);

            var questItemObject = questItemValue.getInternalValue();
            var worldEntity = WorldItemBuilder.buildWorldItem((Item)questItemObject);
            var worldEntityValue = (Value) rtEnv.translateRuntimeObject(worldEntity, interpreter.getCurrentMemorySpace());
            entitySetValue.addValue(worldEntityValue);

            return null;
        }

        @Override
        public ICallable.Type getCallableType() {
            return ICallable.Type.Native;
        }
    }

    // endregion
}
