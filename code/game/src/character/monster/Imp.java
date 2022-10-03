package character.monster;

import graphic.Animation;
import java.util.List;
import level.elements.ILevel;
import myDungeon.collision.Colideable;
import myDungeon.collision.Hitbox;
import textures.TextureHandler;

public class Imp extends Monster {
    private final Animation IDLE_ANIMATION;
    private final Animation RUN_LEFT_ANIMATION;
    private final Animation RUN_RIGHT_ANIMATION;

    public Imp() {
        // 16x16
        super(0.03f, new Hitbox(5, 5,null));
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
    protected void setAnimation(Colideable.Direction direction) {
        if (direction == Colideable.Direction.LEFT) currentAnimation = RUN_LEFT_ANIMATION;
        else if (direction == Colideable.Direction.RIGHT) currentAnimation = RUN_RIGHT_ANIMATION;
        else currentAnimation = IDLE_ANIMATION;
    }

    @Override
    public void setLevel(ILevel level) {
        currentLevel = level;
        currentPosition = level.getStartTile().getCoordinate().toPoint();
    }

    @Override
    public void colide(Colideable other, Colideable.Direction from) {

    }
}
