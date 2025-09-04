package contrib.entities;

import com.badlogic.gdx.audio.Sound;
import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.IdleSoundComponent;
import contrib.components.InventoryComponent;
import contrib.components.SpikyComponent;
import contrib.item.Item;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Shared monster builder to reduce duplication between different monster enums across projects.
 *
 * <p>- Provides sensible defaults for the "core framework" (Dungeon). - Allows sub-projects
 * (e.g., DevDungeon) to override specific parts via factories/hooks.
 *
 * <p>Overridable factories: - velocityFactory - inventoryFactory - healthFactory - extraComponentsHook
 * - onDeathExtra
 */
public final class MonsterBuilder {
  /**
   * Random instance for monsters.
   */
  public static Random RANDOM = new Random();

  /**
   * Maximum distance from hero within which to play the death sound when the monster dies.
   */
  private static final int MAX_DISTANCE_FOR_DEATH_SOUND = 15;

  private static final float DEATH_SOUND_VOLUME = 0.35f;
  /** The delay in seconds, for when the death sound should be disposed.
   * <p> This value practically defines the maximum length of a death sound.
   **/
  private static final int DEATH_SOUND_DISPOSE_DELAY = 10;

  // Basic config
  private String name = "";
  private IPath texture = Animation.MISSING_TEXTURE;

  // Health
  private int health = 1;
  private Consumer<Entity> onDeath = e -> {};
  private boolean removeOnDeath = true;

  // Sounds
  private MonsterDeathSound deathSound = null;
  private MonsterIdleSound idleSound = null;

  // AI
  private Supplier<Consumer<Entity>> fightAISupplier = () -> (e) -> {};
  private Supplier<Consumer<Entity>> idleAISupplier = () -> (e) -> {};
  private Supplier<Function<Entity, Boolean>> transitionAISupplier = () -> (e) -> false;

  // Movement
  private float speed = 1.0f;
  private float mass = 1.0f;
  private boolean canEnterOpenPits = false;
  private Consumer<Entity> onWallHit = e -> {};

  // Combat
  private int collideDamage = 0;
  private int collideCooldown = Game.frameRate();
  private DamageType damageType = DamageType.PHYSICAL;

  // Drops
  private final Set<Item> drops = new HashSet<>();
  private final Set<Item> guaranteedDrops = new HashSet<>();
  private float dropChance = 0.0f;

  // Position / lifecycle
  private Point spawnPoint = new Point(0, 0);
  private boolean addToGame = false;

  // -------------------
  // Builder API
  // -------------------

  /**
   * Set the monster's display name used in the Entity constructor.
   *
   * @param name the display name
   * @return this builder
   */
  public MonsterBuilder name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Set the texture path used to construct a SimpleIPath for the draw component.
   *
   * @param path path string for the texture
   * @return this builder
   */
  public MonsterBuilder texturePath(String path) {
    this.texture = new SimpleIPath(path);
    return this;
  }

  /**
   * Set the IPath texture used by the DrawComponent.
   *
   * @param path the IPath to use
   * @return this builder
   */
  public MonsterBuilder texture(IPath path) {
    this.texture = path;
    return this;
  }

  /**
   * Set max health for the monster.
   *
   * @param health health value
   * @return this builder
   */
  public MonsterBuilder health(int health) {
    this.health = health;
    return this;
  }

  /**
   * Set a death callback upon death.
   *
   * @param onDeath The callback
   * @return this builder
   */
  public MonsterBuilder onDeath(Consumer<Entity> onDeath) {
    this.onDeath = onDeath;
    return this;
  }

  /**
   * Controls whether the entity should be removed from the game on death.
   *
   * @param removeOnDeath true to remove on death
   * @return this builder
   */
  public MonsterBuilder removeOnDeath(boolean removeOnDeath) {
    this.removeOnDeath = removeOnDeath;
    return this;
  }

