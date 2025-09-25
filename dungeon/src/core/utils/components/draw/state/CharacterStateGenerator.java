package core.utils.components.draw.state;

import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import java.util.List;
import java.util.Map;

public class CharacterStateGenerator {

  public static StateMachine createStateMachine(IPath path) {
    return createStateMachine(path, 2.0f);
  }

  public static StateMachine createStateMachine(IPath path, float runSpeedupFactor) {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(path);
    if (animationMap == null || animationMap.isEmpty()) {
      throw new IllegalArgumentException(
          "Failed to load animation map or map was empty for path: " + path.pathString());
    }

    // Cases:
    // only idle -> use for all directions, speed up run animation slightly
    // idle + run -> use for all directions
    // idle_dir -> use for specified directions, speed up run animation slightly
    // idle_<dir> + run_<dir> -> use for specified directions

    State stIdle = null;
    State stRun = null;

    if (animationMap.containsKey("idle") && !animationMap.containsKey("run")) {
      Animation idle = animationMap.get("idle");
      Animation run = idle.clone();
      run.getConfig().framesPerSprite((int) (run.getConfig().framesPerSprite() / runSpeedupFactor));
      stIdle = new SimpleDirectionalState("idle", idle);
      stRun = new SimpleDirectionalState("run", run);

    } else if (animationMap.containsKey("idle") && animationMap.containsKey("run")) {
      Animation idle = animationMap.get("idle");
      Animation run = animationMap.get("run");
      stIdle = new SimpleDirectionalState("idle", idle);
      stRun = new SimpleDirectionalState("run", run);

    } else if (animationMap.containsKey("idle_down") && !animationMap.containsKey("run_down")) {
      Animation idleDown = animationMap.get("idle_down");
      Animation idleLeft = animationMap.get("idle_left");
      Animation idleUp = animationMap.get("idle_up");
      Animation idleRight = animationMap.get("idle_right");
      stIdle = new DirectionalState("idle", idleDown, idleLeft, idleUp, idleRight);

      Animation runDown = idleDown.clone();
      Animation runLeft = idleLeft.clone();
      Animation runUp = idleUp.clone();
      Animation runRight = idleRight.clone();

      runDown
          .getConfig()
          .framesPerSprite((int) (runDown.getConfig().framesPerSprite() / runSpeedupFactor));
      runLeft
          .getConfig()
          .framesPerSprite((int) (runLeft.getConfig().framesPerSprite() / runSpeedupFactor));
      runUp
          .getConfig()
          .framesPerSprite((int) (runUp.getConfig().framesPerSprite() / runSpeedupFactor));
      runRight
          .getConfig()
          .framesPerSprite((int) (runRight.getConfig().framesPerSprite() / runSpeedupFactor));

      stRun = new DirectionalState("run", runDown, runLeft, runUp, runRight);

    } else if (animationMap.containsKey("idle_down") && animationMap.containsKey("run_down")) {
      Animation idleDown = animationMap.get("idle_down");
      Animation idleLeft = animationMap.get("idle_left");
      Animation idleUp = animationMap.get("idle_up");
      Animation idleRight = animationMap.get("idle_right");
      stIdle = new DirectionalState("idle", idleDown, idleLeft, idleUp, idleRight);

      Animation runDown = animationMap.get("run_down");
      Animation runLeft = animationMap.get("run_left");
      Animation runUp = animationMap.get("run_up");
      Animation runRight = animationMap.get("run_right");
      stRun = new DirectionalState("run", runDown, runLeft, runUp, runRight);

    } else {
      throw new IllegalArgumentException(
          "Failed to find suitable animations in map for path: " + path.pathString());
    }

    return createStateMachine(stIdle, stRun);
  }

  private static StateMachine createStateMachine(State stIdle, State stRun) {
    StateMachine sm = new StateMachine(List.of(stIdle, stRun));
    sm.addTransition(stIdle, "move", stRun);
    sm.addTransition(stRun, "idle", stIdle);
    return sm;
  }
}
