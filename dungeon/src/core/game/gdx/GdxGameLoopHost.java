package core.game.gdx;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import contrib.components.UIComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.systems.AttributeBarSystem;
import contrib.systems.DebugDrawSystem;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.configuration.KeyboardConfig;
import core.game.*;
import core.game.bootstrap.ClientStartup;
import core.level.loader.DungeonLoader;
import core.level.loader.LevelParser;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.client.ClientNetwork;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.*;
import core.platform.gdx.GdxInputBridge;
import core.platform.gdx.render.DrawSystem;
import core.platform.gdx.sound.GdxSoundPlayer;
import core.platform.gdx.systems.GdxCameraSystem;
import core.platform.gdx.window.GdxWindowEventsBridge;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.systems.*;
import core.ui.StageHandle;
import core.ui.gdx.GdxStageHandle;
import core.utils.InputManager;
import core.utils.logging.DungeonLogger;
import starter.GdxPlatformBootstrap;

import java.util.Objects;
import java.util.Optional;

/**
 * libGDX host implementation of the dungeon loop.
 *
 * <p>This class keeps all libGDX specific code (window, OpenGL clear, Stage, fullscreen toggle)
 * out of the engine-agnostic core loop logic.
 */
public final class GdxGameLoopHost extends ScreenAdapter {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GdxGameLoopHost.class);

  private static ISoundPlayer soundPlayer = new NoSoundPlayer();
  private static Stage stage;

  private final GameLoopCore core;
  private boolean doSetup = true;

  public GdxGameLoopHost(final GameLoopCore core) {
    this.core = core;
  }

  /** Starts the dungeon with a libGDX window (same behavior as old GameLoop.run). */
  public static void run(final GameLoopCore core) {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setWindowSizeLimits(
      PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight(), 9999, 9999);
    config.setForegroundFPS(PreRunConfiguration.frameRate());
    config.setResizable(PreRunConfiguration.resizeable());
    config.setTitle(PreRunConfiguration.windowTitle());
    config.setWindowIcon(PreRunConfiguration.logoPath().pathString());
    config.disableAudio(PreRunConfiguration.disableAudio());
    config.setWindowListener(GdxWindowEventsBridge.listener());

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
            setScreen(new GdxGameLoopHost(core));
          }
        },
        config);
    } else {
      // Server mode does not create a window (same behavior as before).
      new GdxGameLoopHost(core).setup();
    }
  }

  /** Exposed to core.Game via core.game.GameLoop facade. */
  public static Optional<StageHandle> stage() {
    return Optional.ofNullable(stage).map(GdxStageHandle::new);
  }

  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  @Override
  public void render(float delta) {
    if (doSetup) {
      setup();
    }

    // Host-specific: fullscreen toggle key (still uses libGDX display modes).
    fullscreenKey();

    // Host-specific: keep projection in sync (was in old GameLoop.render()).
    ECSManagement.system(
      DrawSystem.class,
      drawSystem -> DrawSystem.batch().setProjectionMatrix(GdxCameraSystem.camera().combined));

    // Host-specific: sound update belongs to the host (not the core).
    Game.soundPlayer().update(delta);

    // Backend-agnostic parts:
    core.beforeRender(delta);

    // Host-specific: clear screen before systems render.
    clearScreen();

    // Backend-agnostic ECS tick:
    core.tick(delta);

    // Host-specific: input + camera update order (was in old GameLoop.render()).
    InputManager.update();
    GdxCameraSystem.camera().update();

    // Host-specific: Stage act/draw after ECS tick.
    Optional.ofNullable(stage).ifPresent(s -> updateStage(s, delta));
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

    // Bridge libGDX input events into our engine-agnostic InputManager.
    GdxInputBridge.install();
    InputManager.reset();
  }

  private static void updateStage(final Stage stage, final float delta) {
    stage.act(delta);
    stage.draw();
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
  }

  private void setup() {
    LOGGER.info("Setting up game...");
    doSetup = false;

    ECSManagement.bootstrapDefaultSystems(SystemProfile.GDX_CLIENT);
    ECSManagement.bootstrapGameplaySystems(SystemProfile.GDX_CLIENT);
    GdxPlatformBootstrap.installHudSystems();

    // Client-side setup (systems, message handlers, stage, audio) – exactly like before.
    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      setupClient();
    }

    ClientStartup.setupAndLoadInitialLevelOnce();
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

  private void fullscreenKey() {
    if (InputManager.isKeyJustPressed(KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
      if (!Gdx.graphics.isFullscreen()) {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
      } else {
        Gdx.graphics.setWindowedMode(
          PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
      }
    }
  }

  private void clearScreen() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    Optional.ofNullable(stage)
      .ifPresent(
        s -> {
          s.getViewport().setWorldSize(width, height);
          s.getViewport().update(width, height, true);
        });
  }

  /** Create the systems. */
  private void createSystems() {
    // Keep existing LevelSystem hook
    ECSManagement.system(LevelSystem.class, ls -> ls.onLevelLoad(GameLoop.onLevelLoad));

    // GDX-client extras (still contrib/libGDX specific)
    ECSManagement.add(new DebugDrawSystem());
    ECSManagement.add(new AttributeBarSystem());
  }
}
