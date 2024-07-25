package de.fwatermann.dungine.ecs;

public abstract class Component {

  private final boolean multiple;

  public Component(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * Returns whether an entity can have multiple components of this type.
   * @return true if multiple components are allowed, false otherwise.
   */
  public boolean canHaveMultiple() {
    return this.multiple;
  }

}
