package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class UIUtilsTest {

  /**
   * Creates line breaks after a word once a certain character count is reached.
   *
   * @param string String which should be reformatted.
   */
  public static String oldFormatStringMethodForComparison(final String string) {
    final int MAX_ROW_LENGTH = 40;

    StringBuilder formattedMsg = new StringBuilder();
    String[] lines = string.split(System.lineSeparator());

    for (String line : lines) {
      String[] words = line.split(" ");
      int sumLength = 0;

      for (String word : words) {
        sumLength += word.length();
        formattedMsg.append(word);
        formattedMsg.append(" ");

        if (sumLength > MAX_ROW_LENGTH) {
          formattedMsg.append(System.lineSeparator());
          sumLength = 0;
        }
      }
      formattedMsg.append(System.lineSeparator());
    }
    return formattedMsg.toString().trim();
  }

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
            iiiiiiipsum dolor\s
            sit amet, consetetur
            sadipscing elitr,\s
            sed diam nonumy\s
            eirmod tempor\s
            invidunt ut labore\s
            et dolore magna\s
            aliquyam erat, sed\s
            diam voluptua. At\s
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

  @Test
  public void compareOldAndNew() {
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

    String longTextNotExpected =
        """
              Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum
              dolor\s
              sit amet, consetetur sadipscing elitr, sed diam\s
              nonumy eirmod tempor invidunt
              ut labore et dolore\s
              magna aliquyam erat, sed diam voluptua. At vero eos\s
              et accusam
              et justo duo dolores et ea

              rebum.""";

    assertEquals(longTextExpected_wrap, UIUtils.formatText(longText, 40, true));

    // assertEquals(longTextNotExpected, oldFormatStringMethodForComparison(longText));

    // assertEquals(longTextExpected_wrap, oldFormatStringMethodForComparison(longText));
  }
}
