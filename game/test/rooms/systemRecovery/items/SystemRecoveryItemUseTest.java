package rooms.systemRecovery.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Verifies that System Recovery items are not consumed by the generic inventory action. */
class SystemRecoveryItemUseTest {

  private static Stream<Arguments> systemRecoveryItems() {
    return Stream.of(
        Arguments.of(new BatteryItem()),
        Arguments.of(new SearchProgramChipItem()),
        Arguments.of(new SearchProgramChipItem(true)),
        Arguments.of(new SortProgramStickItem()),
        Arguments.of(new SortProgramStickItem(true)),
        Arguments.of(new SystemCoreAccessChipItem()));
  }

  @ParameterizedTest
  @MethodSource("systemRecoveryItems")
  void genericInventoryUseDoesNotConsumeSystemRecoveryItem(Item item) {
    Entity player = new Entity("system-recovery-item-test-player");
    InventoryComponent inventory = new InventoryComponent(2);
    player.add(inventory);
    assertTrue(inventory.add(item));

    item.use(player);

    assertEquals(1, inventory.count(item.getClass()));
    assertTrue(inventory.hasItem(item));
  }
}
