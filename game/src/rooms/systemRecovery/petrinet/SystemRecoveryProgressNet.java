package rooms.systemRecovery.petrinet;

import engine.Entity;
import engine.Game;
import feature.hints.HintComponent;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import feature.petrinet.PlaceComponent;
import feature.petrinet.TransitionComponent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Logger;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Server-authoritative linear Petri net for System Recovery learning progress.
 *
 * <p>Each learning step is represented by its real ECS {@link PlaceComponent}. A stable marking has
 * exactly one token in exactly one place. Completing a step briefly raises its token count to two;
 * the corresponding weighted transition consumes both and creates one token in the next place. The
 * token marking is the only source of truth for {@link #activeStep()}.
 */
public final class SystemRecoveryProgressNet {

  private static final Logger LOGGER = Logger.getLogger(SystemRecoveryProgressNet.class.getName());
  private static final int COMPLETION_INPUT_WEIGHT = 2;

  private static SystemRecoveryProgressNet instance;

  private final PetriNetSystem petriNet;
  private final EnumMap<SystemRecoveryLearningStep, PlaceBinding> places =
      new EnumMap<>(SystemRecoveryLearningStep.class);
  private String lastAcceptedStepKey = "";
  private String lastRejectedStepKey = "";
  private String rejectionReasonKey = "";

  private SystemRecoveryProgressNet(PetriNetSystem petriNet) {
    this.petriNet = petriNet;
    createPlaces();
    connectLinearTransitions();
    places.get(SystemRecoveryLearningStep.ENERGY_ARRAY).place().produce();
  }

  /** Initializes the authoritative room net with {@code ENERGY_ARRAY} as its only token. */
  public static synchronized void initialize() {
    if (instance != null) return;
    Game.system(
        PetriNetSystem.class,
        system -> {
          instance = new SystemRecoveryProgressNet(system);
          SystemRecoveryPuzzleEvents.started(SystemRecoveryPuzzle.ENERGY);
        });
  }

  /** Removes this room's places and transition bindings before a fresh level is created. */
  public static synchronized void reset() {
    if (instance == null) return;
    instance.petriNet.clear();
    instance.places.values().forEach(binding -> Game.remove(binding.entity()));
    instance = null;
  }

  /**
   * Returns the current step by inspecting only the actual ECS place tokens.
   *
   * @return the sole step containing exactly one token, or empty for an invalid marking
   */
  public static synchronized Optional<SystemRecoveryLearningStep> activeStep() {
    return instance == null ? Optional.empty() : instance.findActiveStep();
  }

  /**
   * Returns the ECS entity that owns a learning step's shared hint sequence.
   *
   * @param step learning step whose hint place is requested
   * @return place entity ID, or empty when the net or step is unavailable
   */
  public static synchronized OptionalInt hintEntityId(SystemRecoveryLearningStep step) {
    if (instance == null || step == null) return OptionalInt.empty();
    PlaceBinding binding = instance.places.get(step);
    return binding == null ? OptionalInt.empty() : OptionalInt.of(binding.entity().id());
  }

  /**
   * Completes the expected current learning step and atomically fires its weighted transition.
   *
   * <p>This method must be called only by authoritative server callbacks. Wrong-order, duplicate,
   * terminal, and uninitialized requests do not mutate the marking. If the transition is absent or
   * misconfigured, the exact pre-call marking is restored and {@code false} is returned.
   *
   * @param expectedCurrentStep step whose success callback was just validated
   * @return whether the token moved to the direct successor
   */
  public static synchronized boolean complete(SystemRecoveryLearningStep expectedCurrentStep) {
    if (instance == null) return false;
    if (expectedCurrentStep == null || !expectedCurrentStep.isLearningStep()) {
      return instance.reject(expectedCurrentStep, "invalid-request");
    }
    return instance.completeStep(expectedCurrentStep);
  }

  /**
   * Produces a compact diagnostic view of the current marking for the existing debug UI.
   *
   * @return localized token counts, one line per learning step
   */
  public static synchronized ProgressDebugSnapshot debugSnapshot() {
    if (instance == null) {
      return ProgressDebugSnapshot.fromMarking(Map.of(), -1, 0, 0, "", "", "not-initialized");
    }
    return instance.createDebugSnapshot();
  }

  private boolean completeStep(SystemRecoveryLearningStep expected) {
    Optional<SystemRecoveryLearningStep> active = findActiveStep();
    if (active.isEmpty()) return reject(expected, "invalid-marking");
    if (active.orElseThrow() != expected) return reject(expected, "wrong-order");

    SystemRecoveryLearningStep successor = successorOf(expected);
    if (successor == null) return reject(expected, "terminal-step");
    if (places.get(expected).place().tokenCount() != 1) {
      return reject(expected, "token-invariant");
    }

    int[] before = marking();
    places.get(expected).place().produce();
    petriNet.execute();

    if (findActiveStep().orElse(null) == successor) {
      lastAcceptedStepKey = expected.hintKey();
      SystemRecoveryAchievements.learningStepCompleted(expected);
      trackPuzzleStartIfChanged(expected, successor);
      return true;
    }

    restore(before);
    reject(expected, "transition-failed");
    LOGGER.warning("System Recovery transition failed for " + expected + "; marking rolled back.");
    return false;
  }

  private boolean reject(SystemRecoveryLearningStep expected, String reasonKey) {
    lastRejectedStepKey = expected == null ? "unknown-step" : expected.hintKey();
    rejectionReasonKey = reasonKey;
    return false;
  }

  private Optional<SystemRecoveryLearningStep> findActiveStep() {
    SystemRecoveryLearningStep active = null;
    for (Map.Entry<SystemRecoveryLearningStep, PlaceBinding> entry : places.entrySet()) {
      int tokenCount = entry.getValue().place().tokenCount();
      if (tokenCount == 0) continue;
      if (tokenCount != 1 || active != null) return Optional.empty();
      active = entry.getKey();
    }
    return Optional.ofNullable(active);
  }

  private int[] marking() {
    return Arrays.stream(SystemRecoveryLearningStep.values())
        .mapToInt(step -> places.get(step).place().tokenCount())
        .toArray();
  }

  private void restore(int[] marking) {
    SystemRecoveryLearningStep[] steps = SystemRecoveryLearningStep.values();
    for (int index = 0; index < steps.length; index++) {
      PlaceComponent place = places.get(steps[index]).place();
      int current = place.tokenCount();
      int expected = marking[index];
      if (current > expected) place.consume(current - expected);
      else if (current < expected) place.produce(expected - current);
    }
  }

  private void trackPuzzleStartIfChanged(
      SystemRecoveryLearningStep completed, SystemRecoveryLearningStep activated) {
    if (completed.puzzle().equals(activated.puzzle()) || activated.puzzle().isEmpty()) return;
    activated
        .puzzle()
        .ifPresent(
            puzzle ->
                rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents.started(puzzle));
  }

  private ProgressDebugSnapshot createDebugSnapshot() {
    EnumMap<SystemRecoveryLearningStep, Integer> marking =
        new EnumMap<>(SystemRecoveryLearningStep.class);
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      marking.put(step, places.get(step).place().tokenCount());
    }

    int hintIndex = 0;
    int hintCount = 0;
    Optional<SystemRecoveryLearningStep> active = findActiveStep();
    if (active.isPresent()) {
      PlaceBinding activeBinding = places.get(active.orElseThrow());
      HintSystem.SharedHintProgress[] sharedProgress = {new HintSystem.SharedHintProgress(0, 0)};
      Game.system(
          HintSystem.class,
          system -> sharedProgress[0] = system.sharedProgress(activeBinding.entity()));
      hintIndex = sharedProgress[0].acceptedCount();
      hintCount = sharedProgress[0].hintCount();
    }

    return ProgressDebugSnapshot.fromMarking(
        marking,
        TerminalInterpreter.instance().currentState(),
        hintIndex,
        hintCount,
        lastAcceptedStepKey,
        lastRejectedStepKey,
        rejectionReasonKey);
  }

  private void createPlaces() {
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      Entity entity = new Entity("system-recovery-step-" + step.hintKey());
      PlaceComponent place = new PlaceComponent();
      entity.add(place);
      if (step.isLearningStep()) {
        entity.add(new HintComponent(SystemRecoveryHintCatalog.hints(step)));
      }
      Game.add(entity);
      places.put(step, new PlaceBinding(entity, place));
    }
  }

  private void connectLinearTransitions() {
    SystemRecoveryLearningStep[] steps = SystemRecoveryLearningStep.values();
    for (int index = 0; index < steps.length - 1; index++) {
      SystemRecoveryLearningStep current = steps[index];
      SystemRecoveryLearningStep next = steps[index + 1];
      TransitionComponent transition = new TransitionComponent();
      petriNet.addInputArc(transition, places.get(current).place(), COMPLETION_INPUT_WEIGHT);
      petriNet.addOutputArc(transition, places.get(next).place());
    }
  }

  private static SystemRecoveryLearningStep successorOf(SystemRecoveryLearningStep step) {
    int nextOrdinal = step.ordinal() + 1;
    SystemRecoveryLearningStep[] steps = SystemRecoveryLearningStep.values();
    return nextOrdinal < steps.length ? steps[nextOrdinal] : null;
  }

  static synchronized int tokenCount(SystemRecoveryLearningStep step) {
    if (instance == null || step == null) return 0;
    return instance.places.get(step).place().tokenCount();
  }

  private record PlaceBinding(Entity entity, PlaceComponent place) {}
}
