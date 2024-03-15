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
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.  \s

            Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  \s

            Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse
            """;
    String longTextExpected =
        """
            Lorem                                            \s
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            ipsum dolor sit amet, consetetur sadipscing      \s
            sed diam nonumy eirmod tempor invidunt ut        \s
            et dolore magna aliquyam erat, sed diam          \s
            At vero eos et accusam et justo duo dolores      \s
            ea rebum. Stet clita kasd gubergren, no          \s
            takimata sanctus est Lorem ipsum dolor sit       \s
            Lorem ipsum dolor sit amet, consetetur sadipscing\s
            sed diam nonumy eirmod tempor invidunt ut        \s
            et dolore magna aliquyam erat, sed diam          \s
            At vero eos et accusam et justo duo dolores      \s
            ea rebum. Stet clita kasd gubergren, no          \s
            takimata sanctus est Lorem ipsum dolor sit       \s
            Lorem ipsum dolor sit amet, consetetur sadipscing\s
            sed diam nonumy eirmod tempor invidunt ut        \s
            et dolore magna aliquyam erat, sed diam          \s
            At vero eos et accusam et justo duo dolores      \s
            ea rebum. Stet clita kasd gubergren, no          \s
            takimata sanctus est Lorem ipsum dolor sit       \s
            Duis autem vel eum iriure dolor in hendrerit     \s
            vulputate velit esse molestie consequat,         \s
            illum dolore eu feugiat nulla facilisis          \s
            vero eros et accumsan et iusto odio dignissim    \s
            blandit praesent luptatum zzril delenit          \s
            duis dolore te feugait nulla facilisi. Lorem     \s
            dolor sit amet, consectetuer adipiscing          \s
            sed diam nonummy nibh euismod tincidunt          \s
            laoreet dolore magna aliquam erat volutpat.      \s
            wisi enim ad minim veniam, quis nostrud          \s
            tation ullamcorper suscipit lobortis nisl        \s
            aliquip ex ea commodo consequat. Duis autem      \s
            eum iriure dolor in hendrerit in vulputate       \s
            esse                                             \s""";

    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText));
    assertEquals(longTextExpected, UIUtils.formatString(longText));

    System.out.println(UIUtils.formatString(longText));
  }

  @Test
  public void formatString_2() {
    String emptyText = "";
    String emptyTextExpected = "";
    String longText = "hallo";
    String longTextExpected = "hallo";

    assertEquals(emptyTextExpected, UIUtils.formatString(emptyText, false));
    assertEquals(longTextExpected, UIUtils.formatString(longText, false));

    System.out.println(UIUtils.formatString(longText));
  }

  @Test
  public void formatString_3() {
    for (int i = 0; i <= 40; i++) {
      StringBuilder text = new StringBuilder();
      text.append("a".repeat(i));
      assertEquals(text.toString(), UIUtils.formatString(text.toString(), false, 1.0));
    }
    StringBuilder text = new StringBuilder();
    text.append("a".repeat(41));
    assertNotEquals(text.toString(), UIUtils.formatString(text.toString(), false, 1.0));
  }
}
