package entities;

import components.AntiMaterialBarrierComponent;
import components.LasergridComponent;
import contrib.components.*;
import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

/**
 * A utility class for building different miscellaneous entities in the game world of the advanced
 * dungeon.
 */
public class AdvancedFactory {

  private static final SimpleIPath LASER_GRID = new SimpleIPath("portal/laser_grid");
  private static final int LASER_GRID_DMG = 9999;
  private static final int LASER_GRID_CD = 10;

  private static final SimpleIPath ANTI_MATERIAL_BARRIER =
      new SimpleIPath("portal/anti_material_barrier");

  private static final SimpleIPath PORTAL_CUBE = new SimpleIPath("portal/portal_cube.png");
  private static final float cube_mass = 3f;
  private static final float cube_maxSpeed = 10f;

  private static final SimpleIPath PORTAL_SPHERE = new SimpleIPath("portal/kubus");
  private static final float sphere_mass = 3f;
  private static final float sphere_maxSpeed = 10f;

  /**
   * Creates a laser grid entity at the given position.
   *
   * <p>Objects can pass through it but the hero dies upon collision.
   *
   * @param spawnPoint The position the laser grid will be spawned.
   * @param horizontal whether the laser grid texture ist aligned horizontal or not (will be aligned
   *     vertical if false).
   * @return a new laser grid entity.
   */
  public static Entity laserGrid(Point spawnPoint, boolean horizontal) {
    Entity grid = new Entity("laserGrid");
    grid.add(new PositionComponent(spawnPoint));
    grid.add(new LasergridComponent(true));

    // the laser grid can't be solid to let objects pass
    CollideComponent colComp = new CollideComponent();
    colComp.isSolid(false);
    grid.add(colComp);

    grid.add(new SpikyComponent(LASER_GRID_DMG, DamageType.PHYSICAL, LASER_GRID_CD));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER_GRID);

    DrawComponent dc;

    if (horizontal) {
      State stHorizontalOn = State.fromMap(animationMap, "horizontal_on");
      State stHorizontalOff = State.fromMap(animationMap, "horizontal_off");
      StateMachine sm = new StateMachine(Arrays.asList(stHorizontalOn, stHorizontalOff));
      sm.addTransition(stHorizontalOn, "deactivate_laser_grid", stHorizontalOff);
      sm.addTransition(stHorizontalOff, "activate_laser_grid", stHorizontalOn);
      dc = new DrawComponent(sm);
    } else {
      State stVerticalOn = State.fromMap(animationMap, "vertical_on");
      State stVerticalOff = State.fromMap(animationMap, "vertical_off");
      StateMachine sm = new StateMachine(Arrays.asList(stVerticalOn, stVerticalOff));
      sm.addTransition(stVerticalOn, "deactivate_laser_grid", stVerticalOff);
      sm.addTransition(stVerticalOff, "activate_laser_grid", stVerticalOn);
      dc = new DrawComponent(sm);
    }

    grid.add(dc);

    return grid;
  }

  /**
   * Creates an anti-material barrier entity at the given position.
   *
   * <p>The hero can pass through it but all objects disintegrate upon collision.
   *
   * @param spawnPoint The position the barrier will be spawned.
   * @param horizontal whether the barrier texture ist aligned horizontal or not (will be aligned
   *     vertical if false).
   * @return a new anti-material barrier entity.
   */
  public static Entity antiMaterialBarrier(Point spawnPoint, boolean horizontal) {
    Entity barrier = new Entity("antiMaterialBarrier");
    barrier.add(new PositionComponent(spawnPoint));
    barrier.add(new AntiMaterialBarrierComponent(true));

    TriConsumer<Entity, Entity, Direction> action =
        (self, other, direction) -> {
          if (!other.name().equals("hero")) {
            Game.remove(other);
          }
        };

    // the barrier can't be solid to let the Hero pass
    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    colComp.isSolid(false);
    barrier.add(colComp);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(ANTI_MATERIAL_BARRIER);

    DrawComponent dc;

    if (horizontal) {
      State stHorizontalOn = State.fromMap(animationMap, "horizontal_on");
      State stHorizontalOff = State.fromMap(animationMap, "horizontal_off");
      StateMachine sm = new StateMachine(Arrays.asList(stHorizontalOn, stHorizontalOff));
      sm.addTransition(stHorizontalOn, "deactivate_anti_barrier", stHorizontalOff);
      sm.addTransition(stHorizontalOff, "activate_anti_barrier", stHorizontalOn);
      dc = new DrawComponent(sm);
    } else {
      State stVerticalOn = State.fromMap(animationMap, "vertical_on");
      State stVerticalOff = State.fromMap(animationMap, "vertical_off");
      StateMachine sm = new StateMachine(Arrays.asList(stVerticalOn, stVerticalOff));
      sm.addTransition(stVerticalOn, "deactivate_anti_barrier", stVerticalOff);
      sm.addTransition(stVerticalOff, "activate_anti_barrier", stVerticalOn);
      dc = new DrawComponent(sm);
    }

    barrier.add(dc);

    return barrier;
  }

  /**
   * Creates a portal cube entity at the given position.
   *
   * @param position The initial position of the portal cube.
   * @return A new portal cube entity.
   */
  public static Entity attachablePortalCube(Point position) {
    Entity portalCube = new Entity("attachablePortalCube");

    portalCube.add(new PositionComponent(position));
    portalCube.add(new VelocityComponent(cube_maxSpeed, cube_mass, entity -> {}, false));
    portalCube.add(new DrawComponent(new Animation(PORTAL_CUBE)));
    portalCube.add(new CollideComponent());

    final boolean[] attached = {false};

    portalCube.add(
        new InteractionComponent(
            2.0f,
            true,
            (interacted, interactor) -> {
              if (!attached[0]) {

                interactor
                    .fetch(VelocityComponent.class)
                    .ifPresent(
                        vc -> {
                          interacted.remove(VelocityComponent.class);
                          interacted.add(vc);
                          attached[0] = true;
                        });
              } else {
                interacted.remove(VelocityComponent.class);
                interacted.add(
                    new VelocityComponent(cube_maxSpeed, cube_mass, entity -> {}, false));
                attached[0] = false;
              }
            }));

    return portalCube;
  }

  /**
   * Creats a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @return the sphere entity
   */
  public static Entity moveableSphere(Point position) {
    Entity sphere = new Entity("moveableSphere");

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(PORTAL_SPHERE);

    State stIdle = new State("idle", animationMap.get("idle"));
    State stMove = new State("move", animationMap.get("move"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));

    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "idle", stIdle);

    sphere.add(new DrawComponent(sm));
    sphere.add(new PositionComponent(position));
    sphere.add(new VelocityComponent(sphere_maxSpeed, sphere_mass, entity -> {}, false));
    sphere.add(
        new CollideComponent(
            CollideComponent.DEFAULT_OFFSET,
            CollideComponent.DEFAULT_SIZE,
            ((self, other, direction) -> {
              other
                  .fetch(PlayerComponent.class)
                  .ifPresent(
                      player -> {
                        VelocityComponent vc = self.fetch(VelocityComponent.class).get();
                        VelocityComponent otherVc = other.fetch(VelocityComponent.class).get();
                        vc.currentVelocity(otherVc.currentVelocity());
                      });
            }),
            CollideComponent.DEFAULT_COLLIDER));

    return sphere;
  }
}
