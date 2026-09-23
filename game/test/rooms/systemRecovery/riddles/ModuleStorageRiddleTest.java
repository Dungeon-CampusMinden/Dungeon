package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import feature.interaction.keypad.KeypadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.entities.ModuleEntityFactory;

/** Verifies that the module chips leave the sockets for the inventory scanner. */
class ModuleStorageRiddleTest {
  private final DungeonLevel level = mock(DungeonLevel.class);
  private final List<Entity> sockets = new ArrayList<>();
  private final List<Entity> chips = new ArrayList<>();
  private MockedStatic<Game> game;
  private MockedStatic<ModuleEntityFactory> factory;
  private MockedStatic<KeypadFactory> keypads;

  @BeforeEach
  void setUp() {
    game = mockStatic(Game.class);
    factory = mockStatic(ModuleEntityFactory.class);
    keypads = mockStatic(KeypadFactory.class);
    DoorTile scannerDoor = mock(DoorTile.class);
    game.when(() -> Game.tileAt(any(Point.class))).thenReturn(Optional.of(scannerDoor));

    when(level.getPoint(anyString()))
        .thenAnswer(
            invocation -> {
              String name = invocation.getArgument(0);
              if (name.matches("s\\d")) {
                return new Point(Integer.parseInt(name.substring(1)), 0);
              }
              if (name.startsWith("scanner")) {
                return new Point(Integer.parseInt(name.substring("scanner".length())), 10);
              }
              return new Point(20, 20);
            });
    factory
        .when(() -> ModuleEntityFactory.moduleSocket(any()))
        .thenAnswer(
            invocation -> {
              Entity socket = new Entity("socket");
              socket.add(new PositionComponent(invocation.getArgument(0)));
              sockets.add(socket);
              return socket;
            });
    factory
        .when(() -> ModuleEntityFactory.moduleChip(any(), anyString()))
        .thenAnswer(
            invocation -> {
              Entity chip = new Entity("module");
              chip.add(new PositionComponent(invocation.getArgument(0)));
              chips.add(chip);
              return chip;
            });
    keypads
        .when(() -> KeypadFactory.createKeypad(any(), any(), any(), anyBoolean()))
        .thenReturn(new Entity("keypad"));
  }

  @AfterEach
  void tearDown() {
    keypads.close();
    factory.close();
    game.close();
  }

  @Test
  void portsEveryRemainingModuleToItsMatchingScannerPoint_riddle2() {
    ModuleStorageRiddle riddle = new ModuleStorageRiddle(level);
    riddle.setup();
    riddle.spawnModuleChips();
    riddle.removeGpuChip();

    riddle.portModuleChipsToScanner();

    assertEquals(new Point(0, 10), findAt(new Point(0, 10)));
    assertEquals(new Point(1, 10), findAt(new Point(1, 10)));
    assertEquals(new Point(3, 10), findAt(new Point(3, 10)));
    assertEquals(new Point(4, 10), findAt(new Point(4, 10)));
    assertEquals(new Point(0, 0), position(chips.get(0)));
    assertEquals(new Point(1, 0), position(chips.get(1)));
    assertEquals(new Point(3, 0), position(chips.get(3)));
    assertEquals(new Point(4, 0), position(chips.get(4)));
    game.verify(() -> Game.remove(chips.get(0)));
    game.verify(() -> Game.remove(chips.get(1)));
    game.verify(() -> Game.remove(chips.get(3)));
    game.verify(() -> Game.remove(chips.get(4)));
  }

  private Point findAt(Point target) {
    return chips.stream().map(this::position).filter(target::equals).findFirst().orElseThrow();
  }

  private Point position(Entity entity) {
    return entity.fetch(PositionComponent.class).orElseThrow().position();
  }
}
