package graphic;

import java.util.Collection;

public class TerminatingAnimation extends Animation {
    /**
     * Creates an animation. Which does not reapeaet from the start once the list is iterated
     *
     * @param animationFrames The list of textures that builds the animation. Must be in order.
     * @param frameTime How many frames to wait, before switching to the next texture?
     */
    public TerminatingAnimation(Collection<String> animationFrames, int frameTime) {
        super(animationFrames, frameTime);
    }

    /**
     * @return texture path of the current animation
     */
    @Override
    public String getNextAnimationTexturePath() {
        if (currentFrameIndex == frames - 1) {
            return animationFrames.get(currentFrameIndex);
        }
        return super.getNextAnimationTexturePath();
    }
}
