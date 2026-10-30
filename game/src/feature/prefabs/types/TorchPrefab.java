package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Server-side torch with configurable initial and interaction states. */
public final class TorchPrefab extends Prefab {

  /** Raised when the torch was lit. */
  public static final PrefabEvent<Void> LIT = PrefabEvent.signal("lit", "Lit");

  /** Raised when the torch was extinguished. */
  public static final PrefabEvent<Void> EXTINGUISHED =
      PrefabEvent.signal("extinguished", "Extinguished");

  /** Lights the torch. */
  public static final PrefabAction LIGHT =
      PrefabAction.of("light", "Light", context -> setLit(context, true));

  /** Extinguishes the torch. */
  public static final PrefabAction EXTINGUISH =
      PrefabAction.of("extinguish", "Extinguish", context -> setLit(context, false));

  /** Lights the torch if it is out, otherwise extinguishes it. */
  public static final PrefabAction TOGGLE =
      PrefabAction.of(
          "toggle",
          "Toggle",
          context ->
              context.liveEntities().stream()
                  .findFirst()
                  .flatMap(torch -> torch.fetch(LeverComponent.class))
                  .ifPresent(LeverComponent::toggle));

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  public static final PrefabProperty<Boolean> ON = PrefabProperty.bool("on", "On", true);
  public static final PrefabProperty<Vector2> SCALE =
      PrefabProperty.vector2("scale", "Scale", Vector2.ONE);
  public static final PrefabProperty<String> TOGGLEABLE =
      PrefabProperty.selection(
          "toggleable",
          "Toggleable",
          Toggleable.TOGGLEABLE.label,
          Arrays.stream(Toggleable.values()).map(value -> value.label).toList());

  /** Creates the torch definition. */
  public TorchPrefab() {
    super("torch", "Torch", PrefabSide.SERVER, List.of(POSITION, ON, TOGGLEABLE, SCALE));
  }

  /**
   * Creates a bound view for one authored torch.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TorchPrefab(ILevel level, String name) {
    super(
        "torch", "Torch", PrefabSide.SERVER, List.of(POSITION, ON, TOGGLEABLE, SCALE), level, name);
  }

  /**
   * Returns this instance's live torch entity, if spawned.
   *
   * @return the live torch entity, or empty when it is not spawned
   */
  public Optional<Entity> torchEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  protected void validate(PrefabInstance instance) {
    Vector2 scale = value(instance, SCALE);
    if (!Float.isFinite(scale.x())
        || !Float.isFinite(scale.y())
        || scale.x() <= 0
        || scale.y() <= 0) {
      throw new IllegalArgumentException(
          "Prefab property 'scale' must contain positive finite components");
    }
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(LIT, EXTINGUISHED, LIGHT, EXTINGUISH, TOGGLE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity torch = context.createEntity(instance.name());
    Toggleable toggleable = Toggleable.fromLabel(value(instance, TOGGLEABLE));
    LeverFactory.createTorch(
        torch,
        value(instance, POSITION),
        new ICommand() {
          @Override
          public void execute() {
            fire(context, instance, LIT, null);
          }

          @Override
          public void undo() {
            fire(context, instance, EXTINGUISHED, null);
          }
        },
        value(instance, ON),
        toggleable != Toggleable.NOT_TOGGLEABLE,
        toggleable == Toggleable.TOGGLEABLE_ONCE);
    torch.fetch(PositionComponent.class).orElseThrow().scale(value(instance, SCALE));
    return List.of(torch);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
  }

  private static void setLit(ActionContext context, boolean lit) {
    context.liveEntities().stream()
        .findFirst()
        .flatMap(torch -> torch.fetch(LeverComponent.class))
        .filter(lever -> lever.isOn() != lit)
        .ifPresent(LeverComponent::toggle);
  }

  /** Interaction modes of a torch. */
  private enum Toggleable {
    NOT_TOGGLEABLE("Not toggleable"),
    TOGGLEABLE("Toggleable"),
    TOGGLEABLE_ONCE("Toggleable Once");

    private final String label;

    Toggleable(String label) {
      this.label = label;
    }

    private static Toggleable fromLabel(String label) {
      for (Toggleable value : values()) {
        if (value.label.equals(label)) return value;
      }
      throw new IllegalArgumentException("Unknown torch toggle mode: " + label);
    }
  }
}
