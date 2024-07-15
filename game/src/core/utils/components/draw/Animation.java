package core.utils.components.draw;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An Animation is what you see when a {@link core.Entity} is drawn on the screen.
 *
 * <p>An Animation is basically just a list of different Image files.
 *
 * <p>Use {@link #fromCollection(Collection)} or {@link #fromSingleImage(IPath)} to create an
 * Animation.
 *
 * <p>Animations are stored inside the {@link core.components.DrawComponent}.
 *
 * <p>An Animation is split into different frames. Each frame is one picture (usually also one image
 * file).
 *
 * <p>The {@link core.components.DrawComponent} will automatically create Animations on creation
 * based on the given path, so normally you don't have to create your own instances.
 *
 * <p>An Animation can have different configurations. Use {@link #timeBetweenFrames(int)} to set the
 * time between two frames. Use {@link #loop(boolean)} to define if the Animation stops at the last
 * frame or should loop (starts from the beginning, this is the default setting).
 *
 * @see core.components.DrawComponent
 * @see IPath
 */
public final class Animation {

  private static final IPath MISSING_TEXTURE = new SimpleIPath("animation/missing_texture.png");
  private static final int DEFAULT_FRAME_TIME = 5;
  private static final boolean DEFAULT_IS_LOOP = true;
  private static final int DEFAULT_PRIO = 200;

  private final List<IPath> animationFrames;

  private final int frames;
  private final int priority;
  private int timeBetweenFrames;
  private int currentFrameIndex = 0;
  private int frameTimeCounter = 0;
  private boolean looping;

  /**
   * Creates an animation.
   *
   * @param animationFrames The list of textures that builds the animation. Must be in order.
   * @param frameTime How many frames to wait, before switching to the next texture?
   * @param looping should the Animation continue to repeat ?
   * @param prio priority for playing this animation
   */
  private Animation(
      final Collection<IPath> animationFrames, int frameTime, boolean looping, int prio) {
    assert (animationFrames != null && !animationFrames.isEmpty());
    this.animationFrames = new ArrayList<>(animationFrames);
    frames = animationFrames.size();
    if (frameTime == 0) {
      throw new IllegalArgumentException(
          "Parameter frameTime is set to 0, frameTime must be greater than 0!");
    }
    this.timeBetweenFrames = frameTime;
    this.looping = looping;
    this.priority = prio;
  }

  /**
   * Creates an animation. repeats forever
   *
   * @param animationFrames The list of textures that builds the animation. Must be in order.
   * @param frameTime How many frames to wait, before switching to the next texture?
   * @param prio priority for playing this animation
   * @return foo
   */
  public static Animation fromCollection(
      final Collection<IPath> animationFrames, int frameTime, int prio) {
    return new Animation(animationFrames, frameTime, DEFAULT_IS_LOOP, prio);
  }

  /**
   * Creates an animation with the default configuration.
   *
   * @param animationFrames The list of textures that builds the animation. Must be in order.
   * @return foo
   */
  public static Animation fromCollection(final Collection<IPath> animationFrames) {
    return new Animation(
        animationFrames,
        DEFAULT_FRAME_TIME,
        DEFAULT_IS_LOOP,
        CoreAnimationPriorities.DEFAULT.priority());
  }

  /**
   * Creates an animation.
   *
   * @param animationFrames The list of textures that builds the animation. Must be in order.
   * @param frameTime How many frames to wait, before switching to the next texture?
   * @param looping should the Animation continue to repeat ?
   * @param prio priority for playing this animation
   * @return foo
   */
  public static Animation fromCollection(
      final Collection<IPath> animationFrames, int frameTime, boolean looping, int prio) {
    return new Animation(animationFrames, frameTime, looping, prio);
  }

  /**
   * Create an animation from single frame and the default configuration.
   *
   * @param fileName path to the frame
   * @return The created Animation instance
   */
  public static Animation fromSingleImage(final IPath fileName) {
    return new Animation(List.of(fileName), DEFAULT_FRAME_TIME, DEFAULT_IS_LOOP, DEFAULT_PRIO);
  }

  /**
   * Create an animation from single frame and the given configuration.
   *
   * @param fileName path to the frame
   * @param frameTime How many frames to wait, before switching to the next texture?
   * @return The created Animation instance
   */
  public static Animation fromSingleImage(final IPath fileName, int frameTime) {
    return new Animation(List.of(fileName), frameTime, DEFAULT_IS_LOOP, DEFAULT_PRIO);
  }

  /**
   * Create a new animation with default settings, and the missing_textures file as animation
   * frames.
   *
   * @return missing texture animation
   */
  public static Animation defaultAnimation() {
    return new Animation(Set.of(MISSING_TEXTURE), DEFAULT_FRAME_TIME, DEFAULT_IS_LOOP, 0);
  }

  /**
   * Get the texture to draw.
   *
   * <p>Automatically updates currentFrame to next frame.
   *
   * @return The texture of the next animation step (draw this).
   */
  public IPath nextAnimationTexturePath() {
    if (isFinished()) {
      return animationFrames.get(currentFrameIndex);
    }
    IPath pathToReturn = animationFrames.get(currentFrameIndex);
    frameTimeCounter = (frameTimeCounter + 1) % timeBetweenFrames;
    if (frameTimeCounter == 0) {
      currentFrameIndex = (currentFrameIndex + 1) % frames;
    }
    return pathToReturn;
  }

  /**
   * Check if the animation is finished.
   *
   * @return true when last frame and is not looping, otherwise false
   */
  public boolean isFinished() {
    return !looping && currentFrameIndex == frames - 1;
  }

  /**
   * Check if the animation is looping.
   *
   * @return true when looping, otherwise false
   */
  public boolean isLooping() {
    return looping;
  }

  /**
   * Get the List of animation frames.
   *
   * @return List containing the paths of the single frames of the animation.
   */
  public List<IPath> animationFrames() {
    return new ArrayList<>(animationFrames);
  }

  /**
   * Set the time (in frames) between two animation frames.
   *
   * @param timeBetweenFrames Time before switching to the next animation frame.
   */
  public void timeBetweenFrames(int timeBetweenFrames) {
    this.timeBetweenFrames = timeBetweenFrames;
  }

  /**
   * Set if the Animation should stop at the end or start again if the last frame was played.
   *
   * <p>If loop is set to false, this animation will stop at the last frame.
   *
   * <p>If loop is set to true, after the last frame is played, the animation will restart at the
   * first frame in the list.
   *
   * @param loop true if you want to loop, false if not
   */
  public void loop(boolean loop) {
    this.looping = loop;
  }

  /**
   * Higher Priority means the Animation is more likely to be used.
   *
   * @return the priority of the animation
   */
  public int priority() {
    return priority;
  }

  /**
   * @return the amount of frames it would take to finish one loop of the Animation
   */
  public int duration() {
    return timeBetweenFrames * animationFrames.size();
  }
}
