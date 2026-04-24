package contrib.hud.itemgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link BaseItemGridOverlay}. */
class BaseItemGridOverlayTest {

  @Test
  void renderUsesSharedDialogLifecycleOrder() {
    TestOverlay overlay = new TestOverlay();
    overlay.x(12);
    overlay.y(24);

    Graphics2D graphics = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB).createGraphics();
    try {
      overlay.render(graphics);
    } finally {
      graphics.dispose();
    }

    assertEquals(List.of("measure", "render:86", "input:content", "feedback:content"), overlay.calls);
    assertEquals(320, overlay.width());
    assertEquals(180, overlay.height());
  }

  private static final class TestOverlay extends BaseItemGridOverlay<TestMeasurement, String> {

    private final List<String> calls = new ArrayList<>();

    private TestOverlay() {
      super(100, 50);
    }

    @Override
    protected TestMeasurement measureDialog() {
      calls.add("measure");
      return new TestMeasurement(320, 180);
    }

    @Override
    protected int dialogWidth(TestMeasurement measurement) {
      return measurement.width();
    }

    @Override
    protected int dialogHeight(TestMeasurement measurement) {
      return measurement.height();
    }

    @Override
    protected String dialogTitle() {
      return "Test";
    }

    @Override
    protected String renderContent(Graphics2D g, int contentY, TestMeasurement measurement) {
      calls.add("render:" + contentY);
      return "content";
    }

    @Override
    protected void handleInput(String content) {
      calls.add("input:" + content);
    }

    @Override
    protected void drawPointerFeedback(Graphics2D g, String content) {
      calls.add("feedback:" + content);
    }
  }

  private record TestMeasurement(int width, int height) {}
}
