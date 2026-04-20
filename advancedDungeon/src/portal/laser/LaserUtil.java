package portal.laser;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.collide.Hitbox;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PortalTile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.util.Set;
import portal.portals.components.PortalExtendComponent;

/** Util class for everything laser related. */
public class LaserUtil {

  private static final SimpleIPath EMITTER_ACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_active.png");
  private static final SimpleIPath EMITTER_INACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");

  /**
   * Sets the given emitter to active so it extends its laser.
   *
   * @param emitter emitter that should be activated.
   */
  public static void activate(Entity emitter) {
    LaserComponent laserComponent = emitter.fetch(LaserComponent.class).get();
    if (laserComponent.isActive()) {
      return;
    }
    laserComponent.setActive(true);
    PositionComponent pc = emitter.fetch(PositionComponent.class).get();
    Direction dir = pc.viewDirection();
    LaserPartComponent laserPart = new LaserPartComponent(dir);
    emitter.add(laserPart);

    Point currentPoint = pc.position();
    Tile currentTile = Game.tileAt(pc.position()).orElse(null);
    int totalPoints = 0;
    while (currentTile != null
      && !(currentTile instanceof WallTile)
      && !(currentTile instanceof PortalTile)
      && !Game.entityAtTile(currentTile)
      .anyMatch(entity -> entity.name().startsWith("laserCube"))) {
      Entity segment = LaserFactory.createSegment(currentPoint,  dir);
      segment.add(laserComponent);
      segment.add(laserPart);
      Game.add(segment);
      totalPoints++;
      currentPoint = currentPoint.translate(dir);
      currentTile = Game.tileAt(currentPoint).orElse(null);
    }
    updateEmitterVisual(emitter, true);
    configureEmitterHitbox(emitter, totalPoints, dir);
  }

  /**
   * Deactivates the given emitter so that it retracts its laser.
   *
   * @param emitter emitter that should be deactivated.
   */
  public static void deactivate(Entity emitter) {
    LaserComponent laserComponent = emitter.fetch(LaserComponent.class).get();
    laserComponent.setBeingDeactivated(true);
    if (!laserComponent.isActive()) {
      return;
    }
    laserComponent.setActive(false);
    Game.levelEntities(Set.of(LaserComponent.class))
        .filter(entity -> entity.fetch(LaserComponent.class).isPresent() && entity.fetch(LaserComponent.class).get().equals(laserComponent))
        .filter(entity -> entity.fetch(LaserEmitterComponent.class).isEmpty())
        .filter(entity -> entity.fetch(LaserCubeComponent.class).isEmpty())
        .forEach(Game::remove);

    updateEmitterVisual(emitter, false);
    emitter.remove(SpikyComponent.class);
    emitter.remove(CollideComponent.class);
    laserComponent.setBeingDeactivated(false);
  }

  /**
   * Extends the laser on a new surface.
   *
   * @param direction direction of the laser.
   * @param from starting point of the laser.
   * @param pec needed so the new laser also retracts when the original laser retracts.
   * @param comp needed so the new laser can also extend and retract.
   */
  public static void extendLaser(
      Direction direction, Point from, PortalExtendComponent pec, LaserComponent comp) {

    Entity newEmitter = LaserFactory.createEmitter(from, direction);
    newEmitter.add(comp);
    newEmitter.add(pec);
    newEmitter.remove(DrawComponent.class);
    PositionComponent pc = newEmitter.fetch(PositionComponent.class).get();
    Direction dir = pc.viewDirection();

    LaserPartComponent laserPart = new LaserPartComponent(dir);
    newEmitter.add(laserPart);

    Point currentPoint = pc.position();
    Tile currentTile = Game.tileAt(pc.position()).orElse(null);
    int totalPoints = 0;
    while (currentTile != null
      && !(currentTile instanceof WallTile)
      && !(currentTile instanceof PortalTile)
      && !Game.entityAtTile(currentTile)
      .anyMatch(entity -> entity.name().startsWith("laserCube"))) {
        Entity segment = LaserFactory.createSegment(currentPoint,  dir);
        segment.add(comp);
        segment.add(laserPart);
        Game.add(segment);
        currentPoint = currentPoint.translate(dir);
        currentTile = Game.tileAt(currentPoint).orElse(null);
        totalPoints++;
    }

    configureEmitterHitbox(newEmitter, totalPoints - 1, direction);
    Game.add(newEmitter);
  }

  /**
   * Logically trims the laser by calling the deactivate and then the active methods so it acts as
   * "trimming".
   *
   * @param emitter the original emitter of the laser
   */
  public static void trimLaser(Entity emitter) {
    LaserComponent laserComponent = emitter.fetch(LaserComponent.class).get();
    if (!laserComponent.isBeingDeactivated()) {
      laserComponent.setBeingDeactivated(true);
      Entity originalEmitter = Game.levelEntities(Set.of(LaserComponent.class))
        .filter(entity -> entity.fetch(LaserComponent.class).get().equals(laserComponent))
        .filter(entity -> entity.fetch(LaserEmitterComponent.class).isPresent())
        .findFirst().get();
      LaserUtil.setLaserToReactivate(originalEmitter);
      laserComponent.setBeingDeactivated(false);
    }
  }

