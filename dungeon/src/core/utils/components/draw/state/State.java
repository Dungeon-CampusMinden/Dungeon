package core.utils.components.draw.state;

import com.badlogic.gdx.graphics.g2d.Sprite;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.util.List;
import java.util.Map;

/**
 * Represents a visual state in the game, associated with an animation.
 *
 * <p>A State holds an {@link Animation} and optional data. It provides utility methods to update
 * the animation, retrieve sprite information, and query animation properties.
 *
 * <p>This class serves as a base for directional or multi-animation states, such as {@link
 * DirectionalState}.
 */
public class State {

  /** The name of this state. */
  public final String name;

  /** The animation associated with this state. */
  protected Animation animation;

  /** Optional user-defined data associated with this state. */
  protected Object data;

  /**
   * Constructs a state with a name and an animation.
   *
   * @param name the name of the state
   * @param animation the animation associated with this state
   * @throws IllegalArgumentException if {@code name} or {@code animation} is null
   */
  public State(String name, Animation animation) {
    if (name == null) throw new IllegalArgumentException("name can't be empty");
    if (animation == null) throw new IllegalArgumentException("animation can't be null");
    this.name = name;
    this.animation = animation;
  }

  /**
   * Constructs a state from a single path and animation configuration.
   *
   * @param name the name of the state
   * @param path the path to the animation sprite(s)
   * @param config the animation configuration
   */
  public State(String name, IPath path, AnimationConfig config) {
    this(name, new Animation(path, config));
  }

  /**
   * Constructs a state from a single path and spritesheet configuration.
   *
   * @param name the name of the state
   * @param path the path to the animation sprite(s)
   * @param config the spritesheet configuration
   */
  public State(String name, IPath path, SpritesheetConfig config) {
    this(name, path, new AnimationConfig(config));
  }

  /**
   * Constructs a state from a single path using default animation configuration.
   *
   * @param name the name of the state
   * @param path the path to the animation sprite(s)
   */
  public State(String name, IPath path) {
    this(name, path, new AnimationConfig());
  }

  /**
   * Constructs a state from multiple paths.
   *
   * @param name the name of the state
   * @param paths the animation paths
   */
  public State(String name, IPath... paths) {
    this(name, new Animation(paths));
  }

  /**
   * Constructs a state from a list of paths.
   *
   * @param name the name of the state
   * @param paths the animation paths
   */
  public State(String name, List<IPath> paths) {
    this(name, new Animation(paths));
  }

  /** Updates the current animation to the next frame. */
  public void update() {
    getAnimation().update();
  }

  /**
   * Sets the frame count for the current animation.
   *
   * @param frameCount the number of frames to display per sprite
   */
  public void frameCount(int frameCount) {
    getAnimation().frameCount(frameCount);
  }

  /**
   * @return the current sprite of the animation.
   */
  public Sprite getSprite() {
    return getAnimation().getSprite();
  }

  /**
   * @return the width of the current animation frame.
   */
  public float getWidth() {
    return getAnimation().getWidth();
  }

  /**
   * @return the height of the current animation frame.
   */
  public float getHeight() {
    return getAnimation().getHeight();
  }

  /**
   * @return the sprite width of the current animation frame.
   */
  public float getSpriteWidth() {
    return getAnimation().getSpriteWidth();
  }

  /**
   * @return the sprite height of the current animation frame.
   */
  public float getSpriteHeight() {
    return getAnimation().getSpriteHeight();
  }

  /**
   * @return true if the animation has finished playing; false otherwise.
   */
  public boolean isAnimationFinished() {
    return getAnimation().isFinished();
  }

  /**
   * @return the animation associated with this state.
   */
  public Animation getAnimation() {
    return animation;
  }

  /**
   * @return user-defined data associated with this state.
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
   * Constructs a state from a map of animations by name.
   *
   * @param map the map of animations
   * @param name the name of the animation to use
   * @return a new State using the animation from the map
   * @throws IllegalArgumentException if {@code map} or {@code name} is null
   */
  public static State fromMap(Map<String, Animation> map, String name) {
    if (name == null) throw new IllegalArgumentException("name can't be null");
    if (map == null) throw new IllegalArgumentException("map can't be null");
    return new State(name, map.get(name));
  }

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
