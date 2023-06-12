package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.IInteraction;
import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.components.Traps.ITrigger;
import ecs.components.Traps.Teleportation;
import graphic.Animation;
import tools.Constants;

/**
 * The TeleportationTrap is a "trap" or more like slight nuisance. It's entity
 * in the ECS. This class helps to
 * setup teleportationtraps with all its components and attributes .
 */
public class TeleportationTrap extends Trap {

    private final String pathToIdle = "traps/teleportation/idle";
    private final String pathToTriggered = "traps/teleportation/triggered";
    private final String pathToPostTriggered = "traps/teleportation/idle";

    private boolean active = true;

    private ITrigger trigger = new Teleportation();

    /** Entity with Components */
    public TeleportationTrap() {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
    }

    private void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(pathToIdle);
        new AnimationComponent(this, idle);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> trigger(other),
                (you, other, direction) -> {});
    }

    @Override
    public void trigger(Entity entity) {
        if (!active)
            return;
        Animation triggered = AnimationBuilder.buildAnimation(pathToTriggered);
        ((AnimationComponent) this.getComponent(AnimationComponent.class).get()).setCurrentAnimation(triggered);
        trigger.trigger(entity);
        Animation postTriggered = AnimationBuilder.buildAnimation(pathToPostTriggered);
        ((AnimationComponent) this.getComponent(AnimationComponent.class).get()).setCurrentAnimation(postTriggered);
    }

}
