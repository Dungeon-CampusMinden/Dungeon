package produsAdvanced.abstraction;

/**
 * Eine abstrakte Klasse zur Bearbeitung von Arrays durch Entfernen von Elementen. Diese Klasse
 * ermöglicht das Filtern und Neuerstellen von Arrays ohne leere Positionen.
 */
public abstract class ArrayRemover {

  /**
   * Entfernt leere Positionen aus einem Array und erstellt ein neues, kompaktes Array.
   *
   * @return Ein neues Integer-Array ohne leere Positionen
   * @throws UnsupportedOperationException wenn die Methode nicht implementiert wurde
   */
  public int[] removePosition() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
