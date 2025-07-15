package core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Entity;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link PlayerComponent} class. */
public class PlayerComponentTest {

  private static final int counter = 0;
  private PlayerComponent playableComponent;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    playableComponent = new PlayerComponent();
  }

  /** WTF? . */
  @Test
  public void addFunction() {
    Consumer<Entity> function =
        new Consumer<Entity>() {
          @Override
          public void accept(Entity entity) {}
        };
    assertTrue(playableComponent.registerCallback(1, function).isEmpty());
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
    playableComponent.registerCallback(1, function).get();
    assertEquals(function, playableComponent.registerCallback(1, newfunction).get());
  }
}
