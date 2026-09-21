package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.utils.Point;
import feature.entities.LeverFactory;
import feature.utils.ICommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.entities.EnergyEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;

/** Verifies that the energy display is unlocked by the array step instead of appearing at setup. */
class EnergyRiddleTest {

  private final DungeonLevel level = mock(DungeonLevel.class);
  private MockedStatic<Game> game;
  private MockedStatic<LeverFactory> levers;
  private MockedStatic<EnergyEntityFactory> energyEntities;
  private MockedStatic<SystemRecoveryDisplayFactory> displays;
  private Entity display;

  @BeforeEach
  void setUp() {
    game = mockStatic(Game.class);
    levers = mockStatic(LeverFactory.class);
    energyEntities = mockStatic(EnergyEntityFactory.class);
    displays = mockStatic(SystemRecoveryDisplayFactory.class);

    when(level.getPoint(anyString())).thenReturn(new Point(0, 0));
    levers
        .when(() -> LeverFactory.createLever(any(Point.class), any(ICommand.class)))
        .thenReturn(new Entity("array-lever"));
    energyEntities
        .when(() -> EnergyEntityFactory.batteryBox(any(Point.class), any(Runnable.class)))
        .thenReturn(new Entity("battery-box"));
    energyEntities
        .when(() -> EnergyEntityFactory.cryoBox(any(Point.class), anyBoolean()))
        .thenAnswer(invocation -> new Entity("energy-cryo-box"));
    display = new Entity("energy-display");
    displays
        .when(() -> SystemRecoveryDisplayFactory.hintDisplay(any(Point.class), any(), anyString()))
        .thenReturn(display);
  }

  @AfterEach
  void tearDown() {
    displays.close();
    energyEntities.close();
    levers.close();
    game.close();
  }

  @Test
  void displayIsCreatedOnlyAfterEnergyArrayStep() {
    EnergyRiddle riddle = new EnergyRiddle(level);

    riddle.setup();

    assertFalse(riddle.energyArrayCreated());
    displays.verify(
        () -> SystemRecoveryDisplayFactory.hintDisplay(any(Point.class), any(), anyString()),
        times(1));
    displays.verifyNoMoreInteractions();

    riddle.spawnEnergyCrates();

    assertTrue(riddle.energyArrayCreated());
    displays.verify(
        () -> SystemRecoveryDisplayFactory.updateDisplayText(any(Entity.class), anyString()),
        times(1));
  }

  @Test
  void repeatedArrayCallbackDoesNotCreateDuplicateDisplay() {
    EnergyRiddle riddle = new EnergyRiddle(level);
    riddle.setup();

    riddle.spawnEnergyCrates();
    riddle.spawnEnergyCrates();

    displays.verify(
        () -> SystemRecoveryDisplayFactory.hintDisplay(any(Point.class), any(), anyString()),
        times(1));
    displays.verify(
        () -> SystemRecoveryDisplayFactory.updateDisplayText(any(Entity.class), anyString()),
        times(1));
  }
}
