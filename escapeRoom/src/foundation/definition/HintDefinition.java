package foundation.definition;

/**
 * One authored optional hint in a Foundation riddle.
 *
 * @param id stable hint identifier
 * @param title player-facing title
 * @param text player-facing hint text
 * @param severity one-based authored order within the riddle
 */
public record HintDefinition(String id, String title, String text, int severity) {
  /** Creates a hint definition. */
  public HintDefinition {
    id = DefinitionChecks.requireId(id, "hint id");
    title = DefinitionChecks.requireText(title, "hint title");
    text = DefinitionChecks.requireText(text, "hint text");
    if (severity < 1) {
      throw new IllegalArgumentException("hint severity must be positive");
    }
  }
}
