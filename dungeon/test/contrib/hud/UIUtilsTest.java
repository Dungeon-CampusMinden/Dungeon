package contrib.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Test {@link UIUtils#formatString} for the expected behaviour. */
public class UIUtilsTest {

  /** Check whether the text has been cleaned up correctly. */
  @Test
  public void sanitizeText() {
    assertEquals("", UIUtils.formatString(""));
    assertEquals("abcd", UIUtils.formatString("abcd"));
    assertEquals("abcd", UIUtils.formatString(" abcd"));
    assertEquals("abcd", UIUtils.formatString("abcd "));
    assertEquals("ab cd", UIUtils.formatString("ab cd"));
    assertEquals("ab cd ef", UIUtils.formatString("  ab  cd \n ef  "));
  }

  /** Text with short words (less than line length) should be wrapped between words. */
  @Test
  public void wrapShorterWords() {
    assertEquals("abc\ndef\nghi\njk", UIUtils.formatString("abc def ghi jk", 5));
  }

  /** Text with short words (equal to line length) should be wrapped between words. */
  @Test
  public void wrapLineLengthWords() {
    assertEquals("abcde\nfghij\nk", UIUtils.formatString("abcde fghij k", 5));
  }

  /**
   * Words exceeding line length limit should be word-wrapped.
   *
   * <p>Long word at the beginning of the text.
   */
  @Test
  public void wrapTooLongWordAtStart() {
    assertEquals("abcde\nf ghi\njkl", UIUtils.formatString("abcdef ghi jkl", 5));
  }

  /**
   * Words exceeding line length limit should be word-wrapped.
   *
   * <p>Long word at the middle of the text.
   */
  @Test
  public void wrapTooLongWordInMiddle() {
    assertEquals("abc d\nefghi\njkl", UIUtils.formatString("abc defghi jkl", 5));
  }

  /**
   * Words exceeding line length limit should be word-wrapped.
   *
   * <p>Long word at the end of the text.
   */
  @Test
  public void wrapTooLongWordAtEnd() {
    assertEquals("abc\ndef g\nhijkl", UIUtils.formatString("abc def ghijkl", 5));
  }

  /**
   * Words exceeding line length limit should be word-wrapped.
   *
   * <p>Word with multiple line length at the beginning of the text, another long word at the end of
   * the text.
   */
  @Test
  public void wrapReallyLongWordAtStart() {
    assertEquals(
        "abcde\nfghij\nklmno\npq rs\ntuvwx", UIUtils.formatString("abcdefghijklmnopq rstuvwx", 5));
  }

  /**
   * Minimum Working Example for regression test.
   *
   * <p>@see <a
   * href=https://github.com/Dungeon-CampusMinden/Dungeon/issues/1460>Dungeon-CampusMinden/Dungeon/#1460</a>
   */
  @Test
  public void regression1460() {
    String input =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiipsum
            dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
            et justo duo dolores et ea

            rebum.   \s
            """;

    String expected =
        """
            Lorem iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
            iiiiiiiiiiiiiiiiiiiiiiiiiiipsum dolor
            sit amet, consetetur sadipscing elitr,
            sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat,
            sed diam voluptua. At vero eos et
            accusam et justo duo dolores et ea
            rebum.""";

    assertEquals(expected, UIUtils.formatString(input, 40));
  }

  /** Wrapping {@code null} should result in {@code null}. */
  @Test
  public void formatTextWithNullTextArgument() {
    assertNull(UIUtils.formatString(null));
  }

  /** Wraping empty strings should yield empty strings. */
  @Test
  public void formatTextWithEmptyText() {
    assertEquals("", UIUtils.formatString(""));
  }

  /** Wrapping a single space should yield an empty string. */
  @Test
  public void formatTextWithOneSpace() {
    assertEquals("", UIUtils.formatString(" "));
  }

  /** Wrapping a single word shorter than line length should yield this word. */
  @Test
  public void formatTextWithShortText() {
    String shortText = "hallo";
    String shortTextExpected = "hallo";

    assertEquals(shortTextExpected, UIUtils.formatString(shortText));
  }
}
