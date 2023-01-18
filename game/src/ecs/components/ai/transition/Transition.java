package ecs.components.ai.transition;

import ecs.entities.Entity;

public interface Transition {

    public boolean goFightMode(Entity entity);
}
