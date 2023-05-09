package api.systems;

import api.System;
import api.components.AnimationComponent;
import api.components.MissingComponentException;
import api.components.PositionComponent;
import api.Entity;
import content.utils.animation.Animation;
import api.utils.Painter;
import trashcan.PainterConfig;
import java.util.HashMap;
import java.util.Map;
import starter.Game;

/** used to draw entities */
public final class DrawSystem extends System {

    private Painter painter;
    private Map<String, PainterConfig> configs;

    private record DSData(Entity e, AnimationComponent ac, PositionComponent pc) {}

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
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(AnimationComponent.class).stream())
                .map(ac -> buildDataObject((AnimationComponent) ac))
                .forEach(this::draw);
    }

    private void draw(DSData dsd) {
        final Animation animation = dsd.ac.getCurrentAnimation();
        String currentAnimationTexture = animation.getNextAnimationTexturePath();
        if (!configs.containsKey(currentAnimationTexture)) {
            configs.put(currentAnimationTexture, new PainterConfig(currentAnimationTexture));
        }
        painter.draw(
                dsd.pc.getPosition(),
                currentAnimationTexture,
                configs.get(currentAnimationTexture));
    }

    private DSData buildDataObject(AnimationComponent ac) {
        Entity e = ac.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class).orElseThrow(DrawSystem::missingPC);

        return new DSData(e, ac, pc);
    }

    @Override
    public void toggleRun() {
        // DrawSystem cant pause
        run = true;
    }

    private static MissingComponentException missingPC() {
        return new MissingComponentException("PositionComponent");
    }
}
