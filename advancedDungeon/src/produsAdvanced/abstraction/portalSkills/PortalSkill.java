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
//      System.out.println("-------------------------------");
      PositionComponent pc = entity.fetch(PositionComponent.class).get();
      VelocityComponent vc = entity.fetch(VelocityComponent.class).get();
<<<<<<< HEAD
      Point point = new Point(Math.round(vc.currentVelocity().normalize().x()),Math.round(vc.currentVelocity().normalize().y()));
      Coordinate cords = pc.coordinate().translate(Vector2.of(point));
<<<<<<< HEAD
<<<<<<< HEAD
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f));
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
>>>>>>> 878b072b (added direction to portals)
=======
//      System.out.println("Base Pos: " + pc.position());
      Vector2 velocity = vc.currentVelocity().normalize();
//      System.out.println("Velocity: " + velocity);
      Point movedPos = pc.position().translate(velocity);
//      System.out.println("Edited Pos: " + movedPos);
      Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));
//      System.out.println("Final Pos: " + finalPos);

      if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
        createPortal(finalPos.toCoordinate().toPoint());
      }
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
      Game.remove(entity);
    };
  }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  protected abstract void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition);
=======
  protected abstract void createPortal(Point position);
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
  protected abstract void createPortal(Point position, Vector2 currentVelocity, Point projectilePosition);
>>>>>>> 878b072b (added direction to portals)
=======
  protected abstract void createPortal(Point position);
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)



}

