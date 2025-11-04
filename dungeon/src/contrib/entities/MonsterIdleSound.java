package contrib.entities;

/**
 * Enum for the different sounds that a monster can make when idling.
 *
 * <p>Each sound is represented by a path to the sound file that can be played when the monster is
 * idling.
 */
public enum MonsterIdleSound {
  /** A basic idle sound. */
  BASIC("monster2"),
  /** A burp-like idle sound. */
  BURP("monster3"),
  /** A low-pitched idle sound. */
  LOWER_PITCH("monster4"),
  /** A high-pitched idle sound. */
  HIGH_PITCH("monster1"),
  /** No sound. */
  NONE("");

  private final String soundId;

  MonsterIdleSound(String soundId) {
    this.soundId = soundId;
  }

  /**
   * Returns the monster's idle sound unique identifier.
   *
   * @return The sound effect unique identifier.
   */
  public String soundId() {
    return soundId;
  }
}
