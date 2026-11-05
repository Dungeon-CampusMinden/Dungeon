package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.hud.elements.RichLabel;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.util.MorphCoText;

/**
 * The production control system of Morph & Co. KG.
 *
 * <p>A screen saver hides the status screen until it is clicked. The tabs show the plant status,
 * the error log and the restart. The restart becomes available when F-04 and A-12 run again and is
 * protected by a security question about morphemes.
 */
final class ComputerUI extends MorphCoPanel {

  private static final Color SCREEN = new Color(0.04f, 0.07f, 0.06f, 1f);
  private static final Color SAVER = new Color(0.33f, 0.88f, 0.48f, 1f);
  private static final String GREEN = "[color=#55e07a]";
  private static final String RED = "[color=#ff5a4a]";
  private static final String GREY = "[color=#9aa7a0]";

  private final boolean f04Ok;
  private final boolean a12Ok;
  private final Table screen;
  private String tab = "status";
  private boolean restartAsked;
  private boolean restarting;
  private int wrongAnswers;

  private ComputerUI(DialogContext context) {
    super(context, MorphCoText.text("pc.title"), "", "");
    f04Ok = context.find(MorphCoDialogs.F04_OK, Boolean.class).orElse(false);
    a12Ok = context.find(MorphCoDialogs.A12_OK, Boolean.class).orElse(false);
    screen = new Table(skin);
    screen.setBackground(skin.newDrawable("white", SCREEN));
    screen.pad(24).top().left();
    body.add(screen).grow().minHeight(440);
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

  private void showScreenSaver() {
    screen.clearChildren();
    screen.center();
    screen
        .add(Scene2dElementFactory.createLabel(MorphCoText.text("pc.screensaver"), 64, SAVER))
        .row();
    screen.add(line(GREY + MorphCoText.text("pc.screensaver-hint"))).padTop(16);
    screen.setTouchable(Touchable.enabled);
    screen.setUserObject(Cursors.INTERACT);
    screen.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            screen.removeListener(this);
            screen.setUserObject(null);
            showSystem();
          }
        });
  }

  private void showSystem() {
    boolean ready = f04Ok && a12Ok;
    screen.clearChildren();
    screen.top().left();
    screen.defaults().left();
    screen.add(line(GREEN + "[size=26]" + MorphCoText.text("pc.header"))).growX().row();
    if (ready) {
      screen.add(line(GREEN + MorphCoText.text("pc.ok-header"))).padTop(6).row();
    } else {
      screen.add(line(RED + MorphCoText.text("pc.error"))).padTop(6).row();
      screen.add(line(RED + MorphCoText.text("pc.stop"))).row();
    }

    Table tabs = new Table();
    tabs.defaults().padRight(10).height(44);
    tabs.add(tabButton("status", "pc.tab-status"));
    tabs.add(tabButton("log", "pc.tab-log"));
    tabs.add(tabButton("restart", "pc.tab-restart"));
    screen.add(tabs).padTop(16).padBottom(12).row();

    Table content = new Table();
    content.top().left();
    content.defaults().left().padBottom(6);
    switch (tab) {
      case "log" -> fillLog(content);
      case "restart" -> fillRestart(content);
      default -> fillStatus(content);
    }
    screen.add(content).grow();
  }

  private TextButton tabButton(String id, String key) {
    TextButton button =
        Scene2dElementFactory.createButton(
            MorphCoText.text(key), id.equals(tab) ? "green" : "blue-outline", 20);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            tab = id;
            clearFeedback();
            showSystem();
          }
        });
    return button;
  }

  private void fillStatus(Table content) {
    content.add(status(f04Ok, "pc.status-f04-ok", "pc.status-f04-error")).growX().row();
    content.add(status(a12Ok, "pc.status-a12-ok", "pc.status-a12-error")).growX().row();
    content.add(line(GREEN + MorphCoText.text("pc.status-k03"))).growX().row();
    content.add(line(GREEN + MorphCoText.text("pc.status-z01"))).growX().row();
  }

  private RichLabel status(boolean ok, String okKey, String errorKey) {
    return line(ok ? GREEN + MorphCoText.text(okKey) : RED + MorphCoText.text(errorKey));
  }

  private void fillLog(Table content) {
    content.add(line(GREY + MorphCoText.text("pc.log-1"))).growX().row();
    content.add(line(GREY + MorphCoText.text("pc.log-2"))).growX().row();
    content.add(line(GREY + MorphCoText.text("pc.log-3"))).growX().row();
    if (a12Ok) content.add(line(GREEN + MorphCoText.text("pc.log-a12"))).growX().row();
    if (f04Ok) content.add(line(GREEN + MorphCoText.text("pc.log-f04"))).growX().row();
  }

  private void fillRestart(Table content) {
    if (!f04Ok || !a12Ok) {
      content.add(line(RED + MorphCoText.text("pc.restart-unavailable"))).growX().row();
      content.add(line(GREY + MorphCoText.text("pc.restart-unavailable-hint"))).growX().row();
      return;
    }
    content.add(line(GREEN + MorphCoText.text("pc.restart-available"))).growX().row();
    if (!restartAsked) {
      TextButton restart =
          Scene2dElementFactory.createButton(MorphCoText.text("pc.restart-button"), "green", 26);
      restart.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              restartAsked = true;
              showSystem();
            }
          });
      content.add(restart).height(56).padTop(10).row();
      return;
    }
    content.add(line(GREEN + MorphCoText.text("pc.security-question"))).growX().padTop(8).row();
    TextField answer = Scene2dElementFactory.createTextField("", 26);
    answer.setMessageText(MorphCoText.text("pc.security-placeholder"));
    answer.setTextFieldFilter((field, character) -> Character.isDigit(character));
    answer.setMaxLength(3);
    TextButton confirm =
        Scene2dElementFactory.createButton(MorphCoText.text("pc.security-submit"), "green", 22);
    Runnable send = () -> answer(answer.getText());
    answer.setTextFieldListener(
        (field, character) -> {
          if (character == '\r' || character == '\n') send.run();
        });
    confirm.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            send.run();
          }
        });
    Table row = new Table();
    row.add(answer).width(160).height(52).padRight(12);
    row.add(confirm).height(52);
    content.add(row).padTop(8).row();
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

  private static RichLabel line(String text) {
    RichLabel label = new RichLabel(text, 22, Color.WHITE);
    label.setWrap(true);
    label.setMaxPrefWidth(contentWidth() - 60f);
    return label;
  }
}