  public static void extendTimes(int times, Direction direction, Point from, PortalExtendComponent pec, LaserComponent comp) {
    Entity newEmitter = LaserFactory.createEmitter(from, direction);
    newEmitter.add(comp);
    newEmitter.add(pec);
    newEmitter.remove(DrawComponent.class);
    PositionComponent pc = newEmitter.fetch(PositionComponent.class).get();
    Direction dir = pc.viewDirection();

    LaserPartComponent laserPart = new LaserPartComponent(dir);
    newEmitter.add(laserPart);

    Point currentPoint = pc.position();
    Tile currentTile = Game.tileAt(pc.position()).orElse(null);
    int totalPoints = 0;
    for (int i = 0; i < times; i++) {
      Entity segment = LaserFactory.createSegment(currentPoint,  dir);
      segment.add(comp);
      segment.add(laserPart);
      Game.add(segment);
      currentPoint = currentPoint.translate(dir);
      currentTile = Game.tileAt(currentPoint).orElse(null);
      totalPoints++;
    }

    configureEmitterHitbox(newEmitter, totalPoints - 1, direction);
    Game.add(newEmitter);

  }

  public static void clearLaserPart(Entity laserPartEmitter) {
    LaserComponent laserComponent = laserPartEmitter.fetch(LaserComponent.class).get();
    LaserPartComponent laserPartComponent = laserPartEmitter.fetch(LaserPartComponent.class).get();
    PortalExtendComponent pec = laserPartEmitter.fetch(PortalExtendComponent.class).get();
    laserComponent.setBeingDeactivated(true);
    Game.levelEntities(Set.of(LaserPartComponent.class))
      .filter(entity -> entity.fetch(LaserPartComponent.class).get().equals(laserPartComponent))
      .filter(entity -> entity.fetch(LaserEmitterComponent.class).isEmpty())
      .filter(entity -> entity.fetch(LaserCubeComponent.class).isEmpty())
      .forEach(Game::remove);
    pec.setExtended(true);
    laserComponent.setBeingDeactivated(false);
  }


  public static void setLaserToActivate(Entity laser) {
    laser.fetch(LaserComponent.class).ifPresent(laserComponent -> {
      if (laserComponent.getCurrentStatus() ==LaserStatus.NONE) {
        laserComponent.setCurrentStatus(LaserStatus.ACTIVATE);
      }
    });
  }

  public static void setLaserToDeactivate(Entity laser) {
    laser.fetch(LaserComponent.class).ifPresent(laserComponent -> {
      if (laserComponent.getCurrentStatus() ==LaserStatus.NONE) {
        laserComponent.setCurrentStatus(LaserStatus.DEACTIVATE);
      }
    });
  }

  public static void setLaserToReactivate(Entity laser) {
    laser.fetch(LaserComponent.class).ifPresent(laserComponent -> {
      if (laserComponent.getCurrentStatus() == LaserStatus.NONE && laserComponent.isActive()) {
        laserComponent.setCurrentStatus(LaserStatus.REACTIVATE);
      }
    });
  }

  public static void setEnterCube(Entity cube, Entity laser) {
    cube.fetch(LaserCubeComponent.class).ifPresent(laserCubeComponent -> {
      if (laserCubeComponent.getCurrentStatus() == LaserCubeStatus.NONE && !laserCubeComponent.isActive()) {
        laserCubeComponent.setOnEnterCube(cube);
        laserCubeComponent.setOnEnterLaser(laser);
        laserCubeComponent.setCurrentStatus(LaserCubeStatus.ENTER_CUBE);
      }
    });
  }

  public static void setCubeLeave(Entity cube, Entity laser) {
    cube.fetch(LaserCubeComponent.class).ifPresent(laserCubeComponent -> {
      if (laserCubeComponent.getCurrentStatus() == LaserCubeStatus.NONE && laserCubeComponent.isActive()) {
        laserCubeComponent.setOnLeaveCube(cube);
        laserCubeComponent.setOnLeaveLaser(laser);
        laserCubeComponent.setCurrentStatus(LaserCubeStatus.LEAVE_CUBE);
      }
    });
  }

  /**
   * Sets the hitbox of the CollideComponent so it fits the extended laser.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param totalPoints how many tiles the laser is covering.
   * @param dir direction the laser is extending to.
   */
  public static void configureEmitterHitbox(Entity emitter, int totalPoints, Direction dir) {
    float hitboxX = 1f;
    float hitboxY = 1f;
    float offsetX = 0.375f;
    float offsetY = 0.375f;

    switch (dir) {
      case LEFT -> {
        hitboxX = totalPoints + 1;
        hitboxY = 0.25f;
        offsetX = (-totalPoints);
      }
      case RIGHT -> {
        hitboxX = totalPoints + 1;
        hitboxY = 0.25f;
        offsetX = 0;
      }
      case UP -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints + 1;
        offsetY = 0;
      }
      case DOWN -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints + 1;
        offsetY = (-totalPoints);
      }
      default -> {}
    }
    emitter.add(new SpikyComponent(9999, DamageType.PHYSICAL, 10));
    Hitbox newCollider = new Hitbox(Vector2.of(hitboxX, hitboxY), Vector2.of(offsetX, offsetY));

    CollideComponent cc = new CollideComponent();
    cc.collider(newCollider);
    cc.staticCallback((a) -> false);
    cc.isSolid(false);
    emitter.add(cc);
  }

  /**
   * Updates an emitters visual and name to active or inactive.
   *
   * @param emitter the emitter entity that is getting updated.
   * @param on true if the laser is active, otherwise false.
   */
  private static void updateEmitterVisual(Entity emitter, boolean on) {
    DrawComponent dc = new DrawComponent(on ? EMITTER_ACTIVE : EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
  }
}
