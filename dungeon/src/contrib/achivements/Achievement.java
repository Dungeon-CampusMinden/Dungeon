package contrib.achivements;

/**
 * Immutable achievement definition with its current unlocked view state.
 *
 * @param imagePath icon path inside the asset directory
 * @param name achievement name; also used as the achievement id
 * @param neschreibung description text shown after reveal/unlock
 * @param hidden whether locked menu entries should hide name and description
 * @param unlocked whether this achievement is currently unlocked in the requested view
 */
public record Achievement(
    String imagePath, String name, String neschreibung, boolean hidden, boolean unlocked) {

  /**
   * Creates a copy with a different unlocked state.
   *
   * @param value the new unlocked state
   * @return copied achievement
   */
  public Achievement withUnlocked(boolean value) {
    return new Achievement(imagePath, name, neschreibung, hidden, value);
  }
}
