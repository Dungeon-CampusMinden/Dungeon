package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.BunchOfKeysComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import starter.Game;

import java.util.logging.Logger;

/**
 * Key is used to unlock a Chest
 * <p>
 * Each Key you can only use at one Chest and every belongs only to one Chest.
 */
public class Key extends Entity{
    private final String pathToIdle = "items/key";
    private transient final Logger keyLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new Key
     * <p>
     * Each Key you can only use at one Chest and every belongs only to one Chest.
     */
    public Key(){
        super();
        setupPositionComponent();
        setupAnimationComponent();
        setupHitBoxComponent();
        keyLogger.info("New " + this.getClass().getName() + " was created");
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }
    private void setupAnimationComponent(){
        new AnimationComponent(this, AnimationBuilder.buildAnimation(pathToIdle));
    }

    private void setupHitBoxComponent(){
        new HitboxComponent(
            this,
            (you, other, direction) -> collect(other),
            (you, other, direction) -> {});
    }

    private void collect(Entity entity){
        if(entity.getComponent(BunchOfKeysComponent.class).isEmpty())
            return;
        BunchOfKeysComponent bunchOffKeysComponent = (BunchOfKeysComponent) Game.getHero().get().getComponent(BunchOfKeysComponent.class).get();
        bunchOffKeysComponent.addKey(this);
        keyLogger.info(entity + " collected key");
    }
}
