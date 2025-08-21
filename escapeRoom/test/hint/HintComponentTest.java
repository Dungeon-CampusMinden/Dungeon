package hint;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HintComponent}.
 *
 * <p>Tests cover initialization, hint retrieval by index, behavior of {@link HintComponent#hint()},
 * proper index incrementing using {@link HintComponent#increaseIndex()}, and correct handling when
 * the last hint has been shown.
 */
public class HintComponentTest {

  /**
   * Tests that the constructor correctly initializes a HintComponent with valid hints. Verifies
   * that the initial next hint is the first hint in the array.
   */
  @Test
  void testConstructorWithValidHints() {
    HintComponent hc = new HintComponent("hint1", "hint2");
    assertNotNull(hc, "HintComponent should be created with valid hints");
    assertEquals("hint1", hc.hint(), "Next hint should initially be the first hint");
  }

  /** Tests that the constructor throws an IllegalArgumentException when called with no hints. */
  @Test
  void testConstructorWithEmptyHintsThrowsException() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new HintComponent(),
            "Constructor should throw IllegalArgumentException for empty hints");
    assertEquals("Hints must not be null or empty", ex.getMessage());
  }

  /**
   * Tests that the {@link HintComponent#hint(int)} method returns the correct hint for valid
   * indices and throws an exception for invalid indices.
   */
  @Test
  void testHintByIndex() {
    HintComponent hc = new HintComponent("a", "b", "c");
    assertEquals("a", hc.hint(0));
    assertEquals("b", hc.hint(1));
    assertEquals("c", hc.hint(2));

    assertThrows(IllegalArgumentException.class, () -> hc.hint(-1));
    assertThrows(IllegalArgumentException.class, () -> hc.hint(3));
  }

  /**
   * Tests {@link HintComponent#hint()} in combination with {@link HintComponent#increaseIndex()}.
   * Verifies that the next hint is returned correctly and that after the last hint, nextHint()
   * returns an empty string and {@link HintComponent#isLastHintShown()} is true.
   */
  @Test
  void testNextHintAndIncreaseIndex() {
    HintComponent hc = new HintComponent("first", "second");

    // Initially, nextHint returns first hint
    assertEquals("first", hc.hint());
    assertFalse(hc.isLastHintShown());

    // Increase index and get next hint
    hc.increaseIndex();
    assertEquals("second", hc.hint());
    assertFalse(hc.isLastHintShown());

    // Increase index beyond last hint
    hc.increaseIndex();
    assertEquals("", hc.hint(), "Next hint should be empty after last hint");
    assertTrue(hc.isLastHintShown());
  }

  /**
   * Tests multiple calls to {@link HintComponent#increaseIndex()} to ensure that the internal index
   * does not break and {@link HintComponent#hint()} returns an empty string after exceeding the
   * last hint.
   */
  @Test
  void testMultipleIncreaseIndexCalls() {
    HintComponent hc = new HintComponent("x", "y");
    hc.increaseIndex();
    hc.increaseIndex();
    hc.increaseIndex(); // should not throw
    assertEquals("", hc.hint());
    assertTrue(hc.isLastHintShown());
  }
}
