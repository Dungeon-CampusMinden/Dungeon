package engine.input;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keyboard focus in a group, a dialog or a menu: {@link UiActions#FOCUS_NEXT} and {@link
 * UiActions#FOCUS_PREVIOUS} move it through the buttons and text fields in layout order and wrap
 * around, {@link UiActions#PRESS} presses the focused button like a mouse click. A frame marks the
 * focused control until the mouse or the pad moves the cursor. Text fields take the keyboard focus
 * of the stage; buttons leave it alone, so mouse and pad work as before.
 *
 * <p>The focus lives on the group as a listener, which does not react to events. It stays when the
 * group swaps its children, but forgets the framed control, and a text field in the group finds it
 * for Tab, see {@link #leave}.
 */
public final class KeyboardFocus implements EventListener {
  private final Group group;
  private final Frame frame = new Frame();
  // Where the cursor was when a key last moved the focus.
  private float pointerX;
  private float pointerY;

  private KeyboardFocus(Group group) {
    this.group = group;
  }

  /**
   * Gets the keyboard focus of a group, adding one on first use.
   *
   * @param group the group whose buttons and text fields the focus moves through
   * @return the focus
   */
  public static KeyboardFocus of(Group group) {
    KeyboardFocus focus = find(group);
    if (focus == null) {
      focus = new KeyboardFocus(group);
      group.addListener(focus);
    }
    return focus;
  }

  /**
   * Moves the focus on from a focused text field, in the innermost group with a keyboard focus;
   * without one, the field hands the focus to the next or previous text field as libGDX does.
   *
   * @param field the text field
   * @param step 1 for the next control, -1 for the previous one
   */
  static void leave(TextField field, int step) {
    for (Group parent = field.getParent(); parent != null; parent = parent.getParent()) {
      KeyboardFocus focus = find(parent);
      if (focus != null) {
        focus.move(step);
        return;
      }
    }
    field.next(step < 0);
  }

  private static KeyboardFocus find(Group group) {
    for (EventListener listener : group.getListeners()) {
      if (listener instanceof KeyboardFocus focus) return focus;
    }
    return null;
  }

  @Override
  public boolean handle(Event event) {
    return false;
  }

  /**
   * Moves the focus to the next or previous button or text field. From outside the chain, the first
   * step forward goes to the first one and the first step back to the last one.
   *
   * @param step 1 for the next control, -1 for the previous one
   */
  public void move(int step) {
    List<Actor> chain = new ArrayList<>();
    collect(group, chain);
    if (chain.isEmpty()) return;
    int index = chain.indexOf(current());
    int next;
    if (index >= 0) next = Math.floorMod(index + step, chain.size());
    else next = step > 0 ? 0 : chain.size() - 1;
    focus(chain.get(next));
  }

  /**
   * Presses the focused button. Without a visible focus, it shows the focus instead.
   *
   * @see #pressFocused()
   */
  public void press() {
    if (pressFocused()) return;
    Actor target = frame.target();
    if (reachable(target)) focus(target);
    else move(1);
  }

  /**
   * Presses the focused button with a click in its middle, if the focus shows and nothing covers
   * the button there. A focused text field takes the keyboard focus again.
   *
   * @return true if the focus shows, whether or not the press reached the button
   */
  public boolean pressFocused() {
    Actor target = frame.target();
    Stage stage = group.getStage();
    if (stage == null || !frame.shows(stage, target)) return false;
    if (target instanceof TextField) {
      focus(target);
    } else if (target instanceof Button) {
      FocusFrame.scrollIntoView(target);
      Vector2 center =
          target.localToStageCoordinates(
              new Vector2(target.getWidth() / 2, target.getHeight() / 2));
      if (FocusFrame.hits(stage, target, center)) {
        Vector2 screen = stage.stageToScreenCoordinates(center);
        // A click like the mouse's, with its sound and its checks for disabled buttons.
        stage.touchDown((int) screen.x, (int) screen.y, 0, Buttons.LEFT);
        stage.touchUp((int) screen.x, (int) screen.y, 0, Buttons.LEFT);
      }
    }
    return true;
  }

  private Actor current() {
    Stage stage = group.getStage();
    Actor keyboard = stage == null ? null : stage.getKeyboardFocus();
    if (keyboard instanceof TextField && keyboard.isDescendantOf(group)) return keyboard;
    return frame.target();
  }

  private void focus(Actor control) {
    Stage stage = group.getStage();
    if (stage == null) return;
    Actor previous = frame.target();
    if (previous != null && previous != control) FocusFrame.exit(previous, control);
    frame.target(control);
    engine.utils.Vector2 pointer = GameInput.pointer();
    pointerX = pointer.x();
    pointerY = pointer.y();
    FocusFrame.scrollIntoView(control);
    if (control instanceof TextField field) {
      stage.setKeyboardFocus(field);
      // Like libGDX's own traversal, so that typing replaces the text; in a text area it would
      // replace all that was written.
      if (!(field instanceof TextArea)) field.selectAll();
    } else if (stage.getKeyboardFocus() instanceof TextField) stage.setKeyboardFocus(group);
    if (frame.getParent() != group) group.addActor(frame);
    frame.toFront();
  }

  private boolean reachable(Actor control) {
    return control != null
        && control.isDescendantOf(group)
        && control.ascendantsVisible()
        && !(control instanceof Disableable disableable && disableable.isDisabled());
  }

  // The buttons and text fields a mouse can use, in the order the group lays them out. Text fields
  // that keep Tab for themselves stay out, as in libGDX's own traversal.
  private static void collect(Group parent, List<Actor> chain) {
    for (Actor child : parent.getChildren()) {
      if (!child.isVisible() || child.getTouchable() == Touchable.disabled) continue;
      if (child instanceof Button
          || (child instanceof TextField field && field.getFocusTraversal())) {
        if (!(child instanceof Disableable disableable && disableable.isDisabled())) {
          chain.add(child);
        }
      } else if (child instanceof Group inner) {
        collect(inner, chain);
      }
    }
  }

  /** Shows while the keyboard is in use and the cursor stays where it was. */
  private final class Frame extends FocusFrame {
    @Override
    protected boolean shows(Stage stage, Actor target) {
      engine.utils.Vector2 pointer = GameInput.pointer();
      return GameInput.device() == InputDevice.KEYBOARD_MOUSE
          && pointer.x() == pointerX
          && pointer.y() == pointerY
          && reachable(target);
    }
  }
}
