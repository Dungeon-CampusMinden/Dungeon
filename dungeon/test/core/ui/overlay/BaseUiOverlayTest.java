package core.ui.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.ui.StageHandle;
import java.awt.Graphics2D;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for {@link BaseUiOverlay}. */
class BaseUiOverlayTest {

  /** Width-only overlays remain auto-centered across size changes. */
  @Test
  void autoCenteredOverlayRecentersAfterSizeChange() {
    TestOverlay overlay = new TestOverlay(100, 50);

    overlay.centerIfUnpositioned(1000, 800);
    assertEquals(450, overlay.x());
    assertEquals(375, overlay.y());

    overlay.width(200);
    overlay.height(100);
    overlay.centerIfUnpositioned(1000, 800);

    assertEquals(400, overlay.x());
    assertEquals(350, overlay.y());
  }

  /** An explicit constructor position of (0, 0) is treated as a real position. */
  @Test
  void explicitZeroConstructorPositionIsNotAutoCentered() {
    TestOverlay overlay = new TestOverlay(0, 0, 100, 50);

    overlay.centerIfUnpositioned(1000, 800);

    assertEquals(0, overlay.x());
    assertEquals(0, overlay.y());
  }

  /** Explicit position setters disable auto-centering even when setting (0, 0). */
  @Test
  void explicitZeroSetterPositionIsNotAutoCentered() {
    TestOverlay overlay = new TestOverlay(100, 50);

    overlay.x(0);
    overlay.y(0);
    overlay.centerIfUnpositioned(1000, 800);

    assertEquals(0, overlay.x());
    assertEquals(0, overlay.y());
  }

  /** Centering via the overlay handle does not disable later auto-centering. */
  @Test
  void handleCenteringKeepsAutoCenteringActive() {
    TestOverlay overlay = new TestOverlay(100, 50);
    OverlayHandle handle = new OverlayHandle(overlay);

    handle.centerOn(new StubStageHandle(1000, 800));
    assertEquals(450, overlay.x());
    assertEquals(375, overlay.y());

    overlay.width(200);
    overlay.height(100);
    overlay.centerIfUnpositioned(1000, 800);

    assertEquals(400, overlay.x());
    assertEquals(350, overlay.y());
  }

  private static final class TestOverlay extends BaseUiOverlay {

    private TestOverlay(int width, int height) {
      super(width, height);
    }

    private TestOverlay(int x, int y, int width, int height) {
      super(x, y, width, height);
    }

    @Override
    public void render(Graphics2D g) {}

    private void centerIfUnpositioned(int outerWidth, int outerHeight) {
      centerInIfUnpositioned(outerWidth, outerHeight);
    }
  }

  private record StubStageHandle(int width, int height) implements StageHandle {

    @Override
    public Object raw() {
      return this;
    }

    @Override
    public <T> Optional<T> unwrap(Class<T> type) {
      return Optional.empty();
    }

    @Override
    public float getWidth() {
      return width;
    }

    @Override
    public float getHeight() {
      return height;
    }

    @Override
    public void addActor(Object actor) {}

    @Override
    public void setKeyboardFocus(Object actor) {}

    @Override
    public int mouseX() {
      return 0;
    }

    @Override
    public int mouseY() {
      return 0;
    }
  }
}
