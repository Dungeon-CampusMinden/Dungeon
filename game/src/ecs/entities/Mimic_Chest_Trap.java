package ecs.entities;

import ecs.components.*;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.items.ItemData;
import ecs.items.ItemDataGenerator;
import graphic.Animation;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import level.tools.LevelElement;
import starter.Game;
import tools.Point;
import java.util.stream.Collectors;

/** A trap, that looks like a chest, but damages the hero a soon as he interacts with it.  */
public class Mimic_Chest_Trap extends Trap{

    private static final float defaultInteractionRadius = 1f;
    private static final int frame_time_idle = 1;
    private static final int frame_time_triggered = 1;
    private static final List<String> DEFAULT_IDLE_ANIMATION_FRAMES =
          List.of("objects/mimicchest/mimic_chest_full_open_anim_f0.png");
    private static final List<String> DEFAULT_TRIGGERED_ANIMATION_FRAMES =
            List.of(
                    "objects/mimicchest/mimic_chest_full_open_anim_f0.png",
                    "objects/mimicchest/mimic_chest_full_open_anim_f1.png",
                    "objects/mimicchest/mimic_chest_full_open_anim_f2.png");

    /**
     * Creates a Mimicchest at a random position
     *
     * @return a configured MimicChest
     */
    public static Mimic_Chest_Trap createNewMimicChest() {
        return new Mimic_Chest_Trap(
                Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    /**
     * Creates a new MimicChest which hurts the hero on interaction
     *
     * @param position the position where the chest is placed
     */
    public Mimic_Chest_Trap(Point position) {
        super(
                frame_time_idle,
                frame_time_triggered,
                DEFAULT_IDLE_ANIMATION_FRAMES,
                DEFAULT_TRIGGERED_ANIMATION_FRAMES,
                false,
                position
            );

        new InteractionComponent(this, defaultInteractionRadius, false, this::onTrigger);
    }

    private void onTrigger(Entity entity) {
        // TODO: Remove this
        Logger logger = Logger.getLogger("Mimic_Chest_Trap");
        logger.info("Mimic_Chest_Trap triggered" + entity.toString());

        Game.getHero().stream()
            .flatMap(e -> e.getComponent(HealthComponent.class).stream())
            .map(HealthComponent.class::cast)
            .forEach(healthComponent -> {
                healthComponent.receiveHit(new Damage(1, DamageType.PHYSICAL, this));
            });
                

        entity.getComponent(AnimationComponent.class)
            .map(AnimationComponent.class::cast)
            .ifPresent(x -> x.setCurrentAnimation(x.getIdleRight()));
    }

    /**
     * Helper to create a MissingComponentException with a bit more information
     *
     * @param Component the name of the Component which is missing
     * @param e the Entity which did miss the Component
     * @return the newly created Exception
     */
    private static MissingComponentException createMissingComponentException(
            String Component, Entity e) {
        return new MissingComponentException(
                Component
                        + " missing in "
                        + Mimic_Chest_Trap.class.getName()
                        + " in Entity "
                        + e.getClass().getName());
    }
}