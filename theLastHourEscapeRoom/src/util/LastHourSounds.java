package util;

import core.sound.CoreSounds;
import core.sound.ISound;

/** Enum that lists available in-game sounds and convenient playback helpers. */
public enum LastHourSounds implements ISound {

  /** A sound effect. */
  COMPUTER_TAB_CLICKED(CoreSounds.INTERFACE_BUTTON_CLICKED.soundName(), 1.0f),

  /** A sound effect. */
  COMPUTER_LOGIN_FAILED("kenney_ui_error_002", 0.7f),

  /** A sound effect. */
  COMPUTER_LOGIN_SUCCESS("kenney_ui_confirmation_002", 1.0f),

  /** A sound effect. */
  COMPUTER_VIRUS_CAUGHT("kenney_ui_error_003", 1.0f),

  /** A sound effect. */
  COMPUTER_EMAIL_RECEIVED("", 1.0f),

  /** Sounds like a beep-boop-beeeeep. */
  ELECTRICITY_TURNED_ON("kenney_ui_maximize_001", 1.0f),
  ;

  private final String soundName;
  private final float volume;

  /**
   * Create a Sounds enum element.
   *
   * @param soundName the identifier of the sound resource
   * @param volume the default playback volume for this sound
   */
  LastHourSounds(String soundName, float volume) {
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
