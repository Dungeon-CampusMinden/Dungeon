package dsl.interpreter.mockecs;

/** WTF? . */
public class Component {
  /**
   * WTF? .
   *
   * @param entity foo
   */
  public Component(Entity entity) {
    entity.components.add(this);
  }
}
