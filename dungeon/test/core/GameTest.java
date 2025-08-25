package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.level.elements.ILevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link Game} class. */
public class GameTest {
  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  /** WTF? . */
  @Test
  public void allEntites() {
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    ILevel level = Mockito.mock(ILevel.class);
    Game.currentLevel(level);
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    assertEquals(8, Game.allEntities().count());
  }

  /** WTF? . */
  @Test
  public void removeAllEntites() {
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    ILevel level = Mockito.mock(ILevel.class);
    Game.currentLevel(level);
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    Game.add(new Entity());
    assertEquals(8, Game.allEntities().count());
    Game.removeAllEntities();
    assertEquals(0, Game.allEntities().count());
  }

  /** WTF? . */
  @Test
  public void find_exisiting() {
    Entity e = new Entity();
    DummyComponent dc = new DummyComponent();
    e.add(dc);
    Game.add(e);
    assertEquals(e, Game.findInAll(dc).get());
    // load ne level to check if it still works
    ILevel level = Mockito.mock(ILevel.class);
    assertEquals(e, Game.findInAll(dc).get());
  }

  /** WTF? . */
  @Test
  public void find_nonExisting() {
    DummyComponent dc = new DummyComponent();
    assertTrue(Game.findInAll(dc).isEmpty());
  }

  private static class DummyComponent implements Component {}
}
