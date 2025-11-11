package produsAdvanced.abstraction.portals.portalSkills;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;
import produsAdvanced.abstraction.portals.components.PortalComponent;

/**
 * Base class for portal skills, defines the projectile characteristics like speed, range, hitbox
 * and cooldown. Also defines the behaviour when hitting a wall and when the projectile is getting
 * created.
 */
public class PortalSkill extends ProjectileSkill {

  /* Projectile characteristics */
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.5, 0.5);
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.25, 0.25);
  private static final long COOLDOWN = 500;
  private PortalColor portalColor;

  /**
   * Creates a new portal skill.
   *
   * @param portalColor Color of the portal.
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(PortalColor portalColor, Tuple<Resource, Integer>... resourceCost) {
    super(
        portalColor.equals(PortalColor.BLUE) ? "BLUE_PORTAL" : "GREEN_PORTAL",
        COOLDOWN,
        portalColor.equals(PortalColor.BLUE)
            ? new SimpleIPath("skills/blue_projectile")
            : new SimpleIPath("skills/green_projectile"),
        SPEED,
        RANGE,
        HIT_BOX_SIZE,
        HIT_BOX_OFFSET,
        false,
        SkillTools::cursorPositionAsPoint,
        resourceCost);
    this.portalColor = portalColor;
  }

  /**
   * When a portal wall is hit, creates a portal on the wall position.
   *
   * @param caster Entity that cast the portal
   * @param projectile The portal entity
   */
  @Override
  protected void onWallHit(Entity caster, Entity projectile) {
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();

    Vector2 velocity = vc.currentVelocity().normalize();
    Point movedPos = pc.position().translate(velocity);
    Coordinate portalPosition =
        new Point(Math.round(movedPos.x()), Math.round(movedPos.y())).toCoordinate();

    if (Game.tileAt(portalPosition).isPresent()
        && Game.tileAt(portalPosition).get().levelElement() == LevelElement.PORTAL) {
      Direction direction = setPortalDirection(portalPosition.toPoint(), pc.position());
      PortalFactory.createPortal(portalPosition.toPoint(), direction, portalColor);
    }
    Game.remove(projectile);
  }

  /**
   * Creates the projectile with all relevant Components.
   *
   * @param caster Entity that shoots the portal
   * @param start Position from where the portal shoots
   * @param aimedOn Position where the portal is aimed on
   */
  @Override
  protected void shootProjectile(Entity caster, Point start, Point aimedOn) {
    Entity projectile = new Entity(name() + "_projectile");
    ignoreEntities.add(caster);
    ignoreEntities.add(projectile);

    projectile.add(new FlyComponent());
    DrawComponent dc = new DrawComponent(texture);
    projectile.add(new PortalComponent());
    dc.tintColor(tintColor);
    projectile.add(dc);

    // Target point calculation
    Point targetPoint = SkillTools.calculateLastPositionInRange(start, aimedOn, range);

    Point position = start.translate(hitBoxSize.scale(-0.5)); // +offset
    PositionComponent pc = new PositionComponent(position);
    projectile.add(pc);
    // calculate rotation
    double angleDeg = Vector2.of(position).angleToDeg(Vector2.of(targetPoint));
    pc.rotation((float) angleDeg);
    // Calculate velocity
    Vector2 forceToApply = SkillTools.calculateDirection(start, targetPoint).scale(speed);

    // Add components
    VelocityComponent vc = new VelocityComponent(speed, handleProjectileWallHit(caster), true);
    vc.canEnterGitter(true);
    projectile.add(vc);
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
        new CollideComponent(
            HIT_BOX_OFFSET, HIT_BOX_SIZE, onCollideEnter(caster), onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    cc.isSolid(false);
    projectile.add(cc);

    Game.add(projectile);
    onSpawn(caster, projectile);
  }

  /**
   * Determines the direction a portal should face, based on nearby floor tiles.
   *
   * <p>The closest floor tile to the portal is used to decide its orientation. The calculated
   * direction is also stored for teleportation logic.
   *
   * @param wallPos the position of the portal
   * @param projectilePos original position of the projectile, needed for direction
   * @return the direction the portal should face
   */
  protected Direction setPortalDirection(Point wallPos, Point projectilePos) {
    HashSet<Tile> neighbours = new HashSet<>(Game.neighbours(Game.tileAt(wallPos).get()));
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      double distance = projectilePos.distance(tile.position());
      list.add(new Tuple<>(tile.position(), distance));
    }

    /* Sorts the list so the nearestTile is at the first slot */
    list.sort(Comparator.comparingDouble(Tuple::b));

    Point nearestTile = list.removeFirst().a();
    /* If nearest is a wall, happens if the angle of the impact at the wall is too steep, take the next best option which is the desired direction. */
    if (Game.tileAt(nearestTile).get().levelElement() == LevelElement.WALL
        || Game.tileAt(nearestTile).get().levelElement() == LevelElement.PORTAL) {
      nearestTile = list.removeFirst().a();
    }
    Point pointDirection = new Point(wallPos.x() - nearestTile.x(), wallPos.y() - nearestTile.y());
    Direction direction = toDirection(pointDirection).opposite();

    return direction;
  }

  /**
   * Converts a point into a direction constant.
   *
   * @param p the point offset
   * @return the corresponding direction
   */
  private Direction toDirection(Point p) {
    if (p.equals(new Point(0, 1))) return Direction.UP;
    if (p.equals(new Point(0, -1))) return Direction.DOWN;
    if (p.equals(new Point(1, 0))) return Direction.RIGHT;
    return Direction.LEFT; // default / fallback
  }
}
