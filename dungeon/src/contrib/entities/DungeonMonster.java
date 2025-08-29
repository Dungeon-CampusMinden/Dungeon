package contrib.entities;

import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Defines and build Monster-Entitys for the Dungeon. */
public enum DungeonMonster {
  /**
   * A small, mischievous demon. Fast and light, deals low damage, and can enter open pits. Has a
   * high-pitched death sound and random fight/idle AI.
   */
  IMP(
      "Imp",
      5.0f,
      1.0f,
      e -> {},
      true,
      "character/monster/imp",
      3,
      e -> {},
      true,
      MonsterDeathSound.HIGH_PITCH,
      Set.of(),
      0.2f,
      Set.of(),
      MonsterIdleSound.HIGH_PITCH,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      5,
      2 * Game.frameRate(),
      DamageType.PHYSICAL),
  /**
   * A slow-moving undead. Relatively low health but moderate mass. Cannot enter open pits. Emits a
   * low-pitched death sound and has random fight/idle AI.
   */
  ZOMBIE(
      "Zombie",
      3.2f,
      1.3f,
      e -> {},
      false,
      "character/monster/big_zombie",
      6,
      e -> {},
      true,
      MonsterDeathSound.LOW_PITCH,
      Set.of(),
      0.15f,
      Set.of(),
      MonsterIdleSound.BURP,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      4,
      2 * Game.frameRate(),
      DamageType.PHYSICAL),
  /**
   * A large, powerful monster. Slow but deals high collision damage. Cannot enter open pits. Emits
   * a lower-pitched death sound and uses random fight/idle AI.
   */
  OGRE(
      "Ogre",
      2.5f,
      2.5f,
      e -> {},
      false,
      "character/monster/ogre",
      9,
      e -> {},
      true,
      MonsterDeathSound.LOWER_PITCH,
      Set.of(),
      0.25f,
      Set.of(),
      MonsterIdleSound.LOWER_PITCH,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      8,
      2 * Game.frameRate(),
      DamageType.PHYSICAL),
  /**
   * A small, agile goblin. Fast and light, deals low collision damage. Cannot enter open pits. Uses
   * basic death and idle sounds with random fight/idle AI.
   */
  GOBLIN(
      "Goblin",
      4.2f,
      1.0f,
      e -> {},
      false,
      "character/monster/goblin",
      4,
      e -> {},
      true,
      MonsterDeathSound.BASIC,
      Set.of(),
      0.18f,
      Set.of(),
      MonsterIdleSound.BASIC,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      3,
      2 * Game.frameRate(),
      DamageType.PHYSICAL),
  /**
   * An undead ice-themed monster. Moderate speed and health. Cannot enter open pits. Emits a
   * lower-pitched death sound and uses random fight/idle AI.
   */
  ICE_ZOMBIE(
      "Ice Zombie",
      2.8f,
      1.3f,
      e -> {},
      false,
      "character/monster/ice_zombie",
      6,
      e -> {},
      true,
      MonsterDeathSound.LOW_PITCH,
      Set.of(),
      0.20f,
      Set.of(),
      MonsterIdleSound.LOWER_PITCH,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      4,
      2 * Game.frameRate(),
      DamageType.PHYSICAL),

  /**
   * A magical orc shaman. Moderate speed and health, cannot enter open pits. Uses basic death
   * sound, lower-pitched idle sound, and random fight/idle AI.
   */
  ORC_SHAMAN(
      "Orc Shaman",
      3.4f,
      1.2f,
      e -> {},
      false,
      "character/monster/orc_shaman",
      5,
      e -> {},
      true,
      MonsterDeathSound.BASIC,
      Set.of(),
      0.22f,
      Set.of(),
      MonsterIdleSound.LOWER_PITCH,
      AIFactory::randomFightAI,
      AIFactory::randomIdleAI,
      () -> (self) -> AIFactory.randomTransition(self).apply(self),
      4,
      2 * Game.frameRate(),
      DamageType.PHYSICAL);

  /** Random instance for dungeon monster. */
  public static Random RANDOM = new Random();

  private static final int MAX_DISTANCE_FOR_DEATH_SOUND = 15;

