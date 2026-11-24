package engine.game;

import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.ContextLayer;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.input.UiActions;
import feature.hud.UIUtils;
import feature.interaction.InteractionDialogColors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Keyboard control of a menu: Tab and Shift+Tab move a focus through the buttons, text fields,
 * select boxes and sliders, Enter or Space presses the focused element, and Escape goes back. Left
 * and Right adjust a focused slider or select box, otherwise the view decides (character
 * selection). The Up and Down arrow keys and Page Up and Page Down scroll the text. The focus
 * covers the open dialog, otherwise the menu content.
 *
 * <p>The focused element gets a frame after a key moved the focus; the frame disappears when the
 * mouse or the pad cursor moves. Only text fields take the keyboard focus of the stage, so the
 * other elements keep working with mouse and pad as before.
 */
final class MenuKeyboard {
  private static final ActionId NEXT = menu("next", key(Keys.TAB));
  private static final ActionId PREVIOUS = menu("previous", key(Keys.SHIFT_LEFT, Keys.TAB));
  private static final ActionId PRESS =
      menu("press", key(Keys.ENTER), key(Keys.NUMPAD_ENTER), key(Keys.SPACE));
  private static final ActionId PAGE_UP = menu("page_up", key(Keys.PAGE_UP));
  private static final ActionId PAGE_DOWN = menu("page_down", key(Keys.PAGE_DOWN));
  private static final ActionId SCROLL_UP = menu("scroll_up", key(Keys.UP));
  private static final ActionId SCROLL_DOWN = menu("scroll_down", key(Keys.DOWN));
  private static final ActionId[] ACTIONS = {
    NEXT,
    PREVIOUS,
    PRESS,
    PAGE_UP,
    PAGE_DOWN,
    SCROLL_UP,
    SCROLL_DOWN,
    UiActions.CLOSE,
    UiActions.CLICK,
    UiActions.NAV_LEFT,
    UiActions.NAV_RIGHT
  };

  // A dialog takes the UI layer, so that its modal window passes the keys on to the actions. The
  // pad scrolls with the stick and the d-pad, which repeat while held.
  private final InputContext viewContext =
      handlers(
          ContextSpec.of("main_menu", ContextLayer.BASE, ACTIONS)
              .padOnly(UiActions.NAV_UP, UiActions.NAV_DOWN)
              .blocking());
  private final InputContext dialogContext =
      handlers(
          ContextSpec.of("main_menu_dialog", ContextLayer.UI, ACTIONS)
              .padOnly(UiActions.NAV_UP, UiActions.NAV_DOWN)
              .blocking());
  private final Drawable outline = UIUtils.tinted(InteractionDialogColors.INK);
  private final Drawable highlight = UIUtils.tinted(InteractionDialogColors.SELECTED);

  private final Stage stage;
  // Holds the current view.
  private final Group content;
  // What Escape does in a view outside a dialog.
  private final Runnable back;
  // What Left and Right do in a view while no slider or select box has the focus.
  private final IntConsumer sideways;
  private Actor focused;
  private boolean shown;
  // Where the mouse or pad cursor was when a key last moved the focus.
  private float pointerX;
  private float pointerY;

  MenuKeyboard(Stage stage, Group content, Runnable back, IntConsumer sideways) {
    this.stage = stage;
    this.content = content;
    this.back = back;
    this.sideways = sideways;
  }

  private static ActionId menu(String name, Binding... defaults) {
    return new ActionId("main_menu", name, defaults);
  }

  private InputContext handlers(ContextSpec spec) {
    return new InputContext(spec)
        .onPress(NEXT, () -> move(1))
        .onPress(PREVIOUS, () -> move(-1))
        .onPress(PRESS, this::press)
        .onPress(PAGE_UP, () -> page(-1))
        .onPress(PAGE_DOWN, () -> page(1))
        .onPress(SCROLL_UP, () -> page(-1 / 4f))
        .onPress(SCROLL_DOWN, () -> page(1 / 4f))
        .onPress(UiActions.NAV_UP, () -> GameInput.scroll(-1))
        .onPress(UiActions.NAV_DOWN, () -> GameInput.scroll(1))
        .onPress(UiActions.CLOSE, this::close)
        .onPress(UiActions.NAV_LEFT, () -> horizontal(-1))
        .onPress(UiActions.NAV_RIGHT, () -> horizontal(1));
  }

  /**
   * Activates the menu input in the base layer; contexts activated afterwards in that layer lie
   * above it and still get their keys.
   */
  void activate() {
    GameInput.activate(viewContext);
  }

  void deactivate() {
    GameInput.deactivate(viewContext);
    GameInput.deactivate(dialogContext);
  }

  // Lets Tab and Shift+Tab leave a text field to the next or previous element, and Enter submit the
  // view like its confirm button. The filter takes the Tab: the font has a glyph for it, so the
  // field would type it, and a rejected character never reaches the listener.
  void textField(TextField field, Runnable submit) {
    field.setFocusTraversal(false);
    field.setTextFieldFilter((typedIn, character) -> {
      if (character != '\t') return true;
      move(GameInput.shift() ? -1 : 1);
      return false;
    });
    field.setTextFieldListener((typedIn, character) -> {
      if (character == '\r' || character == '\n') submit.run();
    });
  }

