package dslToGame;

import core.Entity;

import runtime.AggregateValue;
import runtime.IMemorySpace;

public class EntityTranslator implements IRuntimeObjectTranslator<Entity, AggregateValue> {
    @Override
    public AggregateValue translate(Entity object, IMemorySpace ms) {
        // get datatype for entity
        var entityType = ms.resolve("entity");

        // create aggregateValue
        /*new AggregateValue();*/

        return null;
    }
}
