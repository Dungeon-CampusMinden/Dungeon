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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

/** A utility class for building monster entities in the game world. */
public final class MonsterFactory {

  private static final Random RANDOM = new Random();

  private static final IPath[] MONSTER_FILE_PATHS = {
    new SimpleIPath("character/monster/chort"),
    new SimpleIPath("character/monster/imp"),
    new SimpleIPath("character/monster/big_daemon"),
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

  private static final int MIN_MONSTER_HEALTH = 1;
  private static final int MAX_MONSTER_HEALTH = 6;
  private static final float MIN_MONSTER_SPEED = 3.0f;
  private static final float MAX_MONSTER_SPEED = 8.5f;
  private static final DamageType MONSTER_COLLIDE_DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final int MONSTER_COLLIDE_DAMAGE = 5;
  private static final int MONSTER_COLLIDE_COOL_DOWN = 2 * Game.frameRate();
  private static final int MAX_DISTANCE_FOR_DEATH_SOUND = 15;

  private static ItemGenerator randomItemGenerator = ItemGenerator.defaultItemGenerator();

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
    return buildMonster(
        "monster",
        pathToTexture,
        RANDOM.nextInt(MIN_MONSTER_HEALTH, MAX_MONSTER_HEALTH + 1),
        RANDOM.nextFloat(MIN_MONSTER_SPEED, MAX_MONSTER_SPEED),
        RANDOM.nextFloat(),
        randomMonsterDeathSound(),
        null,
        MONSTER_COLLIDE_DAMAGE,
        MONSTER_COLLIDE_COOL_DOWN,
        randomMonsterIdleSound());
  }

  /**
   * Sets the ItemGenerator used to generate random items for monsters upon death.
   *
   * @param randomItemGenerator The ItemGenerator to use for generating random items.
   * @see ItemGenerator
   */
  public static void randomItemGenerator(ItemGenerator randomItemGenerator) {
    MonsterFactory.randomItemGenerator = randomItemGenerator;
  }

  /**
   * Gets the ItemGenerator used to generate random items for monsters upon death.
   *
   * <p>The default ItemGenerator is {@link ItemGenerator#defaultItemGenerator()}.
   *
   * @return The current ItemGenerator used for generating random items.
   * @see ItemGenerator
   */
  public static ItemGenerator randomItemGenerator() {
    return randomItemGenerator;
  }

  private static Sound randomMonsterDeathSound() {
    List<String> deathSoundsPaths =
        Arrays.asList(
            "sounds/die_01.wav", "sounds/die_02.wav", "sounds/die_03.wav", "sounds/die_04.wav");

    return Gdx.audio.newSound(
        Gdx.files.internal(deathSoundsPaths.get(RANDOM.nextInt(deathSoundsPaths.size()))));
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
    List<String> idleSoundsPaths =
        Arrays.asList(
            "sounds/monster1.wav",
            "sounds/monster2.wav",
            "sounds/monster3.wav",
            "sounds/monster4.wav");

    return new SimpleIPath(idleSoundsPaths.get(RANDOM.nextInt(idleSoundsPaths.size())));
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

    InventoryComponent ic = new InventoryComponent(1);
    monster.add(ic);
    // rolls a dice for item chance (itemChance == 0  no item, 1.0 always)
    if (RANDOM.nextFloat() < itemChance) {
      Item item = randomItemGenerator.generateItemData();
      ic.add(item);
    }
    BiConsumer<Entity, Entity> onDeath =
        (e, who) -> {
          playDeathSoundIfNearby(deathSound, e);
          new DropItemsInteraction().accept(e, who);
          Game.remove(e);
        };
    monster.add(new HealthComponent(health, (e) -> onDeath.accept(e, null)));

    monster.add(new PositionComponent());
    if (ai == null) {
      ai = AIFactory.randomAI(monster);
    }
    monster.add(ai);
    monster.add(new DrawComponent(texture));
    monster.add(new VelocityComponent(speed));
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
