package escaperoom.foundation.runtime;

import escaperoom.foundation.definition.CollectionInputDefinition;
import escaperoom.foundation.definition.ComposedRiddleDefinition;
import escaperoom.foundation.definition.HintDefinition;
import escaperoom.foundation.definition.InformationSourceDefinition;
import escaperoom.foundation.definition.InputDefinition;
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.ProgressionDefinition;
import escaperoom.foundation.definition.ProgressionDefinition.RiddleNode;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.definition.RosterSlotDefinition;
import escaperoom.foundation.definition.TimerMode;
import escaperoom.foundation.runtime.Projection.InputView;
import escaperoom.foundation.runtime.Projection.ProgressStatus;
import escaperoom.foundation.runtime.Projection.RiddleView;
import escaperoom.foundation.runtime.Projection.TimerState;
import escaperoom.foundation.runtime.Projection.TimerView;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Thread-safe deterministic authority for one server-hosted room session.
 *
 * <p>All time enters through {@link #advance(Duration)}. The authority owns answers and authored
 * unreleased hints; neither is exposed by {@link #projection()}.
 */
public final class Authority {
  private static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999);

  private final RoomDefinition definition;
  private final Map<String, SlotState> slots = new LinkedHashMap<>();
  private final Map<String, RiddleState> riddles = new LinkedHashMap<>();
  private final Map<String, String> riddleNodeIds = new LinkedHashMap<>();
  private final Map<String, RiddleNode> riddleNodes = new LinkedHashMap<>();
  private final Map<String, List<String>> predecessors = new LinkedHashMap<>();
  private final Map<String, List<String>> successors = new LinkedHashMap<>();
  private final Duration limit;
  private boolean started;
  private Duration elapsed = Duration.ZERO;
  private boolean overtime;
  private boolean doorOpen;
  private TerminalResult terminal;

  /**
   * Creates a fresh authority with all riddles locked until session and dependency activation.
   *
   * @param definition immutable Foundation room definition
   */
  public Authority(final RoomDefinition definition) {
    this.definition = Objects.requireNonNull(definition, "definition");
    limit = Duration.ofMinutes(definition.timer().limitMinutes());
    for (RosterSlotDefinition slot : definition.roster().slots()) {
      slots.put(slot.id(), new SlotState());
    }
    ProgressionDefinition progression = definition.progression();
    predecessors.put(progression.startNodeId(), new ArrayList<>());
    predecessors.put(progression.endNodeId(), new ArrayList<>());
    successors.put(progression.startNodeId(), new ArrayList<>());
    successors.put(progression.endNodeId(), new ArrayList<>());
    for (RiddleNode node : progression.riddleNodes()) {
      riddleNodes.put(node.id(), node);
      riddleNodeIds.put(node.riddle().id(), node.id());
      riddles.put(node.riddle().id(), new RiddleState(node.riddle()));
      predecessors.put(node.id(), new ArrayList<>());
      successors.put(node.id(), new ArrayList<>());
    }
    for (ProgressionDefinition.Edge edge : progression.edges()) {
      predecessors.get(edge.to()).add(edge.from());
      successors.get(edge.from()).add(edge.to());
    }
    successors.values().forEach(values -> values.sort(successorOrder()));
  }

  /**
   * Connects one player slot.
   *
   * @param slotId stable slot identifier
   * @return explicit command result
   */
  public synchronized OperationResult connect(final String slotId) {
    SlotState slot = slots.get(slotId);
    if (slot == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_SLOT);
    }
    if (terminal != null) {
      return OperationResult.idempotent(OperationReason.SESSION_TERMINAL);
    }
    if (slot.connected) {
      return OperationResult.idempotent(OperationReason.ALREADY_CONNECTED);
    }
    slot.connected = true;
    startWhenReady();
    evaluateSuccess();
    return OperationResult.applied();
  }

  /**
   * Disconnects one slot without stopping an already started session.
   *
   * @param slotId stable slot identifier
   * @return explicit command result
   */
  public synchronized OperationResult disconnect(final String slotId) {
    SlotState slot = slots.get(slotId);
    if (slot == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_SLOT);
    }
    if (terminal != null) {
      return OperationResult.idempotent(OperationReason.SESSION_TERMINAL);
    }
    if (!slot.connected) {
      return OperationResult.idempotent(OperationReason.ALREADY_DISCONNECTED);
    }
    slot.connected = false;
    slot.inExit = false;
    evaluateSuccess();
    return OperationResult.applied();
  }

  /**
   * Retains explicit spawn readiness for a connected slot.
   *
   * @param slotId stable slot identifier
   * @return explicit command result
   */
  public synchronized OperationResult markSpawned(final String slotId) {
    SlotState slot = slots.get(slotId);
    if (slot == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_SLOT);
    }
    if (terminal != null) {
      return OperationResult.idempotent(OperationReason.SESSION_TERMINAL);
    }
    if (!slot.connected) {
      return OperationResult.rejected(OperationReason.SLOT_DISCONNECTED);
    }
    if (slot.spawned) {
      return OperationResult.idempotent(OperationReason.ALREADY_SPAWNED);
    }
    slot.spawned = true;
    startWhenReady();
    return OperationResult.applied();
  }

  /**
   * Applies one interaction with a readable information source.
   *
   * @param riddleId owning riddle identifier
   * @param informationSourceId stable information source identifier
   * @return explicit command result
   */
  public synchronized OperationResult interactSource(
      final String riddleId, final String informationSourceId) {
    OperationResult gate = gameplayGate();
    if (gate != null) {
      return gate;
    }
    RiddleState state = riddles.get(riddleId);
    if (state == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_RIDDLE);
    }
    if (state.status == ProgressStatus.SOLVED) {
      return OperationResult.idempotent(OperationReason.RIDDLE_ALREADY_COMPLETED);
    }
    if (state.definition.informationSources().stream()
        .map(InformationSourceDefinition::id)
        .noneMatch(informationSourceId::equals)) {
      return OperationResult.rejected(OperationReason.UNKNOWN_INFORMATION_SOURCE);
    }
    if (state.status == ProgressStatus.LOCKED) {
      return OperationResult.idempotent(OperationReason.RIDDLE_LOCKED);
    }
    List<CollectionInputDefinition> matching =
        state.definition.inputs().stream()
            .filter(CollectionInputDefinition.class::isInstance)
            .map(CollectionInputDefinition.class::cast)
            .filter(input -> input.informationSourceId().equals(informationSourceId))
            .toList();
    if (matching.isEmpty()) {
      return OperationResult.idempotent(OperationReason.INFORMATION_SOURCE_ONLY);
    }
    boolean changed = false;
    for (CollectionInputDefinition input : matching) {
      changed |= state.satisfied.add(input.id());
    }
    if (!changed) {
      return OperationResult.idempotent(OperationReason.INPUT_ALREADY_SATISFIED);
    }
    completeIfSatisfied(state);
    return OperationResult.applied();
  }

  /**
   * Evaluates one numeric attempt exactly inside authority without retaining attempt counts.
   *
   * @param riddleId owning riddle identifier
   * @param inputId stable numeric input identifier
   * @param attempt caller-supplied value evaluated by exact equality
   * @return explicit command and non-secret correctness result
   */
  public synchronized CodeAttemptResult attemptCode(
      final String riddleId, final String inputId, final String attempt) {
    OperationResult gate = gameplayGate();
    if (gate != null) {
      return code(gate, CodeOutcome.NOT_EVALUATED);
    }
    RiddleState state = riddles.get(riddleId);
    if (state == null) {
      return code(
          OperationResult.rejected(OperationReason.UNKNOWN_RIDDLE), CodeOutcome.NOT_EVALUATED);
    }
    if (state.status == ProgressStatus.SOLVED) {
      return code(
          OperationResult.idempotent(OperationReason.RIDDLE_ALREADY_COMPLETED),
          CodeOutcome.NOT_EVALUATED);
    }
    if (state.status == ProgressStatus.LOCKED) {
      return code(
          OperationResult.idempotent(OperationReason.RIDDLE_LOCKED), CodeOutcome.NOT_EVALUATED);
    }
    Optional<NumericInputDefinition> numeric =
        state.definition.inputs().stream()
            .filter(input -> input.id().equals(inputId))
            .filter(NumericInputDefinition.class::isInstance)
            .map(NumericInputDefinition.class::cast)
            .findFirst();
    if (numeric.isEmpty()) {
      return code(
          OperationResult.rejected(OperationReason.UNKNOWN_INPUT), CodeOutcome.NOT_EVALUATED);
    }
    if (state.satisfied.contains(inputId)) {
      return code(
          OperationResult.idempotent(OperationReason.INPUT_ALREADY_SATISFIED),
          CodeOutcome.NOT_EVALUATED);
    }
    if (!numeric.orElseThrow().answer().equals(attempt)) {
      return code(OperationResult.applied(), CodeOutcome.INCORRECT);
    }
    state.satisfied.add(inputId);
    completeIfSatisfied(state);
    return code(OperationResult.applied(), CodeOutcome.CORRECT);
  }

  /**
   * Returns only the identity and disclosure category of the next releasable hint.
   *
   * <p>Reading a preview never changes authoritative state or exposes the hint title or text.
   *
   * @param riddleId stable riddle identifier
   * @return next hint preview while the riddle is active and has an unreleased hint
   */
  public synchronized Optional<HintPreview> previewHint(final String riddleId) {
    if (gameplayGate() != null) {
      return Optional.empty();
    }
    RiddleState state = riddles.get(riddleId);
    if (state == null || state.status != ProgressStatus.ACTIVE) {
      return Optional.empty();
    }
    List<HintDefinition> authored = state.definition.hints();
    if (state.releasedHints == authored.size()) {
      return Optional.empty();
    }
    HintDefinition next = authored.get(state.releasedHints);
    return Optional.of(new HintPreview(next.id(), next.severity()));
  }

  /**
   * Releases exactly the previously previewed hint when it is still next.
   *
   * @param riddleId stable riddle identifier
   * @param expectedHintId identity captured by {@link #previewHint(String)}
   * @return command result and newly released hint when applied
   */
  public synchronized HintRevealResult revealHint(
      final String riddleId, final String expectedHintId) {
    Objects.requireNonNull(expectedHintId, "expectedHintId");
    OperationResult gate = gameplayGate();
    if (gate != null) {
      return hint(gate, Optional.empty());
    }
    RiddleState state = riddles.get(riddleId);
    if (state == null) {
      return hint(OperationResult.rejected(OperationReason.UNKNOWN_RIDDLE), Optional.empty());
    }
    if (state.status == ProgressStatus.SOLVED) {
      return hint(
          OperationResult.idempotent(OperationReason.RIDDLE_ALREADY_COMPLETED), Optional.empty());
    }
    if (state.status == ProgressStatus.LOCKED) {
      return hint(OperationResult.idempotent(OperationReason.RIDDLE_LOCKED), Optional.empty());
    }
    List<HintDefinition> authored = state.definition.hints();
    if (state.releasedHints == authored.size()) {
      return hint(OperationResult.idempotent(OperationReason.HINTS_EXHAUSTED), Optional.empty());
    }
    HintDefinition revealed = authored.get(state.releasedHints);
    if (!revealed.id().equals(expectedHintId)) {
      return hint(OperationResult.idempotent(OperationReason.HINT_PREVIEW_STALE), Optional.empty());
    }
    state.releasedHints++;
    return hint(OperationResult.applied(), Optional.of(released(revealed)));
  }

  /**
   * Places one connected roster slot at the common exit when its door is open.
   *
   * @param slotId stable slot identifier
   * @return explicit command result
   */
  public synchronized OperationResult enterExit(final String slotId) {
    OperationResult gate = gameplayGate();
    if (gate != null) {
      return gate;
    }
    SlotState slot = slots.get(slotId);
    if (slot == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_SLOT);
    }
    if (!slot.connected) {
      return OperationResult.rejected(OperationReason.SLOT_DISCONNECTED);
    }
    if (!doorOpen) {
      return OperationResult.rejected(OperationReason.DOOR_CLOSED);
    }
    if (slot.inExit) {
      return OperationResult.idempotent(OperationReason.ALREADY_IN_EXIT);
    }
    slot.inExit = true;
    evaluateSuccess();
    return OperationResult.applied();
  }

  /**
   * Removes one connected roster slot from the common exit before terminal success.
   *
   * @param slotId stable slot identifier
   * @return explicit command result
   */
  public synchronized OperationResult leaveExit(final String slotId) {
    OperationResult gate = gameplayGate();
    if (gate != null) {
      return gate;
    }
    SlotState slot = slots.get(slotId);
    if (slot == null) {
      return OperationResult.rejected(OperationReason.UNKNOWN_SLOT);
    }
    if (!slot.connected) {
      return OperationResult.rejected(OperationReason.SLOT_DISCONNECTED);
    }
    if (!slot.inExit) {
      return OperationResult.idempotent(OperationReason.NOT_IN_EXIT);
    }
    slot.inExit = false;
    return OperationResult.applied();
  }

  /**
   * Advances explicit authoritative time after the session has started.
   *
   * @param duration nonnegative elapsed duration supplied by the local/server adapter
   * @return explicit command result
   */
  public synchronized OperationResult advance(final Duration duration) {
    Objects.requireNonNull(duration, "duration");
    if (duration.isNegative()) {
      return OperationResult.rejected(OperationReason.NEGATIVE_DURATION);
    }
    if (terminal != null) {
      return OperationResult.idempotent(OperationReason.SESSION_TERMINAL);
    }
    if (!started) {
      return OperationResult.rejected(OperationReason.SESSION_NOT_RUNNING);
    }
    if (duration.isZero()) {
      return OperationResult.idempotent(OperationReason.ZERO_DURATION);
    }
    Duration remaining = nonnegative(limit.minus(elapsed));
    if (definition.timer().mode() == TimerMode.HARD && duration.compareTo(remaining) >= 0) {
      elapsed = limit;
      terminal = TerminalResult.HARD_TIMEOUT;
      return OperationResult.applied();
    }
    elapsed = safeAdd(elapsed, duration);
    if (definition.timer().mode() == TimerMode.SOFT && elapsed.compareTo(limit) >= 0) {
      overtime = true;
    }
    return OperationResult.applied();
  }

  /**
   * Aborts a nonterminal session with an immutable terminal result.
   *
   * @return explicit command result
   */
  public synchronized OperationResult abort() {
    if (terminal != null) {
      return OperationResult.idempotent(OperationReason.SESSION_TERMINAL);
    }
    terminal = TerminalResult.ABORTED;
    return OperationResult.applied();
  }

  /**
   * Builds a detached immutable player-visible projection in definition order.
   *
   * @return coherent public authority state
   */
  public synchronized Projection projection() {
    List<RiddleView> views =
        definition.progression().riddleNodes().stream()
            .map(RiddleNode::riddle)
            .map(
                riddle -> {
                  RiddleState state = riddles.get(riddle.id());
                  return new RiddleView(
                      riddle.id(), state.status(), inputViews(state), releasedHints(state));
                })
            .toList();
    Duration publicElapsed = wholeSeconds(elapsed);
    return new Projection(
        views,
        new TimerView(
            started,
            timerState(),
            publicElapsed,
            nonnegative(limit.minus(publicElapsed)),
            overtime),
        doorOpen,
        Optional.ofNullable(terminal));
  }

  private void startWhenReady() {
    long ready = slots.values().stream().filter(slot -> slot.connected && slot.spawned).count();
    if (!started && ready >= definition.minimumPlayers()) {
      started = true;
      evaluateSuccessors(definition.progression().startNodeId());
    }
  }

  private static Duration wholeSeconds(final Duration duration) {
    return Duration.ofSeconds(duration.getSeconds());
  }

  private OperationResult gameplayGate() {
    if (terminal != null) {
      return OperationResult.rejected(OperationReason.SESSION_TERMINAL);
    }
    if (!started) {
      return OperationResult.rejected(OperationReason.SESSION_NOT_RUNNING);
    }
    return null;
  }

  private TimerState timerState() {
    if (terminal != null) {
      return TimerState.TERMINAL;
    }
    if (!started) {
      return TimerState.WAITING_FOR_READY;
    }
    return TimerState.RUNNING;
  }

  private void completeIfSatisfied(final RiddleState state) {
    if (state.definition.inputs().stream()
        .map(InputDefinition::id)
        .allMatch(state.satisfied::contains)) {
      state.status = ProgressStatus.SOLVED;
      evaluateSuccessors(riddleNodeIds.get(state.definition.id()));
    }
  }

  private void evaluateSuccess() {
    if (terminal == null
        && started
        && doorOpen
        && slots.values().stream()
            .filter(slot -> slot.connected && slot.spawned)
            .allMatch(slot -> slot.inExit)
        && slots.values().stream().anyMatch(slot -> slot.connected && slot.spawned)) {
      terminal = TerminalResult.SUCCESS;
    }
  }

  private void evaluateSuccessors(final String completedNodeId) {
    for (String successor : successors.get(completedNodeId)) {
      if (!predecessors.get(successor).stream().allMatch(this::fulfilled)) {
        continue;
      }
      if (successor.equals(definition.progression().endNodeId())) {
        doorOpen = true;
      } else {
        RiddleState state = riddles.get(riddleNodes.get(successor).riddle().id());
        if (state.status == ProgressStatus.LOCKED) {
          state.status = ProgressStatus.ACTIVE;
        }
      }
    }
  }

  private boolean fulfilled(final String nodeId) {
    if (nodeId.equals(definition.progression().startNodeId())) {
      return started;
    }
    RiddleNode node = riddleNodes.get(nodeId);
    return node != null && riddles.get(node.riddle().id()).status == ProgressStatus.SOLVED;
  }

  private Comparator<String> successorOrder() {
    return Comparator.comparing(
            (String nodeId) -> {
              RiddleNode node = riddleNodes.get(nodeId);
              return node == null ? "\uffff" : node.riddle().id();
            })
        .thenComparing(Comparator.naturalOrder());
  }

  private static List<InputView> inputViews(final RiddleState state) {
    return state.definition.inputs().stream()
        .map(
            input ->
                new InputView(
                    input.id(),
                    state.satisfied.contains(input.id()),
                    input instanceof NumericInputDefinition numeric && numeric.showDigitCount()
                        ? Optional.of(numeric.answer().length())
                        : Optional.empty()))
        .toList();
  }

  private static List<ReleasedHint> releasedHints(final RiddleState state) {
    return state.definition.hints().stream()
        .limit(state.releasedHints)
        .map(Authority::released)
        .toList();
  }

  private static ReleasedHint released(final HintDefinition hint) {
    return new ReleasedHint(hint.id(), hint.title(), hint.text(), hint.severity());
  }

  private static CodeAttemptResult code(
      final OperationResult operation, final CodeOutcome outcome) {
    return new CodeAttemptResult(operation, outcome);
  }

  private static HintRevealResult hint(
      final OperationResult operation, final Optional<ReleasedHint> revealed) {
    return new HintRevealResult(operation, revealed);
  }

  private static Duration safeAdd(final Duration left, final Duration right) {
    try {
      return left.plus(right);
    } catch (ArithmeticException exception) {
      return MAX_DURATION;
    }
  }

  private static Duration nonnegative(final Duration duration) {
    return duration.isNegative() ? Duration.ZERO : duration;
  }

  private static final class SlotState {
    private boolean connected;
    private boolean spawned;
    private boolean inExit;
  }

  private static final class RiddleState {
    private final ComposedRiddleDefinition definition;
    private final Set<String> satisfied = new LinkedHashSet<>();
    private int releasedHints;
    private ProgressStatus status = ProgressStatus.LOCKED;

    private RiddleState(final ComposedRiddleDefinition definition) {
      this.definition = definition;
    }

    private ProgressStatus status() {
      return status;
    }
  }
}
