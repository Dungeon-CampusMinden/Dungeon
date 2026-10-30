package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;

/**
 * Server-side control of a door tile that opens and closes it through linked events, e.g. when a
 * keypad code was entered.
 */
public final class DoorControlPrefab extends Prefab {

  private static final String TYPE = "door-control";
  private static final String DISPLAY_NAME = "Door Control";

  /** Position of the controlled door tile. */
  public static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point("doorPosition", "Door Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Whether the door is closed when the control is spawned. */
  public static final PrefabProperty<Boolean> CLOSED_AT_START =
      PrefabProperty.bool("closedAtStart", "Closed at Start", true);

  /** Raised when the door was opened by an action. */
  public static final PrefabEvent<Void> OPENED = PrefabEvent.signal("opened", "Opened");

  /** Raised when the door was closed by an action. */
  public static final PrefabEvent<Void> CLOSED = PrefabEvent.signal("closed", "Closed");

  /** Opens the door. */
  public static final PrefabAction OPEN =
      PrefabAction.of("open", "Open", context -> setOpen(context, true));

  /** Closes the door. */
  public static final PrefabAction CLOSE =
      PrefabAction.of("close", "Close", context -> setOpen(context, false));

  /** Opens the door if it is closed, otherwise closes it. */
  public static final PrefabAction TOGGLE =
      PrefabAction.of(
          "toggle",
          "Toggle",
          context ->
              doorAt(context.level(), context.value(DOOR_POSITION))
                  .ifPresent(door -> setOpen(context, !door.isOpen())));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(DOOR_POSITION, CLOSED_AT_START);

  /** Creates the door-control definition. */
  public DoorControlPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored door control.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DoorControlPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(OPEN, CLOSE, TOGGLE, OPENED, CLOSED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    doorAt(context.level(), value(instance, DOOR_POSITION))
        .ifPresent(
            door -> {
              if (value(instance, CLOSED_AT_START)) door.close();
              else door.open();
            });
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    if (!hasOtherControl(context.level(), doorPosition, instance)) {
      doorAt(context.level(), doorPosition).ifPresent(DoorTile::open);
    }
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, DOOR_POSITION);
    Point offset = DOOR_POSITION.editorFeedbackOffset();
    Point marker = position.translate(offset.x(), offset.y());
    boolean hasDoor = doorAt(level, position).isPresent();
    feedback.point(marker, hasDoor ? instance.name() : instance.name() + " (missing door)");
  }

  private static void setOpen(ActionContext context, boolean open) {
    Optional<DoorTile> door = doorAt(context.level(), context.value(DOOR_POSITION));
    if (door.isEmpty() || door.get().isOpen() == open) return;
    if (open) door.get().open();
    else door.get().close();
    fire(context.creationContext(), context.instance(), open ? OPENED : CLOSED, null);
  }

  /**
   * Checks whether another active door control targets the same door, so a despawning control does
   * not open a door that is still meant to be controlled.
   *
   * @param level the level to search for door controls
   * @param position the door position to check
   * @param removing the door control being removed
   * @return true if another active door control targets the door
   */
  private static boolean hasOtherControl(ILevel level, Point position, PrefabInstance removing) {
    for (PrefabInstance candidate : level.activePrefabs()) {
      if (!candidate.active() || !candidate.type().equals(TYPE)) continue;
      if (candidate.name().equals(removing.name())) continue;
      try {
        Prefab prefab = PrefabRegistry.require(TYPE);
        if (position.equals(prefab.normalize(candidate).get(DOOR_POSITION))) return true;
      } catch (IllegalArgumentException ignored) {
        // Invalid editor-time records cannot reliably claim a door.
      }
    }
    return false;
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level.tileAt(position).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }
}
