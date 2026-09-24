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
import feature.utils.ICommand;
import java.util.List;
import java.util.Optional;

/** Server-side pressure plate that opens its target door while pressed. */
public final class PressurePlatePrefab extends Prefab {

  private static final PrefabProperty<Point> POSITION =
      PrefabProperty.point(
          "position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));
  private static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point(
          "doorPosition", "Door Position", new Point(1, 0), new Point(0.5f, 0.5f));

  /** Creates the pressure-plate definition. */
  public PressurePlatePrefab() {
    super(
        "pressure-plate",
        "Pressure Plate + Door",
        PrefabSide.SERVER,
        List.of(POSITION, DOOR_POSITION));
  }

  /**
   * Creates a bound view for one authored pressure plate.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public PressurePlatePrefab(ILevel level, String name) {
    super(
        "pressure-plate",
        "Pressure Plate + Door",
        PrefabSide.SERVER,
        List.of(POSITION, DOOR_POSITION),
        level,
        name);
  }

  /**
   * Returns this instance's live plate entity, if spawned.
   *
   * @return the live plate entity, or empty when it is not spawned
   */
  public Optional<Entity> pressurePlateEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    doorAt(context.level(), doorPosition).ifPresent(DoorTile::close);

    Entity plate = context.createEntity(instance.name());
    LeverFactory.pressurePlate(
        plate,
        value(instance, POSITION),
        1.0f,
        new ICommand() {
          @Override
          public void execute() {
            doorAt(context.level(), doorPosition).ifPresent(DoorTile::open);
          }

          @Override
          public void undo() {
            doorAt(context.level(), doorPosition).ifPresent(DoorTile::close);
          }
        });
    return List.of(plate);
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
    Point position = feedbackPoint(instance, POSITION);
    Point doorPosition = feedbackPoint(instance, DOOR_POSITION);
    boolean hasDoor = doorAt(level, value(instance, DOOR_POSITION)).isPresent();
    feedback.point(position, instance.name());
    feedback.point(doorPosition, hasDoor ? null : "Missing door");
    feedback.line(position, doorPosition, true, hasDoor ? null : Color.RED);
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level
        .tileAt(position)
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast);
  }

  private Point feedbackPoint(PrefabInstance instance, PrefabProperty<Point> property) {
    Point point = value(instance, property);
    Point offset = property.editorFeedbackOffset();
    return point.translate(offset.x(), offset.y());
  }
}
