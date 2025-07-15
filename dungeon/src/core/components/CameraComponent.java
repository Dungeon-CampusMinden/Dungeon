package core.components;

import core.Component;

/**
 * Marks an entity as the point of focus for the camera.
 *
 * <p>The {@link core.systems.CameraSystem} will follow the associated entity and will keep the
 * entity in the center of the game window.
 *
 * <p>Note: The associated entity also needs a {@link PositionComponent} for the {@link
 * core.systems.CameraSystem} to work.
 *
 * <p>Note: If there is more than one CameraComponent, i.e. if more than one entity is attached to a
 * CameraComponent, the behaviour is undefined.
 */
public final class CameraComponent implements Component {}
