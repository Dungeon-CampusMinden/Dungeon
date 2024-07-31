package entities;

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
  LOW_PITCH("sounds/monster4.wav"),
  /** A high-pitched idle sound. */
  HIGH_PITCH("sounds/monster1.wav"),
  /** No sound. */
  NONE("");

  private final IPath path;

  MonsterIdleSound(String path) {
    this.path = new SimpleIPath(path);
  }

  /**
   * Returns the path to the sound of the monster's idle sound.
   *
   * @return The path to the sound of the monster's idle sound. If the monster has no sound, returns
   */
  public IPath getPath() {
    return path;
  }
}
