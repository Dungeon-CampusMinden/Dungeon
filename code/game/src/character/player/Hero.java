package character.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import graphic.Animation;
import java.util.List;
import level.elements.ILevel;
import myDungeon.character.Character;
import myDungeon.character.monster.Monster;
import myDungeon.collision.Colideable;
import myDungeon.collision.Hitbox;
import textures.TextureHandler;

public class Hero extends Character {

    private final Animation IDLE_ANIMATION;
    private final Animation RUN_LEFT_ANIMATION;
    private final Animation RUN_RIGHT_ANIMATION;

    public Hero() {
        // 16x28
        super(0.3f, new Hitbox(6, 6,null));
        int frameTime = 5;
        List<String> texturePaths =
            TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_f");
        Animation animation = new Animation(texturePaths, frameTime * 2);
        IDLE_ANIMATION = animation;

        texturePaths =
            TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_mirrored_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_RIGHT_ANIMATION = animation;

        texturePaths = TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_f\\d+");
        animation = new Animation(texturePaths, frameTime);
        RUN_LEFT_ANIMATION = animation;

        currentAnimation = IDLE_ANIMATION;
    }

    @Override
    protected Direction getDirection() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) return Direction.UP;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) return Direction.DOWN;
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) return Direction.RIGHT;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) return Direction.LEFT;
        else return Direction.NONE;
    }

    @Override
    protected void setAnimation(Direction direction) {
        if (direction == Direction.LEFT) currentAnimation = RUN_LEFT_ANIMATION;
        else if (direction == Direction.RIGHT) currentAnimation = RUN_RIGHT_ANIMATION;
        else currentAnimation = IDLE_ANIMATION;
    }

    @Override
    public void setLevel(ILevel level) {
        currentLevel = level;
        currentPosition = level.getStartTile().getCoordinate().toPoint();
    }

    @Override
    public void colide(Colideable other, Direction from) {
        if (other instanceof Monster) System.out.println("Colide with monster from " + from.name());
        else System.out.println("Colide with other from " + from.name());
    }
}
