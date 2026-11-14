package feature.interaction.sorting;

import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.tasks.AssignmentTask;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A sorting task: terms that players sort into labelled bins, and the bin every term belongs in.
 *
 * <p>Terms and bins are numbered in order. A placement lists the bin of every term, or {@link
 * #NONE} for a term outside of all bins. It is right when every term lies in its bin and every term
 * without a bin stays outside.
 *
 * @param title title of the board, empty for the default title
 * @param help help text below the title, empty for the default help
 * @param terms the terms, in the order they lie on the tray
 * @param bins the titles of the bins
 * @param targets the bin of every term, or {@link #NONE} for a term that belongs in no bin
 */
public record Sorting(
    String title, String help, List<String> terms, List<String> bins, List<Integer> targets) {

  /** Bin of a term outside of all bins. */
  public static final int NONE = -1;

  /** Dialog attribute (Boolean): whether the task is solved; the board then shows the solution. */
  public static final String SOLVED_KEY = "sortingSolved";

  // Dialog attributes of the board.
  private static final String TERMS = "sortingTerms";
  private static final String BINS = "sortingBins";
  private static final String TARGETS = "sortingTargets";

  /**
   * Creates a sorting task.
   *
   * @throws IllegalArgumentException if a term has no target or a target is not a bin
   */
  public Sorting {
    terms = List.copyOf(terms);
    bins = List.copyOf(bins);
    targets = List.copyOf(targets);
    int binCount = bins.size();
    if (targets.size() != terms.size()
        || targets.stream().anyMatch(bin -> bin < NONE || bin >= binCount)) {
      throw new IllegalArgumentException("Every term needs a bin or NONE: " + targets);
    }
  }

  /**
   * Reads the task from the attributes of a board dialog, see {@link #put}.
   *
   * @param context dialog context of the board
   * @return the task
   */
  public static Sorting from(DialogContext context) {
    return new Sorting(
        context.find(DialogContextKeys.TITLE, String.class).orElse(""),
        context.find(DialogContextKeys.MESSAGE, String.class).orElse(""),
        List.of(context.require(TERMS, String[].class)),
        List.of(context.require(BINS, String[].class)),
        Arrays.stream(context.require(TARGETS, int[].class)).boxed().toList());
  }

  /**
   * Writes the task into the attributes of a board dialog.
   *
   * @param dialog builder of the dialog context
   */
  void put(DialogContext.Builder dialog) {
    dialog
        .put(DialogContextKeys.TITLE, title)
        .put(DialogContextKeys.MESSAGE, help)
        .put(TERMS, terms.toArray(String[]::new))
        .put(BINS, bins.toArray(String[]::new))
        .put(TARGETS, targets.stream().mapToInt(Integer::intValue).toArray());
  }

  /**
   * Counts the terms that do not lie where they belong.
   *
   * @param placement bin of every term or {@link #NONE}
   * @return number of wrong terms, {@code 0} if the placement is right
   */
  public int wrong(int[] placement) {
    return (int)
        IntStream.range(0, terms.size())
            .filter(term -> bin(placement, term) != targets.get(term))
            .count();
  }

  /**
   * Returns the task that checks placements on the server; terms and bins are identified by their
   * number.
   *
   * @return the assignment task
   */
  AssignmentTask task() {
    Map<String, String> solution = new LinkedHashMap<>();
    for (int term = 0; term < terms.size(); term++) {
      if (targets.get(term) != NONE) solution.put(id(term), id(targets.get(term)));
    }
    return new AssignmentTask(title, ids(bins.size()), ids(terms.size()), solution);
  }

  /**
   * Turns a placement into an answer of {@link #task()}.
   *
   * @param placement bin of every term or {@link #NONE}
   * @return bin of every term that lies in a bin
   */
  Map<String, String> answer(int[] placement) {
    Map<String, String> answer = new LinkedHashMap<>();
    for (int term = 0; term < terms.size(); term++) {
      int bin = bin(placement, term);
      if (bin != NONE) answer.put(id(term), id(bin));
    }
    return answer;
  }

  // Missing entries and bins that do not exist count as outside of all bins.
  private int bin(int[] placement, int term) {
    int bin = term < placement.length ? placement[term] : NONE;
    return bin >= 0 && bin < bins.size() ? bin : NONE;
  }

  private static String id(int number) {
    return Integer.toString(number);
  }

  private static Set<String> ids(int count) {
    return IntStream.range(0, count).mapToObj(Sorting::id).collect(Collectors.toSet());
  }
}
