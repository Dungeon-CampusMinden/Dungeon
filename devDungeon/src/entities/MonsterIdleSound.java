package entities;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public enum MonsterIdleSound {
  BASIC("sounds/monster2.wav"),
  CURSED("sounds/monster3.wav"),
  LOW_PITCH("sounds/monster4.wav"),
  HIGH_PITCH("sounds/monster1.wav");

  private final IPath sound;

  MonsterIdleSound(String path) {
    this.sound = new SimpleIPath(path);
  }

  public IPath getSound() {
    return sound;
  }
}
