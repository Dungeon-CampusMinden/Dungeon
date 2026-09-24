package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.entities.LeverFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;

/** Server-side torch with configurable initial and interaction states. */
public final class TorchPrefab extends Prefab {

  private static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  private static final PrefabProperty<Boolean> ON = PrefabProperty.bool("on", "On", true);
  private static final PrefabProperty<Boolean> TOGGLEABLE =
      PrefabProperty.bool("toggleable", "Toggleable", true);

  /** Creates the torch definition. */
  public TorchPrefab() {
    super("torch", "Torch", PrefabSide.SERVER, List.of(POSITION, ON, TOGGLEABLE));
  }

  /**
   * Creates a bound view for one authored torch.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TorchPrefab(ILevel level, String name) {
    super("torch", "Torch", PrefabSide.SERVER, List.of(POSITION, ON, TOGGLEABLE), level, name);
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
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity torch = context.createEntity(instance.name());
    LeverFactory.createTorch(
        torch, value(instance, POSITION), value(instance, ON), value(instance, TOGGLEABLE));
    return List.of(torch);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
  }
}
