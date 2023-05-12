package api.systems;

import api.Entity;
import api.Game;
import api.System;
import api.components.DrawComponent;
import api.components.PositionComponent;
import api.utils.component_utils.MissingComponentException;
import api.utils.component_utils.animationComponent.Animation;
import api.utils.component_utils.animationComponent.Painter;
import api.utils.component_utils.animationComponent.PainterConfig;
import java.util.HashMap;
import java.util.Map;

/** used to draw entities */
public class DrawSystem extends System {

    private Painter painter;
    private Map<String, PainterConfig> configs;

    private record DSData(Entity e, DrawComponent ac, PositionComponent pc) {}

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
                .flatMap(e -> e.getComponent(DrawComponent.class).stream())
                .map(ac -> buildDataObject((DrawComponent) ac))
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

    private DSData buildDataObject(DrawComponent ac) {
        Entity e = ac.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class).orElseThrow(DrawSystem::missingPC);

        return new DSData(e, ac, pc);
    }

    /** DrawSystem cant be paused */
    @Override
    public void toggleRun() {
        // DrawSystem cant pause
        run = true;
    }

    /** DrawSystem cant be paused */
    @Override
    public void stop() {
        // DrawSystem cant pause
        run = true;
    }

    private static MissingComponentException missingPC() {
        return new MissingComponentException("PositionComponent");
    }
}
