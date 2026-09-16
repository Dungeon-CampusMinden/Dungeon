package feature.prefabs.types;

import engine.Entity;
import engine.level.Tile;
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

/** Prefab that closes a door and opens it when the correct keypad code is entered. */
public final class DoorKeypadPrefab extends Prefab {

  private static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point("doorPosition", "Door Position", new Point(0, 0));
  private static final PrefabProperty<Point> KEYPAD_POSITION =
      PrefabProperty.point("keypadPosition", "Keypad Position", new Point(1, 0));
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
        List.of(DOOR_POSITION, KEYPAD_POSITION, CODE, SHOW_DIGIT_COUNT));
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    Tile tile =
        context
            .level()
            .tileAt(doorPosition)
            .orElseThrow(() -> new IllegalArgumentException("door position is outside the level"));
    if (!(tile instanceof DoorTile door)) {
      throw new IllegalArgumentException("door position does not contain a DoorTile");
    }

    door.close();
    List<Integer> digits =
        value(instance, CODE).chars().map(character -> character - '0').boxed().toList();
    Entity keypad = context.createEntity(instance.name() + " keypad");
    KeypadFactory.createKeypad(
        keypad,
        value(instance, KEYPAD_POSITION),
        digits,
        door::open,
        value(instance, SHOW_DIGIT_COUNT));
    return List.of(keypad);
  }

  @Override
  public void renderEditorFeedback(
      PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point keypad = value(instance, KEYPAD_POSITION);
    Point door = value(instance, DOOR_POSITION);
    feedback.point(keypad, "Keypad");
    feedback.point(door, "Door");
    feedback.line(keypad, door, true);
  }
}
