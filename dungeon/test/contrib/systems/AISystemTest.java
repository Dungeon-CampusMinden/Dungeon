package contrib.systems;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** WTF? . */
public class AISystemTest {

  private int updateCounter;
  private AISystem system;
  private Entity entity;

  /** WTF? . */
  @Before
  public void setup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    system = new AISystem();
    entity = new Entity();
    entity.add(
        new AIComponent(
            null,
            e -> {},
            entity -> {
              updateCounter++;
              return false;
            }));
    Game.add(entity);
    updateCounter = 0;
  }

  /** WTF? . */
  @After
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** WTF? . */
  @Test
  public void update() {
    system.execute();
    assertEquals(1, updateCounter);
  }

  /** WTF? . */
  @Test
  public void update_executeFight() {
    Function<Entity, Boolean> transition = Mockito.mock(Function.class);
    Consumer<Entity> fight = Mockito.mock(Consumer.class);
    Consumer<Entity> idle = Mockito.mock(Consumer.class);
    when(transition.apply(entity)).thenReturn(true);

    AIComponent component = new AIComponent(fight, idle, transition);
    entity.add(component);
    system.execute();
    verify(fight, times(1)).accept(entity);
    verify(idle, never()).accept(entity);
  }

  /** WTF? . */
  @Test
  public void update_executeIdle() {
    Function<Entity, Boolean> transition = Mockito.mock(Function.class);
    Consumer<Entity> fight = Mockito.mock(Consumer.class);
    Consumer<Entity> idle = Mockito.mock(Consumer.class);
    when(transition.apply(entity)).thenReturn(false);

    AIComponent component = new AIComponent(fight, idle, transition);
    entity.add(component);
    system.execute();
    verify(idle, times(1)).accept(entity);
    verify(fight, never()).accept(entity);
  }
}
