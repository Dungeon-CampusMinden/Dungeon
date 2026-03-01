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
 * Represents a visual state in the game, associated with an animation.
 *
 * <p>Core API is engine-agnostic: exposes {@link AnimationFrame} instead of libGDX Sprite.
 */
public class State implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public final String name;
  protected Animation animation;
  protected Object data;

  private Consumer<State> onEnter;
  private Consumer<State> onExit;

  public State(String name, Animation animation) {
    if (name == null) throw new IllegalArgumentException("name can't be empty");
    if (animation == null) throw new IllegalArgumentException("animation can't be null");
    this.name = name;
    this.animation = animation;
  }

  public State(String name, IPath path) {
    this(name, new Animation(path));
  }

  public State(String name, IPath path, AnimationConfig config) {
    this(name, new Animation(path, config));
  }

  public State(String name, IPath path, SpritesheetConfig config) {
    this(name, new Animation(path, new AnimationConfig(config)));
  }

  public Animation getAnimation() {
    return animation;
  }

  public void update() {
    getAnimation().update();
  }

  public void frameCount(int frameCount) {
    getAnimation().frameCount(frameCount);
  }

  public AnimationFrame getFrame() {
    return getAnimation().getFrame();
  }

  public float getWidth() {
    return getAnimation().getWidth();
  }

  public float getHeight() {
    return getAnimation().getHeight();
  }

  public float getSpriteWidth() {
    return getAnimation().getSpriteWidth();
  }

  public float getSpriteHeight() {
    return getAnimation().getSpriteHeight();
  }

  public boolean isLooping() {
    return getAnimation().getConfig().isLooping();
  }

  public boolean isFinished() {
    return getAnimation().isFinished();
  }

  public boolean isAnimationFinished() {
    return getAnimation().isFinished();
  }

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

  public void onEnter() {
    if (onEnter != null) onEnter.accept(this);
  }

  /**
   * Sets a callback to be executed when exiting this state.
   *
   * @param onExit the callback function
   */
  public void setOnExit(Consumer<State> onExit) {
    this.onExit = onExit;
  }

  public void onExit() {
    if (onExit != null) onExit.accept(this);
  }

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
