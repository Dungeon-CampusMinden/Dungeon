package core.platform.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/** Tests for the {@link ClientShutdownExceptionFilter}. */
class ClientShutdownExceptionFilterTest {
  private static final String SHUTDOWN_RACE_MESSAGE =
      "Cannot invoke \"de.gurkenlabs.litiengine.GameWindow.cursor()\" because the return value of"
          + " \"de.gurkenlabs.litiengine.Game.window()\" is null";

  /** Recognizes the known render race from the main update loop. */
  @Test
  void detectsBenignShutdownRenderRace() {
    Throwable throwable = shutdownRaceException();

    assertTrue(
        ClientShutdownExceptionFilter.isBenignShutdownRenderRace(
            mainUpdateLoopThread(), throwable));
  }

  /** Rejects similar exceptions that do not originate from cursor rendering. */
  @Test
  void rejectsNonRenderRaceException() {
    NullPointerException throwable = new NullPointerException(SHUTDOWN_RACE_MESSAGE);
    throwable.setStackTrace(
        new StackTraceElement[] {
          new StackTraceElement("starter.BasicStarter", "onFrame", "BasicStarter.java", 400)
        });

    assertFalse(
        ClientShutdownExceptionFilter.isBenignShutdownRenderRace(
            mainUpdateLoopThread(), throwable));
  }

  /** Does not delegate the known shutdown race to the crash-reporting handler. */
  @Test
  void ignoresKnownShutdownRace() {
    AtomicBoolean delegated = new AtomicBoolean(false);
    ClientShutdownExceptionFilter filter =
        new ClientShutdownExceptionFilter((thread, throwable) -> delegated.set(true));

    filter.uncaughtException(mainUpdateLoopThread(), shutdownRaceException());

    assertFalse(delegated.get());
  }

  /** Delegates all normal exceptions to the previous handler. */
  @Test
  void delegatesUnexpectedException() {
    AtomicBoolean delegated = new AtomicBoolean(false);
    ClientShutdownExceptionFilter filter =
        new ClientShutdownExceptionFilter((thread, throwable) -> delegated.set(true));

    filter.uncaughtException(mainUpdateLoopThread(), new RuntimeException("real failure"));

    assertTrue(delegated.get());
  }

  private static Thread mainUpdateLoopThread() {
    return new Thread("Main Update Loop");
  }

  private static Throwable shutdownRaceException() {
    NullPointerException throwable = new NullPointerException(SHUTDOWN_RACE_MESSAGE);
    throwable.setStackTrace(
        new StackTraceElement[] {
          new StackTraceElement(
              "de.gurkenlabs.litiengine.graphics.RenderComponent",
              "renderGraphics",
              "RenderComponent.java",
              173),
          new StackTraceElement(
              "de.gurkenlabs.litiengine.graphics.RenderComponent",
              "render",
              "RenderComponent.java",
              148)
        });
    return throwable;
  }
}
