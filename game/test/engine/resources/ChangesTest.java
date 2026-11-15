package engine.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.level.elements.ILevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link Changes} tracker. */
public class ChangesTest {

  /**
   * Resource type used to test registry resources.
   *
   * @param value an arbitrary value to tell instances apart
   */
  private record Counter(int value) {}

  /** Resets the global registry. */
  @AfterEach
  public void cleanup() {
    Resources.clear();
  }

  /** The first check always reports a change. */
  @Test
  public void firstCheckReportsChange() {
    Changes changes = Changes.of(Res.of("level"));

    assertTrue(changes.changedSinceLastCheck());
  }

  /** A second check without a change in between reports no change. */
  @Test
  public void secondCheckWithoutChangeReportsNoChange() {
    Changes changes = Changes.of(Res.of("level"));
    changes.changedSinceLastCheck();

    assertFalse(changes.changedSinceLastCheck());
  }

  /** A change of a test handle is reported exactly once. */
  @Test
  public void changeIsReportedOnce() {
    MutableRes<String> res = new MutableRes<>("first");
    Changes changes = Changes.of(res);
    changes.changedSinceLastCheck();

    res.set("second");

    assertTrue(changes.changedSinceLastCheck());
    assertFalse(changes.changedSinceLastCheck());
  }

  /** Several changes between two checks are reported as one change. */
  @Test
  public void severalChangesBetweenChecksAreReportedOnce() {
    MutableRes<String> res = new MutableRes<>("first");
    Changes changes = Changes.of(res);
    changes.changedSinceLastCheck();

    res.set("second");
    res.set("third");

    assertTrue(changes.changedSinceLastCheck());
    assertFalse(changes.changedSinceLastCheck());
  }

  /** Every reader notices a change once, independent of the other readers. */
  @Test
  public void everyReaderNoticesChange() {
    Res<Counter> res = Resources.res(Counter.class);
    Changes first = Changes.of(res);
    Changes second = Changes.of(res);
    first.changedSinceLastCheck();
    second.changedSinceLastCheck();

    Resources.insert(Counter.class, new Counter(1));

    assertTrue(first.changedSinceLastCheck());
    assertFalse(first.changedSinceLastCheck());
    assertTrue(second.changedSinceLastCheck());
    assertFalse(second.changedSinceLastCheck());
  }

  /** A reader created after the resource was inserted still reacts to it once. */
  @Test
  public void readerCreatedLaterNoticesExistingResource() {
    Resources.insert(Counter.class, new Counter(1));

    Changes changes = Changes.of(Resources.res(Counter.class));

    assertTrue(changes.changedSinceLastCheck());
    assertFalse(changes.changedSinceLastCheck());
  }

  /** Inserting, replacing and removing a registry resource are all reported as changes. */
  @Test
  public void registryInsertReplaceAndRemoveAreChanges() {
    Changes changes = Changes.of(Resources.res(Counter.class));
    changes.changedSinceLastCheck();

    Resources.insert(Counter.class, new Counter(1));
    assertTrue(changes.changedSinceLastCheck());

    Resources.insert(Counter.class, new Counter(2));
    assertTrue(changes.changedSinceLastCheck());

    Resources.remove(Counter.class);
    assertTrue(changes.changedSinceLastCheck());
  }

  /** A level change through the registry is noticed by a level tracker. */
  @Test
  public void levelChangeIsNoticed() {
    Resources.insert(ILevel.class, Mockito.mock(ILevel.class));
    Changes changes = Changes.of(Resources.level());
    changes.changedSinceLastCheck();

    Resources.insert(ILevel.class, Mockito.mock(ILevel.class));

    assertTrue(changes.changedSinceLastCheck());
  }
}