  private final String name;
  private final float speed;
  private final float mass;
  private final Consumer<Entity> onWallHit;
  private final boolean canEnterOpenPits;
  // Draw
  private final IPath texture;
  // Health
  private final int health;
  private final Consumer<Entity> onDeath;
  private final boolean removeOnDeath;
  private final MonsterDeathSound deathSound;
  // Drops
  final Set<Item> drops;
  final float dropChance;
  final Set<Item> guaranteedDrops; // NEW
  // Idle sound
  private final MonsterIdleSound idleSound;
  // AI
  private final Supplier<Consumer<Entity>> fightAISupplier;
  private final Supplier<Consumer<Entity>> idleAISupplier;
  private final Supplier<Function<Entity, Boolean>> transitionAISupplier;
  // Damage
  private final int collideDamage;
  private final int collideCooldown;
  private final DamageType damageType;

  /**
   * Creates a new monster archetype with the given configuration.
   *
   * @param name Human-readable monster name.
   * @param speed Movement speed of the monster (affects velocity).
   * @param mass Mass of the monster (affects velocity).
   * @param onWallHit Callback executed when the monster collides with a wall.
   * @param canEnterOpenPits Whether the monster can move into open pits.
   * @param texture Path to the texture used for rendering this monster.
   * @param health Initial health points of the monster.
   * @param onDeath Callback executed when the monster dies.
   * @param removeOnDeath If true, the monster entity will be removed from the game upon death.
   * @param deathSound Sound to play when the monster dies.
   * @param drops Set of items that can randomly drop when the monster dies.
   * @param dropChance Probability (0–1) of dropping an item from {@code drops}.
   * @param guaranteedDrops Set of item that are always dropped when the monster dies.
   * @param idleSound Sound to play at intervals when the monster is idle.
   * @param fightAISupplier Supplier that provides the monster's fight behavior (AI).
   * @param idleAISupplier Supplier that provides the monster's idle behavior (AI).
   * @param transitionAISupplier Supplier that provides AI transitions between fight and idle
   *     states.
   * @param collideDamage Amount of damage dealt when the monster collides with another entity.
   * @param collideCooldown Cooldown time (in frames) between consecutive collision damage events.
   * @param damageType The type of damage the monster deals on collision (e.g., physical, magical).
   */
  DungeonMonster(
      String name,
      float speed,
      float mass,
      Consumer<Entity> onWallHit,
      boolean canEnterOpenPits,
      String texture,
      int health,
      Consumer<Entity> onDeath,
      boolean removeOnDeath,
      MonsterDeathSound deathSound,
      Set<Item> drops,
      float dropChance,
      Set<Item> guaranteedDrops,
      MonsterIdleSound idleSound,
      Supplier<Consumer<Entity>> fightAISupplier,
      Supplier<Consumer<Entity>> idleAISupplier,
      Supplier<Function<Entity, Boolean>> transitionAISupplier,
      int collideDamage,
      int collideCooldown,
      DamageType damageType) {
    this.name = name;
    this.speed = speed;
    this.mass = mass;
    this.onWallHit = onWallHit;
    this.canEnterOpenPits = canEnterOpenPits;
    this.texture = new SimpleIPath(texture);
    this.health = health;
    this.onDeath = onDeath;
    this.removeOnDeath = removeOnDeath;
    this.deathSound = deathSound;
    this.drops = drops;
    this.dropChance = dropChance;
    this.guaranteedDrops = guaranteedDrops;
    this.idleSound = idleSound;
    this.fightAISupplier = fightAISupplier;
    this.idleAISupplier = idleAISupplier;
    this.transitionAISupplier = transitionAISupplier;
    this.collideDamage = collideDamage;
    this.collideCooldown = collideCooldown;
    this.damageType = damageType;
  }

  /**
   * Builds an {@link Entity} of this monster type at the given position.
   *
   * @param position The spawn position.
   * @return A configured {@link Entity} with components attached.
   * @throws IOException If the monster’s texture cannot be loaded.
   */
  public Entity build(Point position) throws IOException {
    Entity monster = new Entity(name);
    monster.add(new PositionComponent(position));
    monster.add(buildDrawComponent());
    monster.add(buildVelocityComponent());
    monster.add(new CollideComponent());
    monster.add(buildSpikeComponent());
    monster.add(buildAIComponent());
    monster.add(buildHealthComponent());
    monster.add(buildInventoryComponent());

    buildIdleSoundComponent().ifPresent(monster::add);
    return monster;
  }

