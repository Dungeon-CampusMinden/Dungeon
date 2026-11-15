package rooms.morphco.puzzles;

import java.util.List;
import java.util.Optional;

/**
 * The circumfix assembly station in A-12.
 *
 * <p>The pile of odds and ends holds broken circumfix parts whose boundary marks have chipped off.
 * Players put a front part into the left bowl and a back part into the right bowl. Only the three
 * circumfixes of the script can be assembled.
 */
public final class CircumfixPuzzle {

  /** A broken circumfix part without its boundary mark. */
  public enum Fragment {
    /** {ge}. */
    GE("ge"),
    /** {be}. */
    BE("be"),
    /** {ig}. */
    IG("ig"),
    /** {e}. */
    E("e"),
    /** {en}. */
    EN("en");

    private final String text;

    Fragment(String text) {
      this.text = text;
    }

    /**
     * Returns the fragment as printed on its slip.
     *
     * @return fragment text without boundary mark
     */
    public String text() {
      return text;
    }

    /**
     * Returns the fragment in morpheme braces.
     *
     * @return label such as {@code {ge}}
     */
    public String label() {
      return "{" + text + "}";
    }

    /**
     * Parses a fragment name sent by a client.
     *
     * @param name fragment name
     * @return the fragment, or empty for unknown names
     */
    public static Optional<Fragment> parse(String name) {
      for (Fragment fragment : values()) {
        if (fragment.name().equals(name)) return Optional.of(fragment);
      }
      return Optional.empty();
    }
  }

  /** The circumfixes that can be assembled. */
  public enum Circumfix {
    /** {ge- ... -e}, as in Gebirge. */
    GE_E(Fragment.GE, Fragment.E, "Gebirge"),
    /** {be- ... -ig}, as in begierig. */
    BE_IG(Fragment.BE, Fragment.IG, "begierig"),
    /** {ge- ... -en}, as in gelesen. */
    GE_EN(Fragment.GE, Fragment.EN, "gelesen");

    private final Fragment front;
    private final Fragment back;
    private final String example;

    Circumfix(Fragment front, Fragment back, String example) {
      this.front = front;
      this.back = back;
      this.example = example;
    }

    /**
     * Returns the part that belongs into the left bowl.
     *
     * @return front part
     */
    public Fragment front() {
      return front;
    }

    /**
     * Returns the part that belongs into the right bowl.
     *
     * @return back part
     */
    public Fragment back() {
      return back;
    }

    /**
     * Returns a word that contains the circumfix.
     *
     * @return example word
     */
    public String example() {
      return example;
    }

    /**
     * Returns the circumfix in morpheme notation.
     *
     * @return label such as {@code {ge- ... -e}}
     */
    public String label() {
      return "{" + front.text() + "- ... -" + back.text() + "}";
    }
  }

  /** Parts lying in the pile, in the order they are handed out. */
  public static final List<Fragment> PILE =
      List.of(Fragment.GE, Fragment.BE, Fragment.IG, Fragment.GE, Fragment.E, Fragment.EN);

  private CircumfixPuzzle() {}

  /**
   * Assembles the parts in the left and right bowl.
   *
   * @param left part in the left bowl
   * @param right part in the right bowl
   * @return the circumfix, or empty if the parts do not fit together
   */
  public static Optional<Circumfix> combine(Fragment left, Fragment right) {
    for (Circumfix circumfix : Circumfix.values()) {
      if (circumfix.front() == left && circumfix.back() == right) return Optional.of(circumfix);
    }
    return Optional.empty();
  }
}
