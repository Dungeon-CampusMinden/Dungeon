package core.utils.components.draw.state;

import core.systems.VelocitySystem;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating StateMachine instances for characters with idle and run states based
 * on available animations.
 */
public class CharacterStateFactory {

  /**
   * Creates a StateMachine for a character with idle and run states based on the available
   * animations in the specified spritesheet.
   *
   * <p>Uses a default run speedup factor of 2.0 if only idle animations are available.
   *
   * <p>Generates:
   *
   * <ul>
   *   <li>Only 1 animation: idle + a sped-up version for run. Animation will be taken for RIGHT and mirrored for LEFT.
   *   <li>2 animations: idle + run. Animation will be taken for RIGHT and mirrored.
   *   <li>4 animations: idle_down, idle_left, idle_up, idle_right + a sped-up version for run.
   *   <li>8 animations: idle_down, idle_left, idle_up, idle_right + run_down, run_left, run_up, run_right.
   * </ul>
   *
   * @param path The path to the spritesheet containing the animations.
   * @return A StateMachine configured with idle and run states.
   * @throws IllegalArgumentException if no suitable animations are found in the spritesheet.
   */
  public static StateMachine createStateMachine(IPath path) {
    return createStateMachine(path, 2.0f);
  }

  /**
   * Creates a StateMachine for a character with idle and run states based on the available
   * animations in the specified spritesheet.
   *
   * <p>Generates:
   *
   * <ul>
   *   <li>Only 1 animation: idle + a sped-up version for run. Animation will be taken for RIGHT and mirrored for LEFT.
   *   <li>2 animations: idle + run. Animation will be taken for RIGHT and mirrored.
   *   <li>4 animations: idle_down, idle_left, idle_up, idle_right + a sped-up version for run.
   *   <li>8 animations: idle_down, idle_left, idle_up, idle_right + run_down, run_left, run_up, run_right.
   * </ul>
   *
   * @param path The path to the spritesheet containing the animations.
   * @param runSpeedupFactor The factor by which to speed up the run animation if only idle
   *     animations are available.
   * @return A StateMachine configured with idle and run states.
   * @throws IllegalArgumentException if no suitable animations are found in the spritesheet.
   */
  public static StateMachine createStateMachine(IPath path, float runSpeedupFactor) {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(path);
    if (animationMap == null || animationMap.isEmpty()) {
      throw new IllegalArgumentException(
          "Failed to load animation map or map was empty for path: " + path.pathString());
    }

    State stIdle, stRun;

    if (animationMap.containsKey("idle") && !animationMap.containsKey("run")) {
      Animation idle = animationMap.get("idle");
      Animation run = idle.clone();
      run.getConfig().framesPerSprite((int) (run.getConfig().framesPerSprite() / runSpeedupFactor));
      stIdle = new SimpleDirectionalState(StateMachine.IDLE_STATE, idle);
      stRun = new SimpleDirectionalState(VelocitySystem.STATE_NAME, run);

    } else if (animationMap.containsKey("idle") && animationMap.containsKey("run")) {
      Animation idle = animationMap.get("idle");
      Animation run = animationMap.get("run");
      stIdle = new SimpleDirectionalState(StateMachine.IDLE_STATE, idle);
      stRun = new SimpleDirectionalState(VelocitySystem.STATE_NAME, run);

    } else if (animationMap.containsKey("idle_down") && !animationMap.containsKey("run_down")) {
      Animation idleDown = animationMap.get("idle_down");
      Animation idleLeft = animationMap.get("idle_left");
      Animation idleUp = animationMap.get("idle_up");
      Animation idleRight = animationMap.get("idle_right");
      stIdle = new DirectionalState(StateMachine.IDLE_STATE, idleDown, idleLeft, idleUp, idleRight);

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

      stRun = new DirectionalState(VelocitySystem.STATE_NAME, runDown, runLeft, runUp, runRight);

    } else if (animationMap.containsKey("idle_down") && animationMap.containsKey("run_down")) {
      Animation idleDown = animationMap.get("idle_down");
      Animation idleLeft = animationMap.get("idle_left");
      Animation idleUp = animationMap.get("idle_up");
      Animation idleRight = animationMap.get("idle_right");
      stIdle = new DirectionalState(StateMachine.IDLE_STATE, idleDown, idleLeft, idleUp, idleRight);

      Animation runDown = animationMap.get("run_down");
      Animation runLeft = animationMap.get("run_left");
      Animation runUp = animationMap.get("run_up");
      Animation runRight = animationMap.get("run_right");
      stRun = new DirectionalState(VelocitySystem.STATE_NAME, runDown, runLeft, runUp, runRight);

    } else {
      throw new IllegalArgumentException(
          "Failed to find suitable animations in map for path: " + path.pathString());
    }

    return createStateMachine(stIdle, stRun);
  }

  private static StateMachine createStateMachine(State stIdle, State stRun) {
    StateMachine sm = new StateMachine(List.of(stIdle, stRun));
    sm.addTransition(stIdle, VelocitySystem.MOVE_SIGNAL, stRun);
    sm.addTransition(stRun, VelocitySystem.IDLE_SIGNAL, stIdle);
    sm.addTransition(stRun, VelocitySystem.MOVE_SIGNAL, stRun);
    return sm;
  }
}
