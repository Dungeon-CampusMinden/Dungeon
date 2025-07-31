package core.components;

import core.Component;

/**
 * Component representing the mass of an entity.
 *
 * <p>The mass value is used in physics calculations, such as determining acceleration from applied
 * forces.
 *
 * @param mass mass of the entity to store in the component
 * @see core.systems.VelocitySystem
 */
public record MassComponent(float mass) implements Component {}
