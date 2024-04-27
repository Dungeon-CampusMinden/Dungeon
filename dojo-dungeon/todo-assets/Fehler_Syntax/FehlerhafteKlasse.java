/**
 * In dieser Klasse sollen unterschiedliche Methoden implementiert werden, die noch keine
 * Implementierung haben. Das Verhalten dieser Methoden wird dann im Spiel überprüft werden.
 */
public class FehlerhafteKlasse {

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
   * Diese Methode soll die Fibonacci-Zahl für {@code n} berechnen und zurückgeben.
   *
   * <p>Wenn {@code in} {@code null} oder keine Zahl ist, soll "NaN" zurückgegeben werden.
   *
   * <p>Wenn {@code in} kleiner 0 oder größer als 10 ist, soll "Too large" zurückgegeben werden.
   *
   * @param in Eingabe, für die die Fibonacci-Zahl berechnet werden soll
   * @return Die Fibonacci-Zahl als String
   */
  public static String calculateFibonacci(String in) {}
}
