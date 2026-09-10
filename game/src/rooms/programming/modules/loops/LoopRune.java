package rooms.programming.modules.loops;

import java.util.Objects;

/**
 * A collectible program available at every checkpoint.
 *
 * @param id the canonical rune ID
 * @param program instructions used both for execution and source display
 */
public record LoopRune(String id, LoopProgram program) {

  /** Creates a rune with its stable content identifiers. */
  public LoopRune {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(program, "program");
    if (id.isBlank()) {
      throw new IllegalArgumentException("rune ID must not be blank");
    }
  }

  /**
   * @return source generated from this rune's executable program
   */
  public String code() {
    return program.code();
  }
}
