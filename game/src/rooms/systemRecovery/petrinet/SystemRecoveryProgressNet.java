package rooms.systemRecovery.petrinet;

import engine.Entity;
import engine.Game;
import feature.hints.HintComponent;
import feature.petrinet.PetriNetSystem;
import feature.petrinet.PlaceComponent;
import feature.petrinet.TransitionComponent;
import java.util.EnumMap;
import java.util.Optional;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

import static rooms.systemRecovery.petrinet.SystemRecoveryProgressPlace.*;

/**
 * Server-side Petri net for the complete System Recovery progression.
 *
 * <p>State places are ECS entities containing a {@link PlaceComponent} and a {@link
 * HintComponent}. Event places are deliberately internal components without hints; they are only
 * the input bridge from authoritative riddle callbacks into the existing generic {@link
 * PetriNetSystem}. World effects, dialogs, tracking and terminal validation remain outside this
 * class.
 */
public final class SystemRecoveryProgressNet {

  private static SystemRecoveryProgressNet instance;

  private final PetriNetSystem petriNet;
  private final EnumMap<SystemRecoveryProgressPlace, PlaceBinding> states =
      new EnumMap<>(SystemRecoveryProgressPlace.class);
  private final EnumMap<ProgressEvent, PlaceComponent> events =
      new EnumMap<>(ProgressEvent.class);
  private final EnumMap<ProgressEvent, TransitionRoute> routes =
      new EnumMap<>(ProgressEvent.class);
  private final PlaceComponent accessGranted = new PlaceComponent();
  /**
   * Mirror of the one player-facing token currently used by the telephone.
   *
   * <p>The generic Petri-net system still owns the actual places and transitions. This mirror
   * makes event handling deterministic when an interaction callback and the regular ECS tick
   * happen in the same frame.
   */
  private SystemRecoveryProgressPlace activeState;

  private SystemRecoveryProgressNet(PetriNetSystem petriNet) {
    this.petriNet = petriNet;
    createStatePlaces();
    createEventPlaces();
    connectStates();
    activeState = SystemRecoveryProgressPlace.OPENING_CALL_PENDING;
    states.get(activeState).place().produce();
  }

  /** Initializes the room net once after the authoritative level has been created. */
  public static void initialize() {
    if (instance != null) return;
    Game.system(PetriNetSystem.class, system -> instance = new SystemRecoveryProgressNet(system));
  }

  /**
   * Resets the room-scoped progress net before a new System Recovery level instance is created.
   *
   * <p>The generic Petri-net system only clears transition bindings. The state entities created by
   * this class therefore have to be removed explicitly as well, otherwise a level restart could
   * leave stale tokens behind.
   */
  public static void reset() {
    if (instance == null) return;
    instance.petriNet.clear();
    instance.states.values().forEach(binding -> Game.remove(binding.entity()));
    instance = null;
  }

  /** Returns the active player-facing place, if the net has been initialized. */
  public static Optional<SystemRecoveryProgressPlace> activePlace() {
    if (instance == null) return Optional.empty();
    return Optional.of(instance.activeState);
  }

  /**
   * Creates a human-readable snapshot of all state and event places.
   *
   * <p>The snapshot is intentionally assembled on the authoritative server. A debug client only
   * receives the resulting text and cannot inspect or mutate the Petri net directly.
   *
   * @return token counts for every player-facing place and internal event place
   */
  public static String debugSnapshot() {
    if (instance == null) {
      return SystemRecoveryText.text("computer.debug-petri-net-not-initialized");
    }
    return instance.createDebugSnapshot();
  }

  /** Emits the event raised when the opening telephone call has been completed. */
  public static void openingCallFinished() {
    emit(ProgressEvent.PHONE_CALL_FINISHED, SystemRecoveryProgressPlace.OPENING_CALL_PENDING);
  }

