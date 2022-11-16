package character.monster;

import collision.CharacterDirection;
import collision.Hitbox;
import graphic.Animation;
import java.util.List;
import level.elements.ILevel;
import level.tools.LevelElement;
import textures.TextureHandler;

public class Imp extends Monster {
    private final Animation IDLE_ANIMATION;
    private final Animation RUN_LEFT_ANIMATION;
    private final Animation RUN_RIGHT_ANIMATION;

    public Imp() {
        // 16x16
        super(2,0.03f, new Hitbox(5, 5));
        int frameTime = 5;
        List<String> texturePaths = TextureHandler.getInstance().getTexturePaths("imp_idle_anim_f");
        Animation animation = new Animation(texturePaths, frameTime * 2);
        IDLE_ANIMATION = animation;

        texturePaths = TextureHandler.getInstance().getTexturePaths("imp_run_anim_mirrored_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_RIGHT_ANIMATION = animation;

        texturePaths = TextureHandler.getInstance().getTexturePaths("imp_run_anim_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_LEFT_ANIMATION = animation;

        currentAnimation = IDLE_ANIMATION;
    }

    @Override
    protected void setAnimation(CharacterDirection direction) {
        if (direction == CharacterDirection.LEFT) currentAnimation = RUN_LEFT_ANIMATION;
        else if (direction == CharacterDirection.RIGHT) currentAnimation = RUN_RIGHT_ANIMATION;
        else currentAnimation = IDLE_ANIMATION;
    }

    @Override
    public void setLevel(ILevel level) {
        currentLevel = level;
        currentPosition = level.getRandomTilePoint(LevelElement.FLOOR);
    }
}
