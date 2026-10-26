package feature.audience;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PositionComponent;
import engine.network.input.InputCommandRouter;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.c2s.InputMessage;
import engine.time.PlayClock;
import engine.tracking.Tracking;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import feature.hud.UIUtils;
import feature.survey.ExternalSurvey;
import feature.survey.SurveyFeature;
import feature.systems.PositionSync;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tracking.core.TrackingJson;

/**
 * Runs audience mode on the game server: the audience controls the hero by voting on their devices.
 *
 * <p>The system connects to the relay, admits devices through the pre survey, collects votes in
 * windows, applies the most frequent vote (ties are decided at random), records votes and results
 * through {@link Tracking}, and publishes what the devices and the game screen show. The host
 * starts, ends, and closes the session with keys on the game-screen client (see {@link
 * AudienceHud}).
 *
 * <p>While the play clock stands, because the host opened the pause menu or the join screen, the
 * session pauses: windows, remaining times, and results stand still, votes cast so far stay, and
 * new votes are ignored until play resumes.
 *
 * <p>Add it to the server before the {@code VelocitySystem}, because it steers the hero's steps.
 * Call {@link #end} when the room is finished; the devices then show the post survey.
 */
public final class AudienceSystem extends System {
  /**
   * Dialog attribute (an {@link Integer}) that turns a free-input dialog into a code entry: the
   * devices show a digit keypad for a code of this many digits.
   */
  public static final String CODE_DIGITS = "audienceCodeDigits";

  /** Input route of the host's start key. */
  static final String START = "audience:start";

  /** Input route of the host's key that ends the game. */
  static final String END = "audience:end";

