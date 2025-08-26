package contrib.entities;

import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public enum HeroClass {
  WIZARD(
      "Wizard",
      "character/wizard",
      Vector2.of(5, 5),
      5,
      1.3f,
      10,
      new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), 500),
      6),

  KNIGHT(
      "Knight", "character/knight", Vector2.of(7, 7), 7, 1.7f, 30, new Skill(entity -> {}, 0), 10);

  private final String name;
  private final IPath textures;
  private final Vector2 stepSpeed;
  private final float maxSpeed;
  private final float mass;
  private final int hp;
  private final Skill startSkill;
  private final int inventorySize;

  HeroClass(
      String name,
      String textures,
      Vector2 stepSpeed,
      float maxSpeed,
      float mass,
      int hp,
      Skill startSkill,
      int inventorySize) {
    this.name = name;
    this.textures = new SimpleIPath(textures);
    this.stepSpeed = stepSpeed;
    this.maxSpeed = maxSpeed;
    this.mass = mass;
    this.hp = hp;
    this.startSkill = startSkill;
    this.inventorySize = inventorySize;
  }

  public float maxSpeed() {
    return maxSpeed;
  }

  public float mass() {
    return mass;
  }

  public IPath texture() {
    return textures;
  }

  public int hp() {
    return hp;
  }

  public int inventorySize() {
    return inventorySize;
  }

  public Skill startSkill() {
    return startSkill;
  }

  public Vector2 stepSpeed() {
    return stepSpeed;
  }
}
