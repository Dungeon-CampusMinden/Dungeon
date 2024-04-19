package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.item.ItemGenerator;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Random;
import java.util.function.BiConsumer;

/** A utility class for building monster entities in the game world. */
public final class MonsterFactory {

  private static final Random RANDOM = new Random();

  private static final IPath[] MONSTER_FILE_PATHS = {
    new SimpleIPath("character/monster/chort"),
    new SimpleIPath("character/monster/imp"),
    new SimpleIPath("character/monster/big_deamon"),
    new SimpleIPath("character/monster/big_zombie"),
    new SimpleIPath("character/monster/doc"),
    new SimpleIPath("character/monster/goblin"),
    new SimpleIPath("character/monster/ice_zombie"),
    new SimpleIPath("character/monster/ogre"),
    new SimpleIPath("character/monster/orc_shaman"),
    new SimpleIPath("character/monster/orc_warrior"),
    new SimpleIPath("character/monster/pumpkin_dude"),
    new SimpleIPath("character/monster/zombie")
  };

  private static final int MIN_MONSTER_HEALTH = 10;
  private static final int MAX_MONSTER_HEALTH = 20;
  private static final float MIN_MONSTER_SPEED = 5.0f;
  private static final float MAX_MONSTER_SPEED = 8.5f;
  private static final DamageType MONSTER_COLLIDE_DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final int MONSTER_COLLIDE_DAMAGE = 2;
  private static final int MONSTER_COLLIDE_COOL_DOWN = 2 * Game.frameRate();
  private static final int MAX_DISTANCE_FOR_DEATH_SOUND = 15;

  /**
   * Get an Entity that can be used as a monster.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} with
   * random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
   * Animation, {@link VelocityComponent}, {@link CollideComponent}, {@link IdleSoundComponent} and
   * a 10% chance for an {@link InventoryComponent}. If it has an Inventory it will use the {@link
   * DropItemsInteraction} on death.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity randomMonster() throws IOException {
    return randomMonster(MONSTER_FILE_PATHS[RANDOM.nextInt(0, MONSTER_FILE_PATHS.length)]);
  }

  /**
   * Get an Entity that can be used as a monster.
   *
   * <p>The Entity is not added to the game yet. *
   *
   * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} with
   * random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
   * Animation, {@link VelocityComponent}, {@link CollideComponent}, {@link IdleSoundComponent} and
   * a 10% chance for an {@link InventoryComponent}. If it has an Inventory it will use the {@link
   * DropItemsInteraction} on death.
   *
   * @param pathToTexture Textures to use for the monster.
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity randomMonster(IPath pathToTexture) throws IOException {
    int health = RANDOM.nextInt(MIN_MONSTER_HEALTH, MAX_MONSTER_HEALTH + 1);
    float speed = RANDOM.nextFloat(MIN_MONSTER_SPEED, MAX_MONSTER_SPEED);
    float itemChance = RANDOM.nextFloat();
    Sound deathSound = randomMonsterDeathSound();
    int collideDamage = MONSTER_COLLIDE_DAMAGE;
    int collideCooldown = MONSTER_COLLIDE_COOL_DOWN;

    return buildMonster(
        "monster",
        pathToTexture,
        health,
        speed,
        itemChance,
        deathSound,
        null,
        collideDamage,
        collideCooldown,
        randomMonsterIdleSound());
  }

  private static Sound randomMonsterDeathSound() {
    if (Gdx.files == null)
      return null; // This is a workaround for the Gdx.files being null in tests

    return switch (RANDOM.nextInt(4)) {
      case 0 -> Gdx.audio.newSound(Gdx.files.internal("sounds/die_01.wav"));
      case 1 -> Gdx.audio.newSound(Gdx.files.internal("sounds/die_02.wav"));
      case 2 -> Gdx.audio.newSound(Gdx.files.internal("sounds/die_03.wav"));
      default -> Gdx.audio.newSound(Gdx.files.internal("sounds/die_04.wav"));
    };
  }

  private static void playMonsterDieSound(Sound sound) {
    if (sound == null) {
      return;
    }
    long soundID = sound.play();
    sound.setLooping(soundID, false);
    sound.setVolume(soundID, 0.35f);
  }

  private static IPath randomMonsterIdleSound() {
    switch (RANDOM.nextInt(4)) {
      case 0 -> {
        return new SimpleIPath("sounds/monster1.wav");
      }
      case 1 -> {
        return new SimpleIPath("sounds/monster2.wav");
      }
      case 2 -> {
        return new SimpleIPath("sounds/monster3.wav");
      }
      default -> {
        return new SimpleIPath("sounds/monster4.wav");
      }
    }
  }

  /**
   * Builds a monster entity with the given parameters.
   *
   * @param name The name of the monster.
   * @param texture The path to the texture to use for the monster.
   * @param health The health of the monster.
   * @param speed The speed of the monster.
   * @param itemChance The chance that the monster will drop an item upon death. If 0, no item will
   *     be dropped. If 1, an item will always be dropped.
   * @param deathSound The sound to play when the monster dies. If null, no sound will be played.
   * @param ai The AI component of the monster. If null, a random AI will be used.
   * @param collideDamage The damage the monster inflicts upon collision.
   * @param collideCooldown The cooldown time between monster's collision damage.
   * @param idleSoundPath The sound component for the monster's idle sound. If empty, no sound will
   *     be played.
   * @return A new Entity representing the monster.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity buildMonster(
      String name,
      IPath texture,
      int health,
      float speed,
      float itemChance,
      Sound deathSound,
      AIComponent ai,
      int collideDamage,
      int collideCooldown,
      IPath idleSoundPath)
      throws IOException {
    Entity monster = new Entity(name);
    // rolls a dice for item chance (itemChance == 0 means no item, 1.0 means always item)
    BiConsumer<Entity, Entity> onDeath;
    if (RANDOM.nextFloat() < itemChance) {
      Item item = ItemGenerator.generateItemData();
      InventoryComponent ic = new InventoryComponent(1);
      monster.add(ic);
      ic.add(item);
      onDeath =
          (e, who) -> {
            playDeathSoundIfNearby(deathSound, e);
            new DropItemsInteraction().accept(e, who);
          };
    } else {
      onDeath =
          (e, who) -> {
            playDeathSoundIfNearby(deathSound, e);
          };
    }
    monster.add(new HealthComponent(health, (e) -> onDeath.accept(e, null)));
    monster.add(new PositionComponent());
    if (ai == null) {
      ai = AIFactory.randomAI(monster);
    }
    monster.add(ai);
    monster.add(new DrawComponent(texture));
    monster.add(new VelocityComponent(speed, speed));
    monster.add(new CollideComponent());
    if (collideDamage > 0) {
      monster.add(new SpikyComponent(collideDamage, MONSTER_COLLIDE_DAMAGE_TYPE, collideCooldown));
    }
    if (!idleSoundPath.pathString().isEmpty()) monster.add(new IdleSoundComponent(idleSoundPath));
    return monster;
  }

  private static void playDeathSoundIfNearby(Sound deathSound, Entity e) {
    if (Game.hero().isEmpty()) return;
    Entity hero = Game.hero().get();
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    PositionComponent monsterPc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));
    if (pc.position().distance(monsterPc.position()) < MAX_DISTANCE_FOR_DEATH_SOUND) {
      playMonsterDieSound(deathSound);
    }
  }
}
