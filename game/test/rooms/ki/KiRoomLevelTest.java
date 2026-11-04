package rooms.ki;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests what the whiteboard of the AI room shows. */
class KiRoomLevelTest {

  @Test
  void boardListsTheFoundAttributesInTheOrderOfTheTerminals() {
    // Terminal 2 shows the weather, terminal 4 the time of day.
    assertEquals(
        "Bisher gefunden:\nWetter 0,25 -> 7\nTageszeit 0,10 -> 5",
        KiRoomLevel.boardText(terminal -> terminal == 1 || terminal == 3, false));
  }

  @Test
  void ruleStandsAboveTheListOnceQuinlanWroteIt() {
    String board = KiRoomLevel.boardText(terminal -> terminal == 0, true);
    assertTrue(board.startsWith("Auf dem Whiteboard steht: Wähle immer zuerst das Attribut"));
    assertTrue(board.endsWith("\n\nBisher gefunden:\nWind 0,05 -> 9"));
  }
}
