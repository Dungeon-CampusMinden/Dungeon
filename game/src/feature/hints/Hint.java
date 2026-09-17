package feature.hints;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a text-based hint that can be used by the {@link HintSystem} to provide assistance to
 * the player during a game or puzzle.
 *
 * @param title the title or identifier of the hint, for example, the name of the riddle the hint is
 *     related to
 * @param text the hint text itself, describing the clue or guidance for the player
 * @param solution whether this hint reveals the complete solution
 */
public record Hint(String title, String text, boolean solution) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** Creates a normal hint that does not reveal the complete solution. */
  public Hint(String title, String text) {
    this(title, text, false);
  }
}
