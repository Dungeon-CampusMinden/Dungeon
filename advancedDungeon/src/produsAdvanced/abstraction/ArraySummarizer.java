package produsAdvanced.abstraction;

/**
 * Eine abstrakte Klasse zur Berechnung von Array-Summen. Diese Klasse bietet eine Grundlage für die
 * Implementierung von Array-Summierungsoperationen.
 */
public abstract class ArraySummarizer {

  /**
   * Berechnet die Summe der Elemente in einem Array.
   *
   * @return Die Summe aller Elemente im Array
   * @throws UnsupportedOperationException wenn die Methode nicht implementiert wurde
   */
  public int summarizeArray() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
