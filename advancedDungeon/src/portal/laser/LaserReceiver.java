package portal.laser;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import portal.util.ToggleableComponent;

/** Class to create the LaserReceiver Entity. */
public class LaserReceiver {

  private static final SimpleIPath LASER_RECEIVER = new SimpleIPath("portal/laser_receiver");

  /**
   * Creates a laser receiver entity at the given position.
   *
   * @param position The initial position of the laser receiver.
   * @return A new laser receiver entity.
   */
  public static Entity laserReceiver(Point position) {
    Entity receiver = new Entity("laserReceiver");

    PositionComponent pc = new PositionComponent(position);
    receiver.add(pc);
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER_RECEIVER);

    State inactive = new State("inactive", animationMap.get("inactive"));
    State active = new State("active", animationMap.get("active"));
    StateMachine sm = new StateMachine(Arrays.asList(active, inactive));

    sm.addTransition(inactive, "active", active);
    sm.addTransition(active, "inactive", inactive);

    DrawComponent dc = new DrawComponent(sm);
    dc.depth(DepthLayer.ForegroundDeco.depth());
    dc.sendSignal("inactive");
    receiver.add(dc);

    receiver.add(new ToggleableComponent(false));

    TriConsumer<Entity, Entity, Direction> actionEnter =
        (you, other, collisionDir) -> {
          other
              .fetch(LaserComponent.class)
              .ifPresent(
                  lc -> {
                    dc.sendSignal("active");
                    you.fetch(ToggleableComponent.class).get().activate();
                  });
        };

    TriConsumer<Entity, Entity, Direction> actionLeave =
        (you, other, collisionDir) -> {
          other
              .fetch(LaserComponent.class)
              .ifPresent(
                  lc -> {
                    dc.sendSignal("inactive");
                    you.fetch(ToggleableComponent.class).get().deactivate();
                  });
        };

    receiver.add(
        new CollideComponent(Vector2.of(0f, 0f), Vector2.of(1f, 1f), actionEnter, actionLeave));

    return receiver;
  }
}
