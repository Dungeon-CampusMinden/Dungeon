package feature.prefabs.types;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Server-side pushable stone matching the mushroom-level push puzzle.
 *
 * <p>Instead of the stone, any decoration can be shown, which makes that decoration pushable.
 */
public final class PushableStonePrefab extends Prefab {

  /** Value of {@link #LOOK} that shows the stone texture, the default. */
  public static final String STONE = "PushStone";

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Shown texture: the stone, or the name of a {@link Deco} that becomes pushable. */
  public static final PrefabProperty<String> LOOK =
      PrefabProperty.selection("deco", "Decoration", STONE, looks());

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, LOOK);
  private static final Vector2 STONE_COLLIDER_OFFSET = Vector2.of(0.05f, 0.05f);
  private static final Vector2 STONE_COLLIDER_SIZE = Vector2.of(0.9f, 0.9f);

  /** Creates the pushable-stone definition. */
  public PushableStonePrefab() {
    super("pushable-stone", "Pushable Stone", PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored pushable stone.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public PushableStonePrefab(ILevel level, String name) {
    super("pushable-stone", "Pushable Stone", PrefabSide.SERVER, PROPERTIES, level, name);
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
    Point position =
        restoredState(context, instance)
            .map(PrefabStateJson::point)
            .orElse(value(instance, POSITION));
    String look = value(instance, LOOK);
    if (look.equals(STONE)) {
      stone.add(new PositionComponent(position));
      stone.add(new DrawComponent(new SimpleIPath("objects/push-stone.png")));
      stone.add(new CollideComponent(STONE_COLLIDER_OFFSET, STONE_COLLIDER_SIZE));
    } else {
      DecoFactory.createDeco(stone, position, Deco.valueOf(look));
      // The decoration belongs to this prefab, not to the level's decorations.
      stone.remove(DecoComponent.class);
      // Only solid entities are pushed, decorations without a collider get the one of the stone.
      // Like other solid decorations, they are drawn on the layer of the players.
      if (stone.fetch(CollideComponent.class).isEmpty()) {
        stone.add(new CollideComponent(STONE_COLLIDER_OFFSET, STONE_COLLIDER_SIZE));
        stone.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.Player.depth()));
      }
    }
    stone.add(new VelocityComponent(5.0f));
    return List.of(stone);
  }

  private static List<String> looks() {
    List<String> looks = new ArrayList<>();
    looks.add(STONE);
    for (Deco deco : Deco.values()) looks.add(deco.name());
    return looks;
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return entities.stream()
        .findFirst()
        .flatMap(stone -> stone.fetch(PositionComponent.class))
        .map(position -> PrefabStateJson.point(position.position()));
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
