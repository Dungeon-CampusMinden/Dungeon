package de.fwatermann.dungine.ecs;

/**
 * The `Component` class represents a base class for all components in the Entity-Component-System
 * (ECS) architecture. It provides functionality to determine if multiple components of the same
 * type can be attached to an entity and to get or set the entity to which the component is
 * attached.
 */
public abstract class Component {

  private final boolean multiple;
  private Entity entity;

  /**
   * Constructs a `Component` with the specified multiple flag.
   *
   * @param multiple whether an entity can have multiple components of this type
   */
  public Component(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * Returns whether an entity can have multiple components of this type.
   *
   * @return true if multiple components are allowed, false otherwise
   */
  public boolean canHaveMultiple() {
    return this.multiple;
  }

  /**
   * Sets the entity to which this component is attached.
   *
   * @param entity the entity to attach this component to
   */
  final void entity(Entity entity) {
    this.entity = entity;
  }

  /**
   * Returns the entity to which this component is attached.
   *
   * @return the entity to which this component is attached
   */
  public final Entity entity() {
    return this.entity;
  }
}
