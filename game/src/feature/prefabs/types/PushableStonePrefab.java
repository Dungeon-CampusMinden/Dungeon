package feature.prefabs.types;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Server-side pushable stone matching the mushroom-level push puzzle.
 *
 * <p>Instead of the stone, any decoration can be shown, which makes that decoration pushable.
 * Actions turn pushing on and off, e.g. to lock a solved push puzzle. While it is not pushable, the
 * stone blocks the way like a wall.
 */
public final class PushableStonePrefab extends Prefab {

  /** Lets players push the stone. */
  public static final PrefabAction SET_PUSHABLE =
      PrefabAction.of("set-pushable", "Set Pushable", context -> setPushable(context, true));

  /** Stops players from pushing the stone, it stays where it is. */
  public static final PrefabAction UNSET_PUSHABLE =
      PrefabAction.of("unset-pushable", "Unset Pushable", context -> setPushable(context, false));

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** The pushed decoration, the stone by default. */
  public static final PrefabProperty<String> LOOK =
      PrefabProperty.decoration("deco", "Decoration", Deco.PushStone);

  /** Whether players can push the stone at the start. */
  public static final PrefabProperty<Boolean> PUSHABLE =
      PrefabProperty.bool("pushable", "Is Pushable", true);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, LOOK, PUSHABLE);
  // A stone without speed is static, so players cannot push it.
  private static final float PUSH_SPEED = 5.0f;
  private static final String STATE_PUSHABLE = "pushable";

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
  protected List<PrefabPort> declaredPorts() {
    return List.of(SET_PUSHABLE, UNSET_PUSHABLE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity stone = context.createEntity(instance.name());
    Optional<JsonNode> saved = restoredState(context, instance);
    Point position = saved.map(PrefabStateJson::point).orElse(value(instance, POSITION));
    // Saves from before the stone could be locked hold only its position.
    boolean pushable =
        saved
            .filter(state -> state.has(STATE_PUSHABLE))
            .map(state -> state.path(STATE_PUSHABLE).asBoolean())
            .orElse(value(instance, PUSHABLE));
    DecoFactory.createDeco(stone, position, Deco.valueOf(value(instance, LOOK)));
    // The decoration belongs to this prefab, not to the level's decorations.
    stone.remove(DecoComponent.class);
    // Only solid entities are pushed, decorations without a collider get the one of the stone.
    // Like other solid decorations, they are drawn on the layer of the players.
    if (stone.fetch(CollideComponent.class).isEmpty()) {
      stone.add(new CollideComponent(Deco.PushStone.defaultCollider()));
      stone.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.Player.depth()));
    }
    stone.add(new VelocityComponent(pushable ? PUSH_SPEED : 0f));
    return List.of(stone);
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return entities.stream()
        .findFirst()
        .flatMap(
            stone ->
                stone
                    .fetch(PositionComponent.class)
                    .map(
                        position ->
                            PrefabStateJson.point(position.position())
                                .put(STATE_PUSHABLE, isPushable(stone))));
  }

  private static boolean isPushable(Entity stone) {
    return stone
        .fetch(VelocityComponent.class)
        .map(velocity -> velocity.maxSpeed() > 0f)
        .orElse(false);
  }

  private static void setPushable(ActionContext context, boolean pushable) {
    context.liveEntities().stream()
        .findFirst()
        .flatMap(stone -> stone.fetch(VelocityComponent.class))
        .ifPresent(velocity -> velocity.maxSpeed(pushable ? PUSH_SPEED : 0f));
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
