package ecs.entities;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import graphic.Animation;
import java.util.List;
import textures.TextureHandler;
import tools.Point;

public class Monster extends Entity {

    public Monster(Point startPosition) {
        super();
        this.addComponent(PositionComponent.name, new PositionComponent(this, startPosition));
        this.addComponent(AIComponent.name, new AIComponent(this));
        setupAnimationComponent();
    }

    private void setupAnimationComponent() {

        int frameTime = 5;
        List<String> texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_f\\d+");

        Animation idleRight = new Animation(texturePaths, frameTime * 2);

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_idle_anim_mirrored_f\\d+");
        Animation idleLeft = new Animation(texturePaths, frameTime * 2);

        texturePaths = TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_f\\d+");
        Animation moveRight = new Animation(texturePaths, frameTime);

        texturePaths =
                TextureHandler.getInstance().getTexturePaths("knight_m_run_anim_mirrored_f\\d+");
        Animation moveLeft = new Animation(texturePaths, frameTime);

        this.addComponent(
                AnimationComponent.name, new AnimationComponent(this, idleLeft, idleRight));

        this.addComponent(
                VelocityComponent.name,
                new VelocityComponent(this, 0, 0, 0.01f, 0.01f, moveLeft, moveRight));
    }
}
