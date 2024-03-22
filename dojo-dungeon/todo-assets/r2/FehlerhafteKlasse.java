/** This class should be fixed, so that testExcpectedOutput7 and testExcpectedOutput8 work. */
public class FehlerhafteKlasse2 {
  int num1 = 5;
  int num2 = 2;

  /**
   * This test should be fixed to print: "Die Summe ist: 7".
   *
   * @param out the output stream to print to.
   */
  public static void testExpectedOutput7(PrintWriter out) {
    int sum = 0;
    sum = FehlerhafteKlasse2.num1 + FehlerhafteKlasse2.num2;
    out.println("Die Summe ist: " + sum);
  }

  /**
   * This test should be fixed to print: "Die dritte Zahl ist: 8".
   *
   * @param out the output stream to print to.
   */
  public static void testExpectedOutput8(PrintWriter out) {
    String perhapsJunk = "10";
    int num3 = Integer.parseInt(perhapsJunk);
    num3--;
    num3--;
    out.println("Die dritte Zahl ist: " + num3);
  }
}
