package engine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import engine.Game;
import engine.language.Language;
import engine.language.Translation;
import engine.network.ConnectionListener;
import engine.network.client.CharacterOptionsClient;
import engine.network.client.ClientConnectionConfig;
import engine.network.messages.s2c.CharacterOptions;
import engine.network.messages.s2c.ConnectReject;
import engine.tracking.TrackingRuntime;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.logging.DungeonLogger;
import engine.utils.settings.ClientSettings;
import feature.achievements.AchievementManager;
import feature.achievements.AchievementMenuView;
import feature.credits.CreditsDefinition;
import feature.credits.CreditsRepository;
import feature.credits.CreditsView;
import feature.entities.CharacterClass;
import feature.hud.UIUtils;
import feature.hud.dialogs.ClientConnectionDialog;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The main menu shown before the engine {@link GameLoop} starts.
 *
 * <p>It is a standalone libGDX {@link com.badlogic.gdx.Screen} with its own {@link Stage}, rendered
 * inside the already-running application. When the player chooses an option it transitions to the
 * engine game by switching the active screen via {@link GameLoop#startGame()}.
 *
 * <p>The {@link GameStarter#title() game name} is rendered big above the menu panel and tinted with
 * the starter's {@link GameStarter#accentColor() accent color}. An optional {@link
 * GameStarter#backgroundImage() background image} is scaled to cover the whole screen.
 */
public class MainMenuScreen extends ScreenAdapter {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MainMenuScreen.class);

  private static final String LOCAL_SERVER_HOST = "127.0.0.1";
  private static final Duration SERVER_START_TIMEOUT = Duration.ofSeconds(30);
  private static final String TITLE_FONT = "fonts/Roboto-Bold.ttf";
  private static final float BUTTON_WIDTH = 360f;

  private static final int TITLE_SIZE = 64;
  private static final Color PANEL_TEXT_COLOR = Color.BLACK;
  private static final Color ERROR_COLOR = new Color(0.6f, 0f, 0f, 1f);
  private static final float PRIVACY_DIALOG_WIDTH = 740f;
  private static final Object DELETE_TRACKING_DATA = new Object();
  // Darkens the background image so the title and panel stay readable on any image.
  private static final Color BACKGROUND_TINT = new Color(0.5f, 0.5f, 0.5f, 1f);

  private static final String T_HOST = "host";
  private static final String T_CONTINUE = "continue";
  private static final String T_JOIN = "join";
  private static final String T_LEVEL_EDITOR = "level_editor";
  private static final String T_ACHIEVEMENTS = "achievements";
  private static final String T_CREDITS = "credits";
  private static final String T_SETTINGS = "settings";
  private static final String T_EXIT = "exit";
  private static final String T_BACK = "back";
  private static final String T_CONFIRM = "confirm";
  private static final String T_PLAYER_NAME = "player_name";
  private static final String T_SERVER_ADDRESS = "server_address";
  private static final String T_PORT = "port";
  private static final String T_INVALID_NAME = "invalid_name";
  private static final String T_INVALID_CONNECTION = "invalid_connection";
  private static final String T_STARTING_SERVER = "starting_server";
  private static final String T_STOPPING_SERVER = "stopping_server";
  private static final String T_SERVER_STOP_FAILED = "server_stop_failed";
  private static final String T_SERVER_START_FAILED = "server_start_failed";
  private static final String T_SERVER_TIMEOUT = "server_timeout";
  private static final String T_NEW_GAME_OVERWRITE_TITLE = "new_game_overwrite_title";
  private static final String T_NEW_GAME_OVERWRITE_MESSAGE = "new_game_overwrite_message";
  private static final String T_CANCEL = "cancel";
  private static final String T_CHARACTER_SELECTION_TITLE = "character_selection_title";
  private static final String T_REFRESHING_CHARACTERS = "refreshing_characters";
  private static final String T_CONNECTION_REFUSED = "connection_refused";
  private static final String T_CONNECTION_TIMEOUT = "connection_timeout";
  private static final String T_CONNECTION_FAILED = "connection_failed";
  private static final String T_NAME_IN_USE = "name_in_use";
  private static final String T_INCOMPATIBLE_VERSION = "incompatible_version";
  private static final String T_SERVER_FULL = "server_full";
  private static final String T_CHARACTER_UNAVAILABLE = "character_unavailable";
  private static final String T_CONNECTION_REJECTED = "connection_rejected";
  private static final Translation trans = new Translation("main_menu");

  private final GameStarter starter;
  private final Runnable levelEditorLauncher;

  /** Identifies which sub-view is currently shown, so it can be re-shown after a rebuild. */
  private enum View {
    MAIN,
    SETTINGS,
    ACHIEVEMENTS,
    CREDITS,
    HOST_NAME,
    JOIN,
    CONNECTING,
    CHARACTER_SELECT
  }

  private Stage stage;
  private Skin skin;
  private Texture backgroundTexture;
  private Table content;
  private Table mainView;
  private Table settingsView;
  private Table achievementsView;
  private Table hostNameView;
  private Table joinView;
  private Table characterSelectionView;
  private CharacterCarousel characterCarousel;
  private View activeView = View.MAIN;
  private boolean showLevelEditorOption;

  private TextField hostNameField;
  private Label hostTitleLabel;
  private Label hostStatusLabel;
  private TextButton hostConfirmButton;
  private TextButton hostBackButton;

  private TextField joinNameField;
  private TextField joinHostField;
  private TextField joinPortField;
  private Label joinStatusLabel;
  private TextButton joinConfirmButton;
  private TextButton joinBackButton;
  private Label characterSelectionStatusLabel;
  private TextButton characterConfirmButton;
  private TextButton characterBackButton;

  private volatile boolean launching = false;
  private boolean continueGameSelected;
  private volatile long pregameGeneration;
  private volatile long hostGeneration;
  private volatile boolean handingOffToGame;
  private volatile boolean admissionStarting;
  private volatile boolean admissionCleanupPending;
  private boolean discoveryInProgress;
  private float characterRefreshElapsed;
  private boolean hostCleanupPending;
  private boolean selectionHosted;
  private boolean disposed;
  private String selectionPlayerName;
  private String selectionHost;
  private int selectionPort;
  private View selectionBackView = View.JOIN;
  private String selectionStatusMessage = "";
  private List<CharacterOptions.Option> currentCharacterOptions = List.of();
  private CharacterClass selectedCharacter;
  private CharacterOptionsClient optionsClient;
  private volatile ConnectionListener admissionListener;
  private ConnectReject.Reason admissionRejection;
  private volatile ServerProcess pendingServer;
  private final Object networkStartLock = new Object();

  /**
   * Rebuilds the localized views on the next frame whenever the language changes. Deferring via
   * {@link Gdx#app} keeps the rebuild out of the settings dropdown's event dispatch.
   */
  private final Consumer<Language> languageChangeListener =
      language -> Gdx.app.postRunnable(this::rebuildViews);

  /**
   * Creates the main menu screen for the given starter.
   *
   * @param starter the game integration used to configure and launch the game
   * @param levelEditorLauncher applies the level-editor configuration, or {@code null} if the level
   *     editor is not available
   */
  public MainMenuScreen(GameStarter starter, Runnable levelEditorLauncher) {
    this.starter = starter;
    this.levelEditorLauncher = levelEditorLauncher;
  }

  @Override
  public void show() {
    int width = Game.windowWidth() > 0 ? Game.windowWidth() : PreRunConfiguration.windowWidth();
    int height = Game.windowHeight() > 0 ? Game.windowHeight() : PreRunConfiguration.windowHeight();
    stage = new Stage(new ScalingViewport(Scaling.stretch, width, height), new SpriteBatch());
    stage
        .getRoot()
        .addListener(
            new InputListener() {
              @Override
              public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F4 && activeView == View.MAIN) {
                  toggleLevelEditorOption();
                  return true;
                }
                return false;
              }
            });
    Gdx.input.setInputProcessor(stage);
    skin = UIUtils.defaultSkin();

    content = new Table(skin);
    content.setBackground("window_background_big");
    mainView = buildMainView();
    settingsView = buildSettingsView();
    achievementsView = buildAchievementsView();
    hostNameView = buildHostNameView();
    joinView = buildJoinView();
    characterSelectionView = buildCharacterSelectionView();

    Stack root = new Stack();
    root.setFillParent(true);
    addBackgroundIfPresent(root);

    Table foreground = new Table();
    foreground.add(buildTitleBanner()).padBottom(30).row();
    foreground.add(content);
    root.add(foreground);

    stage.addActor(root);

    Game.localization().registerLanguageChangeListener(languageChangeListener);

    showMainView();
  }

  private void showStartupConsent(Runnable afterDecision) {
    Optional<GameStarter.StartupConsent> optionalConsent = starter.startupConsent();
    if (optionalConsent.isEmpty()) {
      afterDecision.run();
      return;
    }

    GameStarter.StartupConsent consent = optionalConsent.get();
    Dialog dialog =
        new Dialog("", skin, "no-title") {
          @Override
          protected void result(Object object) {
            consent.decision().accept(Boolean.TRUE.equals(object));
            afterDecision.run();
          }
        };
    DialogDesign.setDialogDefaults(dialog, consent.title(), PANEL_TEXT_COLOR);
    float width = privacyDialogWidth();
    addPrivacySummary(dialog, consent.summary(), width);
    addPrivacyDetails(dialog, consent.message(), width);
    dialog.getButtonTable().defaults().minWidth(220).height(48).space(12);
    dialog.button(new TextButton(consent.declineLabel(), skin, "blue-outline"), Boolean.FALSE);
    dialog.button(new TextButton(consent.acceptLabel(), skin, "green"), Boolean.TRUE);
    dialog.show(stage);
  }

  /**
   * Keeps the short decision summary visible while the detailed notice scrolls below it.
   *
   * @param dialog dialog receiving the summary
   * @param summary short explanation shown above the scroll area
   * @param width available content width
   */
  private void addPrivacySummary(Dialog dialog, String summary, float width) {
    Label label =
        Scene2dElementFactory.createLabel(summary, FontSpec.of(TITLE_FONT, 21, PANEL_TEXT_COLOR));
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    dialog.getContentTable().add(label).width(width - 32).pad(8, 16, 14, 16).left().row();
    dialog
        .getContentTable()
        .add(Scene2dElementFactory.createHorizontalDivider())
        .width(width)
        .padBottom(10)
        .row();
  }

  private void addPrivacyDetails(Dialog dialog, String message, float width) {
    float textWidth = width - 48;
    Table content = new Table();
    content.top().left();
    String[] sections = message.split("\\R\\s*\\R");
    for (int index = 0; index < sections.length; index++) {
      String[] parts = sections[index].trim().split("\\R", 2);
      Label heading =
          Scene2dElementFactory.createLabel(
              parts[0], FontSpec.of(TITLE_FONT, 19, PANEL_TEXT_COLOR));
      heading.setWrap(true);
      heading.setAlignment(Align.topLeft);
      content.add(heading).width(textWidth).padBottom(5).left().row();
      if (parts.length > 1) {
        Label body =
            Scene2dElementFactory.createLabel(parts[1], DialogDesign.DIALOG_FONT_SPEC_NORMAL);
        body.setWrap(true);
        body.setAlignment(Align.topLeft);
        content.add(body).width(textWidth).padBottom(18).left().row();
      }
    }
    ScrollPane pane = Scene2dElementFactory.createScrollPane(content, false, true);
    pane.setFadeScrollBars(false);
    pane.setScrollbarsVisible(true);
    pane.setScrollbarsOnTop(false);
    dialog.getContentTable().add(pane).width(width).height(315).padBottom(14).row();
  }

  private float privacyDialogWidth() {
    return Math.min(PRIVACY_DIALOG_WIDTH, stage.getViewport().getWorldWidth() - 80f);
  }

  private void addBackgroundIfPresent(Stack root) {
    Optional<String> background = starter.backgroundImage();
    if (background.isEmpty()) {
      return;
    }
    String path = background.get();
    try {
      backgroundTexture = new Texture(Gdx.files.internal(path));
      backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      Image image = new Image(backgroundTexture);
      image.setScaling(Scaling.fill);
      image.setColor(BACKGROUND_TINT);
      root.add(image);
    } catch (RuntimeException e) {
      LOGGER.warn("Could not load main menu background '{}': {}", path, e.getMessage());
    }
  }

  /**
   * Builds the big game-name banner using the configured starter accent color.
   *
   * @return title actor rendered above the menu panel
   */
  private Actor buildTitleBanner() {
    String richTitle = RichLabel.toRichText(starter.title());
    RichLabel title =
        new RichLabel(richTitle, FontSpec.of(TITLE_FONT, TITLE_SIZE, starterAccent()));
    title.setWrap(false);
    title.setAlignment(Align.center);
    return title;
  }

  private Color starterAccent() {
    Color color = starter.accentColor();
    return color == null ? Color.WHITE : color;
  }

  private Table buildMainView() {
    String hostLabel = starter.hostActionLabel().orElseGet(() -> trans.text(T_HOST));
    TextButton hostButton = menuButton(hostLabel, "green", this::showHostNameView);
    TextButton continueButton = menuButton(trans.text(T_CONTINUE), "green", this::continueGame);
    TextButton joinButton = menuButton(trans.text(T_JOIN), "blue-outline", this::joinGame);
    TextButton levelEditorButton =
        menuButton(trans.text(T_LEVEL_EDITOR), "blue-outline", this::startLevelEditor);
    TextButton settingsButton =
        menuButton(trans.text(T_SETTINGS), "blue-outline", this::showSettingsView);
    TextButton achievementsButton =
        menuButton(trans.text(T_ACHIEVEMENTS), "blue-outline", this::showAchievementsView);
    TextButton creditsButton =
        menuButton(trans.text(T_CREDITS), "blue-outline", this::showCreditsView);
    TextButton exitButton =
        menuButton(trans.text(T_EXIT), "red-outline", () -> Game.exit("Exit from main menu"));

    Table menu = new Table();
    if (starter.continueAvailable()) {
      menu.add(continueButton).width(BUTTON_WIDTH).padBottom(12).row();
    }
    menu.add(hostButton).width(BUTTON_WIDTH).padBottom(12).row();
    if (starter.showJoinButton()) {
      menu.add(joinButton).width(BUTTON_WIDTH).padBottom(12).row();
    }
    if (showLevelEditorOption && levelEditorLauncher != null) {
      menu.add(levelEditorButton).width(BUTTON_WIDTH).padBottom(12).row();
    }
    if (AchievementManager.isAvailable()) {
      menu.add(achievementsButton).width(BUTTON_WIDTH).padBottom(12).row();
    }
    if (starter.creditsRoomId().flatMap(CreditsRepository::load).isPresent()) {
      menu.add(creditsButton).width(BUTTON_WIDTH).padBottom(12).row();
    }
    menu.add(settingsButton).width(BUTTON_WIDTH).padBottom(12).row();
    menu.add(exitButton).width(BUTTON_WIDTH).row();
    return menu;
  }

  private Table buildHostNameView() {
    hostTitleLabel =
        label(starter.hostActionLabel().orElseGet(() -> trans.text(T_HOST)), 36, PANEL_TEXT_COLOR);
    Label nameLabel = label(trans.text(T_PLAYER_NAME), 22, PANEL_TEXT_COLOR);
    hostNameField = Scene2dElementFactory.createTextField(ClientConnectionDialog.defaultUsername());
    hostStatusLabel = label("", 18, ERROR_COLOR);
    hostConfirmButton = menuButton(trans.text(T_CONFIRM), "green", this::confirmHostName);
    hostBackButton = menuButton(trans.text(T_BACK), "red-outline", this::cancelHostSetup);

    Table buttons = new Table();
    buttons.add(hostBackButton).width(175).padRight(10);
    buttons.add(hostConfirmButton).width(175);

    Table menu = new Table();
    menu.add(hostTitleLabel).padBottom(20).align(Align.center).row();
    menu.add(nameLabel).align(Align.left).padBottom(5).row();
    menu.add(hostNameField).width(BUTTON_WIDTH).padBottom(8).row();
    menu.add(hostStatusLabel).width(BUTTON_WIDTH).minHeight(24).padBottom(8).row();
    menu.add(buttons).align(Align.center).row();
    return menu;
  }

  private Table buildAchievementsView() {
    TextButton backButton = menuButton(trans.text(T_BACK), "green", this::showMainView);
    return AchievementMenuView.build(backButton);
  }

  private void showCreditsView() {
    Optional<CreditsDefinition> definition =
        starter.creditsRoomId().flatMap(CreditsRepository::load);
    if (definition.isEmpty()) return;

    activeView = View.CREDITS;
    swapContent(
        CreditsView.buildMenuView(definition.get(), skin, trans.text(T_BACK), this::showMainView));
  }

  private Table buildJoinView() {
    Label title = label(trans.text(T_JOIN), 36, PANEL_TEXT_COLOR);
    Label nameLabel = label(trans.text(T_PLAYER_NAME), 22, PANEL_TEXT_COLOR);
    Label hostLabel = label(trans.text(T_SERVER_ADDRESS), 22, PANEL_TEXT_COLOR);
    Label portLabel = label(trans.text(T_PORT), 22, PANEL_TEXT_COLOR);

    joinNameField = Scene2dElementFactory.createTextField(ClientConnectionDialog.defaultUsername());
    joinHostField = Scene2dElementFactory.createTextField(ClientConnectionConfig.DEFAULT_HOST);
    joinPortField =
        Scene2dElementFactory.createTextField(Integer.toString(PreRunConfiguration.networkPort()));
    joinStatusLabel = label("", 18, ERROR_COLOR);
    joinConfirmButton = menuButton(trans.text(T_CONFIRM), "green", this::confirmJoin);
    joinBackButton = menuButton(trans.text(T_BACK), "red-outline", this::cancelJoinSetup);

    Table buttons = new Table();
    buttons.add(joinBackButton).width(175).padRight(10);
    buttons.add(joinConfirmButton).width(175);

    Table menu = new Table();
    menu.add(title).padBottom(20).align(Align.center).row();
    menu.add(nameLabel).align(Align.left).padBottom(5).row();
    menu.add(joinNameField).width(BUTTON_WIDTH).padBottom(8).row();
    menu.add(hostLabel).align(Align.left).padBottom(5).row();
    menu.add(joinHostField).width(BUTTON_WIDTH).padBottom(8).row();
    menu.add(portLabel).align(Align.left).padBottom(5).row();
    menu.add(joinPortField).width(BUTTON_WIDTH).padBottom(8).row();
    menu.add(joinStatusLabel).width(BUTTON_WIDTH).minHeight(24).padBottom(8).row();
    menu.add(buttons).align(Align.center).row();
    return menu;
  }

  private Table buildCharacterSelectionView() {
    Label title = label(trans.text(T_CHARACTER_SELECTION_TITLE), 36, PANEL_TEXT_COLOR);
    characterCarousel = new CharacterCarousel(skin);
    characterCarousel.onSelectionChanged(this::updateCharacterConfirmButton);
    characterSelectionStatusLabel = label("", 18, ERROR_COLOR);
    characterSelectionStatusLabel.setWrap(true);
    characterSelectionStatusLabel.setAlignment(Align.center);
    characterConfirmButton = menuButton(trans.text(T_CONFIRM), "green", this::beginAdmission);
    characterBackButton =
        menuButton(trans.text(T_BACK), "red-outline", this::backFromCharacterSelect);

    Table buttons = new Table();
    buttons.add(characterBackButton).width(175).padRight(10);
    buttons.add(characterConfirmButton).width(175);

    Table menu = new Table();
    menu.add(title).padBottom(8).align(Align.center).row();
    menu.add(characterCarousel).padBottom(6).row();
    menu.add(characterSelectionStatusLabel)
        .width(BUTTON_WIDTH + 80)
        .minHeight(24)
        .padBottom(8)
        .row();
    menu.add(buttons).align(Align.center).row();
    return menu;
  }

  private Table buildSettingsView() {
    Label title = label(trans.text(T_SETTINGS), 48, PANEL_TEXT_COLOR);
    TextButton backButton = menuButton(trans.text(T_BACK), "green", this::showMainView);

    Table settingsTable = new Table();
    ClientSettings.getSettings(false)
        .forEach(
            (key, setting) ->
                settingsTable.add(setting.toUIActor()).width(500).pad(0, 10, 20, 10).row());
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(settingsTable, false, true);

    Table menu = new Table();
    menu.add(title).padBottom(15).align(Align.center).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(5).row();
    menu.add(scrollPane).width(550).height(380).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(5).row();
    starter
        .trackingSettings()
        .ifPresent(
            settings ->
                menu.add(menuButton(settings.title(), "blue-outline", this::showTrackingSettings))
                    .width(300)
                    .padTop(15)
                    .row());
    menu.add(backButton).width(300).padTop(15).padBottom(15).row();
    return menu;
  }

  private Label label(String text, int size, Color color) {
    return Scene2dElementFactory.createLabel(text, FontSpec.of(TITLE_FONT, size, color));
  }

  private TextButton menuButton(String text, String style, Runnable action) {
    TextButton button = new TextButton(text, skin, style);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }

  private void showMainView() {
    activeView = View.MAIN;
    swapContent(mainView);
  }

  private void toggleLevelEditorOption() {
    if (levelEditorLauncher == null) {
      return;
    }
    showLevelEditorOption = !showLevelEditorOption;
    mainView = buildMainView();
    showMainView();
  }

  private void startLevelEditor() {
    if (launching || levelEditorLauncher == null) {
      return;
    }
    launching = true;
    levelEditorLauncher.run();
    GameLoop.startGame();
  }

  private void showSettingsView() {
    activeView = View.SETTINGS;
    swapContent(settingsView);
  }

  private void showTrackingSettings() {
    Optional<GameStarter.TrackingSettings> optionalSettings = starter.trackingSettings();
    if (optionalSettings.isEmpty()) return;

    GameStarter.TrackingSettings settings = optionalSettings.get();
    Dialog dialog =
        new Dialog("", skin, "no-title") {
          @Override
          protected void result(Object object) {
            if (object instanceof Boolean decision) {
              settings.decision().accept(decision);
              showMessageDialog(
                  settings.title(),
                  decision ? settings.enabledMessage() : settings.disabledMessage());
            } else if (object == DELETE_TRACKING_DATA) {
              showTrackingDeleteConfirmation(settings);
            }
          }
        };
    DialogDesign.setDialogDefaults(dialog, settings.title(), PANEL_TEXT_COLOR);
    float width = privacyDialogWidth();
    addPrivacySummary(dialog, settings.summary(), width);
    Label status =
        Scene2dElementFactory.createLabel(
            settings.status(), FontSpec.of(TITLE_FONT, 20, PANEL_TEXT_COLOR));
    status.setWrap(true);
    status.setAlignment(Align.topLeft);
    dialog.getContentTable().add(status).width(width - 32).padBottom(12).left().row();
    addPrivacyDetails(dialog, settings.message(), width);
    dialog.getButtonTable().defaults().width(BUTTON_WIDTH).space(8);
    dialog.button(new TextButton(settings.disableLabel(), skin, "blue-outline"), Boolean.FALSE);
    dialog.button(new TextButton(settings.enableLabel(), skin, "green"), Boolean.TRUE);
    dialog.getButtonTable().row();
    dialog.button(
        new TextButton(settings.deleteLabel(), skin, "red-outline"), DELETE_TRACKING_DATA);
    dialog.button(new TextButton(trans.text(T_BACK), skin, "blue-outline"), null);
    dialog.show(stage);
  }

  private void showTrackingDeleteConfirmation(GameStarter.TrackingSettings settings) {
    Dialog confirmation =
        new Dialog("", skin, "no-title") {
          @Override
          protected void result(Object object) {
            if (!Boolean.TRUE.equals(object)) return;
            boolean deleted = settings.deleteLocalData().getAsBoolean();
            showMessageDialog(
                settings.title(),
                deleted ? settings.deletedMessage() : settings.deleteFailedMessage());
          }
        };
    DialogDesign.setDialogDefaults(
        confirmation, settings.deleteConfirmationTitle(), PANEL_TEXT_COLOR);
    addWrappedDialogText(confirmation, settings.deleteConfirmationMessage());
    confirmation.button(trans.text(T_CONFIRM), Boolean.TRUE);
    confirmation.button(trans.text(T_CANCEL), Boolean.FALSE);
    confirmation.show(stage);
  }

  private void showMessageDialog(String title, String message) {
    Dialog dialog = new Dialog("", skin, "no-title");
    DialogDesign.setDialogDefaults(dialog, title, PANEL_TEXT_COLOR);
    addWrappedDialogText(dialog, message);
    dialog.button(trans.text(T_CONFIRM), Boolean.TRUE);
    dialog.show(stage);
  }

  private void addWrappedDialogText(Dialog dialog, String message) {
    Label label = Scene2dElementFactory.createLabel(message, DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    dialog
        .getContentTable()
        .add(label)
        .width(Math.min(590f, privacyDialogWidth() - 40f))
        .pad(10)
        .row();
  }

  private void showAchievementsView() {
    activeView = View.ACHIEVEMENTS;
    achievementsView = buildAchievementsView();
    swapContent(achievementsView);
  }

  private void showHostNameView() {
    continueGameSelected = false;
    if (starter.continueAvailable()) {
      showNewGameOverwriteConfirmation();
      return;
    }
    beginNewGame();
  }

  private void continueGame() {
    if (launching) return;
    continueGameSelected = true;
    showStartupConsent(
        () -> {
          hostNameField.setText(PreRunConfiguration.username());
          showHostNameViewInternal();
        });
  }

  private void showNewGameOverwriteConfirmation() {
    Dialog confirmation =
        new Dialog("", skin, "no-title") {
          @Override
          protected void result(Object object) {
            if (Boolean.TRUE.equals(object)) {
              beginNewGame();
            }
          }
        };
    DialogDesign.setDialogDefaults(confirmation, trans.text(T_NEW_GAME_OVERWRITE_TITLE));
    confirmation.text(trans.text(T_NEW_GAME_OVERWRITE_MESSAGE));
    confirmation.button(trans.text(T_CONFIRM), Boolean.TRUE);
    confirmation.button(trans.text(T_CANCEL), Boolean.FALSE);
    confirmation.show(stage);
  }

  /**
   * Starts the new-game setup after the optional tracking decision has been made.
   *
   * <p>The prompt is shown when the player chooses to start, continue, or join a run, instead of
   * opening automatically with the main menu.
   */
  private void beginNewGame() {
    showStartupConsent(
        () -> {
          hostNameField.setText(ClientConnectionDialog.defaultUsername());
          showHostNameViewInternal();
        });
  }

  private void showHostNameViewInternal() {
    activeView = View.HOST_NAME;
    hostTitleLabel.setText(
        continueGameSelected
            ? trans.text(T_CONTINUE)
            : starter.hostActionLabel().orElseGet(() -> trans.text(T_HOST)));
    hostStatusLabel.setText(
        hostCleanupPending
            ? trans.text(T_STOPPING_SERVER)
            : launching ? trans.text(T_STARTING_SERVER) : "");
    setHostControlsDisabled(launching);
    swapContent(hostNameView);
    stage.setKeyboardFocus(hostNameField);
  }

  private void showJoinView() {
    activeView = View.JOIN;
    joinStatusLabel.setText("");
    setJoinControlsDisabled(discoveryInProgress || admissionCleanupPending);
    swapContent(joinView);
    stage.setKeyboardFocus(joinNameField);
  }

  private void showCharacterSelectionView() {
    activeView = View.CHARACTER_SELECT;
    characterCarousel.setOptions(currentCharacterOptions, selectedCharacter);
    characterSelectionStatusLabel.setText(selectionStatusMessage);
    updateCharacterConfirmButton();
    swapContent(characterSelectionView);
    stage.setKeyboardFocus(null);
  }

  private void showConnectingView() {
    activeView = View.CONNECTING;
    Table connecting = new Table();
    connecting
        .add(label(trans.text("connecting_to_server"), 28, PANEL_TEXT_COLOR))
        .padBottom(16)
        .row();
    connecting
        .add(menuButton(trans.text(T_BACK), "red-outline", this::backFromCharacterSelect))
        .width(BUTTON_WIDTH);
    swapContent(connecting);
    stage.setKeyboardFocus(null);
  }

  private void beginCharacterDiscovery(
      String host, int port, String playerName, View returnView, boolean hosted) {
    if (stage == null || handingOffToGame) {
      return;
    }
    cancelOptionsRequest();
    selectionHost = host;
    selectionPort = port;
    selectionPlayerName = playerName;
    selectionBackView = returnView;
    selectionHosted = hosted;
    currentCharacterOptions = List.of();
    selectedCharacter = null;
    selectionStatusMessage = trans.text(T_REFRESHING_CHARACTERS);
    launching = false;
    admissionStarting = false;
    admissionRejection = null;
    characterCarousel.setOptions(List.of(), null);
    boolean validateJoin = !hosted && returnView == View.JOIN;
    if (!validateJoin) {
      showConnectingView();
    }
    requestCharacterOptions(validateJoin);
  }

  private void requestCharacterOptions(boolean validateJoin) {
    if (selectionHost == null || selectionPlayerName == null || stage == null) {
      return;
    }
    cancelOptionsRequest();
    long operation = ++pregameGeneration;
    View requestView = activeView;
    characterRefreshElapsed = 0;
    boolean recoveringDiscovery = currentCharacterOptions.isEmpty();
    discoveryInProgress = true;
    if (validateJoin) {
      setJoinControlsDisabled(true);
      joinStatusLabel.setText(trans.text("connecting_to_server"));
    } else {
      updateCharacterConfirmButton();
      characterSelectionStatusLabel.setText(selectionStatusMessage);
    }

    CharacterOptionsClient request =
        new CharacterOptionsClient(selectionHost, selectionPort, selectionPlayerName);
    optionsClient = request;
    request
        .request()
        .whenComplete(
            (response, error) ->
                postToRenderThread(
                    () -> {
                      if (operation != pregameGeneration
                          || activeView != requestView
                          || stage == null) {
                        request.close();
                        return;
                      }
                      optionsClient = null;
                      discoveryInProgress = false;
                      if (validateJoin) {
                        setJoinControlsDisabled(admissionCleanupPending);
                      }
                      if (error != null) {
                        currentCharacterOptions = List.of();
                        selectionStatusMessage = connectionError(error);
                        if (!selectionHosted) {
                          showJoinView();
                          joinStatusLabel.setText(selectionStatusMessage);
                          return;
                        }
                      } else {
                        currentCharacterOptions = response.characters();
                        selectedCharacter =
                            characterCarousel
                                .selectedOption()
                                .map(CharacterOptions.Option::characterClass)
                                .orElse(selectedCharacter);
                        if (validateJoin || recoveringDiscovery) {
                          selectionStatusMessage = "";
                        }
                      }
                      characterCarousel.setOptions(currentCharacterOptions, selectedCharacter);
                      characterSelectionStatusLabel.setText(selectionStatusMessage);
                      updateCharacterConfirmButton();
                      if (validateJoin || requestView == View.CONNECTING) {
                        if (error == null
                            && currentCharacterOptions.size() == 1
                            && currentCharacterOptions.getFirst().selectable()) {
                          showConnectingView();
                          beginAdmission();
                        } else {
                          showCharacterSelectionView();
                        }
                      }
                    }));
  }

  private boolean isCurrentPregameOperation(long operation) {
    return operation == pregameGeneration
        && (activeView == View.CHARACTER_SELECT || activeView == View.CONNECTING);
  }

  private void cancelOptionsRequest() {
    CharacterOptionsClient request = optionsClient;
    optionsClient = null;
    if (request != null) {
      request.close();
    }
    discoveryInProgress = false;
  }

  private void updateCharacterConfirmButton() {
    if (characterConfirmButton == null) {
      return;
    }
    selectedCharacter =
        characterCarousel == null
            ? null
            : characterCarousel
                .selectedOption()
                .map(CharacterOptions.Option::characterClass)
                .orElse(null);
    boolean selectable =
        characterCarousel != null
            && characterCarousel
                .selectedOption()
                .map(CharacterOptions.Option::selectable)
                .orElse(false);
    characterConfirmButton.setDisabled(!selectable || admissionStarting || admissionCleanupPending);
  }

  private void beginAdmission() {
    if (admissionStarting || admissionCleanupPending) {
      return;
    }
    Optional<CharacterOptions.Option> selected =
        characterCarousel == null ? Optional.empty() : characterCarousel.selectedOption();
    if (selected.isEmpty() || !selected.orElseThrow().selectable()) {
      return;
    }
    cancelOptionsRequest();
    try {
      PreRunConfiguration.username(selectionPlayerName);
    } catch (IllegalArgumentException e) {
      selectionStatusMessage = trans.text(T_INVALID_NAME);
      characterSelectionStatusLabel.setText(selectionStatusMessage);
      return;
    }

    CharacterClass characterClass = selected.orElseThrow().characterClass();
    selectedCharacter = characterClass;
    PreRunConfiguration.multiplayerCharacterClass(characterClass);
    PreRunConfiguration.networkServerAddress(selectionHost);
    PreRunConfiguration.networkPort(selectionPort);
    admissionStarting = true;
    admissionRejection = null;
    selectionStatusMessage = trans.text("connecting_to_server");
    characterSelectionStatusLabel.setText(selectionStatusMessage);
    updateCharacterConfirmButton();
    long operation = ++pregameGeneration;
    ConnectionListener listener = createAdmissionListener(operation);
    admissionListener = listener;
    Thread connector =
        new Thread(() -> startAdmissionClient(operation, listener), "menu-client-connection");
    connector.setDaemon(true);
    connector.start();
    CompletableFuture.delayedExecutor(15, TimeUnit.SECONDS)
        .execute(
            () ->
                postToRenderThread(
                    () -> {
                      if (isCurrentPregameOperation(operation) && admissionStarting) {
                        handleAdmissionFailure(operation, trans.text(T_CONNECTION_TIMEOUT), null);
                      }
                    }));
  }

  private void startAdmissionClient(long operation, ConnectionListener listener) {
    try {
      synchronized (networkStartLock) {
        if (operation != pregameGeneration) {
          return;
        }
        Game.initializeNetwork();
        if (operation != pregameGeneration) {
          Game.network().shutdown("Main-menu connection cancelled");
          return;
        }
        Game.network().addConnectionListener(listener);
        if (operation != pregameGeneration) {
          Game.network().removeConnectionListener(listener);
          Game.network().shutdown("Main-menu connection cancelled");
          return;
        }
        Game.network().start();
        if (operation != pregameGeneration) {
          Game.network().removeConnectionListener(listener);
          Game.network().shutdown("Main-menu connection cancelled");
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn("Could not start the menu connection.", e);
      postToRenderThread(
          () -> {
            if (isCurrentPregameOperation(operation) && admissionStarting) {
              handleAdmissionFailure(operation, connectionError(e), null);
            }
          });
    }
  }

  private ConnectionListener createAdmissionListener(long operation) {
    return new ConnectionListener() {
      @Override
      public void onConnected() {
        if (!isCurrentPregameOperation(operation) || stage == null || !admissionStarting) {
          return;
        }
        admissionStarting = false;
        Game.network().removeConnectionListener(this);
        admissionListener = null;
        try {
          GameLoop.prepareForMenuConnection();
          handingOffToGame = true;
          GameLoop.startGame();
        } catch (RuntimeException e) {
          LOGGER.error("Could not prepare gameplay after menu connection.", e);
          handingOffToGame = false;
          handleAdmissionFailure(
              operation, trans.text(T_CONNECTION_FAILED, rootCauseMessage(e)), null);
        }
      }

      @Override
      public void onDisconnected(String reason) {
        if (handingOffToGame || !isCurrentPregameOperation(operation) || !admissionStarting) {
          return;
        }
        handleAdmissionFailure(
            operation,
            admissionRejection == null
                ? connectionError(reason)
                : rejectionMessage(admissionRejection),
            admissionRejection);
      }

      @Override
      public void onRejected(ConnectReject.Reason reason) {
        if (isCurrentPregameOperation(operation)) {
          admissionRejection = reason;
        }
      }
    };
  }

  private void handleAdmissionFailure(
      long operation, String message, ConnectReject.Reason rejection) {
    if (!isCurrentPregameOperation(operation)) {
      return;
    }
    admissionStarting = false;
    admissionRejection = null;
    selectionStatusMessage = message;
    characterSelectionStatusLabel.setText(message);
    admissionCleanupPending = true;
    if (!selectionHosted && rejection != ConnectReject.Reason.CHARACTER_UNAVAILABLE) {
      showJoinView();
      joinStatusLabel.setText(message);
      setJoinControlsDisabled(true);
    } else if (activeView == View.CONNECTING) {
      showCharacterSelectionView();
    }
    ConnectionListener listener = admissionListener;
    admissionListener = null;
    if (listener != null) {
      Game.network().removeConnectionListener(listener);
    }
    long cleanupGeneration = ++pregameGeneration;
    Thread cleanup =
        new Thread(
            () -> {
              synchronized (networkStartLock) {
                Game.network().shutdown("Main-menu admission failed");
              }
              admissionCleanupPending = false;
              postToRenderThread(
                  () -> {
                    if (stage == null) {
                      return;
                    }
                    if (activeView == View.JOIN) {
                      setJoinControlsDisabled(discoveryInProgress);
                      return;
                    }
                    if (activeView != View.CHARACTER_SELECT) return;
                    updateCharacterConfirmButton();
                    if (cleanupGeneration == pregameGeneration
                        && rejection == ConnectReject.Reason.CHARACTER_UNAVAILABLE) {
                      requestCharacterOptions(false);
                    }
                  });
            },
            "menu-client-cleanup");
    cleanup.setDaemon(true);
    cleanup.start();
  }

  private String rejectionMessage(ConnectReject.Reason reason) {
    return switch (reason) {
      case INVALID_NAME -> trans.text(T_INVALID_NAME);
      case NAME_IN_USE -> trans.text(T_NAME_IN_USE);
      case INCOMPATIBLE_VERSION -> trans.text(T_INCOMPATIBLE_VERSION);
      case SERVER_FULL -> trans.text(T_SERVER_FULL);
      case CHARACTER_UNAVAILABLE -> trans.text(T_CHARACTER_UNAVAILABLE);
      default -> trans.text(T_CONNECTION_REJECTED);
    };
  }

  private String connectionError(Throwable error) {
    Throwable cause = error;
    while (cause != null && cause.getCause() != null) {
      cause = cause.getCause();
    }
    if (cause instanceof ConnectException
        || (cause != null
            && cause.getMessage() != null
            && cause.getMessage().toLowerCase(java.util.Locale.ROOT).contains("refused"))) {
      return trans.text(T_CONNECTION_REFUSED);
    }
    if (cause instanceof UnknownHostException) {
      return trans.text(T_INVALID_CONNECTION);
    }
    if (cause instanceof SocketTimeoutException) {
      return trans.text(T_CONNECTION_TIMEOUT);
    }
    if (cause instanceof CharacterOptionsClient.CharacterOptionsRejectedException rejected) {
      return rejectionMessage(rejected.reason());
    }
    return trans.text(T_CONNECTION_FAILED, cause == null ? "" : cause.getMessage());
  }

  private String connectionError(String reason) {
    if (reason == null || reason.isBlank()) {
      return trans.text(T_CONNECTION_FAILED, "");
    }
    if (reason.toLowerCase(java.util.Locale.ROOT).contains("refused")) {
      return trans.text(T_CONNECTION_REFUSED);
    }
    return trans.text(T_CONNECTION_FAILED, reason);
  }

  private String rootCauseMessage(Throwable error) {
    Throwable cause = error;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
  }

  private void postToRenderThread(Runnable action) {
    if (Gdx.app != null) {
      Gdx.app.postRunnable(action);
    }
  }

  private void backFromCharacterSelect() {
    if (activeView != View.CHARACTER_SELECT && activeView != View.CONNECTING) {
      return;
    }
    cancelPregameConnection();
    cancelOptionsRequest();
    if (selectionHosted) {
      selectionHosted = false;
      launching = false;
      showHostNameViewInternal();
      stopHostingInBackground();
    } else if (selectionBackView == View.JOIN) {
      showJoinView();
    } else {
      showMainView();
    }
  }

  private void stopHostingInBackground() {
    if (hostCleanupPending) {
      return;
    }
    hostCleanupPending = true;
    ServerProcess server = pendingServer;
    pendingServer = null;
    CompletableFuture<Void> shutdown = HostSession.stopHostingAsync();
    if (server != null) {
      shutdown = shutdown.thenRunAsync(server::stop);
    }
    setHostControlsDisabled(launching);
    hostStatusLabel.setText(trans.text(T_STOPPING_SERVER));
    shutdown.whenComplete(
        (ignored, error) -> {
          if (error != null) {
            LOGGER.error("Could not stop the hosted server.", error);
          }
          postToRenderThread(
              () -> {
                hostCleanupPending = false;
                if (stage == null) {
                  return;
                }
                setHostControlsDisabled(launching);
                hostStatusLabel.setText(
                    error == null ? "" : trans.text(T_SERVER_STOP_FAILED, rootCauseMessage(error)));
              });
        });
  }

  private void cancelPregameConnection() {
    pregameGeneration++;
    admissionStarting = false;
    ConnectionListener listener = admissionListener;
    admissionListener = null;
    if (listener != null) {
      Game.network().removeConnectionListener(listener);
    }
    if (listener == null) {
      return;
    }
    admissionCleanupPending = true;
    Thread cleanup =
        new Thread(
            () -> {
              synchronized (networkStartLock) {
                Game.network().shutdown("Main-menu admission cancelled");
              }
              admissionCleanupPending = false;
              postToRenderThread(
                  () -> {
                    if (stage != null && activeView == View.CHARACTER_SELECT) {
                      updateCharacterConfirmButton();
                    } else if (stage != null && activeView == View.JOIN) {
                      setJoinControlsDisabled(discoveryInProgress);
                    }
                  });
            },
            "menu-client-cancel");
    cleanup.setDaemon(true);
    cleanup.start();
  }

  /**
   * Rebuilds the localized views and re-shows the active one. Triggered when the language changes
   * so the already-rendered labels and buttons reflect the newly selected language.
   */
  private void rebuildViews() {
    if (stage == null) {
      return;
    }
    mainView = buildMainView();
    settingsView = buildSettingsView();
    achievementsView = buildAchievementsView();
    hostNameView = buildHostNameView();
    joinView = buildJoinView();
    characterSelectionView = buildCharacterSelectionView();
    if (activeView == View.SETTINGS) {
      showSettingsView();
    } else if (activeView == View.ACHIEVEMENTS) {
      showAchievementsView();
    } else if (activeView == View.CREDITS) {
      showCreditsView();
    } else if (activeView == View.HOST_NAME) {
      showHostNameViewInternal();
    } else if (activeView == View.JOIN) {
      showJoinView();
    } else if (activeView == View.CONNECTING) {
      showConnectingView();
    } else if (activeView == View.CHARACTER_SELECT) {
      showCharacterSelectionView();
    } else {
      showMainView();
    }
  }

  private void swapContent(Table view) {
    content.clearChildren();
    content.add(view).pad(30);
    content.pack();
  }

  /** Validates and stores the player name, then begins hosting. */
  private void confirmHostName() {
    if (launching) {
      return;
    }
    String input = hostNameField.getText().trim();
    String username = input.isEmpty() ? ClientConnectionDialog.defaultUsername() : input;
    try {
      PreRunConfiguration.username(username);
    } catch (IllegalArgumentException e) {
      hostStatusLabel.setText(trans.text(T_INVALID_NAME));
      return;
    }
    startHosting(continueGameSelected);
  }

  /**
   * Starts a dedicated server child process, waits (off the render thread) until it is reachable,
   * then enters the client view connecting to the local server.
   *
   * @param continueGame whether the child should restore the configured checkpoint
   */
  private void startHosting(boolean continueGame) {
    if (launching || hostCleanupPending) return;
    launching = true;
    setHostControlsDisabled(true);
    hostStatusLabel.setText(trans.text(T_STARTING_SERVER));

    int port = starter.localServerPort();
    long operation = ++hostGeneration;
    String playerName = PreRunConfiguration.username();
    Thread launcher =
        new Thread(
            () -> launchHostedServer(port, continueGame, operation, playerName),
            "hosted-server-launcher");
    launcher.setDaemon(true);
    launcher.start();
  }

  private void launchHostedServer(
      int port, boolean continueGame, long operation, String playerName) {
    ServerProcess server;
    try {
      server =
          ServerProcess.start(
              starter.serverMainClass(),
              port,
              TrackingRuntime.childEnvironmentOverrides(),
              TrackingRuntime::handleManagedServerStatus,
              continueGame ? starter.continueServerArguments() : starter.serverArguments());
    } catch (IOException e) {
      LOGGER.error("Failed to start server process.", e);
      postToRenderThread(
          () -> {
            if (operation == hostGeneration && stage != null) {
              onHostFailed(trans.text(T_SERVER_START_FAILED, e.getMessage()));
            }
          });
      return;
    }

    if (operation != hostGeneration) {
      server.stop();
      return;
    }
    pendingServer = server;
    if (!server.awaitReady(port, SERVER_START_TIMEOUT)) {
      if (pendingServer == server) pendingServer = null;
      server.stop();
      postToRenderThread(
          () -> {
            if (operation == hostGeneration && stage != null) {
              onHostFailed(trans.text(T_SERVER_TIMEOUT));
            }
          });
      return;
    }

    if (operation != hostGeneration) {
      if (pendingServer == server) pendingServer = null;
      server.stop();
      return;
    }
    postToRenderThread(
        () -> {
          if (operation != hostGeneration || stage == null) {
            CompletableFuture.runAsync(server::stop);
            return;
          }
          pendingServer = null;
          HostSession.hosting(server);
          beginCharacterDiscovery(LOCAL_SERVER_HOST, port, playerName, View.HOST_NAME, true);
        });
  }

  private void onHostFailed(String message) {
    launching = false;
    setHostControlsDisabled(false);
    hostStatusLabel.setText(message);
  }

  private void cancelHostSetup() {
    if (launching && activeView == View.HOST_NAME) {
      hostGeneration++;
      launching = false;
      stopHostingInBackground();
      showMainView();
      return;
    }
    if (activeView == View.CHARACTER_SELECT && selectionHosted) {
      backFromCharacterSelect();
      return;
    }
    showMainView();
  }

  /**
   * Shows the Join Game form that gathers the server address, port and player name, so the client
   * can connect directly without bringing up the built-in connection dialog.
   */
  private void joinGame() {
    if (launching) {
      return;
    }
    showJoinView();
  }

  /**
   * Validates the Join Game form and asks the server to confirm the connection and player name
   * before opening character selection.
   */
  private void confirmJoin() {
    if (launching || discoveryInProgress || admissionCleanupPending) {
      return;
    }
    joinStatusLabel.setText("");

    String nameInput = joinNameField.getText().trim();
    String username = nameInput.isEmpty() ? ClientConnectionDialog.defaultUsername() : nameInput;
    try {
      PreRunConfiguration.username(username);
    } catch (IllegalArgumentException e) {
      joinStatusLabel.setText(trans.text(T_INVALID_NAME));
      return;
    }

    ClientConnectionConfig config;
    try {
      config =
          ClientConnectionConfig.fromFields(
              joinHostField.getText(),
              joinPortField.getText(),
              ClientConnectionConfig.DEFAULT_HOST,
              PreRunConfiguration.networkPort());
    } catch (IllegalArgumentException e) {
      joinStatusLabel.setText(trans.text(T_INVALID_CONNECTION));
      return;
    }

    showStartupConsent(
        () -> beginCharacterDiscovery(config.host(), config.port(), username, View.JOIN, false));
  }

  private void cancelJoinSetup() {
    pregameGeneration++;
    cancelOptionsRequest();
    setJoinControlsDisabled(false);
    showMainView();
  }

  private void setJoinControlsDisabled(boolean disabled) {
    joinConfirmButton.setDisabled(disabled);
    joinNameField.setDisabled(disabled);
    joinHostField.setDisabled(disabled);
    joinPortField.setDisabled(disabled);
    joinBackButton.setDisabled(false);
  }

  private void setHostControlsDisabled(boolean disabled) {
    hostConfirmButton.setDisabled(disabled || hostCleanupPending);
    hostBackButton.setDisabled(false);
    hostNameField.setDisabled(disabled);
  }

  @Override
  public void render(float delta) {
    if (stage == null) {
      return;
    }
    Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.act(delta);
    stage.draw();
    if (activeView == View.CHARACTER_SELECT) {
      characterRefreshElapsed += delta;
      if (characterRefreshElapsed >= CharacterCarousel.AVAILABILITY_REFRESH_SECONDS
          && !discoveryInProgress
          && !admissionStarting
          && !admissionCleanupPending) {
        requestCharacterOptions(false);
      }
    }
    if (!handingOffToGame && Game.isMultiplayerClient()) {
      try {
        Game.network().pollAndDispatch();
      } catch (RuntimeException e) {
        LOGGER.warn("Error while polling the menu connection: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0 || stage == null) {
      return;
    }
    stage.getViewport().setWorldSize(width, height);
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void hide() {
    dispose();
  }

  @Override
  public void dispose() {
    if (disposed) {
      return;
    }
    disposed = true;
    Game.localization().removeLanguageChangeListener(languageChangeListener);
    cancelOptionsRequest();
    if (!handingOffToGame) {
      hostGeneration++;
      cancelPregameConnection();
      stopHostingInBackground();
    }
    if (stage != null) {
      stage.dispose();
      stage = null;
    }
    if (backgroundTexture != null) {
      backgroundTexture.dispose();
      backgroundTexture = null;
    }
  }
}
