package core.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import contrib.hud.UIUtils;
import contrib.hud.dialogs.ClientConnectionDialog;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.language.Translation;
import core.utils.FontSpec;
import core.utils.Scene2dElementFactory;
import core.utils.logging.DungeonLogger;
import core.utils.settings.ClientSettings;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * The main menu shown before the core {@link GameLoop} starts.
 *
 * <p>It is a standalone libGDX {@link com.badlogic.gdx.Screen} with its own {@link Stage}, rendered
 * inside the already-running application. When the player chooses an option it transitions to the
 * core game by switching the active screen via {@link GameLoop#startGame()}.
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
  // Darkens the background image so the title and panel stay readable on any image.
  private static final Color BACKGROUND_TINT = new Color(0.5f, 0.5f, 0.5f, 1f);

  private static final String T_HOST = "host";
  private static final String T_JOIN = "join";
  private static final String T_SETTINGS = "settings";
  private static final String T_EXIT = "exit";
  private static final String T_BACK = "back";
  private static final String T_CONFIRM = "confirm";
  private static final String T_PLAYER_NAME = "player_name";
  private static final String T_INVALID_NAME = "invalid_name";
  private static final String T_STARTING_SERVER = "starting_server";
  private static final String T_SERVER_START_FAILED = "server_start_failed";
  private static final String T_SERVER_TIMEOUT = "server_timeout";
  private static final Translation trans = new Translation("main_menu");

  private final GameStarter starter;

  private Stage stage;
  private Skin skin;
  private Texture backgroundTexture;
  private Table content;
  private Table mainView;
  private Table settingsView;
  private Table hostNameView;

  private TextField hostNameField;
  private Label hostStatusLabel;
  private TextButton hostConfirmButton;
  private TextButton hostBackButton;

  private volatile boolean launching = false;

  /**
   * Creates the main menu screen for the given starter.
   *
   * @param starter the game integration used to configure and launch the game
   */
  public MainMenuScreen(GameStarter starter) {
    this.starter = starter;
  }

  @Override
  public void show() {
    int width = Game.windowWidth() > 0 ? Game.windowWidth() : PreRunConfiguration.windowWidth();
    int height = Game.windowHeight() > 0 ? Game.windowHeight() : PreRunConfiguration.windowHeight();
    stage = new Stage(new ScalingViewport(Scaling.stretch, width, height), new SpriteBatch());
    Gdx.input.setInputProcessor(stage);
    skin = UIUtils.defaultSkin();

    content = new Table(skin);
    content.setBackground("window_background_big");
    mainView = buildMainView();
    settingsView = buildSettingsView();
    hostNameView = buildHostNameView();

    Stack root = new Stack();
    root.setFillParent(true);
    addBackgroundIfPresent(root);

    Table foreground = new Table();
    foreground.add(buildTitleBanner()).padBottom(30).row();
    foreground.add(content);
    root.add(foreground);

    stage.addActor(root);

    showMainView();
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

  /** Builds the big game-name banner using the configured starter accent color. */
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
    TextButton hostButton = menuButton(trans.text(T_HOST), "green", this::showHostNameView);
    TextButton joinButton = menuButton(trans.text(T_JOIN), "blue-outline", this::joinGame);
    TextButton settingsButton =
        menuButton(trans.text(T_SETTINGS), "blue-outline", this::showSettingsView);
    TextButton exitButton =
        menuButton(trans.text(T_EXIT), "red-outline", () -> Game.exit("Exit from main menu"));

    Table menu = new Table();
    menu.add(hostButton).width(BUTTON_WIDTH).padBottom(12).row();
    menu.add(joinButton).width(BUTTON_WIDTH).padBottom(12).row();
    menu.add(settingsButton).width(BUTTON_WIDTH).padBottom(12).row();
    menu.add(exitButton).width(BUTTON_WIDTH).row();
    return menu;
  }

  private Table buildHostNameView() {
    Label title = label(trans.text(T_HOST), 36, PANEL_TEXT_COLOR);
    Label nameLabel = label(trans.text(T_PLAYER_NAME), 22, PANEL_TEXT_COLOR);
    hostNameField = Scene2dElementFactory.createTextField(ClientConnectionDialog.defaultUsername());
    hostStatusLabel = label("", 18, ERROR_COLOR);
    hostConfirmButton = menuButton(trans.text(T_CONFIRM), "green", this::confirmHostName);
    hostBackButton = menuButton(trans.text(T_BACK), "red-outline", this::showMainView);

    Table buttons = new Table();
    buttons.add(hostBackButton).width(175).padRight(10);
    buttons.add(hostConfirmButton).width(175);

    Table menu = new Table();
    menu.add(title).padBottom(20).align(Align.center).row();
    menu.add(nameLabel).align(Align.left).padBottom(5).row();
    menu.add(hostNameField).width(BUTTON_WIDTH).padBottom(8).row();
    menu.add(hostStatusLabel).width(BUTTON_WIDTH).minHeight(24).padBottom(8).row();
    menu.add(buttons).align(Align.center).row();
    return menu;
  }

  private Table buildSettingsView() {
    Label title = label(trans.text(T_SETTINGS), 48, PANEL_TEXT_COLOR);
    TextButton backButton = menuButton(trans.text(T_BACK), "green", this::showMainView);

    Table settingsTable = new Table();
    ClientSettings.getSettings()
        .forEach(
            (key, setting) ->
                settingsTable.add(setting.toUIActor()).width(500).pad(0, 10, 20, 10).row());
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(settingsTable, false, true);

    Table menu = new Table();
    menu.add(title).padBottom(15).align(Align.center).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(5).row();
    menu.add(scrollPane).width(550).height(380).row();
    menu.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(5).row();
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
    swapContent(mainView);
  }

  private void showSettingsView() {
    swapContent(settingsView);
  }

  private void showHostNameView() {
    hostStatusLabel.setText("");
    setHostControlsDisabled(false);
    swapContent(hostNameView);
    stage.setKeyboardFocus(hostNameField);
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
    startHosting();
  }

  /**
   * Starts a dedicated server child process, waits (off the render thread) until it is reachable,
   * then enters the client view connecting to the local server.
   */
  private void startHosting() {
    launching = true;
    setHostControlsDisabled(true);
    hostStatusLabel.setText(trans.text(T_STARTING_SERVER));

    int port = starter.localServerPort();
    Thread launcher = new Thread(() -> launchHostedServer(port), "hosted-server-launcher");
    launcher.setDaemon(true);
    launcher.start();
  }

  private void launchHostedServer(int port) {
    ServerProcess server;
    try {
      server = ServerProcess.start(starter.serverMainClass(), port, starter.serverArguments());
    } catch (IOException e) {
      LOGGER.error("Failed to start server process.", e);
      Gdx.app.postRunnable(() -> onHostFailed(trans.text(T_SERVER_START_FAILED, e.getMessage())));
      return;
    }

    if (!server.awaitReady(port, SERVER_START_TIMEOUT)) {
      server.stop();
      Gdx.app.postRunnable(() -> onHostFailed(trans.text(T_SERVER_TIMEOUT)));
      return;
    }

    HostSession.hosting(server);
    Gdx.app.postRunnable(() -> enterClient(LOCAL_SERVER_HOST, port));
  }

  private void onHostFailed(String message) {
    launching = false;
    setHostControlsDisabled(false);
    hostStatusLabel.setText(message);
  }

  private void enterClient(String host, int port) {
    PreRunConfiguration.networkServerAddress(host);
    PreRunConfiguration.networkPort(port);
    GameLoop.startGame();
  }

  /**
   * Enters the client view without a preconfigured server address, so the client's connection
   * dialog asks for the server IP/port and player name.
   */
  private void joinGame() {
    if (launching) {
      return;
    }
    GameLoop.startGame();
  }

  private void setHostControlsDisabled(boolean disabled) {
    hostConfirmButton.setDisabled(disabled);
    hostBackButton.setDisabled(disabled);
    hostNameField.setDisabled(disabled);
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.act(delta);
    stage.draw();
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
