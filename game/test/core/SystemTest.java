package core;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SystemTest {
  private System ts;
  private final boolean[] onAdd = {false};
  private final boolean[] onRemove = {false};

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

  @After
  public void cleanup() {
    onAdd[0] = false;
    onRemove[0] = false;
  }

  @Test
  public void add() {
    Entity e = new Entity();
    ts.triggerOnAdd(e);
    assertTrue(onAdd[0]);
  }

  @Test
  public void remove() {
    Entity e = new Entity();
    ts.triggerOnRemove(e);
    assertTrue(onRemove[0]);
  }

  private class DummyComponent implements Component {}
}
