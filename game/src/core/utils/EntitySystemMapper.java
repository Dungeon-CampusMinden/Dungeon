package core.utils;

import core.Component;
import core.Entity;
import core.System;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Maps {@link System}s with {@link Entity}s if the filter rules are fulfilled.
 *
 * <p>This class stores a collection of systems and entities and indicates that the entities meet
 * the criteria to be processed by the systems.
 *
 * <p>The filter criteria are stored as a collection of {@link Class}. If an entity implements all
 * components of this collection, it will be added to the internal list of entities.
 *
 * <p>Entities must be added using {@link #add(Entity)}. This function internally checks whether the
 * filter criteria are met. When an entity is added, the {@link System#triggerOnAdd(Entity)} method
 * is executed for all systems.
 *
 * <p>Use {@link #remove(Entity)} to remove an entity. When an entity is removed, {@link
 * System#triggerOnRemove(Entity)} is called for each system.
 *
 * <p>If an entity changes on the component level, {@link #update(Entity)} must be called. This
 * function checks whether the entity should be newly added or removed.
 *
 * <p>Systems can be added using {@link #add(System)} or removed using {@link #remove(System)}. When
 * a system is added, {@link System#triggerOnAdd(Entity)} is called for each stored entity.
 *
 * <p>You can query the stored entities as a stream using {@link #stream()}.
 *
 * <p>The {@link #equals(Object)} and {@link #equals(Set)} methods return true if the filter rules
 * are identical.
 */
public final class EntitySystemMapper {

  private final Set<Class<? extends Component>> filterRules;
  private final Set<Entity> entities;
  private final Set<System> systems;

  /**
   * Creates a new EntitySystemMapper with the given filter rules.
   *
   * @param filterRules The Set of Component classes that define the filter rules for the
   *     EntitySystemMapper.
   */
  public EntitySystemMapper(final Set<Class<? extends Component>> filterRules) {
    this.filterRules = filterRules;
    entities = new HashSet<>();
    systems = new HashSet<>();
  }

  /** Creates a new EntitySystemMapper with no filter rules. */
  public EntitySystemMapper() {
    filterRules = new HashSet<>();
    entities = new HashSet<>();
    systems = new HashSet<>();
  }

  /**
   * Adds a new System to the EntitySystemMapper.
   *
   * <p>The System will be associated with the Entities that meet the filter rules of the
   * EntitySystemMapper. If the System is successfully added, the {@link
   * System#triggerOnAdd(Entity)} method of the System will be called for each existing Entity that
   * fulfills the filter rules.
   *
   * @param system The System to be added to the EntitySystemMapper.
   * @return true if the System was added successfully, false if the System was already present in
   *     the EntitySystemMapper and not added again.
   */
  public boolean add(final System system) {
    if (systems.add(system)) {
      entities.forEach(system::triggerOnAdd);
      return true;
    }
    return false;
  }

  /**
   * Removes a System from the EntitySystemMapper.
   *
   * <p>The System will no longer be associated with any Entities in the EntitySystemMapper after
   * removal. If the System is successfully removed, the {@link System#triggerOnRemove(Entity)}
   * method of the System will be called for each existing Entity that was associated with the
   * removed System.
   *
   * @param system The System to be removed from the EntitySystemMapper.
   * @return true if the System was removed successfully, false if the System was not present in the
   *     EntitySystemMapper and no removal was performed.
   */
  public boolean remove(final System system) {
    if (systems.remove(system)) {
      entities.forEach(system::triggerOnRemove);
      return true;
    }
    return false;
  }

  /**
   * Adds a new Entity to the EntitySystemMapper.
   *
   * <p>The Entity will be added to the EntitySystemMapper if it is not already present, and it
   * fulfills the filter rules defined in the EntitySystemMapper. If the Entity is successfully
   * added, the {@link System#triggerOnAdd(Entity)} method of each associated System will be called
   * with the newly added Entity as the parameter.
   *
   * @param entity The Entity to be added to the EntitySystemMapper.
   * @return true if the Entity was added successfully, false if the Entity was already present or
   *     does not fulfill the filter rules.
   */
  public boolean add(final Entity entity) {
    if (!entities.contains(entity) && accept(entity)) {
      entities.add(entity);
      systems.forEach(system -> system.triggerOnAdd(entity));
      return true;
    }
    return false;
  }

  /**
   * Removes an Entity from the EntitySystemMapper.
   *
   * <p>The Entity will no longer be associated with any Systems in the EntitySystemMapper after
   * removal. If the Entity is successfully removed, the {@link System#triggerOnRemove(Entity)}
   * method of each associated System will be called with the removed Entity as the parameter.
   *
   * @param entity The Entity to be removed from the EntitySystemMapper.
   * @return true if the Entity was removed successfully, false if the Entity was not present in the
   *     EntitySystemMapper and no removal was performed.
   */
  public boolean remove(final Entity entity) {
    if (entities.contains(entity)) {
      entities.remove(entity);
      systems.forEach(system -> system.triggerOnRemove(entity));
      return true;
    }
    return false;
  }

  /**
   * Updates the state of an Entity in the EntitySystemMapper.
   *
   * <p>The method checks if the given Entity fulfills the filter rules defined in the
   * EntitySystemMapper. If the Entity fulfills the filter rules and is not already present in the
   * EntitySystemMapper, it will be added. If the Entity does not fulfill the filter rules and is
   * currently present in the EntitySystemMapper, it will be removed.
   *
   * @param entity The Entity to update in the EntitySystemMapper.
   */
  public void update(final Entity entity) {
    if (accept(entity)) add(entity);
    else remove(entity);
  }

  /**
   * Returns a Stream of the Entities in the EntitySystemMapper.
   *
   * @return A Stream of Entities currently present in the EntitySystemMapper.
   */
  public Stream<Entity> stream() {
    return new HashSet<>(entities).stream();
  }

  /**
   * Checks if the given object is equal to this EntitySystemMapper.
   *
   * <p>The method compares the given object with this EntitySystemMapper for equality. If the
   * object is the same instance as this EntitySystemMapper, it returns true. If the object is an
   * instance of EntitySystemMapper, it compares the filterRules of both EntitySystemMappers for
   * equality.
   *
   * @param o The object to compare with this EntitySystemMapper.
   * @return true if the object is equal to this EntitySystemMapper, false otherwise.
   */
  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    else if (o instanceof EntitySystemMapper)
      return filterRules.equals(((EntitySystemMapper) o).filterRules);
    return false;
  }

  /**
   * Checks if the given Set of Component classes is equal to the filterRules of this
   * EntitySystemMapper.
   *
   * @param o The Set of Component classes to compare with the filterRules of this
   *     EntitySystemMapper.
   * @return true if the given Set of Component classes is equal to the filterRules, false
   *     otherwise.
   */
  public boolean equals(final Set<Class<? extends Component>> o) {
    return o.equals(filterRules);
  }

  /**
   * Check if the given entity has all the components needed to be processed by this mapper.
   *
   * @param entity the entity to check
   * @return true if the entity is accepted, false if not.
   */
  private boolean accept(final Entity entity) {
    for (Class<? extends Component> filter : filterRules)
      if (!entity.isPresent(filter)) {
        return false;
      }
    return true;
  }

  /**
   * Checks if the given System is present in the EntitySystemMapper.
   *
   * @param system The System to check for presence in the EntitySystemMapper.
   * @return true if the System is present in the EntitySystemMapper, false otherwise.
   */
  public boolean has(final System system) {
    return systems.contains(system);
  }
}
