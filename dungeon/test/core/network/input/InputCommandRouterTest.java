package core.network.input;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.network.messages.c2s.InputMessage;
import core.network.server.ClientState;
import core.utils.Vector2;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link InputCommandRouter}. */
public class InputCommandRouterTest {

  @BeforeEach
  void setUp() {
    InputCommandRouter.resetForTesting();
  }

  @AfterEach
  void tearDown() {
    InputCommandRouter.resetForTesting();
  }

  /** Verifies duplicate registration with same source is replaced automatically. */
  @Test
  public void registerOverridesExistingRouteHandler() {
    AtomicInteger firstCount = new AtomicInteger();
    AtomicInteger secondCount = new AtomicInteger();

    boolean first =
        InputCommandRouter.register("test:command", false, context -> firstCount.incrementAndGet());
    boolean second =
        InputCommandRouter.register(
            "test:command", false, context -> secondCount.incrementAndGet());

    boolean handled =
        InputCommandRouter.dispatch(
            clientState(), playerEntity(), InputMessage.custom("test:command"), false);

    assertTrue(first);
    assertTrue(second);
    assertTrue(handled);
    assertEquals(0, firstCount.get());
    assertEquals(1, secondCount.get());
    assertEquals(
        List.of(new InputCommandRouter.HandlerRegistration("test:command", false)),
        InputCommandRouter.registrations());
  }

  /** Verifies built-in handlers can be overridden by later registrations. */
  @Test
  public void builtInRouteCanBeOverridden() {
    AtomicInteger defaultCount = new AtomicInteger();
    AtomicInteger overrideCount = new AtomicInteger();
    String route = InputCommandRouter.routeKey(InputMessage.Action.MOVE);

    InputCommandRouter.register(route, false, context -> defaultCount.incrementAndGet());
    InputCommandRouter.register(route, false, context -> overrideCount.incrementAndGet());

    boolean handled =
        InputCommandRouter.dispatch(clientState(), playerEntity(), moveMessage(), false);

    assertTrue(handled);
    assertEquals(0, defaultCount.get());
    assertEquals(1, overrideCount.get());
  }

  /** Verifies explicit pause behavior per handler. */
  @Test
  public void pausedDispatchSkipsHandlerWhenIgnorePauseIsFalse() {
    AtomicInteger executionCount = new AtomicInteger();

    InputCommandRouter.register(
        "mod:pause_test", false, context -> executionCount.incrementAndGet());

    boolean handled =
        InputCommandRouter.dispatch(
            clientState(), playerEntity(), InputMessage.custom("mod:pause_test"), true);

    assertFalse(handled);
    assertEquals(0, executionCount.get());
  }

  /** Verifies pause-ignoring handlers still run while paused. */
  @Test
  public void pausedDispatchExecutesHandlerWhenIgnorePauseIsTrue() {
    AtomicInteger executionCount = new AtomicInteger();
    String route = "mod:pause_allowed";

    InputCommandRouter.register(route, true, context -> executionCount.incrementAndGet());

    boolean handled =
        InputCommandRouter.dispatch(
            clientState(), playerEntity(), InputMessage.custom(route), true);

    assertTrue(handled);
    assertEquals(1, executionCount.get());
  }

  private static Entity playerEntity() {
    return new Entity("test_player");
  }

  private static ClientState clientState() {
    return new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3});
  }

  private static InputMessage moveMessage() {
    return new InputMessage(
        1, 1, (short) 1, InputMessage.Action.MOVE, new InputMessage.Move(Vector2.of(1f, 0f)));
  }
}
