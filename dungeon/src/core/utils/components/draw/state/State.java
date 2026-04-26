package core.utils.components.draw.state;

import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.AnimationFrame;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a state with associated animation and optional data.
 *
 * <p>A state encapsulates an {@link Animation}, optional user-defined data, and optional callbacks
 * that are invoked when entering or exiting the state. States are typically used in animation
 * systems or state machines to manage different visual states of entities.
 *
 * <p>States are serializable and can be stored or transmitted as needed.
 *
 * @see Animation
 * @see AnimationConfig
 * @see SpritesheetConfig
 */
public class State implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** The name of this state. */
  public final String name;

  /** The animation associated with this state. */
  protected Animation animation;

  /** Optional user-defined data attached to this state. */
  protected Object data;

  private Consumer<State> onEnter;
  private Consumer<State> onExit;

  /**
   * Constructs a state with the specified name and animation.
   *
   * @param name the name of the state must not be null
   * @param animation the animation for this state must not be null
   * @throws IllegalArgumentException if the name or animation is null
   */
  public State(String name, Animation animation) {
    if (name == null) throw new IllegalArgumentException("name can't be empty");
    if (animation == null) throw new IllegalArgumentException("animation can't be null");
    this.name = name;
    this.animation = animation;
  }

  /**
   * Constructs a state with the specified name and creates an animation from the given path.
   *
   * @param name the name of the state must not be null
   * @param path the path to the animation resource must not be null
   */
  public State(String name, IPath path) {
    this(name, new Animation(path));
  }

  /**
   * Constructs a state with the specified name, path, and animation configuration.
   *
   * @param name the name of the state must not be null
   * @param path the path to the animation resource must not be null
   * @param config the animation configuration must not be null
   */
  public State(String name, IPath path, AnimationConfig config) {
    this(name, new Animation(path, config));
  }

  /**
   * Constructs a state with the specified name, path, and spritesheet configuration.
   *
   * @param name the name of the state must not be null
   * @param path the path to the animation resource must not be null
   * @param config the spritesheet configuration must not be null
   */
  public State(String name, IPath path, SpritesheetConfig config) {
    this(name, new Animation(path, new AnimationConfig(config)));
  }

  /**
   * Returns the animation associated with this state.
   *
   * @return the animation, never null
   */
  public Animation getAnimation() {
    return animation;
  }

  /** Updates the animation state. This should be called regularly to progress animation frames. */
  public void update() {
    getAnimation().update();
  }

  /**
   * Sets the number of frames for the animation.
   *
   * @param frameCount the number of frames to display
   */
  public void frameCount(int frameCount) {
    getAnimation().frameCount(frameCount);
  }

  /**
   * Returns the current animation frame.
   *
   * @return the current animation frame
   */
  public AnimationFrame getFrame() {
    return getAnimation().getFrame();
  }

  /**
   * Returns the width of the animation.
   *
   * @return the width in units
   */
  public float getWidth() {
    return getAnimation().getWidth();
  }

  /**
   * Returns the height of the animation.
   *
   * @return the height in units
   */
  public float getHeight() {
    return getAnimation().getHeight();
  }

  /**
   * Returns the width of an individual sprite in the animation.
   *
   * @return the sprite width in units
   */
  public float getSpriteWidth() {
    return getAnimation().getSpriteWidth();
  }

  /**
   * Returns the height of an individual sprite in the animation.
   *
   * @return the sprite height in units
   */
  public float getSpriteHeight() {
    return getAnimation().getSpriteHeight();
  }

  /**
   * Checks whether the animation is set to loop.
   *
   * @return true if the animation loops, false otherwise
   */
  public boolean isLooping() {
    return getAnimation().getConfig().isLooping();
  }

  /**
   * Checks whether the animation has finished playing.
   *
   * @return true if the animation is finished, false otherwise
   */
  public boolean isFinished() {
    return getAnimation().isFinished();
  }

  /**
   * Checks whether the animation has finished playing. This is an alias for {@link #isFinished()}.
   *
   * @return true if the animation is finished, false otherwise
   */
  public boolean isAnimationFinished() {
    return getAnimation().isFinished();
  }

  /**
   * Returns the user-defined data attached to this state.
   *
   * @return the user data, or null if no data has been set
   */
  public Object getData() {
    return data;
  }

  /**
   * Sets user-defined data for this state.
   *
   * @param data arbitrary data to attach to this state
   */
  public void setData(Object data) {
    this.data = data;
  }

  /**
   * Sets a callback to be executed when entering this state.
   *
   * @param onEnter the callback function
   */
  public void setOnEnter(Consumer<State> onEnter) {
    this.onEnter = onEnter;
  }

  /** Executes the callback function defined for entering this state, if one has been set. */
  public void onEnter() {
    if (onEnter != null) onEnter.accept(this);
  }

  /** Executes the callback function associated with exiting this state, if one is defined. */
  public void onExit() {
    if (onExit != null) onExit.accept(this);
  }

  /**
   * Creates a state from a map of animations using the provided state name.
   *
   * @param map a map of animation names to animation objects must not be null
   * @param name the name of the state to retrieve from the map must not be null
   * @return a new state with the specified name and the corresponding animation from the map
   * @throws IllegalArgumentException if name or map is null
   */
  public static State fromMap(Map<String, Animation> map, String name) {
    if (name == null) throw new IllegalArgumentException("name can't be null");
    if (map == null) throw new IllegalArgumentException("map can't be null");
    return new State(name, map.get(name));
  }

  /**
   * Returns a string representation of this state.
   *
   * @return a string containing the class name, state name, animation, and data
   */
  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{"
        + "name='"
        + name
        + '\''
        + ", animation="
        + animation
        + ", data="
        + data
        + '}';
  }
}
