package level.devlevel;

import contrib.components.AIComponent;
import contrib.utils.components.ai.fight.RangeAI;
import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.BossAttackSkills;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.utils.ITickable;
import utils.EntityUtils;

public class BossLevel extends DevDungeonLevel implements ITickable {

  // Difficulty
  public static final int BOSS_HP = 100;
  private static final MonsterType BOSS_TYPE = MonsterType.FINAL_BOSS;

  // Spawn Points / Locations
  private final Coordinate levelBossSpawn;
  private Entity boss;
  private long lastAttackChange = 0;
  private boolean isBossNormalAttacking = false;

  public BossLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);

    this.levelBossSpawn = this.customPoints().getFirst();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
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
    this.handleBossAttacks();
  }

  /**
   * Handles the boss attacks.
   *
   * <p>The boss attacks are handled by changing the boss skill every 5 seconds. The boss will use a
   * normal attack skill and a special attack skill.
   *
   * @see BossAttackSkills
   */
  private void handleBossAttacks() {
    AIComponent aiComp =
        this.boss
            .fetch(AIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(this.boss, AIComponent.class));
    if (aiComp.fightBehavior() instanceof RangeAI rangeAI) {
      if (System.currentTimeMillis() - this.lastAttackChange > this.getBossAttackChangeDelay()
          && this.isBossNormalAttacking) {
        this.lastAttackChange = System.currentTimeMillis();
        rangeAI.setSkill(BossAttackSkills.getFinalBossSkill());
        this.isBossNormalAttacking = false;
      } else {
        if (!this.isBossNormalAttacking) rangeAI.setSkill(BossAttackSkills.normalAttack());
        this.isBossNormalAttacking = true;
      }
    }
  }

  /**
   * Gets the delay for changing the boss attack.
   *
   * <p>Starts at 5 seconds and decreases to 350ms as the boss health decreases.
   *
   * <p>E.g. 100% health = 5 seconds, 50% health = 2.5 seconds, 0% health = 350ms.
   *
   * @return The delay for changing the boss attack in milliseconds.
   */
  private int getBossAttackChangeDelay() {
    double currentPercentage = BossAttackSkills.calculateBossHealthPercentage(this.boss);

    double delayAtFullHealth = 5000;
    double delayAtZeroHealth = 350;

    double delay =
        delayAtFullHealth
            + (delayAtZeroHealth - delayAtFullHealth) * (1 - currentPercentage / 100.0);

    return (int) delay;
  }

  private void handleFirstTick() {
    this.boss =
        EntityUtils.spawnBoss(
            BOSS_TYPE,
            this.levelBossSpawn,
            (bossEntity) -> {
              ((ExitTile) this.endTile()).open();
            });
  }
}
