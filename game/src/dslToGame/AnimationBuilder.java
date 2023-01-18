package dslToGame;

import graphic.Animation;
import semanticAnalysis.types.DSLTypeAdapter;
import semanticAnalysis.types.IDSLTypeAdapter;
import textures.TextureHandler;


public class AnimationBuilder {
    public static int frameTime = 5;

    @DSLTypeAdapter(t=Animation.class)
    public static Animation buildAnimation(String path) {
        return new Animation(TextureHandler.getInstance().getTexturePaths(path), frameTime);
    }
}
