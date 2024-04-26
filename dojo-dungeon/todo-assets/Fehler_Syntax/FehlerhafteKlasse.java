// import *;

/** This class contains minor syntax errors and should be minimally adjusted to work. */
public class FehlerhafteKlasse {
  private final int num1 = 5;
  private final int num2 = 2;

  /**
   * This test should be fixed to print: "Die Summe ist: 7".
   *
   * @param out the output stream to print to.
   */
  public static void redirectOutputTo1(PrintWriter out) {
    FehlerhafteKlasse fk = null;
    out.println("Die Summe ist: " + fk.num1 + fk.num2);
  }

  /**
   * This test should be fixed to print: "Die dritte Zahl ist: 8".
   *
   * @param out the output stream to print to.
   */
  public static void redirectOutputTo2(PrintWriter out) {
    String perhapsJunk = "10";
    try {
      int num3 = Integer.parseInt(perhapsJunk);
      out.println("Die dritte Zahl ist: " + ----num3);
    } catch (NumberFormatException e) {
      out.println("Die dritte Zahl ist: ?");
    }
  }
}