  /** Input route of the host's key that closes the session after the game. */
  static final String CLOSE = "audience:close";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AudienceSystem.class);
  private static final int HUD_INTERVAL_MS = 250;
  private static final int TALLY_ENTRIES = 6;
  private static final int INPUTS_PER_FRAME = 500;
  private static final long READY_REST_MS = 5_000;
  private static final long ALL_READY_REST_MS = 2_000;
  private static final long ALL_VOTED_REST_MS = 3_000;

  /** Session phases, in order. */
  enum Phase {
    LOBBY,
    COUNTDOWN,
    PLAYING,
    ENDED,
    CLOSED
  }

  /** Where a device is in the session. */
  enum Status {
    /** Answers the pre survey. */
    PRE,
    /** Votes. */
    PLAY,
    /** Declined consent and only watches. */
    SPECTATOR,
    /** Answers the post survey. */
    POST,
    /** Finished. */
    DONE
  }

  private final AudienceConfig config;
  private final Runnable onStart;
  private final Runnable onClosed;
  private final Random random = new Random();
  private final Map<String, Player> players = new LinkedHashMap<>();

  private AudienceRelayClient relay;
  private Optional<ExternalSurvey> preSurvey = Optional.empty();
  private Optional<ExternalSurvey> postSurvey = Optional.empty();
  private Phase phase = Phase.LOBBY;
  private long phaseEndsAt;
  private Entity hero;
  private TileStepper stepper;
  private AudienceContext context = new AudienceContext.Wait();
  // Title and text of the context's dialog for the devices; null without a dialog.
  private ObjectNode prompt;
  private long windowId = 1;
  private Window window;
  private Result result;
  private String publishedKey = "";
  private long nextHud;
  // Session time stands still while paused: the real time the pause began, or -1, and the total
  // time of earlier pauses.
  private long pausedAt = -1;
  private long pausedFor;

  /**
   * Creates the system.
   *
   * @param config audience settings
   * @param onStart runs when the countdown ends and play begins, for example to show the intro
   * @param onClosed runs once the host closed the session, for example to end the game
   */
  public AudienceSystem(AudienceConfig config, Runnable onStart, Runnable onClosed) {
    super(AuthoritativeSide.SERVER);
    this.config = config;
    this.onStart = onStart;
    this.onClosed = onClosed;
  }

  /**
   * Ends the game: play time stops, windows close, and the voting players get the post survey.
   * Players still answering the pre survey keep it; they get the post survey after it. Dialogs of
   * the hero close from now on, because nobody answers them anymore.
   *
   * @param outcome {@code SUCCESS} when the room was solved; the host's End key reports {@code
   *     FAILURE}, because time ran out
   */
  public void end(PlayClock.Outcome outcome) {
    if (phase == Phase.ENDED || phase == Phase.CLOSED) return;
    Game.playClock().end(outcome);
    phase = Phase.ENDED;
    phaseEndsAt = now() + config.closeAfterEnd().toMillis();
    window = null;
    context = new AudienceContext.Wait();
    for (Player player : players.values()) {
      if (player.status == Status.PLAY) player.status = afterGame(player);
    }
  }

  /**
   * Puts another entity, such as a character standing in the room, on a tile the way the votes
   * place the hero. The center of its hitbox lands on the center of the tile, even when its sprite
   * is larger than a tile.
   *
   * @param entity an entity with a position
   * @param tile origin of the tile
   */
  public static void standOn(Entity entity, Point tile) {
    entity
        .fetch(PositionComponent.class)
        .orElseThrow()
        .position(TileStepper.positionOn(entity, tile));
    PositionSync.syncPosition(entity);
  }

  // Where a consenting player goes once the game is over.
  private Status afterGame(Player player) {
    return player.participant.isPresent() && postSurvey.isPresent() ? Status.POST : Status.DONE;
  }

  @Override
  public void execute() {
    if (relay == null) start();
    if (phase == Phase.CLOSED) return;
    if (hero == null || Game.findEntityById(hero.id()).isEmpty()) {
      hero = Game.allPlayers().findFirst().orElse(null);
      stepper = hero == null ? null : new TileStepper(hero);
      if (stepper != null) stepper.snap();
    }
    pause(phase == Phase.PLAYING && !Game.playClock().running());
    if (stepper != null) {
      if (paused()) stepper.hold();
      else stepper.tick();
    }
    relay.drain(INPUTS_PER_FRAME).forEach(this::receive);
    long now = now();
    if (phase == Phase.COUNTDOWN && now >= phaseEndsAt) {
      phase = Phase.PLAYING;
      Game.playClock().ready();
      onStart.run();
    }
    if (phase == Phase.ENDED) {
      // Nobody votes after the game, so a dialog would stay open and catch the host's keys.
      if (hero != null) AudienceContext.dialog(hero).ifPresent(ui -> UIUtils.closeDialog(ui, true));
      if (now >= phaseEndsAt) close();
    }
    if (phase == Phase.PLAYING && hero != null && !paused()) play(now);
    publish(now);
  }

  private void start() {
    preSurvey = ExternalSurvey.of(SurveyFeature.Phase.PRE);
    postSurvey = ExternalSurvey.of(SurveyFeature.Phase.POST);
    ObjectNode surveys = TrackingJson.object();
    preSurvey.ifPresent(survey -> surveys.set("pre", survey.definition()));
    postSurvey.ifPresent(survey -> surveys.set("post", survey.definition()));
    relay = new AudienceRelayClient(config.relay(), config.relayKey(), surveys);
    AudienceHudMessage.registerConverter();
    // The audience answers dialogs through votes; answers from the game-screen client do not count.
    Game.network()
        .messageDispatcher()
        .registerHandler(DialogResponseMessage.class, (session, response) -> {});
    InputCommandRouter.InputCommandHandler ignored = context -> {};
    for (InputMessage.Action action :
        new InputMessage.Action[] {
          InputMessage.Action.MOVE, InputMessage.Action.INTERACT, InputMessage.Action.CAST_SKILL
        }) {
      InputCommandRouter.register(InputCommandRouter.routeKey(action), true, ignored);
    }
    InputCommandRouter.register(START, true, context -> hostStart());
    InputCommandRouter.register(END, true, context -> end(PlayClock.Outcome.FAILURE));
    InputCommandRouter.register(CLOSE, true, context -> close());
  }

  private void hostStart() {
    if (phase != Phase.LOBBY) return;
    phase = Phase.COUNTDOWN;
    phaseEndsAt = now() + config.countdown().toMillis();
  }

  private void close() {
    if (phase != Phase.ENDED) return;
    phase = Phase.CLOSED;
    relay.close();
    onClosed.run();
  }

  // --- device inputs

  private void receive(JsonNode message) {
    String pid = message.path("pid").asString("");
    switch (message.path("type").asString("")) {
      case "join" ->
          players.computeIfAbsent(
              pid, id -> new Player(preSurvey.isPresent() ? Status.PRE : Status.PLAY));
      case "survey" -> survey(pid, message);
      case "vote" -> vote(pid, message);
      default -> LOGGER.debug("Ignoring relay message {}", message);
    }
  }

  private void survey(String pid, JsonNode message) {
    Player player = players.get(pid);
    if (player == null) return;
    JsonNode answers = message.path("answers");
    String surveyPhase = message.path("phase").asString("");
    if (surveyPhase.equals("pre") && player.status == Status.PRE && preSurvey.isPresent()) {
      if (preSurvey.get().declined(answers)) {
        player.status = Status.SPECTATOR;
        return;
      }
      player.participant = Tracking.remoteParticipantJoined(pid);
      if (!recorded(preSurvey.get(), answers, player)) return;
      player.status = phase == Phase.ENDED ? afterGame(player) : Status.PLAY;
    } else if (surveyPhase.equals("post") && player.status == Status.POST) {
      if (postSurvey.isPresent() && !postSurvey.get().declined(answers)) {
        if (!recorded(postSurvey.get(), answers, player)) return;
      }
      player.status = Status.DONE;
    }
  }

  // Records survey answers; invalid answers leave the device on its survey.
  private static boolean recorded(ExternalSurvey survey, JsonNode answers, Player player) {
    if (player.participant.isEmpty()) return true;
    try {
      survey.record(answers, player.participant.get());
      return true;
    } catch (RuntimeException exception) {
      LOGGER.warn("Ignoring survey answers: {}", exception.getMessage());
      return false;
    }
  }

  private void vote(String pid, JsonNode message) {
    Player player = players.get(pid);
    if (phase != Phase.PLAYING || paused() || player == null || player.status != Status.PLAY) {
      return;
    }
    if (message.path("window").asLong() != windowId) return;
    // The window is resolved later in this frame; a late vote must not change its result.
    if (window != null && now() >= window.endsAt) return;
    Optional<JsonNode> vote = context.normalize(message.path("value"));
    if (vote.isEmpty()) return;
    if (window == null) window = new Window(now(), duration(context));
    window.votes.put(pid, vote.get());
  }

  // --- voting windows

  private void play(long now) {
    AudienceContext current = AudienceContext.of(hero, stepper);
    // A closed or replaced dialog yields another context, because contexts hold their dialog.
    if (!current.equals(context)) {
      context = current;
      prompt = current.dialog().map(AudienceContext::prompt).orElse(null);
      windowId++;
      window =
          current.startsAtOnce() && !(current instanceof AudienceContext.Wait)
              ? new Window(now, duration(current))
              : null;
      return;
    }
    if (window == null) return;
    window.endsAt = windowEnd(context, window.endsAt, window.votes.size(), votingPlayers(), now);
    if (now >= window.endsAt) resolve(now);
  }

  /**
   * Returns when a window ends, given its votes so far. A text closes five seconds after half the
   * voters finished reading and two seconds after all of them did. A puzzle closes three seconds
   * after everybody voted. Nobody waits out a long window, and the seconds left let a voter change
   * their mind. Movement windows keep their end.
   *
   * @param context context of the window
   * @param endsAt current end of the window
   * @param votes number of players who voted in the window
   * @param voters number of players who may vote
   * @param now current session time
   * @return the end of the window, never later than {@code endsAt}
   */
  static long windowEnd(AudienceContext context, long endsAt, int votes, long voters, long now) {
    if (votes == 0) return endsAt;
    return switch (context) {
      case AudienceContext.Continue next -> {
        if (votes >= voters) yield Math.min(endsAt, now + ALL_READY_REST_MS);
        yield votes * 2L >= voters ? Math.min(endsAt, now + READY_REST_MS) : endsAt;
      }
      case AudienceContext.Move move -> endsAt;
      case AudienceContext.Wait wait -> endsAt;
      default -> votes >= voters ? Math.min(endsAt, now + ALL_VOTED_REST_MS) : endsAt;
    };
  }

  private void resolve(long now) {
    Window closed = window;
    long closedId = windowId;
    windowId++;
    window = context.startsAtOnce() ? new Window(now, duration(context)) : null;
    List<Map.Entry<JsonNode, Integer>> tally = tally(closed);
    if (tally.isEmpty() && !(context instanceof AudienceContext.Continue)) return;
    JsonNode winner = tally.isEmpty() ? AudienceContext.JSON.stringNode("continue") : winner(tally);
    record(closedId, closed, tally, winner);
    result = new Result(closedId, context, tally, winner, now + config.resultDisplay().toMillis());
    context.apply(winner, stepper);
  }

  // Votes per distinct vote, most frequent first.
  private static List<Map.Entry<JsonNode, Integer>> tally(Window window) {
    Map<JsonNode, Integer> counts = new LinkedHashMap<>();
    window.votes.values().forEach(vote -> counts.merge(vote, 1, Integer::sum));
    List<Map.Entry<JsonNode, Integer>> entries = new ArrayList<>(counts.entrySet());
    entries.sort(Map.Entry.<JsonNode, Integer>comparingByValue(Comparator.reverseOrder()));
    return entries;
  }

  // Picks the most frequent vote; among equally frequent votes each is equally likely.
  private JsonNode winner(List<Map.Entry<JsonNode, Integer>> tally) {
    int top = tally.getFirst().getValue();
    List<JsonNode> leaders =
        tally.stream().filter(entry -> entry.getValue() == top).map(Map.Entry::getKey).toList();
    return leaders.get(random.nextInt(leaders.size()));
  }

  private void record(
      long id, Window closed, List<Map.Entry<JsonNode, Integer>> tally, JsonNode winner) {
    String kind = context.kind();
    closed.votes.forEach(
        (pid, vote) ->
            Optional.ofNullable(players.get(pid))
                .flatMap(player -> player.participant)
                .ifPresent(participant -> Tracking.voteCast(id, kind, vote, participant)));
    ArrayNode votes = TrackingJson.object().arrayNode();
    tally.forEach(
        entry -> votes.addObject().put("count", entry.getValue()).set("vote", entry.getKey()));
    Tracking.voteResolved(id, kind, votes, winner);
  }

  private long duration(AudienceContext context) {
    return switch (context) {
      case AudienceContext.Move move -> config.moveWindow().toMillis();
      case AudienceContext.Continue next -> config.continueWindow().toMillis();
      default -> config.puzzleWindow().toMillis();
    };
  }

  private long votingPlayers() {
    return players.values().stream().filter(player -> player.status == Status.PLAY).count();
  }

  // --- published state

  // Publishes the devices' state when it changed and the game-screen overlay a few times a second.
  private void publish(long now) {
    ObjectNode state = TrackingJson.object().put("phase", phase.name().toLowerCase());
    if (paused()) state.put("paused", true);
    ObjectNode contextJson = state.putObject("context").put("kind", context.kind());
    contextJson.put("window", windowId);
    context.describe(contextJson);
    if (prompt != null) contextJson.setAll(prompt);
    if (window != null) contextJson.put("windowMs", window.endsAt - window.startedAt);
    ObjectNode statuses = state.putObject("players");
    players.forEach((pid, player) -> statuses.put(pid, player.status.name().toLowerCase()));
    if (result != null && now < result.until) state.set("result", result.json());
    String key = state.toString();
    if (!key.equals(publishedKey)) {
      publishedKey = key;
      if (window != null) contextJson.put("remainingMs", Math.max(0, window.endsAt - now));
      if (phase == Phase.COUNTDOWN) state.put("countdownMs", Math.max(0, phaseEndsAt - now));
      relay.publish(state.toString());
    }
    // Real time, so the join screen keeps counting new players during a pause.
    if (realNow() >= nextHud) {
      nextHud = realNow() + HUD_INTERVAL_MS;
      Game.network().broadcast(new AudienceHudMessage(hud(now).toString()), true);
    }
  }

  private ObjectNode hud(long now) {
    ObjectNode hud = TrackingJson.object().put("phase", phase.name().toLowerCase());
    hud.put("paused", paused());
    hud.put("relay", config.relay().toString()).put("online", relay.online());
    relay.code().ifPresent(code -> hud.put("code", code));
    if (phase == Phase.COUNTDOWN || phase == Phase.ENDED) {
      hud.put("phaseMs", Math.max(0, phaseEndsAt - now));
    }
    ObjectNode counts = hud.putObject("players").put("joined", players.size());
    players
        .values()
        .forEach(
            player -> {
              String name = player.status.name().toLowerCase();
              counts.put(name, counts.path(name).asInt() + 1);
            });
    ObjectNode contextJson = hud.putObject("context").put("kind", context.kind());
    context.describe(contextJson);
    if (window != null) {
      contextJson.put("remainingMs", Math.max(0, window.endsAt - now));
      contextJson.put("windowMs", window.endsAt - window.startedAt);
      contextJson.set("tally", tallyJson(context, tally(window)));
    }
    if (result != null && now < result.until) hud.set("result", result.json());
    return hud;
  }

  private static ArrayNode tallyJson(
      AudienceContext context, List<Map.Entry<JsonNode, Integer>> tally) {
    ArrayNode array = TrackingJson.object().arrayNode();
    tally.stream()
        .limit(TALLY_ENTRIES)
        .forEach(
            entry ->
                array
                    .addObject()
                    .put("label", context.label(entry.getKey()))
                    .put("count", entry.getValue()));
    return array;
  }

  private void pause(boolean paused) {
    if (paused && pausedAt < 0) {
      pausedAt = realNow();
    } else if (!paused && pausedAt >= 0) {
      pausedFor += realNow() - pausedAt;
      pausedAt = -1;
    }
  }

  private boolean paused() {
    return pausedAt >= 0;
  }

  // Session time in milliseconds; windows, phases, and results run on it.
  private long now() {
    return (paused() ? pausedAt : realNow()) - pausedFor;
  }

  private static long realNow() {
    return java.lang.System.nanoTime() / 1_000_000;
  }

  /** A device of the audience. */
  private static final class Player {
    private Status status;
    private Optional<UUID> participant = Optional.empty();

    private Player(Status status) {
      this.status = status;
    }
  }

  /** An open voting window and the latest vote of each device in it. */
  private static final class Window {
    private final long startedAt;
    private long endsAt;
    private final Map<String, JsonNode> votes = new LinkedHashMap<>();

    private Window(long startedAt, long duration) {
      this.startedAt = startedAt;
      this.endsAt = startedAt + duration;
    }
  }

  // The outcome of a closed window, shown for a few seconds.
  private record Result(
      long window,
      AudienceContext context,
      List<Map.Entry<JsonNode, Integer>> tally,
      JsonNode winner,
      long until) {
    ObjectNode json() {
      ObjectNode json = TrackingJson.object().put("window", window).put("kind", context.kind());
      json.put("chosen", context.label(winner));
      json.set("tally", tallyJson(context, tally));
      return json;
    }
  }
}
