package produsAdvanced.riddles;

import produsAdvanced.abstraction.MonsterSort;

/**
 * Eine konkrete Implementierung von {@link MonsterSort}.
 *
 * In dieser Klasse muss die Methode {@link #sortMonsters()} implementiert werden, um die Monster
 * nach ihrer Gesundheit zu sortieren. Diese wird im Array {@code monsterArray} gespeichert.
 *
 */
public class MyMonsterSort extends MonsterSort {

  /**
   * Array, das die Monstergesundheit repräsentiert.
   */
  private final int[] monsterArray;

  /**
   * Erstellt eine neue Instanz von {@code MyMonsterSort}.
   *
   * @param monsterArray ein Array von Integer-Werten, das die Gesundheitswerte der Monster enthält
   */
  public MyMonsterSort(int[] monsterArray) {
    this.monsterArray = monsterArray;
  }

  /**
   * Implementiere hier einen Sortieralgorithmus, um die Monster nach ihren Gesundheitswerten zu sortieren.
   * Sortiere dafür das Array {@code monsterArray} in aufsteigender Reihenfolge.
   *
   * @return ein Array mit den sortierten Gesundheitswerten der Monster
   */
  @Override
  public int[] sortMonsters() {
    throw new UnsupportedOperationException("Diese Methode muss vom Schüler implementiert werden");
  }
}
