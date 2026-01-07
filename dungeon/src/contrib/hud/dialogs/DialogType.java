package contrib.hud.dialogs;

/**
 * Interface representing a dialog type identifier used by {@link DialogFactory}.
 *
 * <p>Dialog types are used to register and look up dialog creator functions in the factory. Custom
 * dialog types can be created by implementing this interface.
 *
 * @see DialogFactory#register(DialogType, java.util.function.Function)
 * @see DialogContext
 */
public interface DialogType {

  /**
   * Returns the unique string identifier for this dialog type.
   *
   * @return The type identifier string
   */
  String type();

  /**
   * Enumeration of built-in dialog types provided by the framework.
   *
   * <p>These types are automatically registered with {@link DialogFactory} at startup.
   */
  enum DefaultTypes implements DialogType {
    /** Simple dialog with a message and single OK button. */
    OK("OK"),

    /** Confirmation dialog with Yes and No buttons. */
    YES_NO("YES_NO"),

    /** Customizable text dialog with configurable buttons. */
    TEXT("TEXT"),

    /** Dialog for displaying images. */
    IMAGE("IMAGE"),

    /** Dialog with a free-form text input field. */
    FREE_INPUT("FREE_INPUT"),

    /** Single inventory display dialog. */
    INVENTORY("INVENTORY"),

    /** Dual inventory display for item transfers. */
    DUAL_INVENTORY("DUAL_INVENTORY"),

    /** Crafting interface dialog. */
    CRAFTING_GUI("CRAFTING_GUI"),

    /** Numeric keypad input dialog. */
    KEYPAD("KEYPAD"),

    /** Progress bar display dialog. */
    PROGRESS_BAR("PROGRESS_BAR"),

    /** Pause menu dialog. */
    PAUSE_MENU("PAUSE_MENU");

    private final String type;

    DefaultTypes(String type) {
      this.type = type;
    }

    @Override
    public String type() {
      return type;
    }
  }
}
