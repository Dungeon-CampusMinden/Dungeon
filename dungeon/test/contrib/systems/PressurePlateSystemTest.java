package contrib.systems;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.LeverComponent;
import contrib.components.PressurePlateComponent;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link PressurePlateSystem}.
 *
 * <p>This test verifies that the system correctly updates the state of a {@link LeverComponent}
 * based on the total mass present on a {@link PressurePlateComponent} attached to the same entity.
 *
 * <p>The tests simulate increasing and decreasing mass on the pressure plate and check if the
 * lever's state changes accordingly after the system executes.
 */
public class PressurePlateSystemTest {

  private Entity plate;
  private PressurePlateSystem system;
  private PressurePlateComponent plateComponent;
  private LeverComponent leverComponent;

  @BeforeEach
  void setUp() {
    plate = new Entity();
    plateComponent = new PressurePlateComponent(2.0f); // threshold 2.0
    leverComponent = new LeverComponent(false, ICommand.NOOP);
    plate.add(plateComponent);
    plate.add(leverComponent);
    system = new PressurePlateSystem();
    Game.add(plate);
  }

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
  }

  /**
   * Tests that the {@link PressurePlateSystem} properly updates the lever state based on the
   * pressure plate's total mass.
   *
   * <p>Specifically, it verifies that:
   *
   * <ul>
   *   <li>The lever starts off in the "off" state.
   *   <li>Adding mass below the pressure plate's trigger threshold keeps the lever off.
   *   <li>Adding enough mass to meet or exceed the threshold turns the lever on.
   *   <li>Removing mass below the threshold turns the lever back off.
   * </ul>
   */
  @Test
  void leverStateIsUpdatedBasedOnPressurePlate() {
    system.execute();
    assertFalse(leverComponent.isOn(), "Lever should be off initially");

    // Add mass below threshold, lever remains off
    plateComponent.increase(1.5f);
    system.execute();
    assertFalse(leverComponent.isOn(), "Lever should still be off");

    // Add mass to exceed threshold, lever should turn on
    plateComponent.increase(1.0f);
    system.execute();
    assertTrue(leverComponent.isOn(), "Lever should turn on when pressure plate is triggered");

    // Remove mass, lever should turn off again
    plateComponent.decrease(2.5f); // test floor at zero
    system.execute();
    assertFalse(
        leverComponent.isOn(), "Lever should turn off when pressure plate is not triggered");
  }
}
