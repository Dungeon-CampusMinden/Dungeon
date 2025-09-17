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
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/PortalSkill.java
<<<<<<< HEAD
      Point point = new Point(Math.round(vc.currentVelocity().normalize().x()),Math.round(vc.currentVelocity().normalize().y()));
      Coordinate cords = pc.coordinate().translate(Vector2.of(point));
<<<<<<< HEAD
<<<<<<< HEAD
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f));
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
>>>>>>> d483f6ff (added direction to portals)
=======
//      System.out.println("Base Pos: " + pc.position());
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/PortalSkill.java
      Vector2 velocity = vc.currentVelocity().normalize();
      Point movedPos = pc.position().translate(velocity);
      Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));

      if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
        createPortal(finalPos.toCoordinate().toPoint());
      }
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
      Game.remove(entity);
    };
  }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  protected abstract void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition);
=======
  protected abstract void createPortal(Point position);
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  protected abstract void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition);
>>>>>>> d483f6ff (added direction to portals)
=======
  protected abstract void createPortal(Point position);
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)



}