  /**
   * Set a death sound to be played (if hero is nearby) when this monster dies.
   *
   * @param sound death sound
   * @return this builder
   */
  public MonsterBuilder deathSound(MonsterDeathSound sound) {
    this.deathSound = sound;
    return this;
  }

  /**
   * Set the idle sound for this monster.
   *
   * @param sound idle sound
   * @return this builder
   */
  public MonsterBuilder idleSound(MonsterIdleSound sound) {
    this.idleSound = sound;
    return this;
  }

  /**
   * Set the fight AI supplier for this monster.
   *
   * @param fight supplier providing fight AI consumer
   * @return this builder
   */
  public MonsterBuilder fightAI(Supplier<Consumer<Entity>> fight) {
    this.fightAISupplier = fight;
    return this;
  }

  /**
   * Set the idle AI supplier for this monster.
   *
   * @param idle supplier providing idle AI consumer
   * @return this builder
   */
  public MonsterBuilder idleAI(Supplier<Consumer<Entity>> idle) {
    this.idleAISupplier = idle;
    return this;
  }

  /**
   * Set the transition AI supplier for this monster.
   *
   * @param transition supplier providing transition AI function
   * @return this builder
   */
  public MonsterBuilder transitionAI(Supplier<Function<Entity, Boolean>> transition) {
    this.transitionAISupplier = transition;
    return this;
  }

  /**
   * Set movement speed.
   *
   * @param speed speed value
   * @return this builder
   */
  public MonsterBuilder speed(float speed) {
    this.speed = speed;
    return this;
  }

  /**
   * Set movement mass.
   *
   * @param mass mass value
   * @return this builder
   */
  public MonsterBuilder mass(float mass) {
    this.mass = mass;
    return this;
  }

  /**
   * Whether this monster can enter open pits.
   *
   * @param can true if can enter open pits
   * @return this builder
   */
  public MonsterBuilder canEnterOpenPits(boolean can) {
    this.canEnterOpenPits = can;
    return this;
  }

  /**
   * Set a callback invoked when the monster hits a wall.
   *
   * @param onWallHit consumer receiving the entity
   * @return this builder
   */
  public MonsterBuilder onWallHit(Consumer<Entity> onWallHit) {
    this.onWallHit = onWallHit;
    return this;
  }

  /**
   * Set collide damage on touch.
   *
   * @param damage damage dealt on collision
   * @return this builder
   */
  public MonsterBuilder collideDamage(int damage) {
    this.collideDamage = damage;
    return this;
  }

  /**
   * Set cooldown (in frames) between collisions that deal damage.
   *
   * @param cooldownFrames cooldown frames
   * @return this builder
   */
  public MonsterBuilder collideCooldown(int cooldownFrames) {
    this.collideCooldown = cooldownFrames;
    return this;
  }

  /**
   * Set the damage type used by the spiky component.
   *
   * @param type damage type
   * @return this builder
   */
  public MonsterBuilder damageType(DamageType type) {
    this.damageType = type;
    return this;
  }

  /**
   * Add an optional drop item.
   *
   * @param item item to add to potential drops
   * @return this builder
   */
  public MonsterBuilder addDrop(Item item) {
    this.drops().add(item);
    return this;
  }

  /**
   * Set the potential drops set (replaces previous).
   *
   * @param items set of items
   * @return this builder
   */
  public MonsterBuilder drops(Set<Item> items) {
    this.drops().clear();
    this.drops().addAll(items);
    return this;
  }

  /**
   * Add a guaranteed drop item.
   *
   * @param item item guaranteed to drop
   * @return this builder
   */
  public MonsterBuilder addGuaranteedDrop(Item item) {
    this.guaranteedDrops().add(item);
    return this;
  }

  /**
   * Set guaranteed drops (replaces previous).
   *
   * @param items set of guaranteed items
   * @return this builder
   */
  public MonsterBuilder guaranteedDrops(Set<Item> items) {
    this.guaranteedDrops().clear();
    this.guaranteedDrops().addAll(items);
    return this;
  }

