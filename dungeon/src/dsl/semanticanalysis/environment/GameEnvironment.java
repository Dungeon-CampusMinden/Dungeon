package dsl.semanticanalysis.environment;

import contrib.components.*;
import contrib.entities.EntityFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.dialogs.OkDialog;
import contrib.item.Item;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import dsl.annotation.DSLExtensionMethod;
import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.interop.RuntimeObjectTranslator;
import dsl.runtime.value.*;
import dsl.runtime.value.PrototypeValue;
import dsl.semanticanalysis.*;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import dslinterop.dslnativefunction.NativeInstantiate;
import dslinterop.dslnativefunction.NativeInstantiateNamed;
import dslinterop.dslnativefunction.NativePrint;
import dslinterop.dsltypeadapters.AIComponentAdapter;
import dslinterop.dsltypeadapters.DrawComponentAdapter;
import dslinterop.dsltypeadapters.QuestItemAdapter;
import dslinterop.dsltypeproperties.EntityExtension;
import dslinterop.dsltypeproperties.QuestItemExtension;
import dslinterop.nativescenariobuilder.NativeScenarioBuilder;
import entrypoint.DungeonConfig;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import task.*;
import task.dslinterop.*;
import task.game.components.TaskComponent;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;
import task.game.hud.QuizUI;
import task.game.hud.YesNoDialog;
import task.reporting.AnswerPickingFunctions;
import task.reporting.GradingFunctions;
import task.tasktype.AssignTask;
import task.tasktype.Element;
import task.tasktype.Quiz;

/** WTF? . */
public class GameEnvironment implements IEnvironment {
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

