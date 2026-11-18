package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import rooms.morphco.puzzles.CircumfixPuzzle.Circumfix;
import rooms.morphco.puzzles.CircumfixPuzzle.Fragment;

class CircumfixPuzzleTest {

  @Test
  void onlyTheThreeCircumfixesOfTheScriptFit() {
    assertEquals(Optional.of(Circumfix.GE_E), CircumfixPuzzle.combine(Fragment.GE, Fragment.E));
    assertEquals(Optional.of(Circumfix.BE_IG), CircumfixPuzzle.combine(Fragment.BE, Fragment.IG));
    assertEquals(Optional.of(Circumfix.GE_EN), CircumfixPuzzle.combine(Fragment.GE, Fragment.EN));
    assertTrue(CircumfixPuzzle.combine(Fragment.E, Fragment.GE).isEmpty());
    assertTrue(CircumfixPuzzle.combine(Fragment.BE, Fragment.EN).isEmpty());
  }

  @Test
  void pileContainsExactlyThePartsOfAllCircumfixes() {
    List<Fragment> pile = new ArrayList<>(CircumfixPuzzle.PILE);
    for (Circumfix circumfix : Circumfix.values()) {
      assertTrue(pile.remove(circumfix.front()));
      assertTrue(pile.remove(circumfix.back()));
    }
    assertTrue(pile.isEmpty());
    assertEquals("{ge- ... -en}", Circumfix.GE_EN.label());
  }
}
