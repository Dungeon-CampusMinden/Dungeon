package core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class EntityIdProviderTest {

  @AfterEach
  public void tearDown() {
    Game.removeAllEntities();
  }

  private Entity createEntityWithFreshExplicitId(String name) {
    int start = (int) (java.lang.System.nanoTime() & 0x3FFF_FFFF);
    int attempts = 0;
    while (attempts < 10000) {
      int id = start + attempts;
      try {
        return new Entity(id, name);
      } catch (IllegalArgumentException ignored) {
        attempts++;
      }
    }
    fail("Could not create entity with a fresh explicit id for test");
    return null;
  }

  @Test
  public void explicitIdIsSetAndNameUnchanged() {
    Entity e = createEntityWithFreshExplicitId("Foo");
    assertEquals("Foo", e.name());
    assertTrue(e.id() >= 0);
    assertTrue(e.toString().endsWith("_" + e.id()));
  }

  @Test
  public void duplicateExplicitIdThrows() {
    Entity first = createEntityWithFreshExplicitId("First");
    int id = first.id();
    assertThrows(IllegalArgumentException.class, () -> new Entity(id, "Second"));
  }

  @Test
  public void concurrentGenerationProducesUniqueIds() throws InterruptedException {
    final int threads = 8;
    final int perThread = 200;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    Set<Integer> ids = Collections.synchronizedSet(new HashSet<>());
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    for (int t = 0; t < threads; t++) {
      pool.execute(
          () -> {
            try {
              start.await();
              for (int i = 0; i < perThread; i++) {
                Entity e = new Entity();
                ids.add(e.id());
              }
            } catch (InterruptedException ex) {
              Thread.currentThread().interrupt();
            } finally {
              done.countDown();
            }
          });
    }

    start.countDown();
    assertTrue(done.await(10, TimeUnit.SECONDS), "Tasks did not finish in time");
    pool.shutdownNow();

    assertEquals(threads * perThread, ids.size(), "IDs must be unique across threads");
    assertTrue(ids.stream().allMatch(id2 -> id2 >= 0));
  }
}
