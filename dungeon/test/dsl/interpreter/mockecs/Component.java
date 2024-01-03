package dsl.interpreter.mockecs;

public class Component {
  public Component(Entity entity) {
    entity.components.add(this);
  }
}
