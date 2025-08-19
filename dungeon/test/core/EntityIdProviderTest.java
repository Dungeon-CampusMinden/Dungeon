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

/**
 * Tests for the Entity ID provider, ensuring that both explicit and auto-generated IDs are handled
 * correctly, and that concurrent ID generation does not lead to collisions.
 */
public class EntityIdProviderTest {

  /**
   * Cleans up after each test by removing all entities from the game to avoid cross-test
   * interference and ID reservation leaks.
   */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
  }

  private Entity createEntityWithFreshExplicitId(String name) {
    // Derive a non-negative, time-varying int seed from nanoTime by masking to 30 bits
    // (0x3FFF_FFFF)
    // to avoid the sign bit and minimize collisions across runs.
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
    return new Entity(name); //  should never hit
  }

  /**
   * Verifies that creating an entity with an explicit, fresh ID succeeds, keeps the provided name
   * unchanged, and produces a toString containing the id suffix.
   */
  @Test
  public void explicitIdIsSetAndNameUnchanged() {
    Entity e = createEntityWithFreshExplicitId("Foo");
    assertEquals("Foo", e.name());
    assertTrue(e.id() >= 0);
    assertTrue(e.toString().endsWith("_" + e.id()));
  }

  /**
   * Ensures that attempting to create a second entity with the same explicit ID results in an
   * IllegalArgumentException, preventing ID collisions.
   */
  @Test
  public void duplicateExplicitIdThrows() {
    Entity first = createEntityWithFreshExplicitId("First");
    int id = first.id();
    assertThrows(IllegalArgumentException.class, () -> new Entity(id, "Second"));
  }

  /**
   * Stress-tests concurrent ID generation to assert that all auto-generated IDs are unique across
   * multiple threads under load and within a reasonable time bound.
   *
   * @throws InterruptedException if the concurrency helpers are interrupted while awaiting
   *     completion
   */
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
