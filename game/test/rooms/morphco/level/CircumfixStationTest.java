package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import feature.components.InventoryComponent;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.items.CircumfixMagazineItem;
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

  @Test
  void stationAssemblesPartsThatTwoPlayersCarry() {
    Entity first = carrying(Fragment.GE);
    Entity second = carrying(Fragment.EN);

    assertFalse(CircumfixStation.consume(List.of(first), Fragment.GE, Fragment.EN));
    assertEquals(1, first.fetch(InventoryComponent.class).orElseThrow().count());
    assertTrue(CircumfixStation.consume(List.of(first, second), Fragment.GE, Fragment.EN));
    assertEquals(0, first.fetch(InventoryComponent.class).orElseThrow().count());
    assertEquals(0, second.fetch(InventoryComponent.class).orElseThrow().count());
  }

  @Test
  void stationHandsOutTheMagazineOnlyWhileNobodyCarriesItAndA12LacksIt() {
    MorphCoState state = new MorphCoState();
    state.assemble(Circumfix.GE_E);
    state.assemble(Circumfix.BE_IG);
    assertFalse(CircumfixStation.magazineMissing(state, Stream.empty()));

    state.assemble(Circumfix.GE_EN);
    assertTrue(CircumfixStation.magazineMissing(state, Stream.empty()));

    Entity carrier = new Entity();
    InventoryComponent inventory = new InventoryComponent();
    carrier.add(inventory);
    inventory.add(new CircumfixMagazineItem());
    assertFalse(CircumfixStation.magazineMissing(state, Stream.of(carrier)));

    // A rejoining player loses the magazine; the station hands out a new one.
    CircumfixStation.returnToPile(carrier);
    assertEquals(0, inventory.count(CircumfixMagazineItem.class));
    assertTrue(CircumfixStation.magazineMissing(state, Stream.of(carrier)));

    state.set(MorphCoState.MAGAZINE_INSERTED);
    assertFalse(CircumfixStation.magazineMissing(state, Stream.empty()));
  }

  @Test
  void leverStaysStuckUntilTheMagazineIsInA12() {
    MorphCoState state = new MorphCoState();
    state.set(MorphCoState.SHAFTS_FILLED);
    for (Circumfix circumfix : Circumfix.values()) state.assemble(circumfix);
    assertTrue(state.leverStuck());
    assertTrue(state.waitsForCircumfixes());

    state.set(MorphCoState.MAGAZINE_INSERTED);
    assertFalse(state.leverStuck());
    assertFalse(state.waitsForCircumfixes());
  }

  private static Entity carrying(Fragment part) {
    Entity player = new Entity();
    InventoryComponent inventory = new InventoryComponent();
    player.add(inventory);
    inventory.add(CircumfixFragmentItem.of(part));
    return player;
  }
}
