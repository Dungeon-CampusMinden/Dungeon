package ecs.systems;

import ecs.components.skill.ManaComponent;
import starter.Game;

public class ManaSystem extends ECS_System{

    /** regenerates all mana */
    @Override
    public void update(){
        Game.getEntities().stream()
            // Consider only entities that have a ManaComponent
            .flatMap(e -> e.getComponent(ManaComponent.class).stream())
            .forEach(mc -> ((ManaComponent) mc).regenerate());
    }
}
