package ecs.components.Traps;

import ecs.entities.Entity;

/** 
 * Interface used by traps to activate
 * One could probably use this for some cool stuff but not me
 */
public interface ITrigger {
    
    void trigger(Entity entity);

}
