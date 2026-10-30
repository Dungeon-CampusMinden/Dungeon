package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.tasks.TaskComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Server-side text keypad that raises an event when an answer is entered. Link {@link
 * #CORRECT_ANSWER} to e.g. {@link DoorControlPrefab#OPEN} to open a door with it.
 */
public final class TextKeypadPrefab extends Prefab {

  private static final String TYPE = "text-keypad";
  private static final String DISPLAY_NAME = "Text Keypad";

  /** Raised when a player entered an accepted answer, with the player as payload. */
  public static final PrefabEvent<Entity> CORRECT_ANSWER =
      PrefabEvent.withPlayer("correct-answer", "Correct Answer");

  /** Raised when a player entered a wrong answer, with the player as payload. */
  public static final PrefabEvent<Entity> WRONG_ANSWER =
      PrefabEvent.withPlayer("wrong-answer", "Wrong Answer");

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));
  public static final PrefabProperty<String> ACCEPTED_ANSWERS =
      PrefabProperty.string(
          "acceptedAnswers",
          "Answers ('|' separated)",
          "OPEN",
          TextKeypadPrefab::hasValidAcceptedAnswers);

  public static final PrefabProperty<Boolean> CLEAR_ON_WRONG =
      PrefabProperty.bool("clearOnWrong", "Clear On Wrong Answer", true);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, ACCEPTED_ANSWERS, CLEAR_ON_WRONG);

  /** Creates the text keypad definition. */
  public TextKeypadPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored text keypad instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TextKeypadPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the text keypad entity for this instance when it is currently spawned.
   *
   * <p>This is empty before server-side spawn, after despawn or deletion, and while running on a
   * side where server entities are not present. A later respawn is resolved automatically.
   *
   * @return the currently live text keypad entity, if any
   */
  public Optional<Entity> textKeypadEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(CORRECT_ANSWER, WRONG_ANSWER);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity keypad =
        KeypadFactory.createTextKeypad(
            value(instance, POSITION), acceptedAnswers(value(instance, ACCEPTED_ANSWERS)));
    keypad.fetch(KeypadComponent.class).orElseThrow().clearOnWrong(value(instance, CLEAR_ON_WRONG));
    TaskComponent<?> task = keypad.fetch(TaskComponent.class).orElseThrow();
    task.onCorrect(who -> fire(context, instance, CORRECT_ANSWER, who));
    task.onWrong(who -> fire(context, instance, WRONG_ANSWER, who));
    return List.of(keypad);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    feedback.point(position.translate(offset.x(), offset.y()), instance.name());
  }

  private static boolean hasValidAcceptedAnswers(String value) {
    return !value.isBlank()
        && Arrays.stream(value.split("\\|", -1))
            .allMatch(answer -> !answer.isBlank() && answer.matches("[A-Za-z ]+"));
  }

  private static List<String> acceptedAnswers(String value) {
    return Arrays.stream(value.split("\\|", -1)).map(String::strip).toList();
  }
}
