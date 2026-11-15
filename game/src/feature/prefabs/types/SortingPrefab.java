package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.sorting.Sorting;
import feature.interaction.sorting.SortingFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.tasks.TaskComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Server-side decoration with a board on which players sort terms into labelled bins, see {@link
 * SortingFactory}. Link {@link #SOLVED} to e.g. {@link DoorControlPrefab#OPEN} to open a door with
 * it.
 *
 * <p>Every term names the bin it belongs in as {@code term=bin}; a term without a bin stays on the
 * tray.
 */
public final class SortingPrefab extends Prefab {

  private static final int MAX_TERMS = 24;
  private static final int MAX_BINS = 6;

  private static final String TYPE = "sorting";
  private static final String DISPLAY_NAME = "Sorting Board";

  /** Raised when a player sorted every term correctly, with the player as payload. */
  public static final PrefabEvent<Entity> SOLVED = PrefabEvent.withPlayer("solved", "Solved");

  /** Raised when a player checked a placement that is not yet right, with the player as payload. */
  public static final PrefabEvent<Entity> CHECKED_WRONG =
      PrefabEvent.withPlayer("checked-wrong", "Checked Wrong");

  /** Bottom-left position of the decoration. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Decoration shown for the board. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.LabWhiteboard.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());

  /** Title of the board; empty for the default title. */
  public static final PrefabProperty<String> TITLE =
      PrefabProperty.string("title", "Title (empty = default)", "", value -> true);

  /** Help text below the title; empty for the default help. */
  public static final PrefabProperty<String> HELP =
      PrefabProperty.string("help", "Help Text (empty = default)", "", value -> true);

  /** Titles of the bins, separated by {@code |}. */
  public static final PrefabProperty<String> BINS =
      PrefabProperty.string(
          "bins", "Bins ('|' separated)", "Animals|Plants", SortingPrefab::hasValidBins);

  /** Terms separated by {@code |}, each as {@code term=bin} or as {@code term} without a bin. */
  public static final PrefabProperty<String> TERMS =
      PrefabProperty.string(
          "terms",
          "Terms ('|' separated, term=bin)",
          "Dog=Animals|Rose=Plants|Cat=Animals|Oak=Plants|Stone",
          SortingPrefab::hasValidTerms);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, DECO, TITLE, HELP, BINS, TERMS);

  /** Creates the sorting-board definition. */
  public SortingPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored sorting board.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public SortingPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(SOLVED, CHECKED_WRONG);
  }

  @Override
  protected void validate(PrefabInstance instance) {
    sorting(instance);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity board = context.createEntity(instance.name());
    DecoFactory.createDeco(board, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    board.remove(DecoComponent.class);
    SortingFactory.createSorting(board, sorting(instance));
    TaskComponent<?> task = board.fetch(TaskComponent.class).orElseThrow();
    task.onCorrect(who -> fire(context, instance, SOLVED, who));
    task.onWrong(who -> fire(context, instance, CHECKED_WRONG, who));
    return List.of(board);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }

  // Throws if a term names a bin that is not in the bin list.
  private static Sorting sorting(PrefabInstance instance) {
    List<String> bins = split(instance.get(BINS));
    List<String> terms = new ArrayList<>();
    List<Integer> targets = new ArrayList<>();
    for (String term : split(instance.get(TERMS))) {
      int separator = term.indexOf('=');
      if (separator < 0) {
        terms.add(term);
        targets.add(Sorting.NONE);
        continue;
      }
      String bin = term.substring(separator + 1).strip();
      if (!bins.contains(bin)) {
        throw new IllegalArgumentException(
            "Prefab property 'terms' names the bin '" + bin + "', which is not in 'bins'");
      }
      terms.add(term.substring(0, separator).strip());
      targets.add(bins.indexOf(bin));
    }
    return new Sorting(instance.get(TITLE), instance.get(HELP), terms, bins, targets);
  }

  private static List<String> split(String value) {
    return Arrays.stream(value.split("\\|", -1)).map(String::strip).toList();
  }

  // Bin titles must be unique, so that every term=bin names one bin.
  private static boolean hasValidBins(String value) {
    List<String> bins = split(value);
    return bins.size() <= MAX_BINS
        && bins.stream().noneMatch(String::isEmpty)
        && new HashSet<>(bins).size() == bins.size();
  }

  private static boolean hasValidTerms(String value) {
    List<String> terms = split(value);
    return terms.size() <= MAX_TERMS
        && terms.stream()
            .map(term -> term.split("=", -1))
            .allMatch(
                parts -> parts.length <= 2 && Arrays.stream(parts).noneMatch(String::isBlank));
  }
}
