package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.systems.PositionSync;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side decoration players can interact with.
 *
 * <p>It only raises {@link #INTERACTED}, link it to e.g. the Show Dialog action of a Dialog or the
 * Show Image action of a Show Image prefab.
 */
public final class DecorationInteractionPrefab extends Prefab {

  /**
   * Raised on the server when a player interacted with the decoration, with the player as payload.
   */
  public static final PrefabEvent<Entity> INTERACTED =
      PrefabEvent.withPlayer("interacted", "Interacted");

  private static final String TYPE = "deco-interaction";
  private static final String DISPLAY_NAME = "Decoration Interaction";
  private static final float INTERACTION_RADIUS = 1.5f;

  /** Bottom-left position of the decoration. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.BookshelfLarge.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());

  public static final PrefabProperty<Vector2> SCALE =
      PrefabProperty.vector2("scale", "Scale", Vector2.ONE);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, DECO, SCALE);

  /** Creates the decoration-interaction definition. */
  public DecorationInteractionPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one decoration-interaction instance.
   *
   * @param level owning level
   * @param name instance name
   */
  public DecorationInteractionPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the decoration entity for this instance when it is currently spawned.
   *
   * @return the currently live decoration entity, if any
   */
  public Optional<Entity> decoEntity() {
    return liveEntities().stream().findFirst();
  }

  /**
   * Registers a listener called on the server whenever a player interacted with the decoration of
   * this instance.
   *
   * @param listener receives the interacting player
   */
  public void onInteracted(Consumer<Entity> listener) {
    listen(INTERACTED, listener);
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
    return List.of(INTERACTED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity deco = context.createEntity(instance.name());
    DecoFactory.createDeco(deco, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    deco.remove(DecoComponent.class);

    PositionComponent position = deco.fetch(PositionComponent.class).orElseThrow();
    position.scale(value(instance, SCALE));
    PositionSync.syncPosition(deco);

    deco.add(
        new InteractionComponent(
            new Interaction(
                (entity, who) -> fire(context, instance, INTERACTED, who), INTERACTION_RADIUS)));
    return List.of(deco);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }
}
