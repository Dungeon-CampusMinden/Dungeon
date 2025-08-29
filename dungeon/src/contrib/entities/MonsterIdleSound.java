package contrib.entities;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Enum for the different sounds that a monster can make when idling.
 *
 * <p>Each sound is represented by a path to the sound file that can be played when the monster is
 * idling.
 */
public enum MonsterIdleSound {
  /** A basic idle sound. */
  BASIC("sounds/monster2.wav"),
  /** A burp-like idle sound. */
  BURP("sounds/monster3.wav"),
  /** A low-pitched idle sound. */
  LOWER_PITCH("sounds/monster4.wav"),
  /** A high-pitched idle sound. */
  HIGH_PITCH("sounds/monster1.wav"),
  /** No sound. */
  NONE("");

  private final IPath path;

  MonsterIdleSound(String path) {
    this.path = new SimpleIPath(path);
  }

  /**
   * Returns the monster's idle path.
   *
   * @return The path of the monster's while idling sound. If the monster has no sound, returns an
   *     empty path.
   */
  public IPath path() {
    return path;
  }
}
