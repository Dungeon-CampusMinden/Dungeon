package observer;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.Game;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ObserverSystem}.
 *
 * <p>These tests cover registering observers, notifying them when the observable triggers,
 * unregistering observers, handling multiple observers, multiple notifications, and clearing all
 * observations.
 */
public class ObserverSystemTest {

  private ObserverSystem system;
  private Entity entity;
  private ObservableComponent observable;
  private ObserverComponent observer;
  private AtomicInteger counter;

  /**
   * Sets up a fresh system, entity, observable component, observer component, and counter before
   * each test.
   */
  @BeforeEach
  void setUp() {
    system = new ObserverSystem();
    entity = new Entity();
    counter = new AtomicInteger(0);
    observable = new ObservableComponent(() -> true);
    observer = new ObserverComponent(e -> counter.incrementAndGet());
    entity.add(observable);
    Game.add(entity);
  }

  /** Cleans up the game entities and clears all registered observations after each test. */
  @AfterEach
  void clear() {
    Game.removeAllEntities();
    ObserverSystem.clear();
  }

  /** Tests that a registered observer is notified when the observable triggers. */
  @Test
  void testObserveAndNotify() {
    ObserverSystem.observe(observer, observable);
    system.execute();
    assertEquals(1, counter.get());
  }

  /**
   * Tests that observers are not notified if the observable's {@code shouldNotify} returns false.
   */
  @Test
  void testShouldNotifyFalse() {
    Entity entity1 = new Entity();
    Game.add(entity1);
    ObservableComponent silentObservable = new ObservableComponent(() -> false);
    entity1.add(silentObservable);
    ObserverComponent obs = new ObserverComponent(e -> counter.incrementAndGet());
    ObserverSystem.observe(obs, silentObservable);
    system.execute();
    assertEquals(0, counter.get());
  }

  /** Tests that an observer which has been unregistered does not get notified. */
  @Test
  void testUnobserve() {
    ObserverSystem.observe(observer, observable);
    ObserverSystem.unobserve(observer, observable);
    system.execute();
    assertEquals(0, counter.get());
  }

  /** Tests that multiple observers registered to the same observable are all notified. */
  @Test
  void testMultipleObservers() {
    AtomicInteger counter1 = new AtomicInteger(0);
    ObserverComponent observer1 = new ObserverComponent(e -> counter.incrementAndGet());
    ObserverComponent observer2 = new ObserverComponent(e -> counter1.incrementAndGet());
    ObserverSystem.observe(observer1, observable);
    ObserverSystem.observe(observer2, observable);
    system.execute();
    assertEquals(1, counter.get(), "Observer 1 should be notified once");
    assertEquals(1, counter1.get(), "Observer 2 should be notified once");
  }

  /**
   * Tests that an observer is notified multiple times if the observable triggers multiple times.
   */
  @Test
  void testMultipleNotifications() {
    ObserverSystem.observe(observer, observable);
    system.execute();
    system.execute();
    system.execute();
    assertEquals(3, counter.get(), "Observer should be notified on each execution");
  }

  /** Tests that clearing all registered observations prevents observers from being notified. */
  @Test
  void testClearObservations() {
    ObserverSystem.observe(observer, observable);
    ObserverSystem.clear();
    system.execute();
    assertEquals(0, counter.get());
  }
}