  /**
   * Hands the keys to an opened dialog and hides the frame once the mouse or pad cursor moved; call
   * once per frame before the stage acts.
   */
  void update() {
    GameInput.active(dialogContext, scope() instanceof Dialog);
    engine.utils.Vector2 pointer = GameInput.pointer();
    if (pointer.x() != pointerX || pointer.y() != pointerY) shown = false;
  }

  /** Draws the frame around the focused element; call once per frame after the stage. */
  void draw() {
    if (!shown || !inScope(scope())) return;

    Vector2 corner = focused.localToStageCoordinates(new Vector2());
    float width = focused.getWidth();
    float height = focused.getHeight();
    Batch batch = stage.getBatch();
    batch.setProjectionMatrix(stage.getCamera().combined);
    batch.setColor(Color.WHITE);
    batch.begin();
    frame(batch, outline, corner.x - 6, corner.y - 6, width + 12, height + 12, 2);
    frame(batch, highlight, corner.x - 4, corner.y - 4, width + 8, height + 8, 3);
    batch.end();
  }

  private static void frame(
      Batch batch, Drawable drawable, float x, float y, float width, float height, float line) {
    drawable.draw(batch, x, y, width, line);
    drawable.draw(batch, x, y + height - line, width, line);
    drawable.draw(batch, x, y, line, height);
    drawable.draw(batch, x + width - line, y, line, height);
  }

  private Group scope() {
    Actor keyboardFocus = stage.getKeyboardFocus();
    Dialog dialog = keyboardFocus == null ? null : keyboardFocus.firstAscendant(Dialog.class);
    return dialog != null ? dialog : content;
  }

  private boolean inScope(Group scope) {
    return focused != null && focused.isDescendantOf(scope) && focused.ascendantsVisible();
  }

  private void move(int step) {
    Group scope = scope();
    if (stage.getKeyboardFocus() instanceof TextField field) focused = field;
    List<Actor> chain = chain(scope);
    if (chain.isEmpty()) return;
    int index = chain.indexOf(focused);
    int next;
    if (index >= 0) next = Math.floorMod(index + step, chain.size());
    else if (step > 0) next = 0;
    else next = chain.size() - 1;
    focus(chain.get(next), scope);
  }

  // The elements a mouse can use, in the order the views lay them out.
  private static List<Actor> chain(Group group) {
    List<Actor> chain = new ArrayList<>();
    collect(group, chain);
    return chain;
  }

  private static void collect(Group group, List<Actor> chain) {
    for (Actor child : group.getChildren()) {
      if (!child.isVisible() || child.getTouchable() == Touchable.disabled) continue;
      if (child instanceof Button
          || child instanceof TextField
          || child instanceof SelectBox
          || child instanceof Slider) {
        if (!(child instanceof Disableable element && element.isDisabled())) chain.add(child);
      } else if (child instanceof Group inner) {
        collect(inner, chain);
      }
    }
  }

  private void focus(Actor actor, Group scope) {
    focused = actor;
    shown = true;
    engine.utils.Vector2 pointer = GameInput.pointer();
    pointerX = pointer.x();
    pointerY = pointer.y();
    if (actor instanceof TextField) stage.setKeyboardFocus(actor);
    else if (scope instanceof Dialog) stage.setKeyboardFocus(scope);
    else stage.setKeyboardFocus(null);
    ScrollPane pane = actor.firstAscendant(ScrollPane.class);
    if (pane != null) {
      Vector2 position = actor.localToAscendantCoordinates(pane.getActor(), new Vector2());
      pane.scrollTo(position.x, position.y, actor.getWidth(), actor.getHeight());
    }
  }

  // Without a visible focus, the first press only shows it.
  private void press() {
    Group scope = scope();
    if (!inScope(scope)) {
      move(1);
    } else if (!shown) {
      focus(focused, scope);
    } else if (focused instanceof Button) {
      Vector2 center =
          focused.localToScreenCoordinates(
              new Vector2(focused.getWidth() / 2, focused.getHeight() / 2));
      // A click like the mouse's, with its sound and its checks for disabled buttons.
      stage.touchDown((int) center.x, (int) center.y, 0, Buttons.LEFT);
      stage.touchUp((int) center.x, (int) center.y, 0, Buttons.LEFT);
    } else if (focused instanceof SelectBox<?> box) {
      cycle(box, 1);
    }
  }

  private static void cycle(SelectBox<?> box, int step) {
    int size = box.getItems().size;
    if (size > 0) box.setSelectedIndex(Math.floorMod(box.getSelectedIndex() + step, size));
  }

  private void close() {
    if (scope() instanceof Dialog dialog) dialog.hide();
    else back.run();
  }

  private void horizontal(int step) {
    Group scope = scope();
    boolean adjusting = shown && inScope(scope);
    if (adjusting && focused instanceof Slider slider) {
      slider.setValue(slider.getValue() + step * slider.getStepSize());
    } else if (adjusting && focused instanceof SelectBox<?> box) {
      cycle(box, step);
    } else if (scope == content) {
      sideways.accept(step);
    }
  }

  private void page(float pages) {
    UIUtils.findTypeInGroup(scope(), ScrollPane.class)
        .ifPresent(pane -> pane.setScrollY(pane.getScrollY() + pages * pane.getScrollHeight()));
  }
}