  /** Emits the state transition caused by a successful terminal submission. */
  public static void terminalSuccess(int state) {
    if (instance == null) return;
    switch (state) {
      case 0 -> emit(ProgressEvent.R1_ARRAY_ACCEPTED, SystemRecoveryProgressPlace.R1_ARRAY);
      case 1 -> emit(ProgressEvent.R1_VALUES_ACCEPTED, SystemRecoveryProgressPlace.R1_VALUES);
      case 2 -> emit(ProgressEvent.R2_ARRAY_ACCEPTED, SystemRecoveryProgressPlace.R2_ARRAY);
      case 3 -> emit(ProgressEvent.R2_VALUES_ACCEPTED, SystemRecoveryProgressPlace.R2_VALUES);
      case 4 -> emit(ProgressEvent.R2_GPU_REMOVED, SystemRecoveryProgressPlace.R2_GPU);
      case 5 -> emit(ProgressEvent.R2_LENGTH_READ, SystemRecoveryProgressPlace.R2_LENGTH);
      case 6 -> emit(ProgressEvent.R3_COUNT_ACCEPTED, SystemRecoveryProgressPlace.R3_COUNT);
      case 7 -> emit(ProgressEvent.R4_ARRAY_ACCEPTED, SystemRecoveryProgressPlace.R4_ARRAY);
      case 8 -> emit(ProgressEvent.R4_COLLECT_ACCEPTED, SystemRecoveryProgressPlace.R4_COLLECT);
      case 9 -> emit(ProgressEvent.R7_ARRAYS_ACCEPTED, SystemRecoveryProgressPlace.R7_ARCHIVE);
      case 10 -> emit(ProgressEvent.R8_ARRAY_ACCEPTED, SystemRecoveryProgressPlace.R8_ARRAY);
      case 11 -> emit(ProgressEvent.R8_VALUES_ACCEPTED, SystemRecoveryProgressPlace.R8_VALUES);
      case 13 -> emit(ProgressEvent.R10_SORT_ACCEPTED, SystemRecoveryProgressPlace.R10_SORT);
      case 14 -> emit(ProgressEvent.R10_COUNT_ACCEPTED, SystemRecoveryProgressPlace.R10_COUNT);
      case 15 -> emit(ProgressEvent.R10_SEARCH_ACCEPTED, SystemRecoveryProgressPlace.R10_SEARCH);
      case 16 -> emit(ProgressEvent.R10_META_ACCEPTED, SystemRecoveryProgressPlace.R10_META);
      default -> {}
    }
  }

  /**
   * Jumps the progress net along with a debug-only terminal step.
   *
   * <p>Debug mode may intentionally skip physical actions such as opening a door or waiting for
   * an animation. The normal event transitions must remain strict, so this separate method is only
   * called by the debug button after the terminal callback has completed.
   *
   * @param completedTerminalState terminal state that was skipped
   */
  public static void debugAdvanceAfterTerminal(int completedTerminalState) {
    if (instance == null) return;
    SystemRecoveryProgressPlace target = debugTargetAfter(completedTerminalState);
    if (target == null) return;
    instance.clearTokens(instance.states.values().stream().map(PlaceBinding::place));
    instance.clearTokens(instance.events.values().stream());
    instance.activate(target);
  }

