package dslToGame;

import graphic.Animation;
import semanticAnalysis.types.DSLTypeAdapter;
import starter.Game;

public class AnimationBuilder {
    public static int frameTime = 5;

    @DSLTypeAdapter(t = Animation.class)
    public static Animation buildAnimation(String path) {
        return new Animation(Game.getHandler().getTexturePaths(path), frameTime);
    }
}
