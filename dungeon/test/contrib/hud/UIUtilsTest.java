package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class UIUtilsTest {

  @Test
  public void formatString_1() {
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
            iiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor si
            t amet, consetetur sadipscing elitr, sed
            diam nonumy eirmod tempor invidunt ut la
            bore et dolore magna aliquyam erat, sed\s
            diam voluptua. At vero eos et accusam et
            justo duo dolores et ea rebum.""";
    String longTextExpected_no_wrap =
        """
              Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
              iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
              iiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor  \s
              sit amet, consetetur sadipscing elitr, \s
              sed diam nonumy eirmod tempor invidunt \s
              ut labore et dolore magna aliquyam erat,
              sed diam voluptua. At vero eos et      \s
              accusam et justo duo dolores et ea     \s
              rebum.""";

    assertEquals(nearlyEmptyTextExpected, UIUtils.formatString(nearlyEmptyText, true));
    assertEquals(nearlyEmptyTextExpected, UIUtils.formatString(nearlyEmptyText, false));
    assertEquals(longTextExpected_wrap, UIUtils.formatString(longText, true));
    assertEquals(longTextExpected_no_wrap, UIUtils.formatString(longText, false));
  }

  @Test
  public void formatString_2() {
    String mediumText = "hallo";
    String mediumTextExpected = "hallo";

    assertEquals(mediumTextExpected, UIUtils.formatString(mediumText, true));
    assertEquals(mediumTextExpected, UIUtils.formatString(mediumText, false));
  }

  @Test
  public void formatString_3() {
    int max = 40;
    for (int i = 0; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      assertEquals(textExpected, UIUtils.formatString(text, false));
    }

    for (int i = 0; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      assertEquals(textExpected, UIUtils.formatString(text, true));
    }

    String text = "a".repeat(max + 1);

    assertNotEquals(text, UIUtils.formatString(text, false));
    assertNotEquals("a".repeat(max) + " ", UIUtils.formatString(text, false));
  }
}
