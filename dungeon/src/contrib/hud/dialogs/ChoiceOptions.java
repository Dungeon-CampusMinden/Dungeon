package contrib.hud.dialogs;

import java.util.List;
import java.util.Objects;

/**
 * Immutable wrapper for the selectable options of a {@link MultipleChoiceDialog}.
 *
 * @param values The selectable choice options.
 */
public record ChoiceOptions(List<ChoiceOption> values) {

  /** Creates a wrapper with a defensive copy of the provided options. */
  public ChoiceOptions {
    values = List.copyOf(Objects.requireNonNull(values, "values"));
  }
}
