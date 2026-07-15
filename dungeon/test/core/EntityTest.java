package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link Entity} class. */
public class EntityTest {

  private final Component testComponent = Mockito.mock(Component.class);
  private Entity entity;

  /** WTF? . */
  @BeforeEach
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

  /** Replacing a component must run system removal and addition lifecycle callbacks. */
  @Test
  public void replaceComponentNotifiesSystems() {
    Entity trackedEntity = new Entity();
    trackedEntity.add(new TestComponent());
    Game.add(trackedEntity);
    TrackingSystem system = new TrackingSystem();
    Game.add(system);

    TestComponent replacement = new TestComponent();
    trackedEntity.add(replacement);

    assertEquals(2, system.addCount.get());
    assertEquals(1, system.removeCount.get());

    trackedEntity.add(replacement);

    assertEquals(2, system.addCount.get());
    assertEquals(1, system.removeCount.get());
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
    assertEquals(0, entity.compareTo(entity));
  }

  /** WTF? . */
  @Test
  public void compareToLowerID() {
    Entity entity1 = new Entity();
    Entity entity2 = new Entity();
    assertTrue(entity1.id() < entity2.id());
    assertTrue(entity1.compareTo(entity2) < 0);
  }

  /** WTF? . */
  @Test
  public void compareToHigherID() {
    Entity entity1 = new Entity();
    Entity entity2 = new Entity();

    assertTrue(entity2.id() > entity1.id());
    assertTrue(entity2.compareTo(entity1) > 0);
  }

  /** Gets called after each @Test and cleans up any Entity left in game. */
  @AfterEach
  public void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  private static final class TestComponent implements Component {}

  private static final class TrackingSystem extends System {
    private final AtomicInteger addCount = new AtomicInteger();
    private final AtomicInteger removeCount = new AtomicInteger();

    private TrackingSystem() {
      super(TestComponent.class);
      onEntityAdd = ignored -> addCount.incrementAndGet();
      onEntityRemove = ignored -> removeCount.incrementAndGet();
    }

    @Override
    public void execute() {}
  }
}
