package hint;

import core.Component;

/**
 * Component that manages a sequence of hints. Each hint can be accessed by index, and the component
 * tracks the next hint index.
 *
 * <p>Use {@link #hint()} to get the next hint in the sequence. Note that the internal index is
 * <b>not automatically updated</b>; you must call {@link #increaseIndex()} manually to advance to
 * the next hint.
 *
 * <p>The component also provides a flag indicating whether the last hint has been shown.
 */
public class HintComponent implements Component {

  private final String[] hints;
  private int next = 0;

  /**
   * Creates a HintComponent with the given hints.
   *
   * @param hints the array of hints; must not be null or empty
   * @throws IllegalArgumentException if hints is null or empty
   */
  public HintComponent(String... hints) {
    if (hints == null || hints.length == 0) {
      throw new IllegalArgumentException("Hints must not be null or empty");
    }
    this.hints = hints;
  }

  /**
   * Returns the hint at the specified index.
   *
   * @param index the index of the hint to retrieve
   * @return the hint string at the specified index
   * @throws IllegalArgumentException if index is out of bounds
   */
  public String hint(int index) {
    if (index < 0 || index >= hints.length) {
      throw new IllegalArgumentException("Given index is out of bounds.");
    }
    return hints[index];
  }

  /**
   * Returns the next hint based on the internal index. The internal index is <b>not automatically
   * incremented</b>; call {@link #increaseIndex()} to move forward.
   *
   * <p>If the last hint has already been shown, an empty string is returned.
   *
   * @return the next hint string, or an empty string if all hints have been shown
   */
  public String hint() {
    if (isLastHintShown()) return "";
    return hint(next);
  }

  /** Increases the internal index by one. The index will not exceed the last valid hint index. */
  public void increaseIndex() {
    next++;
  }

  /**
   * Indicates whether the last hint has been shown.
   *
   * @return true if the last hint has been shown, false otherwise
   */
  public boolean isLastHintShown() {
    return next > hints.length - 1;
  }

  /** Resets the index to 0. */
  public void resetIndex() {
    next = 0;
  }
}
