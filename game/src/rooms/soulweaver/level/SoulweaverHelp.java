package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.soulweaver.modules.loops.LoopExecution;
import rooms.soulweaver.modules.loops.LoopPuzzle;
import rooms.soulweaver.util.SoulweaverText;

/** Server-owned hint stages release requested tips without publishing future answers. */
public final class SoulweaverHelp {
  public static final String ID = "soulweaver.help";
  private static State received;
  private final SoulweaverGolemRuntime runtime;
  private final Map<String, Integer> puzzles = new LinkedHashMap<>();
  private final List<ReleasedHint> history = new ArrayList<>();

  /**
   * A previously requested hint remains readable after its puzzle is complete.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param title display title
   * @param step released hint stage, starting at one
   * @param text displayed text
   */
  public record ReleasedHint(String puzzleId, String title, int step, String text) {}

  /**
   * Immutable snapshot; eligibility is room-wide, with player permission checked on each action.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param title display title
   * @param goal current puzzle objective
   * @param history previously released hints
   * @param level number of released hint stages
   * @param canRequest whether another hint can be requested
   * @param canSolve whether the automatic solution is available
   * @param status current help availability message
   */
  public record State(
      String puzzleId,
      String title,
      String goal,
      List<ReleasedHint> history,
      int level,
      boolean canRequest,
      boolean canSolve,
      String status) {
    /**
     * Copies the released hints so later requests cannot change this snapshot.
     *
     * @param puzzleId stable room-local puzzle identifier
     * @param title display title
     * @param goal current puzzle objective
     * @param history previously released hints
     * @param level number of released hint stages
     * @param canRequest whether another hint can be requested
     * @param canSolve whether the automatic solution is available
     * @param status current help availability message
     */
    public State {
      history = List.copyOf(history);
    }
  }

  SoulweaverHelp(SoulweaverGolemRuntime runtime) {
    this.runtime = runtime;
    for (String id :
        List.of(
            "vessels",
            "essences",
            "cellar-0",
            "cellar-1",
            "cellar-2",
            "cellar-3",
            "cellar-4",
            "methods",
            "decisions")) puzzles.put(id, 0);
  }

  State snapshot() {
    String id = runtime.helpPuzzle();
    int level = puzzles.get(id);
    boolean ready = runtime.helpReady();
    String status = runtime.helpStatus();
    if (id.startsWith("cellar-") && level == 3 && ready) {
      int checkpoint = Integer.parseInt(id.substring(7));
      String family = LoopPuzzle.challenges().get(checkpoint);
      if (runtime.terminalState().collectedRunes().stream()
          .noneMatch(rune -> rune.startsWith(family + "-")))
        status +=
            " "
                + SoulweaverText.key("help.no-rune")
                + " "
                + SoulweaverText.key("help.rune-location." + checkpoint);
    }
    return new State(
        id,
        title(id),
        goal(id),
        history,
        level,
        ready && level < 3,
        ready && level == 3 && runtime.helpSolvable(),
        status);
  }

  int level(String puzzleId) {
    return puzzles.get(puzzleId);
  }

  void accept(Entity who, String event, String expected) {
    State state = snapshot();
    if (!state.puzzleId().equals(expected) || !runtime.helpAuthorized(who)) return;
    if (event.equals("help.next")) {
      if (!state.canRequest()) return;
      int step = Math.min(3, puzzles.get(expected) + 1);
      puzzles.put(expected, step);
      String hint = hints(expected).get(step - 1);
      history.add(new ReleasedHint(expected, state.title(), step, hint));
      SoulweaverProgress.hint(
          expected,
          "step-" + step,
          SoulweaverText.key("help.tip-title", state.title(), step),
          hint,
          who);
      if (step == 3) SoulweaverProgress.interaction(expected, "simplification", who);
    } else if (event.equals("help.solve")) {
      if (!state.canSolve() || !runtime.helpSolveAuthorized(who)) return;
      SoulweaverProgress.interaction(expected, "solve-confirm", who);
      runtime.solveHelp(who);
    } else {
      String action =
          switch (event) {
            case "help.open" -> "help-open";
            case "help.return" -> "help-return";
            case "help.cancel" -> "solve-cancel";
            case "help.solve.request" -> "solve-request";
            default -> "";
          };
      if (!action.isEmpty()) SoulweaverProgress.interaction(expected, action, who);
    }
  }

  static feature.hud.dialogs.DialogContext.Builder context(
      feature.hud.dialogs.DialogContext.Builder builder) {
    state().ifPresent(state -> builder.put(ID, encode(state)));
    return builder;
  }

