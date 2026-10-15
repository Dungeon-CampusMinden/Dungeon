package feature.survey;

import com.badlogic.gdx.Gdx;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.tracking.Tracking;
import engine.tracking.TrackingRuntime;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;

/**
 * Asks each player one of the room's surveys before or after the game and records the answers as
 * {@code SURVEY_ANSWERED} tracking events.
 *
 * <p>Register the dialog in every runtime with {@link #register()}, then call {@link #show} on the
 * authoritative side: with {@link Phase#PRE} before the intro of a new run and the intro as {@code
 * onComplete}, with {@link Phase#POST} as the last step after the room outcome and the action that
 * ends the game as {@code onComplete}.
 */
public final class SurveyFeature {

  static final String FILE_KEY = "surveyFile";
  static final String PHASE_KEY = "surveyPhase";
  static final String RESULT_KEY = "surveyResult";
  static final String OUTBOX_KEY = "surveyOutbox";
  static final String OPERATOR_EMAIL_KEY = "surveyOperatorEmail";

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

  /**
   * When a survey runs. {@code surveys/<phase>-<roomId>.json} holds a room's questions for the
   * phase; for A/B tests, {@code surveys/<phase>-a-<roomId>.json} and {@code
   * surveys/<phase>-b-<roomId>.json} replace it in the runs of their variant.
   */
  public enum Phase {
    /** Before the intro of a new run; closing it starts the game. */
    PRE,
    /** As the last step after the room outcome; closing it ends the game. */
    POST;

    /**
     * Returns the asset path of a room's survey in this phase for one run: the file of the run's
     * variant when it exists, otherwise the room's file for the phase.
     *
     * @param roomId tracking room ID
     * @param runId run identifier; it assigns half of the runs to variant a and the other half to
     *     b, and a loaded run keeps its variant
     * @return path such as {@code surveys/pre-a-system-recovery.json}
     */
    String file(String roomId, UUID runId) {
      String prefix = "surveys/" + name().toLowerCase(Locale.ROOT) + "-";
      String variant = (runId.getLeastSignificantBits() & 1) == 0 ? "a" : "b";
      String variantFile = prefix + variant + "-" + roomId + ".json";
      if (Gdx.files != null && Gdx.files.internal(variantFile).exists()) return variantFile;
      return prefix + roomId + ".json";
    }
  }

  /** Storage state reported to a player after submitting. */
  enum Result {
    /** The tracking backend acknowledged every answer. */
    CONFIRMED,
    /** The deployment stores tracking data only in the local outbox. */
    SAVED_LOCALLY,
    /** The backend failed its health check; the answers are only in the local outbox. */
    UNREACHABLE,
    /** The backend is healthy but did not acknowledge in time; the outbox keeps the answers. */
    PENDING,
    /** The answers were rejected or could not be recorded. */
    FAILED
  }

  /** Registers the survey dialog with the shared dialog factory. */
  public static void register() {
    DialogFactory.register(Type.SURVEY, SurveyDialog::build);
  }

  /**
   * Shows the tracked room's survey of one phase to each player and runs {@code onComplete} once
   * each of them is done: they skipped it, sent no answers, declined consent, closed the storage
   * result after submitting, or left the game.
   *
   * <p>The survey is the {@link Phase#file} of the room configured through {@link
   * Tracking#configureRoom(String)}. Each player gets it once their client finished joining,
   * because the tracking session and their participant start only then. A player is done right away
   * when that file is absent or invalid, or tracking does not record them, because answers could
   * not be recorded. Declining consent stops tracking for the run, so later surveys of the run are
   * done right away as well.
   *
   * @param phase whether the survey comes before or after the game
   * @param onComplete action after every player finished the survey
   * @param playerIds players to ask; all players when empty
   */
  public static void show(Phase phase, Runnable onComplete, int... playerIds) {
    Optional<String> file =
        Tracking.roomId().flatMap(roomId -> Tracking.runId().map(run -> phase.file(roomId, run)));
    Round round = new Round(phase, file, file.flatMap(SurveyDefinition::load), onComplete);
    if (playerIds.length == 0) {
      Game.allPlayers().forEach(player -> round.joining.add(player.id()));
    } else {
      for (int id : playerIds) round.joining.add(id);
    }
    SurveySystem.instance().rounds.add(round);
  }

