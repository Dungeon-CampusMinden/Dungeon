package entities;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public enum MonsterIdleSound {
  BASIC("sounds/monster2.wav"),
  BURP("sounds/monster3.wav"),
  LOW_PITCH("sounds/monster4.wav"),
  HIGH_PITCH("sounds/monster1.wav"),
  NONE("");

  private final IPath path;

  MonsterIdleSound(String path) {
    this.path = new SimpleIPath(path);
  }

  public IPath getPath() {
    return this.path;
  }
}
