package produsAdvanced.abstraction;

/**
 * Abstrakte Basisklasse für die Erstellung und Verarbeitung von Arrays mit Monster-Zählungen. Diese
 * Klasse dient als Template für die Implementierung von Array-Operationen in einem Spiel-Kontext.
 */
public abstract class ArrayCreator {

  /**
   * Zählt die Monster in verschiedenen Räumen und erstellt ein entsprechendes Array. Diese Methode
   * muss von der konkreten Implementierung überschrieben werden.
   *
   * @return Ein Integer-Array, das die Anzahl der Monster pro Raum enthält
   * @throws UnsupportedOperationException wenn die Methode nicht implementiert wurde
   */
  public int[] countMonstersInRooms() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
