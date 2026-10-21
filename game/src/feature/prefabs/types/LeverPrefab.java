package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.LeverComponent;
import feature.entities.LeverFactory;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
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
 * Server-side lever that raises an event when it is switched. Link {@link #SWITCHED_ON} and {@link
 * #SWITCHED_OFF} to e.g. {@link DoorControlPrefab#OPEN} and {@link DoorControlPrefab#CLOSE} to
 * control a door with it.
 */
public final class LeverPrefab extends Prefab {

  private static final String TYPE = "lever";
  private static final String DISPLAY_NAME = "Lever";

  /** Raised when the lever was switched on. */
  public static final PrefabEvent<Void> SWITCHED_ON =
      PrefabEvent.signal("switched-on", "Switched On");

  /** Raised when the lever was switched off. */
  public static final PrefabEvent<Void> SWITCHED_OFF =
      PrefabEvent.signal("switched-off", "Switched Off");

  /** Switches the lever on. */
  public static final PrefabAction SWITCH_ON =
      PrefabAction.of("switch-on", "Switch On", context -> setOn(context, true));

  /** Switches the lever off. */
  public static final PrefabAction SWITCH_OFF =
      PrefabAction.of("switch-off", "Switch Off", context -> setOn(context, false));

  /** Switches the lever on if it is off, otherwise off. */
  public static final PrefabAction TOGGLE =
      PrefabAction.of(
          "toggle", "Toggle", context -> leverOf(context).ifPresent(LeverComponent::toggle));

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Whether players can switch the lever back off; otherwise it can be switched only once. */
  public static final PrefabProperty<Boolean> REVERTABLE =
      PrefabProperty.bool("revertable", "Revertable", true);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, REVERTABLE);

  /** Creates the lever definition. */
  public LeverPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored lever instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public LeverPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
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
  protected List<PrefabPort> declaredPorts() {
    return List.of(SWITCHED_ON, SWITCHED_OFF, SWITCH_ON, SWITCH_OFF, TOGGLE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity lever = context.createEntity(instance.name());
    LeverFactory.createLever(
        lever,
        value(instance, POSITION),
        new ICommand() {
          @Override
          public void execute() {
            fire(context, instance, SWITCHED_ON, null);
          }

          @Override
          public void undo() {
            fire(context, instance, SWITCHED_OFF, null);
          }
        },
        !value(instance, REVERTABLE));
    return List.of(lever);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    feedback.point(position.translate(offset.x(), offset.y()), instance.name());
  }

  private static void setOn(ActionContext context, boolean on) {
    leverOf(context).filter(lever -> lever.isOn() != on).ifPresent(LeverComponent::toggle);
  }

  private static Optional<LeverComponent> leverOf(ActionContext context) {
    return context.liveEntities().stream()
        .findFirst()
        .flatMap(lever -> lever.fetch(LeverComponent.class));
  }
}
