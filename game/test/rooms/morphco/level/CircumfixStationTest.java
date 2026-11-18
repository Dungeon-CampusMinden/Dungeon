package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import feature.components.InventoryComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.puzzles.CircumfixPuzzle.Circumfix;
import rooms.morphco.puzzles.CircumfixPuzzle.Fragment;

class CircumfixStationTest {

  @Test
  void pileKeepsWhatNoPlayerCarriesAndGetsBackThePartsOfAPlayerWhoLeft() {
    MorphCoState state = new MorphCoState();
    Circumfix assembled = Circumfix.values()[0];
    state.assemble(assembled);
    Entity carrier = new Entity();
    InventoryComponent inventory = new InventoryComponent();
    carrier.add(inventory);
    Fragment carried = Circumfix.values()[1].back();
    inventory.add(CircumfixFragmentItem.of(carried));

    List<Fragment> expected = new ArrayList<>(CircumfixPuzzle.PILE);
    expected.remove(assembled.front());
    expected.remove(assembled.back());
    List<Fragment> withoutCarried = new ArrayList<>(expected);
    withoutCarried.remove(carried);
    assertEquals(withoutCarried, CircumfixStation.inPile(state, Stream.of(carrier)));
    assertEquals(expected, CircumfixStation.inPile(state, Stream.empty()));
  }
}
