package engine.input;

import static engine.input.Binding.CTRL;
import static engine.input.Binding.key;
import static engine.input.Binding.mouse;
import static engine.input.Binding.pad;
import static engine.input.Binding.wheel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import engine.utils.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests routing, ownership, cancelling and combos of {@link InputResolver}. */
class InputResolverTest {
  private static final ActionId ATTACK =
      new ActionId("test", "attack", key(Keys.Q), mouse(Buttons.LEFT));
  private static final ActionId OTHER = new ActionId("test", "other", key(Keys.TAB), key(Keys.I));
  private static final ActionId CLOSE = new ActionId("test", "close", key(Keys.ESCAPE));
  private static final ActionId PAUSE = new ActionId("test", "pause", key(Keys.ESCAPE));
  private static final ActionId HUD = new ActionId("test", "hud", key(Keys.F3));
  private static final ActionId LIST = new ActionId("test", "list", key(Keys.F3, Keys.A));
  private static final ActionId LEFT = new ActionId("test", "left", key(Keys.A));
  private static final ActionId COPY = new ActionId("test", "copy", key(CTRL, Keys.C));
  private static final ActionId DOORS = new ActionId("test", "doors", key(Keys.C));
  private static final ActionId STICK = new ActionId("test", "stick", pad(PadAxisId.LSTICK));
  private static final ActionId ZOOM = new ActionId("test", "zoom", wheel());

  private static final ContextSpec GAME =
      ContextSpec.of("game", ContextLayer.BASE, ATTACK, OTHER, PAUSE, LEFT, DOORS, STICK, ZOOM);
  private static final ContextSpec DEBUG =
      ContextSpec.of("debug", ContextLayer.DEBUG, HUD, LIST, COPY);
  private static final ContextSpec DIALOG =
      ContextSpec.of("dialog", ContextLayer.UI, CLOSE).blocking();

  private final Set<Integer> stageHandledKeys = new HashSet<>();
  private final List<String> stageEvents = new ArrayList<>();
  private InputResolver resolver;
  private InputContext game;

