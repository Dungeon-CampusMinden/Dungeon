package dslToGame;

import content.utils.animation.Animation;
import content.utils.animation.textures.TextureHandler;
import semanticAnalysis.types.DSLTypeAdapter;

public class AnimationBuilder {
    public static int frameTime = 5;

    @DSLTypeAdapter(t = Animation.class)
    public static Animation buildAnimation(String path) {
        return new Animation(TextureHandler.getInstance().getTexturePaths(path), frameTime);
    }
}
