package level.devlevel;

import components.MagicShieldComponent;
import contrib.components.AIComponent;
import contrib.components.InventoryComponent;
import contrib.entities.AIFactory;
import contrib.entities.MiscFactory;
import contrib.entities.MonsterBuilder;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.systems.HealthSystem;
import contrib.utils.EntityUtils;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import contrib.utils.components.health.IHealthObserver;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.components.MissingComponentException;
import entities.BossAttackSkills;
import entities.DevDungeonMonster;
import item.concreteItem.ItemResourceBerry;
import item.concreteItem.ItemReward;
import java.util.List;
import level.DevDungeonLevel;
import systems.DevHealthSystem;

/** The Final Boss Level. */
public class BossLevel extends DevDungeonLevel implements IHealthObserver {

  private static final DevDungeonMonster BOSS_TYPE = DevDungeonMonster.FINAL_BOSS;
  private static final int MIN_MOB_COUNT = 5;
  private static final int MAX_MOB_COUNT = 7;
  private static final DevDungeonMonster[] MOB_TYPES =
      new DevDungeonMonster[] {DevDungeonMonster.IMP};

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

  /**
   * Creates a new BossLevel.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public BossLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Final Boss",
        "Woah! What is this place? This place is scorching, and I'm getting uneasy. We should prepare ourselves just in case.");

    this.levelBossSpawn = customPoints().getFirst();
    this.pillars = getCoordinates(1, 4); // Top left corner (each 2x2)
    this.entrance = customPoints().get(5); // Entrance into the arena
    this.chestSpawn = customPoints().get(6);
    this.cauldronSpawn = customPoints().get(7);
  }

  @Override
  protected void onFirstTick() {
    this.boss = utils.EntityUtils.spawnBoss(BOSS_TYPE, levelBossSpawn, this::handleBossDeath);
    ((DevHealthSystem) Game.systems().get(DevHealthSystem.class)).registerObserver(this);
    spawnChestsAndCauldrons();
  }

  @Override
  protected void onTick() {
    Coordinate heroCoord = EntityUtils.getHeroCoordinate();
    if (heroCoord == null) return;
    if (heroCoord.y() > entrance.y()) {
      tileAt(entrance).ifPresent(t -> changeTileElementType(t, LevelElement.WALL));
    } else {
      tileAt(entrance).ifPresent(t -> changeTileElementType(t, LevelElement.FLOOR));
    }
    handleBossAttacks();
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
      throw new RuntimeException("Failed to create chest entity at " + chestSpawn, e);
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    pc.position(chestSpawn.toPoint());
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
      throw new RuntimeException("Failed to create cauldron entity at " + cauldronSpawn, e);
    }
    pc =
        cauldon
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldon, PositionComponent.class));
    pc.position(cauldronSpawn.toPoint());
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
        boss.fetch(AIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(boss, AIComponent.class));
    if (!(aiComp.fightBehavior() instanceof AIRangeBehaviour AIRangeBehaviour)) return;

    if (isBoss2ndPhase) {
      if (anyOtherMobsAlive()) {
        AIRangeBehaviour.skill(BossAttackSkills.SKILL_NONE());
        return;
      } else {
        boss.remove(MagicShieldComponent.class);
        // workaround to remove the tint color, as no callback is provided
        boss.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(-1));
      }
    }

    if (System.currentTimeMillis() - lastAttackChange > getBossAttackChangeDelay()
        && isBossNormalAttacking) {
      this.lastAttackChange = System.currentTimeMillis();
      AIRangeBehaviour.skill(BossAttackSkills.getFinalBossSkill(boss));
      this.isBossNormalAttacking = false;
    } else if (!isBossNormalAttacking) {
      AIRangeBehaviour.skill(BossAttackSkills.normalAttack(AIFactory.FIREBALL_COOL_DOWN));
      this.isBossNormalAttacking = true;
    }
  }

  /**
   * Gets called when the boss dies.
   *
   * @param boss The boss entity.
   * @see #onFirstTick()
   * @see utils.EntityUtils#spawnBoss(MonsterBuilder, Coordinate)
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
   * @see #onHealthEvent(HealthSystem.HSData, HealthEvent)
   */
  private void triggerBoss2ndPhase() {
    this.isBoss2ndPhase = true;

    boss.add(new MagicShieldComponent(Integer.MAX_VALUE, 0));

    Coordinate[] tilesAroundBoss =
        LevelUtils.accessibleTilesInRange(levelBossSpawn.toPoint(), 6).stream()
            .map(Tile::coordinate)
            .filter(c -> c.distance(levelBossSpawn) > 3)
            .toArray(Coordinate[]::new);
    utils.EntityUtils.spawnMobs(
        ILevel.RANDOM.nextInt(MIN_MOB_COUNT, MAX_MOB_COUNT), MOB_TYPES, tilesAroundBoss);

    // Destroy pillars
    for (Coordinate pillarTopLeftCoord : pillars) {
      tileAt(pillarTopLeftCoord).ifPresent(t -> changeTileElementType(t, LevelElement.FLOOR));
      tileAt(new Coordinate(pillarTopLeftCoord.x() + 1, pillarTopLeftCoord.y()))
          .ifPresent(t -> changeTileElementType(t, LevelElement.FLOOR));
      tileAt(new Coordinate(pillarTopLeftCoord.x(), pillarTopLeftCoord.y() - 1))
          .ifPresent(t -> changeTileElementType(t, LevelElement.FLOOR));
      tileAt(new Coordinate(pillarTopLeftCoord.x() + 1, pillarTopLeftCoord.y() - 1))
          .ifPresent(t -> changeTileElementType(t, LevelElement.FLOOR));
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
    double currentPercentage = BossAttackSkills.calculateBossHealthPercentage(boss);

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
    return Game.levelEntities().filter(e -> e.isPresent(AIComponent.class)).toList().size()
        > 1; // 1 is the boss
  }

  @Override
  public void onHealthEvent(HealthSystem.HSData hsData, HealthEvent healthEvent) {
    if (hsData.e() != boss || healthEvent != HealthEvent.DAMAGE) return;

    // on first time 50% trigger sub phase
    if (!isBoss2ndPhase
        && hsData.hc().currentHealthpoints() <= hsData.hc().maximalHealthpoints() / 2) {
      triggerBoss2ndPhase();
    }
  }
}
