package core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link InputLabel}. */
public class InputLabelTest {

  /** Ensures overlapping integer codes are labeled according to their explicit input type. */
  @Test
  public void labelUsesExplicitInputTypeForOverlappingCodes() {
    assertEquals("UNKNOWN", InputLabel.label(InputLabel.InputType.KEYBOARD, 0));
    assertEquals("LMB", InputLabel.label(InputLabel.InputType.MOUSE_BUTTON, 0));
  }

  /** Ensures typed input codes delegate to the matching input label source. */
  @Test
  public void inputCodeLabelDelegatesByType() {
    assertEquals("E", InputLabel.keyboard(Keys.E).label());
    assertEquals("RMB", InputLabel.mouseButton(MouseButtons.RIGHT).label());
  }

  /** Ensures callers must choose an input type explicitly. */
  @Test
  public void labelRejectsMissingInputType() {
    assertThrows(NullPointerException.class, () -> InputLabel.label(null, Keys.E));
  }
}
