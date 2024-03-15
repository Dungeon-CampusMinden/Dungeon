package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import org.junit.Test;

public class UIUtilsTest {

  @Test
  public void formatString_1() {
    char[] temp = new char[(int) (40 * 1.25)];
    Arrays.fill(temp, ' ');
    String emptyText = "";
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
            Lorem                                            \s
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            ipsum dolor sit amet, consetetur sadipscing      \s
            elitr, sed diam nonumy eirmod tempor invidunt    \s
            ut labore et dolore magna aliquyam erat,         \s
            sed diam voluptua. At vero eos et accusam        \s
            et justo duo dolores et ea rebum. Stet clita     \s
            kasd gubergren, no sea takimata sanctus          \s
            est Lorem ipsum dolor sit amet. Lorem ipsum      \s
            dolor sit amet, consetetur sadipscing elitr,     \s
            sed diam nonumy eirmod tempor invidunt ut        \s
            labore et dolore magna aliquyam erat, sed        \s
            diam voluptua. At vero eos et accusam et         \s
            justo duo dolores et ea rebum. Stet clita        \s
            kasd gubergren, no sea takimata sanctus          \s
            est Lorem ipsum dolor sit amet. Lorem ipsum      \s
            dolor sit amet, consetetur sadipscing elitr,     \s
            sed diam nonumy eirmod tempor invidunt ut        \s
            labore et dolore magna aliquyam erat, sed        \s
            diam voluptua. At vero eos et accusam et         \s
            justo duo dolores et ea rebum. Stet clita        \s
            kasd gubergren, no sea takimata sanctus          \s
            est Lorem ipsum dolor sit amet. Duis autem       \s
            vel eum iriure dolor in hendrerit in             \s""";

    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText));
    assertEquals(longTextExpected, UIUtils.formatString(longText));
  }

  @Test
  public void formatString_2() {
    String emptyText = "";
    String emptyTextExpected = "";
    String longText = "hallo";
    String longTextExpected = "hallo                                             ";

    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText, false));
    assertEquals(longTextExpected, UIUtils.formatString(longText, false));
  }

  @Test
  public void formatString_3() {
    assertEquals("", UIUtils.formatString("", false, 1.0));

    for (int i = 1; i <= 40; i++) {
      String text = "a".repeat(i);
      String textExpected = "a".repeat(i) + " ".repeat(40 - i);
      assertEquals(textExpected, UIUtils.formatString(text, false, 1.0));
    }

    StringBuilder text = new StringBuilder();
    text.append("a".repeat(41));
    assertNotEquals(text.toString(), UIUtils.formatString(text.toString(), false, 1.0));
  }
}