  private static void ask(SurveyDefinition survey, int player, UUID participant, Round round) {
    UIComponent ui =
        round.show(
            player,
            DialogContext.builder()
                .type(Type.SURVEY)
                .put(FILE_KEY, round.file.orElseThrow())
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
            return;
          }
          // Without a configured backend the outbox is the only storage.
          TrackingRuntime.backendHealth()
              .ifPresentOrElse(
                  reachable ->
                      SurveySystem.instance()
                          .await(
                              last.get(), reachable, result -> showResult(result, player, round)),
                  () -> showResult(Result.SAVED_LOCALLY, player, round));
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
   * Validates one submission and records each answered question. A submission that declines consent
   * records nothing and withdraws consent for the run instead.
   *
   * @param survey survey the player answered
   * @param participant tracking participant of the player
   * @param data client response carrying the answers as a JSON object
   * @return the last recorded event, or empty when the submission answered nothing or declined
   *     consent
   * @throws RuntimeException if the answers are invalid or tracking stopped recording
   */
  private static Optional<TrackingEvent> record(
      SurveyDefinition survey, UUID participant, DialogResponseMessage.Payload data) {
    JsonNode answers = TrackingJson.object(((DialogResponseMessage.StringValue) data).value());
    if (survey.declined(answers)) {
      // All players share one session, so this stops tracking for the run like a refusal on join.
      TrackingRuntime.disableTrackingForRun();
      return Optional.empty();
    }
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
    DialogContext.Builder context =
        DialogContext.builder()
            .type(Type.SURVEY)
            .put(RESULT_KEY, result.name())
            .put(PHASE_KEY, round.phase.name())
            .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true);
    // Unconfirmed answers reach the operator only as the outbox file, which is complete only after
    // the game.
    if (round.phase == Phase.POST && (result == Result.UNREACHABLE || result == Result.PENDING)) {
      Tracking.outboxPath()
          .ifPresent(path -> context.put(OUTBOX_KEY, path.toAbsolutePath().toString()));
      Tracking.operatorEmail().ifPresent(email -> context.put(OPERATOR_EMAIL_KEY, email));
    }
    UIComponent ui = round.show(player, context.build());
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> round.finish(player));
  }

  /**
   * Players still busy with one {@link #show} call: those whose client is still joining and those
   * with an open survey or result dialog; completes once when none remain.
   */
  private static final class Round {
    private final Phase phase;
    private final Optional<String> file;
    private final Optional<SurveyDefinition> survey;
    private final Runnable onComplete;
    private final Set<Integer> joining = new HashSet<>();
    private final Map<Integer, UIComponent> dialogs = new HashMap<>();
    private final Set<Integer> answered = new HashSet<>();
    private boolean completed;

    private Round(
        Phase phase,
        Optional<String> file,
        Optional<SurveyDefinition> survey,
        Runnable onComplete) {
      this.phase = phase;
      this.file = file;
      this.survey = survey;
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

    /**
     * Asks players whose client finished joining and drops players who left. Closing the dialogs of
     * departed players keeps a reconnect from restoring them.
     */
    void update() {
      for (int player : List.copyOf(joining)) {
        if (Game.findEntityById(player).isEmpty()) {
          joining.remove(player);
        } else if (Game.playClock().joined(player)) {
          joining.remove(player);
          Optional<UUID> participant =
              Tracking.active() ? Tracking.participantForEntity(player) : Optional.empty();
          if (survey.isPresent() && participant.isPresent()) {
            ask(survey.get(), player, participant.get(), this);
          }
        }
      }
      List.copyOf(dialogs.keySet()).stream()
          .filter(player -> Game.findEntityById(player).isEmpty())
          .forEach(this::finish);
      completeIfDone();
    }

    private void completeIfDone() {
      if (completed || !joining.isEmpty() || !dialogs.isEmpty()) return;
      completed = true;
      onComplete.run();
    }
  }

  /**
   * Runs survey bookkeeping on the game thread: asks players once they joined, notices players who
   * left mid-survey, and polls backend health checks and acknowledgements, because the uploader
   * thread must not touch dialogs and a headless server has no libGDX runnable queue.
   */
  private static final class SurveySystem extends engine.System {
    private final List<Round> rounds = new ArrayList<>();
    private final List<Waiter> waiters = new ArrayList<>();

    /**
     * Waits for the backend to store a submission.
     *
     * @param event last recorded answer of the submission
     * @param reachable result of the backend health check started on submit
     * @param deadlineNanos time after which a healthy backend counts as {@link Result#PENDING}
     * @param onResult receives the storage state once known
     */
    private record Waiter(
        TrackingEvent event,
        CompletableFuture<Boolean> reachable,
        long deadlineNanos,
        Consumer<Result> onResult) {
      Optional<Result> result() {
        if (Tracking.remoteAcknowledged(event)) return Optional.of(Result.CONFIRMED);
        if (!reachable.getNow(true)) return Optional.of(Result.UNREACHABLE);
        if (java.lang.System.nanoTime() >= deadlineNanos) return Optional.of(Result.PENDING);
        return Optional.empty();
      }
    }

    private SurveySystem() {
      super(AuthoritativeSide.SERVER);
    }

    static SurveySystem instance() {
      if (Game.systems().get(SurveySystem.class) instanceof SurveySystem system) return system;
      SurveySystem system = new SurveySystem();
      Game.add(system);
      return system;
    }

    void await(
        TrackingEvent event, CompletableFuture<Boolean> reachable, Consumer<Result> onResult) {
      long deadline = java.lang.System.nanoTime() + ACKNOWLEDGEMENT_TIMEOUT.toNanos();
      waiters.add(new Waiter(event, reachable, deadline, onResult));
    }

    @Override
    public void execute() {
      for (Round round : List.copyOf(rounds)) round.update();
      rounds.removeIf(round -> round.completed);
      for (Waiter waiter : List.copyOf(waiters)) {
        waiter
            .result()
            .ifPresent(
                result -> {
                  waiters.remove(waiter);
                  waiter.onResult().accept(result);
                });
      }
    }

    /** Survey dialogs pause singleplayer systems, but this bookkeeping must keep running. */
    @Override
    public void stop() {}
  }
}
