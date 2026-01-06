package portal.antiMaterialBarrier;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
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
import portal.portals.PortalFactory;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.TractorBeamComponent;

public class AntiMaterialBarrier {
  private static final SimpleIPath ANTI_MATERIAL_BARRIER =
      new SimpleIPath("portal/anti_material_barrier");

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

    CollideComponent colComp = getCollideComponent();
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
   * Creates the CollideComponent for the AntiMaterialBarrier.
   *
   * @return the new CollideComponent.
   */
  public static CollideComponent getCollideComponent() {
    TriConsumer<Entity, Entity, Direction> action =
        (self, other, direction) -> {
          String otherEntityName = other.name();
          if (other.isPresent(PlayerComponent.class)) {
            PortalFactory.clearAllPortals();
          } else if (other.isPresent(TractorBeamComponent.class)
              || other.isPresent(PortalExtendComponent.class)
              || otherEntityName.contains("energyPelletLauncher")) {
            // do nothing
          } else {
            Game.remove(other);
          }
        };

    // the barrier can't be solid to let the Hero pass
    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    colComp.isSolid(false);
    return colComp;
  }
}
