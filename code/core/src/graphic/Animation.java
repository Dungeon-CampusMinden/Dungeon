package graphic;

import com.badlogic.gdx.graphics.Texture;

import java.util.List;

/** A list of textures from an animation. */
public class Animation {

    /** The set of textures that build the animation. */
    private final List<Texture> animationFrames;

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
    public Animation(List<Texture> animationFrames, int frameTime) {
        if (animationFrames.isEmpty()) {
            throw new IllegalArgumentException("An animation must have at least 1 frame");
        }
        if (frameTime < 0) {
            throw new IllegalArgumentException("frameTime cant be lower than 0");
        }
        this.animationFrames = animationFrames;
        frames = animationFrames.size();
        this.frameTime = frameTime;
    }

    /**
     * Automatically updates currentFrame to next frame.
     *
     * @return The texture of the next animation step (draw this).
     */
    public Texture getNextAnimationTexture() {
        int returnFrame = currentFrameIndex;
        // is it time to switch frame?
        if (frameTimeCounter == frameTime) {
            // after the last frame is returned, go back to the first frame
            currentFrameIndex = (currentFrameIndex + 1) % frames;
            frameTimeCounter = 0;
        } else {
            frameTimeCounter++;
        }
        return animationFrames.get(returnFrame);
    }
}
