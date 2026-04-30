package contrib.hud.dialogs;

import contrib.hud.elements.RichLabel;
import core.network.messages.c2s.DialogResponseMessage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A single selectable option in a {@link MultipleChoiceDialog}.
 *
 * <p>Each option has a display label (which may contain {@link RichLabel} markup) and a plain
 * {@link String} value that is sent back to the server when the option is selected. Use enum {@link
 * Enum#name()} or another stable string discriminator for the value, and parse it at the call site
 * (e.g. {@code MyEnum.valueOf(value)}).
 *
 * <p>Keeping the value as a String avoids forcing every project to register a {@code
 * DialogValueCodec} for every custom type that could be carried by an MCD response — strings have
 * built-in wire support via {@link DialogResponseMessage.StringValue}.
 *
 * @param label The text (with optional rich markup) displayed for this option.
 * @param value The string value returned when this option is selected.
 */
public record ChoiceOption(String label, String value) implements Serializable {

  /**
   * Creates a choice option with a label and an associated string value.
   *
   * @param label The text displayed for this option (may contain RichLabel markup).
   * @param value The string value returned on selection.
   * @return A new ChoiceOption.
   */
  public static ChoiceOption of(String label, String value) {
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
}
