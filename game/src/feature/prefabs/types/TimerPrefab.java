package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProblem;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabWiring;
import feature.systems.EventScheduler;
import feature.timer.WorldTimerComponent;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Server-side prefab that shows a timer on an LED display in the world: a countdown that raises
 * {@link #EXPIRED} when it reaches zero, or a stopwatch that counts up and never expires.
 *
 * <p>The timer runs in play time, so it pauses while the game is paused. {@link #STOP} freezes it,
 * {@link #START} lets it run on, or starts a countdown over after it expired. Resetting the
 * instance sets it back to its start.
 */
public final class TimerPrefab extends Prefab {

  private static final String TYPE = "timer";
  private static final String DISPLAY_NAME = "Timer";
  // Drawn until the client replaces it with the display.
  private static final String PLACEHOLDER_TEXTURE = "animation/missing_texture.png";
  private static final String STATE_ELAPSED = "elapsed-ms";
  private static final String STATE_RUNNING = "running";
  private static final String STATE_EXPIRED = "expired";
  private static final SpawnState<Clock> CLOCKS = new SpawnState<>();

  /** Raised when the countdown reached zero. */
  public static final PrefabEvent<Void> EXPIRED = PrefabEvent.signal("expired", "Expired");

  /** Lets the timer run, or starts a countdown over after it expired. */
  public static final PrefabAction START = PrefabAction.of("start", "Start", TimerPrefab::start);

  /** Freezes the timer. */
  public static final PrefabAction STOP = PrefabAction.of("stop", "Stop", TimerPrefab::stop);

  /** Position of the display. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Whether the timer counts up from zero like a stopwatch instead of down to zero. */
  public static final PrefabProperty<Boolean> COUNT_UP =
      PrefabProperty.bool("count-up", "Count Up", false)
          .describedAs(
              "Counts up from 00:00 like a stopwatch. It has no end, so it never expires.");

  /** Length of the countdown in seconds. */
  public static final PrefabProperty<Integer> DURATION =
      PrefabProperty.integer("duration", "Duration (s)", 60, 1, 5999)
          .visibleWhen(values -> !COUNT_UP.get(values));

  /** Whether the timer runs from the start. */
  public static final PrefabProperty<Boolean> START_COUNTING =
      PrefabProperty.bool("start-counting", "Start Counting", true);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, COUNT_UP, DURATION, START_COUNTING);

  /** Creates the timer definition. */
  public TimerPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored timer.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TimerPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  public String description() {
    return "A countdown on a display in the world that raises 'Expired' at zero, e.g. players"
        + " have five minutes to solve a puzzle. It can also count up like a stopwatch.";
  }

  @Override
  public List<String> aliases() {
    return List.of("countdown", "clock", "time limit", "stopwatch");
  }

  @Override
  public List<PrefabProblem> problems(ILevel level, PrefabInstance instance) {
    if (!value(instance, COUNT_UP)
        || PrefabWiring.outgoing(level.prefabs(), instance.name(), EXPIRED.key()).isEmpty()) {
      return List.of();
    }
    return List.of(
        PrefabProblem.warning(
            instance.name(),
            "'"
                + COUNT_UP.displayName()
                + "' has no end, so '"
                + EXPIRED.displayName()
                + "' never fires"));
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(START, STOP, EXPIRED);
  }

  @Override
  protected List<PrefabAction> actionsToRun(PrefabInstance instance) {
    return value(instance, START_COUNTING) ? List.of() : List.of(START);
  }

  @Override
  protected List<PrefabPort> outputsToUse(PrefabInstance instance) {
    // A stopwatch only shows the time.
    return value(instance, COUNT_UP) ? List.of() : List.of(EXPIRED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    boolean countUp = value(instance, COUNT_UP);
    // A stopwatch has no length, see WorldTimerComponent#stopwatch.
    int duration = countUp ? 0 : value(instance, DURATION);
    Optional<JsonNode> saved = restoredState(context, instance);
    long elapsed = saved.map(state -> state.path(STATE_ELAPSED).asLong()).orElse(0L);
    boolean expired = saved.map(state -> state.path(STATE_EXPIRED).asBoolean()).orElse(false);
    boolean running =
        !expired
            && saved
                .map(state -> state.path(STATE_RUNNING).asBoolean())
                .orElse(value(instance, START_COUNTING));

    long now = Game.playClock().activeMs();
    Entity timer = context.createEntity(instance.name());
    timer.add(new PositionComponent(value(instance, POSITION)));
    timer.add(new DrawComponent(new SimpleIPath(PLACEHOLDER_TEXTURE)));
    // A timer with the elapsed time behind it, running or frozen right now.
    timer.add(
        new WorldTimerComponent(now - elapsed, duration, running ? Long.MAX_VALUE : now, countUp));

    Clock clock = CLOCKS.start(context.level(), instance.name(), new Clock(timer, expired));
    if (running) run(context, instance, clock);
    return List.of(timer);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    CLOCKS.end(context.level(), instance.name()).ifPresent(Clock::cancel);
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return CLOCKS
        .get(context.level(), instance.name())
        .map(
            clock ->
                PrefabStateJson.object()
                    .put(STATE_ELAPSED, clock.timer().elapsedMs())
                    .put(STATE_RUNNING, clock.running())
                    .put(STATE_EXPIRED, clock.expired));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      // What the display shows at the start.
      int seconds = value(instance, COUNT_UP) ? 0 : value(instance, DURATION);
      feedback.label(
          position.translate(0f, -0.2f), String.format("(%02d:%02d)", seconds / 60, seconds % 60));
    }
  }

  private static void start(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    CLOCKS
        .get(context.level(), context.instance().name())
        .filter(clock -> !clock.running())
        .ifPresent(
            clock -> {
              WorldTimerComponent timer = clock.timer();
              long now = Game.playClock().activeMs();
              // An expired countdown starts over, a frozen timer goes on where it stopped.
              long elapsed =
                  clock.expired ? 0 : timer.stoppedAtActiveMs() - timer.startedAtActiveMs();
              clock.expired = false;
              clock.entity.add(
                  new WorldTimerComponent(
                      now - elapsed, timer.duration(), Long.MAX_VALUE, timer.countsUp()));
              run(creation, context.instance(), clock);
            });
  }

  private static void stop(ActionContext context) {
    CLOCKS
        .get(context.level(), context.instance().name())
        .filter(Clock::running)
        .ifPresent(
            clock -> {
              clock.cancel();
              clock.entity.add(clock.timer().stoppedAt(Game.playClock().activeMs()));
            });
  }

  private static void run(PrefabCreationContext context, PrefabInstance instance, Clock clock) {
    clock.cancel();
    // A stopwatch never expires.
    if (!clock.timer().countsUp()) {
      clock.expiry =
          EventScheduler.schedulePlayAction(
              () -> expire(context, instance, clock), clock.timer().remainingMs());
    }
    // Levels without an intro have nothing else that starts the play time.
    Game.playClock().ready();
  }

  private static void expire(PrefabCreationContext context, PrefabInstance instance, Clock clock) {
    clock.expiry = null;
    if (!clock.running()) return;
    WorldTimerComponent timer = clock.timer();
    if (timer.remainingMs() > 0) {
      run(context, instance, clock);
      return;
    }
    clock.expired = true;
    // Frozen at zero, so the display keeps showing 00:00.
    clock.entity.add(timer.stoppedAt(timer.startedAtActiveMs() + timer.duration() * 1000L));
    fire(context, instance, EXPIRED, null);
  }

  private static final class Clock {
    private final Entity entity;
    private boolean expired;
    private EventScheduler.ScheduledAction expiry;

    private Clock(Entity entity, boolean expired) {
      this.entity = entity;
      this.expired = expired;
    }

    private WorldTimerComponent timer() {
      return entity.fetch(WorldTimerComponent.class).orElseThrow();
    }

    private boolean running() {
      return !expired && timer().stoppedAtActiveMs() == Long.MAX_VALUE;
    }

    private void cancel() {
      if (expiry != null) EventScheduler.cancelAction(expiry);
      expiry = null;
    }
  }
}
