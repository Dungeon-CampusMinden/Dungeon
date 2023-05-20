package contrib.utils.components.health;

import core.Entity;

/**
 * Interface for definition of death behaviour.
 *
 * <p>This interface is used to define a function that is performed when an entity dies. An
 * implementation of this interface can be passed to the {@link contrib.components.HealthComponent}
 * to define the behaviour of an entity when it dies. The {@link contrib.systems.HealthSystem} will
 * call the function when the entity dies.
 */
public interface IOnDeathFunction {

    /**
     * Definition of the behaviour that is performed when an entity dies.
     *
     * @param entity Entity that has died
     */
    void onDeath(Entity entity);
}
