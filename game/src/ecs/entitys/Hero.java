package ecs.entitys;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import graphic.Animation;
import java.util.ArrayList;
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
        List<Animation> animationList = new ArrayList<>();
        int frameTime = 5;
        List<String> texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_f");
        animationList.add(new Animation(texturePaths, frameTime * 2));

        texturePaths = TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_f\\d+");
        animationList.add(new Animation(texturePaths, frameTime));

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_mirrored_f\\d+");
        animationList.add(new Animation(texturePaths, frameTime));

        new AnimationComponent(this, animationList, animationList.get(0));
    }

    /**
     * @return position of hero
     */
    public PositionComponent getPositionComponent() {
        return positionComponent;
    }
}
