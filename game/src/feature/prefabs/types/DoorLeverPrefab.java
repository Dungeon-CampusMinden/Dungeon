package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.level.elements.ILevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.entities.LeverFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.utils.IEntityCommand;
import java.util.List;
import java.util.Optional;

/** Server-side prefab for a lever controlling a door. */
public final class DoorLeverPrefab extends Prefab {

  private static final PrefabProperty<Point> LEVER_POSITION =
      PrefabProperty.point(
          "leverPosition", "Lever Position", new Point(1, 0), new Point(0.5f, 0.5f));
  private static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point("doorPosition", "Door Position", new Point(0, 0), new Point(0.5f, 0.5f));
  private static final PrefabProperty<Boolean> OPEN = PrefabProperty.bool("open", "Open", false);
  private static final PrefabProperty<Boolean> REVERTABLE =
      PrefabProperty.bool("revertable", "Revertable", true);

  /** Creates the door-lever definition. */
  public DoorLeverPrefab() {
    super(
        "door-lever",
        "Door + Lever",
        PrefabSide.SERVER,
        List.of(LEVER_POSITION, DOOR_POSITION, OPEN, REVERTABLE));
  }

  /**
   * Creates a bound view for one authored door-lever instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DoorLeverPrefab(ILevel level, String name) {
    super(
        "door-lever",
        "Door + Lever",
        PrefabSide.SERVER,
        List.of(LEVER_POSITION, DOOR_POSITION, OPEN, REVERTABLE),
        level,
        name);
  }

  /**
   * Returns the lever entity for this instance when it is currently spawned.
   *
   * @return the currently live lever entity, if any
   */
  public Optional<Entity> leverEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    boolean open = value(instance, OPEN);
    boolean revertable = value(instance, REVERTABLE);
    setDoor(context.level(), doorPosition, open);

    IEntityCommand command =
        new IEntityCommand() {
          @Override
          public void execute(Entity lever) {
            setDoor(context.level(), doorPosition, !open);
          }

          @Override
          public void undo(Entity lever) {
            if (revertable) setDoor(context.level(), doorPosition, open);
          }
        };
    Entity lever = context.createEntity(instance.name());
    LeverFactory.createLever(lever, value(instance, LEVER_POSITION), command, !revertable);
    return List.of(lever);
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
    Point lever = feedbackPoint(instance, LEVER_POSITION);
    Point door = feedbackPoint(instance, DOOR_POSITION);
    feedback.point(lever, instance.name());
    boolean hasDoor = doorAt(level, value(instance, DOOR_POSITION)).isPresent();
    feedback.point(door, hasDoor ? null : "Missing door");
    feedback.line(lever, door, true, hasDoor ? null : Color.RED);
  }

  private static void setDoor(ILevel level, Point position, boolean open) {
    doorAt(level, position)
        .ifPresent(
            door -> {
              if (open) door.open();
              else door.close();
            });
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level.tileAt(position).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }

  private Point feedbackPoint(PrefabInstance instance, PrefabProperty<Point> property) {
    Point point = value(instance, property);
    Point offset = property.editorFeedbackOffset();
    return point.translate(offset.x(), offset.y());
  }
}
