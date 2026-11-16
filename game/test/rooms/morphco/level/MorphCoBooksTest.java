package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feature.components.InventoryComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.CircumfixFragmentItem;
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.ui.MorphCoDialogs;

/** Tests the morpheme books that players carry. */
class MorphCoBooksTest {

  @Test
  void bothVolumesCombineIntoTheHandoutInEitherOrder() {
    MorphemeTypesItem part1 = new MorphemeTypesItem.Part1();
    MorphemeTypesItem part2 = new MorphemeTypesItem.Part2();

    assertInstanceOf(MorphemeTypesItem.Handout.class, part1.combine(part2).orElseThrow());
    assertInstanceOf(MorphemeTypesItem.Handout.class, part2.combine(part1).orElseThrow());
    assertTrue(part1.combine(new MorphemeTypesItem.Part1()).isEmpty());
    assertTrue(part1.combine(new MorphemeTypesItem.Handout()).isEmpty());
    assertTrue(part2.combine(CircumfixFragmentItem.of(CircumfixPuzzle.Fragment.GE)).isEmpty());
  }

  @Test
  void aLoadedStateHandsEveryPlayerTheirOwnBooks() {
    MorphCoState state = new MorphCoState();
    state.restore(
        Set.of(
            MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Ada"),
            MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Bo"),
            MorphCoBooks.flag(MorphCoDialogs.MORPH_2, "Bo"),
            MorphCoBooks.flag(MorphCoDialogs.HANDOUT, "Bo")),
        Set.of(),
        0,
        0);

    assertEquals(List.of(MorphemeTypesItem.Part1.class), books(state, "Ada"));
    assertEquals(List.of(MorphemeTypesItem.Handout.class), books(state, "Bo"));
    assertEquals(List.of(), books(state, "Cy"));
  }

  private static List<Class<?>> books(MorphCoState state, String name) {
    InventoryComponent inventory = new InventoryComponent();
    MorphCoBooks.restore(state, name, inventory);
    return Arrays.stream(inventory.items())
        .filter(Objects::nonNull)
        .<Class<?>>map(Object::getClass)
        .toList();
  }
}
