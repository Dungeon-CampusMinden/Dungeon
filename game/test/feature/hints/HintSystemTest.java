package feature.hints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import feature.petrinet.PlaceComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HintSystem}.
 *
 * <p>These tests cover the behavior of the {@link HintSystem} with multiple entities, ensuring that
 * entities with tokens are queued correctly, hints are returned in order, and entities are removed
 * when tokens are depleted.
 */
class HintSystemTest {

  private HintSystem hintSystem;
  private Entity entity1;
  private Entity entity2;

  private Hint hint1_1 = new Hint("Hint 1-1", "Hint 1-1");
  private Hint hint1_2 = new Hint("Hint 1-2", "Hint 1-2");
  private Hint hint2_1 = new Hint("Hint 2-1", "Hint 2-1");

  /**
   * Sets up a new {@link HintSystem} and test entities before each test.
   *
   * <p>Entity1 has 1 token and 2 hints. Entity2 has 2 tokens and 1 hint. Both are added to the
   * game.
   */
  @BeforeEach
  void setUp() {
    hintSystem = new HintSystem();
    Game.add(hintSystem);

    entity1 = new Entity();
    PlaceComponent p1 = new PlaceComponent();
    p1.produce();
    entity1.add(p1);
    entity1.add(new HintComponent(hint1_1, hint1_2));
    Game.add(entity1);
    hintSystem.execute();

    entity2 = new Entity();
    PlaceComponent p2 = new PlaceComponent();
    p2.produce();
    entity2.add(p2);
    entity2.add(new HintComponent(hint2_1));
    Game.add(entity2);
    hintSystem.execute();
  }

  /** Clears all entities and systems from the game after each test to ensure test isolation. */
  @AfterEach
  void clear() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /**
   * Tests that hints are returned in the correct order for each entity and that the system moves to
   * the next entity after the last hint.
   */
  @Test
  void testNextHintOrder() {
    assertEquals(hint1_1, hintSystem.nextHint().get());
    hintSystem.execute();
    assertEquals(hint1_2, hintSystem.nextHint().get());
    hintSystem.execute();
    assertEquals(hint2_1, hintSystem.nextHint().get());
    hintSystem.execute();
    assertEquals(hint1_1, hintSystem.nextHint().get());
    hintSystem.execute();
    assertEquals(hint1_2, hintSystem.nextHint().get());
  }

  /**
   * Tests that entities without tokens are not added to the hint queue and are removed if already
   * present.
   */
  @Test
  void testEntityRemovalOnZeroTokens() {
    assertEquals(hint1_1, hintSystem.nextHint().get());
    hintSystem.execute();
    entity1.fetch(PlaceComponent.class).ifPresent(p -> p.consume(p.tokenCount()));
    hintSystem.execute();
    assertEquals(hint2_1, hintSystem.nextHint().get());
    entity2.fetch(PlaceComponent.class).ifPresent(p -> p.consume(p.tokenCount()));
    hintSystem.execute();
    assertTrue(hintSystem.nextHint().isEmpty());
  }

  /** Tests that {@link HintSystem} returns "No more hints" if there are no entities with tokens. */
  @Test
  void testNoHintsWhenQueueEmpty() {
    entity1.fetch(PlaceComponent.class).ifPresent(p -> p.consume(p.tokenCount()));
    entity2.fetch(PlaceComponent.class).ifPresent(p -> p.consume(p.tokenCount()));
    hintSystem.execute();
    hintSystem.execute();
    assertTrue(hintSystem.nextHint().isEmpty());
  }

  /** Tests that hints are removed correctly when an entity is removed from the game. */
  @Test
  void testHintRemovalOnEntityRemove() {
    Game.remove(entity1);
    Game.remove(entity2);
    hintSystem.execute();
    assertTrue(hintSystem.nextHint().isEmpty());
  }

  /** Verifies that the telephone hint sequence is shared by all players in the room. */
  @Test
  void testPhoneHintProgressIsShared() {
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());

    assertEquals(hint1_1, hintSystem.acceptSharedHint().orElseThrow());
    assertEquals(hint1_2, hintSystem.peekSharedHint().orElseThrow());

    assertEquals(hint1_2, hintSystem.acceptSharedHint().orElseThrow());
    assertTrue(hintSystem.peekSharedHint().isEmpty());
  }

  /** Verifies that declining the confirmation leaves the shared hint available. */
  @Test
  void testDeclinedPhoneHintDoesNotAdvanceSharedProgress() {
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());
  }

  /** A stale phone offer must not consume the next shared hint. */
  @Test
  void stalePhoneOfferDoesNotAdvanceSharedProgress() {
    assertTrue(hintSystem.acceptSharedHint(hint1_2).isEmpty());
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());
  }

  /** Two confirmations for the same offer can advance the shared sequence only once. */
  @Test
  void duplicatePhoneOfferConfirmationAdvancesOnce() {
    assertEquals(hint1_1, hintSystem.acceptSharedHint(hint1_1).orElseThrow());
    assertTrue(hintSystem.acceptSharedHint(hint1_1).isEmpty());
    assertEquals(hint1_2, hintSystem.peekSharedHint().orElseThrow());
  }

  /** The debug view can inspect the shared counter without consuming a hint. */
  @Test
  void sharedProgressReportsAcceptedCountWithoutAdvancingIt() {
    HintSystem.SharedHintProgress initial = hintSystem.sharedProgress(entity1);
    assertEquals(0, initial.acceptedCount());
    assertEquals(2, initial.hintCount());

    hintSystem.acceptSharedHint(hint1_1).orElseThrow();
    HintSystem.SharedHintProgress afterAcceptance = hintSystem.sharedProgress(entity1);
    assertEquals(1, afterAcceptance.acceptedCount());
    assertEquals(2, afterAcceptance.hintCount());
  }

  /** An offer tied to a previous Petri place cannot consume an identical next-place hint. */
  @Test
  void confirmationMustMatchTheEntityThatOwnedTheOffer() {
    assertTrue(hintSystem.acceptSharedHint(entity2.id(), hint2_1).isEmpty());
    assertEquals(hint1_1, hintSystem.peekSharedHint().orElseThrow());

    assertEquals(hint1_1, hintSystem.acceptSharedHint(entity1.id(), hint1_1).orElseThrow());
  }
}
