package rooms.systemRecovery.util.interpreter;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;

/**
 * Guards System Recovery terminal submissions with the room's authoritative learning progress.
 *
 * <p>The generic {@link TerminalInterpreter} deliberately knows nothing about the Petri net. This
 * room-specific controller is the adapter between both systems: it permits a submission only when
 * the interpreter's current terminal step is also the active learning step. Physical tasks can
 * therefore pause terminal progression without coupling reusable parsing code to this room.
 */
public final class SystemRecoveryTerminalController {

  private final TerminalInterpreter interpreter;
  private final Supplier<Optional<SystemRecoveryLearningStep>> activeStep;
  private final Consumer<TerminalAttempt> onUnavailableStep;

  /** Creates the production controller for the shared System Recovery terminal. */
  public SystemRecoveryTerminalController() {
    this(
        TerminalInterpreter.instance(),
        SystemRecoveryProgressNet::activeStep,
        InterpretationCallbacks::onIncorrectTerminalInput);
  }

  /**
   * Creates a controller with explicit collaborators for focused tests.
   *
   * @param interpreter reusable source-code interpreter
   * @param activeStep authoritative provider of the currently active learning step
   * @param onUnavailableStep feedback for valid-looking input submitted out of sequence
   */
  SystemRecoveryTerminalController(
      TerminalInterpreter interpreter,
      Supplier<Optional<SystemRecoveryLearningStep>> activeStep,
      Consumer<TerminalAttempt> onUnavailableStep) {
    this.interpreter = Objects.requireNonNull(interpreter);
    this.activeStep = Objects.requireNonNull(activeStep);
    this.onUnavailableStep = Objects.requireNonNull(onUnavailableStep);
  }

  /**
   * Interprets source only when the corresponding terminal-backed learning step is active.
   *
   * @param source complete source submitted by the player
   * @param playerId authoritative player ID
   * @return whether the source was accepted by the interpreter
   */
  public synchronized boolean interpret(String source, int playerId) {
    return interpret(source, playerId, null);
  }

  /**
   * Interprets a submission while preserving the originating dialog ID for feedback routing.
   *
   * @param source complete source submitted by the player
   * @param playerId authoritative player ID
   * @param dialogId originating dialog ID, or {@code null}
   * @return whether the source was accepted by the interpreter
   */
  public synchronized boolean interpret(String source, int playerId, String dialogId) {
    int state = interpreter.currentState();
    if (!currentTerminalStepIsActive(state)) {
      onUnavailableStep.accept(new TerminalAttempt(state, source, playerId, dialogId));
      return false;
    }
    return interpreter.interpret(source, playerId, dialogId);
  }

  private boolean currentTerminalStepIsActive(int state) {
    Optional<TerminalStep> terminalStep = TerminalStep.fromStateId(state);
    if (terminalStep.isEmpty()
        || terminalStep.orElseThrow().inputMode() != TerminalStep.InputMode.TERMINAL) {
      return false;
    }

    Optional<SystemRecoveryLearningStep> requiredStep =
        SystemRecoveryLearningStep.fromTerminalStep(terminalStep.orElseThrow());
    return requiredStep.isPresent() && requiredStep.equals(activeStep.get());
  }
}
