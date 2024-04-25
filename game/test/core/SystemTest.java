package core;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link System} class. */
public class SystemTest {
  private System ts;
  private final boolean[] onAdd = {false};
  private final boolean[] onRemove = {false};

  /** WTF? . */
  @Before
  public void setup() {
    ts =
        new System(DummyComponent.class) {
          @Override
          public void execute() {}
        };
    ts.onEntityAdd = entity -> onAdd[0] = true;

    ts.onEntityRemove = entity -> onRemove[0] = true;
  }

  /** WTF? . */
  @After
  public void cleanup() {
    onAdd[0] = false;
    onRemove[0] = false;
  }

  /** WTF? . */
  @Test
  public void add() {
    Entity e = new Entity();
    ts.triggerOnAdd(e);
    assertTrue(onAdd[0]);
  }

  /** WTF? . */
  @Test
  public void remove() {
    Entity e = new Entity();
    ts.triggerOnRemove(e);
    assertTrue(onRemove[0]);
  }

  private static class DummyComponent implements Component {}
}
