package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.util.MorphCoText;

/**
 * The production control system of Morph & Co. KG.
 *
 * <p>A screen saver hides the status screen until it is clicked. The tabs show the plant status,
 * the error log and the restart. The restart becomes available when F-04 and A-12 run again and is
 * protected by a security question about morphemes.
 */
final class ComputerUI extends MorphCoConsoleUI {

  private final boolean f04Ok;
  private final boolean a12Ok;
  private final PanelNode monitor =
      area().addNode(new PanelNode("monitor", PanelNode.Look.SCREEN, 36));
  private final Table screen = monitor.content();
  private String tab = "status";
  private boolean restartAsked;
  private boolean restarting;
  private int wrongAnswers;

  private ComputerUI(DialogContext context) {
    super(context, MorphCoText.text("pc.title"), "", "");
    f04Ok = context.find(MorphCoDialogs.F04_OK, Boolean.class).orElse(false);
    a12Ok = context.find(MorphCoDialogs.A12_OK, Boolean.class).orElse(false);
    showScreenSaver();
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new ComputerUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    float monitorHeight = Math.max(height - 4, monitor.fit(width));
    monitor.size(width, monitorHeight).position(0, -monitorHeight);
    return monitorHeight;
  }

  private void showScreenSaver() {
    screen.clearChildren();
    screen.center();
    Label saver = MorphCoUI.display(MorphCoText.text("pc.screensaver"), 64, MorphCoUI.GO);
    saver.setAlignment(Align.center);
    screen.add(saver).growX().row();
    Label hint = MorphCoUI.label(MorphCoText.text("pc.screensaver-hint"), 20, MorphCoUI.MUTED);
    hint.setAlignment(Align.center);
    screen.add(hint).growX().padTop(16);
    screen.setTouchable(Touchable.enabled);
    screen.setUserObject(Cursors.INTERACT);
    screen.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            screen.removeListener(this);
            screen.setTouchable(Touchable.childrenOnly);
            screen.setUserObject(null);
            showSystem();
          }
        });
  }

  private void showSystem() {
    boolean ready = f04Ok && a12Ok;
    screen.clearChildren();
    screen.top().left();
    screen.defaults().left().growX();
    screen.add(MorphCoUI.display(MorphCoText.text("pc.header"), 26, MorphCoUI.GO)).row();
    if (ready) {
      screen.add(line(MorphCoText.text("pc.ok-header"), MorphCoUI.GO)).padTop(6).row();
    } else {
      screen.add(line(MorphCoText.text("pc.error"), MorphCoUI.STOP)).padTop(6).row();
      screen.add(line(MorphCoText.text("pc.stop"), MorphCoUI.STOP)).row();
    }

    Table tabs = new Table();
    tabs.defaults().padRight(10).minHeight(44);
    tabs.add(tabButton("status", "pc.tab-status"));
    tabs.add(tabButton("log", "pc.tab-log"));
    tabs.add(tabButton("restart", "pc.tab-restart"));
    screen.add(tabs).fill(false, false).padTop(18).padBottom(14).row();

    switch (tab) {
      case "log" -> fillLog();
      case "restart" -> fillRestart();
      default -> fillStatus();
    }
    changed();
  }

  private TextButton tabButton(String id, String key) {
    boolean active = id.equals(tab);
    TextButton button =
        MorphCoUI.button(
            MorphCoText.text(key),
            false,
            () -> {
              tab = id;
              showSystem();
            });
    TextButton.TextButtonStyle style = button.getStyle();
    style.up = MorphCoUI.plate(active ? MorphCoUI.GO : MorphCoUI.STEEL_DARK);
    style.over = MorphCoUI.plate(active ? MorphCoUI.GO : MorphCoUI.STEEL);
    style.down = style.over;
    style.fontColor = active ? MorphCoUI.INK : MorphCoUI.GO;
    style.overFontColor = style.fontColor;
    style.downFontColor = style.fontColor;
    return button;
  }

  private void fillStatus() {
    status(f04Ok, "pc.status-f04-ok", "pc.status-f04-error");
    status(a12Ok, "pc.status-a12-ok", "pc.status-a12-error");
    screen.add(line(MorphCoText.text("pc.status-k03"), MorphCoUI.GO)).padBottom(6).row();
    screen.add(line(MorphCoText.text("pc.status-z01"), MorphCoUI.GO)).padBottom(6).row();
  }

  private void status(boolean ok, String okKey, String errorKey) {
    screen
        .add(line(MorphCoText.text(ok ? okKey : errorKey), ok ? MorphCoUI.GO : MorphCoUI.STOP))
        .padBottom(6)
        .row();
  }

  private void fillLog() {
    for (String key : new String[] {"pc.log-1", "pc.log-2", "pc.log-3"}) {
      screen.add(line(MorphCoText.text(key), MorphCoUI.MUTED)).padBottom(6).row();
    }
    if (a12Ok) screen.add(line(MorphCoText.text("pc.log-a12"), MorphCoUI.GO)).padBottom(6).row();
    if (f04Ok) screen.add(line(MorphCoText.text("pc.log-f04"), MorphCoUI.GO)).padBottom(6).row();
  }

  private void fillRestart() {
    if (!f04Ok || !a12Ok) {
      screen.add(line(MorphCoText.text("pc.restart-unavailable"), MorphCoUI.STOP)).row();
      screen
          .add(line(MorphCoText.text("pc.restart-unavailable-hint"), MorphCoUI.MUTED))
          .padTop(6)
          .row();
      return;
    }
    screen.add(line(MorphCoText.text("pc.restart-available"), MorphCoUI.GO)).row();
    if (!restartAsked) {
      TextButton restart =
          MorphCoUI.button(
              MorphCoText.text("pc.restart-button"),
              true,
              () -> {
                restartAsked = true;
                showSystem();
              });
      screen.add(restart).fill(false, false).minWidth(200).height(56).padTop(12).row();
      return;
    }
    screen.add(line(MorphCoText.text("pc.security-question"), MorphCoUI.GO)).padTop(8).row();
    TextField answer = Scene2dElementFactory.createTextField("", 26);
    answer.setMessageText(MorphCoText.text("pc.security-placeholder"));
    answer.setTextFieldFilter((field, character) -> Character.isDigit(character));
    answer.setMaxLength(3);
    Runnable send = () -> answer(answer.getText());
    answer.setTextFieldListener(
        (field, character) -> {
          if (character == '\r' || character == '\n') send.run();
        });
    Table row = new Table();
    row.add(answer).width(160).height(52).padRight(12);
    row.add(MorphCoUI.button(MorphCoText.text("pc.security-submit"), true, send)).height(52);
    screen.add(row).fill(false, false).padTop(10).row();
    if (getStage() != null) getStage().setKeyboardFocus(answer);
  }

  private void answer(String value) {
    if (restarting || value == null || value.isBlank()) return;
    boolean correct = MorphemeQuestions.restartAnswer(value);
    if (correct) {
      restarting = true;
      feedback(MorphCoText.text("pc.security-correct"), true);
    } else {
      wrongAnswers++;
      String text = MorphCoText.text("pc.security-wrong");
      if (wrongAnswers >= 2) text += " " + MorphCoText.text("pc.security-hint");
      feedback(text, false);
    }
    submit(value.trim());
  }

  private static Label line(String text, Color color) {
    return MorphCoUI.label(text, 20, color);
  }
}
