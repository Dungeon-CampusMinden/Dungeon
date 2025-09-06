package contrib.entities;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * An enumeration of the different sounds that a monster can make when it dies.
 *
 * <p>Each sound is represented by a Sound object that can be played when the monster dies.
 */
public enum MonsterDeathSound {
  /** A basic death sound. */
  BASIC("sounds/die_01.wav"),
  /** A more low-pitched death sound. */
  LOW_PITCH("sounds/die_02.wav"),
  /** A even more low-pitched death sound. */
  LOWER_PITCH("sounds/die_03.wav"),
  /** A high-pitched death sound. */
  HIGH_PITCH("sounds/die_04.wav"),
  /** No sound. */
  NONE("");

  private final IPath path;

  MonsterDeathSound(String path) {
    this.path = new SimpleIPath(path);
  }

  /**
   * Returns the sound of the monster's death.
   *
   * @return The sound of the monster's death. If the monster has no sound, returns null.
   */
  public IPath path() {
    return path;
  }
}
