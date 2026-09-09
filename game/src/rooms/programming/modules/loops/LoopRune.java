package rooms.programming.modules.loops;

import java.util.Objects;

/**
 * A collectible typed program. Its source checkpoint does not restrict where it can execute.
 *
 * @param id the canonical rune ID
 * @param challengeId the station this rune belongs to
 * @param type the loop form to execute
 * @param title label shown on the archive card
 * @param program instructions used both for execution and source display
 */
public record LoopRune(
    String id, String challengeId, LoopType type, String title, LoopProgram program) {

  /** Creates a rune with its stable content identifiers. */
  public LoopRune {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(challengeId, "challengeId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(program, "program");
    if (id.isBlank() || challengeId.isBlank()) {
      throw new IllegalArgumentException("rune identifiers must not be blank");
    }
  }

  /**
   * @return source generated from this rune's executable program
   */
  public String code() {
    return program.code();
  }
}