  /** Emits a successful physical interaction or chip operation. */
  public static void successfulInteraction(
      SystemRecoveryPuzzle puzzle, String answerKind) {
    if (instance == null || puzzle == null || answerKind == null) return;

    switch (puzzle) {
      case ENERGY ->
          emitIfAnswer(
              answerKind,
              "battery-inserted",
              ProgressEvent.R1_BATTERY_INSERTED,
              SystemRecoveryProgressPlace.R1_BATTERY);
      case MODULE_STORAGE -> {
        emitIfAnswer(
            answerKind,
            "inspect-length",
            ProgressEvent.R2_DISPLAY_INSPECTED,
            SystemRecoveryProgressPlace.R2_DISPLAY);
        emitIfAnswer(
            answerKind, "5", ProgressEvent.R2_DOOR_OPENED, SystemRecoveryProgressPlace.R2_DOOR);
      }
      case INVENTORY_SCANNER -> {
        emitIfAnswer(
            answerKind, "lever", ProgressEvent.R3_LEVER_PULLED, SystemRecoveryProgressPlace.R3_LEVER);
        emitIfAnswer(
            answerKind, "4", ProgressEvent.R3_DOOR_OPENED, SystemRecoveryProgressPlace.R3_DOOR);
      }
      case TRANSPORT_STORAGE ->
          emitIfAnswer(
              answerKind,
              "all-packages-collected",
              ProgressEvent.R4_PACKAGES_COLLECTED,
              SystemRecoveryProgressPlace.R4_COLLECT);
      case BUBBLE_SORT -> {
        emitIfAnswer(
            answerKind, "insert", ProgressEvent.R6_STICK_INSERTED, SystemRecoveryProgressPlace.R6_STICK);
        emitIfAnswer(
            answerKind, "source", ProgressEvent.R6_CODE_PROGRAMMED, SystemRecoveryProgressPlace.R6_CODE);
      }
      case TWO_DIMENSIONAL_STORAGE -> {
        emitIfAnswer(
            answerKind,
            "matrix-filled",
            ProgressEvent.R8_VALUES_ACCEPTED,
            SystemRecoveryProgressPlace.R8_VALUES);
        emitIfAnswer(
            answerKind,
            "chip-delivered",
            ProgressEvent.R8_CHIP_DELIVERED,
            SystemRecoveryProgressPlace.R8_CHIP);
      }
      case SEARCH_ROBOT -> {
        emitIfAnswer(
            answerKind, "search-program", ProgressEvent.R9_CODE_PROGRAMMED, SystemRecoveryProgressPlace.R9_PROGRAM);
        emitIfAnswer(
            answerKind, "insert", ProgressEvent.R9_CONTROLLER_INSERTED, SystemRecoveryProgressPlace.R9_CONTROLLER);
      }
      case SYSTEM_CORE ->
          emitIfAnswer(
              answerKind,
              "execute",
              ProgressEvent.SYSTEM_ACCESS_GRANTED,
              SystemRecoveryProgressPlace.SYSTEM_ACCESS);
      default -> {}
    }
  }

  /** Emits the completion event for a physical riddle sequence. */
  public static void solved(SystemRecoveryPuzzle puzzle) {
    if (instance == null || puzzle == null) return;
    switch (puzzle) {
      case INVENTORY_SCANNER ->
          emit(ProgressEvent.R3_SCAN_COMPLETED, SystemRecoveryProgressPlace.R3_SCAN);
      case MANUAL_SORTING ->
          emit(ProgressEvent.R5_SORT_COMPLETED, SystemRecoveryProgressPlace.R5_COMPARE);
      case BUBBLE_SORT ->
          emit(ProgressEvent.R6_MACHINE_COMPLETED, SystemRecoveryProgressPlace.R6_MACHINE);
      case SEARCH_ROBOT ->
          emit(ProgressEvent.R9_SCAN_COMPLETED, SystemRecoveryProgressPlace.R9_SCAN);
      case SYSTEM_CORE -> {}
      default -> {}
    }
  }

  private static void emitIfAnswer(
      String answerKind,
      String expectedAnswer,
      ProgressEvent event,
      SystemRecoveryProgressPlace expectedPlace) {
    if (expectedAnswer.equals(answerKind)) emit(event, expectedPlace);
  }

  private static void emit(ProgressEvent event, SystemRecoveryProgressPlace expectedPlace) {
    if (instance == null || !instance.hasToken(expectedPlace)) return;
    PlaceComponent eventPlace = instance.events.get(event);
    if (eventPlace == null || eventPlace.tokenCount() != 0) return;

    eventPlace.produce();

    // UI interactions can emit multiple authoritative events before the next ECS tick. Flush the
    // net here so the following event observes the newly active place instead of being discarded.
    instance.petriNet.execute();
    instance.activateAfterTransition(expectedPlace, event);
  }

