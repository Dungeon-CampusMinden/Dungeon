package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import engine.Entity;
import engine.components.DrawComponent;
import feature.components.InventoryComponent;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.items.BatteryItem;

class EnergyEntityFactoryTest {

  @Test
  void markingInsertionTurnsOnAndRemovesTheBoxInventoryOnlyOnce() {
    Entity box = new Entity("battery-box");
    DrawComponent draw = mock(DrawComponent.class);
    InventoryComponent inventory = new InventoryComponent(1);
    assertTrue(inventory.add(new BatteryItem()));
    box.add(draw);
    box.add(inventory);

    EnergyEntityFactory.markBatteryInserted(box);
    EnergyEntityFactory.markBatteryInserted(box);

    verify(draw, times(1)).sendSignal("battery_inserted");
    assertFalse(box.isPresent(InventoryComponent.class));
  }
}
