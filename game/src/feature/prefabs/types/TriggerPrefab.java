package feature.prefabs.types;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side rectangular area that raises {@link #ENTERED} when a player enters it, e.g. to show a
 * dialog or play a sound.
 *
 * <p>The repetition limits how often the event is raised. Its history is reset when the instance is
 * respawned or reset.
 */
public final class TriggerPrefab extends Prefab {

  /** Raised on the server when a player entered the area, with the player as payload. */
  public static final PrefabEvent<Entity> ENTERED = PrefabEvent.withPlayer("entered", "Entered");

  private static final String TYPE = "trigger";
  private static final String DISPLAY_NAME = "Trigger";

  public static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  public static final PrefabProperty<String> REPETITION = TriggerRepetition.property();

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(REGION, REPETITION);

  /** Creates the trigger definition. */
  public TriggerPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored trigger.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TriggerPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the trigger entity for this instance when it is currently spawned.
   *
   * @return the currently live trigger entity, if any
   */
  public Optional<Entity> triggerEntity() {
    return liveEntities().stream().findFirst();
  }

  /**
   * Registers a listener called on the server whenever a player entered the area of this instance,
   * as limited by its repetition.
   *
   * @param listener receives the entering player
   */
  public void onEntered(Consumer<Entity> listener) {
    listen(ENTERED, listener);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(ENTERED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    Point bottomLeft = region.bottomLeft();
    Point topRight = region.topRight();
    TriggerRepetition repetition = TriggerRepetition.fromLabel(value(instance, REPETITION));

    Entity trigger = context.createEntity(instance.name());
    trigger.add(new PositionComponent(bottomLeft));
    trigger.add(new TriggerRepetition.History(repetition));
    if (bottomLeft.equals(topRight)) return List.of(trigger);
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.of(topRight.x() - bottomLeft.x(), topRight.y() - bottomLeft.y()),
                (self, who, direction) -> {
                  if (!who.isPresent(PlayerComponent.class)) return;
                  boolean allowed =
                      self.fetch(TriggerRepetition.History.class)
                          .map(history -> history.tryTrigger(who))
                          .orElse(false);
                  if (allowed) fire(context, instance, ENTERED, who);
                },
                CollideComponent.DEFAULT_COLLIDER)
            .isSolid(false));
    return List.of(trigger);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.region(value(instance, REGION), instance.name());
  }
}
