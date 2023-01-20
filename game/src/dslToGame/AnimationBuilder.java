package dslToGame;

import graphic.Animation;
import textures.TextureHandler;

public class AnimationBuilder {
    public static int frameTime = 5;

    public static Animation buildAnimation(String path) {
        return new Animation(TextureHandler.getInstance().getTexturePaths(path), frameTime);
    }
}
