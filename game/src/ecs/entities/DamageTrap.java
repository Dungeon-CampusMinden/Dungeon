package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.IInteraction;
import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import game.src.ecs.components.Traps.Damage;
import game.src.ecs.components.Traps.ITrigger;
import game.src.ecs.components.Traps.Teleportation;
import graphic.Animation;
import tools.Constants;


/**
 * The DamageTrap is a Trap. It's entity in the ECS. This class helps to
 * setup damagetraps with all its components and attributes .
 */
public class DamageTrap extends Trap {

    private final String pathToIdle = "traps/damage/idle";
    private final String pathToTriggered = "traps/damage/triggered";
    private final String pathToPostTriggered = "traps/damage/postTriggered";

    private boolean active = true;

    private ITrigger trigger = new Damage();

    /** Entity with Components */
    public DamageTrap() {
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
                (you, other, direction) -> System.out.println("trapCollisionLeave"));
    }

    @Override
    public void trigger(Entity entity) {
        if (!active) return;
        Animation triggered = AnimationBuilder.buildAnimation(pathToTriggered);
        ((AnimationComponent) this.getComponent(AnimationComponent.class).get()).setCurrentAnimation(triggered);
        trigger.trigger(entity);
        active = !active;
        Animation postTriggered = AnimationBuilder.buildAnimation(pathToPostTriggered);
        ((AnimationComponent) this.getComponent(AnimationComponent.class).get()).setCurrentAnimation(postTriggered);
    }

}
