package core.systems;

import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;

import java.util.HashMap;
import java.util.Map;

/** used to draw entities */
public class DrawSystem extends System {

    private final Painter painter;
    private final Map<String, PainterConfig> configs;

    private record DSData(Entity e, DrawComponent ac, PositionComponent pc) {}

    /**
     * @param painter PM-Dungeon painter to draw
     */
    public DrawSystem(Painter painter) {
        super();
        this.painter = painter;
        configs = new HashMap<>();
    }

    @Override
    protected boolean accept(Entity entity) {
        if (entity.getComponent(DrawComponent.class).isPresent())
            if (entity.getComponent(PositionComponent.class).isPresent()) return true;
            else logMissingComponent(entity, PositionComponent.class);
        return false;
    }

    /** draw entities at their position */
    public void systemUpdate() {
        getEntityStream().map(this::buildDataObject).forEach(this::draw);
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

    private DSData buildDataObject(Entity e) {

        DrawComponent dc = (DrawComponent) e.getComponent(DrawComponent.class).get();
        PositionComponent pc = (PositionComponent) e.getComponent(PositionComponent.class).get();

        return new DSData(e, dc, pc);
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
}