  private boolean hasToken(SystemRecoveryProgressPlace state) {
    return states.get(state).place().tokenCount() > 0;
  }

  /**
   * Confirms the state produced by the generic Petri-net system after an event has fired.
   *
   * <p>Callbacks are authoritative and may be invoked from a network message between two regular
   * ECS ticks. If a transition was not observed in this callback, the declared route is repaired
   * here. This keeps the hint phone and the visible riddle progression on the same state without
   * allowing an event to skip a required step.
   */
  private void activateAfterTransition(
      SystemRecoveryProgressPlace expectedPlace, ProgressEvent event) {
    TransitionRoute route = routes.get(event);
    if (route == null || route.from() != expectedPlace) return;

    if (hasToken(route.to())) {
      activeState = route.to();
      return;
    }

    // Restore the one-token invariant from the same declared route and discard stale event tokens.
    clearTokens(states.values().stream().map(PlaceBinding::place));
    clearTokens(events.values().stream());
    activate(route.to());
  }

  /** Activates exactly one player-facing state place. */
  private void activate(SystemRecoveryProgressPlace state) {
    clearTokens(states.values().stream().map(PlaceBinding::place));
    activeState = state;
    states.get(state).place().produce();
  }

  private void clearTokens(java.util.stream.Stream<PlaceComponent> places) {
    places.forEach(place -> place.consume(place.tokenCount()));
  }

  private String createDebugSnapshot() {
    StringBuilder snapshot = new StringBuilder();
    snapshot.append(SystemRecoveryText.text("computer.debug-petri-net-header")).append("\n\n");
    snapshot.append(SystemRecoveryText.text("computer.debug-petri-net-states")).append("\n");
    states.forEach(
        (state, binding) ->
            appendTokenLine(
                snapshot,
                state.name(),
                binding.place().tokenCount(),
                state.riddleKey() + " / " + state.hintKey()));
    snapshot.append("\n").append(SystemRecoveryText.text("computer.debug-petri-net-events"));
    snapshot.append("\n");
    events.forEach(
        (event, place) ->
            appendTokenLine(
                snapshot,
                event.name(),
                place.tokenCount(),
                SystemRecoveryText.text("computer.debug-petri-net-event")));
    snapshot.append("\n").append(SystemRecoveryText.text("computer.debug-petri-net-footer"));
    return snapshot.toString();
  }

  private static void appendTokenLine(
      StringBuilder snapshot, String name, int tokenCount, String description) {
    snapshot
        .append(
            tokenCount > 0
                ? SystemRecoveryText.text("computer.debug-petri-net-token", tokenCount) + " "
                : SystemRecoveryText.text("computer.debug-petri-net-empty") + " ")
        .append(name)
        .append("  (" + description + ")\n");
  }

  private static SystemRecoveryProgressPlace debugTargetAfter(int completedTerminalState) {
    return switch (completedTerminalState) {
      case 0 -> R1_VALUES;
      case 1 -> R1_BATTERY;
      case 2 -> R2_VALUES;
      case 3 -> R2_GPU;
      case 4 -> R2_LENGTH;
      case 5 -> R3_COUNT;
      case 6 -> R4_ARRAY;
      case 7 -> R4_COLLECT;
      case 8 -> R5_COMPARE;
      case 9 -> R8_ARRAY;
      case 10 -> R8_VALUES;
      case 11 -> R9_PROGRAM;
      case 12 -> R10_SORT;
      case 13 -> R10_COUNT;
      case 14 -> R10_SEARCH;
      case 15 -> R10_META;
      case 16 -> R10_FINAL;
      default -> null;
    };
  }

  private void createStatePlaces() {
    for (SystemRecoveryProgressPlace state : SystemRecoveryProgressPlace.values()) {
      Entity entity = new Entity("system-recovery-place-" + state.hintKey());
      PlaceComponent place = new PlaceComponent();
      entity.add(place);
      entity.add(new HintComponent(SystemRecoveryHintCatalog.hints(state)));
      Game.add(entity);
      states.put(state, new PlaceBinding(entity, place));
    }
  }

