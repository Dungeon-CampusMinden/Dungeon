package contrib.hud.dialogs;

import java.io.Serializable;

/**
 * A single selectable option in a {@link MultipleChoiceDialog}.
 *
 * <p>Each option has a display label and an optional icon path. The icon path can be either a skin
 * drawable name or a file path to a texture. The dialog builder will attempt to resolve it as a
 * skin drawable first, and fall back to loading it as a texture file.
 *
 * @param label The text displayed for this option.
 * @param iconPath The skin drawable name or texture file path for the icon, or {@code null} for no
 *     icon.
 */
public record ChoiceOption(String label, String iconPath) implements Serializable {

  /**
   * Creates a choice option with only a text label and no icon.
   *
   * @param label The text displayed for this option.
   * @return A new ChoiceOption with no icon.
   */
  public static ChoiceOption of(String label) {
    return new ChoiceOption(label, null);
  }

  /**
   * Creates a choice option with a text label and an icon.
   *
   * @param label The text displayed for this option.
   * @param iconPath The skin drawable name or texture file path for the icon.
   * @return A new ChoiceOption.
   */
  public static ChoiceOption of(String label, String iconPath) {
    return new ChoiceOption(label, iconPath);
  }
}
