package contrib.components;

import core.Component;

/**
 * Marks an entity as eligible for transport/teleport mechanics.
 *
 * p>It is used in PortalCollisionHandler to filter entities that have this component.
 * Only those entities will be teleported.
 */
public record TransportableComponent() implements Component {}
