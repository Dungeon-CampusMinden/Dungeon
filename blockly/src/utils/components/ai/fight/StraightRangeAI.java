package utils.components.ai.fight;

import contrib.systems.HealthSystem;
import contrib.utils.components.health.IHealthObserver;
import contrib.utils.components.skill.ISkillUser;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.game.ECSManagment;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import java.util.function.Consumer;
import utils.Direction;
import utils.EntityUtils;

/**
 * Attacks the player if he is the view range of the entity. The entity will only shoot in its view
 * direction.
 *
 * <p>This AI is used for ranged entities that can only attack in a straight line.
 */
public class StraightRangeAI implements Consumer<Entity>, ISkillUser, IHealthObserver {
  private static final int DEFAULT_RANGE_COLOR = 0xFF0000FF; // Red

  private final int range;
  private final boolean paintRange;
  private Skill skill;
  private Entity user;

  /**
   * Creates a new StraightRangeAI.
   *
   * @param range The view range of the AI.
   * @param skill The skill to be used when an attack is performed.
   */
  public StraightRangeAI(int range, Skill skill) {
    this(range, skill, true);
  }

  /**
   * Creates a new StraightRangeAI.
   *
   * @param range The view range of the AI.
   * @param skill The skill to be used when an attack is performed.
   * @param paintRange Whether to paint the range of the AI on to the Tiles.
   */
  public StraightRangeAI(int range, Skill skill, boolean paintRange) {
    this.range = range;
    this.skill = skill;
    this.paintRange = paintRange;

    // Register the observer to the HealthSystem
    if (!ECSManagment.systems().containsKey(HealthSystem.class)) {
      throw new IllegalStateException("HealthSystem is not available.");
    }
    ((HealthSystem) (ECSManagment.systems().get(HealthSystem.class))).registerObserver(this);
  }

  @Override
  public void accept(final Entity entity) {
    boolean playerInRange =
        EntityUtils.canEntitySeeOther(
            entity, Game.hero().orElseThrow(MissingHeroException::new), range);
    if (!playerInRange) return;

    useSkill(skill, entity);
  }

  @Override
  public void useSkill(Skill skill, Entity skillUser) {
    if (skill == null) {
      return;
    }
    skill.execute(skillUser);
  }

  @Override
  public Skill skill() {
    return this.skill;
  }

  @Override
  public void skill(Skill skill) {
    this.skill = skill;
  }

  private void tintRange(int color) {
    if (!paintRange) return;

    ILevel level = Game.currentLevel();
    if (level == null) return;
    PositionComponent pc =
        user.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(user, PositionComponent.class));
    Direction viewDirection = Direction.convertPosCompDirectionToUtilsDirection(pc.viewDirection());
    Coordinate start = pc.position().toCoordinate();
    Coordinate end;
    int currentRange = 0;
    while (currentRange < range) {
      end =
          start.add(
              new Coordinate(viewDirection.x() * currentRange, viewDirection.y() * currentRange));
      if (!utils.LevelUtils.canSee(start, end, viewDirection)) {
        break;
      }
      currentRange++;
    }
    end =
        start.add(
            new Coordinate(
                viewDirection.x() * (currentRange - 1), viewDirection.y() * (currentRange - 1)));
    LevelUtils.tintArea(start, end, color);
  }

  @Override
  public void onHealthEvent(HealthSystem.HSData hsData, HealthEvent healthEvent) {
    if (healthEvent != HealthEvent.DEATH || hsData.e() != user) {
      return;
    }

    tintRange(-1); // Remove the tint
  }

  /** Paints the range of the AI on to the Tiles. */
  public void paintRange() {
    tintRange(DEFAULT_RANGE_COLOR);
  }

  /**
   * Sets the user of this AI.
   *
   * <p>A user is needed to specify the Tiles to be painted.
   *
   * @param user The user entity.
   * @see #paintRange
   */
  public void user(Entity user) {
    this.user = user;
  }
}
