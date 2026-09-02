package rooms.programming.state;

import engine.Component;
import java.util.Objects;
import java.util.Optional;

/** Shared room progress synchronized from the authoritative server. */
public record ProgrammingStateComponent(ProgrammingPhase phase, Optional<GolemState> golem)
    implements Component {

  /** Creates an immutable room state. */
  public ProgrammingStateComponent {
    phase = Objects.requireNonNull(phase, "phase");
    golem = Objects.requireNonNull(golem, "golem");
  }

  /** Creates the initial state of a new room. */
  public static ProgrammingStateComponent initial() {
    return new ProgrammingStateComponent(ProgrammingPhase.VARIABLES, Optional.empty());
  }

  /** Returns a copy in another phase. */
  public ProgrammingStateComponent withPhase(ProgrammingPhase newPhase) {
    return new ProgrammingStateComponent(newPhase, golem);
  }

  /** Returns a copy with the solved golem values. */
  public ProgrammingStateComponent withGolem(GolemState newGolem) {
    return new ProgrammingStateComponent(phase, Optional.of(newGolem));
  }

  /** Advances to the next phase and remains unchanged after completion. */
  public ProgrammingStateComponent advance() {
    return phase.next().map(this::withPhase).orElse(this);
  }
}
