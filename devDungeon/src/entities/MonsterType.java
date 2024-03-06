package entities;

import com.badlogic.gdx.audio.Sound;
import contrib.components.AIComponent;
import contrib.entities.MonsterFactory;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import core.Entity;
import core.Game;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public enum MonsterType {
  CHORT(
      "Chort",
      "character/monster/chort",
      20,
      3.0f,
      0.0f,
      MonsterDeathSound.LOWER_PITCH,
      new CollideAI(0.5f),
      new RadiusWalk(5f, 2),
      new RangeTransition(5),
      4,
      2 * Game.frameRate(),
      MonsterIdleSound.LOW_PITCH);

  private final String name;
  private final IPath texture;
  private final Sound deathSound;
  private final AIComponent ai;
  private final int collideDamage;
  private final int collideCooldown;
  private final IPath idleSoundPath;
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
      Consumer<Entity> fightAI,
      Consumer<Entity> idleAI,
      Function<Entity, Boolean> transitionAI,
      int collideDamage,
      int collideCooldown,
      MonsterIdleSound idleSound) {
    this.name = name;
    this.texture = new SimpleIPath(texture);
    this.health = health;
    this.speed = speed;
    this.itemChance = canHaveItems;
    this.deathSound = deathSound.getSound();
    this.ai = new AIComponent(fightAI, idleAI, transitionAI);
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.idleSoundPath = idleSound.getPath();
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
        idleSoundPath);
  }
}
