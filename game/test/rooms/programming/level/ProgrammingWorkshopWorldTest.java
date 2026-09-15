package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.level.utils.LevelElement;
import engine.utils.Point;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import rooms.programming.modules.methods.MethodsRoute;

class ProgrammingWorkshopWorldTest {
  @Test
  void originalProgramConnectsReachableObjectsAndRestoresTheirState() {
    assertEquals(new Point(30, 26), ProgrammingWorkshopWorld.start(0));
    for (int station = 1; station < MethodsRoute.STATIONS.size(); station++) {
      assertEquals(
          ProgrammingWorkshopWorld.path(station - 1).getLast(),
          ProgrammingWorkshopWorld.start(station));
      assertEquals(
          ProgrammingWorkshopWorld.endFacing(station - 1),
          ProgrammingWorkshopWorld.startFacing(station));
    }
    var points = new HashMap<String, Point>();
    ProgrammingWorkshopWorld.layout(new LevelElement[23][47], points);
    assertEquals(points.get("methods-exit"), ProgrammingWorkshopWorld.path(7).getLast());
    ProgrammingWorkshopWorld.resetAll();
    assertFalse(
        ProgrammingWorkshopWorld.perform(
                MethodsRoute.Action.COLLECT, ProgrammingWorkshopWorld.actionPoint(4), 0)
            .success());
    for (var station : MethodsRoute.STATIONS) {
      var action =
          station.body().stream()
              .filter(
                  step ->
                      step.action() != MethodsRoute.Action.MOVE
                          && step.action() != MethodsRoute.Action.TURN)
              .findFirst()
              .orElseThrow()
              .action();
      Point at = ProgrammingWorkshopWorld.actionPoint(station.index());
      assertEquals(
          station.index(),
          ProgrammingWorkshopWorld.stationAt(action, at.translate(.5f, 0)).orElseThrow());
      assertTrue(ProgrammingWorkshopWorld.stationAt(action, new Point(0, 0)).isEmpty());
      if (action == MethodsRoute.Action.PLACE)
        assertFalse(ProgrammingWorkshopWorld.perform(action, at, station.amount() + 1).success());
      var result = ProgrammingWorkshopWorld.perform(action, at, station.amount());
      assertTrue(result.success(), result.reason());
      if (action == MethodsRoute.Action.COLLECT) {
        assertEquals(station.amount(), result.value());
        assertEquals(0, ProgrammingWorkshopWorld.perform(action, at, 0).value());
      }
    }
    assertTrue(ProgrammingWorkshopWorld.solved());
    ProgrammingWorkshopWorld.resetAll();
    assertFalse(ProgrammingWorkshopWorld.solved());
    assertFalse(
        ProgrammingWorkshopWorld.perform(
                MethodsRoute.Action.COLLECT, ProgrammingWorkshopWorld.actionPoint(4), 0)
            .success());
  }

  @Test
  void fullFiveByTwoAndAHalfFootprintFitsEveryMovement() {
    var layout = ProgrammingWorkshopWorld.layout(new LevelElement[23][47], new HashMap<>());
    for (int station = 0; station < MethodsRoute.STATIONS.size(); station++) {
      Point from = ProgrammingWorkshopWorld.start(station);
      for (Point to : ProgrammingWorkshopWorld.path(station)) {
        int samples = Math.max(1, (int) (Point.calculateDistance(from, to) * 10));
        for (int sample = 0; sample <= samples; sample++) {
          float x = from.x() + (to.x() - from.x()) * sample / samples;
          float y = from.y() + (to.y() - from.y()) * sample / samples;
          for (int tileY = (int) y; tileY <= (int) (y + 2.5f - .001f); tileY++)
            for (int tileX = (int) x; tileX <= (int) (x + 5 - .001f); tileX++)
              assertTrue(
                  layout[tileY][tileX] == LevelElement.FLOOR,
                  "station "
                      + station
                      + " at "
                      + x
                      + ","
                      + y
                      + " blocked by "
                      + tileX
                      + ","
                      + tileY);
        }
        from = to;
      }
    }
  }

  @Test
  void thresholdGatesFillTheirOnlyWallOpenings() {
    var layout = ProgrammingWorkshopWorld.layout(new LevelElement[23][47], new HashMap<>());
    for (int y : new int[] {29, 33})
      for (int x = 27; x <= 38; x++)
        assertEquals(x >= 30 && x <= 34 ? LevelElement.FLOOR : LevelElement.WALL, layout[y][x]);
  }
}
