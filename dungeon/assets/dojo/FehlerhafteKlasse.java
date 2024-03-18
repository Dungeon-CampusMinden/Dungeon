public class FehlerhafteKlasse {
  int num1 = 5;
  int num2 = 2;

  public static void main(String[] args, PrintWriter out) {
    int sum = 0;
    sum = FehlerhafteKlasse.num1 + FehlerhafteKlasse.num2;
    out.println("Die Summe ist: " + sum);

    String zahlAlsString = "7";
    int num3 = Integer.parseInt(zahlAlsString);
    num3++;
    out.println("Die dritte Zahl ist: " + num3);
  }
}
