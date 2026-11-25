package rooms.morphco.ui;

import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.input.ActionId;
import engine.input.ContextSpec;
import engine.input.UiActions;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import rooms.morphco.util.MorphCoText;

/**
 * Close-up of the console in the control room: labelled push buttons, gauges, faders and the alarm
 * lamp.
 *
 * <p>The button "Nerviger Alarmton" is pushed in and trembles with the siren. A pressed button
 * sends its name to the server, which answers on the display of the close-up; the close-up stays
 * open until the player closes it. Once the alarm is off, the lamp goes dark and the button is
 * released. Without a mouse, the arrow keys, Tab or the pad's direction keys pick a button; space,
 * enter or the pad's A button presses it. The first button is picked from the start, so the first
 * press already pushes it. The mouse picks the button it rests on.
 */
final class AlarmConsoleUI extends MorphCoConsoleUI implements DialogFeedbackReceiver {

  /** Picks the next button without a mouse. */
  private static final ActionId NEXT = new ActionId("morphco", "console_next", key(Keys.TAB));

  /** Presses the picked button without a mouse. */
  private static final ActionId PRESS = new ActionId("morphco", "console_press", key(Keys.ENTER));

  /** Input of the console: the directions and Tab pick a button, confirm and enter press it. */
  static final ContextSpec INPUT =
      UiActions.modal(
          "morphco_console",
          UiActions.NAV_LEFT,
          UiActions.NAV_RIGHT,
          UiActions.NAV_UP,
          UiActions.NAV_DOWN,
          UiActions.CONFIRM,
          NEXT,
          PRESS);

  private static final String ALARM = "alarm";

  // Buttons from left to right with the color of their caps; the server knows them by name.
  private static final List<Spec> BUTTONS =
      List.of(
          new Spec("conveyor", MorphCoUI.SIGNAL),
          new Spec(ALARM, Color.valueOf("c8382d")),
          new Spec("coffee", Color.valueOf("b07a45")),
          new Spec("music", Color.valueOf("3d7be0")),
          new Spec("light", MorphCoUI.GO));

  private static final float WIDTH = 1080;
  private static final float HEIGHT = 380;
  private static final float MARGIN = 16;
  private static final float SLOT = (WIDTH - 80) / BUTTONS.size();
  private static final float BUTTON_HEIGHT = 166;
  private static final float CAP_WIDTH = 80;
  private static final float CAP_HEIGHT = 52;
  private static final float CAP_Y = 100;
  private static final float PLATE_HEIGHT = 72;

  // Gauges and faders in the upper half of the desk, relative to its bottom left corner.
  private static final float GAUGES_Y = 285;
  private static final float[] FADERS = {.3f, .75f, .5f, .9f};

  private final Desk desk = area().addNode(new Desk());
  private final List<PushButton> buttons = new ArrayList<>();
  private PushButton focused;
  private PushButton pressed;
  private boolean silenced;

