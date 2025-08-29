package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

public class FireShockWaveSkill extends DamageProjectileSkill {

  /*
     return new Skill(
     (skillUser) -> {
       Point bossPos =
           skillUser
               .fetch(PositionComponent.class)
               .orElseThrow(
                   () -> MissingComponentException.build(skillUser, PositionComponent.class))
               .position();
       Tile bossTile = Game.tileAt(bossPos).orElse(null);
       if (bossTile == null) {
         return;
       }
       List<Coordinate> placedPositions = new ArrayList<>();
       LevelUtils.explosionAt(
           bossTile.coordinate(),
           radius,
           250L,
           (tile -> {
             if (tile == null
                 || tile.levelElement() == LevelElement.WALL
                 || tile.coordinate().equals(bossTile.coordinate())
                 || placedPositions.contains(tile.coordinate())) {
               return;
             }
             placedPositions.add(tile.coordinate());

             Entity entity = new Entity("fire");
             PositionComponent posComp =
                 new PositionComponent(tile.coordinate().toCenteredPoint());
             entity.add(posComp);
             entity.add(new CollideComponent());
             try {
               DrawComponent drawComp = new DrawComponent(new SimpleIPath("skills/fireball"));
               drawComp.currentAnimation("run_down");
               entity.add(drawComp);
             } catch (IOException e) {
               throw new RuntimeException("Could not load fireball texture" + e);
             }
             entity.add(
                 new SpikyComponent(
                     FIRE_SHOCKWAVE_DAMAGE, DamageType.FIRE, Game.frameRate() / 4));
             Game.add(entity);

             EventScheduler.scheduleAction(() -> Game.remove(entity), 2000);
           }));
     },
     10 * 1000);
  */

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param end A supplier providing the endpoint (target location) of the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param pircing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param hitBoxSize The hitbox size of the projectile used for collision detection.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  public FireShockWaveSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
        cooldown,
        texture,
        end,
        speed,
        range,
        pircing,
        damageAmount,
        damageType,
        hitBoxSize,
        resourceCost);
  }

  public FireShockWaveSkill(int radius) {
    this(
        "",
        0,
        new SimpleIPath(""),
        new Supplier<Point>() {
          @Override
          public Point get() {
            return new Point(0, 0);
          }
        },
        0,
        0,
        false,
        0,
        DamageType.FIRE,
        Vector2.ZERO);
  }
}
