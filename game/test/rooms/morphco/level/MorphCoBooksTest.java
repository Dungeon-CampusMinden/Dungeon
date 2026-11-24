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
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.ui.MorphCoDialogs;

class MorphCoBooksTest {

  private static List<Class<?>> books(InventoryComponent inventory) {
    return Arrays.stream(inventory.items())
        .filter(Objects::nonNull)
        .<Class<?>>map(Item::getClass)
        .toList();
  }

  private static void bindsInOrder(MorphemeTypesItem first, MorphemeTypesItem second) {
    String order = first.document() + " then " + second.document();
    InventoryComponent inventory = new InventoryComponent();

    assertEquals(Optional.of(first), MorphCoBooks.receive(inventory, first), order);
    assertTrue(
        MorphCoBooks.receive(inventory, second).orElseThrow() instanceof MorphemeTypesItem.Handout,
        order);
    assertEquals(List.of(MorphemeTypesItem.Handout.class), books(inventory), order);

    assertEquals(
        Optional.empty(), MorphCoBooks.receive(inventory, new MorphemeTypesItem.Part1()), order);
    assertEquals(
        Optional.empty(), MorphCoBooks.receive(inventory, new MorphemeTypesItem.Part2()), order);
    assertEquals(List.of(MorphemeTypesItem.Handout.class), books(inventory), order);
  }

  @Test
  void secondVolumeBindsBothIntoTheHandoutInEitherOrder() {
    bindsInOrder(new MorphemeTypesItem.Part1(), new MorphemeTypesItem.Part2());
    bindsInOrder(new MorphemeTypesItem.Part2(), new MorphemeTypesItem.Part1());
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
    assertEquals(List.of(MorphemeTypesItem.Handout.class), books(bound));

    InventoryComponent volumes = new InventoryComponent();
    MorphCoBooks.restore(state, "Kim", volumes);
    assertEquals(
        List.of(MorphemeTypesItem.Part1.class, MorphemeTypesItem.Part2.class), books(volumes));
  }
}
