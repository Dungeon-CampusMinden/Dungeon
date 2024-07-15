package core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for the {@link Entity} class. */
public class EntityTest {

  private final Component testComponent = Mockito.mock(Component.class);
  private Entity entity;

  /** WTF? . */
  @Before
  public void setup() {
    entity = new Entity();
    entity.add(testComponent);
  }

  /** WTF? . */
  @Test
  public void addComponent() {
    assertEquals(testComponent, entity.fetch(testComponent.getClass()).get());
  }

  /** WTF? . */
  @Test
  public void addAlreadyExistingComponent() {
    Component newComponent = Mockito.mock(Component.class);
    entity.add(newComponent);
    assertEquals(newComponent, entity.fetch(testComponent.getClass()).get());
  }

  /** WTF? . */
  @Test
  public void removeComponent() {
    entity.remove(testComponent.getClass());
    assertTrue(entity.fetch(testComponent.getClass()).isEmpty());
  }

  /** WTF? . */
  @Test
  public void compareToSameID() {
    assertEquals(entity.id(), entity.id());
    assertEquals("Entity with the same id should return a 0. ", 0, entity.compareTo(entity));
  }

  /** WTF? . */
  @Test
  public void compareToLowerID() {
    Entity entity1 = new Entity();
    Entity entity2 = new Entity();
    assertTrue(
        "Entity which gets created earlier should have a lower id.", entity1.id() < entity2.id());
    assertTrue(
        "Entity which gets created earlier should return negative number.",
        entity1.compareTo(entity2) < 0);
  }

  /** WTF? . */
  @Test
  public void compareToHigherID() {
    Entity entity1 = new Entity();
    Entity entity2 = new Entity();

    assertTrue(
        "Entity which gets created later should have a higher id.", entity2.id() > entity1.id());
    assertTrue(
        "Entity which gets created later should return a number higher then 0.",
        entity2.compareTo(entity1) > 0);
  }

  /** Gets called after each @Test and cleans up any Entity left in game. */
  @After
  public void tearDown() {
    Game.removeAllEntities();
  }
}
