package feature.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Tests for collider metadata synchronization. */
class CollideSyncTest {
  private static final float DELTA = 1e-6f;

  @Test
  void metadataUsesConfiguredPrefixAndRestoresHitcircle() {
    CollideSync sync = CollideSync.withPrefix("room.collider");
    Hitcircle collider = new Hitcircle(1.25f, 0.2f, 0.3f);
    collider.position(new Point(4f, 5f));
    collider.scale(Vector2.of(2f, 3f));
    CollideComponent component = new CollideComponent();
    component.collider(collider);
    component.isSolid(false);

    Map<String, String> metadata = sync.metadataOf(component);

    assertEquals("hitcircle", metadata.get("room.collider.type"));
    assertFalse(metadata.containsKey("collider.type"));

    CollideComponent restored = sync.fromMetadata(metadata).orElseThrow();
    assertFalse(restored.isSolid());
    assertTrue(restored.collider() instanceof Hitcircle);
    assertEquals(2.5f, restored.collider().width(), DELTA);
    assertEquals(2.5f, restored.collider().height(), DELTA);
    assertEquals(0.2f, restored.collider().offset().x(), DELTA);
    assertEquals(0.3f, restored.collider().offset().y(), DELTA);
    assertEquals(4f, restored.collider().position().x(), DELTA);
    assertEquals(5f, restored.collider().position().y(), DELTA);
    assertEquals(2f, restored.collider().scale().x(), DELTA);
    assertEquals(3f, restored.collider().scale().y(), DELTA);
  }

  @Test
  void applyKeepsExistingCollisionCallbacks() {
    CollideSync sync = CollideSync.withPrefix("room.collider");
    Entity target = new Entity("target");
    AtomicBoolean entered = new AtomicBoolean(false);
    CollideComponent existing =
        new CollideComponent(
            (self, other, direction) -> entered.set(true),
            CollideComponent.DEFAULT_COLLIDER);
    target.add(existing);

    CollideComponent incoming =
        new CollideComponent(Vector2.of(0.4f, 0.5f), Vector2.of(3f, 2f)).isSolid(false);
    sync.apply(target, incoming);

    CollideComponent updated = target.fetch(CollideComponent.class).orElseThrow();
    assertSame(existing, updated);
    assertFalse(updated.isSolid());
    assertEquals(3f, updated.collider().width(), DELTA);
    assertEquals(2f, updated.collider().height(), DELTA);

    updated.onEnter(target, new Entity("other"), Direction.DOWN);
    assertTrue(entered.get());
  }
}
