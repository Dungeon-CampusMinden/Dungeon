package contrib.entities;

/**
 * An enumeration of the different sounds that a monster can make when it dies.
 *
 * <p>Each sound is represented by a Sound object that can be played when the monster dies.
 */
public enum MonsterDeathSound {
  /** A basic death sound. */
  BASIC("die_01"),
  /** A more low-pitched death sound. */
  LOW_PITCH("die_02"),
  /** A even more low-pitched death sound. */
  LOWER_PITCH("die_03"),
  /** A high-pitched death sound. */
  HIGH_PITCH("die_04"),
  /** No sound. */
  NONE("");

  private final String soundId;

  MonsterDeathSound(String soundId) {
    this.soundId = soundId;
  }

  /**
   * Returns the monster's death sound unique identifier.
   *
   * @return The sound effect unique identifier.
   */
  public String soundId() {
    return soundId;
  }
}