  static void callbacks(UIComponent ui, Entity who) {
    ui.registerCallback(
        "help.questlog",
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue value)) return;
          Game.currentLevel()
              .filter(SoulweaverLevel.class::isInstance)
              .map(SoulweaverLevel.class::cast)
              .ifPresent(
                  level -> {
                    var help = level.runtime().help();
                    if (!help.snapshot().puzzleId().equals(value.value())
                        || !level.runtime().helpAuthorized(who)) return;
                    SoulweaverProgress.interaction(value.value(), "questlog-open", who);
                    feature.questlog.QuestLogUI.showQuestLogForPlayers(who.id());
                  });
        });
    for (String event :
        List.of(
            "help.open",
            "help.next",
            "help.solve",
            "help.cancel",
            "help.solve.request",
            "help.return"))
      ui.registerCallback(
          event,
          payload -> {
            if (payload instanceof DialogResponseMessage.StringValue value)
              Game.currentLevel()
                  .filter(SoulweaverLevel.class::isInstance)
                  .map(SoulweaverLevel.class::cast)
                  .ifPresent(level -> level.runtime().help().accept(who, event, value.value()));
          });
  }

  /**
   * Current authoritative state on the host, or the most recent client snapshot.
   *
   * @return current help snapshot, or empty when no host or client snapshot is available
   */
  public static Optional<State> state() {
    return Game.currentLevel()
        .filter(SoulweaverLevel.class::isInstance)
        .map(SoulweaverLevel.class::cast)
        .map(SoulweaverLevel::runtime)
        .map(SoulweaverGolemRuntime::help)
        .map(SoulweaverHelp::snapshot)
        .or(() -> Optional.ofNullable(received));
  }

  /**
   * Serializes released hint references; each client resolves their static titles and texts.
   *
   * @param state authoritative help snapshot
   * @return compact help snapshot
   */
  public static String encode(State state) {
    return SoulweaverStateCodec.encode(
        new WireState(
            state.puzzleId(),
            state.history().stream()
                .map(hint -> new HintRef(hint.puzzleId(), hint.step()))
                .toList(),
            state.level(),
            state.canRequest(),
            state.canSolve(),
            state.status()));
  }

  private record HintRef(String puzzleId, int step) {}

  private record WireState(
      String puzzleId,
      List<HintRef> history,
      int level,
      boolean canRequest,
      boolean canSolve,
      String status) {}

  /**
   * Stores the latest help snapshot received from the host.
   *
   * @param value serialized help snapshot
   */
  public static void receive(String value) {
    WireState state = SoulweaverStateCodec.decode(value, WireState.class);
    received =
        new State(
            state.puzzleId(),
            title(state.puzzleId()),
            goal(state.puzzleId()),
            state.history().stream()
                .map(
                    hint ->
                        new ReleasedHint(
                            hint.puzzleId(),
                            title(hint.puzzleId()),
                            hint.step(),
                            hints(hint.puzzleId()).get(hint.step() - 1)))
                .toList(),
            state.level(),
            state.canRequest(),
            state.canSolve(),
            state.status());
  }

  /** Clears the cached client help snapshot. */
  public static void reset() {
    received = null;
  }

  /** Used only for an explicitly released simplification or a confirmed solve. */
  private static final Map<Integer, String> RECOMMENDED_RUNES = new java.util.HashMap<>();

  /**
   * Finds a rune that solves the requested cellar checkpoint.
   *
   * @param checkpoint zero-based cellar checkpoint
   * @return identifier of a rune verified by the loop interpreter
   */
  public static String recommendedRune(int checkpoint) {
    return RECOMMENDED_RUNES.computeIfAbsent(checkpoint, SoulweaverHelp::findRecommendedRune);
  }

  private static String findRecommendedRune(int checkpoint) {
    return LoopPuzzle.runes().stream()
        .filter(
            rune -> {
              var execution = new LoopExecution(checkpoint, rune, checkpoint <= 2);
              while (execution.next().isPresent()) execution.complete();
              return execution.success();
            })
        .findFirst()
        .orElseThrow()
        .id();
  }

  private static String title(String id) {
    return id.startsWith("cellar-")
        ? SoulweaverText.key("help.title.cellar", Integer.parseInt(id.substring(7)) + 1)
        : SoulweaverText.key("help.title." + id);
  }

  private static String goal(String id) {
    return SoulweaverText.key("help.goal." + (id.startsWith("cellar-") ? "cellar" : id));
  }

  private static List<String> hints(String id) {
    if (!id.startsWith("cellar-"))
      return java.util.stream.IntStream.rangeClosed(1, 3)
          .mapToObj(step -> SoulweaverText.key("help.hint." + id + "." + step))
          .toList();
    return List.of(
        SoulweaverText.key("help.hint.cellar.1"),
        SoulweaverText.key("help.hint.cellar.2." + id.substring(7)),
        SoulweaverText.key("help.hint.cellar.3"));
  }
}
