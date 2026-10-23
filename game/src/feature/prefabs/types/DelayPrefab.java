package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.systems.EventScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side timer that passes a signal on after a delay, e.g. to close a door a few seconds after
 * it was opened.
 *
 * <p>Every {@link #START} raises {@link #FINISHED} once the delay passed, with the player that
 * caused the start. Pending timers are dropped when the instance despawns.
 */
public final class DelayPrefab extends Prefab {

  private static final String TYPE = "delay";
  private static final String DISPLAY_NAME = "Delay";
  private static final SpawnState<List<EventScheduler.ScheduledAction>> PENDING =
      new SpawnState<>();

  /** Time between a start and the finish, in milliseconds. */
  public static final PrefabProperty<Integer> DELAY =
      PrefabProperty.integer("delay", "Delay (ms)", 1000, 0, Integer.MAX_VALUE);

  /** Whether starting again drops the pending timers, instead of running them side by side. */
  public static final PrefabProperty<Boolean> RESTART =
      PrefabProperty.bool("restart", "Restart When Started Again", false);

  /** Raised once the delay passed after a start, with the player that caused the start. */
  public static final PrefabEvent<Entity> FINISHED = PrefabEvent.withPlayer("finished", "Finished");

  /** Starts a timer. */
  public static final PrefabAction START = PrefabAction.of("start", "Start", DelayPrefab::start);

  /** Drops all pending timers. */
  public static final PrefabAction CANCEL =
      PrefabAction.of(
          "cancel",
          "Cancel",
          context ->
              PENDING
                  .get(context.level(), context.instance().name())
                  .ifPresent(DelayPrefab::cancelAll));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(DELAY, RESTART);

  /** Creates the delay definition. */
  public DelayPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored delay.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DelayPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(START, CANCEL, FINISHED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    PENDING.start(context.level(), instance.name(), new ArrayList<>());
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    PENDING.end(context.level(), instance.name()).ifPresent(DelayPrefab::cancelAll);
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, DELAY) + " ms");
  }

  private static void start(ActionContext context) {
    PENDING
        .get(context.level(), context.instance().name())
        .ifPresent(
            pending -> {
              if (context.value(RESTART)) cancelAll(pending);
              PrefabCreationContext creation = context.creationContext();
              PrefabInstance instance = context.instance();
              Entity player = context.player().orElse(null);
              EventScheduler.ScheduledAction[] timer = new EventScheduler.ScheduledAction[1];
              // At least one millisecond, so a timer started by a finishing timer is scheduled
              // behind the one the scheduler is currently running.
              long delay = Math.max(1L, context.value(DELAY));
              timer[0] =
                  EventScheduler.scheduleAction(
                      () -> {
                        pending.remove(timer[0]);
                        fire(creation, instance, FINISHED, player);
                      },
                      delay);
              pending.add(timer[0]);
            });
  }

  private static void cancelAll(List<EventScheduler.ScheduledAction> pending) {
    EventScheduler.cancelAction(pending.toArray(EventScheduler.ScheduledAction[]::new));
    pending.clear();
  }
}
