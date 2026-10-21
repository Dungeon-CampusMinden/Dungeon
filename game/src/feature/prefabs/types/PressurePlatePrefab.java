package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.entities.LeverFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.utils.ICommand;
import java.util.List;
import java.util.Optional;

/**
 * Server-side pressure plate that raises an event when it is pressed or released. Link {@link
 * #PRESSED} and {@link #RELEASED} to e.g. {@link DoorControlPrefab#OPEN} and {@link
 * DoorControlPrefab#CLOSE} to keep a door open while the plate is pressed.
 */
public final class PressurePlatePrefab extends Prefab {

  private static final String TYPE = "pressure-plate";
  private static final String DISPLAY_NAME = "Pressure Plate";

  /** Raised when enough weight is on the plate. */
  public static final PrefabEvent<Void> PRESSED = PrefabEvent.signal("pressed", "Pressed");

  /** Raised when the weight on the plate dropped below the trigger mass. */
  public static final PrefabEvent<Void> RELEASED = PrefabEvent.signal("released", "Released");

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION);

  /** Creates the pressure-plate definition. */
  public PressurePlatePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored pressure plate.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public PressurePlatePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
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
  protected List<PrefabPort> declaredPorts() {
    return List.of(PRESSED, RELEASED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity plate = context.createEntity(instance.name());
    LeverFactory.pressurePlate(
        plate,
        value(instance, POSITION),
        1.0f,
        new ICommand() {
          @Override
          public void execute() {
            fire(context, instance, PRESSED, null);
          }

          @Override
          public void undo() {
            fire(context, instance, RELEASED, null);
          }
        });
    return List.of(plate);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    feedback.point(position.translate(offset.x(), offset.y()), instance.name());
  }
}
