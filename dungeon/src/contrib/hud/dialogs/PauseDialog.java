package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Game;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.BaseContainerUI;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.settings.ClientSettings;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private builder for the pause menu.
 *
 * <p>Creates a simple pause dialog.
 */
public class PauseDialog extends Table {

  private Skin skin;
  private DialogContext ctx;

  private Table contentTable;
  private Table mainMenu;
  private Table settingsMenu;

  private PauseDialog(Skin skin, DialogContext ctx) {
    this.skin = skin;
    this.ctx = ctx;
    createActors();
  }

  private void createActors() {
    contentTable = new Table(skin);
    contentTable.setBackground("window_background_big");

    mainMenu = createMainView(ctx);
    settingsMenu = createSettingsView(ctx);

    contentTable.add(mainMenu);
    contentTable.pack();

    // Add the content table as a cell so this Table's pref size reflects the content.
    this.add(contentTable);
    this.pack();
  }

  /**
   * Shows the pause menu dialog for the given target entity IDs.
   *
   * @param targetIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showPauseDialog(int... targetIds) {
    DialogContext ctx = DialogContext.builder().type(DialogType.DefaultTypes.PAUSE_MENU).build();

    UIComponent ui = DialogFactory.show(ctx, targetIds);

    // Register callback
    ui.registerCallback(DialogContextKeys.ON_RESUME, data -> UIUtils.closeDialog(ui, true, true));
    ui.registerCallback(DialogContextKeys.ON_QUIT, data -> Game.exit("Quit from pause menu"));

    return ui;
  }

  /**
   * Builds a pause menu from the given dialog context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and confirmation callback
   * @return A fully configured pause menu or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    return new BaseContainerUI(new PauseDialog(UIUtils.defaultSkin(), ctx));
  }

  private Table createMainView(DialogContext ctx) {
    Label label =
        Scene2dElementFactory.createLabel(
            "PAUSED", FontSpec.of("fonts/Roboto-Bold.ttf", 48, Color.BLACK));
    TextButton resumeBtn = Scene2dElementFactory.createButton("Resume", "clean-green", 32);
    TextButton settingsBtn =
        Scene2dElementFactory.createButton("Settings", "clean-blue-outline", 32);
    TextButton quitBtn =
        Scene2dElementFactory.createButton("Quit to Desktop", "clean-red-outline", 32);

    resumeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(ctx.dialogId(), DialogContextKeys.ON_RESUME)
                .accept(null);
            Sounds.play(CoreSounds.INTERFACE_DIALOG_CLOSED);
          }
        });
    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            showSettings();
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
          }
        });
    quitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(ctx.dialogId(), DialogContextKeys.ON_QUIT)
                .accept(null);
            Sounds.play(CoreSounds.INTERFACE_DIALOG_CLOSED);
          }
        });

    Table menu = new Table();
    menu.add(label).padBottom(30).align(Align.center).row();
    menu.add(resumeBtn).width(300).align(Align.center).padBottom(10).row();
    menu.add(settingsBtn).width(300).align(Align.center).padBottom(70).row();
    menu.add(quitBtn).width(300).align(Align.center).padBottom(15).row();
    return menu;
  }

  private Table createSettingsView(DialogContext ctx) {
    Label label =
        Scene2dElementFactory.createLabel(
            "SETTINGS", FontSpec.of("fonts/Roboto-Bold.ttf", 48, Color.BLACK));
    TextButton backBtn = Scene2dElementFactory.createButton("Back", "clean-green", 32);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            showMainView();
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
          }
        });
    List<Actor> settingsActors = new ArrayList<>();

    ClientSettings.getSettings()
        .forEach(
            (s, setting) -> {
              settingsActors.add(setting.toUIActor());
            });

    Table menu = new Table();
    menu.add(label).padBottom(15).align(Align.center).row();

    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(5).row();

    Table settingsTable = new Table();
    settingsActors.forEach(
        actor -> {
          actor.addListener(
              new InputListener() {
                @Override
                public void enter(
                    InputEvent event, float x, float y, int pointer, Actor fromActor) {
                  if (fromActor != null && fromActor.isDescendantOf(actor) || pointer != -1) return;
                  Sounds.play(CoreSounds.INTERFACE_ITEM_HOVERED, 1, 0.6f);
                  super.enter(event, x, y, pointer, fromActor);
                }
              });
          settingsTable.add(actor).width(500).align(Align.center).pad(0, 10, 20, 10).row();
        });

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(settingsTable, false, true);
    scrollPane.setFlickScroll(false);
    ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle(scrollPane.getStyle());
    style.background = null;
    style.corner = null;
    scrollPane.setStyle(style);
    menu.add(scrollPane).width(550).height(400).align(Align.center).row();

    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(5).row();
    menu.add(backBtn).width(300).align(Align.center).padTop(15).padBottom(15).row();
    return menu;
  }

  private void showMainView() {
    contentTable.clearChildren();
    contentTable.add(mainMenu);
    contentTable.pack();
    this.pack();
  }

  private void showSettings() {
    contentTable.clearChildren();
    contentTable.add(settingsMenu);
    contentTable.pack();
    this.pack();
  }
}