  private InventoryComponent buildInventoryComponent() {
    InventoryComponent ic = new InventoryComponent(drops.size() + guaranteedDrops.size());

    // 1. Always drop guaranteed items
    for (Item item : guaranteedDrops) {
      ic.add(item);
    }

    // 2. Chance-based drops
    if (!drops.isEmpty() && RANDOM.nextFloat() < dropChance) {
      ic.add(drops.stream().skip(RANDOM.nextInt(drops.size())).findFirst().orElse(null));
    }

    return ic;
  }

  private SpikyComponent buildSpikeComponent() {
    return new SpikyComponent(collideDamage, damageType, collideCooldown);
  }

  private AIComponent buildAIComponent() {
    return new AIComponent(fightAISupplier.get(), idleAISupplier.get(), transitionAISupplier.get());
  }

  private VelocityComponent buildVelocityComponent() {
    return new VelocityComponent(speed, mass, onWallHit, canEnterOpenPits);
  }

  private HealthComponent buildHealthComponent() {
    Consumer<Entity> constructedOnDeath =
        entity -> {
          onDeath.accept(entity);
          playDeathSoundIfNearby(deathSound.sound(), entity);

          entity
              .fetch(InventoryComponent.class)
              .ifPresent(inventoryComponent -> new DropItemsInteraction().accept(entity, null));

          if (removeOnDeath) Game.remove(entity);
        };

    return new HealthComponent(health, constructedOnDeath);
  }

  private DrawComponent buildDrawComponent() throws IOException {
    return new DrawComponent(texture);
  }

  private Optional<IdleSoundComponent> buildIdleSoundComponent() {
    if (idleSound == null || idleSound.path().pathString().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new IdleSoundComponent(idleSound.path()));
  }

  private static void playMonsterDieSound(Sound sound) {
    if (sound == null) {
      return;
    }
    long soundID = sound.play();
    sound.setLooping(soundID, false);
    sound.setVolume(soundID, 0.35f);
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

  /** Builder class for creating and configuring monster entities. */
  public static class MonsterBuilder {
    private final DungeonMonster type;
    private Point spawnPoint = new Point(0, 0);
    private boolean addToGame = false;

    /**
     * Builder for creating and configuring monster entities.
     *
     * @param type Type of monster to build.
     */
    private MonsterBuilder(DungeonMonster type) {
      this.type = type;
    }

    /**
     * Add this entity to the {@link core.Game Game} upon building it.
     *
     * @return This builder for method chaining.
     * @see Game#add(Entity)
     */
    public MonsterBuilder addToGame() {
      this.addToGame = true;
      return this;
    }

    /**
     * Sets the spawn point for the monster.
     *
     * @param spawnPoint The position to spawn the monster.
     * @return This builder.
     */
    public MonsterBuilder spawn(Point spawnPoint) {
      this.spawnPoint = spawnPoint;
      return this;
    }

    /**
     * Adds a guaranteed item drop for the monster.
     *
     * @param item Item to guarantee as a drop.
     * @return This builder.
     */
    public MonsterBuilder addGuaranteedDrop(Item item) {
      this.type.guaranteedDrops.add(item);
      return this;
    }

    /**
     * Builds and returns the configured monster entity.
     *
     * @return A configured {@link Entity}.
     * @throws IOException If texture loading fails.
     */
    public Entity build() throws IOException {
      Entity monster = type.build(spawnPoint);
      if (addToGame) Game.add(monster);
      return monster;
    }
  }

  /** Utility class providing random monster selection. */
  public static final class MonsterTable {
    private static final DungeonMonster[] all = DungeonMonster.values();

    /**
     * Returns a random monster type.
     *
     * @return A random {@link DungeonMonster}.
     */
    public static DungeonMonster randomMonsterType() {
      return all[RANDOM.nextInt(all.length)];
    }
  }

  /**
   * Creates a {@link MonsterBuilder} for this monster type.
   *
   * @return A builder instance for configuring and spawning this monster.
   */
  public MonsterBuilder builder() {
    return new MonsterBuilder(this);
  }
}
