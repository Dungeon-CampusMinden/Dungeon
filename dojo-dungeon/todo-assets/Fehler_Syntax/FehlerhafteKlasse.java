/**
 * In dieser Klasse sollen unterschiedliche Methoden implementiert werden, die noch keine
 * Implementierung haben. Das Verhalten dieser Methoden wird dann im Spiel überprüft werden.
 */
public class FehlerhafteKlasse {
  private final int addend1 = 5;
  private final int addend2 = 2;

  /**
   * Diese Methode soll 2 zu {@code in} addieren und das Ergebnis in einen String umwandeln und
   * zurückgeben.
   *
   * <p>Wenn {@code in} {@code null} oder keine Zahl ist, soll "NaN" zurückgegeben werden.
   *
   * <p>Wenn das Ergebnis größer als {@link Integer#MAX_VALUE} ist, soll "Integer overflow"
   * zurückgegeben werden.
   *
   * @param in Eingabe, zu der 2 addiert werden soll
   * @return Die Summe der Addition als String
   * @see Integer
   */
  public static String incrementByTwo(String in) {}

  /**
   * Diese Methode korregiert werden und sie soll die Summe der beiden Attribute {@link
   * FehlerhafteKlasse#addend1} und {@link FehlerhafteKlasse#addend2} als String zurückgeben.
   *
   * <p>Passen Sie dazu nur diese Methode an.
   *
   * @return Die Summe beider Addenden als String.
   */
  public static String getSum() {
    FehlerhafteKlasse fk = null;
    return "" + fk.addend1 + fk.addend2;
  }
}
