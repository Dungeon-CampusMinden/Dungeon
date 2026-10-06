package rooms.soulweaver.modules.decisions;

import java.util.List;
import java.util.stream.Collectors;

/** The six programs and the persistent values carried through the labyrinth. */
public final class DecisionMaze {
  /** The two executable leaf choices in each rune. */
  public enum Side {
    LEFT,
    RIGHT
  }

  /**
   * Values carried through both successful and failed routes.
   *
   * @param kraft current strength
   * @param energie current energy
   * @param temperatur current temperature
   */
  public record Values(int kraft, int energie, int temperatur) {
    /**
     * @param k strength change
     * @param e energy change
     * @param t temperature change
     * @return updated values without resetting earlier events
     */
    public Values plus(int k, int e, int t) {
      return new Values(kraft + k, energie + e, temperatur + t);
    }

    /**
     * @return all three values with unambiguous labels
     */
    public String label() {
      return "KRAFT " + kraft + "     ENERGIE " + energie + "     TEMPERATUR " + temperatur;
    }
  }

  private enum Attribute {
    KRAFT("Kraft"),
    ENERGIE("Energie"),
    TEMPERATUR("Temperatur");

    private final String label;

    Attribute(String label) {
      this.label = label;
    }

    int value(Values values) {
      return switch (this) {
        case KRAFT -> values.kraft();
        case ENERGIE -> values.energie();
        case TEMPERATUR -> values.temperatur();
      };
    }
  }

  private enum Operator {
    GT(">"),
    GE(">="),
    LT("<");

    private final String symbol;

    Operator(String symbol) {
      this.symbol = symbol;
    }

    boolean test(int value, int threshold) {
      return switch (this) {
        case GT -> value > threshold;
        case GE -> value >= threshold;
        case LT -> value < threshold;
      };
    }
  }

  private sealed interface Condition permits Comparison, And, Or {
    boolean test(Values values);

    String text();
  }

  private record Comparison(Attribute attribute, Operator operator, int threshold)
      implements Condition {
    public boolean test(Values values) {
      return operator.test(attribute.value(values), threshold);
    }

    public String text() {
      return attribute.label + " " + operator.symbol + " " + threshold;
    }
  }

  private record And(Condition left, Condition right) implements Condition {
    public boolean test(Values values) {
      return left.test(values) && right.test(values);
    }

    public String text() {
      return left.text() + " UND " + right.text();
    }
  }

  private record Or(Condition left, Condition right) implements Condition {
    public boolean test(Values values) {
      return left.test(values) || right.test(values);
    }

    public String text() {
      return left.text() + " ODER " + right.text();
    }
  }

  private sealed interface Program permits Branch, Leaf {
    Side evaluate(Values values);

    String text(String indent);
  }

  private record Leaf(Side side) implements Program {
    public Side evaluate(Values values) {
      return side;
    }

    public String text(String indent) {
      return indent + (side == Side.LEFT ? "LINKS" : "RECHTS") + "\n";
    }
  }

  private record Branch(Condition condition, Program yes, Program no) implements Program {
    public Side evaluate(Values values) {
      return (condition.test(values) ? yes : no).evaluate(values);
    }

    public String text(String indent) {
      return indent
          + "WENN "
          + condition.text()
          + ":\n"
          + yes.text(indent + "    ")
          + indent
          + "SONST:\n"
          + no.text(indent + "    ");
    }
  }

  private record Event(String name, Values change) {
    String text() {
      return name
          + ": "
          + java.util.Arrays.stream(Attribute.values())
              .filter(attribute -> attribute.value(change) != 0)
              .map(
                  attribute ->
                      attribute.name()
                          + " "
                          + (attribute.value(change) > 0 ? "+" : "")
                          + attribute.value(change))
              .collect(Collectors.joining(" · "));
    }
  }

  private record Rune(String title, Program program, List<Event> events) {}

