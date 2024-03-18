package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class UIUtilsTest {
  @Test
  public void formatTextWithLongText() {
    String nearlyEmptyText = " ";
    String nearlyEmptyTextExpected = "";
    String longText =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum
            dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea

            rebum.   \s
            """;
    String longTextExpected_wrap =
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

    assertEquals(nearlyEmptyTextExpected, UIUtils.formatString(nearlyEmptyText));
    assertEquals(longTextExpected_wrap, UIUtils.formatString(longText));
  }

  @Test
  public void formatTextWithMediumText() {
    String mediumText = "hallo";
    String mediumTextExpected = "hallo";

    assertEquals(mediumTextExpected, UIUtils.formatString(mediumText));
  }

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
