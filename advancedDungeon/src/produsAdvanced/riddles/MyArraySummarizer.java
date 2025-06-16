package produsAdvanced.riddles;

import produsAdvanced.abstraction.ArraySummarizer;

/**
 * Implementierung für das Iterieren eines gegebenen Arrays mit Monsteranzahlen. Die Klasse erhält
 * ein schon gefülltes Array und gibt das summierte Ergebnis der enthaltenen Anzahlen zurück.
 */
public class MyArraySummarizer extends ArraySummarizer {
  private final int[] monsterArray;

  /**
   * Konstruktor, der ein Array mit Monsteranzahlen entgegennimmt.
   *
   * @param monsterArray Ein Integer-Array, das die Anzahl der Monster in verschiedenen Räumen
   *     enthält
   */
  public MyArraySummarizer(int[] monsterArray) {
    this.monsterArray = monsterArray;
  }

  /**
   * Implementiere die Methode summarizeArray(), die die Gesamtanzahl aller Monster berechnet.
   *
   * <p>Die Aufgabe: - Du erhältst ein Array 'monsterArray' mit der Anzahl von Monstern in
   * verschiedenen Räumen - Addiere die Anzahl aller Monster aus allen Räumen - Räume ohne Monster
   * (Wert = 0) werden nicht mitgezählt
   *
   * <p>Beispiel für das Array: monsterArray = [5, 0, 3, 0, 2] // 5 Monster in Raum 0, keine in Raum
   * 1, 3 in Raum 2, usw.
   *
   * <p>Hinweise: - Nutze eine Schleife um durch das Array zu laufen - Addiere nur Werte die größer
   * als 0 sind - Speichere die Summe in einer Variable - Gib am Ende die Gesamtsumme zurück
   *
   * @return Die Summe aller Monster im Array (nur Räume mit Monstern werden gezählt)
   * @throws UnsupportedOperationException wenn die Methode nicht implementiert wurde
   */
  public int summarizeArray() {
    throw new UnsupportedOperationException("Diese Methode muss noch implementiert werden.");
  }
}
