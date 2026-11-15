package rooms.morphco.puzzles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Word-class sorting next to the inflection machine F-04.
 *
 * <p>Only inflectable words belong into one of the five crates. Adverbs, particles, conjunctions
 * and prepositions stay on the table. The model is shared by the authoritative server, which checks
 * submitted placements, and the client, which renders the sentences.
 */
public final class WordClassPuzzle {

  /** Crates beside the inflection machine, in the order they are printed in the script. */
  public enum Crate {
    /** Verbs. */
    VERBEN,
    /** Adjectives. */
    ADJEKTIVE,
    /** Nouns. */
    NOMEN,
    /** Articles. */
    ARTIKEL,
    /** Pronouns. */
    PRONOMINA;

    /**
     * Parses a crate name sent by a client.
     *
     * @param name crate name
     * @return the crate, or empty for unknown names
     */
    public static Optional<Crate> parse(String name) {
      for (Crate crate : values()) {
        if (crate.name().equals(name)) return Optional.of(crate);
      }
      return Optional.empty();
    }
  }

  /**
   * One word of the task.
   *
   * @param id stable identifier used in client answers
   * @param text word as printed in the sentence
   * @param sentence index of the sentence that contains the word
   * @param target crate the word belongs into, or empty if it stays on the table
   */
  public record Word(String id, String text, int sentence, Optional<Crate> target) {}

  /**
   * Outcome of a submitted placement.
   *
   * @param solved whether every word lies where it belongs
   * @param misplaced words that lie in a wrong crate or in a crate although they belong nowhere
   * @param missing inflectable words that still lie on the table
   */
  public record Result(boolean solved, int misplaced, int missing) {}

  private static final List<Word> WORDS = createWords();

  private WordClassPuzzle() {}

  private static List<Word> createWords() {
    List<Word> words = new ArrayList<>();
    sentence(
        words,
        0,
        "Ein",
        Crate.ARTIKEL,
        "Hamster",
        Crate.NOMEN,
        "kommt",
        Crate.VERBEN,
        "selten",
        null,
        "zu",
        null,
        "spät",
        Crate.ADJEKTIVE);
    sentence(
        words,
        1,
        "Grüne",
        Crate.ADJEKTIVE,
        "Törtchen",
        Crate.NOMEN,
        "sind",
        Crate.VERBEN,
        "äußerst",
        null,
        "lecker",
        Crate.ADJEKTIVE,
        "gewesen",
        Crate.VERBEN);
    sentence(
        words,
        1,
        "und",
        null,
        "diese",
        Crate.PRONOMINA,
        "schmecken",
        Crate.VERBEN,
        "gut",
        Crate.ADJEKTIVE);
    return Collections.unmodifiableList(words);
  }

  private static void sentence(List<Word> words, int sentence, Object... pairs) {
    // The script prints the second sentence on two table rows; both parts share its numbering.
    int offset = (int) words.stream().filter(word -> word.sentence() == sentence).count();
    for (int index = 0; index < pairs.length; index += 2) {
      words.add(
          new Word(
              "s" + sentence + "w" + (offset + index / 2),
              (String) pairs[index],
              sentence,
              Optional.ofNullable((Crate) pairs[index + 1])));
    }
  }

  /**
   * Returns all words of the two sentences in reading order.
   *
   * @return immutable word list
   */
  public static List<Word> words() {
    return WORDS;
  }

  /**
   * Returns the number of sentences.
   *
   * @return sentence count
   */
  public static int sentenceCount() {
    return WORDS.getLast().sentence() + 1;
  }

  /**
   * Checks a placement of words in crates.
   *
   * @param placement crate of every word that lies in a crate; unknown word IDs are ignored
   * @return how close the placement is to the solution
   */
  public static Result check(Map<String, Crate> placement) {
    int misplaced = 0;
    int missing = 0;
    for (Word word : WORDS) {
      Crate placed = placement.get(word.id());
      if (word.target().isEmpty()) {
        if (placed != null) misplaced++;
      } else if (placed == null) {
        missing++;
      } else if (placed != word.target().get()) {
        misplaced++;
      }
    }
    return new Result(misplaced == 0 && missing == 0, misplaced, missing);
  }

  /**
   * Encodes a placement for a dialog response, e.g. {@code s0w0=ARTIKEL;s0w1=NOMEN}.
   *
   * @param placement crate of every word that lies in a crate
   * @return encoded placement
   */
  public static String encode(Map<String, Crate> placement) {
    List<String> parts = new ArrayList<>();
    placement.forEach((word, crate) -> parts.add(word + "=" + crate.name()));
    Collections.sort(parts);
    return String.join(";", parts);
  }

  /**
   * Decodes a placement produced by {@link #encode(Map)}; malformed parts are skipped.
   *
   * @param encoded encoded placement
   * @return decoded placement
   */
  public static Map<String, Crate> decode(String encoded) {
    Map<String, Crate> placement = new LinkedHashMap<>();
    if (encoded == null || encoded.isBlank()) return placement;
    for (String part : encoded.split(";")) {
      int separator = part.indexOf('=');
      if (separator <= 0) continue;
      String word = part.substring(0, separator);
      Crate.parse(part.substring(separator + 1)).ifPresent(crate -> placement.put(word, crate));
    }
    return placement;
  }
}
