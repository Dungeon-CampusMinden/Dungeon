package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public enum MonsterDeathSound {
  BASIC("sounds/die_01.wav"),
  LOW_PITCH("sounds/die_02.wav"),
  LOWER_PITCH("sounds/die_03.wav"),
  HIGH_PITCH("sounds/die_04.wav"),
  NONE("");

  private final Sound sound;

  MonsterDeathSound(String path) {
    if (path.isEmpty()) {
      this.sound = null;
    } else {
      this.sound = Gdx.audio.newSound(Gdx.files.internal(path));
    }
  }

  /**
   * Returns the sound of the monster's death.
   *
   * @return The sound of the monster's death. If the monster has no sound, returns null.
   */
  public Sound getSound() {
    return this.sound;
  }
}
