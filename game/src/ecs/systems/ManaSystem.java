package ecs.systems;

import ecs.components.ManaComponent;
import starter.Game;

public class ManaSystem extends ECS_System {

    @Override
    public void update() {
        Game.getEntities().stream()
                // Considers only entities that have ManaComponents
                .filter(e -> e.getComponent(ManaComponent.class).isPresent())
                // Gets the ManaComponents as components
                .flatMap(e -> e.getComponent(ManaComponent.class).stream())
                // Casts the components to ManaComponents
                .map(c -> (ManaComponent) c)
                // Regenerates the components
                .forEach(mc -> mc.regenerate());
    }

}
