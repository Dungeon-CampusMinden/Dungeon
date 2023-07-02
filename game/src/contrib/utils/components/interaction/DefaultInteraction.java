package contrib.utils.components.interaction;

import core.Entity;

import java.util.function.Consumer;

public class DefaultInteraction implements Consumer<Entity> {

    @Override
    public void accept(Entity entity) {
        System.out.println(entity.id() + " did use the DefaultInteraction");
    }
}
