package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.loader.LevelParser;
import engine.systems.FrictionSystem;
import engine.systems.LevelSystem;
import engine.systems.MoveSystem;
import engine.systems.VelocitySystem;
import engine.utils.Point;
import feature.components.ItemComponent;
import feature.systems.AISystem;
import feature.systems.CollisionSystem;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.entities.SystemCoreEntityFactory;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;

/** Runs the search robot with the real movement systems on the shipped room layout. */
class SearchRobotMovementTest {
  private AISystem ai;
  private VelocitySystem velocity;
  private FrictionSystem friction;
  private MoveSystem move;
  private CollisionSystem collision;
  private DungeonLevel level;
  private int previousFrameRate;

  @BeforeEach
  void setUp() throws Exception {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new LevelSystem());
    previousFrameRate = Game.frameRate();
    Game.frameRate(60);
    try (InputStream resource =
        getClass().getResourceAsStream("/levels/systemRecovery/systemrecovery_1.level")) {
      assertNotNull(resource);
      level =
          LevelParser.parseLevel(
              new String(resource.readAllBytes(), StandardCharsets.UTF_8), "test");
    }
    Game.currentLevel(level);
    collision = new CollisionSystem();
    Game.add(collision);
    ai = new AISystem();
    velocity = new VelocitySystem();
    friction = new FrictionSystem();
    move = new MoveSystem();
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
    Game.frameRate(previousFrameRate);
  }

  @Test
  void robotKeepsWalkingPastFirstMatrixCell() {
    AtomicLong clock = new AtomicLong();
    SearchRobotRiddle riddle = new SearchRobotRiddle(level, RiddleCallbacks.noop(), clock::get);
    riddle.setup();
    riddle.startScan();
    Entity robot =
        Game.levelEntities().filter(e -> "search_robot".equals(e.name())).findFirst().orElseThrow();

    for (int tick = 0; tick < 180; tick++) {
      clock.addAndGet(50);
      velocity.execute();
      friction.execute();
      move.execute();
      ai.execute();
      collision.execute();
    }

    assertFalse(riddle.completed());
    assertTrue(robot.fetch(PositionComponent.class).orElseThrow().position().x() > 24.8f);
  }

  @Test
  void robotScansEveryCellAndDeliversModule() {
    AtomicLong clock = new AtomicLong();
    SearchRobotRiddle riddle = new SearchRobotRiddle(level, RiddleCallbacks.noop(), clock::get);
    riddle.setup();
    riddle.startScan();
    assertEquals(-1, Game.tileAt(new Point(23, 41)).orElseThrow().tintColor());
    Entity robot =
        Game.levelEntities().filter(e -> "search_robot".equals(e.name())).findFirst().orElseThrow();
    Point previous = robot.fetch(PositionComponent.class).orElseThrow().position();
    Set<Integer> visited = new HashSet<>();
    boolean observedHighlight = false;
    SearchRobotMatrix matrix =
        SearchRobotMatrix.between(level.getPoint("roboter_start"), level.getPoint("roboter_end"));
    int previousIndex = 0;

    for (int tick = 0; tick < 5000 && !riddle.completed(); tick++) {
      clock.addAndGet(50);
      velocity.execute();
      friction.execute();
      move.execute();
      ai.execute();
      collision.execute();
      Point current = robot.fetch(PositionComponent.class).orElseThrow().position();
      assertTrue(
          Point.calculateDistance(previous, current) < 0.1f, "robot teleported at tick " + tick);
      previous = current;
      int index = riddle.currentCellIndex();
      if (index > previousIndex) {
        assertTrue(Point.inRange(current, matrix.pointAt(previousIndex), 0.1f));
        previousIndex = index;
      }
      if (index >= 0) visited.add(index);
      observedHighlight |=
          matrix.points().stream()
              .map(point -> Game.tileAt(point).orElseThrow().tintColor())
              .anyMatch(tint -> tint == SearchRobotRiddle.SCAN_TINT);
    }

    assertTrue(riddle.completed(), "robot did not finish the search and delivery");
    assertTrue(observedHighlight, "no matrix cell was highlighted during scanning");
    assertEquals(28, visited.size());
    assertEquals(
        0, Game.levelEntities().filter(e -> "search_target_item".equals(e.name())).count());
    Entity accessModule =
        Game.levelEntities()
            .filter(
                entity ->
                    entity
                        .fetch(ItemComponent.class)
                        .map(component -> component.item() instanceof SystemCoreAccessChipItem)
                        .orElse(false))
            .findFirst()
            .orElseThrow();
    assertEquals(
        level.getPoint("roboter_item_destionation"),
        accessModule.fetch(PositionComponent.class).orElseThrow().position());
    for (Point cell : matrix.points()) {
      assertEquals(-1, Game.tileAt(cell).orElseThrow().tintColor());
    }
    riddle.startScan();
    assertTrue(riddle.completed(), "finished search restarted");
  }

  @Test
  void dedicatedSecondRobotCanScanTheSystemCoreMatrix() {
    AtomicLong clock = new AtomicLong();
    SearchRobotRiddle firstRiddle =
        new SearchRobotRiddle(level, RiddleCallbacks.noop(), clock::get);
    firstRiddle.setup();
    firstRiddle.startScan();

    for (int tick = 0; tick < 5000 && !firstRiddle.completed(); tick++) {
      clock.addAndGet(50);
      velocity.execute();
      friction.execute();
      move.execute();
      ai.execute();
      collision.execute();
    }
    assertTrue(firstRiddle.completed(), "the first search must deliver the access module first");

    AtomicBoolean coreScanCompleted = new AtomicBoolean();
    SearchRobotMatrix coreMatrix =
        SearchRobotMatrix.between(level.getPoint("map00"), level.getPoint("map24"));
    for (int row = 0; row < coreMatrix.rows(); row++) {
      for (int column = 0; column < coreMatrix.columns(); column++) {
        Game.add(SystemCoreEntityFactory.mapCell(coreMatrix.pointAt(row, column), row, column));
      }
    }
    SearchRobotRiddle coreRiddle = new SearchRobotRiddle(level, RiddleCallbacks.noop(), clock::get);
    coreRiddle.setupSystemCoreRobot(coreMatrix.pointAt(0));
    assertTrue(
        coreRiddle.startSystemCoreScan(coreMatrix, () -> coreScanCompleted.set(true)),
        "the dedicated System Core robot should accept its scan");

    for (int tick = 0; tick < 10000 && !coreScanCompleted.get(); tick++) {
      clock.addAndGet(50);
      velocity.execute();
      friction.execute();
      move.execute();
      ai.execute();
      collision.execute();
    }

    assertTrue(
        coreScanCompleted.get(),
        "the System Core robot did not finish its scan at cell "
            + coreRiddle.currentCellIndex()
            + " from position "
            + Game.levelEntities()
                .filter(e -> "search_robot".equals(e.name()))
                .skip(1)
                .findFirst()
                .flatMap(e -> e.fetch(PositionComponent.class))
                .map(PositionComponent::position)
                .orElse(null));
    assertFalse(coreRiddle.running());
    assertTrue(firstRiddle.completed(), "the original access-module completion must be preserved");
  }
}
