package observer;

import core.Component;
import java.util.function.Supplier;

/**
 * Represents a component that can be observed by {@link ObserverComponent}s in an ECS system.
 *
 * <p>This component holds a {@link Supplier Boolean } which is evaluated each tick to determine
 * whether observers should be notified during the next execution of the {@link ObserverSystem}.
 *
 * @param shouldNotify a supplier returning true if observers should be notified, false otherwise
 */
public record ObservableComponent(Supplier<Boolean> shouldNotify) implements Component {}
