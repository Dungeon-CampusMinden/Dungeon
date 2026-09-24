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
import java.util.List;
import java.util.Optional;

/** Prefab that closes a door and opens it when the correct keypad code is entered. */
public final class DoorKeypadPrefab extends Prefab {

  private static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point(
          "doorPosition", "Door Position", new Point(0, 0), new Point(0.5f, 0.5f));
  private static final PrefabProperty<Point> KEYPAD_POSITION =
      PrefabProperty.point(
          "keypadPosition", "Keypad Position", new Point(1, 0), new Point(0.5f, 0.5f));
  private static final PrefabProperty<String> CODE =
      PrefabProperty.string(
          "code",
          "Code",
          "1234",
          value ->
              !value.isBlank()
                  && value.chars().allMatch(character -> character >= '0' && character <= '9'));
  private static final PrefabProperty<Boolean> SHOW_DIGIT_COUNT =
      PrefabProperty.bool("showDigitCount", "Show Digit Count", true);

  /** Creates the door-keypad definition. */
  public DoorKeypadPrefab() {
    super(
        "door-keypad",
        "Door + Keypad",
        PrefabSide.SERVER,
        List.of(KEYPAD_POSITION, DOOR_POSITION, CODE, SHOW_DIGIT_COUNT));
  }

  /**
   * Creates a bound view for one authored door-keypad instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DoorKeypadPrefab(ILevel level, String name) {
    super(
        "door-keypad",
        "Door + Keypad",
        PrefabSide.SERVER,
        List.of(KEYPAD_POSITION, DOOR_POSITION, CODE, SHOW_DIGIT_COUNT),
        level,
        name);
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
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    doorAt(context.level(), doorPosition).ifPresent(DoorTile::close);
    List<Integer> digits =
        value(instance, CODE).chars().map(character -> character - '0').boxed().toList();
    Entity keypad = context.createEntity(instance.name() + " keypad");
    KeypadFactory.createKeypad(
        keypad,
        value(instance, KEYPAD_POSITION),
        digits,
        () -> doorAt(context.level(), doorPosition).ifPresent(DoorTile::open),
        value(instance, SHOW_DIGIT_COUNT));
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
    Color lineColor =
        doorAt(level, value(instance, DOOR_POSITION)).isPresent() ? null : Color.RED;
    feedback.line(keypad, door, true, lineColor);
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level
        .tileAt(position)
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast);
  }

  private Point editorFeedbackPoint(
      PrefabInstance instance, PrefabProperty<Point> property) {
    Point point = value(instance, property);
    Point offset = property.editorFeedbackOffset();
    return point.translate(offset.x(), offset.y());
  }
}
