package contrib.utils.components.skill.selfSkill;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.systems.EventScheduler;
import contrib.utils.LevelUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Starts a shock wave from the boss. The shock wave is a circular explosion of fireballs. */
public class FireShockWaveSkill extends Skill {

  private static final String SKILL_NAME = "FIRE_SHOCKWAVE";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final int REMOVE_AFTER = 2000;
  private static final long DELAY_BETWEEN_WAVES = 250L;
  private static final int HIT_COOLDOWN = Game.frameRate() / 4;

  private final int radius;
  private final int damage;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param damageAmount The base damage dealt by the projectile.
   * @param radius The radius of the shockwave.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  @SafeVarargs
  public FireShockWaveSkill(
      long cooldown, int damageAmount, int radius, Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, cooldown, resourceCost);
    this.radius = radius;
    this.damage = damageAmount;
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point casterPos = caster.fetch(PositionComponent.class).orElseThrow().position();
    Tile casterTile = Game.tileAt(casterPos).orElse(null);
    if (casterTile == null) {
      return;
    }
    List<Coordinate> placedPositions = new ArrayList<>();
    LevelUtils.explosionAt(
        casterTile.coordinate(),
      radius,
      DELAY_BETWEEN_WAVES,
        (tile -> {
          if (tile == null
              || tile.levelElement() == LevelElement.WALL
              || tile.coordinate().equals(casterTile.coordinate())
              || placedPositions.contains(tile.coordinate())) {
            return;
          }
          placedPositions.add(tile.coordinate());

          Entity entity = new Entity(SKILL_NAME + "_entity");
          PositionComponent posComp = new PositionComponent(tile.coordinate().toCenteredPoint());
          entity.add(posComp);
          entity.add(new CollideComponent());
          try {
            DrawComponent drawComp = new DrawComponent(TEXTURE);
            drawComp.currentAnimation("run_down");
            entity.add(drawComp);
          } catch (IOException e) {
            throw new RuntimeException("Could not load fireball texture" + e);
          }
          entity.add(new SpikyComponent(damage, DAMAGE_TYPE, HIT_COOLDOWN));
          Game.add(entity);

          EventScheduler.scheduleAction(() -> Game.remove(entity), REMOVE_AFTER);
        }));
  }
}
