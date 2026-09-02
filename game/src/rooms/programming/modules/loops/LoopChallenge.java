package rooms.programming.modules.loops;

import java.util.Objects;

/** One stable labyrinth situation and its expected loop type. */
public record LoopChallenge(String id, LoopType expectedType) {

  /** Creates a validated loop challenge. */
  public LoopChallenge {
    id = Objects.requireNonNull(id, "id").trim();
    expectedType = Objects.requireNonNull(expectedType, "expectedType");
    if (id.isEmpty()) {
      throw new IllegalArgumentException("id must not be blank");
    }
  }

  /** Reports whether an answer selects the expected loop type. */
  public boolean accepts(LoopType answer) {
    return expectedType == answer;
  }
}
