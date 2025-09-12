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
 * Animation#loadAnimationSpritesheet(IPath)} will check if there is a .json file with the same name
 * next to the image file, containing a {@link Map}{@code <String, }{@link AnimationConfig}{@code
 * >}, outlining the different Animations found in the image, and load this {@link Map} with all
 * Animations.
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
   * @param config The animation config to use
   * @see Animation
   */
  public DrawComponent(final IPath path, AnimationConfig config) {
    this(path, config, null);
  }

  /**
   * Create a new DrawComponent.
   *
   * @param path Path to the image in the assets folder. If the path leads to a folder, it will be
   *     assumed that the target image file is within that folder with the same name as the folder
   *     but as png. Example: "character/knight" resolves to "assets/character/knight/knight.png".
   * @param config The animation config to use
   * @param defaultStateName Name of the default state to use
   * @see Animation
   */
  public DrawComponent(final IPath path, AnimationConfig config, String defaultStateName) {
    stateMachine = new StateMachine(path, config, defaultStateName);
  }

  /**
   * Create a new DrawComponent from a spritesheet configuration.
   *
   * @param path Path to the spritesheet in the assets folder.
   * @param config The spritesheet configuration to use.
   * @see SpritesheetConfig
   */
  public DrawComponent(final IPath path, SpritesheetConfig config) {
    stateMachine = new StateMachine(path, config);
  }

  /**
   * Create a new DrawComponent with a default {@link AnimationConfig}.
   *
   * @param path Path to the image in the assets folder. If the path leads to a folder, it will be
   *     assumed that the target image file is within that folder with the same name as the folder
   *     but as png. Example: "character/knight" resolves to "assets/character/knight/knight.png".
   * @see AnimationConfig
   */
  public DrawComponent(final IPath path) {
    this(path, new AnimationConfig(), null);
  }

  /**
   * Create a new DrawComponent with a default {@link AnimationConfig}.
   *
   * @param path Path to the image in the assets folder. If the path leads to a folder, it will be
   *     assumed that the target image file is within that folder with the same name as the folder
   *     but as png. Example: "character/knight" resolves to "assets/character/knight/knight.png".
   * @param defaultStateName Name of the state to be used as default
   * @see AnimationConfig
   */
  public DrawComponent(final IPath path, String defaultStateName) {
    this(path, new AnimationConfig(), defaultStateName);
  }

  /**
   * Create a new DrawComponent from a single {@link Animation}.
   *
   * @param animation The animation to initialize the component with.
   */
  public DrawComponent(final Animation animation) {
    stateMachine = new StateMachine(animation);
  }

  /**
   * Create a new DrawComponent from a list of states.
   *
   * @param states The list of states to initialize the state machine with.
   */
  public DrawComponent(List<State> states) {
    stateMachine = new StateMachine(states);
  }

  /**
   * Create a new DrawComponent from a list of states.
   *
   * @param states The list of states to initialize the state machine with.
   * @param defaultState The state to be used as default
   */
  public DrawComponent(List<State> states, State defaultState) {
    stateMachine = new StateMachine(states, defaultState);
  }

  /**
   * Create a new DrawComponent directly from a {@link StateMachine}.
   *
   * @param stateMachine The state machine to use for this component.
   */
  public DrawComponent(StateMachine stateMachine) {
    this.stateMachine = stateMachine;
  }

  /**
   * Create a new DrawComponent directly from a {@link StateMachine} with the given depth layer.
   *
   * @param stateMachine The state machine to use for this component.
   * @param depthLayer The depth layer where the textures will be drawn.
   */
  public DrawComponent(StateMachine stateMachine, DepthLayer depthLayer) {
    this(stateMachine);
    this.depth = depthLayer.depth();
  }

  /**
   * Send a {@link Signal} with associated data to the {@link StateMachine}.
   *
   * @param signal The signal name.
   * @param data The data to pass along with the signal.
   */
  public void sendSignal(String signal, Object data) {
    stateMachine.sendSignal(new Signal(signal, data));
  }

  /**
   * Send a {@link Signal} without data to the {@link StateMachine}.
   *
   * @param signal The signal name.
   */
  public void sendSignal(String signal) {
    sendSignal(signal, null);
  }

  /**
   * Update the {@link StateMachine} of this component.
   *
   * <p>This should be called once per game loop to progress animations and handle transitions.
   */
  public void update() {
    stateMachine.update();
  }

  /**
   * Get the current {@link Sprite} of this component.
   *
   * @return The current sprite frame.
   */
  public Sprite getSprite() {
    return stateMachine.getSprite();
  }

  /**
   * Get the logical width of this component (may differ from sprite width).
   *
   * @return The width in world units.
   */
  public float getWidth() {
    return stateMachine.getWidth();
  }

  /**
   * Get the logical height of this component (may differ from sprite height).
   *
   * @return The height in world units.
   */
  public float getHeight() {
    return stateMachine.getHeight();
  }

  /**
   * Get the pixel width of the current sprite.
   *
   * @return The width of the current sprite frame in pixels.
   */
  public float getSpriteWidth() {
    return stateMachine.getSpriteWidth();
  }

  /**
   * Get the pixel height of the current sprite.
   *
   * @return The height of the current sprite frame in pixels.
   */
  public float getSpriteHeight() {
    return stateMachine.getSpriteHeight();
  }

  /**
   * Get the current {@link Animation}.
   *
   * @return The active animation of the component.
   */
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

  /**
   * Check whether a state with the given name exists.
   *
   * @param name The name of the state.
   * @return true if the state exists, false otherwise.
   */
  public boolean hasState(String name) {
    return stateMachine.getState(name) != null;
  }

  /**
   * Get the current {@link State}.
   *
   * @return The active state of the component.
   */
  public State currentState() {
    return stateMachine.getCurrentState();
  }

  /**
   * Get the name of the current state.
   *
   * @return The name of the active state.
   */
  public String currentStateName() {
    return stateMachine.getCurrentStateName();
  }

  /**
   * Get the data associated with the current state.
   *
   * @return The data object attached to the current state.
   */
  public Object currentStateData() {
    return stateMachine.getCurrentState().getData();
  }

  /** Reset the {@link StateMachine} to its initial state. */
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
   * @return The tint color in RGBA of the DrawComponent. If the tint color is -1, no tint is
   *     applied.
   */
  public int tintColor() {
    return this.tintColor;
  }

  /**
   * Sets the tint color of the DrawComponent.
   *
   * <p>The color must be specified as a 32-bit RGBA integer in the form {@code 0xRRGGBBAA}:
   *
   * <ul>
   *   <li>{@code RR} = Red component (0x00–0xFF)
   *   <li>{@code GG} = Green component (0x00–0xFF)
   *   <li>{@code BB} = Blue component (0x00–0xFF)
   *   <li>{@code AA} = Alpha (transparency), 0x00 is fully transparent, 0xFF is fully opaque
   * </ul>
   *
   * <p>Passing {@code -1} will remove the tint.
   *
   * @param tintColor The new tint color to set, in RGBA format ({@code 0xRRGGBBAA}).
   */
  public void tintColor(int tintColor) {
    this.tintColor = tintColor;
  }

  /**
   * Get the underlying {@link StateMachine}.
   *
   * @return The state machine of this component.
   */
  public StateMachine stateMachine() {
    return stateMachine;
  }

  /**
   * Get the rendering depth of this component.
   *
   * <p>The depth determines the draw order. Lower values are drawn earlier (behind), higher values
   * later (in front).
   *
   * @return The depth value.
   */
  public int depth() {
    return depth;
  }

  /**
   * Set the rendering depth of this component.
   *
   * <p>The depth determines the draw order. Lower values are drawn earlier (behind), higher values
   * later (in front).
   *
   * @param depth The new depth value.
   */
  public void depth(int depth) {
    this.depth = depth;
  }
}
