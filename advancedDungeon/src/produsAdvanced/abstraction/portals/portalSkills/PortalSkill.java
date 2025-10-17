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
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.IPath;
import produsAdvanced.abstraction.portals.components.PortalComponent;

/**
 * Base class for portal skills, defines the projectile characteristics like speed, range, hitbox
 * and cooldown. Also defines the behaviour when hitting a wall and when the projectile is getting
 * created.
 */
public abstract class PortalSkill extends ProjectileSkill {

  /* Projectile characteristics */
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.2, 0.2);
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.2, 0.2);
  private static final long COOLDOWN = 500;

  /**
   * Creates a new portal skill.
   *
   * @param skillName Name of the skill.
   * @param texture Path of the texture for the skill.
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(String skillName, IPath texture, Tuple<Resource, Integer>... resourceCost) {
    super(
        skillName,
        COOLDOWN,
        texture,
        SPEED,
        RANGE,
        HIT_BOX_SIZE,
        HIT_BOX_OFFSET,
        false,
        resourceCost);
  }

  /**
   * Calculates the end position (target point) of the projectile.
   *
   * @param caster The entity that cast the projectile.
   * @return The endpoint of the projectile.
   */
  @Override
  protected Point end(Entity caster) {
    return SkillTools.cursorPositionAsPoint();
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
    Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));
    if (Game.tileAt(finalPos.toCoordinate()).isPresent()
        && Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
      createPortal(finalPos.toCoordinate().toPoint(), pc.position());
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
    vc.moveboxSize(Vector2.of(0.5, 0.5));
    vc.moveboxOffset(Vector2.of(0.25, 0.25));
    projectile.add(vc);
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
        new CollideComponent(
            Vector2.of(0, 0), Vector2.of(0, 0), onCollideEnter(caster), onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    cc.isSolid(false);
    projectile.add(cc);

    Game.add(projectile);
    onSpawn(caster, projectile);
  }

  /**
   * Method that has to be implemented in the actual skill where the corresponding portal is
   * created.
   *
   * @param portalPosition Position where the portal will be created
   * @param originalProjectilePosition original position of the projectile, needed for direction
   */
  protected abstract void createPortal(Point portalPosition, Point originalProjectilePosition);
}
