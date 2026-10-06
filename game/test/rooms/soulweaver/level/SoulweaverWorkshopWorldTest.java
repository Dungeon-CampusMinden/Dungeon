package rooms.soulweaver.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.level.utils.LevelElement;
import engine.utils.Point;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import rooms.soulweaver.modules.methods.MethodsRoute;

class SoulweaverWorkshopWorldTest {
  @Test
  void originalProgramConnectsReachableObjectsAndRestoresTheirState() {
    assertEquals(new Point(30, 26), SoulweaverWorkshopWorld.start(0));
    for (int station = 1; station < MethodsRoute.STATIONS.size(); station++) {
      assertEquals(
          SoulweaverWorkshopWorld.path(station - 1).getLast(),
          SoulweaverWorkshopWorld.start(station));
      assertEquals(
          SoulweaverWorkshopWorld.endFacing(station - 1),
          SoulweaverWorkshopWorld.startFacing(station));
    }
    var points = new HashMap<String, Point>();
    SoulweaverWorkshopWorld.layout(new LevelElement[23][47], points);
    assertEquals(points.get("methods-exit"), SoulweaverWorkshopWorld.path(7).getLast());
    SoulweaverWorkshopWorld.resetAll();
    assertFalse(
        SoulweaverWorkshopWorld.perform(
                MethodsRoute.Action.COLLECT, SoulweaverWorkshopWorld.actionPoint(4), 0)
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
      Point at = SoulweaverWorkshopWorld.actionPoint(station.index());
      assertEquals(
          station.index(),
          SoulweaverWorkshopWorld.stationAt(action, at.translate(.5f, 0)).orElseThrow());
      assertTrue(SoulweaverWorkshopWorld.stationAt(action, new Point(0, 0)).isEmpty());
      if (action == MethodsRoute.Action.PLACE)
        assertFalse(SoulweaverWorkshopWorld.perform(action, at, station.amount() + 1).success());
      var result = SoulweaverWorkshopWorld.perform(action, at, station.amount());
      assertTrue(result.success(), result.reason());
      if (action == MethodsRoute.Action.COLLECT) {
        assertEquals(station.amount(), result.value());
        assertEquals(0, SoulweaverWorkshopWorld.perform(action, at, 0).value());
      }
    }
    assertTrue(SoulweaverWorkshopWorld.solved());
    SoulweaverWorkshopWorld.resetAll();
    assertFalse(SoulweaverWorkshopWorld.solved());
    assertFalse(
        SoulweaverWorkshopWorld.perform(
                MethodsRoute.Action.COLLECT, SoulweaverWorkshopWorld.actionPoint(4), 0)
            .success());
  }

  @Test
  void fullFiveByTwoAndAHalfFootprintFitsEveryMovement() {
    var layout = SoulweaverWorkshopWorld.layout(new LevelElement[23][47], new HashMap<>());
    for (int station = 0; station < MethodsRoute.STATIONS.size(); station++) {
      Point from = SoulweaverWorkshopWorld.start(station);
      for (Point to : SoulweaverWorkshopWorld.path(station)) {
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
    var layout = SoulweaverWorkshopWorld.layout(new LevelElement[23][47], new HashMap<>());
    for (int y : new int[] {29, 33})
      for (int x = 27; x <= 38; x++)
        assertEquals(x >= 30 && x <= 34 ? LevelElement.FLOOR : LevelElement.WALL, layout[y][x]);
  }
}
