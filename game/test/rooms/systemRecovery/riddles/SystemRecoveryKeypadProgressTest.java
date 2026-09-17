package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.hints.HintSystem;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.petrinet.PetriNetSystem;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;

/** Ensures a correct keypad code only opens its door during the matching learning step. */
class SystemRecoveryKeypadProgressTest {

  private final DungeonLevel level = Mockito.mock(DungeonLevel.class);
  private MockedStatic<Game> game;
  private MockedStatic<KeypadFactory> keypads;
  private DoorTile door;
  private KeypadComponent keypad;

  @BeforeEach
  void setUp() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    Game.add(new HintSystem());
    SystemRecoveryProgressNet.initialize();

    door =
        new DoorTile(new SimpleIPath("tiles/door.png"), new Coordinate(2, 2), DesignLabel.DEFAULT);
    door.close();
    game = mockStatic(Game.class, CALLS_REAL_METHODS);
    game.when(() -> Game.tileAt(any(Point.class))).thenReturn(Optional.of(door));
    game.when(() -> Game.add(any(Entity.class))).thenAnswer(ignored -> null);

    keypads = mockStatic(KeypadFactory.class);
    keypads
        .when(
            () ->
                KeypadFactory.createKeypad(
                    any(Point.class), anyList(), any(Runnable.class), anyBoolean()))
        .thenAnswer(
            invocation -> {
              List<Integer> code = invocation.getArgument(1);
              Runnable action = invocation.getArgument(2);
              keypad = new KeypadComponent(code, action, invocation.getArgument(3));
              Entity entity = new Entity("test-keypad");
              entity.add(keypad);
              return entity;
            });

    Mockito.when(level.getPoint(any(String.class))).thenAnswer(invocation -> new Point(1, 1));
  }

  @AfterEach
  void tearDown() {
    if (keypads != null) keypads.close();
    if (game != null) game.close();
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void inventoryScannerDoorCodeCannotBeConsumedBeforeItsStep() throws ReflectiveOperationException {
    invokeSetup(new ModuleStorageRiddle(level), "setupRoomThreeKeypad");

    assertEquals(List.of(0, 5, 0, 2), keypad.correctDigits());
    submit("5");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("52");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0502");

    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());
    assertTrue(keypad.enteredDigits().isEmpty());
    assertEquals(
        SystemRecoveryLearningStep.ENERGY_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());

    activate(SystemRecoveryLearningStep.ROOM2_DOOR_CODE);
    submit("5");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("52");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0503");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0502");

    assertTrue(door.isOpen());
    assertTrue(keypad.isUnlocked());
    assertEquals(
        SystemRecoveryLearningStep.INVENTORY_COUNT,
        SystemRecoveryProgressNet.activeStep().orElseThrow());
  }

  @Test
  void transportDoorCodeCannotBeConsumedBeforeItsStep() throws ReflectiveOperationException {
    invokeSetup(new InventoryScannerRiddle(level), "setupTransportStorageKeypad");

    assertEquals(List.of(0, 5, 0, 4), keypad.correctDigits());
    submit("4");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("54");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0504");

    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());
    assertTrue(keypad.enteredDigits().isEmpty());
    assertEquals(
        SystemRecoveryLearningStep.ENERGY_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());

    activate(SystemRecoveryLearningStep.ROOM3_DOOR_CODE);
    submit("4");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("54");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0505");
    assertFalse(door.isOpen());
    assertFalse(keypad.isUnlocked());

    submit("0504");

    assertTrue(door.isOpen());
    assertTrue(keypad.isUnlocked());
    assertEquals(
        SystemRecoveryLearningStep.TRANSPORT_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());
  }

  private void submit(String code) {
    Entity player = new Entity("test-player");
    keypad.enteredDigits().clear();
    code.chars().forEach(digit -> keypad.addDigit(Character.digit(digit, 10)));
    keypad.checkUnlock(player);
  }

  private void activate(SystemRecoveryLearningStep target) {
    SystemRecoveryLearningStep current = SystemRecoveryProgressNet.activeStep().orElseThrow();
    while (current != target) {
      assertTrue(SystemRecoveryProgressNet.complete(current), "failed advancing past " + current);
      current = SystemRecoveryProgressNet.activeStep().orElseThrow();
    }
  }

  private static void invokeSetup(Object riddle, String methodName)
      throws ReflectiveOperationException {
    Method method = riddle.getClass().getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(riddle);
  }
}
