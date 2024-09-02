package de.fwatermann.dungine.ecs;

public abstract class Component {

  private final boolean multiple;

  private Entity entity;

  public Component(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * Returns whether an entity can have multiple components of this type.
   *
   * @return true if multiple components are allowed, false otherwise.
   */
  public boolean canHaveMultiple() {
    return this.multiple;
  }

  final void entity(Entity entity) {
    this.entity = entity;
  }

  public final Entity entity() {
    return this.entity;
  }

}
