package portal.laserGrid;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

public class LaserGrid {
  private static final SimpleIPath LASER_GRID = new SimpleIPath("portal/laser_grid");
  private static final int LASER_GRID_DMG = 9999;
  private static final int LASER_GRID_CD = 10;

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
}
