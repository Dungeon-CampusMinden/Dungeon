package core;

/**
 * Components store the data (or attributes) for an associated {@link Entity}.
 *
 * <p>This class is the abstract base class for each component.
 *
 * <p>Each component is linked to exactly one entity. Use {@link #entity} to get the associated
 * entity of the component.
 *
 * <p>Each component will automatically add itself to the associated entity using {@link
 * Entity#addComponent}.
 *
 * <p>Components are used to describe an entity. {@link System}s will check the components of an
 * entity and decide if they want to process the entity. The systems will then modify the values of
 * the data stored in the components.
 *
 * <p>Remember that an entity can only store one component of each component class.
 */
public interface Component {}
