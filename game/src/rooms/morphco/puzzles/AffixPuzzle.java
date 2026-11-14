package rooms.morphco.puzzles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The jammed affix sorter A-12.
 *
 * <p>Step one: players cut seven words into morphs and sort every affix into the prefix, suffix or
 * circumfix bin. Words without any affix go into their own bin as a whole. Stems stay where they
 * are. Step two: the freed affixes are pushed into the input shafts for word formation or
 * inflection.
 */
public final class AffixPuzzle {

  /** Bins of the first step. */
  public enum Bin {
    /** Words that contain no affix at all. */
    KEIN_AFFIX("K"),
    /** Prefixes. */
    PRAEFIX("P"),
    /** Suffixes. */
    SUFFIX("S"),
    /** Both parts of a circumfix. */
    ZIRKUMFIX("Z");

    private final String code;

    Bin(String code) {
      this.code = code;
    }

    /**
     * Returns the one-letter code used in encoded answers.
     *
     * @return answer code
     */
    public String code() {
      return code;
    }

    static Optional<Bin> fromCode(String code) {
      for (Bin bin : values()) {
        if (bin.code.equals(code)) return Optional.of(bin);
      }
      return Optional.empty();
    }
  }

  /** Input shafts of the second step. */
  public enum Shaft {
    /** Derivation: a new word is formed. */
    WORTBILDUNG,
    /** Inflection: a new word form is formed. */
    FLEXION;

    static Optional<Shaft> parse(String name) {
      for (Shaft shaft : values()) {
        if (shaft.name().equals(name)) return Optional.of(shaft);
      }
      return Optional.empty();
    }
  }

  /**
   * A placed morph.
   *
   * @param text morph as cut from the word, compared case-insensitively
   * @param bin bin the morph lies in
   */
  public record Piece(String text, Bin bin) {
    /**
     * Normalizes the morph text.
     *
     * @param text morph as cut from the word
     * @param bin bin the morph lies in
     */
    public Piece {
      text = text.toLowerCase(Locale.ROOT);
    }
  }

  /**
   * One word on the sorter's conveyor.
   *
   * @param id stable identifier used in client answers
   * @param text word as delivered to the sorter
   * @param expected morphs that must be placed, everything else stays in the word
   * @param alternatives further accepted placements, e.g. {@code sag-te-n} next to {@code sag-ten}
   */
  public record Word(
      String id, String text, List<Piece> expected, List<List<Piece>> alternatives) {}

  /**
   * A cut word together with the bins its parts were placed in.
   *
   * @param cuts positions between letters, each in {@code 1..text.length()-1}
   * @param bins bin of each part in reading order; {@code null} for parts that stay
   */
  public record WordAnswer(List<Integer> cuts, List<Bin> bins) {
    /**
     * Copies the lists; the bin list may contain {@code null}.
     *
     * @param cuts positions between letters
     * @param bins bin of each part in reading order
     */
    public WordAnswer {
      cuts = List.copyOf(cuts);
      bins = Collections.unmodifiableList(new ArrayList<>(bins));
    }
  }

  /**
   * Outcome of the first step.
   *
   * @param solved whether every word is handled correctly
   * @param correctWords number of correctly handled words
   * @param totalWords number of words
   */
  public record Result(boolean solved, int correctWords, int totalWords) {}

  /**
   * An affix that has been freed in the first step.
   *
   * @param id stable identifier used in client answers
   * @param label label in morpheme notation
   * @param target shaft the affix belongs into
   */
  public record FreedAffix(String id, String label, Shaft target) {}

  private static final List<Word> WORDS =
      List.of(
          word("w0", "unbezahlbar", "un:P", "be:P", "bar:S"),
          word("w1", "auf", "auf:K"),
          // The handout lists {-t}/{-et} as allomorphs, so ge-regne-t is accepted as well.
          word("w2", "geregnet", "ge:Z", "et:Z", "|", "ge:Z", "t:Z"),
          word("w3", "Störung", "ung:S"),
          // The script treats {-ten} as one suffix; sag-te-n is the finer analysis.
          word("w4", "sagten", "ten:S", "|", "te:S", "n:S"),
          word("w5", "Schornstein", "Schornstein:K"),
          word("w6", "Hamster", "Hamster:K"));

