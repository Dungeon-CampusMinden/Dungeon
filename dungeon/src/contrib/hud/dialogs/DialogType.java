package contrib.hud.dialogs;

public interface DialogType {
  String type();

  enum DefaultTypes implements DialogType {
    OK("OK"),
    YES_NO("YES_NO"),
    TEXT("TEXT"),
    IMAGE("IMAGE"),
    FREE_INPUT("FREE_INPUT"),
    QUIZ("QUIZ"),
    INVENTORY("INVENTORY"),
    DUAL_INVENTORY("DUAL_INVENTORY"),
    CRAFTING_GUI("CRAFTING_GUI"),
    KEYPAD("KEYPAD"),
    PROGRESS_BAR("PROGRESS_BAR");

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
