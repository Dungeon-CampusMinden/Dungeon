package entities;

import com.badlogic.gdx.audio.Sound;
import contrib.components.AIComponent;
import contrib.components.IdleSoundComponent;
import contrib.entities.MonsterFactory;
import core.Entity;
import core.Game;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

public enum MonsterType {
  CHORT(
      "Chort",
      "character/monster/chort",
      20,
      3.0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      null,
      2,
      2 * Game.frameRate(),
      MonsterIdleSound.HIGH_PITCH);

  private final String name;
  private final IPath texture;
  private final Sound deathSound;
  private final AIComponent ai;
  private final int collideDamage;
  private final int collideCooldown;
  private final IdleSoundComponent idleSoundComponent;
  private final int health;
  private final float speed;
  private final float itemChance; // 0.0f means no items, 1.0f means always items

  MonsterType(
      String name,
      String texture,
      int health,
      float speed,
      float canHaveItems,
      MonsterDeathSound deathSound,
      AIComponent ai,
      int collideDamage,
      int collideCooldown,
      MonsterIdleSound idleSound) {
    this.name = name;
    this.texture = new SimpleIPath(texture);
    this.health = health;
    this.speed = speed;
    this.itemChance = canHaveItems;
    this.deathSound = deathSound.getSound();
    this.ai = ai;
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.idleSoundComponent = new IdleSoundComponent(idleSound.getSound());
  }

  public Entity buildMonster() throws IOException {
    return MonsterFactory.buildMonster(
        name,
        texture,
        health,
        speed,
        itemChance,
        deathSound,
        ai,
        collideDamage,
        collideCooldown,
        idleSoundComponent);
  }
}