  private static final List<FreedAffix> AFFIXES =
      List.of(
          new FreedAffix("un", "{un-}", Shaft.WORTBILDUNG),
          new FreedAffix("be", "{be-}", Shaft.WORTBILDUNG),
          new FreedAffix("bar", "{-bar}", Shaft.WORTBILDUNG),
          new FreedAffix("ung", "{-ung}", Shaft.WORTBILDUNG),
          new FreedAffix("ten", "{-ten}", Shaft.FLEXION),
          new FreedAffix("ge_et", "{ge- ... -et}", Shaft.FLEXION));

  private AffixPuzzle() {}

  private static Word word(String id, String text, String... pieces) {
    List<List<Piece>> placements = new ArrayList<>();
    List<Piece> current = new ArrayList<>();
    for (String piece : pieces) {
      if ("|".equals(piece)) {
        placements.add(List.copyOf(current));
        current.clear();
        continue;
      }
      String[] parts = piece.split(":");
      current.add(new Piece(parts[0], Bin.fromCode(parts[1]).orElseThrow()));
    }
    placements.add(List.copyOf(current));
    return new Word(
        id, text, placements.getFirst(), List.copyOf(placements.subList(1, placements.size())));
  }

  /**
   * Returns the seven words of the first step.
   *
   * @return immutable word list
   */
  public static List<Word> words() {
    return WORDS;
  }

  /**
   * Returns the affixes of the second step.
   *
   * @return immutable affix list
   */
  public static List<FreedAffix> affixes() {
    return AFFIXES;
  }

  /**
   * Splits a word at the given cut positions.
   *
   * @param text word to split
   * @param cuts positions between letters; invalid and duplicate positions are ignored
   * @return the parts in reading order
   */
  public static List<String> split(String text, List<Integer> cuts) {
    List<Integer> sorted =
        cuts.stream()
            .filter(Objects::nonNull)
            .filter(cut -> cut > 0 && cut < text.length())
            .distinct()
            .sorted()
            .toList();
    List<String> parts = new ArrayList<>();
    int start = 0;
    for (int cut : sorted) {
      parts.add(text.substring(start, cut));
      start = cut;
    }
    parts.add(text.substring(start));
    return parts;
  }

  /**
   * Checks whether one word has been cut and sorted correctly.
   *
   * @param word word on the conveyor
   * @param answer cut positions and bins, or {@code null} if the word was not touched
   * @return whether exactly the expected morphs lie in the expected bins
   */
  public static boolean correct(Word word, WordAnswer answer) {
    if (answer == null) return false;
    List<String> parts = split(word.text(), answer.cuts());
    if (answer.bins().size() != parts.size()) return false;
    Map<Piece, Integer> placed = new HashMap<>();
    for (int index = 0; index < parts.size(); index++) {
      Bin bin = answer.bins().get(index);
      if (bin != null) placed.merge(new Piece(parts.get(index), bin), 1, Integer::sum);
    }
    if (placed.equals(count(word.expected()))) return true;
    return word.alternatives().stream().anyMatch(pieces -> placed.equals(count(pieces)));
  }

  private static Map<Piece, Integer> count(List<Piece> pieces) {
    Map<Piece, Integer> counted = new HashMap<>();
    pieces.forEach(piece -> counted.merge(piece, 1, Integer::sum));
    return counted;
  }

  /**
   * Checks the first step.
   *
   * @param answers answer per word ID; missing words count as wrong
   * @return number of correct words and whether the step is solved
   */
  public static Result check(Map<String, WordAnswer> answers) {
    int correct = 0;
    for (Word word : WORDS) {
      if (correct(word, answers.get(word.id()))) correct++;
    }
    return new Result(correct == WORDS.size(), correct, WORDS.size());
  }

