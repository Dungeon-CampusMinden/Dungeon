package level.devlevel;

import components.MagicShieldComponent;
import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.AIFactory;
import contrib.entities.DialogFactory;
import contrib.entities.IHealthObserver;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.utils.components.ai.fight.RangeAI;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.components.MissingComponentException;
import entities.BossAttackSkills;
import entities.MonsterType;
import item.concreteItem.ItemReward;
import java.util.List;
import java.util.function.Consumer;
import level.DevDungeonLevel;
import level.utils.ITickable;
import starter.DevDungeon;
import systems.DevHealthSystem;
import utils.EntityUtils;

/** The Final Boss Level */
public class BossLevel extends DevDungeonLevel implements ITickable, IHealthObserver {

  // Difficulty
  public static final int BOSS_HP = 100;
  private static final MonsterType BOSS_TYPE = MonsterType.FINAL_BOSS;
  private static final int MIN_MOB_COUNT = 5;
  private static final int MAX_MOB_COUNT = 7;
  private static final MonsterType[] MOB_TYPES = new MonsterType[] {MonsterType.IMP};

  // Spawn Points / Locations
  private final Coordinate levelBossSpawn;
  private final Coordinate[] pillars;
  private final Coordinate entrance;
  private final Coordinate chestSpawn;
  private final Coordinate cauldronSpawn;
  private Entity boss;
  private long lastAttackChange = 0;
  private boolean isBossNormalAttacking = false;
  private boolean isBoss2ndPhase = false;