  private void createEventPlaces() {
    for (ProgressEvent event : ProgressEvent.values()) {
      events.put(event, new PlaceComponent());
    }
  }

  private void connectStates() {
    connect(OPENING_CALL_PENDING, ProgressEvent.PHONE_CALL_FINISHED, R1_ARRAY);
    connect(R1_ARRAY, ProgressEvent.R1_ARRAY_ACCEPTED, R1_VALUES);
    connect(R1_VALUES, ProgressEvent.R1_VALUES_ACCEPTED, R1_BATTERY);
    connect(R1_BATTERY, ProgressEvent.R1_BATTERY_INSERTED, R2_ARRAY);
    connect(R2_ARRAY, ProgressEvent.R2_ARRAY_ACCEPTED, R2_VALUES);
    connect(R2_VALUES, ProgressEvent.R2_VALUES_ACCEPTED, R2_GPU);
    connect(R2_GPU, ProgressEvent.R2_GPU_REMOVED, R2_LENGTH);
    connect(R2_LENGTH, ProgressEvent.R2_LENGTH_READ, R2_DISPLAY);
    connect(R2_DISPLAY, ProgressEvent.R2_DISPLAY_INSPECTED, R2_DOOR);
    connect(R2_DOOR, ProgressEvent.R2_DOOR_OPENED, R3_COUNT);
    connect(R3_COUNT, ProgressEvent.R3_COUNT_ACCEPTED, R3_LEVER);
    connect(R3_LEVER, ProgressEvent.R3_LEVER_PULLED, R3_SCAN);
    connect(R3_SCAN, ProgressEvent.R3_SCAN_COMPLETED, R3_DOOR);
    connect(R3_DOOR, ProgressEvent.R3_DOOR_OPENED, R4_ARRAY);
    connect(R4_ARRAY, ProgressEvent.R4_ARRAY_ACCEPTED, R4_COLLECT);
    connect(R4_COLLECT, ProgressEvent.R4_COLLECT_ACCEPTED, R4_COLLECT);
    connect(R4_COLLECT, ProgressEvent.R4_PACKAGES_COLLECTED, R5_COMPARE);
    connect(R5_COMPARE, ProgressEvent.R5_SORT_COMPLETED, R6_STICK);
    connect(R6_STICK, ProgressEvent.R6_STICK_INSERTED, R6_CODE);
    connect(R6_CODE, ProgressEvent.R6_CODE_PROGRAMMED, R6_MACHINE);
    connect(R6_MACHINE, ProgressEvent.R6_MACHINE_COMPLETED, R7_ARCHIVE);
    connect(R7_ARCHIVE, ProgressEvent.R7_ARRAYS_ACCEPTED, R8_ARRAY);
    connect(R8_ARRAY, ProgressEvent.R8_ARRAY_ACCEPTED, R8_VALUES);
    connect(R8_VALUES, ProgressEvent.R8_VALUES_ACCEPTED, R8_CHIP);
    connect(R8_CHIP, ProgressEvent.R8_CHIP_DELIVERED, R9_PROGRAM);
    connect(R9_PROGRAM, ProgressEvent.R9_CODE_PROGRAMMED, R9_CONTROLLER);
    connect(R9_CONTROLLER, ProgressEvent.R9_CONTROLLER_INSERTED, R9_SCAN);
    connect(R10_SORT, ProgressEvent.R10_SORT_ACCEPTED, R10_COUNT);
    connect(R10_COUNT, ProgressEvent.R10_COUNT_ACCEPTED, R10_SEARCH);
    connect(R10_SEARCH, ProgressEvent.R10_SEARCH_ACCEPTED, R10_META);
    connect(R10_META, ProgressEvent.R10_META_ACCEPTED, R10_FINAL);
    connectWithAdditionalOutput(
        R9_SCAN,
        ProgressEvent.R9_SCAN_COMPLETED,
        R10_SORT,
        SYSTEM_ACCESS);
    connectToInternal(
        SYSTEM_ACCESS, ProgressEvent.SYSTEM_ACCESS_GRANTED, accessGranted);
    connectPlaces(R10_FINAL, accessGranted, ESCAPE_COMPLETE);
  }

