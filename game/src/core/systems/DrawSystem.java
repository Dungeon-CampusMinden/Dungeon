package core.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.IPath;
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

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    private static final SpriteBatch batch = new SpriteBatch();

    /** Draws objects */
    private static final Painter painter = new Painter(batch);

    private final Map<String, PainterConfig> configs;

    /**
     * Create a new DrawSystem to draw entities.
     *
     * @see Painter
     */
    public DrawSystem() {
        super(DrawComponent.class, PositionComponent.class);
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
        entityStream().map(this::buildDataObject).forEach(this::draw);
    }

    private void draw(DSData dsd) {
        setNextAnimation(dsd.dc);
        final Animation animation = dsd.dc.currentAnimation();
        String currentAnimationTexture = animation.nextAnimationTexturePath();
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

    // checks the status of animations in the animationQueue and selects the next animation by
    // priority
    public void setNextAnimation(DrawComponent dc) {

        IPath highestPrio = null;

        // iterate through animationQueue
        for (Map.Entry<IPath[], Integer> animationArr : dc.animationQueue().entrySet()) {
            // subtract 1 from every frametimer, if value below zero, remove animation from
            // queue
            animationArr.setValue(animationArr.getValue() - 1);
            if (animationArr.getValue() < 0) {
                dc.animationQueue().remove(animationArr.getKey());
                break;
            }

            // if animation has frametime left, check if it's the highest priority
            // then generate the first valid Animation from that array
            for (IPath animationPath : animationArr.getKey()) {
                if (highestPrio == null
                    || highestPrio.priority() < animationPath.priority()) {
                    highestPrio = animationPath;
                    dc.animationMap().get(animationPath.pathString());
                }
            }
            dc.currentAnimation(highestPrio);
        }
    }

    private record DSData(Entity e, DrawComponent dc, PositionComponent pc) {}

    /**
     * @return the {@link #painter} of the Drawsystem
     */
    public static Painter painter() {
        return painter;
    }

    /**
     * @return the {@link #batch} of the Drawsystem
     */
    public static SpriteBatch batch() {
        return batch;
    }
}
