package contrib.entities;

import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.components.CameraComponent;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 *
 * <p>This class only references Methods of the {@link HeroFactory} and {@link MiscFactory}
 */
public final class EntityFactory {

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link core.components.PlayerComponent}. {@link
   * PositionComponent}, {@link VelocityComponent} {@link core.components.DrawComponent}, {@link
   * contrib.components.CollideComponent} and {@link HealthComponent}.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero() throws IOException {
    return HeroFactory.newHero();
  }

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link core.components.PlayerComponent}. {@link
   * PositionComponent}, {@link VelocityComponent} {@link core.components.DrawComponent}, {@link
   * contrib.components.CollideComponent} and {@link HealthComponent}.
   *
   * @param deathCallback function that will be executed if the hero dies
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero(Consumer<Entity> deathCallback) throws IOException {
    return HeroFactory.newHero(HeroFactory.DEFAULT_HERO_CLASS, deathCallback);
  }

  /**
   * Get an Entity that can be used as a monster.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} with
   * random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
   * Animation, {@link VelocityComponent}, {@link CollideComponent} and a 10% chance for an {@link
   * InventoryComponent}. If it has an Inventory it will use the {@link DropItemsInteraction} on
   * death.
   *
   * <p>The Monster will be placed at the {@link PositionComponent#ILLEGAL_POSITION}.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity randomMonster() throws IOException {
    return DungeonMonster.randomMonster().builder().build(PositionComponent.ILLEGAL_POSITION);
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>Will contain some random items.
   *
   * <p>The Entity is not added to the game yet. *
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newChest() throws IOException {
    return MiscFactory.newChest(MiscFactory.FILL_CHEST.RANDOM);
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>It will contain the given items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param item Items that should be in the chest.
   * @param position Where should the chest be placed?
   * @return A new Entity.
   * @throws IOException If the animation could not be loaded.
   */
  public static Entity newChest(final Set<Item> item, final Point position) throws IOException {
    return MiscFactory.newChest(item, position);
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newCraftingCauldron() throws IOException {
    return MiscFactory.newCraftingCauldron();
  }

  /**
   * Creates a destructible stone entity.
   *
   * <p>The stone requires a hammer to break. All items in {@code items} are stored inside and
   * dropped upon destruction.
   *
   * @param spawnPoint the world position where the stone is spawned.
   * @param items the items stored inside the stone.
   * @return a new {@link Entity} representing the stone.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newStone(Point spawnPoint, final Set<Item> items) throws IOException {
    return MiscFactory.newStone(spawnPoint, items);
  }

  /**
   * Creates a destructible stone entity with no stored items.
   *
   * <p>The stone requires a hammer to break.
   *
   * @param spawnPoint the world position where the stone is spawned.
   * @return a new {@link Entity} representing the stone.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newStone(Point spawnPoint) throws IOException {
    Set<Item> stoneItems = new HashSet<>();
    return MiscFactory.newStone(spawnPoint, stoneItems);
  }

  /**
   * Creates a destructible stone entity with randomized drops.
   *
   * <p>The stone requires a hammer to break. Instead of a fixed set of items, a single random item
   * may be dropped based on {@code dropChance}.
   *
   * @param spawnPoint the world position where the stone is spawned.
   * @param dropChance a value between 0.0 and 1.0 indicating the probability that the stone drops
   *     an item when destroyed.
   * @return a new {@link Entity} representing the stone.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newStone(Point spawnPoint, float dropChance) throws IOException {
    return MiscFactory.newStone(spawnPoint, dropChance);
  }

  /**
   * Creates a destructible vase entity.
   *
   * <p>The vase does not require a hammer to break. All items in {@code items} are stored inside
   * and dropped upon destruction.
   *
   * @param spawnPoint the world position where the vase is spawned.
   * @param items the items stored inside the vase.
   * @return a new {@link Entity} representing the vase.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newVase(Point spawnPoint, final Set<Item> items) throws IOException {
    return MiscFactory.newVase(spawnPoint, items);
  }

  /**
   * Creates a destructible vase entity with no stored items.
   *
   * <p>The vase does not require a hammer to break.
   *
   * @param spawnPoint the world position where the vase is spawned.
   * @return a new {@link Entity} representing the vase.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newVase(Point spawnPoint) throws IOException {
    Set<Item> vaseItems = new HashSet<>();
    return MiscFactory.newVase(spawnPoint, vaseItems);
  }

  /**
   * Creates a destructible vase entity with randomized drops.
   *
   * <p>The vase does not require a hammer to break. Instead of a fixed set of items, a single
   * random item may be dropped based on {@code dropChance}.
   *
   * @param spawnPoint the world position where the vase is spawned.
   * @param dropChance a value between 0.0 and 1.0 indicating the probability that the vase drops an
   *     item when destroyed.
   * @return a new {@link Entity} representing the vase.
   * @throws IOException if loading textures or animations fails.
   */
  public static Entity newVase(Point spawnPoint, float dropChance) throws IOException {
    return MiscFactory.newVase(spawnPoint, dropChance);
  }

  /**
   * Creates a moving and fireball shooting sentry.
   *
   * @param a the first patrol point and spawn position.
   * @param b the second patrol point.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that patrols and launches fireball projectiles.
   */
  public static Entity newMovingFireballSentry(
      Point a, Point b, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.projectileLauncherSentry(
        a,
        b,
        shootDirection,
        new FireballSkill(SkillTools::heroPositionAsPoint, cooldown, range, false),
        range,
        false);
  }

  /**
   * Creates a moving and fireball shooting sentry.
   *
   * @param a the first patrol point and spawn position.
   * @param b the second patrol point.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that patrols and launches fireball projectiles.
   */
  public static Entity newMovingArrowSentry(
      Point a, Point b, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.projectileLauncherSentry(
        a,
        b,
        shootDirection,
        new BowSkill(SkillTools::heroPositionAsPoint, cooldown, range, false),
        range,
        false);
  }

  /**
   * Creates a moving and fireball shooting sentry.
   *
   * <p>This variant is intended for sentries placed inside a wall.
   *
   * @param a the first patrol point and spawn position.
   * @param b the second patrol point.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that patrols and launches fireball projectiles.
   */
  public static Entity newMovingFireballWallSentry(
      Point a, Point b, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.projectileLauncherSentry(
        a,
        b,
        shootDirection,
        new FireballSkill(SkillTools::heroPositionAsPoint, cooldown, range, true),
        range,
        true);
  }

  /**
   * Creates a moving and fireball shooting sentry.
   *
   * <p>This variant is intended for sentries placed inside a wall.
   *
   * @param a the first patrol point and spawn position.
   * @param b the second patrol point.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that patrols and launches arrow projectiles.
   */
  public static Entity newMovingArrowWallSentry(
      Point a, Point b, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.projectileLauncherSentry(
        a,
        b,
        shootDirection,
        new BowSkill(SkillTools::heroPositionAsPoint, cooldown, range, true),
        range,
        true);
  }

  /**
   * Creates a stationary fireball shooting sentry.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that is standing still on a fixed position.
   */
  public static Entity newStationaryFireballSentry(
      Point spawnPoint, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.stationarySentry(
        spawnPoint,
        shootDirection,
        new FireballSkill(SkillTools::heroPositionAsPoint, cooldown, range, false),
        range,
        false);
  }

  /**
   * Creates a stationary arrow shooting sentry.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between arrows.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that is standing still on a fixed position.
   */
  public static Entity newStationaryArrowSentry(
      Point spawnPoint, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.stationarySentry(
        spawnPoint,
        shootDirection,
        new BowSkill(SkillTools::heroPositionAsPoint, cooldown, range, false),
        range,
        false);
  }

  /**
   * Creates a stationary fireball shooting sentry.
   *
   * <p>This variant is intended for sentries placed inside a wall.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between fireballs.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that is standing still on a fixed position.
   */
  public static Entity newStationaryFireballWallSentry(
      Point spawnPoint, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.stationarySentry(
        spawnPoint,
        shootDirection,
        new FireballSkill(SkillTools::heroPositionAsPoint, cooldown, range, true),
        range,
        true);
  }

  /**
   * Creates a stationary arrow shooting sentry.
   *
   * <p>This variant is intended for sentries placed inside a wall.
   *
   * @param spawnPoint the spawn position of the entity.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param cooldown cooldown between arrows.
   * @param range Maximum shooting (projectile travel) range.
   * @return a sentry entity that is standing still on a fixed position.
   */
  public static Entity newStationaryArrowWallSentry(
      Point spawnPoint, Direction shootDirection, long cooldown, float range) {
    return SentryFactory.stationarySentry(
        spawnPoint,
        shootDirection,
        new BowSkill(SkillTools::heroPositionAsPoint, cooldown, range, true),
        range,
        true);
  }
}