  private AlarmConsoleUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.control.console-title"),
        "",
        MorphCoText.text("ui.console.help"));
    for (Spec spec : BUTTONS) buttons.add(area().addNode(new PushButton(spec)));
    focused = buttons.getFirst();
    UIUtils.onUiAction(this, UiActions.NAV_LEFT, () -> focusNext(-1));
    UIUtils.onUiAction(this, UiActions.NAV_RIGHT, () -> focusNext(1));
    UIUtils.onUiAction(this, UiActions.NAV_UP, () -> focusNext(-1));
    UIUtils.onUiAction(this, UiActions.NAV_DOWN, () -> focusNext(1));
    UIUtils.onUiAction(this, NEXT, () -> focusNext(1));
    UIUtils.onUiAction(this, UiActions.CONFIRM, () -> press(focused));
    UIUtils.onUiAction(this, PRESS, () -> press(focused));
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new AlarmConsoleUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    float left = Math.max(0, Math.round((width - WIDTH) / 2));
    float bottom = -MARGIN - HEIGHT;
    desk.position(left, bottom);
    for (int index = 0; index < buttons.size(); index++) {
      buttons.get(index).position(left + 40 + index * SLOT, bottom + 28);
    }
    return MARGIN + HEIGHT + MARGIN;
  }

  private void focusNext(int step) {
    focused = buttons.get(Math.floorMod(buttons.indexOf(focused) + step, buttons.size()));
  }

  // The button stays down until the server has answered, so it cannot be pressed twice.
  private void press(PushButton button) {
    if (pressed != null) return;
    pressed = button;
    submit(button.spec.name());
  }

  @Override
  public void applyFeedback(DialogFeedbackMessage message) {
    pressed = null;
    if (message.successful()) silenced = true;
    feedback(MorphCoText.text(message.messageKey()), message.successful());
  }

  private record Spec(String name, Color cap) {}

  /** The desk with the alarm lamp, gauges and faders; it takes no input. */
  private final class Desk extends CanvasNode {
    private final long openedAt = TimeUtils.millis();

    private Desk() {
      super("desk", WIDTH, HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {
      // Drawn entirely in drawBackground.
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      long time = TimeUtils.timeSinceMillis(openedAt);
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      MorphCoUI.hazard(batch, alpha, x + 16, y + height() - 18, width() - 32, 8);
      MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x + 24, y + 20, width() - 48, 186);
      // The alarm lamp flashes with the siren.
      boolean flash = !silenced && time / 400 % 2 == 0;
      MorphCoUI.ellipse(batch, MorphCoUI.OUTLINE, alpha, x + 90, y + GAUGES_Y, 34, 34);
      if (flash)
        MorphCoUI.ellipse(batch, MorphCoUI.STOP, alpha * .3f, x + 90, y + GAUGES_Y, 54, 54);
      MorphCoUI.ellipse(
          batch, flash ? MorphCoUI.STOP : MorphCoUI.LED_OFF, alpha, x + 90, y + GAUGES_Y, 28, 28);
      for (int gauge = 0; gauge < 3; gauge++) {
        float angle = 1.2f + .5f * MathUtils.sin(time / 90f + gauge * 2.1f);
        gauge(batch, alpha, x + 250 + gauge * 110, y + GAUGES_Y, angle);
      }
      for (int fader = 0; fader < FADERS.length; fader++) {
        float faderX = x + width() - 300 + fader * 64;
        CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, faderX - 4, y + 228, 8, 110);
        float knobY = y + 228 + FADERS[fader] * 96;
        MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, faderX - 18, knobY - 7, 36, 14);
        CanvasGraphics.fill(batch, MorphCoUI.TEXT, alpha, faderX - 14, knobY, 28, 2);
      }
    }

    // A round dial whose needle swings with the siren.
    private static void gauge(Batch batch, float alpha, float x, float y, float angle) {
      MorphCoUI.ellipse(batch, MorphCoUI.OUTLINE, alpha, x, y, 38, 38);
      MorphCoUI.ellipse(batch, MorphCoUI.PAPER, alpha, x, y, 33, 33);
      CanvasGraphics.fill(batch, MorphCoUI.STOP, alpha, x + 12, y + 18, 12, 6);
      float cos = MathUtils.cos(angle);
      float sin = MathUtils.sin(angle);
      for (float step = 0; step < 26; step += 2)
        CanvasGraphics.fill(
            batch, MorphCoUI.INK, alpha, x + cos * step - 1, y + sin * step - 1, 3, 3);
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x - 4, y - 4, 8, 8);
    }
  }

  /** A push button with its label plate below; the alarm button is stuck until the alarm is off. */
  private final class PushButton extends CanvasNode {
    private final Spec spec;
    private final boolean stuck;

    private PushButton(Spec spec) {
      super("button-" + spec.name(), SLOT, BUTTON_HEIGHT);
      this.spec = spec;
      stuck = ALARM.equals(spec.name());
      movable(false);
      selectable(false);
      deletable(false);
      setUserObject(Cursors.INTERACT);
      // The mouse picks the button it rests on, so there is only ever one picked button.
      addListener(
          new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              super.enter(event, x, y, pointer, fromActor);
              focused = PushButton.this;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
              press(PushButton.this);
            }
          });
    }

    @Override
    protected void buildContent() {
      Label label =
          MorphCoUI.bold(
              MorphCoText.text("world.control.console-" + spec.name()), 16, MorphCoUI.INK);
      label.setAlignment(Align.center);
      label.setBounds(14, 12, width() - 28, PLATE_HEIGHT - 16);
      addActor(label);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      boolean picked = focused == this && pressed == null;
      float capX = x + (width() - CAP_WIDTH) / 2;
      float capY = y + CAP_Y;
      CanvasGraphics.fill(
          batch, MorphCoUI.OUTLINE, alpha, capX - 8, capY - 8, CAP_WIDTH + 16, CAP_HEIGHT + 16);
      // A pressed button sits deep in its collar; the stuck alarm button sits halfway down and
      // trembles in time with the siren.
      float depth = 0;
      if (pressed == this) {
        depth = 18;
      } else if (stuck && !silenced) {
        depth = 10;
        capX += TimeUtils.millis() / 60 % 2 == 0 ? -1 : 1;
      }
      Color face = picked ? spec.cap().cpy().lerp(Color.WHITE, .25f) : spec.cap();
      if (depth > 0) face = face.cpy().lerp(Color.BLACK, depth / 50);
      MorphCoUI.plate(batch, face, alpha, capX, capY, CAP_WIDTH, CAP_HEIGHT - depth);
      CanvasGraphics.fill(
          batch, Color.WHITE, alpha * .35f, capX + 8, capY + CAP_HEIGHT - depth - 12, 24, 4);
      MorphCoUI.plate(batch, MorphCoUI.TAG, alpha, x + 8, y + 4, width() - 16, PLATE_HEIGHT);
      if (picked)
        CanvasGraphics.outline(batch, MorphCoUI.SIGNAL, alpha, x, y, width(), height(), 3);
    }
  }
}
