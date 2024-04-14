// what is wrong here?

/** This class should be fixed, so that testExcpectedOutput7 and testExcpectedOutput8 work. */
public class FehlerhafteKlasse { // what is wrong here?
  int num1 = 5; // what is wrong here?
  int num2 = 2; // what is wrong here?

  /**
   * This test should be fixed to print: "Die Summe ist: 7".
   *
   * @param out the output stream to print to.
   */
  public static void redirectOutputTo1(PrintWriter out) {
    int sum = 0;
    sum = FehlerhafteKlasse.num1 + FehlerhafteKlasse.num2; // what is wrong here?
    out.println("Die Summe ist: " + sum);
  }

  /**
   * This test should be fixed to print: "Die dritte Zahl ist: 8".
   *
   * <p>Tip: Don't forget to use trycatch.
   *
   * @param out the output stream to print to.
   */
  public static void redirectOutputTo2(PrintWriter out) {
    String perhapsJunk = "10";
    int num3 = Integer.parseInt(perhapsJunk); // what is wrong here?
    num3--;
    num3--;
    out.println("Die dritte Zahl ist: " + num3);
  }
}
