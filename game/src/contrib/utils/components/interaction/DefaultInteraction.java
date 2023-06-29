package contrib.utils.components.interaction;

import core.Entity;

public class DefaultInteraction implements IInteraction{
    @Override
    public void onInteraction(Entity entity) {
        System.out.println(entity.id() + " did use the DefaultInteraction");
    }
}
