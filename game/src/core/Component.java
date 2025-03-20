package core;

/**
 * Components store the data (or attributes) for an associated {@link Entity}.
 *
 * <p>This interface needs to be implemented by each component.
 *
 * <p>Each component can be linked to zero to n entities. Use {@link Entity#add(Component)} to
 * register a component at an entity.
 *
 * <p>Components are used to describe an entity. {@link System}s will check the components of an
 * entity and decide if they want to process the entity. The systems will then modify the values of
 * the data stored in the components.
 *
 * <p>Remember that an entity can only store one component of each component class.
 */
public interface Component {}
