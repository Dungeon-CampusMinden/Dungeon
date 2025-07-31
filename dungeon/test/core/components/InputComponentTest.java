package core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Entity;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link InputComponent} class. */
public class InputComponentTest {

  private static final int counter = 0;
  private InputComponent inputComponent;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    inputComponent = new InputComponent();
  }

  /** WTF? . */
  @Test
  public void addFunction() {
    Consumer<Entity> function =
        new Consumer<Entity>() {
          @Override
          public void accept(Entity entity) {}
        };
    assertTrue(inputComponent.registerCallback(1, function).isEmpty());
  }

  /** WTF? . */
  public void addFunction_exisitng() {
    Consumer<Entity> function =
        new Consumer<Entity>() {
          @Override
          public void accept(Entity entity) {}
        };
    Consumer<Entity> newfunction =
        new Consumer<Entity>() {
          @Override
          public void accept(Entity entity) {}
        };
    inputComponent.registerCallback(1, function).get();
    assertEquals(function, inputComponent.registerCallback(1, newfunction).get());
  }
}
