package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.entities.Entity;
import ecs.components.Traps.ITrigger;
import graphic.Animation;

/**
 * Class that helps me remember what traps need
 * But otherwise its mostly useless
 */
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
