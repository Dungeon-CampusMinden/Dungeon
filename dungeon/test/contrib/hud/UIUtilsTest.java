package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import org.junit.Test;

public class UIUtilsTest {

  @Test
  public void formatString_1() {
    char[] temp = new char[40];
    Arrays.fill(temp, ' ');
    String emptyText = " ";
    String emptyTextExpected = new String(temp);
    String longText =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum
            dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus
            est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
            sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed
            diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita
            kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum
            dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
            labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et
            justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est
            Lorem ipsum dolor sit amet.   \s

            Duis autem vel eum iriure dolor in hendrerit in                                                 \s
            """;
    String longTextExpected =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor si
            t amet, consetetur sadipscing elitr, sed
            diam nonumy eirmod tempor invidunt ut la
            bore et dolore magna aliquyam erat, sed\s
            diam voluptua. At vero eos et accusam et
            justo duo dolores et ea rebum. Stet clit
            a kasd gubergren, no sea takimata sanctu
            s est Lorem ipsum dolor sit amet. Lorem\s
            ipsum dolor sit amet, consetetur sadipsc
            ing elitr, sed diam nonumy eirmod tempor
            invidunt ut labore et dolore magna aliqu
            yam erat, sed diam voluptua. At vero eos
            et accusam et justo duo dolores et ea re
            bum. Stet clita kasd gubergren, no sea t
            akimata sanctus est Lorem ipsum dolor si
            t amet. Lorem ipsum dolor sit amet, cons
            etetur sadipscing elitr, sed diam nonumy
            eirmod tempor invidunt ut labore et dolo
            re magna aliquyam erat, sed diam voluptu
            a. At vero eos et accusam et justo duo d
            olores et ea rebum. Stet clita kasd gube
            rgren, no sea takimata sanctus est Lorem
            ipsum dolor sit amet. Duis autem vel eum
            iriure dolor in hendrerit in""";

    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText));
    assertEquals(longTextExpected, UIUtils.formatString(longText));
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
    for (int i = 1; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      if (i == 39) {
        textExpected += " ";
      }

      assertEquals(textExpected, UIUtils.formatString(text, false));
    }

    for (int i = 1; i <= max; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i);

      if (i == 39) {
        textExpected += " ";
      }

      assertEquals(textExpected, UIUtils.formatString(text, true));
    }

    StringBuilder text = new StringBuilder();
    text.append("a".repeat(41));
    assertNotEquals(text.toString(), UIUtils.formatString(text.toString(), false));
  }
}
