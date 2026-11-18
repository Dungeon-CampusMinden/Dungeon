package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import feature.components.InventoryComponent;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.puzzles.CircumfixPuzzle.Circumfix;
import rooms.morphco.puzzles.CircumfixPuzzle.Fragment;

class CircumfixStationTest {

  @Test
  void pileKeepsWhatNoPlayerCarriesAndGetsBackThePartsOfAPlayerWhoLeftOrRejoined() {
    MorphCoState state = new MorphCoState();
    state.assemble(Circumfix.GE_E);
    Entity carrier = new Entity();
    InventoryComponent inventory = new InventoryComponent();
    carrier.add(inventory);
    inventory.add(CircumfixFragmentItem.of(Circumfix.BE_IG.back()));
    inventory.add(CircumfixFragmentItem.of(Circumfix.GE_EN.back()));

    assertEquals(
        List.of(Fragment.BE, Fragment.GE), CircumfixStation.inPile(state, Stream.of(carrier)));
    assertEquals(
        List.of(Fragment.BE, Fragment.IG, Fragment.GE, Fragment.EN),
        CircumfixStation.inPile(state, Stream.empty()));

    CircumfixStation.returnToPile(carrier);
    assertEquals(
        List.of(Fragment.BE, Fragment.IG, Fragment.GE, Fragment.EN),
        CircumfixStation.inPile(state, Stream.of(carrier)));
  }
}
