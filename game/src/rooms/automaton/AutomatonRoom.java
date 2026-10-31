package rooms.automaton;

import com.badlogic.gdx.graphics.Color;
import java.time.Duration;
import rooms.escape.EscapeRoom;

/**
 * An escape room about regular languages, played in audience mode. The audience steers the hero
 * with single steps.
 *
 * <p>The hero is trapped inside a finite automaton. Four terminals ask about regular languages and
 * grammars and reveal one digit of the door code each. Start it with the Gradle task {@code
 * runAutomatonRoom}.
 */
public final class AutomatonRoom {
  /**
   * Tracking room ID; the surveys are {@code surveys/pre-automaton.json} and {@code
   * post-automaton.json}.
   */
  public static final String ROOM_ID = "automaton";

  private AutomatonRoom() {}

  /**
   * Starts the room.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server
   */
  public static void main(String[] args) {
    new EscapeRoom(
            ROOM_ID,
            "Gefangen im Automaten",
            AutomatonRoom.class,
            Color.valueOf("3cc8b4ff"),
            AutomatonRoomLevel.class,
            AutomatonRoomLevel::intro,
            // Five seconds per step leave the audience time to agree on a direction.
            Duration.ofSeconds(5))
        .start(args);
  }
}
