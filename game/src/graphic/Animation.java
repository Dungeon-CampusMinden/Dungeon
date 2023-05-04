package graphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A list of textures from an animation. */
public class Animation {

    /** The set of textures that build the animation. */
    protected final List<String> animationFrames;

    /** The count of textures for the animation. */
    protected final int frames;

    /** Number of frames between switching to the next animation? */
    private final int frameTime;

    /** Index of the NEXT texture that will be returned. */
    protected int currentFrameIndex = 0;

    /** How many frames since the last texture switching? */
    private int frameTimeCounter = 0;

    protected boolean looping;

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
     * Get the List of animation frames.
     *
     * @return List containing the paths of the single frames of the animation.
     */
    public List<String> getAnimationFrames() {
        return animationFrames;
    }
}
