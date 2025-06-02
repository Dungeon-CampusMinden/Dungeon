package produsAdvanced.abstraction;

/**
 * Abstrakte Klasse zum Sortieren von Monstern.
 *
 * <p>Diese Klasse definiert eine Methode zum Sortieren von Monstern, die typischerweise durch ihre
 * Gesundheitswerte repräsentiert werden. Unterklassen müssen die Methode {@code sortMonsters}
 * überschreiben, um das gewünschte Sortierverhalten zu implementieren.
 */
public abstract class MonsterSort {

  /**
   * Sortiert die Monster basierend auf ihren Gesundheitswerten.
   *
   * <p>Die Methode gibt ein Array mit den Monstern sortiert nach ihren Gesundheitswerten zurück.
   * Falls die Methode von der Unterklasse nicht implementiert wurde, wird eine {@link
   * UnsupportedOperationException} geworfen.
   *
   * @param monsterArray Array mit den Monstern, die sortiert werden sollen.
   * @return Ein Array der Monster in sortierter Reihenfolge.
   * @throws UnsupportedOperationException Falls die Methode nicht implementiert wurde.
   */
  public Monster[] sortMonsters(Monster[] monsterArray) {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
