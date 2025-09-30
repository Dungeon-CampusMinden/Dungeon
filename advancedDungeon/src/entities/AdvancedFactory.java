package entities;

import components.AntiMaterialBarrierComponent;
import components.LasergridComponent;
import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
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

    // the laser grid cant be solid to let objects pass
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

    // the barrier cant be solid to let the Hero pass
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
}