  /**
   * Checks the second step.
   *
   * @param placement shaft of every affix ID
   * @return whether every affix lies in its shaft
   */
  public static boolean checkShafts(Map<String, Shaft> placement) {
    for (FreedAffix affix : AFFIXES) {
      if (placement.get(affix.id()) != affix.target()) return false;
    }
    return placement.size() == AFFIXES.size();
  }

  /**
   * Counts affixes that lie in the wrong shaft or in none.
   *
   * @param placement shaft of every affix ID
   * @return number of affixes that are not yet where they belong
   */
  public static int wrongShafts(Map<String, Shaft> placement) {
    int wrong = 0;
    for (FreedAffix affix : AFFIXES) {
      if (placement.get(affix.id()) != affix.target()) wrong++;
    }
    return wrong;
  }

  /**
   * Encodes answers of the first step, e.g. {@code w0:2,4,8:P,P,-,S;w1::K}.
   *
   * @param answers answer per word ID
   * @return encoded answers
   */
  public static String encodeWords(Map<String, WordAnswer> answers) {
    List<String> parts = new ArrayList<>();
    new TreeMap<>(answers)
        .forEach(
            (id, answer) -> {
              List<String> cuts = answer.cuts().stream().map(String::valueOf).toList();
              List<String> bins =
                  answer.bins().stream().map(bin -> bin == null ? "-" : bin.code()).toList();
              parts.add(id + ":" + String.join(",", cuts) + ":" + String.join(",", bins));
            });
    return String.join(";", parts);
  }

  /**
   * Decodes answers produced by {@link #encodeWords(Map)}; malformed parts are skipped.
   *
   * @param encoded encoded answers
   * @return answer per word ID
   */
  public static Map<String, WordAnswer> decodeWords(String encoded) {
    Map<String, WordAnswer> answers = new LinkedHashMap<>();
    if (encoded == null || encoded.isBlank()) return answers;
    for (String part : encoded.split(";")) {
      String[] fields = part.split(":", -1);
      if (fields.length != 3 || fields[0].isBlank()) continue;
      try {
        List<Integer> cuts = new ArrayList<>();
        if (!fields[1].isBlank()) {
          for (String cut : fields[1].split(",")) cuts.add(Integer.parseInt(cut.trim()));
        }
        List<Bin> bins = new ArrayList<>();
        for (String code : fields[2].split(",", -1)) {
          bins.add("-".equals(code) || code.isBlank() ? null : Bin.fromCode(code).orElse(null));
        }
        answers.put(fields[0], new WordAnswer(cuts, bins));
      } catch (NumberFormatException ignored) {
        // A malformed answer counts as untouched.
      }
    }
    return answers;
  }

  /**
   * Encodes answers of the second step, e.g. {@code be=WORTBILDUNG;ten=FLEXION}.
   *
   * @param placement shaft of every affix ID
   * @return encoded placement
   */
  public static String encodeShafts(Map<String, Shaft> placement) {
    List<String> parts = new ArrayList<>();
    new TreeMap<>(placement).forEach((id, shaft) -> parts.add(id + "=" + shaft.name()));
    return String.join(";", parts);
  }

  /**
   * Decodes answers produced by {@link #encodeShafts(Map)}; malformed parts are skipped.
   *
   * @param encoded encoded placement
   * @return shaft of every affix ID
   */
  public static Map<String, Shaft> decodeShafts(String encoded) {
    Map<String, Shaft> placement = new LinkedHashMap<>();
    if (encoded == null || encoded.isBlank()) return placement;
    for (String part : encoded.split(";")) {
      int separator = part.indexOf('=');
      if (separator <= 0) continue;
      String id = part.substring(0, separator);
      Shaft.parse(part.substring(separator + 1)).ifPresent(shaft -> placement.put(id, shaft));
    }
    return placement;
  }
}
