package character.player;

import character.DungeonCharacter;
import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import graphic.Animation;
import java.util.List;
import level.elements.ILevel;
import textures.TextureHandler;

/** Player-Character. */
public class Hero extends DungeonCharacter {

    private final Animation IDLE_ANIMATION;
    private final Animation RUN_LEFT_ANIMATION;
    private final Animation RUN_RIGHT_ANIMATION;

    public Hero() {
        // 16x28
        super(0.3f, new Hitbox(6, 6));
        int frameTime = 5;
        List<String> texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_f");
        Animation animation = new Animation(texturePaths, frameTime * 2);
        IDLE_ANIMATION = animation;

        texturePaths = TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_RIGHT_ANIMATION = animation;

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_mirrored_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_LEFT_ANIMATION = animation;

        currentAnimation = IDLE_ANIMATION;
    }

    @Override
    /** Movement based on Key-Inputs */
    protected CharacterDirection getDirection() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) return CharacterDirection.UP;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) return CharacterDirection.DOWN;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) return CharacterDirection.RIGHT;
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) return CharacterDirection.LEFT;
        else return CharacterDirection.NONE;
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
        currentPosition = level.getStartTile().getCoordinate().toPoint();
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {
        // todo
    }
}
