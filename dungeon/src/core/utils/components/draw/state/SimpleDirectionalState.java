package core.utils.components.draw.state;

import core.utils.Direction;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;

/**
 * Represents a directional state which only has one animation. The animation is assumed to be
 * facing right by default and is mirrored when facing left.
 *
 * <p>This class extends {@link State} and automatically selects the correct animation based on the
 * current {@link Direction} stored in the state's data.
 *
 * <p>It supports multiple ways to construct the state, either from existing {@link Animation}
 * objects, {@link IPath} textures with {@link AnimationConfig}, or a {@link SpritesheetConfig}.
 */
public class SimpleDirectionalState extends State {

  /** Animation for moving left. */
  private Direction previousFacing;

  private Direction lastFramesDirection;

  /**
   * Constructs the state directly from an {@link Animation} object.
   *
   * @param name the name of the state
   * @param animation the animation
   */
  public SimpleDirectionalState(String name, Animation animation) {
    super(name, animation);
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
    Animation anim = super.getAnimation();

    if (direction == null) return anim;
    if (direction == Direction.NONE) return anim;

    // If currently moving UP/DOWN, show the last horizontally facing direction. Otherwise, show the
    // current direction.
    // If left, mirror the animation.
    if (direction == Direction.DOWN || direction == Direction.UP) {
      if (previousFacing == Direction.LEFT) {
        anim.mirrored(true);
      } else if (previousFacing == Direction.RIGHT) {
        anim.mirrored(false);
      }
    } else if (direction == Direction.LEFT) {
      anim.mirrored(true);
      previousFacing = Direction.LEFT;
    } else if (direction == Direction.RIGHT) {
      anim.mirrored(false);
      previousFacing = Direction.RIGHT;
    }

    if (previousFacing == null || direction != lastFramesDirection) {
      previousFacing = direction;
    }
    lastFramesDirection = direction;

    return anim;
  }
}
