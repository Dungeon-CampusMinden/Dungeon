package core.sound;

/** Enum that lists core sound effects and convenient playback helpers. */
public enum CoreSounds implements ISound {

  /** A sound effect. */
  INTERFACE_ITEM_HOVERED("kenney_ui_glass_005", 0.3f),
  /** A sound effect. */
  INTERFACE_BUTTON_CLICKED("kenney_ui_select_001", 0.4f),
  /** A sound effect. */
  INTERFACE_BUTTON_FORWARD("kenney_ui_confirmation_001", 1.0f),
  /** A sound effect. */
  INTERFACE_BUTTON_BACKWARD("kenney_ui_back_001", 1.0f),
  /** A sound effect. */
  INTERFACE_DIALOG_OPENED("kenney_ui_maximize_008", 1.0f),
  /** A sound effect. */
  INTERFACE_DIALOG_CLOSED("kenney_ui_minimize_008", 1.0f),
  /** A sound effect. */
  INTERFACE_TEXTFIELD_TYPED("kenney_ui_bong_001", 0.7f),

  /** A sound effect. */
  SETTINGS_TOGGLE_CLICK("kenney_ui_switch_001", 0.4f),
  /** A sound effect. */
  SETTINGS_SLIDER_STEP("kenney_ui_select_002", 0.3f),
  /** A sound effect. */
  SETTINGS_ENUM_VALUE_SELECTED("kenney_ui_drop_004", 0.3f),
  ;

  private final String soundName;
  private final float volume;

  /**
   * Create a Sounds enum element.
   *
   * @param soundName the identifier of the sound resource
   * @param volume the default playback volume for this sound
   */
  CoreSounds(String soundName, float volume) {
    this.soundName = soundName;
    this.volume = volume;
  }

  @Override
  public String soundName() {
    return this.soundName;
  }

  @Override
  public float volume() {
    return this.volume;
  }
}
