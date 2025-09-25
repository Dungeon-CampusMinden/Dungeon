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
import produsAdvanced.abstraction.portals.PortalComponent;

import java.util.function.Consumer;

public abstract class PortalSkill extends ProjectileSkill {

  /** Name of the Skill. */
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.2, 0.2);
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.2, 0.2);
  private static final long COOLDOWN = 500;

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(String skillName, IPath texture, Tuple<Resource, Integer>... resourceCost) {
    super(skillName, COOLDOWN, texture, SPEED, RANGE, HIT_BOX_SIZE,HIT_BOX_OFFSET, false, resourceCost);
  }

  @Override
  protected Point end(Entity caster) {
    return SkillTools.cursorPositionAsPoint();
  }

  @Override
  protected void onWallHit(Entity caster, Entity projectile) {
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();
    Vector2 velocity = vc.currentVelocity().normalize();
    Point movedPos = pc.position().translate(velocity);
    Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));

    if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
      createPortal(finalPos.toCoordinate().toPoint(), vc.currentVelocity());
    }
    Game.remove(projectile);
  }

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
    vc.moveboxSize(Vector2.of(0.5,0.5));
    vc.moveboxOffset(Vector2.of(0.25,0.25));
    projectile.add(vc);
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
      new CollideComponent(
        Vector2.of(0,0),
        Vector2.of(0,0),
        onCollideEnter(caster),
        onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    projectile.add(cc);


    Game.add(projectile);
    onSpawn(caster, projectile);
  }

  protected abstract void   createPortal(Point position, Vector2 currentVelocity);



}

