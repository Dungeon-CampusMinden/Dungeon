package contrib.entities;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.systems.VelocitySystem;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

public class NPCFactory {

  public static Entity createNPC(Point pos, String path) {
    Entity npc = new Entity("NPC");
    npc.add(new PositionComponent(pos));

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(new SimpleIPath(path));
    State stIdle = new DirectionalState(StateMachine.IDLE_STATE, animationMap);
    State stMove = new DirectionalState(VelocitySystem.STATE_NAME, animationMap, "run");

    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));
    sm.addTransition(stIdle, VelocitySystem.MOVE_SIGNAL, stMove);
    sm.addTransition(stIdle, VelocitySystem.IDLE_SIGNAL, stIdle);
    sm.addTransition(stMove, VelocitySystem.MOVE_SIGNAL, stMove);
    sm.addTransition(stMove, VelocitySystem.IDLE_SIGNAL, stIdle);
    DrawComponent dc = new DrawComponent(sm);
    dc.depth(DepthLayer.Player.depth());
    npc.add(dc);

    CollideComponent cc = new CollideComponent(Vector2.of(0.5f, 0), Vector2.of(0.9f, 0.9f));
    cc.isSolid(true);
    npc.add(cc);

    npc.add(new VelocityComponent(0));
    return npc;
  }
}
