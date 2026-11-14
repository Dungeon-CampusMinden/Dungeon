package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.List;
import rooms.morphco.puzzles.PhoneLine;
import rooms.morphco.util.MorphCoText;

/**
 * The phone in the control room: handset, mailbox lamp, keypad and the note with the important
 * extensions.
 *
 * <p>Confirming the digits dials. An unknown number gets "no connection" right away; for a {@link
 * PhoneLine} the server closes the phone and lets the line answer.
 */
final class PhoneUI extends MorphCoConsoleUI {

  private static final float PHONE_WIDTH = 480;
  private static final float PHONE_HEIGHT = 300;
  private static final float NOTE_WIDTH = 300;
  private static final float GAP = 28;
  private static final float MARGIN = 16;
  private static final float KEYPAD_X = PHONE_WIDTH - 24 - KeypadNode.WIDTH;
  // The mailbox lamp sits between the handset and the keypad.
  private static final float LAMP_X = 206;
  private static final Color HANDSET_EDGE = Color.valueOf("96461a");

  private final boolean messageWaiting;
  private final Phone phone = area().addNode(new Phone());
  private final KeypadNode keypad = area().addNode(new KeypadNode("keypad", 4, this::dial));
  private final PanelNode note = area().addNode(new PanelNode("note", PanelNode.Look.PAPER, 16));

  private PhoneUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("world.control.phone-title"),
        MorphCoText.text(
            messageWaiting(context) ? "world.control.phone" : "world.control.phone-quiet"),
        MorphCoText.text("ui.phone.help"));
    messageWaiting = messageWaiting(context);
    // Below the strip of tape.
    note.content().padTop(8).defaults().left();
    note.content()
        .add(MorphCoUI.bold(MorphCoText.text("ui.phone.note-title"), 18, MorphCoUI.INK))
        .colspan(2)
        .growX()
        .padBottom(10)
        .row();
    for (PhoneLine line : List.of(PhoneLine.MAILBOX, PhoneLine.HOTLINE, PhoneLine.SECURITY)) {
      note.content()
          .add(MorphCoUI.label(MorphCoText.text("ui.phone." + line.name()), 16, MorphCoUI.INK))
          .growX()
          .padBottom(6);
      Label number = MorphCoUI.bold(line.number(), 18, MorphCoUI.INK);
      number.setWrap(false);
      note.content().add(number).right().top().padLeft(12).padBottom(6).row();
    }
    addListener(
        new InputListener() {
          @Override
          public boolean keyTyped(InputEvent event, char character) {
            return keypad.type(character);
          }
        });
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new PhoneUI(context);
  }

  private static boolean messageWaiting(DialogContext context) {
    return context.find(MorphCoDialogs.MESSAGE_WAITING, Boolean.class).orElse(false);
  }

  @Override
  protected float arrange(float width, float height) {
    float left = Math.max(0, Math.round((width - PHONE_WIDTH - GAP - NOTE_WIDTH) / 2));
    float top = -MARGIN;
    phone.position(left, top - PHONE_HEIGHT);
    keypad.position(left + KEYPAD_X, top - PHONE_HEIGHT + (PHONE_HEIGHT - KeypadNode.HEIGHT) / 2);
    float noteHeight = note.fit(NOTE_WIDTH);
    note.position(left + PHONE_WIDTH + GAP, top - noteHeight);
    return MARGIN + Math.max(PHONE_HEIGHT, noteHeight) + MARGIN;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    // The digits belong to the keypad, not to the hero.
    if (stage != null) stage.setKeyboardFocus(this);
  }

  private void dial(String digits) {
    submit(digits);
    if (PhoneLine.dial(digits).isPresent()) {
      // The display keeps the call until the server closes the phone, so OK cannot dial twice.
      keypad.show(MorphCoText.text("ui.phone.calling"), MorphCoUI.SIGNAL);
      return;
    }
    keypad.show(MorphCoText.text("ui.phone.no-line-short"), MorphCoUI.STOP);
    feedback(MorphCoText.text("ui.phone.no-line"), false);
  }

  /** Body of the phone with the handset and the mailbox lamp; it takes no input. */
  private final class Phone extends CanvasNode {

    private Phone() {
      super("phone", PHONE_WIDTH, PHONE_HEIGHT);
      movable(false);
      selectable(false);
      deletable(false);
      setTouchable(Touchable.disabled);
    }

    @Override
    protected void buildContent() {
      Table tag = MorphCoUI.tag(MorphCoText.text("ui.phone.lamp"));
      tag.pack();
      tag.setPosition(LAMP_X - tag.getWidth() / 2, 150);
      addActor(tag);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      float x = x();
      float y = y();
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width(), height());
      // The cradle, the two cups of the handset and the grip between them.
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 28, y + 22, 116, 256);
      cup(batch, alpha, x + 34, y + 206);
      cup(batch, alpha, x + 34, y + 32);
      CanvasGraphics.fill(batch, HANDSET_EDGE, alpha, x + 62, y + 92, 48, 116);
      CanvasGraphics.fill(batch, MorphCoUI.ORANGE, alpha, x + 64, y + 92, 44, 116);
      // The lamp blinks while the mailbox holds the unheard message.
      boolean lit = messageWaiting && TimeUtils.millis() / 500 % 2 == 0;
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + LAMP_X - 14, y + 196, 28, 28);
      CanvasGraphics.fill(
          batch, lit ? MorphCoUI.STOP : MorphCoUI.LED_OFF, alpha, x + LAMP_X - 10, y + 200, 20, 20);
    }

    private void cup(Batch batch, float alpha, float x, float y) {
      CanvasGraphics.fill(batch, HANDSET_EDGE, alpha, x, y, 104, 62);
      CanvasGraphics.fill(batch, MorphCoUI.ORANGE, alpha, x + 2, y + 3, 100, 57);
      for (int column = 0; column < 4; column++) {
        for (int row = 0; row < 2; row++) {
          CanvasGraphics.fill(
              batch, HANDSET_EDGE, alpha, x + 28 + column * 14, y + 20 + row * 14, 6, 6);
        }
      }
    }
  }
}
