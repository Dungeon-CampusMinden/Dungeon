package game.src.ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.IInteraction;
import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import game.src.ecs.components.Traps.ITrigger;
import game.src.ecs.components.Traps.Summon;
import game.src.ecs.components.Traps.Teleportation;
import graphic.Animation;
import tools.Constants;
import java.lang.Math;

public class SummoningTrap extends Trap {

    // private final String pathToIdle = "traps/summon/idle";
    // private final String pathToTriggered = "traps/summon/triggered";
    // private final String pathToPostTriggered = "traps/summon/postTriggered";

    private final String pathToIdle = "traps/summon/idle";
    private final String pathToTriggered = "traps/summon/triggered";
    private final String pathToPostTriggered = "traps/summon/postTriggered";

    private boolean active = true;

    private ITrigger trigger;

    public SummoningTrap() {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
        Class klass;
        int r = (int) (Math.random() * 3);
        if (r == 0) klass = Summon.IMP;
        else if (r == 1) klass = Summon.CHORT;
        else klass = Summon.DARKKNIGHT;
        trigger = new Summon(klass);
    }

    public SummoningTrap(Class klass) {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
        trigger = new Summon(klass);
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
