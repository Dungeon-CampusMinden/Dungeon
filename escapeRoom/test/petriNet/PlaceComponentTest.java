package petriNet;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PlaceComponent}.
 *
 * <p>Tests basic token operations: adding, removing, and querying the token count.
 */
public class PlaceComponentTest {

  private PlaceComponent place;

  @BeforeEach
  void setUp() {
    place = new PlaceComponent();
  }

  /** Tests that a new place has zero tokens. */
  @Test
  void testInitialCounter() {
    assertEquals(0, place.tokenCount(), "New place should start with 0 tokens");
  }

  /** Tests that adding a token increments the counter. */
  @Test
  void testProduceToken() {
    place.produce();
    assertEquals(1, place.tokenCount(), "Counter should be 1 after adding a token");
    place.produce();
    assertEquals(2, place.tokenCount(), "Counter should be 2 after adding another token");
  }

  /** Tests that removing a token decrements the counter. */
  @Test
  void testConsumeToken() {
    place.produce();
    place.produce();
    boolean b = place.consume();
    assertEquals(1, place.tokenCount(), "Counter should be 1 after removing a token");
    assertTrue(b);
    b = place.consume();
    assertEquals(0, place.tokenCount(), "Counter should be 0 after removing another token");
    assertTrue(b);
  }

  /** Tests that removing does not allow the counter to go below zero. */
  @Test
  void testConsumeDoesNotGoNegative() {
    assertEquals(0, place.tokenCount(), "Counter should be 0");
    boolean b = place.consume();
    assertEquals(0, place.tokenCount(), "Counter should still be 0");
    assertFalse(b);
  }
}
