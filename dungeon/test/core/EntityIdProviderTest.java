package core;

import static org.junit.jupiter.api.Assertions.*;

import core.utils.EntityIdProvider;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for core.utils.EntityIdProvider.
 *
 * <p>Each public test method is documented. Tests reset the internal static state of the provider
 * between runs using reflection to avoid test interference.
 */
public class EntityIdProviderTest {

  /**
   * Reset EntityIdProvider internal state after each test to ensure isolation between tests.
   *
   * @throws RuntimeException if reflective access to internal fields fails
   */
  @AfterEach
  public void resetProviderState() {
    try {
      Field nextField = EntityIdProvider.class.getDeclaredField("NEXT");
      nextField.setAccessible(true);
      AtomicInteger next = (AtomicInteger) nextField.get(null);
      next.set(0);

      Field usedField = EntityIdProvider.class.getDeclaredField("USED");
      usedField.setAccessible(true);
      @SuppressWarnings("unchecked")
      ConcurrentHashMap<Integer, Boolean> used =
          (ConcurrentHashMap<Integer, Boolean>) usedField.get(null);
      used.clear();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to reset EntityIdProvider state", e);
    }
  }

  /** Verifies that nextId() produces unique, non-negative IDs across a sequence of calls. */
  @Test
  public void nextIdGeneratesUniqueNonNegativeIds() {
    Set<Integer> ids = new HashSet<>();
    int count = 10_000;
    for (int i = 0; i < count; i++) {
      int id = EntityIdProvider.nextId();
      assertTrue(id >= 0, "ID must be non-negative");
      assertTrue(ids.add(id), "Duplicate ID generated: " + id);
    }
    assertEquals(count, ids.size());
  }

  /**
   * Verifies that registerOrThrow accepts a freshly available ID (one that was generated and
   * released).
   */
  @Test
  public void registerOrThrowWithFreshIdSucceeds() {
    int id = EntityIdProvider.nextId();
    EntityIdProvider.unregister(id);
    assertDoesNotThrow(() -> EntityIdProvider.registerOrThrow(id));
  }

  /** Verifies that registerOrThrow throws when attempting to register an already used ID. */
  @Test
  public void registerOrThrowDuplicateThrows() {
    int id = EntityIdProvider.nextId();
    assertThrows(IllegalArgumentException.class, () -> EntityIdProvider.registerOrThrow(id));
  }

  /** Verifies that registerOrThrow rejects negative IDs. */
  @Test
  public void registerOrThrowRejectsNegative() {
    assertThrows(IllegalArgumentException.class, () -> EntityIdProvider.registerOrThrow(-1));
  }

  /**
   * Verifies that ensureRegistered is idempotent and reserves an ID such that subsequent
   * registerOrThrow on the same ID fails.
   */
  @Test
  public void ensureRegisteredIsIdempotentAndReserves() {
    int id = 1234;
    assertDoesNotThrow(() -> EntityIdProvider.ensureRegistered(id));
    assertDoesNotThrow(() -> EntityIdProvider.ensureRegistered(id));
    assertThrows(IllegalArgumentException.class, () -> EntityIdProvider.registerOrThrow(id));
  }

  /** Verifies that ensureRegistered rejects negative IDs. */
  @Test
  public void ensureRegisteredRejectsNegative() {
    assertThrows(IllegalArgumentException.class, () -> EntityIdProvider.ensureRegistered(-42));
  }

  /**
   * Verifies that unregister releases an ID and is idempotent (re-registering after release
   * succeeds).
   */
  @Test
  public void unregisterReleasesIdAndIsIdempotent() {
    int id = 777;
    EntityIdProvider.ensureRegistered(id);
    EntityIdProvider.unregister(id);
    EntityIdProvider.unregister(id); // idempotent
    assertDoesNotThrow(() -> EntityIdProvider.registerOrThrow(id));
  }

  /**
   * Verifies that nextId() never returns an explicitly registered id (no collision), regardless of
   * the provider's internal NEXT position.
   */
  @Test
  public void nextIdDoesNotReturnExplicitlyRegisteredId() {
    int base = EntityIdProvider.nextId();
    int reserved = base + 2;
    EntityIdProvider.ensureRegistered(reserved);
    int id1 = EntityIdProvider.nextId();
    int id2 = EntityIdProvider.nextId();

    assertNotEquals(reserved, id1);
    assertNotEquals(reserved, id2);
    assertTrue(id1 >= 0 && id2 >= 0);
    assertNotEquals(id1, id2);
  }

  /**
   * Verifies registerOrThrow bumps the internal NEXT pointer when registering a higher ID, so
   * subsequent auto-generated IDs don't loop over large used ranges.
   */
  @Test
  public void registerOrThrowBumpsNext() {
    int first = EntityIdProvider.nextId(); // typically 0 after reset
    int high = first + 10_000;
    EntityIdProvider.registerOrThrow(high);
    int next = EntityIdProvider.nextId();
    assertEquals(high + 1, next);
  }

  /**
   * Verifies ensureRegistered also bumps the internal NEXT pointer when a high ID is ensured, so
   * subsequent auto-generated IDs continue above that high value.
   */
  @Test
  public void ensureRegisteredBumpsNext() {
    int first = EntityIdProvider.nextId();
    int high = first + 20_000;
    EntityIdProvider.ensureRegistered(high);
    int next = EntityIdProvider.nextId();
    assertEquals(high + 1, next);
  }

  /**
   * Stress-tests concurrent generation of IDs to ensure uniqueness across multiple threads.
   *
   * @throws InterruptedException if the test is interrupted while awaiting thread completion
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
                ids.add(EntityIdProvider.nextId());
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
    assertTrue(ids.stream().allMatch(id -> id >= 0));
  }

  /**
   * Verifies that nextId() throws IllegalStateException when the provider has reached
   * Integer.MAX_VALUE and that ID is already taken.
   *
   * <p>This test sets the internal NEXT counter to Integer.MAX_VALUE and ensures that that ID is
   * registered; calling nextId() must then fail.
   */
  @Test
  public void exhaustionWhenMaxValueIsTaken() {
    // Set NEXT to Integer.MAX_VALUE
    setNextTo(Integer.MAX_VALUE);
    // Ensure MAX_VALUE is marked used
    EntityIdProvider.ensureRegistered(Integer.MAX_VALUE);
    assertThrows(IllegalStateException.class, EntityIdProvider::nextId);
  }

  private void setNextTo(int value) {
    try {
      Field nextField = EntityIdProvider.class.getDeclaredField("NEXT");
      nextField.setAccessible(true);
      AtomicInteger next = (AtomicInteger) nextField.get(null);
      next.set(value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to set NEXT via reflection", e);
    }
  }
}
