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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    DialogContext context =
        DialogContext.builder()
            .type(Type.SURVEY)
            .put(ROOM_ID_KEY, survey.roomId())
            .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
            .build();
    UIComponent ui = DialogFactory.show(context, true, false, player);
    round.dialog(player, ui);
    boolean[] submitted = {false};
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          if (submitted[0] || !round.asking(player)) return;
          submitted[0] = true;
          record(
              survey,
              participant,
              data,
              result -> {
                if (!round.asking(player)) return;
                UIUtils.closeDialog(ui, true);
                showResult(survey.roomId(), result, player, round);
              },
              () -> round.finish(player));
        });
    if (survey.skippable()) {
      ui.registerCallback(
          DialogContextKeys.ON_CANCEL,
          data -> {
            if (submitted[0] || !round.asking(player)) return;
            submitted[0] = true;
            round.finish(player);
          });
    }
  }

  /**
   * Validates and records one submission, then reports how it was stored.
   *
   * @param onResult receives the storage state, possibly several ticks later
   * @param onEmpty runs instead when the submission contained no answers
   */
  private static void record(
      SurveyDefinition survey,
      UUID participant,
      DialogResponseMessage.Payload data,
      Consumer<Result> onResult,
      Runnable onEmpty) {
    Optional<TrackingEvent> last = Optional.empty();
    try {
      String json = ((DialogResponseMessage.StringValue) data).value();
      JsonNode answers = TrackingJson.object(json);
      var problems = survey.problems(answers);
      if (!problems.isEmpty()) {
        throw new IllegalArgumentException("invalid answers " + problems);
      }
      for (SurveyDefinition.Question question : survey.questions()) {
        JsonNode answer = answers.get(question.id());
        if (answer == null || answer.isNull()) continue;
        last =
            Optional.of(
                Tracking.surveyAnswered(
                        survey.questionnaireId(), question.id(), answer, participant)
                    .orElseThrow(() -> new IllegalStateException("tracking stopped recording")));
      }
    } catch (RuntimeException exception) {
      LOGGER.warn("Survey submission for {} failed: {}", survey.roomId(), exception.getMessage());
      onResult.accept(Result.FAILED);
      return;
    }
    if (last.isEmpty()) {
      onEmpty.run();
    } else if (!Tracking.remoteStorageEnabled()) {
      onResult.accept(Result.SAVED_LOCALLY);
    } else {
      SurveySystem.instance().await(last.get(), onResult);
    }
  }

  private static void showResult(String roomId, Result result, int player, Round round) {
    DialogContext context =
        DialogContext.builder()
            .type(Type.SURVEY)
            .put(ROOM_ID_KEY, roomId)
            .put(RESULT_KEY, result.name())
            .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
            .build();
    UIComponent ui = DialogFactory.show(context, true, false, player);
    round.dialog(player, ui);
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> round.finish(player));
  }

  /**
   * Players still busy with one {@link #show} call and their open survey or result dialog;
   * completes once when none remain.
   */
  private static final class Round {
    private final Map<Integer, UIComponent> dialogs = new HashMap<>();
    private final Runnable onComplete;
    private boolean completed;

    private Round(Runnable onComplete) {
      this.onComplete = onComplete;
    }

    void dialog(int player, UIComponent ui) {
      dialogs.put(player, ui);
    }

    /** Callbacks arriving after a player finished or left must not reopen anything. */
    boolean asking(int player) {
      return dialogs.containsKey(player);
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
