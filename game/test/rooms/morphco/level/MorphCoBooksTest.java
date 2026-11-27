package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.MorphCoBookItem;
import rooms.morphco.ui.MorphCoDialogs;

class MorphCoBooksTest {

  private static List<Class<?>> books(InventoryComponent inventory) {
    return Arrays.stream(inventory.items())
        .filter(Objects::nonNull)
        .<Class<?>>map(Item::getClass)
        .toList();
  }

  private static void bindsInOrder(MorphCoBookItem first, MorphCoBookItem second) {
    String order = first.document() + " then " + second.document();
    InventoryComponent inventory = new InventoryComponent();

    assertEquals(Optional.of(first), MorphCoBooks.receive(inventory, first), order);
    assertTrue(
        MorphCoBooks.receive(inventory, second).orElseThrow() instanceof MorphCoBookItem.Handout,
        order);
    assertEquals(List.of(MorphCoBookItem.Handout.class), books(inventory), order);

    assertEquals(
        Optional.empty(), MorphCoBooks.receive(inventory, new MorphCoBookItem.Part1()), order);
    assertEquals(
        Optional.empty(), MorphCoBooks.receive(inventory, new MorphCoBookItem.Part2()), order);
    assertEquals(List.of(MorphCoBookItem.Handout.class), books(inventory), order);
  }

  @Test
  void secondVolumeBindsBothIntoTheHandoutInEitherOrder() {
    bindsInOrder(new MorphCoBookItem.Part1(), new MorphCoBookItem.Part2());
    bindsInOrder(new MorphCoBookItem.Part2(), new MorphCoBookItem.Part1());
  }

  @Test
  void manualAndBothVolumesBindIntoTheCollectedEditionInAnyOrder() {
    List<List<MorphCoBookItem>> orders =
        List.of(
            List.of(
                new MorphCoBookItem.Manual(),
                new MorphCoBookItem.Part1(),
                new MorphCoBookItem.Part2()),
            List.of(
                new MorphCoBookItem.Part1(),
                new MorphCoBookItem.Manual(),
                new MorphCoBookItem.Part2()),
            List.of(
                new MorphCoBookItem.Part2(),
                new MorphCoBookItem.Part1(),
                new MorphCoBookItem.Manual()));
    for (List<MorphCoBookItem> order : orders) {
      InventoryComponent inventory = new InventoryComponent();
      order.forEach(book -> MorphCoBooks.receive(inventory, book));
      assertEquals(List.of(MorphCoBookItem.Collected.class), books(inventory), order.toString());
      assertEquals(
          Optional.empty(),
          MorphCoBooks.receive(inventory, new MorphCoBookItem.Manual()),
          order.toString());
    }
  }

  @Test
  void manualStaysApartFromASingleVolume() {
    InventoryComponent inventory = new InventoryComponent();
    MorphCoBooks.receive(inventory, new MorphCoBookItem.Manual());
    MorphCoBooks.receive(inventory, new MorphCoBookItem.Part1());
    assertEquals(
        List.of(MorphCoBookItem.Manual.class, MorphCoBookItem.Part1.class), books(inventory));
  }

  @Test
  void checkpointHandsOutTheCollectedEdition() {
    MorphCoState state = new MorphCoState();
    state.set(MorphCoBooks.flag(MorphCoDialogs.HANDBOOK, "Andre"));
    state.set(MorphCoBooks.flag(MorphCoDialogs.HANDOUT, "Andre"));
    state.set(MorphCoBooks.flag(MorphCoDialogs.COLLECTED, "Andre"));
    assertTrue(MorphCoBooks.anyHandout(state));
    InventoryComponent inventory = new InventoryComponent();
    MorphCoBooks.restore(state, "Andre", inventory);
    assertEquals(List.of(MorphCoBookItem.Collected.class), books(inventory));
  }

  @Test
  void checkpointHandsOutTheHandoutInsteadOfTheVolumes() {
    MorphCoState state = new MorphCoState();
    for (String name : List.of("Andre", "Kim")) {
      state.set(MorphCoBooks.flag(MorphCoDialogs.MORPH_1, name));
      state.set(MorphCoBooks.flag(MorphCoDialogs.MORPH_2, name));
    }
    assertFalse(MorphCoBooks.anyHandout(state));
    state.set(MorphCoBooks.flag(MorphCoDialogs.HANDOUT, "Andre"));
    assertTrue(MorphCoBooks.anyHandout(state));

    InventoryComponent bound = new InventoryComponent();
    MorphCoBooks.restore(state, "Andre", bound);
    assertEquals(List.of(MorphCoBookItem.Handout.class), books(bound));

    InventoryComponent volumes = new InventoryComponent();
    MorphCoBooks.restore(state, "Kim", volumes);
    assertEquals(List.of(MorphCoBookItem.Handout.class), books(volumes));

    InventoryComponent single = new InventoryComponent();
    state.set(MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Lea"));
    MorphCoBooks.restore(state, "Lea", single);
    assertEquals(List.of(MorphCoBookItem.Part1.class), books(single));
  }
}
