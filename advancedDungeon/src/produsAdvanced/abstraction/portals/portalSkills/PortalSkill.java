package produsAdvanced.abstraction.portals.portalSkills;

<<<<<<< HEAD
<<<<<<< HEAD
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
<<<<<<< HEAD
<<<<<<< HEAD
import core.components.DrawComponent;
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import core.components.DrawComponent;
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.IPath;
import produsAdvanced.abstraction.portals.components.PortalComponent;

public abstract class PortalSkill extends ProjectileSkill {

  /** Name of the Skill. */
<<<<<<< HEAD
<<<<<<< HEAD
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.2, 0.2);
<<<<<<< HEAD
=======
  public static final String SKILL_NAME = "BLUE_PORTAL";
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
  private static final float SPEED = 13f;
  private static final float RANGE = 10f;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(0.2, 0.2);
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
  private static final Vector2 HIT_BOX_OFFSET = Vector2.of(0.2, 0.2);
>>>>>>> fe1cebb8 (updated onWallHit method)
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
<<<<<<< HEAD
  protected Consumer<Entity> onWallHit(Entity caster) {
    return entity ->  {
      PositionComponent pc = entity.fetch(PositionComponent.class).get();
      VelocityComponent vc = entity.fetch(VelocityComponent.class).get();
<<<<<<< HEAD
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/PortalSkill.java
=======
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/portalSkills/PortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
<<<<<<< HEAD
      Point point = new Point(Math.round(vc.currentVelocity().normalize().x()),Math.round(vc.currentVelocity().normalize().y()));
      Coordinate cords = pc.coordinate().translate(Vector2.of(point));
<<<<<<< HEAD
<<<<<<< HEAD
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f));
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
>>>>>>> d483f6ff (added direction to portals)
=======
//      System.out.println("Base Pos: " + pc.position());
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/PortalSkill.java
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
      createPortal(new Point(cords.toCenteredPoint().x(), cords.toCenteredPoint().y()-0.25f), vc.currentVelocity().normalize(), pc.position());
>>>>>>> 878b072b (added direction to portals)
=======
//      System.out.println("Base Pos: " + pc.position());
========
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/portalSkills/PortalSkill.java
>>>>>>> ac8cf0c7 (restructed portal related files)
      Vector2 velocity = vc.currentVelocity().normalize();
      Point movedPos = pc.position().translate(velocity);
      Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));

      if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
<<<<<<< HEAD
<<<<<<< HEAD
        createPortal(finalPos.toCoordinate().toPoint(), vc.currentVelocity());
      }
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
        createPortal(finalPos.toCoordinate().toPoint());
=======
        createPortal(finalPos.toCoordinate().toPoint(), vc.currentVelocity());
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
      }
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
>>>>>>> ac8cf0c7 (restructed portal related files)
      Game.remove(entity);
    };
=======
  protected void onWallHit(Entity caster, Entity projectile) {
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();
    Vector2 velocity = vc.currentVelocity().normalize();
    Point movedPos = pc.position().translate(velocity);
    Point finalPos = new Point(Math.round(movedPos.x()), Math.round(movedPos.y()));

    if (Game.tileAt(finalPos.toCoordinate()).get().levelElement() == LevelElement.PORTAL) {
      createPortal(finalPos.toCoordinate().toPoint());
    }
    Game.remove(projectile);
>>>>>>> fe1cebb8 (updated onWallHit method)
  }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
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

<<<<<<< HEAD
<<<<<<< HEAD
  protected abstract void createPortal(Point position, Vector2 currentVelocity);
<<<<<<< HEAD
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
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
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
  protected abstract void   createPortal(Point position, Vector2 currentVelocity);
>>>>>>> deaec5ba (fixed random teleport bug + adjusted for new collidesystem)
=======
  protected abstract void   createPortal(Point position);
>>>>>>> c71f73d8 (decluttered portal create method)



}