  @BeforeEach
  void setUp() {
    resolver = new InputResolver(new BindingTable());
    resolver.stage(
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            stageEvents.add("down " + keycode);
            return stageHandledKeys.contains(keycode);
          }

          @Override
          public boolean keyUp(int keycode) {
            stageEvents.add("up " + keycode);
            return false;
          }
        });
    game = new InputContext(GAME);
    resolver.activate(game);
    resolver.activate(new InputContext(DEBUG));
  }

  private void tap(int key) {
    resolver.keyDown(key);
    resolver.keyUp(key);
  }

  @Test
  void pressGoesToTopmostContextThatTakesIt() {
    List<String> fired = new ArrayList<>();
    game.onPress(PAUSE, () -> fired.add("pause"));
    InputContext dialog = new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close"));
    resolver.activate(dialog);

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);

    assertEquals(List.of("close"), fired);
  }

  @Test
  void eventIsNeverRoutedTwiceAfterContextChange() {
    List<String> fired = new ArrayList<>();
    game.onPress(PAUSE, () -> fired.add("pause"));
    InputContext dialog = new InputContext(DIALOG);
    dialog.onPress(
        CLOSE,
        () -> {
          fired.add("close");
          resolver.deactivate(dialog);
        });
    resolver.activate(dialog);

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.keyUp(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.advance(0);

    assertEquals(List.of("close"), fired);
  }

  @Test
  void pressThatOpensBlockingContextDoesNotReachIt() {
    List<String> fired = new ArrayList<>();
    InputContext dialog = new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close"));
    game.onPress(PAUSE, () -> resolver.activate(dialog));

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.advance(0);

    assertEquals(List.of(), fired);
  }

  @Test
  void blockingContextHidesLowerActions() {
    resolver.activate(new InputContext(DIALOG));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    assertFalse(resolver.pressed(ATTACK));
    assertEquals(List.of("down " + Input.Keys.Q), stageEvents);
  }

  @Test
  void textContextKeepsEscapeFromDialogAndOthersBelow() {
    List<String> fired = new ArrayList<>();
    resolver.activate(new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close")));
    resolver.activate(new InputContext(CoreActions.TEXT));

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);

    assertEquals(List.of(), fired);
    assertEquals(List.of("down " + Input.Keys.ESCAPE), stageEvents);
  }

  @Test
  void stageHandledEventCreatesNoAction() {
    stageHandledKeys.add(Input.Keys.Q);

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    assertFalse(resolver.justPressed(ATTACK));
    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void releaseAlwaysReachesStageAndOwner() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);

    assertTrue(stageEvents.contains("up " + Input.Keys.Q));
    assertTrue(resolver.justReleased(ATTACK));
    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void quickTapReachesFrameAsPressThenRelease() {
    tap(Input.Keys.Q);

    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));
    assertTrue(resolver.pressed(ATTACK));
    assertFalse(resolver.justReleased(ATTACK));

    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));
    assertTrue(resolver.justReleased(ATTACK));
  }

  @Test
  void severalPressesInOneFrameStaySeparate() {
    tap(Input.Keys.Q);
    tap(Input.Keys.Q);

    int presses = 0;
    for (int frame = 0; frame < 6; frame++) {
      resolver.advance(0);
      if (resolver.justPressed(ATTACK)) presses++;
    }

    assertEquals(2, presses);
  }

  @Test
  void bindingsOfOneActionAggregate() {
    resolver.keyDown(Input.Keys.TAB);
    resolver.advance(0);
    assertTrue(resolver.justPressed(OTHER));

    resolver.keyDown(Input.Keys.I);
    resolver.advance(0);
    assertFalse(resolver.justPressed(OTHER));

    resolver.keyUp(Input.Keys.TAB);
    resolver.advance(0);
    assertFalse(resolver.justReleased(OTHER));
    assertTrue(resolver.pressed(OTHER));

    resolver.keyUp(Input.Keys.I);
    resolver.advance(0);
    assertTrue(resolver.justReleased(OTHER));
  }

  @Test
  void bindingAndVirtualSourceAggregate() {
    resolver.keyDown(Input.Keys.Q);
    resolver.press(ATTACK, "touch", 3);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));

    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));

    resolver.release("touch", 3);
    resolver.advance(0);
    assertTrue(resolver.justReleased(ATTACK));
  }

  @Test
  void ownershipAndCancelAreTrackedPerPointer() {
    resolver.press(ATTACK, "touch", 1);
    resolver.advance(0);
    resolver.activate(new InputContext(DIALOG));
    resolver.press(OTHER, "touch", 2);
    resolver.advance(0);

    assertFalse(resolver.pressed(ATTACK), "pointer 1 is cancelled by the dialog");
    assertFalse(
        resolver.pressed(OTHER), "pointer 2 belongs to the dialog, which has no such action");

    resolver.release("touch", 1);
    resolver.advance(0);
    assertFalse(resolver.justReleased(ATTACK), "a cancelled pointer fires no release");
  }

  @Test
  void blockingContextCancelsWithoutReleaseAndStaysLocked() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));

    InputContext dialog = new InputContext(DIALOG);
    resolver.activate(dialog);
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.deactivate(dialog);
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK), "closing the dialog must not re-arm the held key");

    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);
    assertFalse(resolver.justReleased(ATTACK));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK), "a real new press works again");
  }

  @Test
  void handlerThatOpensBlockingContextStopsTheOtherHandlersOfThatFrame() {
    List<String> fired = new ArrayList<>();
    InputContext dialog = new InputContext(DIALOG);
    Runnable open =
        () -> {
          fired.add("press");
          resolver.activate(dialog);
        };
    game.onPress(PAUSE, open).onPress(ATTACK, open);

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    assertEquals(List.of("press"), fired);
    assertFalse(resolver.justPressed(ATTACK) || resolver.justPressed(PAUSE));
  }

  @Test
  void orderChangesOnlyTheGivenContexts() {
    InputContext lower = new InputContext(DIALOG);
    InputContext upper = new InputContext(DIALOG);
    resolver.activate(lower);
    resolver.activate(upper);

    resolver.order(List.of(upper, lower));

    List<InputContext> stack = resolver.contexts();
    assertEquals(game, stack.get(0));
    assertEquals(upper, stack.get(1));
    assertEquals(lower, stack.get(2));
  }

  @Test
  void touchesOfDifferentPointersAreSeparatePresses() {
    resolver.touchDown(10, 10, 0, Buttons.LEFT);
    resolver.touchDown(20, 20, 1, Buttons.LEFT);
    resolver.advance(0);
    resolver.touchUp(10, 10, 0, Buttons.LEFT);
    resolver.advance(0);

    assertTrue(resolver.pressed(ATTACK));
  }

  @Test
  void resetBindingCancelsHeldInput() {
    resolver.rebind(ATTACK, List.of(key(Keys.F)));
    resolver.keyDown(Keys.F);
    resolver.advance(0);

    resolver.resetBinding(ATTACK);
    resolver.advance(0);

    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void focusLossDropsCollectedWheelTicks() {
    resolver.scrolled(0, 2);

    resolver.cancelAll();
    resolver.advance(0);

    assertEquals(0f, resolver.axis(ZOOM));
  }

  @Test
  void focusLossDropsBufferedTap() {
    tap(Input.Keys.Q);

    resolver.cancelAll();
    resolver.advance(0);

    assertFalse(resolver.justPressed(ATTACK));
    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void blockingContextDropsBufferedTapOfOwnerBelow() {
    tap(Input.Keys.Q);

    resolver.activate(new InputContext(DIALOG));
    resolver.advance(0);

    assertFalse(resolver.justPressed(ATTACK));
  }

  @Test
  void padReachesUiBelowTextContext() {
    ActionId click = new ActionId("test", "click", pad(Pad.A));
    resolver.activate(new InputContext(ContextSpec.of("ui", ContextLayer.UI, click).blocking()));
    resolver.activate(new InputContext(CoreActions.TEXT));

    resolver.padDown(pad(Pad.A));
    resolver.advance(0);

    assertTrue(resolver.pressed(click));
  }

  @Test
  void focusLossCancelsAndKeyDownAfterMissedReleaseStartsNewPress() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    resolver.cancelAll();
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));
  }

  @Test
  void removingOwnerCancelsOnlyItsPresses() {
    InputContext dialog = new InputContext(DIALOG);
    resolver.activate(dialog);
    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    assertTrue(resolver.pressed(CLOSE));

    resolver.deactivate(dialog);
    resolver.advance(0);

    assertFalse(resolver.pressed(CLOSE));
    assertFalse(resolver.justReleased(CLOSE));
  }

  @Test
  void comboWinsOverPlainKey() {
    resolver.keyDown(Input.Keys.F3);
    resolver.keyDown(Input.Keys.A);
    resolver.advance(0);

    assertTrue(resolver.justPressed(LIST));
    assertFalse(resolver.pressed(LEFT));
  }

  @Test
  void latePrefixStartsNoCombo() {
    resolver.keyDown(Input.Keys.C);
    resolver.keyDown(Input.Keys.CONTROL_LEFT);
    resolver.advance(0);

    assertTrue(resolver.justPressed(DOORS));
    assertFalse(resolver.justPressed(COPY));
  }

  @Test
  void comboModifierWorksOnEitherSide() {
    resolver.keyDown(Input.Keys.CONTROL_RIGHT);
    resolver.keyDown(Input.Keys.C);
    resolver.advance(0);

    assertTrue(resolver.justPressed(COPY));
    assertFalse(resolver.justPressed(DOORS));
  }

  @Test
  void keyUsedByComboDoesNotBecomeOwnActionAfterPrefixRelease() {
    resolver.keyDown(Input.Keys.F3);
    resolver.keyDown(Input.Keys.A);
    resolver.keyUp(Input.Keys.F3);
    resolver.advance(0);
    resolver.advance(0);
    assertFalse(resolver.pressed(LEFT));

    resolver.keyUp(Input.Keys.A);
    resolver.keyDown(Input.Keys.A);
    resolver.advance(0);
    resolver.advance(0);
    assertTrue(resolver.pressed(LEFT), "only the next press is the plain key");
  }

  @Test
  void keyThatIsActionAndPrefixFiresOnRelease() {
    resolver.keyDown(Input.Keys.F3);
    resolver.advance(0);
    assertFalse(resolver.justPressed(HUD));

    resolver.keyUp(Input.Keys.F3);
    resolver.advance(0);
    assertTrue(resolver.justPressed(HUD));
  }

  @Test
  void prefixKeyStaysSilentAfterCombo() {
    resolver.keyDown(Input.Keys.F3);
    tap(Input.Keys.A);
    resolver.keyUp(Input.Keys.F3);

    boolean fired = false;
    for (int frame = 0; frame < 4; frame++) {
      resolver.advance(0);
      fired |= resolver.justPressed(HUD);
    }

    assertFalse(fired);
  }

  @Test
  void cancelledPrefixKeyStaysSilent() {
    resolver.keyDown(Input.Keys.F3);
    resolver.cancelAll();
    resolver.keyUp(Input.Keys.F3);

    resolver.advance(0);

    assertFalse(resolver.justPressed(HUD));
  }

  @Test
  void stickKeepsAnalogValueAndIsLimitedToLengthOne() {
    resolver.padAxis(pad(PadAxisId.LSTICK), 0.3f, 0.4f);
    Vector2 half = resolver.vector(STICK);
    assertEquals(0.5, half.length(), 1e-4);

    resolver.padAxis(pad(PadAxisId.LSTICK), 3f, 4f);
    assertEquals(1.0, resolver.vector(STICK).length(), 1e-4);
  }

  @Test
  void cancelledStickStaysLockedUntilNeutral() {
    Binding stick = pad(PadAxisId.LSTICK);
    resolver.padAxis(stick, 1f, 0f);
    InputContext dialog = new InputContext(DIALOG);
    resolver.activate(dialog);
    resolver.deactivate(dialog);

    resolver.padAxis(stick, 1f, 0f);
    assertTrue(resolver.vector(STICK).isZero());

    resolver.padAxis(stick, 0f, 0f);
    resolver.padAxis(stick, 1f, 0f);
    assertEquals(1.0, resolver.vector(STICK).length(), 1e-4);
  }

  @Test
  void wheelIsCollectedPerFrame() {
    resolver.scrolled(0, 1);
    resolver.scrolled(0, 2);

    resolver.advance(0);
    assertEquals(3f, resolver.axis(ZOOM));

    resolver.advance(0);
    assertEquals(0f, resolver.axis(ZOOM));
  }

  @Test
  void rebindingCancelsHeldInputAndNewKeyWorks() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    resolver.rebind(ATTACK, List.of(key(Keys.F)));
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.keyDown(Input.Keys.F);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));
  }

  @Test
  void repeatingActionReportsNewPressesWhileHeld() {
    resolver.repeating(ATTACK);
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));

    resolver.advance(0.1f);
    assertFalse(resolver.justPressed(ATTACK));

    resolver.advance(0.4f);
    assertTrue(resolver.justPressed(ATTACK));
  }
}
