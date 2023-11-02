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

import dslnativefunction.NativeInstantiate;

import dsltypeadapters.DrawComponentAdapter;
import dsltypeadapters.QuestItemAdapter;

import dsltypeproperties.EntityExtension;
import dsltypeproperties.QuestItemExtension;

import dungeonFiles.DungeonConfig;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import reporting.GradingFunctions;

import runtime.nativefunctions.NativeFunction;
import runtime.nativefunctions.NativePrint;

import semanticanalysis.*;
import semanticanalysis.types.*;

import task.QuestItem;
import task.Quiz;
import task.Task;
import task.TaskContent;
import task.components.TaskComponent;
import task.taskdsltypes.MultipleChoiceTask;
import task.taskdsltypes.SingleChoiceDescriptionProperty;
import task.taskdsltypes.SingleChoiceTask;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

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
                    TaskContent.class,
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

    public List<IDSLExtensionMethod<?, ?>> getBuiltInMethods() {
        ArrayList<IDSLExtensionMethod<?, ?>> methods = new ArrayList<>();

        methods.add(SingleChoiceTask.GetContentMethod.instance);
        methods.add(MultipleChoiceTask.GetContentMethod.instance);
        methods.add(SingleChoiceTask.SingleChoiceSetGradingFunction.instance);
        methods.add(MultipleChoiceTask.MultipleChoiceSetGradingFunction.instance);

        return methods;
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

        // build functions with dependency on specific non-builtin types
        IType questItemType = (IType) this.globalScope.resolve("quest_item");
        IType entityType = (IType) this.globalScope.resolve("entity");
        IType entitySetType = new SetType(entityType, this.globalScope);

        NativeFunction placeQuestItem =
                new NativePlaceQuestItem(Scope.NULL, questItemType, entitySetType);
        nativeFunctions.add(placeQuestItem);

        IType taskContentType = (IType) this.globalScope.resolve("task_content");
        NativeFunction nativeBuildQuestItem =
                new NativeBuildQuestItem(Scope.NULL, questItemType, taskContentType);
        nativeFunctions.add(nativeBuildQuestItem);

        // grading functions
        var taskSymbol = this.globalScope.resolve("task");
        if (!taskSymbol.equals(Symbol.NULL)) {
            IType taskType = (IType) taskSymbol;
            IType taskContentSetType = new SetType(taskContentType, this.globalScope);
            NativeFunction nativeGradeSingleChoice =
                    new SingleChoiceGrading(this.globalScope, taskType, taskContentSetType);
            nativeFunctions.add(nativeGradeSingleChoice);

            NativeFunction nativeGradeMultipleChoice =
                    new MultipleChoiceGrading(this.globalScope, taskType, taskContentSetType);
            nativeFunctions.add(nativeGradeMultipleChoice);
        }

        return nativeFunctions;
    }

    // region native functions with dependency on specific types

    /** Native function to place a quest item in a "room" (which is represented by an entity set) */
    private static class NativePlaceQuestItem extends NativeFunction {
        /**
         * Constructor
         *
         * @param parentScope parent scope of this function
         * @param questItemType the {@link IType} representing quest items
         * @param entitySetType the {@link IType} representing a {@link SetValue} of entities
         */
        public NativePlaceQuestItem(IScope parentScope, IType questItemType, IType entitySetType) {
            super(
                    "place_quest_item",
                    parentScope,
                    new FunctionType(BuiltInType.noType, questItemType, entitySetType));
        }

        @Override
        public Object call(DSLInterpreter interpreter, List<Node> parameters) {
            assert parameters != null && parameters.size() > 0;

            // evaluate parameters
            RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
            Value questItemValue = (Value) parameters.get(0).accept(interpreter);
            SetValue entitySetValue = (SetValue) parameters.get(1).accept(interpreter);

            // build an entity for the quest item with the WorldItemBuilder
            var questItemObject = questItemValue.getInternalValue();
            var worldEntity = WorldItemBuilder.buildWorldItem((Item) questItemObject);

            // add the world entity to the SetValue passed to this function
            var worldEntityValue =
                    (Value)
                            rtEnv.translateRuntimeObject(
                                    worldEntity, interpreter.getCurrentMemorySpace());
            entitySetValue.addValue(worldEntityValue);

            return null;
        }

        @Override
        public ICallable.Type getCallableType() {
            return ICallable.Type.Native;
        }
    }

    /**
     * Native function to create a {@link QuestItem} from an item prototype; will link a passed task
     * content automatically to the internal {@link task.components.TaskContentComponent} of the
     * newly created {@link QuestItem}.
     */
    private class NativeBuildQuestItem extends NativeFunction {
        /**
         * Constructor
         *
         * @param parentScope parent scope of this function
         * @param questItemType {@link IType} representing quest items
         * @param contentType {@link IType} representing task content
         */
        private NativeBuildQuestItem(IScope parentScope, IType questItemType, IType contentType) {
            super(
                    "build_quest_item",
                    parentScope,
                    new FunctionType(questItemType, Prototype.ITEM_PROTOTYPE, contentType));
        }

        @Override
        public Object call(DSLInterpreter interpreter, List<Node> parameters) {
            assert parameters != null && parameters.size() > 0;

            // evaluate parameters
            RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
            Value prototypeValue = (Value) parameters.get(0).accept(interpreter);
            Value contentValue = (Value) parameters.get(1).accept(interpreter);

            // check for correct parameter type
            if (prototypeValue.getDataType() != Prototype.ITEM_PROTOTYPE) {
                throw new RuntimeException(
                        "Wrong type ('"
                                + prototypeValue.getDataType().getName()
                                + "') of parameter for call of build_quest_item()!");
            } else {
                // instantiate new QuestItem from passed item prototype
                var dslItemInstance =
                        (AggregateValue)
                                interpreter.instantiateDSLValue((Prototype) prototypeValue);
                var questItemType = (AggregateType) rtEnv.getGlobalScope().resolve("quest_item");
                var questItemObject =
                        (QuestItem)
                                interpreter.instantiateRuntimeValue(dslItemInstance, questItemType);

                // link the passed TaskContent to the newly created QuestItem
                var contentObject = (TaskContent) contentValue.getInternalValue();
                questItemObject.taskContentComponent().content(contentObject);

                return questItemObject;
            }
        }

        @Override
        public ICallable.Type getCallableType() {
            return ICallable.Type.Native;
        }
    }

    // endregion

    // region native grading functions

    private static class SingleChoiceGrading extends NativeFunction {
        private BiFunction<Task, Set<TaskContent>, Float> func;

        /**
         * Constructor
         *
         * @param parentScope parent scope of this function
         */
        public SingleChoiceGrading(IScope parentScope, IType taskType, IType taskContentSetType) {
            super(
                    "grade_single_choice_task",
                    parentScope,
                    new FunctionType(BuiltInType.floatType, taskType, taskContentSetType));
            this.func = GradingFunctions.singleChoiceGrading();
        }

        @Override
        public Object call(DSLInterpreter interpreter, List<Node> parameters) {
            assert parameters != null && parameters.size() > 0;

            var parameterValues = interpreter.evaluateNodes(parameters);
            var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
            Task task = (Task) parameterObjects.get(0);
            Set<TaskContent> taskContentSet = (Set<TaskContent>) parameterObjects.get(1);

            // call func
            return func.apply(task, taskContentSet);
        }

        @Override
        public ICallable.Type getCallableType() {
            return ICallable.Type.Native;
        }
    }

    private static class MultipleChoiceGrading extends NativeFunction {
        private BiFunction<Task, Set<TaskContent>, Float> func;

        /**
         * Constructor
         *
         * @param parentScope parent scope of this function
         */
        public MultipleChoiceGrading(IScope parentScope, IType taskType, IType taskContentSetType) {
            super(
                    "grade_multiple_choice_task",
                    parentScope,
                    new FunctionType(BuiltInType.floatType, taskType, taskContentSetType));
            this.func = GradingFunctions.multipeChoiceGrading();
        }

        @Override
        public Object call(DSLInterpreter interpreter, List<Node> parameters) {
            assert parameters != null && parameters.size() > 0;

            var parameterValues = interpreter.evaluateNodes(parameters);
            var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
            Task task = (Task) parameterObjects.get(0);
            Set<TaskContent> taskContentSet = (Set<TaskContent>) parameterObjects.get(1);

            // call func
            return func.apply(task, taskContentSet);
        }

        @Override
        public ICallable.Type getCallableType() {
            return ICallable.Type.Native;
        }
    }

    // endregion
}
