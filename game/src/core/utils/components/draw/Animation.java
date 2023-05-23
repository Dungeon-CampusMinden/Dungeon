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
 * <p>An Animation can have different configurations. Use {@link #setAnimationFrames} to set the
 * time between two frames. Use {@link #setLoop} to define if the Animation stops at the last frame
 * or should loop (starts from the beginning, this is the default setting).
 *
 * @see core.components.DrawComponent
 * @see IAnimationPathEnum
 */
public final class Animation {

    /** The set of textures that build the animation. */
    private final List<String> animationFrames;

    /** The count of textures for the animation. */
    private final int frames;

    /** Number of frames between switching to the next animation? */
    private int frameTime;

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
        this.frameTime = frameTime;
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
     * Create an animation from the files in the given path.
     *
     * @param subDir Path to the subdirectory where the animation frames are stored
     * @param frameTime Time between two animation frames
     * @param looping Should the animation loop or stop at the last frame?
     * @return The created Animation instance
     */
    public static Animation of(File subDir, int frameTime, boolean looping) {
        Set<String> fileNames =
                Arrays.stream(subDir.listFiles())
                        .filter(File::isFile)
                        .map(File::getPath)
                        .collect(Collectors.toSet());
        return new Animation(fileNames, frameTime, looping);
    }

    /**
     * Automatically updates currentFrame to next frame.
     *
     * @return The texture of the next animation step (draw this).
     */
    public String getNextAnimationTexturePath() {
        if (isFinished()) {
            return animationFrames.get(currentFrameIndex);
        }
        String stringToReturn = animationFrames.get(currentFrameIndex);
        frameTimeCounter = (frameTimeCounter + 1) % frameTime;
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
     * @param frameTime Time before switching to the next animation frame.
     */
    public void setAnimationFrames(int frameTime) {
        this.frameTime = frameTime;
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
