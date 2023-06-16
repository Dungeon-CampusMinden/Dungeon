package core.systems;

import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * This system draws the entities on the screen.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * <p>The system will get the current animation from the {@link DrawComponent} and will get the next
 * animation frame from the {@link Animation}, and then draw it on the current position stored in
 * the {@link PositionComponent}.
 *
 * <p>This system will not set the current animation. This must be done by other systems.
 *
 * <p>The DrawSystem can't be paused.
 *
 * @see DrawComponent
 * @see Animation
 */
public final class DrawSystem extends System {

    private final Painter painter;
    private final Map<String, PainterConfig> configs;

    /**
     * Create a new DrawSystem to draw entities.
     *
     * @param painter The Painter to use for drawing.
     * @see Painter
     */
    public DrawSystem(Painter painter) {
        super(DrawComponent.class, PositionComponent.class);
        this.painter = painter;
        configs = new HashMap<>();
    }

    /**
     * Will draw entities at their position with their current animation.
     *
     * @see DrawComponent
     * @see Animation
     */
    @Override
    public void execute() {
        getEntityStream().map(this::buildDataObject).forEach(this::draw);
    }

    private void draw(DSData dsd) {
        final Animation animation = dsd.ac.getCurrentAnimation();
        String currentAnimationTexture = animation.getNextAnimationTexturePath();
        if (!configs.containsKey(currentAnimationTexture)) {
            configs.put(currentAnimationTexture, new PainterConfig(currentAnimationTexture));
        }
        painter.draw(
                dsd.pc.position(), currentAnimationTexture, configs.get(currentAnimationTexture));
    }

    private DSData buildDataObject(Entity e) {
        DrawComponent dc =
                e.fetch(DrawComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(e, DrawComponent.class));
        PositionComponent pc =
                e.fetch(PositionComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(e, PositionComponent.class));
        return new DSData(e, dc, pc);
    }

    /** DrawSystem cant be paused */
    @Override
    public void stop() {
        // DrawSystem cant pause
        run = true;
    }

    private record DSData(Entity e, DrawComponent ac, PositionComponent pc) {}
}
