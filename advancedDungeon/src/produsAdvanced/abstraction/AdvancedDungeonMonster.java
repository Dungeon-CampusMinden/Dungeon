package produsAdvanced.abstraction;

import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.entities.DungeonMonster;
import contrib.entities.MonsterDeathSound;
import contrib.entities.MonsterIdleSound;
import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
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

/** Monster for the AdvancedDungeon. */
public enum AdvancedDungeonMonster {
  /** Elemental monster. */
  ELEMENTAL(
      "Monster Elemental",
      5f,
      1f,
      e -> {},
      false,
      "character/monster/elemental_goo_small",
      4,
      e -> {},
      true,
      MonsterDeathSound.HIGH_PITCH,
      Set.of(),
      0f,
      Set.of(),
      MonsterIdleSound.BURP,
      () -> entity -> {},
      () -> entity -> {},
      () -> entity -> false,
      0,
      0,
      DamageType.PHYSICAL),

  /** A non moving chort. */
  STATIC_CHORT(
      "Static Chort",
      0f,
      1f,
      e -> {},
      false,
      "character/monster/chort",
      1,
      e -> {},
      true,
      MonsterDeathSound.LOWER_PITCH,
      Set.of(),
      0.1f,
      Set.of(),
      MonsterIdleSound.BURP,
      () -> entity -> {},
      () -> entity -> {},
      () -> entity -> false,
      0,
      0,
      DamageType.PHYSICAL),
  /** A small red imp. */
  IMP(
      "Imp",
      4f,
      1f,
      e -> {},
      false,
      "character/monster/imp",
      5,
      e -> {},
      true,
      MonsterDeathSound.HIGH_PITCH,
      Set.of(new ItemPotionHealth()),
      0.1f,
      Set.of(),
      MonsterIdleSound.BURP,
      () -> entity -> {},
      () -> entity -> {},
      () -> entity -> false,
      0,
      0,
      DamageType.PHYSICAL),
  /** Dr. acula. */
  DOC(
      "Doc",
      4f,
      1f,
      e -> {},
      false,
      "character/monster/doc",
      5,
      e -> {},
      true,
      MonsterDeathSound.HIGH_PITCH,
      Set.of(),
      0.0f,
      Set.of(new ItemPotionHealth()),
      MonsterIdleSound.BURP,
      () -> entity -> {},
      () -> entity -> {},
      () -> entity -> false,
      0,
      0,
      DamageType.PHYSICAL),
  /** Looks familiar. */
  GOBLIN(
      "Goblin",
      4f,
      1f,
      e -> {},
      false,
      "character/monster/goblin",
      5,
      e -> {},
      true,
      MonsterDeathSound.HIGH_PITCH,
      Set.of(new ItemPotionWater()),
      0.0f,
      Set.of(),
      MonsterIdleSound.BURP,
      () -> entity -> {},
      () -> entity -> {},
      () -> entity -> false,
      0,
      0,
      DamageType.PHYSICAL);

  private static Random RANDOM = new Random();
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
  AdvancedDungeonMonster(
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
          playDeathSoundIfNearby(deathSound.path(), entity);

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
  public static class AdvancedMonsterBuilder {
    private final AdvancedDungeonMonster type;
    private Point spawnPoint = new Point(0, 0);

    private boolean addToGame = false;

    /**
     * Builder for creating and configuring monster entities.
     *
     * @param type Type of the Monster to build.
     */
    private AdvancedMonsterBuilder(AdvancedDungeonMonster type) {
      this.type = type;
    }

    /**
     * Add this entity to the {@link core.Game Game} upon building it.
     *
     * @return This builder for method chaining.
     * @see Game#add(Entity)
     */
    public AdvancedMonsterBuilder addToGame() {
      this.addToGame = true;
      return this;
    }

    /**
     * Sets the spawn point for the monster.
     *
     * @param spawnPoint The position to spawn the monster.
     * @return This builder.
     */
    public AdvancedMonsterBuilder spawn(Point spawnPoint) {
      this.spawnPoint = spawnPoint;
      return this;
    }

    /**
     * Adds a guaranteed item drop for the monster.
     *
     * @param item Item to guarantee as a drop.
     * @return This builder.
     */
    public AdvancedMonsterBuilder addGuaranteedDrop(Item item) {
      this.type.guaranteedDrops.add(item);
      return this;
    }

    /**
     * Builds and returns the configured monster entity.
     *
     * @return A configured {@link Entity}.
     * @throws IOException If texture loading fails.
     */
    public Entity build(Point spawnPoint) throws IOException {
      Entity monster = type.build(spawnPoint);
      if (addToGame) Game.add(monster);
      return monster;
    }
  }

  /** Utility class providing random monster selection. */
  public static final class MonsterTable {
    private static final AdvancedDungeonMonster[] all = AdvancedDungeonMonster.values();

    /**
     * Returns a random monster type.
     *
     * @return A random {@link DungeonMonster}.
     */
    public static AdvancedDungeonMonster randomMonsterType() {
      return all[RANDOM.nextInt(all.length)];
    }
  }

  /**
   * Creates a {@link DungeonMonster.MonsterBuilder} for this monster type.
   *
   * @return A builder instance for configuring and spawning this monster.
   */
  public AdvancedMonsterBuilder builder() {
    return new AdvancedMonsterBuilder(this);
  }
}
