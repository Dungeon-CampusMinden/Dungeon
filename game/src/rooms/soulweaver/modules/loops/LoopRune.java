package rooms.soulweaver.modules.loops;

import java.util.Objects;
import java.util.function.UnaryOperator;

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
   * @param word renders a key below the {@code soulweaver} namespace, as text for this client or as
   *     a transport key on the server
   * @return source generated from this rune's executable program
   */
  public String code(UnaryOperator<String> word) {
    return program.code(word);
  }
}
