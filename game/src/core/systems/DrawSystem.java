package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;

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
