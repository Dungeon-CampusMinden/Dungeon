package hint;

/**
 * Represents a text-based hint that can be used by the {@link HintSystem} to provide assistance to
 * the player during a game or puzzle.
 *
 * @param titel the title or identifier of the hint, for example, the name of the riddle the hint is
 *     related to
 * @param text the hint text itself, describing the clue or guidance for the player
 */
public record Hint(String titel, String text) {}
