package contrib.hud.dialogs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A single selectable option in a {@link MultipleChoiceDialog}.
 *
 * <p>Each option has a display label (which may contain {@link contrib.hud.elements.RichLabel}
 * markup) and a serializable value that is returned when the option is selected.
 *
 * @param label The text (with optional rich markup) displayed for this option.
 * @param value The serializable value returned when this option is selected.
 */
public record ChoiceOption(String label, Serializable value) implements Serializable {

  /**
   * Creates a choice option with a label and an associated value.
   *
   * @param label The text displayed for this option (may contain RichLabel markup).
   * @param value The serializable value returned on selection.
   * @return A new ChoiceOption.
   */
  public static ChoiceOption of(String label, Serializable value) {
    return new ChoiceOption(label, value);
  }

  /**
   * Creates a choice option where the label string is also used as the value.
   *
   * @param label The text displayed for this option (may contain RichLabel markup).
   * @return A new ChoiceOption whose value equals the label.
   */
  public static ChoiceOption of(String label) {
    return new ChoiceOption(label, label);
  }

  /**
   * Creates a choice option from a serializable object. The label is derived from the object's
   * {@link Object#toString()} method; the value is the object itself.
   *
   * @param value The serializable object to use as both label source and value.
   * @return A new ChoiceOption.
   */
  public static ChoiceOption ofValue(Serializable value) {
    return new ChoiceOption(value.toString(), value);
  }

  /**
   * Creates a list of choice options from an array of strings. Each string is used as both the
   * label and the value.
   *
   * @param labels The strings to create options from.
   * @return An unmodifiable list of ChoiceOptions.
   */
  public static List<ChoiceOption> ofList(String... labels) {
    return Arrays.stream(labels).map(ChoiceOption::of).toList();
  }

  /**
   * Creates a list of choice options from a collection of strings. Each string is used as both the
   * label and the value.
   *
   * @param labels The strings to create options from.
   * @return An unmodifiable list of ChoiceOptions.
   */
  public static List<ChoiceOption> ofList(Collection<String> labels) {
    return labels.stream().map(ChoiceOption::of).toList();
  }

  /**
   * Creates a list of choice options from an array of serializable objects. Each object's {@link
   * Object#toString()} is used as the label; the object itself is the value.
   *
   * @param values The serializable objects to create options from.
   * @return An unmodifiable list of ChoiceOptions.
   */
  public static List<ChoiceOption> ofList(Serializable... values) {
    return Arrays.stream(values).map(ChoiceOption::ofValue).toList();
  }

  /**
   * Creates a list of choice options from a collection of serializable objects. Each object's
   * {@link Object#toString()} is used as the label; the object itself is the value.
   *
   * @param values The serializable objects to create options from.
   * @return An unmodifiable list of ChoiceOptions.
   */
  public static List<ChoiceOption> ofValueList(Collection<? extends Serializable> values) {
    return values.stream().map(ChoiceOption::ofValue).toList();
  }
}
