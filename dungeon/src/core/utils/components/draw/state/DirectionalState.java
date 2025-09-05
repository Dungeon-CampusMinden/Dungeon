package core.utils.components.draw.state;

import core.utils.Direction;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import java.util.Map;

/**
 * Represents a state that holds different animations for each cardinal direction (up, down, left,
 * right).
 *
 * <p>This class extends {@link State} and automatically selects the correct animation based on the
 * current {@link Direction} stored in the state's data.
 *
 * <p>It supports multiple ways to construct the state, either from existing {@link Animation}
 * objects, {@link IPath} textures with {@link AnimationConfig}, or a {@link SpritesheetConfig}.
 */
public class DirectionalState extends State {

  /** Animation for moving left. */
  private Animation left;

  /** Animation for moving up. */
  private Animation up;

  /** Animation for moving right. */
  private Animation right;

  /**
   * Constructs a directional state from a map of animations using a prefix for key lookup.
   *
   * <p>The down animation is used as the default and is passed to the superclass {@link State}.
   *
   * @param name the name of the state
   * @param map a map of animation names to {@link Animation} objects
   * @param prefix the prefix used to find the directional animations (e.g., "walk")
   */
  public DirectionalState(String name, Map<String, Animation> map, String prefix) {
    super(name, map.get(prefix + "_down"));
    this.left = map.get(prefix + "_left");
    this.up = map.get(prefix + "_up");
    this.right = map.get(prefix + "_right");
  }

  /**
   * Constructs a directional state from a map of animations using the state name as prefix.
   *
   * @param name the name of the state
   * @param map a map of animation names to {@link Animation} objects
   */
  public DirectionalState(String name, Map<String, Animation> map) {
    this(name, map, name);
  }

  /**
   * Constructs a directional state directly from {@link Animation} objects.
   *
   * @param name the name of the state
   * @param down the down/default animation
   * @param left the left animation
   * @param up the up animation
   * @param right the right animation
   */
  public DirectionalState(
      String name, Animation down, Animation left, Animation up, Animation right) {
    super(name, down);
    this.left = left;
    this.up = up;
    this.right = right;
  }

  /**
   * Constructs a directional state from {@link IPath} textures with individual {@link
   * AnimationConfig} objects for each direction.
   *
   * @param name the name of the state
   * @param down path to down/default animation texture
   * @param left path to left animation texture
   * @param up path to up animation texture
   * @param right path to right animation texture
   * @param configDown configuration for down animation
   * @param configLeft configuration for left animation
   * @param configUp configuration for up animation
   * @param configRight configuration for right animation
   */
  public DirectionalState(
      String name,
      IPath down,
      IPath left,
      IPath up,
      IPath right,
      AnimationConfig configDown,
      AnimationConfig configLeft,
      AnimationConfig configUp,
      AnimationConfig configRight) {
    this(
        name,
        new Animation(down, configDown),
        new Animation(left, configLeft),
        new Animation(up, configUp),
        new Animation(right, configRight));
  }

  /**
   * Constructs a directional state from {@link IPath} textures using the same {@link
   * AnimationConfig} for all directions.
   *
   * @param name the name of the state
   * @param down path to down/default animation texture
   * @param left path to left animation texture
   * @param up path to up animation texture
   * @param right path to right animation texture
   * @param config configuration for all animations
   */
  public DirectionalState(
      String name, IPath down, IPath left, IPath up, IPath right, AnimationConfig config) {
    this(name, down, left, up, right, config, config, config, config);
  }

  /**
   * Constructs a directional state from a single {@link IPath} texture with a {@link
   * SpritesheetConfig}.
   *
   * @param name the name of the state
   * @param path the path to the spritesheet
   * @param config the spritesheet configuration
   */
  public DirectionalState(String name, IPath path, SpritesheetConfig config) {
    super(name, path, config);
  }

  /**
   * Constructs a directional state from {@link IPath} textures without any configuration.
   *
   * @param name the name of the state
   * @param down path to down/default animation texture
   * @param left path to left animation texture
   * @param up path to up animation texture
   * @param right path to right animation texture
   */
  public DirectionalState(String name, IPath down, IPath left, IPath up, IPath right) {
    this(name, down, left, up, right, null);
  }

  /**
   * Returns the appropriate animation based on the current direction stored in the state's data.
   *
   * <p>If no direction is set or the direction is DOWN/NONE, the default (down) animation is
   * returned.
   *
   * @return the animation corresponding to the current direction
   */
  @Override
  public Animation getAnimation() {
    Direction direction = (Direction) getData();
    if (direction == null) return super.getAnimation();
    return switch (direction) {
      case DOWN, NONE -> super.getAnimation();
      case LEFT -> left;
      case UP -> up;
      case RIGHT -> right;
    };
  }
}
