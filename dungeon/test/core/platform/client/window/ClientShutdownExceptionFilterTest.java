package core.platform.client.window;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for filtering known LITIENGINE shutdown render races. */
class ClientShutdownExceptionFilterTest {

  @Test
  void test_isBenignShutdownRenderRace_screensShutdownRace() {
    Thread thread = new Thread();
    thread.setName("Main Update Loop");
    NullPointerException exception =
        new NullPointerException(
            "Cannot invoke \"de.gurkenlabs.litiengine.gui.screens.ScreenManager.current()\" "
                + "because the return value of \"de.gurkenlabs.litiengine.Game.screens()\" is null");
    exception.setStackTrace(
        new StackTraceElement[] {
          new StackTraceElement(
              "de.gurkenlabs.litiengine.graphics.RenderComponent",
              "renderGraphics",
              "RenderComponent.java",
              168)
        });

    assertTrue(ClientShutdownExceptionFilter.isBenignShutdownRenderRace(thread, exception));
  }

  @Test
  void test_isBenignShutdownRenderRace_unrelatedNullPointerException() {
    Thread thread = new Thread();
    thread.setName("Main Update Loop");
    NullPointerException exception = new NullPointerException("unrelated");
    exception.setStackTrace(
        new StackTraceElement[] {
          new StackTraceElement(
              "de.gurkenlabs.litiengine.graphics.RenderComponent",
              "renderGraphics",
              "RenderComponent.java",
              168)
        });

    assertFalse(ClientShutdownExceptionFilter.isBenignShutdownRenderRace(thread, exception));
  }
}
