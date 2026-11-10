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
 * when all of them reach the selected state.
 *
 * <p>The event is raised when the last flag changes into the state, so it is raised again only
 * after a flag left the state in between. Flags that are already in the state when the instance
 * spawns do not trigger it. Restoring a save triggers it only if "Trigger On Restore" is set.
 */
public final class FlagAndTriggerPrefab extends Prefab {

  private static final String TYPE = "flag-and-trigger";
  private static final String DISPLAY_NAME = "Flag And Trigger";
  private static final String STATE_ACTIVE = "Are Active";
  private static final String STATE_INACTIVE = "Are Inactive";
  // Removes the flag listener of a spawned instance.
  private static final SpawnState<Runnable> SUBSCRIPTIONS = new SpawnState<>();

  /** Raised when all flags reached the state, with the player that changed the last one. */
  public static final PrefabEvent<Entity> TRIGGERED =
      PrefabEvent.withPlayer("triggered", "Triggered");

  // Watched flags.
  private static final PrefabFlagList FLAGS = PrefabFlagList.create(2);

  /** State all flags must be in. */
  public static final PrefabProperty<String> WHEN =
      PrefabProperty.selection(
          "when", "When All Flags", STATE_ACTIVE, List.of(STATE_ACTIVE, STATE_INACTIVE));

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the flag-and-trigger definition. */
  public FlagAndTriggerPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored flag-and trigger.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public FlagAndTriggerPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(TRIGGERED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    List<String> flags = FLAGS.values(instance);
    boolean active = value(instance, WHEN).equals(STATE_ACTIVE);
    boolean onRestore = value(instance, PrefabFlagList.TRIGGER_ON_RESTORE);
    // Whether all flags were in the state at the last check.
    boolean[] reached = {allInState(flags, active)};
    SUBSCRIPTIONS.start(
        context.level(),
        instance.name(),
        GameFlags.listen(
            flags,
            change -> {
              // Read the flags themselves, a listener called earlier may have changed them again.
              boolean now = allInState(flags, active);
              boolean entered = now && !reached[0];
              reached[0] = now;
              if (entered && (onRestore || !change.restored())) {
                fire(context, instance, TRIGGERED, change.player().orElse(null));
              }
            }));
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    SUBSCRIPTIONS.end(context.level(), instance.name()).ifPresent(Runnable::run);
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(
        "all of " + FLAGS.summary(instance) + " " + value(instance, WHEN).toLowerCase());
  }

  private static boolean allInState(List<String> flags, boolean active) {
    return flags.stream().allMatch(flag -> GameFlags.isSet(flag) == active);
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>(FLAGS.properties());
    properties.add(WHEN);
    properties.add(PrefabFlagList.TRIGGER_ON_RESTORE);
    return List.copyOf(properties);
  }
}
