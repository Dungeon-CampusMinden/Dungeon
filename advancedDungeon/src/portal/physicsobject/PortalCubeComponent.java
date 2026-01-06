package portal.physicsobject;

import core.Component;

/**
 * Marker component indicating that an entity represents a cube.
 *
 * <p>This component does not contain any data or behavior; it simply identifies the entity as a
 * cube for filtering and collision logic (e.g., activating cube-specific pressure plates).
 */
public record PortalCubeComponent() implements Component {}
