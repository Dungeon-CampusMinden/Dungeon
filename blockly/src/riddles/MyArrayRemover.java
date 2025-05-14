package riddles;

import abstraction.ArrayRemover;

public class MyArrayRemover extends ArrayRemover {

  private final int[] monsterArray;

  public MyArrayRemover(int[] monsterArray) {
      this.monsterArray = monsterArray;
  }

  /**
   * Implementiere die Methode entfernePositionen(), die ein neues Array ohne leere Monsterräume erstellt.
   *
   * Die Aufgabe:
   * - Du erhältst das Array 'monsterArray' mit Monstern in verschiedenen Räumen
   * - Ändere die Monsteranzahl in den Räumen, in denen jetzt neue Monster stehen
   * - Räume ohne Monster sollen nicht im neuen Array vorkommen
   *
   * Beispiel für das Array:
   * monsterArray = [5, 0, 3, 0, 2]  // 5 Monster in Raum 0, keine in Raum 1, 3 in Raum 2, usw.
   *
   * Hinweise:
   * - Arrays haben in Java eine feste Größe
   * - Wenn du einen Eintrag in einem Array löschen möchtest, muss sich das Array also verkleinern
   * - Das geht nicht direkt, aber du kannst ein neues Array erstellen und die Werte übertragen
   *
   * @return Ein neues Array, das nur die Anzahl der Monster aus den nicht-leeren Räumen enthält
   * @throws UnsupportedOperationException wenn die Methode nicht implementiert wurde
   */

  @Override
  public int[] entfernePositionen() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
