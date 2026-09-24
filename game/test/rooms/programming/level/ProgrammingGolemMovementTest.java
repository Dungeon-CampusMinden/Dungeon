package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.level.utils.LevelElement;
import engine.systems.LevelSystem;
import engine.systems.MoveSystem;
import engine.systems.VelocitySystem;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.state.StateMachine;
import feature.components.CollideComponent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rooms.programming.ProgrammingRoomController;
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.variables.VariablePuzzle;
import testingUtils.MockNetworkHandler;

/** Executes real loop instructions and movement against the generated cellar geometry. */
class ProgrammingGolemMovementTest {
  private static final Point ORIGIN = new Point(20, 20);
  private ProgrammingGolemRuntime runtime;
  private ProgrammingRoomController controller;
  private PositionComponent position;
  private Entity player;
  private MoveSystem movement;
  private VelocitySystem velocities;
  private final java.util.Set<Coordinate> blocked = new java.util.HashSet<>();

  @BeforeEach
  void setup() throws Exception {
    MockNetworkHandler.useLocalNetworkHandler();
    Game.add(new LevelSystem());
    DungeonLevel level = mock(DungeonLevel.class);
    Map<String, Point> points = Map.of("maze-origin", ORIGIN, "loop-terminal", new Point(1, 1));
    when(level.namedPoints()).thenReturn(points);
    when(level.getPoint(anyString())).thenAnswer(call -> points.get(call.getArgument(0)));
    LevelElement[][] layout =
        ProgrammingMazeWorld.layout(new LevelElement[][] {{LevelElement.FLOOR}}, points);
    Tile floor = mock(Tile.class);
    when(floor.isAccessible()).thenReturn(true);
    when(level.tileAt(any(Coordinate.class)))
        .thenAnswer(
            call -> {
              Coordinate c = call.getArgument(0);
              return c.y() >= 0
                      && c.y() < layout.length
                      && c.x() >= 0
                      && c.x() < layout[0].length
                      && layout[c.y()][c.x()] == LevelElement.FLOOR
                      && !blocked.contains(c)
                  ? Optional.of(floor)
                  : Optional.empty();
            });
    when(level.tileAt(any(Point.class)))
        .thenAnswer(call -> level.tileAt(((Point) call.getArgument(0)).toCoordinate()));
    Game.currentLevel(level);
    Entity golem = Entity.createLocalEntity("programming-golem");
    position = new PositionComponent(ORIGIN);
    float scale = 5f * 64f / 63f;
    position.scale(scale);
    golem.add(position);
    golem.add(new VelocityComponent(2.5f, 8f));
    golem.add(
        new CollideComponent(Vector2.of(3f / 64f, 3f / 64f), Vector2.of(58f / 64f, 2.5f / scale)));
    DrawComponent draw = mock(DrawComponent.class);
    when(draw.stateMachine()).thenReturn(mock(StateMachine.class));
    Game.add(golem);
    golem.add(draw);
    try (var props = mockStatic(ProgrammingProps.class);
        var machinery = mockConstruction(ProgrammingCellarMachinery.class);
        var workshop = mockConstruction(ProgrammingWorkshopRuntime.class)) {
      props.when(() -> ProgrammingProps.wall(level)).thenReturn(List.of());
      runtime = new ProgrammingGolemRuntime(level, golem);
    }
    controller = (ProgrammingRoomController) field("controller").get(runtime);
    controller.submitVessels(VariablePuzzle.vesselSolution());
    controller.submitEssences(VariablePuzzle.essenceSolution());
    controller.activateGolem();
    LoopPuzzle.runes().forEach(r -> controller.collectLoopRune(r.id()));
    field("mazeReady").set(runtime, true);
    player = new Entity();
    player.add(new PositionComponent(new Point(1, 1)));
    player.add(new engine.components.PlayerComponent());
    Game.add(player);
    movement = new MoveSystem();
    velocities = new VelocitySystem();
    Game.add(movement);
    Game.add(velocities);
  }

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  @Test
  void mapReportsPhysicalPositionBetweenCells() {
    runtime.executeRune("forge-press-for", player);
    for (int i = 0; i < 12; i++) tick();
    assertTrue(position.position().x() > ORIGIN.x());
    assertTrue(position.position().x() < ORIGIN.x() + LoopMaze.CELL_WIDTH);
    assertEquals(
        (position.position().x() - ORIGIN.x()) / LoopMaze.CELL_WIDTH,
        runtime.terminalState().cellX(),
        .0001f);
    assertEquals("schritt()", runtime.terminalState().status());
  }

