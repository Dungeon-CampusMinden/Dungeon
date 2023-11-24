package core.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.path.IPath;

import java.util.*;
import java.util.stream.Collectors;

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
        Map<Boolean, List<Entity>> partitionedEntities =
                entityStream()
                        .collect(
                                Collectors.partitioningBy(
                                        entity -> entity.isPresent(PlayerComponent.class)));

        List<Entity> players = partitionedEntities.get(true);
        List<Entity> npcs = partitionedEntities.get(false);

        npcs.forEach(entity -> draw(buildDataObject(entity)));
        players.forEach(entity -> draw(buildDataObject(entity)));
    }

    private void draw(DSData dsd) {
        reduceFrameTimer(dsd.dc);
        setNextAnimation(dsd.dc);
        final Animation animation = dsd.dc.currentAnimation();
        String currentAnimationTexture = animation.nextAnimationTexturePath();
        if (!configs.containsKey(currentAnimationTexture)) {
            configs.put(currentAnimationTexture, new PainterConfig(currentAnimationTexture));
        }
        painter.draw(
                dsd.pc.position(), currentAnimationTexture, configs.get(currentAnimationTexture));
    }

    private void reduceFrameTimer(DrawComponent dc) {

        // iterate through animationQueue
        for (Map.Entry<IPath, Integer> entry : dc.animationQueue().entrySet()) {
            // reduce remaining frame time of animation by 1
            entry.setValue(entry.getValue() - 1);
        }
        // remove animations when there is no remaining frame time
        dc.animationQueue().entrySet().removeIf(x -> x.getValue() < 0);
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
    private void setNextAnimation(DrawComponent dc) {

        Optional<Map.Entry<IPath, Integer>> highestfind =
                dc.animationQueue().entrySet().stream()
                        .max(Comparator.comparingInt(x -> x.getKey().priority()));

        // when there is an animation load it
        if (highestfind.isPresent()) {
            IPath highestPrio = highestfind.get().getKey();
            // making sure the animation exists
            dc.animationMap().get(highestPrio.pathString());
            // changing the Animation
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
