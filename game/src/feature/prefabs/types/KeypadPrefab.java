package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ArtifactType;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.tasks.TaskComponent;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server-side keypad that raises an event when a code is entered. Link {@link #CORRECT_CODE} to
 * e.g. {@link DoorControlPrefab#OPEN} to open a door with it.
 */
public final class KeypadPrefab extends Prefab {

  private static final String TYPE = "keypad";
  private static final String DISPLAY_NAME = "Keypad";

  /** Raised when a player entered the correct code, with the player as payload. */
  public static final PrefabEvent<Entity> CORRECT_CODE =
      PrefabEvent.withPlayer("correct-code", "Correct Code");

  /** Raised when a player submitted a wrong code, with the player as payload. */
  public static final PrefabEvent<Entity> WRONG_CODE =
      PrefabEvent.withPlayer("wrong-code", "Wrong Code");

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));
  public static final PrefabProperty<String> CODE =
      PrefabProperty.string("code", "Code", "1234", KeypadPrefab::isDigits);
  public static final PrefabProperty<Boolean> SHOW_DIGIT_COUNT =
      PrefabProperty.bool("showDigitCount", "Show Digit Count", true);
  public static final PrefabProperty<Boolean> CLEAR_ON_WRONG =
      PrefabProperty.bool("clearOnWrong", "Clear On Wrong Code", true);

  /** Takes the code from another prefab instead of the {@link #CODE} property while linked. */
  public static final ArtifactSlot<String> CODE_INPUT =
      ArtifactSlot.forProperty(CODE, ArtifactType.TEXT)
          .validatedBy(KeypadPrefab::isDigits, "only digits");

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, CODE, SHOW_DIGIT_COUNT, CLEAR_ON_WRONG);

  /** Creates the keypad definition. */
  public KeypadPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored keypad instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public KeypadPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the keypad entity for this instance when it is currently spawned.
   *
   * <p>This is empty before server-side spawn, after despawn or deletion, and while running on a
   * side where server entities are not present. A later respawn is resolved automatically.
   *
   * @return the currently live keypad entity, if any
   */
  public Optional<Entity> keypadEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(CORRECT_CODE, WRONG_CODE, CODE_INPUT);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Supplier<Optional<String>> code = linkableValue(context, instance, CODE);
    Entity keypad = context.createEntity(instance.name());
    KeypadFactory.createKeypad(
        keypad,
        value(instance, POSITION),
        () -> code.get().orElse(""),
        value(instance, SHOW_DIGIT_COUNT));
    keypad.fetch(KeypadComponent.class).orElseThrow().clearOnWrong(value(instance, CLEAR_ON_WRONG));
    TaskComponent<?> task = keypad.fetch(TaskComponent.class).orElseThrow();
    task.onCorrect(who -> fire(context, instance, CORRECT_CODE, who));
    task.onWrong(who -> fire(context, instance, WRONG_CODE, who));
    return List.of(keypad);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    feedback.point(position.translate(offset.x(), offset.y()), instance.name());
  }

  private static boolean isDigits(String value) {
    return !value.isBlank()
        && value.chars().allMatch(character -> character >= '0' && character <= '9');
  }
}