  @Test
  void longRuneReachesCorridorEndBeforeReportingWall() {
    runtime.executeRune("archive-long", player);
    for (int i = 0; i < 2000 && !runtime.terminalState().status().contains("Wand"); i++) {
      Point previous = position.position();
      tick();
      assertTrue(
          Point.calculateDistance(previous, position.position())
              <= 2.5f / Game.frameRate() + .001f);
      if (position.position().x() < ORIGIN.x() + 4 * LoopMaze.CELL_WIDTH - .01f)
        assertFalse(runtime.terminalState().status().contains("Wand"));
    }
    assertTrue(runtime.terminalState().status().contains("Wand"), runtime.terminalState().status());
    float feetRight = position.position().x() + 61f / 64f * position.scale().x();
    assertEquals(
        ORIGIN.x() + 5 * LoopMaze.CELL_WIDTH,
        feetRight,
        2.5f / Game.frameRate() + .001f,
        "The invalid step must run until Nox's feet reach the wall");
    for (int i = 0; i < 2000 && runtime.terminalState().busy(); i++) {
      Point previous = position.position();
      tick();
      assertTrue(
          Point.calculateDistance(previous, position.position()) <= 2.5f / Game.frameRate() + .001f,
          "Returning must not teleport");
    }
    assertFalse(runtime.terminalState().busy());
    assertEquals(ORIGIN, position.position());
  }

  @ParameterizedTest
  @CsvSource({
    "0,forge-press-for",
    "1,bellows-while",
    "2,chain-lift-while",
    "3,cooling-channel-do-while",
    "4,heart-gate-for"
  })
  void correctRuneFinishesOnlyAfterPhysicalExecution(int checkpoint, String rune) {
    for (int i = 0; i < checkpoint; i++)
      controller.completeExecutedLoop(LoopPuzzle.challenges().get(i));
    position.position(LoopMaze.world(ORIGIN, LoopMaze.checkpoints().get(checkpoint).start()));
    runtime.faceWorkshop(LoopMaze.checkpoints().get(checkpoint).facing());
    runtime.executeRune(rune, player);
    for (int i = 0; i < 2000 && !runtime.terminalState().status().contains("bestätigt"); i++) {
      tick();
      assertEquals(
          (position.position().x() - ORIGIN.x()) / LoopMaze.CELL_WIDTH,
          ProgrammingTerminal.mapPosition(runtime.terminalState()).x(),
          .0001f);
      assertEquals(
          (position.position().y() - ORIGIN.y()) / LoopMaze.CELL_HEIGHT,
          ProgrammingTerminal.mapPosition(runtime.terminalState()).y(),
          .0001f);
    }
    assertTrue(
        runtime.terminalState().status().contains("bestätigt"), runtime.terminalState().status());
    assertEquals(
        LoopMaze.world(ORIGIN, LoopMaze.checkpoints().get(checkpoint).goal()), position.position());
  }

  @Test
  void unrelatedGamePauseDoesNotFailAnActiveRune() {
    runtime.executeRune("forge-press-for", player);
    for (int i = 0; i < 12; i++) tick();
    Point paused = position.position();
    movement.stop();
    for (int i = 0; i < 300; i++) runtime.tick();
    assertEquals(paused, position.position());
    assertEquals("schritt()", runtime.terminalState().status());
    movement.run();
    tick();
    assertTrue(position.position().x() > paused.x());
  }

  @Test
  void interruptedStepReturnsFromActualPositionAndWaitsForBlockedReturn() {
    blocked.add(new Coordinate(27, 20));
    runtime.executeRune("forge-press-for", player);
    for (int i = 0; i < 300 && !runtime.terminalState().status().contains("gestoppt"); i++) tick();
    assertTrue(runtime.terminalState().status().contains("gestoppt"));
    Point stopped = position.position();
    assertTrue(stopped.x() > ORIGIN.x());
    assertTrue(stopped.x() < ORIGIN.x() + LoopMaze.CELL_WIDTH);
    blocked.add(new Coordinate(22, 20));
    for (int i = 0; i < 300; i++) {
      tick();
      assertEquals(
          stopped,
          position.position(),
          "A blocked return must wait, not teleport or move to the unreached destination");
    }
    blocked.clear();
    for (int i = 0; i < 300 && runtime.terminalState().busy(); i++) {
      Point previous = position.position();
      tick();
      assertTrue(position.position().x() <= previous.x());
      assertTrue(
          Point.calculateDistance(previous, position.position())
              <= 2.5f / Game.frameRate() + .001f);
    }
    assertFalse(runtime.terminalState().busy());
    assertEquals(ORIGIN, position.position());
  }

  private void tick() {
    runtime.tick();
    velocities.execute();
    movement.execute();
  }

  private static Field field(String name) throws Exception {
    Field field = ProgrammingGolemRuntime.class.getDeclaredField(name);
    field.setAccessible(true);
    return field;
  }
}
