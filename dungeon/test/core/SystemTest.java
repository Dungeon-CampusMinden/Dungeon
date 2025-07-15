package core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link System} class. */
public class SystemTest {
  private final boolean[] onAdd = {false};
  private final boolean[] onRemove = {false};
  private System testSystem;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    testSystem =
        new System(DummyComponent.class) {
          @Override
          public void execute() {}
        };
    testSystem.onEntityAdd = entity -> onAdd[0] = true;

    testSystem.onEntityRemove = entity -> onRemove[0] = true;
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    onAdd[0] = false;
    onRemove[0] = false;
  }

  /** WTF? . */
  @Test
  public void add() {
    Entity e = new Entity();
    testSystem.triggerOnAdd(e);
    assertTrue(onAdd[0]);
  }

  /** WTF? . */
  @Test
  public void remove() {
    Entity e = new Entity();
    testSystem.triggerOnRemove(e);
    assertTrue(onRemove[0]);
  }

  /**
   * Tests the filteredEntityStream method with no parameters. Ensures that the stream contains
   * entities matching the default filter rules defined in the system constructor.
   */
  @Test
  public void filteredEntityStream_no_parameter() {
    Entity e1 = new Entity();
    Entity e2 = new Entity();
    e1.add(new DummyComponent());
    Game.add(e1);
    Game.add(e2);
    List<Entity> stream = testSystem.filteredEntityStream().toList();
    assertTrue(stream.contains(e1));
    assertFalse(stream.contains(e2));
  }

  /**
   * Tests the filteredEntityStream method with an array parameter. Ensures that the stream contains
   * entities matching the specified filter rule (DummyComponent).
   */
  @Test
  public void filteredEntityStream_array_parameter() {
    Entity e1 = new Entity();
    Entity e2 = new Entity();
    e1.add(new DummyComponent());
    Game.add(e1);
    Game.add(e2);
    List<Entity> stream = testSystem.filteredEntityStream(DummyComponent.class).toList();
    assertTrue(stream.contains(e1));
    assertFalse(stream.contains(e2));
  }

  /**
   * Tests the filteredEntityStream method with a set parameter. Ensures that the stream contains
   * entities matching the specified filter rule (DummyComponent).
   */
  @Test
  public void filteredEntityStream_set_parameter() {
    Entity e1 = new Entity();
    Entity e2 = new Entity();
    e1.add(new DummyComponent());
    Game.add(e1);
    Game.add(e2);
    List<Entity> stream = testSystem.filteredEntityStream(Set.of(DummyComponent.class)).toList();
    assertTrue(stream.contains(e1));
    assertFalse(stream.contains(e2));
  }

  /**
   * Tests the filteredEntityStream method with an empty set parameter. Ensures that the stream
   * contains all entities as no filter rules are applied.
   */
  @Test
  public void filteredEntityStream_empty_set_parameter() {
    Entity e1 = new Entity();
    Entity e2 = new Entity();
    e1.add(new DummyComponent());
    Game.add(e1);
    Game.add(e2);
    List<Entity> stream = testSystem.filteredEntityStream(Set.of()).toList();
    assertTrue(stream.contains(e1));
    assertTrue(stream.contains(e2));
  }

  private static class DummyComponent implements Component {}
}
