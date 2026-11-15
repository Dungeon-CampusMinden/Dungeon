package engine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.level.elements.ILevel;
import engine.systems.LevelSystem;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link Resources} registry. */
public class ResourcesTest {

  /**
   * Resource type used to test resources that are not part of the engine core.
   *
   * @param value an arbitrary value to tell instances apart
   */
  private record Counter(int value) {}

  /** Registers the level system, which owns the level resource. */
  @BeforeEach
  public void setup() {
    Game.add(new LevelSystem());
  }

  /** Resets the global game state. */
  @AfterEach
  public void cleanup() {
    Game.currentLevel(null);
    Game.removeAllSystems();
    Resources.clear();
  }

  /** A handle for a type that was never inserted is empty. */
  @Test
  public void resIsEmptyForUnknownType() {
    assertTrue(Resources.res(Counter.class).get().isEmpty());
  }

  /** An inserted resource can be read through a handle. */
  @Test
  public void insertedResourceIsResolved() {
    Resources.insert(Counter.class, new Counter(1));

    assertEquals(Optional.of(new Counter(1)), Resources.res(Counter.class).get());
  }

  /** A handle requested before the resource exists resolves once the resource is inserted. */
  @Test
  public void handleRequestedBeforeInsertResolvesAfterInsert() {
    Res<Counter> handle = Resources.res(Counter.class);

    Resources.insert(Counter.class, new Counter(1));

    assertEquals(Optional.of(new Counter(1)), handle.get());
  }

  /** A handle resolves to the latest value after the resource was replaced. */
  @Test
  public void handleFollowsReplacement() {
    Resources.insert(Counter.class, new Counter(1));
    Res<Counter> handle = Resources.res(Counter.class);

    Resources.insert(Counter.class, new Counter(2));

    assertEquals(Optional.of(new Counter(2)), handle.get());
  }

  /** After removing a resource, existing handles are empty. */
  @Test
  public void removeMakesHandleEmpty() {
    Resources.insert(Counter.class, new Counter(1));
    Res<Counter> handle = Resources.res(Counter.class);

    Resources.remove(Counter.class);

    assertTrue(handle.get().isEmpty());
  }

  /** Inserting {@code null} removes the resource. */
  @Test
  public void insertNullRemovesResource() {
    Resources.insert(Counter.class, new Counter(1));

    Resources.insert(Counter.class, null);

    assertTrue(Resources.res(Counter.class).get().isEmpty());
  }

  /** {@code clear} removes all resources. */
  @Test
  public void clearRemovesAllResources() {
    Resources.insert(Counter.class, new Counter(1));

    Resources.clear();

    assertTrue(Resources.res(Counter.class).get().isEmpty());
  }

  /** Without a loaded level, the level handle is empty. */
  @Test
  public void levelIsEmptyWithoutLoadedLevel() {
    assertTrue(Resources.level().get().isEmpty());
  }

  /** Loading a level through the game stores it in the registry. */
  @Test
  public void loadedLevelIsStoredInRegistry() {
    ILevel level = Mockito.mock(ILevel.class);

    Game.currentLevel(level);

    assertEquals(Optional.of(level), Resources.level().get());
    assertEquals(Optional.of(level), Resources.res(ILevel.class).get());
  }

  /** A level handle obtained before a level change resolves to the new level afterwards. */
  @Test
  public void levelHandleFollowsLevelChange() {
    ILevel first = Mockito.mock(ILevel.class);
    ILevel second = Mockito.mock(ILevel.class);
    Game.currentLevel(first);
    Res<ILevel> handle = Resources.level();

    Game.currentLevel(second);

    assertEquals(Optional.of(second), handle.get());
  }

  /** A type that was never inserted has version 0. */
  @Test
  public void versionIsZeroForUnknownType() {
    /** Type that is never inserted into the registry. */
    record NeverInserted() {}

    assertEquals(0L, Resources.version(NeverInserted.class));
  }

  /** Every insert increases the version, also when the same value is inserted again. */
  @Test
  public void everyInsertIncreasesVersion() {
    Counter counter = new Counter(1);
    long before = Resources.version(Counter.class);

    Resources.insert(Counter.class, counter);
    Resources.insert(Counter.class, counter);

    assertEquals(before + 2, Resources.version(Counter.class));
  }

  /** Removing increases the version only if a resource was stored. */
  @Test
  public void removeIncreasesVersionOnlyIfPresent() {
    long before = Resources.version(Counter.class);

    Resources.remove(Counter.class);
    assertEquals(before, Resources.version(Counter.class));

    Resources.insert(Counter.class, new Counter(1));
    Resources.remove(Counter.class);
    assertEquals(before + 2, Resources.version(Counter.class));
  }

  /** {@code clear} does not reset versions, so trackers never miss a later change. */
  @Test
  public void clearDoesNotResetVersions() {
    Resources.insert(Counter.class, new Counter(1));
    long beforeClear = Resources.version(Counter.class);

    Resources.clear();

    assertTrue(Resources.version(Counter.class) > beforeClear);
  }

  /** A registry handle reports the version stored in the registry. */
  @Test
  public void handleReportsRegistryVersion() {
    Res<Counter> handle = Resources.res(Counter.class);

    Resources.insert(Counter.class, new Counter(1));

    assertEquals(Resources.version(Counter.class), handle.version());
  }

  /** The game facade reads the level from the registry. */
  @Test
  public void gameFacadeReadsFromRegistry() {
    ILevel level = Mockito.mock(ILevel.class);

    Game.currentLevel(level);

    assertEquals(Optional.of(level), Game.currentLevel());
    assertEquals(Optional.of(level), LevelSystem.level());
  }
}
