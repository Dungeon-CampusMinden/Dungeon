package produsAdvanced.abstraction.portals.portalSkills;

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

import java.lang.reflect.GenericArrayType;
import java.util.function.Consumer;

public abstract class PortalSkill extends ProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final long COOLDOWN = 500;

  /**
   * Creates a new projectile skill.
   *
   * @param resourceCost Resource costs for casting.
   */
  public PortalSkill(String skillName, IPath texture, Tuple<Resource, Integer>... resourceCost) {
    super(skillName, COOLDOWN, texture, SPEED, RANGE, HIT_BOX_SIZE, resourceCost);
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
      Vector2 velocity = vc.currentVelocity().normalize();
      Point movedPos = pc.position().translate(velocity);
      Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));

      if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
        createPortal(finalPos.toCoordinate().toPoint());
      }
      Game.remove(entity);
    };
  }

  protected abstract void createPortal(Point position);



}

