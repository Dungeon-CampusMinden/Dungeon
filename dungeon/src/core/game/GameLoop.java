package core.game;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import contrib.components.UIComponent;
import contrib.crafting.Crafting;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.entities.deco.DecoFactory;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogFactory;
import contrib.systems.AttributeBarSystem;
import contrib.systems.DebugDrawSystem;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.loader.DungeonLoader;
import core.level.loader.LevelParser;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.client.ClientNetwork;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.*;
import core.sound.player.GdxSoundPlayer;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.systems.*;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.InputManager;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * The Dungeon-GameLoop.
 *
 * <p>This class contains the game loop method that is connected with libGDX. It controls the system
 * flow, will execute the Systems, and triggers the event callbacks configured in the {@link
 * PreRunConfiguration}.
 *
 * <p>Use {@link #run()} to start the game.
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class GameLoop extends ScreenAdapter {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoop.class);
  private static ISoundPlayer soundPlayer = new NoSoundPlayer();
  private static Stage stage;
  private boolean doSetup = true;
  private boolean gameIsRunning = false;
  private Table mainMenuTable = null;
  private Skin skin;

  /**
   * Sets {@link Game#currentLevel} to the new level and changes the currently active entity
   * storage.
   *
   * <p>Will remove all Systems using {@link ECSManagement#removeAllSystems()} from the Game. This
   * will trigger {@link System#onEntityRemove} for the old level. Then, it will readd all Systems
   * using {@link ECSManagement#add(System)}, triggering {@link System#onEntityAdd} for the new
   * level.
   *
   * <p>Will re-add the player if they exist.
   */
  public static final IVoidFunction onLevelLoad =
      () -> {
        if (!PreRunConfiguration.isNetworkServer()) return; // no authority

        List<Entity> allPlayers = ECSManagement.allPlayers().toList();
        boolean firstLoad = !ECSManagement.levelStorageMap().containsKey(Game.currentLevel().get());
        allPlayers.forEach(ECSManagement::remove);
        // Remove the systems so that each triggerOnRemove(entity) will be called (basically
        // cleanup).
        Map<Class<? extends System>, System> s = ECSManagement.systems();
        ECSManagement.removeAllSystems();
        ECSManagement.activeEntityStorage(
            ECSManagement.levelStorageMap()
                .computeIfAbsent(Game.currentLevel().orElse(null), k -> new HashSet<>()));
        // readd the systems so that each triggerOnAdd(entity) will be called (basically
        // setup). This will also create new EntitySystemMapper if needed.
        s.values().forEach(ECSManagement::add);

        try {
          allPlayers.forEach(GameLoop::placeOnLevelStart);
        } catch (MissingComponentException e) {
          LOGGER.warn(e.getMessage());
        }
        ECSManagement.allEntities()
            .filter(Entity::isPersistent)
            .map(ECSManagement::remove)
            .forEach(ECSManagement::add);

        Game.currentLevel()
            .ifPresent(
                level ->
                    level
                        .decorations()
                        .forEach(tuple -> Game.add(DecoFactory.createDeco(tuple.b(), tuple.a()))));

        if (firstLoad && Game.isCheckPatternEnabled())
          Game.currentLevel()
              .ifPresent(level -> CheckPatternPainter.paintCheckerPattern(level.layout()));
        PreRunConfiguration.userOnLevelLoad().accept(firstLoad);
      };

  // for singleton
  private GameLoop() {}

  /**
   * Starts the dungeon.
   *
   * <p>If multiplayer is enabled and this is a network server, no window will be created, instead
   * the server will run headless.
   *
   * @see PreRunConfiguration
   */
  public static void run() {
    java.lang.System.out.println("function run called");
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setWindowSizeLimits(
        PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight(), 9999, 9999);
    config.setForegroundFPS(PreRunConfiguration.frameRate());
    config.setResizable(PreRunConfiguration.resizeable());
    config.setTitle(PreRunConfiguration.windowTitle());
    config.setWindowIcon(PreRunConfiguration.logoPath().pathString());
    config.disableAudio(PreRunConfiguration.disableAudio());
    config.setWindowListener(WindowEventManager.windowListener());
    if (SharedLibraryLoader.isMac && Gdx.app == null) {
      org.lwjgl.system.Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
    }
    if (PreRunConfiguration.fullScreen()) {
      config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
    } else {
      config.setWindowedMode(PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
    }

    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      new Lwjgl3Application(
          new com.badlogic.gdx.Game() {
            @Override
            public void create() {
              setScreen(new GameLoop());
            }
          },
          config);
    } else {
      // Server mode does not create a window.
      new GameLoop().setup();
    }
  }

  /**
   * Get the {@link Stage} that can be used to draw HUD elements.
   *
   * @return The configured stage, can be empty.
   */
  public static Optional<Stage> stage() {
    return Optional.ofNullable(stage);
  }

  private static void updateStage(final Stage stage) {
    stage.act(Gdx.graphics.getDeltaTime());
    stage.draw();
  }

  private static void setupStage() {
    stage =
        new Stage(
            new ScalingViewport(
                Scaling.stretch,
                PreRunConfiguration.windowWidth(),
                PreRunConfiguration.windowHeight()),
            new SpriteBatch());
    Gdx.input.setInputProcessor(stage);


  }

  /**
   * Get the current tick of the game.
   *
   * <p>The tick is incremented every frame, starting from 0 at the beginning of the game.
   *
   * @return the current tick
   */
  public static int currentTick() {
    return ECSManagement.currentTick();
  }

  /**
   * Main game loop.
   *
   * <p>Triggers the execution of the systems and the event callbacks.
   *
   * <p>Will trigger {@link #frame} and {@link PreRunConfiguration#userOnFrame()}.
   *
   * <p>On the first frame, {@link #setup()} and {@link PreRunConfiguration#userOnSetup()} are
   * triggered.
   *
   * @param delta The time since the last loop.
   */
  @Override
  public void render(float delta) {
    if (doSetup) setup();
    ECSManagement.system(
        DrawSystem.class,
        drawSystem -> DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined));
    // Drain any inbound network messages on the game thread before running systems
    try {
      Game.network().pollAndDispatch();
    } catch (Exception e) {
      LOGGER.warn("Error while polling network messages: {}", e.getMessage(), e);
    }
    frame(delta);
    clearScreen();

    if (gameIsRunning) {
//       Execute ECS tick using shared runner. In MP client mode, run render/input/camera only.
      final boolean isMultiplayerClient =
        PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();
      ECSManagement.executeOneTick(
        isMultiplayerClient ? System.AuthoritativeSide.CLIENT : System.AuthoritativeSide.BOTH);

      InputManager.update();
      CameraSystem.camera().update();


    }
    //       stage logic
    stage().ifPresent(GameLoop::updateStage);
  }

  /**
   * Set up the client side of the game.
   *
   * <p>This method should be called only if in single player mode or multiplayer client mode.
   *
   * <p>Will execute {@link LevelSystem#execute()} once to load the first level before the actual
   * game loop starts. This ensures the first level is set at the start of the game loop, even if
   * the {@link LevelSystem} is not executed as the first system in the game loop.
   *
   * <p>It will:
   *
   * <ul>
   *   <li>Create all client relevant systems.
   *   <li>Set up the message handlers for network messages. (If multiplayer is enabled)
   *   <li>Set up connection listeners to reset input sequence on disconnect. (If multiplayer is
   *       enabled)
   *   <li>Set up the stage for HUD rendering.
   * </ul>
   *
   * <p>Will perform some setup.
   */
  private void setupClient() {
    java.lang.System.out.println("setup client called");
    LOGGER.info("Setting up client...");
    doSetup = false;
    if (Gdx.audio != null && !PreRunConfiguration.disableAudio()) {
      AssetManager assetManager = new AssetManager();
      soundPlayer = new GdxSoundPlayer(assetManager);
    }
    createSystems();

    if (PreRunConfiguration.multiplayerEnabled()) {
      DungeonLoader.afterAllLevels(() -> {}); // server controls this
      setupMessageHandlers();
      Game.network()
          .addConnectionListener(
              new ConnectionListener() {
                @Override
                public void onConnected() {}

                @Override
                public void onDisconnected(String reason) {
                  InputMessage.resetSequence();
                }
              });
    }
    setupStage();
    skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

  }

  /**
   * Called once at the beginning of the game.
   *
   * <p>It will:
   *
   * <ul>
   *   <li>Set up the client if not in server mode.
   *   <li>Execute the user-defined setup callback.
   *   <li>Execute the LevelSystem to load the initial level.
   *   <li>Start the network handler.
   * </ul>
   *
   * @see PreRunConfiguration#userOnSetup()
   */
  private void setup() {
    java.lang.System.out.println("setting up the game");
    LOGGER.info("Setting up game...");
    doSetup = false;
    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      setupClient();
    }

    PreRunConfiguration.userOnSetup().execute();
    Game.network().start();

    if (!Game.isHeadless()) InputManager.init();

    Crafting.loadRecipes();

    Game.system(LevelSystem.class, LevelSystem::execute); // load initial level
  }

  private void setupMessageHandlers() {
    MessageDispatcher dispatcher = Game.network().messageDispatcher();

    dispatcher.registerHandler(
        EntitySpawnEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received EntitySpawnEvent event: " + event.entityId());

          // check if the entity already exists
          if (Game.allEntities().anyMatch(e -> e.id() == event.entityId())) {
            LOGGER.warn(
                "Received spawn event for already existing entity with ID: " + event.entityId());
            return;
          }

          // is hero?
          if (event.playerComponent() != null) {
            PlayerComponent pc = event.playerComponent();
            boolean alreadyGotAHero = Game.player().isPresent();
            boolean isLocal = Objects.equals(pc.playerName(), PreRunConfiguration.username());

            if (alreadyGotAHero) {
              LOGGER.info("Already got a hero, checking if local player...");
              if (isLocal) {
                LOGGER.warn(
                    "Received spawn event for local player, but we already have a local player! ID: {} ",
                    event.entityId());
                return;
              }
            }

            Game.add(
                HeroBuilder.builder()
                    .id(event.entityId())
                    .characterClass(CharacterClass.fromByteId(event.characterClassId()))
                    .isLocalPlayer(isLocal)
                    .username(pc.playerName())
                    .build());
            return;
          }

          Entity newEntity = new Entity(event.entityId());
          newEntity.add(event.positionComponent());
          newEntity.add(event.drawComponent());
          newEntity.persistent(event.isPersistent());
          Game.add(newEntity);
        });

    dispatcher.registerHandler(
        EntityDespawnEvent.class,
        (ctx, event) -> {
          LOGGER.info(
              "Received EntityDespawnEvent event: "
                  + event.entityId()
                  + ", reason: "
                  + event.reason());
          Entity entity =
              Game.allEntities().filter(e -> e.id() == event.entityId()).findFirst().orElse(null);
          if (entity == null) {
            LOGGER.warn("Received despawn event for unknown entity with ID: " + event.entityId());
            return;
          }
          Game.remove(entity);
        });

    dispatcher.registerHandler(
        LevelChangeEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received LevelChangeEvent event: {}", event.levelName());
          try {
            Game.currentLevel(LevelParser.parseLevel(event.levelData(), event.levelName()));
            Game.player().ifPresent(GameLoop::placeOnLevelStart);
          } catch (Exception e) {
            LOGGER.error("Failed to handle LevelChangeEvent: {}", e.getMessage(), e);
          }
        });
    dispatcher.registerHandler(
        GameOverEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received GameOverEvent event (reason: {})", event.reason());
          ClientNetwork.invalidateLastSessionFile();
          Game.exit(event.reason());
        });
    dispatcher.registerHandler(
        SnapshotMessage.class,
        (ctx, event) -> {
          try {
            Game.network().snapshotTranslator().applySnapshot(event, dispatcher);
          } catch (Exception ignored) {
            LOGGER.warn("Error while applying snapshot message: {}", ignored.getMessage(), ignored);
          }
        });

    dispatcher.registerHandler(
        DialogShowMessage.class,
        (ctx, msg) -> {
          LOGGER.debug("Received DialogShowMessage for dialog: {}", msg.context().dialogId());

          DialogFactory.show(msg.context(), false, msg.canBeClosed(), new int[] {});
        });

    dispatcher.registerHandler(
        DialogCloseMessage.class,
        (ctx, msg) -> {
          LOGGER.debug("Received DialogCloseMessage for dialog: {}", msg.dialogId());
          // Find and remove the UiComponent with the given dialogId
          Game.allEntities()
              .filter(
                  e ->
                      e.fetch(UIComponent.class)
                          .map(
                              comp ->
                                  comp.dialogContext() != null
                                      && msg.dialogId().equals(comp.dialogContext().dialogId()))
                          .orElse(false))
              .findFirst()
              .flatMap(e -> e.fetch(UIComponent.class))
              .ifPresent(UIUtils::closeDialog);
        });
  }

  /**
   * Called at the beginning of each frame, before the entities are updated and the systems are
   * executed.
   *
   * <p>This is the place to add basic logic that isn't part of any system.
   *
   * @param delta The time since the last loop.
   */
  private void frame(float delta) {
    fullscreenKey();
    Game.soundPlayer().update(delta);
    PreRunConfiguration.userOnFrame().execute();
    renderMainMenu();

  }

  private void createMainMenu() {
    mainMenuTable = new Table();
    mainMenuTable.setFillParent(true);
    mainMenuTable.center(); // Menü zentrieren

    TextButton startButton = new TextButton("Start", skin);
    TextButton exitButton  = new TextButton("Exit", skin);

    // Start Button startet das Spiel
    startButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        gameIsRunning = true;
        mainMenuTable.setVisible(false);
      }
    });

