package feature.prefabs.types;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;

/** Server-side pushable stone matching the mushroom-level push puzzle. */
public final class PushableStonePrefab extends Prefab {

  private static final PrefabProperty<Point> POSITION =
      PrefabProperty.point(
          "position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Creates the pushable-stone definition. */
  public PushableStonePrefab() {
    super("pushable-stone", "Pushable Stone", PrefabSide.SERVER, List.of(POSITION));
  }

  /**
   * Creates a bound view for one authored pushable stone.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public PushableStonePrefab(ILevel level, String name) {
    super(
        "pushable-stone",
        "Pushable Stone",
        PrefabSide.SERVER,
        List.of(POSITION),
        level,
        name);
  }

  /**
   * Returns this instance's live stone entity, if spawned.
   *
   * @return the live stone entity, or empty when it is not spawned
   */
  public Optional<Entity> pushableStoneEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity stone = context.createEntity(instance.name());
    stone.add(new PositionComponent(value(instance, POSITION)));
    stone.add(new DrawComponent(new SimpleIPath("objects/push-stone.png")));
    stone.add(new CollideComponent(Vector2.of(0.05f, 0.05f), Vector2.of(0.9f, 0.9f)));
    stone.add(new VelocityComponent(5.0f));
    return List.of(stone);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = feedbackPoint(instance, POSITION);
    feedback.point(position, instance.name());
  }

  private Point feedbackPoint(PrefabInstance instance, PrefabProperty<Point> property) {
    Point point = value(instance, property);
    Point offset = property.editorFeedbackOffset();
    return point.translate(offset.x(), offset.y());
  }
}
