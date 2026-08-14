package escaperoom.foundation.definition;

import java.util.Objects;

/**
 * Host-evaluated numeric keypad input.
 *
 * @param id stable input identifier
 * @param surfaceId authored keypad surface identifier
 * @param answer exact host-evaluated answer
 * @param showDigitCount whether clients may see the answer length
 */
public record NumericInputDefinition(
    String id, String surfaceId, String answer, boolean showDigitCount) implements InputDefinition {
  /** Creates an immutable numeric input. */
  public NumericInputDefinition {
    id = DefinitionChecks.requireId(id, "numeric input id");
    surfaceId = DefinitionChecks.requireId(surfaceId, "numeric input surface id");
    Objects.requireNonNull(answer, "numeric answer");
    if (!answer.matches("[0-9]{1,8}")) {
      throw new IllegalArgumentException("numeric answer must contain one to eight digits");
    }
  }
}
