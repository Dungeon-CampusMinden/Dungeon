package core.utils.components.draw;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An Animation is what you see when a {@link core.Entity} is drawn on the screen.
 *
 * <p>An Animation is basically just a list of different Image files.
 *
 * <p>Animations are stored inside the {@link core.components.DrawComponent}.
 *
 * <p>An Animation is split into different frames. Each frame is one picture (usually also one image
 * file).
 *
 * <p>The {@link core.components.DrawComponent} will automatically create Animations on creation
 * based on the given path, so normally you don't have to create your own instances.
 *
 * <p>An Animation can have different configurations. Use {@link #setTimeBetweenFrames} to set the
 * time between two frames. Use {@link #setLoop} to define if the Animation stops at the last frame
 * or should loop (starts from the beginning, this is the default setting).
 *
 * @see core.components.DrawComponent
 * @see IPath
 */
public final class Animation {
    private static final int DEFAULT_FRAME_TIME = 5;
    private static final boolean DEFAULT_IS_LOOP = true;

    /** The set of textures that build the animation. */
    private final List<String> animationFrames;

    /** The count of textures for the animation. */
    private final int frames;

    /** Number of frames between switching to the next animation? */
    private int timeBetweenFrames;

    /** Index of the NEXT texture that will be returned. */
    private int currentFrameIndex = 0;

    /** How many frames since the last texture switching? */
    private int frameTimeCounter = 0;

    private boolean looping;

    /**
     * Creates an animation.
     *
     * @param animationFrames The list of textures that builds the animation. Must be in order.
     * @param frameTime How many frames to wait, before switching to the next texture?
     * @param looping should the Animation continue to repeat ?
     */
    public Animation(Collection<String> animationFrames, int frameTime, boolean looping) {
        assert (animationFrames != null && !animationFrames.isEmpty());
        assert (frameTime > 0);
        this.animationFrames = new ArrayList<>(animationFrames);
        frames = animationFrames.size();
        this.timeBetweenFrames = frameTime;
        this.looping = looping;
    }

    /**
     * Creates an animation. repeats forever
     *
     * @param animationFrames The list of textures that builds the animation. Must be in order.
     * @param frameTime How many frames to wait, before switching to the next texture?
     */
    public Animation(Collection<String> animationFrames, int frameTime) {
        this(animationFrames, frameTime, true);
    }

    /**
     * Create an animation from the files in the given path and the given configuration.
     *
     * <p>Will sort the the textures in lexicographic order. This is the order in which the
     * animations will be shown.
     *
     * @param subDir Path to the subdirectory where the animation frames are stored
     * @param frameTime How many frames to wait, before switching to the next texture?
     * @param loop should the Animation continue to repeat ?
     * @return The created Animation instance
     */
    public static Animation of(File subDir, int frameTime, boolean loop) {
        List<String> fileNames =
                Arrays.stream(Objects.requireNonNull(subDir.listFiles()))
                        .filter(File::isFile)
                        .map(File::getName)
                        .collect(Collectors.toList());

        // sort the files in lexicographic order (like the most os)
        // animations will be played in order
        Collections.sort(fileNames);
        return new Animation(fileNames, frameTime, loop);
    }

    /**
     * Create an animation from the files in the given path and the default configuration.
     *
     * <p>Will sort the the textures in lexicographic order. This is the order in which the
     * animations will be shown.
     *
     * @param subDir Path to the subdirectory where the animation frames are stored
     * @return The created Animation instance
     */
    public static Animation of(File subDir) {
        return Animation.of(subDir, DEFAULT_FRAME_TIME, DEFAULT_IS_LOOP);
    }

    public static Animation of(List<String> fileNamesRelativeToResources) {
        return Animation.of(fileNamesRelativeToResources, DEFAULT_FRAME_TIME, DEFAULT_IS_LOOP);
    }

    public static Animation of(List<String> fileNamesRelativeToResources, int frameTime, boolean loop) {
        return new Animation(fileNamesRelativeToResources, frameTime, loop);
    }

    /**
     * Automatically updates currentFrame to next frame.
     *
     * @return The texture of the next animation step (draw this).
     */
    public String nextAnimationTexturePath() {
        if (isFinished()) {
            return animationFrames.get(currentFrameIndex);
        }
        String stringToReturn = animationFrames.get(currentFrameIndex);
        frameTimeCounter = (frameTimeCounter + 1) % timeBetweenFrames;
        if (frameTimeCounter == 0) {
            currentFrameIndex = (currentFrameIndex + 1) % frames;
        }
        return stringToReturn;
    }

    /**
     * @return true when last frame and is not looping, otherwise false
     */
    public boolean isFinished() {
        return !looping && currentFrameIndex == frames - 1;
    }

    /**
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
    public List<String> getAnimationFrames() {
        return animationFrames;
    }

    /**
     * Set the time (in frames) between two animation frames.
     *
     * @param timeBetweenFrames Time before switching to the next animation frame.
     */
    public void setTimeBetweenFrames(int timeBetweenFrames) {
        this.timeBetweenFrames = timeBetweenFrames;
    }

    public int timeBetweenFrames() {
        return timeBetweenFrames;
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
    public void setLoop(boolean loop) {
        this.looping = loop;
    }
}
