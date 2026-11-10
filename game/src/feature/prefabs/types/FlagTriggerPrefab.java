package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.GameFlags;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side logic prefab that watches {@link GameFlags game flags} and raises {@link #TRIGGERED}
 * when any of them changes as selected.
 *
 * <p>Only changes while the instance is active count, a flag that already exists when the instance
 * spawns does not trigger it. Restoring a save triggers it at most once, and only if "Trigger On
 * Restore" is set.
 */
public final class FlagTriggerPrefab extends Prefab {

  private static final String TYPE = "flag-trigger";
  private static final String DISPLAY_NAME = "Flag Trigger";
  private static final String WHEN_ACTIVE = "Becomes Active";
  private static final String WHEN_INACTIVE = "Becomes Inactive";
  private static final String WHEN_CHANGED = "Changes";
  // Removes the flag listener of a spawned instance.
  private static final SpawnState<Runnable> SUBSCRIPTIONS = new SpawnState<>();

  /** Raised when a flag changed as selected, with the player that caused it. */
  public static final PrefabEvent<Entity> TRIGGERED =
      PrefabEvent.withPlayer("triggered", "Triggered");

  // Watched flags.
  private static final PrefabFlagList FLAGS = PrefabFlagList.create(1);

  /** Which changes of a flag raise the event. */
  public static final PrefabProperty<String> WHEN =
      PrefabProperty.selection(
          "when", "When Any Flag", WHEN_ACTIVE, List.of(WHEN_ACTIVE, WHEN_INACTIVE, WHEN_CHANGED));

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the flag-trigger definition. */
  public FlagTriggerPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored flag trigger.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public FlagTriggerPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(TRIGGERED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    String when = value(instance, WHEN);
    boolean onRestore = value(instance, PrefabFlagList.TRIGGER_ON_RESTORE);
    SUBSCRIPTIONS.start(
        context.level(),
        instance.name(),
        GameFlags.listen(
            FLAGS.values(instance),
            change -> {
              if (change.restored() && !onRestore) return;
              boolean matches = when.equals(WHEN_CHANGED) || change.any(when.equals(WHEN_ACTIVE));
              if (matches) fire(context, instance, TRIGGERED, change.player().orElse(null));
            }));
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    SUBSCRIPTIONS.end(context.level(), instance.name()).ifPresent(Runnable::run);
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(FLAGS.summary(instance) + " " + value(instance, WHEN).toLowerCase());
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>(FLAGS.properties());
    properties.add(WHEN);
    properties.add(PrefabFlagList.TRIGGER_ON_RESTORE);
    return List.copyOf(properties);
  }
}
