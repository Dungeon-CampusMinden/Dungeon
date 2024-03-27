package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/** This class tests the {@link UIUtils} class for expected behaviour. */
public class UIUtilsTest {

  /**
   * Tests that {@link UIUtils#formatString(String)} returns an empty string for an empty string.
   */
  @Test
  public void formatTextWithEmptyText() {
    String emptyText = "";
    String emptyTextExpected = "";
    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText));
  }

  /** Tests that {@link UIUtils#formatString(String)} returns an empty string for a single space. */
  @Test
  public void formatTextWithOneSpace() {
    String oneSpace = " ";
    String oneSpaceExpected = "";
    assertEquals(oneSpaceExpected, UIUtils.formatString(oneSpace));
  }

  /**
   * Tests that {@link UIUtils#formatString(String)} returns the expected string for a regular text.
   */
  @Test
  public void formatTextWithRegularText() {
    String regularText =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum
            dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea

            rebum.   \s
            """;
    String regularTextExpected =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor\s
            sit amet, consetetur sadipscing elitr,\s
            sed diam nonumy eirmod tempor invidunt\s
            ut labore et dolore magna aliquyam erat,
            sed diam voluptua. At vero eos et\s
            accusam et justo duo dolores et ea\s
            rebum.""";
    assertEquals(regularTextExpected, UIUtils.formatString(regularText));
  }

  /**
   * Tests that {@link UIUtils#formatString(String)} returns the expected string for a short text.
   */
  @Test
  public void formatTextWithShortText() {
    String shortText = "hallo";
    String shortTextExpected = "hallo";

    assertEquals(shortTextExpected, UIUtils.formatString(shortText));
  }

  /**
   * Tests that {@link UIUtils#formatString(String)} returns the expected string for a generic text.
   */
  @Test
  public void formatTextWithShortGenericText() {
    int max = 40;

    for (int i = 0; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      assertEquals(textExpected, UIUtils.formatString(text));
    }

    assertNotEquals("a".repeat(max + 1), UIUtils.formatString("a".repeat(max + 1)));

    assertNotEquals("a".repeat(max) + " ", UIUtils.formatString("a".repeat(max + 1)));
  }
}
