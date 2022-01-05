package graphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A list of textures from an animation. */
public class Animation {

    /** The set of textures that build the animation. */
    private final List<String> animationFrames;

    /** The count of textures for the animation. */
    private final int frames;

    /** Number of frames between switching to the next animation? */
    private final int frameTime;

    /** Index of the NEXT texture that will be returned. */
    private int currentFrameIndex = 0;

    /** How many frames since the last texture switching? */
    private int frameTimeCounter = 0;

    /**
     * Creates an animation.
     *
     * @param animationFrames The list of textures that builds the animation. Must be in order.
     * @param frameTime How many frames to wait, before switching to the next texture?
     */
    public Animation(Collection<String> animationFrames, int frameTime) {
        assert (animationFrames != null && !animationFrames.isEmpty());
        assert (frameTime > 0);
        this.animationFrames = new ArrayList<>(animationFrames);
        frames = animationFrames.size();
        this.frameTime = frameTime;
    }

    /**
     * Automatically updates currentFrame to next frame.
     *
     * @return The texture of the next animation step (draw this).
     */
    public String getNextAnimationTexture() {
        String stringToReturn = animationFrames.get(currentFrameIndex);
        frameTimeCounter = (frameTimeCounter + 1) % frameTime;
        if (frameTimeCounter == 0) {
            currentFrameIndex = (currentFrameIndex + 1) % frames;
        }
        return stringToReturn;
    }
}