//    // Exit Button schließt das Spiel
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Game.exit("User Exit");
      }
    });

    mainMenuTable.add(startButton).pad(10).width(200);
    mainMenuTable.row();
    mainMenuTable.add(exitButton).pad(10).width(200);

    stage.addActor(mainMenuTable);
  }

  private void renderMainMenu() {
    if (mainMenuTable == null) {
      java.lang.System.out.println("drawing main menu");
      createMainMenu();
    }
  }

  private void fullscreenKey() {
    if (InputManager.isKeyJustPressed(
        core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
      if (!Gdx.graphics.isFullscreen()) {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
      } else {
        Gdx.graphics.setWindowedMode(
            PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
      }
    }
  }

  /**
   * Set the position of the given entity to the position of the level-start.
   *
   * <p>A {@link PositionComponent} is needed.
   *
   * @param entity entity to set on the start of the level, normally this is the player.
   */
  private static void placeOnLevelStart(final Entity entity) {
    ECSManagement.add(entity);
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Game.startTile()
                  .ifPresentOrElse(
                      pc::position, () -> LOGGER.warn("No start tile found for the current level"));
              pc.viewDirection(Direction.DOWN); // look down by default
            });

    // reset animations
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
  }

  /**
   * Clear the screen. Removes all.
   *
   * <p>Needs to be called before redraw something.
   */
  private void clearScreen() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    stage()
        .ifPresent(
            x -> {
              x.getViewport().setWorldSize(width, height);
              x.getViewport().update(width, height, true);
            });
  }

  /**
   * Get the sound player used by the game.
   *
   * @return The sound player.
   */
  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  /** Create the systems. */
  private void createSystems() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.system(LevelSystem.class, ls -> ls.onLevelLoad(onLevelLoad));
    ECSManagement.add(new CameraSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new InputSystem());
    ECSManagement.add(new DebugDrawSystem());
    ECSManagement.add(new AttributeBarSystem());
  }
}
