/**
 * In dieser Klasse sollen unterschiedliche Methoden implementiert werden, die noch keine
 * Implementierung haben. Das Verhalten dieser Methoden wird dann im Spiel überprüft werden.
 */
public class FehlerhafteKlasse {
  private final int addend1 = 5;
  private final int addend2 = 2;

  /**
   * Diese Methode soll 2 zum Zahlenwert, der im Parameter {@code in} als String übergeben wird,
   * addieren und das Ergebnis wieder in einen String umwandeln und zurückgeben.
   *
   * <p>Wenn {@code in} {@code null} oder keine Zahl ist, soll "NaN" zurückgegeben werden.
   *
   * <p>Wenn die Operation einen Integer-Overflow verursachen würde, soll "Integer Overflow"
   * zurückgegeben werden.
   *
   * @param in Eingabe, zu der 2 addiert werden soll
   * @return Die Summe der Addition als String
   * @see Integer
   */
  public static String incrementByTwo(String in) {}

  /**
   * Diese Methode soll korrigiert werden und die Summe der beiden Attribute {@link
   * FehlerhafteKlasse#addend1} und {@link FehlerhafteKlasse#addend2} als String zurückgeben.
   *
   * <p>Passen Sie dazu nur diese Methode an.
   *
   * @return Die Summe beider Summanden als String.
   */
  public static String getSum() {
    FehlerhafteKlasse fk;
    return "" + fk.addend1 + fk.addend2;
  }
}