  /**
   * Set the probability to drop one of the optional drop items.
   *
   * @param chance between 0.0 and 1.0
   * @return this builder
   */
  public MonsterBuilder dropChance(float chance) {
    this.dropChance = chance;
    return this;
  }

  /**
   * Set the spawnPoint point for the monster.
   *
   * @param spawn spawnPoint point
   * @return this builder
   */
  public MonsterBuilder spawnPoint(Point spawn) {
    this.spawnPoint = spawn;
    return this;
  }

  /**
   * Mark this monster to be added to the Game when built.
   *
   * @return this builder
   */
  public MonsterBuilder addToGame() {
    this.addToGame = true;
    return this;
  }

  /**
   * Set whether to add this monster to the Game when built.
   *
   * @param add whether to add
   * @return this builder
   */
  public MonsterBuilder addToGame(boolean add) {
    this.addToGame = add;
    return this;
  }

  /**
   * Get the configured name.
   *
   * @return the name
   */
  public String name() {
    return name;
  }

  /**
   * Get the configured texture IPath.
   *
   * @return the texture IPath
   */
  public IPath texture() {
    return texture;
  }

  /**
   * Get the configured health.
   *
   * @return health value
   */
  public int health() {
    return health;
  }

  /**
   * Gets the callback that gets call upon death.
   *
   * @return death callback
   */
  public Consumer<Entity> onDeath() {
    return onDeath;
  }

  /**
   * Whether the monster is removed on death.
   *
   * @return true if removed on death
   */
  public boolean removeOnDeath() {
    return removeOnDeath;
  }

  /**
   * Get the optional death sound.
   *
   * @return optional death sound
   */
  public Optional<MonsterDeathSound> deathSound() {
    return Optional.ofNullable(deathSound);
  }

  /**
   * Get the optional idle sound path.
   *
   * @return optional idle sound IPath
   */
  public Optional<MonsterIdleSound> idleSoundPath() {
    return Optional.ofNullable(idleSound);
  }

  /**
   * Get the fight AI supplier.
   *
   * @return fight AI supplier
   */
  public Supplier<Consumer<Entity>> fightAISupplier() {
    return fightAISupplier;
  }

  /**
   * Get the idle AI supplier.
   *
   * @return idle AI supplier
   */
  public Supplier<Consumer<Entity>> idleAISupplier() {
    return idleAISupplier;
  }

  /**
   * Get the transition AI supplier.
   *
   * @return transition AI supplier
   */
  public Supplier<Function<Entity, Boolean>> transitionAISupplier() {
    return transitionAISupplier;
  }

  /**
   * Get the configured speed.
   *
   * @return speed
   */
  public float speed() {
    return speed;
  }

  /**
   * Get the configured mass.
   *
   * @return mass
   */
  public float mass() {
    return mass;
  }

  /**
   * Whether the monster can enter open pits.
   *
   * @return true if can enter open pits
   */
  public boolean canEnterOpenPits() {
    return canEnterOpenPits;
  }

  /**
   * Get the on-wall-hit consumer.
   *
   * @return onWallHit consumer
   */
  public Consumer<Entity> onWallHit() {
    return onWallHit;
  }

  /**
   * Get collide damage.
   *
   * @return collide damage
   */
  public int collideDamage() {
    return collideDamage;
  }

  /**
   * Get collide cooldown in frames.
   *
   * @return collide cooldown
   */
  public int collideCooldown() {
    return collideCooldown;
  }

  /**
   * Get damage type.
   *
   * @return damage type
   */
  public DamageType damageType() {
    return damageType;
  }

  /**
   * Get the potential drops set.
   *
   * @return set of potential drops
   */
  public Set<Item> drops() {
    return drops;
  }

  /**
   * Get the guaranteed drops set.
   *
   * @return set of guaranteed drops
   */
  public Set<Item> guaranteedDrops() {
    return guaranteedDrops;
  }

