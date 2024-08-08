package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;

/**
 * RegexRiddle is an enum that contains riddles about regular expressions. Each riddle is a
 * SingleChoice question with a question and a list of possible answers. The correct answer is
 * obfuscated by counting the occurrences of a character in a string.
 *
 * @see level.devlevel.riddleHandler.BridgeGuardRiddleHandler BridgeGuardRiddleHandler
 * @see task.tasktype.quizquestion.SingleChoice SingleChoice
 */
public enum RegexRiddle {
  /** Introduction question. */
  READY_0(
      "Hello there! I'm the bridge guard. Behind this bridge is a hidden and powerful artifact."
          + " To pass, you must answer my Regex riddles. Let's start with an easy one: "
          + "Which regex pattern matches 'abc' exactly?",
      Arrays.asList("abc", "a.b", "a*c", "[abc]"),
      countChar("lll23l1l4l1ll41ll124l124lll", 'I')),
  /** Question about character classes. */
  CHAR_CLASS_0(
      "Which regex matches any single digit?",
      Arrays.asList(".", "\\w", "\\s", "\\d"),
      countChar("lll223l1l4l1ll41ll14l124lll", '2')),
  /** Question about quantifiers. */
  QUANTIFIERS_0(
      "Which regex pattern matches three alphabetic characters in a row?",
      Arrays.asList(".{3}", "\\w{3}", "[a-zA-Z]{3}", "\\D{3}"),
      countChar("lll23l1l4l1ll41ll14l124lll", '2')),
  /** Question about wildcards. */
  WILDCARDS_0(
      "Which regex matches any character except newline?",
      Arrays.asList("\\n", ".", "\\w", "\\S"),
      countChar("562hzh2hh2qlot298GGQ", 'z')),
  /** Question about escape characters. */
  ESCAPE_0(
      "Which of these patterns matches a period (.) character exactly?",
      Arrays.asList(".", "\\.", "[^.]", "(.)"),
      countChar("lll23l1l4l1Il41ll124l124lil", 'I')),
  /** Question about anchors. */
  ANCHORS_0(
      "Which regex matches a line that starts and ends with the same word?",
      Arrays.asList("^(\\w+).*\\1$", "^\\b.*\\b$", "^(\\w+)\\1$", "^\\w+$"),
      countChar("lvl13v1l4l1ll41wl14l1w4lwwvv", '2')),
  /** Question about groups and capture. */
  GROUP_CAPTURE_0(
      "Which pattern finds 'cat' or 'dog'?",
      Arrays.asList("[cat|dog]", "(cat|dog)", "{cat,dog}", "/cat|dog/"),
      countChar("lvvI23l1l4l1ll41ll124l124lll", 'I')),
  /** Question about greedy and lazy quantifiers. */
  GREEDY_LAZY_0(
      "Which regex performs a lazy match for '<tag>content</tag>'?",
      Arrays.asList("<.*>", "<.+!>", "<.*?>", "<.+>"),
      countChar("p23l1l4l11II4lvv14l124lbp", '2')),
  /** Question about lookaheads and lookbehinds. */
  LOOKAHEAD_BEHIND_0(
      "Which regex uses a positive lookahead to ensure 'q' is followed by 'u'?",
      Arrays.asList("q(?=u)", "q(u)?", "q[!u]", "q{u}"),
      countChar("l23l1l4l1ll41ll124l124lll", 'I')),
  /** Question about back references. */
  PRACTICAL_0(
      "Which regex validates an email address (simplified version)?",
      Arrays.asList("\\w+@\\w+\\.\\w+", ".+@.+\\..+", "[^@]+@[^\\.]+\\..+", "\\S+@\\S+\\.\\S+"),
      countChar("l23lIl4l1ll4IllI24lI24lll", '1')),
  /** Question about grouping and word boundaries. */
  GROUPING_0(
      "Which regex matches a word boundary followed by 'cat' or 'dog' not followed by 'fish'?",
      Arrays.asList(
          "\\b(cat|dog)(?!fish)",
          "\\b(?:cat|dog)(?!fish)",
          "(\\bcat|dog)(?!fish)",
          "\\b(cat|dog)\\Bfish"),
      countChar("l23l1l4l1ll41lI124l124lll", 'I'));

  private final SingleChoice riddle;

  RegexRiddle(String question, List<String> answers, int correctAnswerIndex) {
    this.riddle = new SingleChoice(question);
    riddle.addAnswer(answers.stream().map(Quiz.Content::new).toArray(Quiz.Content[]::new));
    riddle.addCorrectAnswerIndex(correctAnswerIndex);
  }

  /**
   * Get a list of random riddles. The first riddle must be a READY_ riddle. And amount - 1 riddles
   * are randomly selected from the rest of the riddles and shuffled.
   *
   * @param amount The amount of riddles to get
   * @return A list of random riddles
   * @throws IllegalArgumentException If the amount is less than 1 or greater than the amount of
   *     riddles
   */
  public static List<SingleChoice> getRandRiddles(int amount) {
    if (amount < 1 || amount > values().length) {
      throw new IllegalArgumentException("Amount must be between 1 and " + values().length);
    }

    List<RegexRiddle> startRiddles = new ArrayList<>();
    List<RegexRiddle> restRiddles = new ArrayList<>();

    for (RegexRiddle riddle : RegexRiddle.values()) {
      if (riddle.name().startsWith("READY_")) {
        startRiddles.add(riddle);
      } else {
        restRiddles.add(riddle);
      }
    }
    Collections.shuffle(startRiddles);
    Collections.shuffle(restRiddles);

    List<RegexRiddle> selectedRiddles = new ArrayList<>();
    selectedRiddles.add(startRiddles.getFirst());
    selectedRiddles.addAll(restRiddles.subList(0, amount - 1));

    List<SingleChoice> devRiddles = new ArrayList<>();
    for (RegexRiddle regexRiddle : selectedRiddles) {
      regexRiddle.riddle.shuffleAnswers();
      devRiddles.add(regexRiddle.riddle);
    }

    return devRiddles;
  }

  /**
   * Count the occurrences of a character in a string
   *
   * <p>This method is used to obfuscate the correct answer index in the riddles.
   *
   * @param str The string to count the character in
   * @param cha The character to count
   * @return The amount of occurrences of the character in the string
   */
  private static int countChar(String str, char cha) {
    return (int) str.chars().filter(ch -> ch == cha).count();
  }
}