  private static final Program LEFT = new Leaf(Side.LEFT);
  private static final Program RIGHT = new Leaf(Side.RIGHT);
  private static final List<Rune> PROGRAMS =
      List.of(
          new Rune(
              "Das Tor der Energie",
              branch(gt(Attribute.ENERGIE, 50), branch(ge(Attribute.KRAFT, 40), RIGHT, LEFT), LEFT),
              List.of(new Event("Schwellenrune", new Values(0, -30, 0)))),
          new Rune(
              "Das Tor der Temperatur",
              branch(
                  ge(Attribute.ENERGIE, 60),
                  branch(lt(Attribute.TEMPERATUR, 25), LEFT, RIGHT),
                  branch(ge(Attribute.KRAFT, 40), RIGHT, LEFT)),
              List.of(new Event("Kristallquelle", new Values(0, 30, 0)))),
          new Rune(
              "Das Tor der drei Runen",
              branch(
                  ge(Attribute.ENERGIE, 50),
                  branch(
                      gt(Attribute.TEMPERATUR, 20),
                      branch(ge(Attribute.KRAFT, 50), RIGHT, LEFT),
                      RIGHT),
                  LEFT),
              List.of(
                  new Event("Kraftquelle", new Values(10, 0, 0)),
                  new Event("Feuerrune", new Values(0, 0, 5)))),
          new Rune(
              "Das Tor der zwei Pfade",
              branch(
                  ge(Attribute.ENERGIE, 50),
                  branch(
                      gt(Attribute.TEMPERATUR, 25),
                      branch(ge(Attribute.KRAFT, 60), RIGHT, LEFT),
                      branch(ge(Attribute.KRAFT, 40), RIGHT, LEFT)),
                  LEFT),
              List.of(new Event("Schmiederune", new Values(13, -15, 0)))),
          new Rune(
              "Das Tor der verbundenen Kräfte",
              branch(
                  ge(Attribute.ENERGIE, 50),
                  branch(
                      gt(Attribute.TEMPERATUR, 25),
                      branch(
                          new And(gt(Attribute.KRAFT, 60), lt(Attribute.ENERGIE, 60)), RIGHT, LEFT),
                      RIGHT),
                  LEFT),
              List.of(new Event("Herzglut", new Values(0, -13, 4)))),
          new Rune(
              "Das Herzfeuer-Siegel",
              branch(
                  ge(Attribute.ENERGIE, 40),
                  branch(
                      gt(Attribute.KRAFT, 60),
                      branch(
                          new And(gt(Attribute.TEMPERATUR, 30), lt(Attribute.ENERGIE, 50)),
                          branch(
                              new Or(ge(Attribute.KRAFT, 70), gt(Attribute.TEMPERATUR, 35)),
                              RIGHT,
                              LEFT),
                          LEFT),
                      LEFT),
                  RIGHT),
              List.of()));

  // Each attempt starts from its own values, so a retry needs recalculation.
  private static final Values[] START_VALUES = {
    new Values(45, 70, 22), // RRLLRL
    new Values(50, 90, 21), // RLRRLL
    new Values(35, 70, 15), // LLRRRL
    new Values(40, 75, 27) // RRLLLR
  };

  public static final String[] TITLES = PROGRAMS.stream().map(Rune::title).toArray(String[]::new);
  public static final String[] RUNES =
      PROGRAMS.stream().map(rune -> rune.program().text("")).toArray(String[]::new);

  private DecisionMaze() {}

  private static Program branch(Condition condition, Program yes, Program no) {
    return new Branch(condition, yes, no);
  }

  private static Condition gt(Attribute attribute, int threshold) {
    return new Comparison(attribute, Operator.GT, threshold);
  }

  private static Condition ge(Attribute attribute, int threshold) {
    return new Comparison(attribute, Operator.GE, threshold);
  }

  private static Condition lt(Attribute attribute, int threshold) {
    return new Comparison(attribute, Operator.LT, threshold);
  }

  /**
   * @param failures completed return trips
   * @return start values of the next attempt
   */
  public static Values start(int failures) {
    return START_VALUES[failures % START_VALUES.length];
  }

  /**
   * @param junction zero-based rune index
   * @param values current carried values
   * @return leaf reached by executing the displayed nested program
   */
  public static Side evaluate(int junction, Values values) {
    return PROGRAMS.get(junction).program().evaluate(values);
  }

  /**
   * @param junction successful junction being left
   * @return fixed changes applied by its onward rune
   */
  public static Values delta(int junction) {
    return PROGRAMS.get(junction).events().stream()
        .map(Event::change)
        .reduce(
            new Values(0, 0, 0),
            (sum, change) -> sum.plus(change.kraft(), change.energie(), change.temperatur()));
  }

  /**
   * @param junction successful junction being left
   * @return visible explanation of its value changes
   */
  public static String event(int junction) {
    List<Event> events = PROGRAMS.get(junction).events();
    return events.isEmpty()
        ? "Das Herzfeuer erwacht."
        : events.stream().map(Event::text).collect(Collectors.joining(" · "));
  }
}
