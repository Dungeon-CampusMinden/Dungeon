package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class UIUtilsTest {

  @Test
  public void formatTextWithLongText() {
    int max = 20;
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
    String longTextExpected_no_wrap =
        """
            Lorem iiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiipsum dolor si
            t amet, consetetur s
            adipscing elitr, sed
            diam nonumy eirmod t
            empor invidunt ut la
            bore et dolore magna
            aliquyam erat, sed d
            iam voluptua. At ver
            o eos et accusam et\s
            justo duo dolores et
            ea rebum.""";
    String longTextExpected_wrap =
        """
            Lorem iiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiii
            iiiiiiipsum dolor  \s
            sit amet, consetetur
            sadipscing elitr,  \s
            sed diam nonumy    \s
            eirmod tempor      \s
            invidunt ut labore \s
            et dolore magna    \s
            aliquyam erat, sed \s
            diam voluptua. At  \s
            vero eos et accusam\s
            et justo duo dolores
            et ea rebum.""";

    assertEquals(nearlyEmptyTextExpected, UIUtils.formatText(nearlyEmptyText, max, true));
    assertEquals(nearlyEmptyTextExpected, UIUtils.formatText(nearlyEmptyText, max, false));
    assertEquals(longTextExpected_wrap, UIUtils.formatText(longText, max, true));
    assertEquals(longTextExpected_no_wrap, UIUtils.formatText(longText, max, false));
  }

  @Test
  public void formatTextWithMediumText() {
    int max1 = 5;
    int max2 = 6;
    String mediumText = "hallo";
    String mediumTextExpected = "hallo";

    assertEquals(mediumTextExpected, UIUtils.formatText(mediumText, max1, true));
    assertEquals(mediumTextExpected, UIUtils.formatText(mediumText, max1, false));
    assertEquals(mediumTextExpected, UIUtils.formatText(mediumText, max2, true));
    assertEquals(mediumTextExpected, UIUtils.formatText(mediumText, max2, false));
  }

  @Test
  public void formatTextWithShortGenericText() {
    int max = 5;
    for (int i = 0; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      assertEquals(textExpected, UIUtils.formatText(text, max, false));
    }

    for (int i = 0; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      assertEquals(textExpected, UIUtils.formatText(text, max, true));
    }

    String text = "a".repeat(max + 1);

    assertNotEquals(text, UIUtils.formatText(text, max, false));
    assertNotEquals("a".repeat(max) + " ", UIUtils.formatText(text, max, false));
  }
}
