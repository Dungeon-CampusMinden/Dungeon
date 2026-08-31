package feature.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.game.HostSession;
import engine.game.PreRunConfiguration;
import engine.language.Translation;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.tracking.TrackingRuntime;
import engine.utils.BaseContainerUI;
import engine.utils.FontSpec;
import engine.utils.NetworkUtils;
import engine.utils.Scene2dElementFactory;
import engine.utils.settings.ClientSettings;
import feature.achievements.AchievementManager;
import feature.achievements.AchievementMenuView;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import feature.questlog.QuestLogUI;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-private builder for the pause menu.
 *
 * <p>Creates a simple pause dialog.
 */
public class PauseDialog extends Table {

  private static final String T_PAUSED = "paused";
  private static final String T_RESUME = "resume";
  private static final String T_QUESTLOG = "questlog";
  private static final String T_ACHIEVEMENTS = "achievements";
  private static final String T_SETTINGS = "settings";
  private static final String T_QUIT_TO_DESKTOP = "quit_to_desktop";
  private static final String T_BACK = "back";
  private static final String T_YOU = "you";
  private static final String T_SERVER_STATUS = "server_status";
  private static final String T_SERVER_RUNNING = "server_running";
  private static final String T_SERVER_STOPPED = "server_stopped";
  private static final String T_TRACKING_BACKEND_STATUS = "tracking_backend_status";
  private static final String T_TRACKING_BACKEND_CHECKING = "tracking_backend_checking";
  private static final String T_TRACKING_BACKEND_REACHABLE = "tracking_backend_reachable";
  private static final String T_TRACKING_BACKEND_UNREACHABLE = "tracking_backend_unreachable";
  private static final String T_TRACKING_BACKEND_NOT_CONFIGURED = "tracking_backend_not_configured";
  private static final String T_PLAYERS_CAN_CONNECT_VIA = "players_can_connect_via";
  private static final Translation trans = new Translation("dialog.pause_dialog");

  private Skin skin;
  private DialogContext ctx;

  private Table contentTable;
  private Table mainMenu;
  private Table settingsMenu;
  private Table achievementsMenu;

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
    achievementsMenu = createAchievementsView();

    contentTable.add(mainMenu);
    contentTable.pack();

