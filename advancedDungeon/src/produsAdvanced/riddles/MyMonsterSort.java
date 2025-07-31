package produsAdvanced.riddles;

import produsAdvanced.abstraction.Monster;
import produsAdvanced.abstraction.MonsterSort;

/**
 * Eine konkrete Implementierung von {@link MonsterSort}.
 *
 * <p>In dieser Klasse muss die Methode {@link #sortMonsters(Monster[])} implementiert werden, um
 * die Monster nach ihrer Gesundheit zu sortieren. Diese wird im Array {@code monsterArray}
 * gespeichert.
 */
public class MyMonsterSort extends MonsterSort {
  /**
   * Implementiere hier einen Sortieralgorithmus, um die Monster nach ihren Gesundheitswerten zu
   * sortieren. Sortiere dafür das Array {@code monsterArray} in aufsteigender Reihenfolge.
   *
   * <p>HINWEIS: Verwende getHealthPoints(), um die Lebenspunkte der Monster zu vergleichen. Die
   * meisten Sortieralgorithmen benötigen verschachtelte Schleifen (eine äußere und eine innere), um
   * Objekte zu vergleichen.
   *
   * <p>WICHTIG: Immer wenn du zwei Monster im Array tauschst, musst du auch ihre visuellen
   * Positionen im Spiel tauschen! Nutze dafür die swapPosition() Methode.
   *
   * @param monsterArray Array mit den Monstern, die sortiert werden sollen.
   * @return Ein Array der Monster in sortierter Reihenfolge.
   */
  public Monster[] sortMonsters(Monster[] monsterArray) {
    throw new UnsupportedOperationException("Diese Methode muss noch implementiert werden.");
  }
}