  private void connect(
      SystemRecoveryProgressPlace from,
      ProgressEvent event,
      SystemRecoveryProgressPlace to) {
    routes.put(event, new TransitionRoute(from, to));
    TransitionComponent transition = new TransitionComponent();
    petriNet.addInputArc(transition, states.get(from).place());
    petriNet.addInputArc(transition, events.get(event));
    petriNet.addOutputArc(transition, states.get(to).place());
  }

  private void connectWithAdditionalOutput(
      SystemRecoveryProgressPlace from,
      ProgressEvent event,
      SystemRecoveryProgressPlace firstOutput,
      SystemRecoveryProgressPlace secondOutput) {
    routes.put(event, new TransitionRoute(from, firstOutput));
    TransitionComponent transition = new TransitionComponent();
    petriNet.addInputArc(transition, states.get(from).place());
    petriNet.addInputArc(transition, events.get(event));
    petriNet.addOutputArc(transition, states.get(firstOutput).place());
    petriNet.addOutputArc(transition, states.get(secondOutput).place());
  }

  private void connectToInternal(
      SystemRecoveryProgressPlace from, ProgressEvent event, PlaceComponent output) {
    TransitionComponent transition = new TransitionComponent();
    petriNet.addInputArc(transition, states.get(from).place());
    petriNet.addInputArc(transition, events.get(event));
    petriNet.addOutputArc(transition, output);
  }

  private void connectPlaces(
      SystemRecoveryProgressPlace from,
      PlaceComponent additionalInput,
      SystemRecoveryProgressPlace output) {
    TransitionComponent transition = new TransitionComponent();
    petriNet.addInputArc(transition, states.get(from).place());
    petriNet.addInputArc(transition, additionalInput);
    petriNet.addOutputArc(transition, states.get(output).place());
  }

  private record PlaceBinding(Entity entity, PlaceComponent place) {}

  private record TransitionRoute(
      SystemRecoveryProgressPlace from, SystemRecoveryProgressPlace to) {}

  private enum ProgressEvent {
    PHONE_CALL_FINISHED,
    R1_ARRAY_ACCEPTED,
    R1_VALUES_ACCEPTED,
    R1_BATTERY_INSERTED,
    R2_ARRAY_ACCEPTED,
    R2_VALUES_ACCEPTED,
    R2_GPU_REMOVED,
    R2_LENGTH_READ,
    R2_DISPLAY_INSPECTED,
    R2_DOOR_OPENED,
    R3_COUNT_ACCEPTED,
    R3_LEVER_PULLED,
    R3_SCAN_COMPLETED,
    R3_DOOR_OPENED,
    R4_ARRAY_ACCEPTED,
    R4_COLLECT_ACCEPTED,
    R4_PACKAGES_COLLECTED,
    R5_SORT_COMPLETED,
    R6_STICK_INSERTED,
    R6_CODE_PROGRAMMED,
    R6_MACHINE_COMPLETED,
    R7_ARRAYS_ACCEPTED,
    R8_ARRAY_ACCEPTED,
    R8_VALUES_ACCEPTED,
    R8_CHIP_DELIVERED,
    R9_CODE_PROGRAMMED,
    R9_CONTROLLER_INSERTED,
    R9_SCAN_COMPLETED,
    R10_SORT_ACCEPTED,
    R10_COUNT_ACCEPTED,
    R10_SEARCH_ACCEPTED,
    R10_META_ACCEPTED,
    SYSTEM_ACCESS_GRANTED,
    ESCAPE_COMPLETED
  }
}
