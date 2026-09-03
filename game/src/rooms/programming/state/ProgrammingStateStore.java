package rooms.programming.state;

import engine.Entity;
import engine.Game;
import engine.game.ECSManagement;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/** Access to the single Programming 1 room-state entity. */
public final class ProgrammingStateStore {

  private static final String STATE_ENTITY_NAME = "programming-state";

  private ProgrammingStateStore() {}

  /** Creates the initial state entity if the current level has none. */
  public static ProgrammingStateComponent initialize() {
    requireAuthority();
    Optional<ProgrammingStateComponent> current = current();
    if (current.isPresent()) {
      return current.orElseThrow();
    }

    ProgrammingStateComponent initial = ProgrammingStateComponent.initial();
    Entity stateEntity = new Entity(STATE_ENTITY_NAME);
    stateEntity.add(initial);
    Game.add(stateEntity);
    return initial;
  }

  /** Returns the current synchronized room state. */
  public static Optional<ProgrammingStateComponent> current() {
    return stateEntity().flatMap(entity -> entity.fetch(ProgrammingStateComponent.class));
  }

  /** Resets the current room state, creating its entity when necessary. */
  public static ProgrammingStateComponent reset() {
    return replace(ignored -> ProgrammingStateComponent.initial());
  }

  /** Unlocks essence assignment after all vessels were assigned correctly. */
  public static ProgrammingStateComponent completeVessels() {
    return replace(
        current -> {
          requireVariableStage(current, VariablePuzzleStage.VESSELS);
          return current.withVariableStage(VariablePuzzleStage.ESSENCES);
        });
  }

  /** Stores the solved golem values and unlocks the data-type reveal. */
  public static ProgrammingStateComponent completeEssences(GolemState golem) {
    return replace(
        current -> {
          requireVariableStage(current, VariablePuzzleStage.ESSENCES);
          return current.withGolem(golem).withVariableStage(VariablePuzzleStage.REVEAL);
        });
  }

  /** Marks the reveal as complete and starts the loop act. */
  public static ProgrammingStateComponent activateGolem() {
    return replace(
        current -> {
          requireVariableStage(current, VariablePuzzleStage.REVEAL);
          return current
              .withVariableStage(VariablePuzzleStage.COMPLETE)
              .withPhase(ProgrammingPhase.LOOPS);
        });
  }

  /** Records one correctly solved loop situation. */
  public static ProgrammingStateComponent completeLoopChallenge(String challengeId) {
    return replace(
        current -> {
          if (current.phase() != ProgrammingPhase.LOOPS) {
            throw new IllegalStateException("loop puzzle is not active");
          }
          return current.withCompletedLoopChallenge(challengeId);
        });
  }

  /** Advances the room to its next phase. */
  public static ProgrammingStateComponent advance() {
    return replace(ProgrammingStateComponent::advance);
  }

  /** Moves directly to a phase for development tools. */
  public static ProgrammingStateComponent debugPhase(ProgrammingPhase phase) {
    return replace(current -> current.withPhase(phase));
  }

  private static ProgrammingStateComponent replace(
      UnaryOperator<ProgrammingStateComponent> operation) {
    requireAuthority();
    Entity entity =
        stateEntity()
            .orElseGet(
                () -> {
                  initialize();
                  return stateEntity().orElseThrow();
                });
    ProgrammingStateComponent updated =
        operation.apply(entity.fetch(ProgrammingStateComponent.class).orElseThrow());
    entity.remove(ProgrammingStateComponent.class);
    entity.add(updated);
    return updated;
  }

  private static Optional<Entity> stateEntity() {
    return ECSManagement.entities(Set.of(ProgrammingStateComponent.class)).findFirst();
  }

  private static void requireVariableStage(
      ProgrammingStateComponent current, VariablePuzzleStage expected) {
    if (current.phase() != ProgrammingPhase.VARIABLES || current.variableStage() != expected) {
      throw new IllegalStateException("variable puzzle stage " + expected + " is not active");
    }
  }

  private static void requireAuthority() {
    if (Game.isMultiplayerClient()) {
      throw new IllegalStateException("only the authoritative room may change Programming state");
    }
  }
}
