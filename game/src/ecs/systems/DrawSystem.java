package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import graphic.Painter;
import graphic.PainterConfig;
import java.util.HashMap;
import java.util.Map;
import mydungeon.ECS;

/** used to draw entities */
public class DrawSystem extends ECS_System {

    private Painter painter;
    private Map<String, PainterConfig> configs;

    /**
     * @param painter PM-Dungeon painter to draw
     */
    public DrawSystem(Painter painter) {
        super();
        this.painter = painter;
        configs = new HashMap<>();
    }

    /** draw entities at their position */
    public void update() {
        for (Entity entity : ECS.entities) {
            AnimationComponent ac =
                    (AnimationComponent)
                            entity.getComponent(AnimationComponent.name)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "AnimationComponent"));
            if (ac != null) {
                Animation animation = ac.getCurrentAnimation();
                PositionComponent positionComponent =
                        (PositionComponent)
                                entity.getComponent(PositionComponent.name)
                                        .orElseThrow(
                                                () ->
                                                        new MissingComponentException(
                                                                "PositionComponent"));
                ;

                if (positionComponent != null) {
                    if (animation != null) {
                        String currentAnimationTexture = animation.getNextAnimationTexturePath();
                        if (!configs.containsKey(currentAnimationTexture)) {
                            configs.put(
                                    currentAnimationTexture,
                                    new PainterConfig(currentAnimationTexture));
                        }
                        painter.draw(
                                positionComponent.getPosition(),
                                currentAnimationTexture,
                                configs.get(currentAnimationTexture));
                    }
                }
            }
        }
    }

    @Override
    public void toggleRun() {
        // DrawSystem cant pause
        run = true;
    }
}
