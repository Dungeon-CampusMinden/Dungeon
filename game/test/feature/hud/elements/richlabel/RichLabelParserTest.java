package feature.hud.elements.richlabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.graphics.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks the optional tint of inline images. */
class RichLabelParserTest {

  private static ImageRun image(String markup) {
    List<Run> runs = new RichLabelParser().parse(markup, false);
    return runs.stream()
        .filter(ImageRun.class::isInstance)
        .map(ImageRun.class::cast)
        .findFirst()
        .orElseThrow();
  }

  /** A named color tints the image. */
  @Test
  void namedColorTintsImage() {
    assertEquals(Color.BLACK, image("[img path=hud/input/keyboard_mouse.png color=black]").tint());
  }

  /** A hex color tints the image. */
  @Test
  void hexColorTintsImage() {
    assertEquals(
        Color.valueOf("1f5fa8ff"),
        image("[img path=hud/input/keyboard_mouse.png color=#1f5fa8]").tint());
  }

  /** Without a color the image is drawn unchanged. */
  @Test
  void noColorKeepsImageUntinted() {
    assertNull(image("[img path=hud/input/keyboard_mouse.png]").tint());
  }
}
