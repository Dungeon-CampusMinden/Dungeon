package engine.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import engine.input.BindingTable.Bound;
import engine.utils.Vector2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Turns physical input into actions.
 *
 * <p>Every press has one owner, fixed when it starts: the topmost context that takes the input,
 * either through a matching action or because it blocks. The event then goes to the UI ({@link
 * #stage(InputProcessor)}); if a widget handles it, no action starts. The event is never routed a
 * second time, so the Escape that closes a dialog does not open the pause menu afterwards. The
 * release goes to the same owner.
 *
 * <p>A press ends without firing a release when it is cancelled: by losing the window focus, by a
 * blocking context above its owner, by removing the owner, by rebinding its action or by unplugging
 * its device. A cancelled input stays locked until its real release or neutral axis.
 *
 * <p>Actions keep their held state and an ordered list of unconsumed edges. {@link #advance(float)}
 * moves one edge per action into the current frame, so several presses within one frame stay
 * separate presses.
 *
 * <p>Keyboard, mouse and pad feed the same press, release and axis calls as virtual sources.
 *
 * <p>This class needs no graphics; the UI is any {@link InputProcessor}.
 */
public final class InputResolver implements InputProcessor {
  private static final float REPEAT_DELAY = 0.4f;
  private static final float REPEAT_INTERVAL = 0.12f;
  private static final boolean MAC_OS = System.getProperty("os.name", "").startsWith("Mac");

  // Identifies a press: per key or button, for pointers per source and pointer id.
  private record SourceId(String source, int pointer) {}

  private record Route(InputContext owner, Bound bound) {
    private static final Route NONE = new Route(null, null);

    Optional<ActionId> action() {
      return bound == null ? Optional.empty() : Optional.of(bound.action());
    }
  }

  private static final class Press {
    private final SourceId source;
    private final InputContext owner;
    private final ActionId action;
    private boolean active;
    private boolean deferred;
    private boolean comboUsed;
    private boolean cancelled;
    private float x;
    private float y;

    private Press(SourceId source, InputContext owner, ActionId action) {
      this.source = source;
      this.owner = owner;
      this.action = action;
    }
  }

  private record Edge(boolean press, InputContext owner, boolean pad) {}

  private static final class ActionState {
    private final Set<Press> active = new LinkedHashSet<>();
    private final Deque<Edge> edges = new ArrayDeque<>();
    private boolean pressed;
    private boolean justPressed;
    private boolean released;
    private float pendingAxis;
    private InputContext pendingOwner;
    private float axis;
    private boolean repeats;
    private float repeatTimer;
  }

  private final BindingTable bindings;
  private final List<InputContext> stack = new ArrayList<>();
  private final Map<SourceId, Press> presses = new LinkedHashMap<>();
  private final Map<ActionId, ActionState> actions = new HashMap<>();
  private final Set<Integer> heldKeys = new HashSet<>();
  private final Set<Integer> heldButtons = new HashSet<>();
  private final Map<Integer, Vector2> pointers = new HashMap<>();
  private InputProcessor stage;
  private InputDevice device = InputDevice.KEYBOARD_MOUSE;

  /**
   * Creates a resolver.
   *
   * @param bindings the bindings to resolve against
   */
  public InputResolver(BindingTable bindings) {
    this.bindings = bindings;
  }

  /**
   * Sets the UI that sees events after the owner of a press is fixed.
   *
   * @param stage the UI, or null if there is none
   */
  public void stage(InputProcessor stage) {
    this.stage = stage;
  }

  // region Contexts

  /**
   * Adds a context at its place in the stack, unless it is active already. A blocking context
   * cancels the input owned by the contexts below it.
   *
   * @param context the context
   */
  public void activate(InputContext context) {
    if (stack.contains(context)) return;
    bindings.register(context.spec());
    int index = stack.size();
    while (index > 0 && layer(stack.get(index - 1)) > layer(context)) index--;
    stack.add(index, context);
    if (context.spec().blocksBelow()) cancelCovered();
  }

  // Input ends when a blocking context comes to lie above its owner, however that happened.
  private void cancelCovered() {
    cancelWhere(
        press ->
            press.owner != null
                && stack.contains(press.owner)
                && covered(press.owner, isPad(press)));
    dropEdges(edge -> stack.contains(edge.owner()) && covered(edge.owner(), edge.pad()));
  }

  private static boolean isPad(Press press) {
    return press.source.source().startsWith("pad:");
  }

  private static int layer(InputContext context) {
    return context.spec().layer().ordinal();
  }

  /**
   * Removes exactly this context and cancels the input it owns.
   *
   * @param context the context
   */
  public void deactivate(InputContext context) {
    if (!stack.remove(context)) return;
    cancelWhere(press -> press.owner == context);
    dropEdges(edge -> edge.owner() == context);
  }

  /**
   * Gets the context stack.
   *
   * @return the contexts from bottom to top
   */
  public List<InputContext> contexts() {
    return List.copyOf(stack);
  }

  /**
   * Puts active contexts into a new order among themselves, e.g. after their dialogs changed places
   * on screen. Other contexts keep their positions.
   *
   * @param ordered the contexts from bottom to top
   */
  public void order(List<InputContext> ordered) {
    List<InputContext> before = List.copyOf(stack);
    Iterator<InputContext> next = ordered.iterator();
    for (int i = 0; i < stack.size(); i++) {
      if (ordered.contains(stack.get(i))) stack.set(i, next.next());
    }
    if (!stack.equals(before)) cancelCovered();
  }

  /**
   * Checks whether a blocking context lies above a context.
   *
   * @param context the context
   * @return true if input cannot reach the context
   */
  public boolean covered(InputContext context) {
    return covered(context, false);
  }

  private boolean covered(InputContext context, boolean pad) {
    int index = stack.indexOf(context);
    return stack.subList(index + 1, stack.size()).stream()
        .anyMatch(above -> above.spec().blocks(pad));
  }

  /**
   * Checks whether a context is on the stack.
   *
   * @param context the context
   * @return true if it is on the stack
   */
  public boolean contains(InputContext context) {
    return stack.contains(context);
  }

  // endregion

  // region Reading

  /**
   * Checks whether an action is held in the current frame.
   *
   * @param action the action
   * @return true from the frame of its press up to the frame of its release
   */
  public boolean pressed(ActionId action) {
    ActionState state = actions.get(action);
    return state != null && state.pressed;
  }

  /**
   * Checks whether a press of an action reached the current frame.
   *
   * @param action the action
   * @return true in the frame of the press
   */
  public boolean justPressed(ActionId action) {
    ActionState state = actions.get(action);
    return state != null && state.justPressed;
  }

  /**
   * Checks whether a release of an action reached the current frame.
   *
   * @param action the action
   * @return true in the frame of the release
   */
  public boolean justReleased(ActionId action) {
    ActionState state = actions.get(action);
    return state != null && state.released;
  }

  /**
   * Reads a one-dimensional axis action.
   *
   * @param action the action
   * @return the movement collected for the current frame
   */
  public float axis(ActionId action) {
    ActionState state = actions.get(action);
    return state == null ? 0f : state.axis;
  }

  /**
   * Reads a vector action. Its sources add up; the length never exceeds 1.
   *
   * @param action the action
   * @return the current vector
   */
  public Vector2 vector(ActionId action) {
    ActionState state = actions.get(action);
    if (state == null) return Vector2.ZERO;
    float x = 0;
    float y = 0;
    for (Press press : state.active) {
      x += press.x;
      y += press.y;
    }
    return Vector2.of(x, y).clampLength(1);
  }

  /**
   * Checks a physical key, whatever the context stack does with it.
   *
   * @param keycode libGDX keycode
   * @return true if the key is down
   */
  public boolean keyHeld(int keycode) {
    return heldKeys.contains(keycode);
  }

  /**
   * Checks whether a modifier is held on either side.
   *
   * @param modifier {@link Input.Keys#CONTROL_LEFT}, {@link Input.Keys#SHIFT_LEFT}, {@link
   *     Input.Keys#ALT_LEFT} or any other key
   * @return true if the modifier is down; on macOS, Command counts as Control
   */
  public boolean modifierHeld(int modifier) {
    return switch (modifier) {
      case Input.Keys.CONTROL_LEFT ->
          keyHeld(Input.Keys.CONTROL_LEFT)
              || keyHeld(Input.Keys.CONTROL_RIGHT)
              || (MAC_OS && keyHeld(Input.Keys.SYM));
      case Input.Keys.SHIFT_LEFT ->
          keyHeld(Input.Keys.SHIFT_LEFT) || keyHeld(Input.Keys.SHIFT_RIGHT);
      case Input.Keys.ALT_LEFT -> keyHeld(Input.Keys.ALT_LEFT) || keyHeld(Input.Keys.ALT_RIGHT);
      default -> keyHeld(modifier);
    };
  }

  /**
   * Checks whether any mouse button is down.
   *
   * @return true if a button is down
   */
  public boolean anyButtonHeld() {
    return !heldButtons.isEmpty();
  }

  /**
   * Gets the position of a pointer in screen coordinates. Pointer 0 is the mouse and the pad
   * cursor.
   *
   * @param pointer pointer id
   * @return the position, zero for an unknown pointer
   */
  public Vector2 pointer(int pointer) {
    return pointers.getOrDefault(pointer, Vector2.ZERO);
  }

  /**
   * Moves a pointer.
   *
   * @param pointer pointer id
   * @param x screen x
   * @param y screen y
   */
  public void pointer(int pointer, float x, float y) {
    pointers.put(pointer, Vector2.of(x, y));
  }

  /**
   * Gets the device the player used last.
   *
   * @return the last device
   */
  public InputDevice device() {
    return device;
  }

  /**
   * Sets the device the player used last.
   *
   * @param device the device
   */
  public void device(InputDevice device) {
    this.device = device;
  }

  /**
   * Gets a readable line per held input for the debug HUD.
   *
   * @return one line per press
   */
  public List<String> describePresses() {
    List<String> lines = new ArrayList<>();
    for (Press press : presses.values()) {
      lines.add(
          press.source.source()
              + " "
              + press.source.pointer()
              + " -> "
              + (press.owner == null ? "-" : press.owner.spec().name())
              + (press.action == null ? "" : " " + press.action)
              + (press.cancelled ? " (cancelled)" : ""));
    }
    return lines;
  }

  /**
   * Gets the actions held in the current frame for the debug HUD.
   *
   * @return the held actions
   */
  public List<ActionId> heldActions() {
    return actions.entrySet().stream()
        .filter(entry -> entry.getValue().pressed)
        .map(Map.Entry::getKey)
        .toList();
  }

  // endregion

  // region Frame

  /**
   * Marks an action as repeating: while held, it reports a new press after a delay and then at a
   * fixed interval.
   *
   * @param action the action
   */
  public void repeating(ActionId action) {
    state(action).repeats = true;
  }

  /**
   * Starts the next frame: moves one edge per action into the current frame and runs the handlers
   * of owners that are still on the stack.
   *
   * @param delta seconds since the last frame
   */
  public void advance(float delta) {
    Map<ActionId, Edge> fired = new LinkedHashMap<>();
    for (Map.Entry<ActionId, ActionState> entry : actions.entrySet()) {
      ActionState state = entry.getValue();
      state.justPressed = false;
      state.released = false;
      state.axis = state.pendingAxis;
      state.pendingAxis = 0;
      Edge edge = state.edges.poll();
      if (edge != null) {
        state.pressed = edge.press();
        state.justPressed = edge.press();
        state.released = !edge.press();
        state.repeatTimer = REPEAT_DELAY;
        if (edge.press()) fired.put(entry.getKey(), edge);
      } else if (state.repeats && state.pressed) {
        state.repeatTimer -= delta;
        if (state.repeatTimer <= 0 && !state.active.isEmpty()) {
          state.repeatTimer = REPEAT_INTERVAL;
          state.justPressed = true;
          Press held = state.active.iterator().next();
          fired.put(entry.getKey(), new Edge(true, held.owner, isPad(held)));
        }
      }
    }
    // A handler may open a dialog or close its context; the remaining handlers then lost their
    // turn.
    fired.forEach(
        (action, edge) -> {
          InputContext owner = edge.owner();
          if (stack.contains(owner) && !covered(owner, edge.pad())) owner.firePress(action);
        });
  }

  // endregion

  // region Virtual sources

  /**
   * Presses an action from a virtual source.
   *
   * @param action the action
   * @param source name of the source
   * @param pointer pointer id inside the source
   */
  public void press(ActionId action, String source, int pointer) {
    release(source, pointer);
    SourceId id = new SourceId(source, pointer);
    Press press = virtualPress(id, action);
    presses.put(id, press);
    startAction(press);
  }

  /**
   * Releases what a virtual source holds.
   *
   * @param source name of the source
   * @param pointer pointer id inside the source
   */
  public void release(String source, int pointer) {
    Press press = presses.remove(new SourceId(source, pointer));
    if (press != null) endAction(press);
  }

  /**
   * Sets a vector value from a virtual source. Leaving neutral starts a press, returning to neutral
   * ends it.
   *
   * @param action the action
   * @param source name of the source
   * @param pointer pointer id inside the source
   * @param x horizontal value
   * @param y vertical value
   */
  public void axis(ActionId action, String source, int pointer, float x, float y) {
    SourceId id = new SourceId(source, pointer);
    if (x == 0 && y == 0) {
      release(source, pointer);
      return;
    }
    if (!presses.containsKey(id)) press(action, source, pointer);
    Press press = presses.get(id);
    press.x = x;
    press.y = y;
  }

  private Press virtualPress(SourceId id, ActionId action) {
    for (int i = stack.size() - 1; i >= 0; i--) {
      InputContext context = stack.get(i);
      if (context.spec().actions().contains(action)) return new Press(id, context, action);
      if (context.spec().blocksBelow()) return new Press(id, context, null);
    }
    return new Press(id, null, null);
  }

  // endregion

  // region Pad

  /**
   * Starts a press of a pad button or a digital pad direction.
   *
   * @param input the pad input
   */
  public void padDown(Binding input) {
    SourceId id = padSource(input);
    Route route = resolve(input);
    Press press = new Press(id, route.owner(), route.action().orElse(null));
    presses.put(id, press);
    startAction(press);
  }

  /**
   * Ends a press of a pad button or a digital pad direction.
   *
   * @param input the pad input
   */
  public void padUp(Binding input) {
    Press press = presses.remove(padSource(input));
    if (press != null) endAction(press);
  }

  /**
   * Sets the value of a pad stick.
   *
   * @param input the stick
   * @param x horizontal value after the dead zone
   * @param y vertical value after the dead zone
   */
  public void padAxis(Binding input, float x, float y) {
    SourceId id = padSource(input);
    if (x == 0 && y == 0) {
      padUp(input);
      return;
    }
    if (!presses.containsKey(id)) padDown(input);
    Press press = presses.get(id);
    press.x = x;
    press.y = y;
  }

  /** Cancels everything a pad holds, e.g. because it was unplugged. */
  public void cancelPad() {
    cancelWhere(InputResolver::isPad);
    presses.values().removeIf(InputResolver::isPad);
  }

  private static SourceId padSource(Binding input) {
    return new SourceId(input.format(), 0);
  }

  // endregion

  // region Cancelling

  /**
   * Cancels all held input, e.g. because the window lost focus. Keys released meanwhile send no
   * release, so the physical state starts over; the cancelled presses stay until the next real edge
   * of their input.
   */
  public void cancelAll() {
    cancelWhere(press -> true);
    dropEdges(edge -> true);
    heldKeys.clear();
    heldButtons.clear();
  }

  /**
   * Changes the player's bindings of an action and cancels the input that drives it.
   *
   * @param action the action
   * @param newBindings the new bindings; empty means deliberately unbound
   * @return the conflict that prevented the change, or empty if the change was applied
   */
  public Optional<BindingTable.Conflict> rebind(ActionId action, List<Binding> newBindings) {
    Optional<BindingTable.Conflict> conflict = bindings.rebind(action, newBindings);
    if (conflict.isEmpty()) cancel(action);
    return conflict;
  }

  /**
   * Removes the player's override of an action and cancels the input that drives it.
   *
   * @param action the action
   */
  public void resetBinding(ActionId action) {
    bindings.reset(action);
    cancel(action);
  }

  private void cancel(ActionId action) {
    cancelWhere(press -> action.equals(press.action));
    ActionState state = state(action);
    state.edges.clear();
    state.pendingAxis = 0;
    if (state.active.isEmpty()) state.pressed = false;
  }

  private void cancelWhere(Predicate<Press> predicate) {
    for (Press press : presses.values()) {
      if (press.cancelled || !predicate.test(press)) continue;
      press.cancelled = true;
      if (!press.active) continue;
      press.active = false;
      ActionState state = state(press.action);
      state.active.remove(press);
      if (state.active.isEmpty()) {
        state.edges.clear();
        state.pressed = false;
        state.justPressed = false;
      }
    }
  }

  // A quick tap or a wheel tick has no press left to cancel, only what it buffered.
  private void dropEdges(Predicate<Edge> which) {
    for (ActionState state : actions.values()) {
      if (state.edges.removeIf(which) && state.active.isEmpty()) {
        state.pressed = false;
        state.justPressed = false;
      }
      if (state.pendingOwner != null && which.test(new Edge(true, state.pendingOwner, false))) {
        state.pendingAxis = 0;
      }
    }
  }

  // endregion

  // region Routing

  private ActionState state(ActionId action) {
    return actions.computeIfAbsent(action, id -> new ActionState());
  }

  private Route resolve(Binding trigger) {
    List<Bound> candidates = bindings.bound(trigger);
    for (int i = stack.size() - 1; i >= 0; i--) {
      InputContext context = stack.get(i);
      Bound best = null;
      for (Bound bound : candidates) {
        if (context.spec().accepts(bound.action(), bound.binding())
            && prefixesHeld(bound.binding())
            && (best == null || prefixCount(bound.binding()) > prefixCount(best.binding()))) {
          best = bound;
        }
      }
      if (best != null) return new Route(context, best);
      if (context.spec().blocks(trigger.isPad())) return new Route(context, null);
    }
    return Route.NONE;
  }

  private boolean prefixesHeld(Binding binding) {
    return !(binding instanceof Binding.Key key)
        || key.prefixes().stream().allMatch(this::modifierHeld);
  }

  private static int prefixCount(Binding binding) {
    return binding instanceof Binding.Key key ? key.prefixes().size() : 0;
  }

  // A key that is an action and also a prefix there fires on release, if no combo used it.
  private boolean isDeferred(Route route, int keycode) {
    ContextSpec spec = route.owner().spec();
    return spec.actions().stream()
        .flatMap(
            action -> bindings.bindings(action).stream().filter(key -> spec.accepts(action, key)))
        .anyMatch(
            binding -> binding instanceof Binding.Key key && key.prefixes().contains(keycode));
  }

  private void markComboUse(Binding binding) {
    if (!(binding instanceof Binding.Key key)) return;
    for (int prefix : key.prefixes()) {
      for (Press press : presses.values()) {
        if (press.source.source().equals("key") && matchesPrefix(press.source.pointer(), prefix)) {
          press.comboUsed = true;
        }
      }
    }
  }

  private static boolean matchesPrefix(int keycode, int prefix) {
    return keycode == prefix
        || (prefix == Input.Keys.CONTROL_LEFT
            && (keycode == Input.Keys.CONTROL_RIGHT || (MAC_OS && keycode == Input.Keys.SYM)))
        || (prefix == Input.Keys.SHIFT_LEFT && keycode == Input.Keys.SHIFT_RIGHT)
        || (prefix == Input.Keys.ALT_LEFT && keycode == Input.Keys.ALT_RIGHT);
  }

  private void startAction(Press press) {
    if (press.action == null || press.cancelled) return;
    ActionState state = state(press.action);
    press.active = true;
    state.active.add(press);
    if (state.active.size() == 1) state.edges.add(new Edge(true, press.owner, isPad(press)));
  }

  private void endAction(Press press) {
    if (!press.active) return;
    press.active = false;
    ActionState state = state(press.action);
    state.active.remove(press);
    if (state.active.isEmpty()) state.edges.add(new Edge(false, press.owner, isPad(press)));
  }

  private void firePressAndRelease(Press press) {
    startAction(press);
    endAction(press);
  }

  private void pressKeyOrButton(SourceId id, Route route, boolean handledByStage, int keycode) {
    Press press = new Press(id, route.owner(), handledByStage ? null : route.action().orElse(null));
    Press previous = presses.put(id, press);
    if (previous != null) endAction(previous);
    if (handledByStage || route.bound() == null) return;
    if (keycode >= 0 && isDeferred(route, keycode)) press.deferred = true;
    else startAction(press);
  }

  private void releaseKeyOrButton(SourceId id) {
    Press press = presses.remove(id);
    if (press == null || press.cancelled) return;
    if (press.deferred) {
      if (!press.comboUsed && press.action != null) firePressAndRelease(press);
    } else {
      endAction(press);
    }
  }

  @Override
  public boolean keyDown(int keycode) {
    heldKeys.add(keycode);
    device = InputDevice.KEYBOARD_MOUSE;
    Route route = resolve(new Binding.Key(keycode));
    if (route.bound() != null) markComboUse(route.bound().binding());
    boolean handled = stage != null && stage.keyDown(keycode);
    pressKeyOrButton(new SourceId("key", keycode), route, handled, keycode);
    return handled || route.bound() != null;
  }

  @Override
  public boolean keyUp(int keycode) {
    heldKeys.remove(keycode);
    boolean handled = stage != null && stage.keyUp(keycode);
    releaseKeyOrButton(new SourceId("key", keycode));
    return handled;
  }

  @Override
  public boolean keyTyped(char character) {
    return stage != null && stage.keyTyped(character);
  }

  private static SourceId mouseSource(int button, int pointer) {
    return new SourceId("mouse:" + button, pointer);
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    pointer(pointer, screenX, screenY);
    device = InputDevice.KEYBOARD_MOUSE;
    heldButtons.add(button);
    Route route = resolve(new Binding.MouseButton(button));
    boolean handled = stage != null && stage.touchDown(screenX, screenY, pointer, button);
    pressKeyOrButton(mouseSource(button, pointer), route, handled, -1);
    return handled || route.bound() != null;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    pointer(pointer, screenX, screenY);
    heldButtons.remove(button);
    boolean handled = stage != null && stage.touchUp(screenX, screenY, pointer, button);
    releaseKeyOrButton(mouseSource(button, pointer));
    return handled;
  }

  @Override
  public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
    heldButtons.remove(button);
    SourceId id = mouseSource(button, pointer);
    Press press = presses.get(id);
    if (press != null) cancelWhere(candidate -> candidate == press);
    presses.remove(id);
    return stage != null && stage.touchCancelled(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    pointer(pointer, screenX, screenY);
    device = InputDevice.KEYBOARD_MOUSE;
    return stage != null && stage.touchDragged(screenX, screenY, pointer);
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    pointer(0, screenX, screenY);
    device = InputDevice.KEYBOARD_MOUSE;
    return stage != null && stage.mouseMoved(screenX, screenY);
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    Route route = resolve(new Binding.MouseWheel());
    boolean handled = stage != null && stage.scrolled(amountX, amountY);
    if (!handled && route.bound() != null) {
      ActionState state = state(route.bound().action());
      state.pendingAxis += amountY;
      state.pendingOwner = route.owner();
    }
    return handled || route.bound() != null;
  }

  // endregion
}
