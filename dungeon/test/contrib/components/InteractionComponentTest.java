package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link InteractionComponent}. */
public class InteractionComponentTest {

  /** Tests if the simple ctor sets the attributes to the default values. */
  @Test
  public void createSimpleConstructor() {
    Entity e = new Entity();
    InteractionComponent component = new InteractionComponent();
    e.add(component);
    assertEquals(
        Interaction.DEFAULT_INTERACTION_RADIUS,
        component.interactions().interact().range(),
        0.0001);
  }

  /** Tests if the complex Constructor sets the attributes to the parameter. */
  @Test
  public void createComplexConstructor() {
    Entity e = new Entity();
    float radius = 100;
    boolean repeat = true;
    BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);

    InteractionComponent component =
        new InteractionComponent(() -> new Interaction(iInteraction, radius, repeat));
    e.add(component);

    assertEquals(radius, component.interactions().interact().range(), 0.0001);
  }
}
