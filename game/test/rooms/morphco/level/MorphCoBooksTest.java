package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.components.PlayerComponent;
import feature.components.InventoryComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.ui.MorphCoDialogs;

/** Tests the morpheme books that players carry. */
class MorphCoBooksTest {

  private final MorphCoState state = new MorphCoState();
  private final MorphCoLevel level = mock(MorphCoLevel.class);

  MorphCoBooksTest() {
    when(level.state()).thenReturn(state);
    when(level.observe(anyString())).thenAnswer(call -> state.set(call.getArgument(0)));
  }

  @Test
  void aLoadedStateHandsEveryPlayerTheirOwnBooks() {
    state.restore(
        Set.of(
            MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Ada"),
            MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Bo"),
            MorphCoBooks.flag(MorphCoDialogs.MORPH_2, "Bo"),
            MorphCoBooks.flag(MorphCoDialogs.HANDOUT, "Bo")),
        Set.of(),
        0,
        0);

    assertEquals(List.of(MorphemeTypesItem.Part1.class), books(join("Ada")));
    assertEquals(List.of(MorphemeTypesItem.Handout.class), books(join("Bo")));
    assertEquals(List.of(), books(join("Cy")));
    verify(level, never()).save();
  }

  @Test
  void aBookTakenAfterJoiningIsRecordedAndSavedOnce() {
    InventoryComponent inventory = join("Ada");

    inventory.add(new MorphemeTypesItem.Part1());
    inventory.add(new MorphemeTypesItem.Part1());

    assertTrue(state.has(MorphCoBooks.flag(MorphCoDialogs.MORPH_1, "Ada")));
    verify(level, times(1)).save();
    assertEquals(List.of(MorphemeTypesItem.Part1.class), books(join("Ada")));
  }

  private InventoryComponent join(String name) {
    Entity player = new Entity();
    player.add(new PlayerComponent(false, name));
    InventoryComponent inventory = new InventoryComponent();
    player.add(inventory);
    MorphCoBooks.carry(level, player);
    return inventory;
  }

  private static List<Class<?>> books(InventoryComponent inventory) {
    return Arrays.stream(inventory.items())
        .filter(Objects::nonNull)
        .<Class<?>>map(Object::getClass)
        .toList();
  }
}
