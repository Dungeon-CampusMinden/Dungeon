package escaperoom.foundation.definition;

import java.util.Objects;

/**
 * One authored optional hint in a Foundation riddle.
 *
 * @param id stable hint identifier
 * @param title player-facing title
 * @param text player-facing hint text
 * @param severity disclosure category announced before release
 */
public record HintDefinition(String id, String title, String text, HintSeverity severity) {
  /** Creates a hint definition. */
  public HintDefinition {
    id = DefinitionChecks.requireId(id, "hint id");
    title = DefinitionChecks.requireText(title, "hint title");
    text = DefinitionChecks.requireText(text, "hint text");
    severity = Objects.requireNonNull(severity, "hint severity");
  }
}
