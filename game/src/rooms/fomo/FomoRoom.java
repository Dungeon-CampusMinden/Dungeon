package rooms.fomo;

import com.badlogic.gdx.graphics.Color;
import java.time.Duration;
import rooms.escape.EscapeRoom;

/**
 * An escape room about models and modelling for the module FOMO, played in audience mode. The
 * audience picks where the hero goes from a list of targets that grows as the story goes on.
 *
 * <p>The hero is trapped inside a model of a laboratory. Three terminals ask about regular
 * languages and grammars and one about the steps of modelling; each reveals one digit of the door
 * code. Start it with the Gradle task {@code runFomoRoom}.
 */
public final class FomoRoom {
  /** Tracking room ID; the surveys are {@code surveys/pre-fomo.json} and {@code post-fomo.json}. */
  public static final String ROOM_ID = "fomo";

  private FomoRoom() {}

  /**
   * Starts the room.
   *
   * @param args command-line arguments; {@code --server} starts the dedicated server
   */
  public static void main(String[] args) {
    new EscapeRoom(
            ROOM_ID,
            "Gefangen im Modell",
            FomoRoom.class,
            Color.valueOf("9b7bf0ff"),
            FomoRoomLevel.class,
            FomoRoomLevel::intro,
            // The audience picks a target from a list, which takes longer to read than an arrow;
            // once everybody voted, the window closes three seconds later.
            Duration.ofSeconds(15))
        .start(args);
  }
}
