package rooms.morphco.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.Test;

class MorphCoUITest {

  @Test
  void roomTagsBecomeColorMarkupInTheDefaultColor() {
    assertEquals(
        "[#000000ff]Nomen: [#1F5FA8]{Fisch}[]\n[BLACK]Quelle[]",
        MorphCoUI.markup(
            "Nomen: [color=#1f5fa8]{Fisch}[/color][n][color=black]Quelle[/color]", Color.BLACK));
  }

  @Test
  void otherBracketsStayVisible() {
    assertEquals("[#ffffffff][[NEUSTART]", MorphCoUI.markup("[NEUSTART]", Color.WHITE));
  }
}
