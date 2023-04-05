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
import starter.Game;

/**
 * A system used to draw entities on the screen based on their current animation and position. It
 * uses a Painter object to draw the entities and maintains a mapping of PainterConfig objects for
 * each texture used.
 */
public class DrawSystem extends ECS_System {

    private Painter painter;
    private Map<String, PainterConfig> configs;

    /**
     * Private record class used to store the entity, its animation component and its position
     * component in one object
     */
    private record DSData(Entity e, AnimationComponent ac, PositionComponent pc) {}

    /**
     * Constructor for DrawSystem
     *
     * @param painter the PM-Dungeon painter used to draw the entities
     */
    public DrawSystem(Painter painter) {
        super();
        this.painter = painter;
        configs = new HashMap<>();
    }

    /**
     * It loops through all the entities in the game and draws the animation component of each
     * entity that has it. It uses the current animation of the entity and draws it at the position
     * of the entity.
     */
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(AnimationComponent.class).stream())
                .map(ac -> buildDataObject((AnimationComponent) ac))
                .forEach(this::draw);
    }

    /**
     * Draws the current animation of the provided entity at the current location
     *
     * @param dsd object containing the entity, its animation component and its position component
     */
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

    /**
     * Builds a DSData object containing the entity, its animation component and its position
     * component
     *
     * @param ac the animation component of the entity
     * @return the DSData object containing the entity, its animation component and its position
     *     component
     */
    private DSData buildDataObject(AnimationComponent ac) {
        Entity e = ac.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class).orElseThrow(DrawSystem::missingPC);

        return new DSData(e, ac, pc);
    }

    /** It is not possible to pause the DrawSystem, so it always runs. */
    @Override
    public void toggleRun() {
        // DrawSystem cant pause
        run = true;
    }

    /**
     * Returns a new MissingComponentException for a missing PositionComponent
     *
     * @return the new MissingComponentException
     */
    private static MissingComponentException missingPC() {
        return new MissingComponentException("PositionComponent");
    }
}
