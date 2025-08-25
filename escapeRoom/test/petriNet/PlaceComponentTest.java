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

  /** Tests that producing multiple tokens increases the counter by the specified amount. */
  @Test
  void testProduceMultipleTokens() {
    place.produce(3);
    assertEquals(3, place.tokenCount(), "Counter should be 3 after producing 3 tokens");
    place.produce(2);
    assertEquals(5, place.tokenCount(), "Counter should be 5 after producing 2 more tokens");
  }

  /**
   * Tests that consuming multiple tokens decreases the counter by the specified amount when enough
   * tokens are available.
   */
  @Test
  void testConsumeMultipleTokensSuccess() {
    place.produce(5);
    boolean result = place.consume(3);
    assertTrue(result, "Consume should succeed when enough tokens are available");
    assertEquals(2, place.tokenCount(), "Counter should be 2 after consuming 3 tokens from 5");
  }

  /** Tests that consuming more tokens than available fails and does not change the counter. */
  @Test
  void testConsumeMultipleTokensFail() {
    place.produce(2);
    boolean result = place.consume(5);
    assertFalse(result, "Consume should fail when not enough tokens are available");
    assertEquals(2, place.tokenCount(), "Counter should remain unchanged when consume fails");
  }

  /**
   * Tests that consuming the exact number of available tokens succeeds and leaves the counter at
   * zero.
   */
  @Test
  void testConsumeExactAmount() {
    place.produce(4);
    boolean result = place.consume(4);
    assertTrue(result, "Consume should succeed when consuming all available tokens");
    assertEquals(0, place.tokenCount(), "Counter should be 0 after consuming all tokens");
  }
}
