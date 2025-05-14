package riddles;

import abstraction.ArraySummarizer;

public class MyArraySummarizer extends ArraySummarizer {
  private final int[] monsterArray;

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
  @Override
  public int summarizeArray() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