    // Add the content table as a cell so this Table's pref size reflects the content.
    this.add(contentTable);
    this.pack();
  }

  /**
   * Shows the pause menu dialog for the given target entity.
   *
   * @param caller the entity for which the pause menu should be shown.
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showPauseDialog(Entity caller) {
    boolean isInInput = Game.stage().map(s -> s.getKeyboardFocus() != null).orElse(false);
    if (isInInput) return null;

    // Find if the player has any open pause menu dialog already:
    boolean hasClosed =
        caller
            .fetch(UIComponent.class)
            .map(
                uic -> {
                  if (uic.dialogContext().dialogType() == DialogType.DefaultTypes.PAUSE_MENU) {
                    UIUtils.closeDialog(uic);
                    return true;
                  }
                  return false;
                })
            .orElse(false);

    if (hasClosed) return null;

    DialogContext ctx = DialogContext.builder().type(DialogType.DefaultTypes.PAUSE_MENU).build();
    ctx.owner(caller.id());

    UIComponent ui = DialogFactory.show(ctx, caller.id());

    // Register callback
    ui.registerCallback(
        DialogContextKeys.ON_RESUME,
        data -> {
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(
        DialogContextKeys.ON_QUIT,
        data -> {
          Game.exit("Quit from pause menu");
        });
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
            trans.text(T_PAUSED), FontSpec.of("fonts/Roboto-Bold.ttf", 48, Color.BLACK));
    TextButton resumeBtn = Scene2dElementFactory.createButton(trans.text(T_RESUME), "green", 32);
    TextButton questlogBtn =
        Scene2dElementFactory.createButton(trans.text(T_QUESTLOG), "blue-outline", 32);
    TextButton achievementsBtn =
        Scene2dElementFactory.createButton(trans.text(T_ACHIEVEMENTS), "blue-outline", 32);
    TextButton settingsBtn =
        Scene2dElementFactory.createButton(trans.text(T_SETTINGS), "blue-outline", 32);
    TextButton quitBtn =
        Scene2dElementFactory.createButton(trans.text(T_QUIT_TO_DESKTOP), "red-outline", 32);

    resumeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Game.player().orElseThrow().fetch(UIComponent.class).ifPresent(UIUtils::closeDialog);
            Sounds.play(CoreSounds.INTERFACE_DIALOG_CLOSED);
          }
        });
    questlogBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Entity player = Game.player().orElseThrow();
            player.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog);
            QuestLogUI.requestQuestLog(player);
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
          }
        });
    achievementsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            showAchievements();
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
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
            Game.exit("Quit from pause menu");
            Sounds.play(CoreSounds.INTERFACE_DIALOG_CLOSED);
          }
        });

    Table menu = new Table();
    menu.add(label).padBottom(30).align(Align.center).row();
    menu.add(resumeBtn).width(300).align(Align.center).padBottom(10).row();
    menu.add(questlogBtn).width(300).align(Align.center).padBottom(10).row();
    if (AchievementManager.isAvailable()) {
      menu.add(achievementsBtn).width(300).align(Align.center).padBottom(10).row();
    }
    menu.add(settingsBtn).width(300).align(Align.center).padBottom(70).row();
    menu.add(quitBtn).width(300).align(Align.center).padBottom(15).row();

    if (PreRunConfiguration.multiplayerEnabled()) {
      menu.add(Scene2dElementFactory.createHorizontalDivider()).width(300).padBottom(8).row();
      menu.add(createServerStatusSection()).width(300).align(Align.left).padBottom(5).row();
    }

    return menu;
  }

  private Table createAchievementsView() {
    TextButton backBtn = Scene2dElementFactory.createButton(trans.text(T_BACK), "green", 32);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            showMainView();
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
          }
        });
    return AchievementMenuView.build(backBtn);
  }

  /**
   * Builds the server-status section shown at the bottom of the pause menu: the local player name
   * and the connection address, plus, when this client is hosting, the live server status and the
   * addresses other players can use to connect.
   *
   * @return the populated server-status section
   */
  private Table createServerStatusSection() {
    Table section = new Table();

    String player = PreRunConfiguration.username();
    int port = PreRunConfiguration.networkPort();

    section.add(statusLabel(trans.text(T_YOU, player))).left().row();

    if (HostSession.isHosting()) {
      String serverStatus =
          HostSession.isServerRunning()
              ? trans.text(T_SERVER_RUNNING)
              : trans.text(T_SERVER_STOPPED);
      section.add(statusLabel(trans.text(T_SERVER_STATUS, serverStatus))).left().padTop(0).row();
      section.add(createTrackingBackendStatus()).left().padTop(0).row();
      section.add(statusLabel(trans.text(T_PLAYERS_CAN_CONNECT_VIA, port))).left().padTop(0).row();
      for (String ip : NetworkUtils.localIpAddresses()) {
        section.add(statusLabel(ip)).left().row();
      }
    }

    return section;
  }

  private RichLabel createTrackingBackendStatus() {
    var health = TrackingRuntime.backendHealth();
    String initialStatus =
        health.isPresent()
            ? trans.text(T_TRACKING_BACKEND_CHECKING)
            : trans.text(T_TRACKING_BACKEND_NOT_CONFIGURED);
    RichLabel label = statusLabel(trans.text(T_TRACKING_BACKEND_STATUS, initialStatus));
    health.ifPresent(
        result ->
            result.thenAccept(
                reachable ->
                    Gdx.app.postRunnable(
                        () -> {
                          String status =
                              reachable
                                  ? trans.text(T_TRACKING_BACKEND_REACHABLE)
                                  : trans.text(T_TRACKING_BACKEND_UNREACHABLE);
                          label.setText(
                              statusMarkup(trans.text(T_TRACKING_BACKEND_STATUS, status)));
                        })));
    return label;
  }

  private RichLabel statusLabel(String text) {
    return new RichLabel(statusMarkup(text));
  }

  private String statusMarkup(String text) {
    return "[color=#555555][size=18]" + text;
  }

  private Table createSettingsView(DialogContext ctx) {
    Label label =
        Scene2dElementFactory.createLabel(
            trans.text(T_SETTINGS), FontSpec.of("fonts/Roboto-Bold.ttf", 48, Color.BLACK));
    TextButton backBtn = Scene2dElementFactory.createButton(trans.text(T_BACK), "green", 32);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            showMainView();
            Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
          }
        });
    List<Actor> settingsActors = new ArrayList<>();

    ClientSettings.getSettings(true)
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

  private void showAchievements() {
    achievementsMenu = createAchievementsView();
    contentTable.clearChildren();
    contentTable.add(achievementsMenu);
    contentTable.pack();
    this.pack();
  }
}
