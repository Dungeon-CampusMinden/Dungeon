package engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import engine.game.WindowEventManager;
import engine.utils.logging.DungeonLogger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Entry point of the input system.
 *
 * <p>{@link #install()} puts the resolver in front of the UI before the first screen appears. It
 * then runs for the whole process; screens only change the UI it forwards to. Game code reads
 * actions here and registers {@link InputContext}s.
 */
public final class GameInput {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameInput.class);
  private static final Path BINDING_FILE = Path.of("input_bindings.json");

  private static BindingTable bindings;
  private static InputResolver resolver;
  private static InputContext textContext;
  private static Stage stage;
  private static boolean windowFocused;

  static {
    reset();
  }

  private GameInput() {}

  /** Starts over with default bindings and an empty stack, for a new process or a test. */
  public static void reset() {
    bindings = new BindingTable();
    CoreActions.define(bindings);
    UiActions.define(bindings);
    resolver = new InputResolver(bindings);
    resolver.repeating(UiActions.NAV_UP);
    resolver.repeating(UiActions.NAV_DOWN);
    resolver.push(new InputContext(CoreActions.SYSTEM));
    textContext = new InputContext(CoreActions.TEXT);
    stage = null;
    windowFocused = true;
    PadInput.reset();
  }

  /** Connects the input system to the window. Call once, before the first screen appears. */
  public static void install() {
    bindings.load(BINDING_FILE);
    resolver.pointer(0, Gdx.input.getX(), Gdx.input.getY());
    Gdx.input.setInputProcessor(new WindowInput());
    WindowEventManager.registerFocusChangeListener(GameInput::windowFocused);
  }

  /** Logs colliding bindings of everything registered so far, once a game has been set up. */
  public static void logConflicts() {
    bindings
        .conflicts()
        .forEach(
            conflict ->
                LOGGER.warn(
                    "Input conflict in {}: {} drives {} and {}",
                    conflict.context(),
                    conflict.binding().format(),
                    conflict.first(),
                    conflict.second()));
  }

  /** Cancels held input because the application pauses. */
  public static void applicationPaused() {
    resolver.cancelAll();
    if (stage != null) stage.cancelTouchFocus();
  }

  private static void windowFocused(boolean focused) {
    windowFocused = focused;
    if (!focused) applicationPaused();
  }

  /**
   * Sets the UI of the current screen.
   *
   * @param newStage the stage that receives events
   */
  public static void stage(Stage newStage) {
    stage = newStage;
    resolver.stage(newStage);
  }

  /**
   * Starts the next frame. Runs once per rendered frame, on every screen.
   *
   * @param delta seconds since the last frame
   */
  public static void update(float delta) {
    syncTextContext();
    if (windowFocused) PadInput.poll(resolver, delta);
    resolver.advance(delta);
    PadCursor.update(resolver, stage);
  }

  private static void syncTextContext() {
    boolean textFocused = stage != null && stage.getKeyboardFocus() instanceof TextField;
    if (textFocused) resolver.push(textContext);
    else resolver.remove(textContext);
  }

  // Clears the text focus when a press lands outside the focused field.
  static void clearTextFocusOutside(int screenX, int screenY) {
    if (stage == null || !(stage.getKeyboardFocus() instanceof TextField field)) return;
    Vector2 position = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
    Actor hit = stage.hit(position.x, position.y, true);
    if (hit == null || !hit.isDescendantOf(field)) stage.setKeyboardFocus(null);
  }

  /**
   * Draws what the input system shows itself, after the UI.
   *
   * @param targetStage the stage of the current screen
   */
  public static void draw(Stage targetStage) {
    PadCursor.draw(targetStage.getBatch(), targetStage);
  }

  /**
   * Gets the resolver, for code that drives or inspects it directly.
   *
   * @return the resolver of this process
   */
  public static InputResolver resolver() {
    return resolver;
  }

  /**
   * Gets the bindings of this process.
   *
   * @return the binding table
   */
  public static BindingTable bindings() {
    return bindings;
  }

  /**
   * Adds a context.
   *
   * @param context the context
   * @see InputResolver#push(InputContext)
   */
  public static void push(InputContext context) {
    resolver.push(context);
  }

  /**
   * Removes a context.
   *
   * @param context the context
   * @see InputResolver#remove(InputContext)
   */
  public static void remove(InputContext context) {
    resolver.remove(context);
  }

  /**
   * Removes everything a game session left on the stack, for the way back to the menu.
   *
   * @see ContextLayer#SYSTEM
   */
  public static void clearSessionContexts() {
    for (InputContext context : resolver.contexts()) {
      if (context.spec().layer() != ContextLayer.SYSTEM) resolver.remove(context);
    }
  }

  /**
   * Checks whether an action is held.
   *
   * @param action the action
   * @return true while it is held
   */
  public static boolean pressed(ActionId action) {
    return resolver.pressed(action);
  }

  /**
   * Checks whether a press of an action reached this frame.
   *
   * @param action the action
   * @return true in the frame of the press
   */
  public static boolean justPressed(ActionId action) {
    return resolver.justPressed(action);
  }

  /**
   * Checks whether a release of an action reached this frame.
   *
   * @param action the action
   * @return true in the frame of the release
   */
  public static boolean released(ActionId action) {
    return resolver.released(action);
  }

  /**
   * Reads a one-dimensional axis action.
   *
   * @param action the action
   * @return the movement collected for this frame
   */
  public static float axis(ActionId action) {
    return resolver.axis(action);
  }

  /**
   * Reads a vector action.
   *
   * @param action the action
   * @return the current vector, of length at most 1
   */
  public static engine.utils.Vector2 vector(ActionId action) {
    return resolver.vector(action);
  }

  /**
   * Gets the primary pointer: the mouse or the pad cursor.
   *
   * @return its position in screen coordinates
   */
  public static engine.utils.Vector2 pointer() {
    return resolver.pointer(0);
  }

  /**
   * Checks whether Control is held on either side, for widgets with modifier clicks.
   *
   * @return true if it is held; on macOS, Command counts as Control
   */
  public static boolean ctrl() {
    return resolver.modifierHeld(Input.Keys.CONTROL_LEFT);
  }

  /**
   * Checks whether Shift is held on either side, for widgets with modifier clicks.
   *
   * @return true if it is held
   */
  public static boolean shift() {
    return resolver.modifierHeld(Input.Keys.SHIFT_LEFT);
  }

  /**
   * Checks whether the primary pointer is down, whatever the context stack does with it.
   *
   * @return true while a mouse button or the pad's click is held
   */
  public static boolean pointerDown() {
    return resolver.anyButtonHeld() || pressed(UiActions.CLICK);
  }

  /**
   * Checks whether a blocking context lies above a context.
   *
   * @param context the context
   * @return true if input cannot reach the context
   */
  public static boolean covered(InputContext context) {
    return resolver.covered(context);
  }

  /**
   * Describes the input state for the debug HUD.
   *
   * @return the context stack from top to bottom with its blocking flags, the owners of held input
   *     and the held actions
   */
  public static List<String> describe() {
    List<String> lines = new ArrayList<>();
    lines.add("Input contexts:");
    List<InputContext> contexts = new ArrayList<>(resolver.contexts());
    Collections.reverse(contexts);
    for (InputContext context : contexts) {
      lines.add(" " + context + (context.spec().blocksBelow() ? " (blocks)" : ""));
    }
    lines.add("Held input:");
    resolver.describePresses().forEach(line -> lines.add(" " + line));
    lines.add("Active actions:");
    resolver.heldActions().forEach(action -> lines.add(" " + action));
    return lines;
  }

  /**
   * Gets the device the player used last.
   *
   * @return the last device
   */
  public static InputDevice device() {
    return resolver.device();
  }

  /**
   * Gets the binding shown for an action on the device the player used last.
   *
   * @param action the action
   * @param index which binding of that device to show
   * @return that binding, or else the first binding of any device
   */
  public static Optional<Binding> displayBinding(ActionId action, int index) {
    List<Binding> list = bindings.bindings(action);
    boolean pad = device() == InputDevice.PAD;
    return list.stream()
        .filter(binding -> binding.isPad() == pad)
        .skip(index)
        .findFirst()
        .or(() -> list.stream().findFirst());
  }

  /**
   * Names the current binding of an action for the player.
   *
   * @param action the action
   * @return the label of its first binding on the active device, or a dash if it is unbound
   */
  public static String label(ActionId action) {
    return displayBinding(action, 0).map(KeyNames::label).orElse("-");
  }

  /** Forwards window events, keeping the text context up to date before each press is routed. */
  private static final class WindowInput implements InputProcessor {
    @Override
    public boolean keyDown(int keycode) {
      syncTextContext();
      boolean editing = resolver.contains(textContext);
      boolean handled = resolver.keyDown(keycode);
      // The widget keeps Escape; without own handling it cancels the input by losing the focus.
      if (editing && keycode == Input.Keys.ESCAPE && stage != null) {
        stage.setKeyboardFocus(null);
      }
      return handled;
    }

    @Override
    public boolean keyUp(int keycode) {
      return resolver.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
      return resolver.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      clearTextFocusOutside(screenX, screenY);
      syncTextContext();
      return resolver.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
      return resolver.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
      return resolver.touchCancelled(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      return resolver.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
      return resolver.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
      return resolver.scrolled(amountX, amountY);
    }
  }
}
