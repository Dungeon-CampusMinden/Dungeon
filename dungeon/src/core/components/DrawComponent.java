package core.components;

import com.badlogic.gdx.graphics.g2d.Sprite;
import core.Component;
import core.utils.components.draw.*;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.draw.state.Signal;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Store all {@link Animation}s for an entity.
 *
 * <p>At creation, the component will read in each subdirectory in the given path and create an
 * animation for each subdirectory.
 *
 * <p>Each Animation will be created with default settings. If you want to change these settings,
 * use the methods from {@link Animation}.
 *
 * <p>The {@link core.systems.DrawSystem} uses a Priority-based queue. Use {@link
 * #queueAnimation(IPath...)} or {@link #queueAnimation(int, IPath...)} to add an animation to the
 * queue. The {@link core.systems.DrawSystem} will always show the animation with the highest
 * priority in the queue.
 *
 * <p>Use {@link #currentAnimation} to get the current active animation or use {@link #animation} to
 * get a specific animation.
 *
 * <p>Use {@link #hasAnimation} to check if the component has the desired animation.
 *
 * <p>If you want to add your own Animations, create a subdirectory for the animation and add the
 * path to an enum that implements the {@link IPath} interface. So if you want to add a jump
 * animation to the hero, just create a new directory "jump" in the asset directory of your hero
 * (for example character/hero) and then add a new Enum-Value JUMP("jump") to the enum that
 * implements {@link IPath}.
 *
 * <p>Animations will be searched in the default asset directory. Normally, this is "game/assets",
 * but you can change it in the "gradle.build" file if you like.
 *
 * <p>Note: Each entity needs at least a {@link CoreAnimations#IDLE} Animation.
 *
 * @see Animation
 * @see IPath
 */
public final class DrawComponent implements Component {
  private final Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());

  private final StateMachine stateMachine;
  private int depth = DepthLayer.Normal.depth();

  private int tintColor = -1; // -1 means no tinting
  private boolean isVisible = true;

  /**
   * Create a new DrawComponent.
   *
   * <p>Will read in all subdirectories of the given path and use each file in the subdirectory to
   * create an animation. So each subdirectory should contain only the files for one animation.
   *
   * <p>Animations should not be set directly via {@link #currentAnimation()} but rather be queued
   * via {@link #queueAnimation(IPath...)} or {@link #queueAnimation(int, IPath...)}.
   *
   * <p>Will set the current animation to either idle down, idle left, idle right, idle up, or idle,
   * depending on which one of these animations exists.
   *
   * <p>If no animations for any idle-state exist, {@link Animation#defaultAnimation()} for "IDLE"
   * is set.
   *
   * @param path Path (as a string) to the directory in the assets folder where the subdirectories
   *     containing the animation files are stored. Example: "character/knight".
   * @throws IOException if the given path does not exist.
   * @see Animation
   */
  public DrawComponent(final IPath path, AnimationConfig config) {
    stateMachine = new StateMachine(path, config);
  }
  public DrawComponent(final IPath path, SpritesheetConfig config) {
    stateMachine = new StateMachine(path, config);
  }
  public DrawComponent(final IPath path) {
    this(path, new AnimationConfig());
  }
  public DrawComponent(final Animation animation) {
    stateMachine = new StateMachine(animation);
  }
  public DrawComponent(List<State> states) {
    stateMachine = new StateMachine(states);
  }
  public DrawComponent(StateMachine stateMachine) {
    this.stateMachine = stateMachine;
  }

  public void sendSignal(String signal, Object data){
    stateMachine.sendSignal(new Signal(signal, data));
  }
  public void sendSignal(String signal){
    sendSignal(signal, null);
  }

  public void update(){
    stateMachine.update();
  }

  public Sprite getSprite(){
    return stateMachine.getSprite();
  }
  public float getSpriteWidth(){
    return stateMachine.getSpriteWidth();
  }
  public float getSpriteHeight(){
    return stateMachine.getSpriteHeight();
  }

  public Animation currentAnimation(){
    return stateMachine.getCurrentState().getAnimation();
  }

  /**
   * Check if the current animation is a looping animation.
   *
   * @return true if the current animation is looping.
   */
  public boolean isCurrentAnimationLooping() {
    return currentAnimation().isLooping();
  }

  /**
   * Check if the current animation has finished playing.
   *
   * @return true if the current animation has finished playing.
   */
  public boolean isCurrentAnimationFinished() {
    return stateMachine.isAnimationFinished();
  }

  public boolean hasState(String name){
    return stateMachine.getState(name) != null;
  }

  public void resetState(){
    stateMachine.reset();
  }

  /**
   * Check if the component is visible. If the component is visible, it will be drawn by the {@link
   * core.systems.DrawSystem}.
   *
   * @return true if the component is visible, false if not.
   */
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * Set the visibility of the component. If the component is visible, it will be drawn by the
   * {@link core.systems.DrawSystem}.
   *
   * @param visible The new visibility status to set. True for visible, false for hidden.
   */
  public void setVisible(boolean visible) {
    isVisible = visible;
  }

  /**
   * Returns the tint color of the DrawComponent.
   *
   * @return The tint color of the DrawComponent. If the tint color is -1, no tint is applied.
   */
  public int tintColor() {
    return this.tintColor;
  }

  /**
   * Sets the tint color of the DrawComponent. Set it to -1 to remove the tint.
   *
   * @param tintColor The new tint color to set.
   */
  public void tintColor(int tintColor) {
    this.tintColor = tintColor;
  }

  public StateMachine stateMachine() { return stateMachine; }
  public int depth() { return depth; }
  public void depth(int depth) { this.depth = depth; }
}
