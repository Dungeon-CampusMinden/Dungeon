package feature.survey;

import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.tracking.Tracking;
import engine.utils.logging.DungeonLogger;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;

/**
 * Asks each player the room's survey after the game and records the answers as {@code
 * SURVEY_ANSWERED} tracking events.
 *
 * <p>Register the dialog in every runtime with {@link #register()}, then call {@link #show} on the
 * authoritative side after the room outcome and before the tracking session finishes.
 */
public final class SurveyFeature {

  static final String ROOM_ID_KEY = "surveyRoomId";
  static final String RESULT_KEY = "surveyResult";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SurveyFeature.class);
  private static final Duration ACKNOWLEDGEMENT_TIMEOUT = Duration.ofSeconds(10);

  private SurveyFeature() {}

  /** Dialog type rendering both the survey form and its submission result. */
  enum Type implements DialogType {
    SURVEY;

    @Override
    public String type() {
      return name();
    }
  }

  /** Storage state reported to a player after submitting. */
  enum Result {
    /** The tracking backend acknowledged every answer. */
    CONFIRMED,
    /** The deployment stores tracking data only in the local outbox. */
    SAVED_LOCALLY,
    /** The backend did not acknowledge in time; the outbox keeps the answers for a later upload. */
    PENDING,
    /** The answers were rejected or could not be recorded. */
    FAILED
  }

  /** Registers the survey dialog with the shared dialog factory. */
  public static void register() {
    DialogFactory.register(Type.SURVEY, SurveyDialog::build);
  }

  /**
   * Shows the survey of the tracked room to each player and runs {@code onComplete} once each of
   * them is done: they skipped it, sent no answers, closed the storage result after submitting, or
   * left the game.
   *
   * <p>The survey is {@code surveys/<roomId>.json} for the room configured through {@link
   * Tracking#configureRoom(String)}. Runs {@code onComplete} immediately when that file is absent
   * or invalid, or tracking is inactive, because answers could not be recorded.
   *
   * @param onComplete action after every player finished the survey
   * @param playerIds players to ask; all players when empty
   */
  public static void show(Runnable onComplete, int... playerIds) {
    Optional<SurveyDefinition> survey =
        Tracking.active() ? Tracking.roomId().flatMap(SurveyDefinition::load) : Optional.empty();
    List<Integer> players = new ArrayList<>();
    if (playerIds.length == 0) {
      Game.allPlayers().forEach(player -> players.add(player.id()));
    } else {
      for (int id : playerIds) players.add(id);
    }
    players.removeIf(id -> Tracking.participantForEntity(id).isEmpty());
    if (survey.isEmpty() || players.isEmpty()) {
      onComplete.run();
      return;
    }
    Round round = new Round(onComplete);
    SurveySystem.instance().rounds.add(round);
    for (int player : players) {
      ask(survey.get(), player, Tracking.participantForEntity(player).orElseThrow(), round);
    }
  }

  private static void ask(SurveyDefinition survey, int player, UUID participant, Round round) {
    UIComponent ui =
        round.show(
            player,
            DialogContext.builder()
                .type(Type.SURVEY)
                .put(ROOM_ID_KEY, survey.roomId())
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .build());
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          if (!round.answer(player)) return;
          Optional<TrackingEvent> last;
          try {
            last = record(survey, participant, data);
          } catch (RuntimeException exception) {
            LOGGER.warn(
                "Survey submission for {} failed: {}", survey.roomId(), exception.getMessage());
            showResult(Result.FAILED, player, round);
            return;
          }
          if (last.isEmpty()) {
            round.finish(player);
          } else if (!Tracking.remoteStorageEnabled()) {
            showResult(Result.SAVED_LOCALLY, player, round);
          } else {
            SurveySystem.instance().await(last.get(), result -> showResult(result, player, round));
          }
        });
    if (survey.skippable()) {
      ui.registerCallback(
          DialogContextKeys.ON_CANCEL,
          data -> {
            if (round.answer(player)) round.finish(player);
          });
    }
  }

  /**
   * Validates one submission and records each answered question.
   *
   * @param survey survey the player answered
   * @param participant tracking participant of the player
   * @param data client response carrying the answers as a JSON object
   * @return the last recorded event, or empty when the submission answered nothing
   * @throws RuntimeException if the answers are invalid or tracking stopped recording
   */
  private static Optional<TrackingEvent> record(
      SurveyDefinition survey, UUID participant, DialogResponseMessage.Payload data) {
    JsonNode answers = TrackingJson.object(((DialogResponseMessage.StringValue) data).value());
    if (!survey.valid(answers)) throw new IllegalArgumentException("invalid survey answers");
    Optional<TrackingEvent> last = Optional.empty();
    for (SurveyDefinition.Question question : survey.questions()) {
      JsonNode answer = answers.get(question.id());
      if (answer == null || answer.isNull()) continue;
      last =
          Optional.of(
              Tracking.surveyAnswered(survey.questionnaireId(), question.id(), answer, participant)
                  .orElseThrow(() -> new IllegalStateException("tracking stopped recording")));
    }
    return last;
  }

  private static void showResult(Result result, int player, Round round) {
    // The player may have left while the backend acknowledgement was pending.
    if (!round.asking(player)) return;
    UIComponent ui =
        round.show(
            player,
            DialogContext.builder()
                .type(Type.SURVEY)
                .put(RESULT_KEY, result.name())
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .build());
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> round.finish(player));
  }

  /**
   * Players still busy with one {@link #show} call and their open survey or result dialog;
   * completes once when none remain.
   */
  private static final class Round {
    private final Map<Integer, UIComponent> dialogs = new HashMap<>();
    private final Set<Integer> answered = new HashSet<>();
    private final Runnable onComplete;
    private boolean completed;

    private Round(Runnable onComplete) {
      this.onComplete = onComplete;
    }

    /**
     * Shows a dialog to one player and closes that player's previous survey dialog.
     *
     * @param player player entity ID
     * @param context dialog to show
     * @return the shown dialog
     */
    UIComponent show(int player, DialogContext context) {
      UIComponent previous = dialogs.remove(player);
      if (previous != null) UIUtils.closeDialog(previous, true);
      UIComponent ui = DialogFactory.show(context, true, false, player);
      dialogs.put(player, ui);
      return ui;
    }

    boolean asking(int player) {
      return dialogs.containsKey(player);
    }

    /**
     * Accepts a player's first submit or skip; the server ignores repeated or late responses.
     *
     * @param player player entity ID
     * @return whether this response is the player's answer
     */
    boolean answer(int player) {
      return asking(player) && answered.add(player);
    }

    void finish(int player) {
      UIComponent ui = dialogs.remove(player);
      if (ui != null) UIUtils.closeDialog(ui, true);
      completeIfDone();
    }

    /** Closes the dialogs of players whose entity is gone, so a reconnect does not restore them. */
    void dropDeparted() {
      List.copyOf(dialogs.keySet()).stream()
          .filter(player -> Game.findEntityById(player).isEmpty())
          .forEach(this::finish);
      completeIfDone();
    }

    private void completeIfDone() {
      if (completed || !dialogs.isEmpty()) return;
      completed = true;
      onComplete.run();
    }
  }

  /**
   * Runs survey bookkeeping on the game thread: notices players who left mid-survey and polls
   * backend acknowledgements, because the uploader thread must not touch dialogs and a headless
   * server has no libGDX runnable queue.
   */
  private static final class SurveySystem extends engine.System {
    private final List<Round> rounds = new ArrayList<>();
    private final List<Waiter> waiters = new ArrayList<>();

    private record Waiter(TrackingEvent event, long deadlineNanos, Consumer<Result> onResult) {}

    private SurveySystem() {
      super(AuthoritativeSide.SERVER);
    }

    static SurveySystem instance() {
      if (Game.systems().get(SurveySystem.class) instanceof SurveySystem system) return system;
      SurveySystem system = new SurveySystem();
      Game.add(system);
      return system;
    }

    void await(TrackingEvent event, Consumer<Result> onResult) {
      long deadline = java.lang.System.nanoTime() + ACKNOWLEDGEMENT_TIMEOUT.toNanos();
      waiters.add(new Waiter(event, deadline, onResult));
    }

    @Override
    public void execute() {
      for (Round round : List.copyOf(rounds)) round.dropDeparted();
      rounds.removeIf(round -> round.completed);
      for (Waiter waiter : List.copyOf(waiters)) {
        boolean confirmed = Tracking.remoteAcknowledged(waiter.event());
        if (!confirmed && java.lang.System.nanoTime() < waiter.deadlineNanos()) continue;
        waiters.remove(waiter);
        waiter.onResult().accept(confirmed ? Result.CONFIRMED : Result.PENDING);
      }
    }

    /** Survey dialogs pause singleplayer systems, but this bookkeeping must keep running. */
    @Override
    public void stop() {}
  }
}