  /**
   * Get drop chance.
   *
   * @return drop chance
   */
  public float dropChance() {
    return dropChance;
  }

  /**
   * Get spawnPoint point.
   *
   * @return spawnPoint point
   */
  public Point spawnPoint() {
    return spawnPoint;
  }

  // Build

  /**
   * Construct the Entity configured by this builder.
   *
   * @return constructed Entity
   * @throws IOException if resource loading fails
   */
  public Entity build() throws IOException {
    Entity monster = new Entity(name);

    monster.add(new PositionComponent(spawnPoint()));
    monster.add(new DrawComponent(texture()));
    monster.add(new VelocityComponent(speed(), mass(), onWallHit(), canEnterOpenPits()));
    monster.add(new CollideComponent());
    if (collideDamage() > 0)
      monster.add(new SpikyComponent(collideDamage(), damageType(), collideCooldown()));
    monster.add(new AIComponent(fightAISupplier().get(), idleAISupplier().get(), transitionAISupplier().get()));
    monster.add(buildInventoryComponent());
    monster.add(buildHealthComponent());

    buildIdleSoundComponent().ifPresent(monster::add);

    if (addToGame) {
      Game.add(monster);
    }
    return monster;
  }

  private HealthComponent buildHealthComponent() {
    Consumer<Entity> constructedOnDeath =
      entity -> {
        onDeath().accept(entity);
        deathSound().ifPresent(deathSound -> playDeathSoundIfNearby(deathSound.sound(), DEATH_SOUND_DISPOSE_DELAY, entity));

        entity
          .fetch(InventoryComponent.class)
          .ifPresent(inventoryComponent -> new DropItemsInteraction().accept(entity, null));

        if (removeOnDeath()) Game.remove(entity);
      };

    return new HealthComponent(health, constructedOnDeath);
  }

  private InventoryComponent buildInventoryComponent() {
    InventoryComponent ic = new InventoryComponent(drops().size() + guaranteedDrops().size());

    // 1. Always drop guaranteed items
    for (Item item : guaranteedDrops()) {
      ic.add(item);
    }

    // 2. Chance-based drops
    if (!drops().isEmpty() && RANDOM.nextFloat() < dropChance()) {
      ic.add(drops().stream().skip(RANDOM.nextInt(drops().size())).findFirst().orElse(null));
    }

    return ic;
  }

  private Optional<IdleSoundComponent> buildIdleSoundComponent() {
    return idleSoundPath().flatMap(
      p -> Optional.of(new IdleSoundComponent(p.path())));
  }

  /**
   * Play the monster death sound with predefined volume and dispose delay.
   *
   * @param sound The sound to be played.
   * @param disposeDelay The delay in seconds after which the sound should be disposed.
   */
  protected void playMonsterDieSound(Sound sound, long disposeDelay) {
    // TODO: Replace with a more robust sound management system
    if (sound == null) return;
    long id = sound.play();
    sound.setLooping(id, false);
    sound.setVolume(id, DEATH_SOUND_VOLUME);

    EventScheduler.scheduleAction(sound::dispose, disposeDelay  * 1000L);
  }

  /**
   * Play the death sound if the hero is within a certain distance.
   *
   * @param entity The entity that died.
   * @param sound The sound to be played
   *              @param disposeDelay The delay in seconds after which the sound should be disposed.
   */
  protected void playDeathSoundIfNearby(Sound sound, long disposeDelay, Entity entity) {
    if (Game.hero().isEmpty()) return;

    Entity hero = Game.hero().get();
    PositionComponent pc =
      hero.fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    PositionComponent monsterPc =
      entity.fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    if (pc.position().distance(monsterPc.position()) < MAX_DISTANCE_FOR_DEATH_SOUND) {
      playMonsterDieSound(sound, disposeDelay);
    }
  }
}
