package rooms.programming.state;

import engine.Component;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Shared room progress synchronized from the authoritative server. */
public record ProgrammingStateComponent(
    ProgrammingPhase phase,
    VariablePuzzleStage variableStage,
    Optional<GolemState> golem,
    Set<String> completedLoopChallenges)
    implements Component {

  /** Creates an immutable room state. */
  public ProgrammingStateComponent {
    phase = Objects.requireNonNull(phase, "phase");
    variableStage = Objects.requireNonNull(variableStage, "variableStage");
    golem = Objects.requireNonNull(golem, "golem");
    completedLoopChallenges =
        Set.copyOf(Objects.requireNonNull(completedLoopChallenges, "completedLoopChallenges"));
  }

  /** Creates the initial state of a new room. */
  public static ProgrammingStateComponent initial() {
    return new ProgrammingStateComponent(
        ProgrammingPhase.VARIABLES, VariablePuzzleStage.VESSELS, Optional.empty(), Set.of());
  }

  /** Returns a copy in another phase. */
  public ProgrammingStateComponent withPhase(ProgrammingPhase newPhase) {
    return new ProgrammingStateComponent(newPhase, variableStage, golem, completedLoopChallenges);
  }

  /** Returns a copy in another variable-puzzle stage. */
  public ProgrammingStateComponent withVariableStage(VariablePuzzleStage newStage) {
    return new ProgrammingStateComponent(phase, newStage, golem, completedLoopChallenges);
  }

  /** Returns a copy with the solved golem values. */
  public ProgrammingStateComponent withGolem(GolemState newGolem) {
    return new ProgrammingStateComponent(
        phase, variableStage, Optional.of(newGolem), completedLoopChallenges);
  }

  /** Returns a copy with one completed loop situation. */
  public ProgrammingStateComponent withCompletedLoopChallenge(String challengeId) {
    Objects.requireNonNull(challengeId, "challengeId");
    Set<String> completed = new HashSet<>(completedLoopChallenges);
    completed.add(challengeId);
    return new ProgrammingStateComponent(phase, variableStage, golem, completed);
  }

  /** Advances to the next phase and remains unchanged after completion. */
  public ProgrammingStateComponent advance() {
    return phase.next().map(this::withPhase).orElse(this);
  }
}
