package core.platform.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.camera.CameraViewportState;
import core.platform.client.adapters.ClientRenderAdapter;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Regression tests for world-to-stage projection in the LITIENGINE render adapter. */
class ClientRenderAdapterTest {

  @AfterEach
  void tearDown() {
    CameraViewportState.reset();
  }

  @Test
  void projectWorldToStageUsesSharedCameraView() {
    CameraViewportState.set(100, 50, 10, 32);

    Point projected =
        new ClientRenderAdapter()
            .projectWorldToStage(new Point(2.5f, 3f), new StubStageHandle())
            .orElseThrow();

    assertEquals(180f, projected.x());
    assertEquals(242f, projected.y());
  }

  private static final class StubStageHandle implements StageHandle {
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
      return 1600;
    }

    @Override
    public float getHeight() {
      return 900;
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