  /**
   * WTF? .
   *
   * @return foo
   */
  public Class<?>[] getBuiltInAggregateTypeClasses() {
    return (Class<?>[])
        new Class[] {
          DungeonConfig.class,
          Entity.class,
          QuestItem.class,
          PositionComponent.class,
          VelocityComponent.class,
          HealthComponent.class,
          AIComponent.class,
          CollideComponent.class,
          DrawComponent.class,
          TaskComponent.class,
          TaskContentComponent.class,
          InventoryComponent.class,
          InteractionComponent.class,
          Task.class,
          TaskContent.class,
          Quiz.Content.class,
          Element.class,
          Tile.Direction.class
        };
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public List<IDSLExtensionProperty<?, ?>> getBuiltInProperties() {
    ArrayList<IDSLExtensionProperty<?, ?>> properties = new ArrayList<>();
    properties.add(DSLSingleChoice.SingleChoiceDescriptionProperty.instance);

    properties.add(EntityExtension.VelocityComponentProperty.instance);
    properties.add(EntityExtension.PositionComponentProperty.instance);
    properties.add(EntityExtension.DrawComponentProperty.instance);
    properties.add(EntityExtension.TaskComponentProperty.instance);
    properties.add(EntityExtension.TaskContentComponentProperty.instance);
    properties.add(EntityExtension.InventoryComponentProperty.instance);
    properties.add(EntityExtension.InteractionComponentProperty.instance);
    properties.add(TaskComponent.TaskProperty.instance);
    properties.add(QuestItemExtension.TaskContentComponentProperty.instance);
    properties.add(TaskContentComponent.ContentProperty.instance);

    return properties;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public List<IDSLExtensionMethod<?, ?>> getBuiltInMethods() {
    ArrayList<IDSLExtensionMethod<?, ?>> methods = new ArrayList<>();

    methods.add(DSLSingleChoice.GetContentMethod.instance);
    methods.add(DSLSingleChoice.SetScenarioText.instance);
    methods.add(DSLSingleChoice.SingleChoiceSetGradingFunction.instance);
    methods.add(DSLSingleChoice.SingleChoiceSetAnswerPickerFunction.instance);

    methods.add(DSLMultipleChoice.GetContentMethod.instance);
    methods.add(DSLMultipleChoice.SetScenarioText.instance);
    methods.add(DSLMultipleChoice.MultipleChoiceSetGradingFunction.instance);
    methods.add(DSLMultipleChoice.MultipleChoiceSetAnswerPickerFunction.instance);

    methods.add(DSLAssignTask.GetSolutionMethod.instance);
    methods.add(DSLAssignTask.SetScenarioText.instance);
    methods.add(DSLAssignTask.AssignTaskSetGradingFunction.instance);
    methods.add(DSLAssignTask.AssignTaskSetAnswerPickerFunction.instance);

    methods.add(IsElementEmptyMethod.instance);
    methods.add(IsTaskActiveMethod.instance);
    methods.add(ElementContentMethod.instance);
    methods.add(QuizContentContentMethod.instance);
    methods.add(TaskContentContentMethod.instance);
    methods.add(EntityExtension.OpenInventoryMethod.instance);
    methods.add(EntityExtension.AddNamedTaskContentMethod.instance);
    methods.add(EntityExtension.AddTaskContentMethod.instance);
    methods.add(EntityExtension.AddItemToInventoryMethod.instance);
    methods.add(EntityExtension.DropItemsMethod.instance);

    return methods;
  }

  @Override
  public TypeBuilder getTypeBuilder() {
    return typeBuilder;
  }

  /**
   * Constructor. Creates fresh global scope and symbol table and binds built in types and native
   * functions.
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
    typeBuilder.registerTypeAdapter(AIComponentAdapter.class, this.globalScope);

    typeBuilder.registerTypeAdapter(DSLSingleChoice.class, this.globalScope);
    typeBuilder.registerTypeAdapter(DSLMultipleChoice.class, this.globalScope);
    typeBuilder.registerTypeAdapter(DSLAssignTask.class, this.globalScope);
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
            "A type with the name '" + type.getName() + "' is already loaded in the environment!");
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
    for (IDSLExtensionProperty<?, ?> property : getBuiltInProperties()) {
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
    this.globalScope.bind(PrototypeValue.PROTOTYPE);
    this.globalScope.bind(PrototypeValue.ITEM_PROTOTYPE);
  }

  protected ArrayList<Symbol> buildDependantNativeFunctions() {
    ArrayList<Symbol> nativeFunctions = new ArrayList<>();
    nativeFunctions.add(ShowInfoFunction.func);

    // build functions with dependency on specific non-builtin types
    IType questItemType = (IType) this.globalScope.resolve("quest_item");
    IType entityType = (IType) this.globalScope.resolve("entity");
    SetType entitySetType = new SetType(entityType, this.globalScope);
    this.globalScope.bind(entitySetType);

    NativeFunction placeQuestItem =
        new NativePlaceQuestItem(Scope.NULL, questItemType, entitySetType);
    nativeFunctions.add(placeQuestItem);

    NativeFunction addFillerContent = new GenerateRandomFillerContent(Scope.NULL, entityType);
    nativeFunctions.add(addFillerContent);

    IType taskContentType = (IType) this.globalScope.resolve("task_content");
    NativeFunction nativeBuildQuestItem =
        new NativeBuildQuestItem(Scope.NULL, questItemType, taskContentType);
    nativeFunctions.add(nativeBuildQuestItem);

    var taskSymbol = this.globalScope.resolve("task");
    if (!taskSymbol.equals(Symbol.NULL)) {
      IType taskType = (IType) taskSymbol;
      IType taskContentSetType = new SetType(taskContentType, this.globalScope);

      NativeFunction showYesNoTask = new AskYesNoDialogFunction(this.globalScope, taskType);
      nativeFunctions.add(showYesNoTask);

      NativeFunction showTaskOnUI = new ShowQuizOnUI(this.globalScope, taskType);
      nativeFunctions.add(showTaskOnUI);

      // grading functions
      NativeFunction nativeGradeSingleChoice =
          new SingleChoiceGrading(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(nativeGradeSingleChoice);

      NativeFunction nativeGradeMultipleChoice =
          new MultipleChoiceGrading(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(nativeGradeMultipleChoice);

      NativeFunction nativeGradeAssignEasy =
          new AssignmentGradingEasy(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(nativeGradeAssignEasy);

      NativeFunction nativeGradeAssignHard =
          new AssignmentGradingHard(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(nativeGradeAssignHard);

      // answer picker functions
      NativeFunction answerPickerSingleChest =
          new AnswerPickerSingleChest(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(answerPickerSingleChest);

      NativeFunction answerPickerMultiChest =
          new AnswerPickerMultiChest(this.globalScope, taskType, taskContentSetType);
      nativeFunctions.add(answerPickerMultiChest);
    }

    // native scenario builder functions
    var entitySetSetSymbol = this.globalScope.resolve("entity<><>");
    SetType entitySetSetType;
    if (entitySetSetSymbol.equals(Symbol.NULL)) {
      entitySetSetType = new SetType(entitySetType, this.globalScope);
      this.globalScope.bind(entitySetSetType);
    } else {
      entitySetSetType = (SetType) entitySetSetSymbol;
    }
    var singleChoiceSymbol = this.globalScope.resolve("single_choice_task");
    if (!singleChoiceSymbol.equals(Symbol.NULL)) {
      IType singleChoiceTaskType = (IType) singleChoiceSymbol;
      NativeFunction singleChoiceScenarioBuilderFunc =
          new SingleChoiceNativeScenarioBuilder(
              this.globalScope, singleChoiceTaskType, entitySetSetType);
      nativeFunctions.add(singleChoiceScenarioBuilderFunc);
    }

    var multipleChoiceSymbol = this.globalScope.resolve("multiple_choice_task");
    if (!multipleChoiceSymbol.equals(Symbol.NULL)) {
      IType multipleChoiceTaskType = (IType) multipleChoiceSymbol;
      NativeFunction multipleChoiceScenarioBuilderFunc =
          new MultipleChoiceNativeScenarioBuilder(
              this.globalScope, multipleChoiceTaskType, entitySetSetType);
      nativeFunctions.add(multipleChoiceScenarioBuilderFunc);
    }

    var assignSymbol = this.globalScope.resolve("assign_task");
    if (!assignSymbol.equals(Symbol.NULL)) {
      IType assignTaskType = (IType) assignSymbol;
      NativeFunction assignTaskNativeScenarioBuilder =
          new AssignTaskNativeScenarioBuilder(this.globalScope, assignTaskType, entitySetSetType);
      nativeFunctions.add(assignTaskNativeScenarioBuilder);
    }

    return nativeFunctions;
  }

  protected ArrayList<Symbol> buildNativeFunctions() {
    ArrayList<Symbol> nativeFunctions = new ArrayList<>();
    nativeFunctions.add(NativePrint.func);
    nativeFunctions.add(NativeInstantiate.func);
    nativeFunctions.add(NativeInstantiateNamed.func);

    var dependantNativeFunctions = buildDependantNativeFunctions();
    nativeFunctions.addAll(dependantNativeFunctions);
    return nativeFunctions;
  }

  // region native functions with dependency on specific types

  /** Native function to place a quest item in a "room" (which is represented by an entity set). */
  private static class NativePlaceQuestItem extends NativeFunction {
    /**
     * Constructor.
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
          (Value) rtEnv.translateRuntimeObject(worldEntity, interpreter.getCurrentMemorySpace());
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
   * content automatically to the internal {@link TaskContentComponent} of the newly created {@link
   * QuestItem}.
   */
  private class NativeBuildQuestItem extends NativeFunction {
    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param questItemType {@link IType} representing quest items
     * @param contentType {@link IType} representing task content
     */
    private NativeBuildQuestItem(IScope parentScope, IType questItemType, IType contentType) {
      super(
          "build_quest_item",
          parentScope,
          new FunctionType(questItemType, PrototypeValue.ITEM_PROTOTYPE, contentType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      // evaluate parameters
      RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
      Value prototypeValue = (Value) parameters.get(0).accept(interpreter);
      Value contentValue = (Value) parameters.get(1).accept(interpreter);

      // check for correct parameter type
      if (prototypeValue.getDataType() != PrototypeValue.ITEM_PROTOTYPE) {
        throw new RuntimeException(
            "Wrong type ('"
                + prototypeValue.getDataType().getName()
                + "') of parameter for call of build_quest_item()!");
      } else {
        // instantiate new QuestItem from passed item prototype
        var dslItemInstance =
            (AggregateValue) interpreter.instantiateDSLValue((PrototypeValue) prototypeValue);
        var questItemType = (AggregateType) rtEnv.getGlobalScope().resolve("quest_item");
        var questItemObject =
            (QuestItem) interpreter.instantiateRuntimeValue(dslItemInstance, questItemType);

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

  /** WTF? . */
  @DSLExtensionMethod(name = "is_empty", extendedType = Element.class)
  public static class IsElementEmptyMethod implements IDSLExtensionMethod<Element, Boolean> {
    /** WTF? . */
    public static IsElementEmptyMethod instance = new IsElementEmptyMethod();

    @Override
    public Boolean call(Element instance, List<Object> params) {
      if (instance.equals(AssignTask.EMPTY_ELEMENT)
          || instance.content() == null
          || instance.content().equals("")) {
        return true;
      }
      return false;
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  /** WTF? . */
  @DSLExtensionMethod(name = "text", extendedType = Quiz.Content.class)
  public static class QuizContentContentMethod
      implements IDSLExtensionMethod<Quiz.Content, String> {
    /** WTF? . */
    public static QuizContentContentMethod instance = new QuizContentContentMethod();

    @Override
    public String call(Quiz.Content instance, List<Object> params) {
      return instance.content();
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  /** WTF? . */
  @DSLExtensionMethod(name = "text", extendedType = Element.class)
  public static class ElementContentMethod implements IDSLExtensionMethod<Element, String> {
    /** WTF? . */
    public static ElementContentMethod instance = new ElementContentMethod();

    @Override
    public String call(Element instance, List<Object> params) {
      return instance.content().toString();
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  /** WTF? . */
  @DSLExtensionMethod(name = "text", extendedType = TaskContent.class)
  public static class TaskContentContentMethod implements IDSLExtensionMethod<TaskContent, String> {
    /** WTF? . */
    public static TaskContentContentMethod instance = new TaskContentContentMethod();

    @Override
    public String call(TaskContent instance, List<Object> params) {
      if (instance instanceof Quiz.Content quizcontent) {
        return quizcontent.content();
      } else if (instance instanceof Element element) {
        return element.content().toString();
      } else {
        return "undefined TaskContent";
      }
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  /** WTF? . */
  @DSLExtensionMethod(name = "is_active", extendedType = Task.class)
  public static class IsTaskActiveMethod implements IDSLExtensionMethod<Task, Boolean> {
    /** WTF? . */
    public static IsTaskActiveMethod instance = new IsTaskActiveMethod();

    @Override
    public Boolean call(Task instance, List<Object> params) {
      return instance.state().equals(Task.TaskState.ACTIVE)
          || instance.state().equals(Task.TaskState.PROCESSING_ACTIVE);
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {};
      return Arrays.stream(arr).toList();
    }
  }

  private static class AskYesNoDialogFunction extends NativeFunction {
    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     */
    public AskYesNoDialogFunction(IScope parentScope, IType taskType) {
      super("ask_task_yes_no", parentScope, new FunctionType(BuiltInType.noType, taskType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Task task = (Task) parameterObjects.get(0);

      YesNoDialog.showYesNoDialog(task);
      return null;
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class ShowQuizOnUI extends NativeFunction {
    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     */
    public ShowQuizOnUI(IScope parentScope, IType taskType) {
      super("show_task_on_ui", parentScope, new FunctionType(BuiltInType.noType, taskType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Quiz task = (Quiz) parameterObjects.get(0);

      QuizUI.askQuizOnHud(task);
      return null;
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class ShowInfoFunction extends NativeFunction {
    public static ShowInfoFunction func = new ShowInfoFunction(Scope.NULL);

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     */
    private ShowInfoFunction(IScope parentScope) {
      super("show_info", parentScope, new FunctionType(BuiltInType.noType, BuiltInType.stringType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      String text = (String) parameterObjects.get(0);

      OkDialog.showOkDialog(text, "Info", () -> {});
      return null;
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
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
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
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
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

  private static class AssignmentGradingEasy extends NativeFunction {
    private BiFunction<Task, Set<TaskContent>, Float> func;

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
     */
    public AssignmentGradingEasy(IScope parentScope, IType taskType, IType taskContentSetType) {
      super(
          "grade_assign_task_easy",
          parentScope,
          new FunctionType(BuiltInType.floatType, taskType, taskContentSetType));
      this.func = GradingFunctions.assignGradingEasy();
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

  private static class AssignmentGradingHard extends NativeFunction {
    private BiFunction<Task, Set<TaskContent>, Float> func;

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
     */
    public AssignmentGradingHard(IScope parentScope, IType taskType, IType taskContentSetType) {
      super(
          "grade_assign_task_hard",
          parentScope,
          new FunctionType(BuiltInType.floatType, taskType, taskContentSetType));
      this.func = GradingFunctions.assignGradingHard();
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

  private static class GenerateRandomFillerContent extends NativeFunction {

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param entityType foo
     */
    public GenerateRandomFillerContent(IScope parentScope, IType entityType) {
      super("get_random_content", parentScope, new FunctionType(entityType, BuiltInType.noType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      Random rand = new Random();
      int randVal = rand.nextInt();
      Entity randomContent = null;
      try {
        if (randVal % 2 == 0) {
          randomContent = EntityFactory.randomMonster();
        } else {
          randomContent = EntityFactory.newChest();
        }
      } catch (Exception ex) {
        //
      }

      return randomContent;
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  // endregion

  // region native answer picking
  private static class AnswerPickerSingleChest extends NativeFunction {
    private Function<Task, Set<TaskContent>> func;

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
     */
    public AnswerPickerSingleChest(IScope parentScope, IType taskType, IType taskContentSetType) {
      super(
          "answer_picker_single_chest",
          parentScope,
          new FunctionType(taskContentSetType, taskType));
      this.func = AnswerPickingFunctions.singleChestPicker();
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Task task = (Task) parameterObjects.get(0);

      // call func
      return func.apply(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class AnswerPickerMultiChest extends NativeFunction {
    private Function<Task, Set<TaskContent>> func;

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
     */
    public AnswerPickerMultiChest(IScope parentScope, IType taskType, IType taskContentSetType) {
      super(
          "answer_picker_multi_chest", parentScope, new FunctionType(taskContentSetType, taskType));
      this.func = AnswerPickingFunctions.multipleChestPicker();
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Task task = (Task) parameterObjects.get(0);

      // call func
      return func.apply(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class HeroInventoryPicker extends NativeFunction {
    private Function<Task, Set<TaskContent>> func;

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param taskType foo
     * @param taskContentSetType foo
     */
    public HeroInventoryPicker(IScope parentScope, IType taskType, IType taskContentSetType) {
      super(
          "answer_picker_hero_inventory",
          parentScope,
          new FunctionType(taskContentSetType, taskType));
      this.func = AnswerPickingFunctions.heroInventoryPicker();
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Task task = (Task) parameterObjects.get(0);

      // call func
      return func.apply(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  // endregion

  // region native scenario builder function
  private static class SingleChoiceNativeScenarioBuilder extends NativeFunction {

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param singleChoiceType foo
     * @param entitySetSetType foo
     */
    public SingleChoiceNativeScenarioBuilder(
        IScope parentScope, IType singleChoiceType, IType entitySetSetType) {
      super(
          "$default_single_choice_scenario$",
          parentScope,
          new FunctionType(entitySetSetType, singleChoiceType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Quiz task = (Quiz) parameterObjects.get(0);

      // call func
      return NativeScenarioBuilder.quizOnHud(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class MultipleChoiceNativeScenarioBuilder extends NativeFunction {

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param multipleChoiceType foo
     * @param entitySetSetType foo
     */
    public MultipleChoiceNativeScenarioBuilder(
        IScope parentScope, IType multipleChoiceType, IType entitySetSetType) {
      super(
          "$default_multiple_choice_scenario$",
          parentScope,
          new FunctionType(entitySetSetType, multipleChoiceType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      Quiz task = (Quiz) parameterObjects.get(0);

      // call func
      return NativeScenarioBuilder.quizOnHud(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }

  private static class AssignTaskNativeScenarioBuilder extends NativeFunction {

    /**
     * Constructor.
     *
     * @param parentScope parent scope of this function
     * @param assignTaskType foo
     * @param entitySetSetType foo
     */
    public AssignTaskNativeScenarioBuilder(
        IScope parentScope, IType assignTaskType, IType entitySetSetType) {
      super(
          "$default_assign_task_scenario$",
          parentScope,
          new FunctionType(entitySetSetType, assignTaskType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
      assert parameters != null && parameters.size() > 0;

      var parameterValues = interpreter.evaluateNodes(parameters);
      var parameterObjects = interpreter.translateValuesToObjects(parameterValues);
      AssignTask task = (AssignTask) parameterObjects.get(0);

      // call func
      return NativeScenarioBuilder.assignTaskA(task);
    }

    @Override
    public ICallable.Type getCallableType() {
      return ICallable.Type.Native;
    }
  }
  // endregion
}
