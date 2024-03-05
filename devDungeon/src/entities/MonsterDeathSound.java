package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public enum MonsterDeathSound {
  BASIC("sounds/die_01.wav"),
  LOW_PITCH("sounds/die_02.wav"),
  LOWER_PITCH("sounds/die_03.wav"),
  HIGH_PITCH("sounds/die_04.wav");

  private final Sound sound;

  MonsterDeathSound(String path) {
    this.sound = Gdx.audio.newSound(Gdx.files.internal(path));
  }

  public Sound getSound() {
    return sound;
  }
}
