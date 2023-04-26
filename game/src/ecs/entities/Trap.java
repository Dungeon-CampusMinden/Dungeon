package game.src.ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.entities.Entity;
import game.src.ecs.components.Traps.ITrigger;
import graphic.Animation;

public class Trap extends Entity {

    private final String pathToIdle = "";
    private final String pathToTriggered = "";
    private final String pathToPostTriggered = "";

    private boolean active = true;

    private ITrigger trigger = null;

    public Trap() {
        super();
    }

    public void trigger(Entity entity) {
        //
    }

}
