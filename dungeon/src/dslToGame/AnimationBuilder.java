package dslToGame;

import core.Game;
import core.utils.components.draw.Animation;

import semanticAnalysis.types.DSLTypeAdapter;

public class AnimationBuilder {
    public static int frameTime = 5;

    @DSLTypeAdapter(t = Animation.class)
    public static Animation buildAnimation(String path) {
        return new Animation(Game.getHandler().getTexturePaths(path), frameTime);
    }
}
