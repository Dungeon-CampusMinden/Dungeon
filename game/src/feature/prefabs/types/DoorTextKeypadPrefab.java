package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.level.elements.ILevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.interaction.keypad.KeypadFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Prefab that closes a door and opens it when an accepted text keypad answer is entered. */
public final class DoorTextKeypadPrefab extends Prefab {

  public static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point("doorPosition", "Door Position", new Point(0, 0), new Point(0.5f, 0.5f));
  public static final PrefabProperty<Point> KEYPAD_POSITION =
      PrefabProperty.point(
          "keypadPosition", "Keypad Position", new Point(1, 0), new Point(0.5f, 0.5f));
  public static final PrefabProperty<String> ACCEPTED_ANSWERS =
      PrefabProperty.string(
          "acceptedAnswers",
          "Answers ('|' separated)",
          "OPEN",
          DoorTextKeypadPrefab::hasValidAcceptedAnswers);

  /** Creates the door-text-keypad definition. */
  public DoorTextKeypadPrefab() {
    super(
        "door-text-keypad",
        "Door + Text Keypad",
        PrefabSide.SERVER,
        List.of(KEYPAD_POSITION, DOOR_POSITION, ACCEPTED_ANSWERS));
  }

  /**
   * Creates a bound view for one authored door-text-keypad instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DoorTextKeypadPrefab(ILevel level, String name) {
    super(
        "door-text-keypad",
        "Door + Text Keypad",
        PrefabSide.SERVER,
        List.of(KEYPAD_POSITION, DOOR_POSITION, ACCEPTED_ANSWERS),
        level,
        name);
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
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    doorAt(context.level(), doorPosition).ifPresent(DoorTile::close);
    Entity keypad =
        KeypadFactory.createTextKeypad(
            value(instance, KEYPAD_POSITION),
            acceptedAnswers(value(instance, ACCEPTED_ANSWERS)),
            () -> doorAt(context.level(), doorPosition).ifPresent(DoorTile::open));
    return List.of(keypad);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    if (!DoorPrefabSupport.hasOtherTarget(context.level(), doorPosition, instance)) {
      doorAt(context.level(), doorPosition).ifPresent(DoorTile::open);
    }
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point keypad = editorFeedbackPoint(instance, KEYPAD_POSITION);
    Point door = editorFeedbackPoint(instance, DOOR_POSITION);
    feedback.point(keypad, instance.name());
    Color lineColor = doorAt(level, value(instance, DOOR_POSITION)).isPresent() ? null : Color.RED;
    feedback.line(keypad, door, true, lineColor);
  }

  private static boolean hasValidAcceptedAnswers(String value) {
    return !value.isBlank()
        && Arrays.stream(value.split("\\|", -1))
            .allMatch(answer -> !answer.isBlank() && answer.matches("[A-Za-z ]+"));
  }

  private static List<String> acceptedAnswers(String value) {
    return Arrays.stream(value.split("\\|", -1)).map(String::strip).toList();
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level.tileAt(position).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }

  private Point editorFeedbackPoint(PrefabInstance instance, PrefabProperty<Point> property) {
    Point point = value(instance, property);
    Point offset = property.editorFeedbackOffset();
    return point.translate(offset.x(), offset.y());
  }
}
