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
import core.utils.components.draw.state.Transition;
import core.utils.components.path.IPath;
import java.util.*;
import java.util.logging.Logger;

/**
 * Store all {@link Animation}s for an entity.
 *
 * <p>At creation, the component will read in a single file, which can directly be a sprite or a
 * spritesheet.
 *
 * <p>Example: "character/knight" resolves to "assets/character/knight/knight.png".
 *
 * <p>To make loading spritesheets easier, the function {@link
 * Animation.loadAnimationSpritesheet(IPath)} will check if there is a .json file with the same name
 * next to the image file, containing a {@link Map}<String, {@link AnimationConfig}>, outlining the
 * different Animations found in the image, and load this {@link Map} with all Animations.
 *
 * <p>This component will build a {@link StateMachine}, where each {@link State} represents one
 * animation that the entity can have. Between {@link State}s, transitions are responsible to denote
 * how the entity switches between states and what {@link Signal}s are required to do so.
 *
 * <p>Each Animation will be created with default settings. If you want to change these settings,
 * use the methods from {@link Animation}.
 *
 * <p>Animations will be searched in the default asset directory. Normally, this is "game/assets",
 * but you can change it in the "gradle.build" file if you like.
 *
 * @see StateMachine
 * @see State
 * @see Transition
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
   * @param path Path to the image in the assets folder. If the path leads to a folder, it will be
   *     assumed that the target image file is within that folder with the same name as the folder
   *     but as png. Example: "character/knight" resolves to "assets/character/knight/knight.png".
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

  public void sendSignal(String signal, Object data) {
    System.out.println("Got signal: " + signal + " | with data: " + data);
    stateMachine.sendSignal(new Signal(signal, data));
  }

  public void sendSignal(String signal) {
    sendSignal(signal, null);
  }

  public void update() {
    stateMachine.update();
  }

  public Sprite getSprite() {
    return stateMachine.getSprite();
  }

  public float getSpriteWidth() {
    return stateMachine.getSpriteWidth();
  }

  public float getSpriteHeight() {
    return stateMachine.getSpriteHeight();
  }

  public Animation currentAnimation() {
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

  public boolean hasState(String name) {
    return stateMachine.getState(name) != null;
  }

  public State currentState() {
    return stateMachine.getCurrentState();
  }

  public String currentStateName() {
    return stateMachine.getCurrentStateName();
  }

  public Object currentStateData() {
    return stateMachine.getCurrentState().getData();
  }

  public void resetState() {
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

  public StateMachine stateMachine() {
    return stateMachine;
  }

  public int depth() {
    return depth;
  }

  public void depth(int depth) {
    this.depth = depth;
  }
}