  public BossLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);

    this.levelBossSpawn = this.customPoints().getFirst();
    this.pillars = this.getCoordinates(1, 4); // Top left corner (each 2x2)
    this.entrance = this.customPoints().get(5); // Entrance into the arena
    this.chestSpawn = this.customPoints().get(6);
    this.cauldronSpawn = this.customPoints().get(7);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogFactory.showTextPopup(
          "Woah! What is this place? This place is scorching, and I'm getting uneasy. We should prepare ourselves just in case.",
          "Level " + DevDungeon.DUNGEON_LOADER.currentLevelIndex() + ": The Final Boss");
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
      this.handleFirstTick();
    }
    Coordinate heroCoord = EntityUtils.getHeroCoordinate();
    if (heroCoord == null) return;
    if (heroCoord.y > this.entrance.y) {
      this.changeTileElementType(this.tileAt(this.entrance), LevelElement.WALL);
    } else {
      this.changeTileElementType(this.tileAt(this.entrance), LevelElement.FLOOR);
    }
    this.handleBossAttacks();
  }

  private void handleFirstTick() {
    this.boss = EntityUtils.spawnBoss(BOSS_TYPE, this.levelBossSpawn, this::handleBossDeath);
    ((DevHealthSystem) Game.systems().get(DevHealthSystem.class)).registerObserver(this);
    this.spawnChestsAndCauldrons();
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create chest entity at " + this.chestSpawn, e);
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    pc.position(this.chestSpawn.toCenteredPoint());
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.WEAK));
    ic.add(new ItemResourceBerry());
    ic.add(new ItemResourceBerry());

    Game.add(chest);

    Entity cauldon;
    try {
      cauldon = MiscFactory.newCraftingCauldron();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create cauldron entity at " + this.cauldronSpawn, e);
    }
    pc =
        cauldon
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldon, PositionComponent.class));
    pc.position(this.cauldronSpawn.toCenteredPoint());
    Game.add(cauldon);
  }

  // Boss Methods

  /**
   * Handles the boss attacks.
   *
   * <p>The boss attacks are handled by changing the boss skill every 5 seconds. The boss will use a
   * normal attack skill and a special attack skill.
   *
   * @see BossAttackSkills
   * @see #getBossAttackChangeDelay()
   */
  private void handleBossAttacks() {
    AIComponent aiComp =
        this.boss
            .fetch(AIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(this.boss, AIComponent.class));
    if (!(aiComp.fightBehavior() instanceof RangeAI rangeAI)) return;

    if (this.isBoss2ndPhase) {
      if (this.anyOtherMobsAlive()) {
        rangeAI.setSkill(BossAttackSkills.SKILL_NONE());
        return;
      } else {
        this.boss.remove(MagicShieldComponent.class);
        // workaround to remove the tint color, as no callback is provided
        this.boss.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(-1));
      }
    }

    if (System.currentTimeMillis() - this.lastAttackChange > this.getBossAttackChangeDelay()
        && this.isBossNormalAttacking) {
      this.lastAttackChange = System.currentTimeMillis();
      rangeAI.setSkill(BossAttackSkills.getFinalBossSkill());
      this.isBossNormalAttacking = false;
    } else if (!this.isBossNormalAttacking) {
      rangeAI.setSkill(BossAttackSkills.normalAttack(AIFactory.FIREBALL_COOL_DOWN));
      this.isBossNormalAttacking = true;
    }
  }

  /**
   * Gets called when the boss dies.
   *
   * @param boss The boss entity.
   * @see #handleFirstTick()
   * @see EntityUtils#spawnBoss(MonsterType, Coordinate, Consumer)
   */
  private void handleBossDeath(Entity boss) {
    InventoryComponent invComp = new InventoryComponent();
    boss.add(invComp);
    invComp.add(new ItemReward());

    // TODO: Drop item on death.
  }

  /**
   * Gets called when the boss reaches 50% health.
   *
   * <p>Triggers the 2nd phase of the boss fight. The boss gets a magic shield and spawns mobs
   * around him. While these mobs are alive, the boss will not attack and is invulnerable.
   *
   * @see #handleBossAttacks()
   * @see #onHeathEvent(Entity, HealthComponent, HealthEvent)
   */
  private void triggerBoss2ndPhase() {
    this.isBoss2ndPhase = true;

    this.boss.add(new MagicShieldComponent(Integer.MAX_VALUE, 0));

    Coordinate[] tilesAroundBoss =
        LevelUtils.accessibleTilesInRange(this.levelBossSpawn.toPoint(), 6).stream()
            .map(Tile::coordinate)
            .filter(c -> c.distance(this.levelBossSpawn) > 3)
            .toArray(Coordinate[]::new);
    EntityUtils.spawnMobs(
        Game.currentLevel().RANDOM.nextInt(MIN_MOB_COUNT, MAX_MOB_COUNT),
        MOB_TYPES,
        tilesAroundBoss);

    // Destroy pillars
    for (Coordinate pillarTopLeftCoord : this.pillars) {
      this.changeTileElementType(this.tileAt(pillarTopLeftCoord), LevelElement.FLOOR);
      this.changeTileElementType(
          this.tileAt(new Coordinate(pillarTopLeftCoord.x + 1, pillarTopLeftCoord.y)),
          LevelElement.FLOOR);
      this.changeTileElementType(
          this.tileAt(new Coordinate(pillarTopLeftCoord.x, pillarTopLeftCoord.y - 1)),
          LevelElement.FLOOR);
      this.changeTileElementType(
          this.tileAt(new Coordinate(pillarTopLeftCoord.x + 1, pillarTopLeftCoord.y - 1)),
          LevelElement.FLOOR);
    }
  }

  // Util methods for Boss Logic

  /**
   * Gets the delay for changing the boss attack.
   *
   * <p>Starts at 5 seconds and decreases to 750ms as the boss health decreases.
   *
   * <p>E.g. 100% health = 5 seconds, 50% health = 2.875 seconds, 0% health = 750ms
   *
   * @return The delay for changing the boss attack in milliseconds.
   */
  private int getBossAttackChangeDelay() {
    double currentPercentage = BossAttackSkills.calculateBossHealthPercentage(this.boss);

    double delayAtFullHealth = 5000;
    double delayAtZeroHealth = 750;

    double delay =
        delayAtFullHealth
            + (delayAtZeroHealth - delayAtFullHealth) * (1 - currentPercentage / 100.0);

    return (int) delay;
  }

  /**
   * Checks if any other mobs are alive.
   *
   * <p>It checks how many mobs are alive by filtering all entities that have an AIComponent. There
   * should be at least 1 mob alive, the boss. But if any other mob is alive, this method returns
   * true.
   *
   * @return true if any other mobs are alive, false otherwise.
   */
  private boolean anyOtherMobsAlive() {
    return Game.entityStream().filter(e -> e.isPresent(AIComponent.class)).toList().size()
        > 1; // 1 is the boss
  }

  @Override
  public void onHeathEvent(
      Entity entity, HealthComponent healthComponent, HealthEvent healthEvent) {
    if (entity != this.boss || healthEvent != HealthEvent.DAMAGE) return;

    // on first time 50% trigger sub phase
    if (!this.isBoss2ndPhase
        && healthComponent.currentHealthpoints() <= healthComponent.maximalHealthpoints() / 2) {
      this.triggerBoss2ndPhase();
    }
  }
}
