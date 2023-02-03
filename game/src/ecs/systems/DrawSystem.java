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
            entity.getComponent(AnimationComponent.class)
                    .ifPresent(
                            ac -> {
                                final Animation animation =
                                        ((AnimationComponent) ac).getCurrentAnimation();
                                PositionComponent positionComponent =
                                        (PositionComponent)
                                                entity.getComponent(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        new MissingComponentException(
                                                                                "PositionComponent"));
                                ;

                                String currentAnimationTexture =
                                        animation.getNextAnimationTexturePath();
                                if (!configs.containsKey(currentAnimationTexture)) {
                                    configs.put(
                                            currentAnimationTexture,
                                            new PainterConfig(currentAnimationTexture));
                                }
                                painter.draw(
                                        positionComponent.getPosition(),
                                        currentAnimationTexture,
                                        configs.get(currentAnimationTexture));
                            });
        }
    }

    @Override
    public void toggleRun() {
        // DrawSystem cant pause
        run = true;
    }
}
