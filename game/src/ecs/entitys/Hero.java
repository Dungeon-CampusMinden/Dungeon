package ecs.entitys;

import ecs.components.AnimationComponent;
import ecs.components.AnimationList;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import graphic.Animation;
import java.util.List;
import textures.TextureHandler;
import tools.Point;

public class Hero extends Entity {

    private PositionComponent positionComponent;
    /**
     * Entity with Components
     *
     * @param startPosition position at start
     */
    public Hero(Point startPosition) {
        super();
        positionComponent = new PositionComponent(this, startPosition);
        new VelocityComponent(this, 0, 0, 0.3f, 0.3f);
        setupAnimationComponent();
    }

    private void setupAnimationComponent() {
        int frameTime = 5;
        AnimationList animations = new AnimationList();
        List<String> texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_f");

        animations.setIdleRight(new Animation(texturePaths, frameTime * 2));

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_mirrored_f");
        animations.setIdleLeft(new Animation(texturePaths, frameTime * 2));

        texturePaths = TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_f\\d+");
        animations.setMoveRight(new Animation(texturePaths, frameTime));

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_mirrored_f\\d+");
        animations.setMoveLeft(new Animation(texturePaths, frameTime));

        new AnimationComponent(this, animations, animations.getIdleLeft());
    }

    /**
     * @return position of hero
     */
    public PositionComponent getPositionComponent() {
        return positionComponent;
    }
}
