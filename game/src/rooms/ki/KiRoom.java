package rooms.ki;

import com.badlogic.gdx.graphics.Color;
import java.time.Duration;
import rooms.escape.EscapeRoom;

/**
 * An escape room about machine learning and decision trees, played in audience mode. The audience
 * picks where the hero goes from a list of targets that grows as the story goes on.
 *
 * <p>The hero is trapped inside a decision tree. Five terminals ask about machine learning, CAL2,
 * entropy, ID3 and random forests; each reveals one digit of the door code. Start it with the
 * Gradle task {@code runKiRoom}.
 */
public final class KiRoom {
  /** Tracking room ID; the surveys are {@code surveys/pre-ki.json} and {@code post-ki.json}. */
  public static final String ROOM_ID = "ki";

  private KiRoom() {}

  /**
   * Starts the room.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server
   */
  public static void main(String[] args) {
    new EscapeRoom(
            ROOM_ID,
            "Gefangen im Entscheidungsbaum",
            KiRoom.class,
            Color.valueOf("4cc38aff"),
            KiRoomLevel.class,
            KiRoomLevel::intro,
            // The audience picks a target from a list, which takes longer to read than an arrow;
            // once everybody voted, the window closes three seconds later.
            Duration.ofSeconds(15))
        .start(args);
  }
}
