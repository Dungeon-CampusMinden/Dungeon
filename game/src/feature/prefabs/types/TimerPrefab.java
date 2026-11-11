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
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.systems.EventScheduler;
import feature.timer.WorldTimerComponent;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Server-side prefab that shows a countdown on an LED display in the world and raises {@link
 * #EXPIRED} when it reaches zero.
 *
 * <p>The countdown runs in play time, so it pauses while the game is paused. {@link #STOP} freezes
 * it, {@link #START} lets it run on, or starts it over after it expired. Resetting the instance
 * sets it back to the full duration.
 */
public final class TimerPrefab extends Prefab {

  private static final String TYPE = "timer";
  private static final String DISPLAY_NAME = "Timer";
  // Drawn until the client replaces it with the display.
  private static final String PLACEHOLDER_TEXTURE = "animation/missing_texture.png";
  private static final String STATE_REMAINING = "remaining-ms";
  private static final String STATE_RUNNING = "running";
  private static final String STATE_EXPIRED = "expired";
  private static final SpawnState<Countdown> COUNTDOWNS = new SpawnState<>();

  /** Raised when the countdown reached zero. */
  public static final PrefabEvent<Void> EXPIRED = PrefabEvent.signal("expired", "Expired");

  /** Lets the countdown run, or starts it over after it expired. */
  public static final PrefabAction START = PrefabAction.of("start", "Start", TimerPrefab::start);

  /** Freezes the countdown. */
  public static final PrefabAction STOP = PrefabAction.of("stop", "Stop", TimerPrefab::stop);

  /** Position of the display. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Length of the countdown in seconds. */
  public static final PrefabProperty<Integer> DURATION =
      PrefabProperty.integer("duration", "Duration (s)", 60, 1, 5999);

  /** Whether the countdown runs from the start. */
  public static final PrefabProperty<Boolean> START_COUNTING =
      PrefabProperty.bool("start-counting", "Start Counting", true);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, DURATION, START_COUNTING);

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
  protected List<PrefabPort> declaredPorts() {
    return List.of(START, STOP, EXPIRED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    long durationMs = value(instance, DURATION) * 1000L;
    Optional<JsonNode> saved = restoredState(context, instance);
    long remaining =
        saved.map(state -> state.path(STATE_REMAINING).asLong(durationMs)).orElse(durationMs);
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
    // A countdown with the remaining time left, running or frozen right now.
    timer.add(
        new WorldTimerComponent(
            now - (durationMs - remaining),
            value(instance, DURATION),
            running ? Long.MAX_VALUE : now));

    Countdown countdown =
        COUNTDOWNS.start(context.level(), instance.name(), new Countdown(timer, expired));
    if (running) run(context, instance, countdown);
    return List.of(timer);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    COUNTDOWNS.end(context.level(), instance.name()).ifPresent(Countdown::cancel);
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return COUNTDOWNS
        .get(context.level(), instance.name())
        .map(
            countdown ->
                PrefabStateJson.object()
                    .put(STATE_REMAINING, countdown.timer().remainingMs())
                    .put(STATE_RUNNING, countdown.running())
                    .put(STATE_EXPIRED, countdown.expired));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      int seconds = value(instance, DURATION);
      feedback.label(
          position.translate(0f, -0.2f), String.format("(%02d:%02d)", seconds / 60, seconds % 60));
    }
  }

  private static void start(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    COUNTDOWNS
        .get(context.level(), context.instance().name())
        .filter(countdown -> !countdown.running())
        .ifPresent(
            countdown -> {
              WorldTimerComponent timer = countdown.timer();
              long now = Game.playClock().activeMs();
              // An expired countdown starts over, a frozen one keeps its remaining time.
              long elapsed =
                  countdown.expired ? 0 : timer.stoppedAtActiveMs() - timer.startedAtActiveMs();
              countdown.expired = false;
              countdown.entity.add(new WorldTimerComponent(now - elapsed, timer.duration()));
              run(creation, context.instance(), countdown);
            });
  }

  private static void stop(ActionContext context) {
    COUNTDOWNS
        .get(context.level(), context.instance().name())
        .filter(Countdown::running)
        .ifPresent(
            countdown -> {
              countdown.cancel();
              countdown.entity.add(countdown.timer().stoppedAt(Game.playClock().activeMs()));
            });
  }

  private static void run(
      PrefabCreationContext context, PrefabInstance instance, Countdown countdown) {
    countdown.cancel();
    countdown.expiry =
        EventScheduler.schedulePlayAction(
            () -> expire(context, instance, countdown), countdown.timer().remainingMs());
    // Levels without an intro have nothing else that starts the play time.
    Game.playClock().ready();
  }

  private static void expire(
      PrefabCreationContext context, PrefabInstance instance, Countdown countdown) {
    countdown.expiry = null;
    if (!countdown.running()) return;
    WorldTimerComponent timer = countdown.timer();
    if (timer.remainingMs() > 0) {
      run(context, instance, countdown);
      return;
    }
    countdown.expired = true;
    // Frozen at zero, so the display keeps showing 00:00.
    countdown.entity.add(timer.stoppedAt(timer.startedAtActiveMs() + timer.duration() * 1000L));
    fire(context, instance, EXPIRED, null);
  }

  private static final class Countdown {
    private final Entity entity;
    private boolean expired;
    private EventScheduler.ScheduledAction expiry;

    private Countdown(Entity entity, boolean expired) {
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
