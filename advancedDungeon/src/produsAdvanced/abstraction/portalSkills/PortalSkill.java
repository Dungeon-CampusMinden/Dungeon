package produsAdvanced.abstraction.portalSkills;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

import java.util.function.Consumer;

public abstract class PortalSkill extends ProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final float SPEED = 22f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final long COOLDOWN = 500;

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(IPath texture, Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, COOLDOWN, texture, SPEED, RANGE, HIT_BOX_SIZE, resourceCost);
  }

  @Override
  protected Point end(Entity caster) {
    return SkillTools.cursorPositionAsPoint();
  }

  @Override
  protected Consumer<Entity> onWallHit(Entity caster) {
    return entity ->  {
      PositionComponent pc = entity.fetch(PositionComponent.class).get();
      VelocityComponent vc = entity.fetch(VelocityComponent.class).get();
      Point point = new Point(Math.round(vc.currentVelocity().normalize().x()),Math.round(vc.currentVelocity().normalize().y()));
      Coordinate cords = pc.coordinate().translate(Vector2.of(point));
<<<<<<< HEAD
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f));
>>>>>>> ef71cb29 (added green and blue portal variants)
      Game.remove(entity);
    };
  }

<<<<<<< HEAD
  protected abstract void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition);
=======
  protected abstract void createPortal(Point position);
>>>>>>> ef71cb29 (added green and blue portal variants)



}

