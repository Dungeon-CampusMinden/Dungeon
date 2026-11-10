package feature.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.CameraComponent;
import engine.components.PositionComponent;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.loader.DungeonLoader;
import engine.level.loader.DungeonSaver;
import engine.systems.CameraSystem;
import engine.systems.DrawSystem;
import engine.utils.FontHelper;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.PassthroughShader;
import engine.utils.logging.DungeonLogger;
import feature.components.HealthComponent;
import feature.leveleditor.DecoMode;
import feature.leveleditor.EditorActions;
import feature.leveleditor.LevelEditorMode;
import feature.leveleditor.LevelEditorSettings;
import feature.leveleditor.PointMode;
import feature.leveleditor.PrefabMode;
import feature.leveleditor.SettingsMode;
import feature.leveleditor.StartTilesMode;
import feature.leveleditor.TilesMode;
import feature.leveleditor.ui.LevelEditorUI;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import java.io.File;
import java.util.Objects;

/**
 * The LevelEditorSystem is responsible for handling the level editor. It allows the user to change
 * the {@link DungeonLevel} layout by setting different tiles. The user can set the following tiles:
 * skip, pit, floor, wall, hole, exit, door, and custom points. The user can also fill an area with
 * floor tiles and save the current dungeon.
 */
public class LevelEditorSystem extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LevelEditorSystem.class);

  /** The font used for rendering the level editor text. */
  public static final BitmapFont FONT = FontHelper.getDefaultFont(24);

  private static boolean internalStopped = false;
  private static boolean active = false;
  private static boolean activateOnStart = false;
  private static String pathToLevels = "";
  private static String defaultSaveFolder = "";
  private static boolean autoSave = true;
  private static boolean defaultAutoSave = true;
  private static String settingsLevelPath;

  private boolean debugShaderActive = false;
  private static final String DEBUG_SHADER_KEY = "LevelEditorSystem_debug";

  private static Mode currentMode = Mode.Tiles;
  private static LevelEditorMode currentModeInstance =
      currentMode.getModeInstance(LevelEditorSystem::levelChanged);

  private static String feedbackMessage = "";
  private static Color feedbackMessageColor = Color.WHITE;
  private static float feedbackMessageTimer = 0.0f;
  private static final float FEEDBACK_MESSAGE_DURATION = 3.0f; // seconds

  private static final float FREE_CAMERA_SPEED = 20f;
  private static final float FREE_CAMERA_SHIFT_SPEED_MULTIPLIER = 2f;
  private static final float FREE_CAMERA_ZOOM_STEP = 0.05f;
  private static final float MIN_FREE_CAMERA_ZOOM = 0.1f;
  private static Entity editorCameraEntity;
  private static CameraComponent editorCameraComponent;
  private static Float cameraZoomBeforeEditor;
  private static final InputContext TOGGLE_INPUT = new InputContext(EditorActions.EDITOR_TOGGLE);
  private static final InputContext EDITOR_INPUT = new InputContext(EditorActions.EDITOR);

  private static LevelEditorUI ui = null;

  /**
   * Creates a new LevelEditorSystem.
   *
   * @param pathToLevels the default folder used to construct the initial save file path.
   */
  public LevelEditorSystem(String pathToLevels) {
    super();
    settingsLevelPath = null;
    defaultSaveFolder = normalizePath(pathToLevels);
    LevelEditorSystem.pathToLevels = defaultSaveFolder;
    autoSave = defaultAutoSave = true;
  }

  /** Creates a new LevelEditorSystem. */
  public LevelEditorSystem() {
    super();
  }

  private static void loadSettingsForCurrentLevel() {
    String currentLevelPath = DungeonLoader.currentLevelAssetPath().orElse(null);
    if (Objects.equals(settingsLevelPath, currentLevelPath)) return;

    settingsLevelPath = currentLevelPath;
    LevelEditorSettings.Values settings =
        LevelEditorSettings.load(
            currentLevelPath, defaultSavePathForCurrentLevel(), defaultAutoSave);
    pathToLevels = settings.savePath();
    autoSave = settings.autoSave();
  }

  private static void persistSettings() {
    if (settingsLevelPath == null || settingsLevelPath.isBlank()) return;
    LevelEditorSettings.save(
        settingsLevelPath, new LevelEditorSettings.Values(pathToLevels, autoSave));
  }

  private static String normalizePath(String path) {
    return path == null ? "" : path;
  }

  private static String defaultSavePathForCurrentLevel() {
    String levelFileName =
        DungeonLoader.currentLevelAssetPath()
            .map(File::new)
            .map(File::getName)
            .orElseGet(() -> DungeonLoader.currentLevel() + "_1.level");
    if (defaultSaveFolder.isBlank()) return levelFileName;
    return new File(defaultSaveFolder, levelFileName).getPath();
  }

  /**
   * Gets the active status of the LevelEditorSystem.
   *
   * @return true if the LevelEditorSystem is active, false if not.
   */
  public static boolean active() {
    return active;
  }

  /**
   * Sets the active status of the LevelEditorSystem.
   *
   * @param active The active status to set.
   */
  public static void active(boolean active) {
    if (LevelEditorSystem.active == active) return;

    Entity player = Game.player().orElseThrow();
    if (active) {
      loadSettingsForCurrentLevel();
      Game.hud().dialogsSuppressed(true);
      Game.stage().ifPresent(Stage::unfocusAll);
      cameraZoomBeforeEditor = CameraSystem.camera().zoom;
      editorCameraComponent = player.fetch(CameraComponent.class).orElseThrow();
      player.remove(CameraComponent.class);
      Entity editorCamera = editorCameraEntity();
      editorCamera.add(
          new PositionComponent(
              CameraSystem.camera().position.x, CameraSystem.camera().position.y));
      editorCamera.add(editorCameraComponent);
      LevelEditorSystem.active = true;
      GameInput.activate(EDITOR_INPUT);
      player
          .fetch(HealthComponent.class)
          .ifPresent(
              hc -> {
                hc.godMode(true);
              });
      if (currentModeInstance != null) {
        currentModeInstance.onEnter();
      }
    } else {
      Entity editorCamera = editorCameraEntity();
      editorCamera.remove(CameraComponent.class);
      player.add(editorCameraComponent);
      editorCamera.remove(PositionComponent.class);
      editorCameraComponent = null;
      LevelEditorSystem.active = false;
      if (cameraZoomBeforeEditor != null) {
        CameraSystem.camera().zoom = cameraZoomBeforeEditor;
        cameraZoomBeforeEditor = null;
      }
      GameInput.deactivate(EDITOR_INPUT);
      player.fetch(HealthComponent.class).ifPresent(hc -> hc.godMode(false));
      currentModeInstance.onExit();
      Game.stage().ifPresent(Stage::unfocusAll);
      Game.hud().dialogsSuppressed(false);
    }
    updateUI();
  }

  /**
   * Gets the currently selected editor mode.
   *
   * @return the current mode.
   */
  public static Mode currentMode() {
    return currentMode;
  }

  /**
   * Gets the file path used when saving levels from the level editor.
   *
   * @return the configured level output file path.
   */
  public static String pathToLevels() {
    return pathToLevels;
  }

  /**
   * Sets the file path used when saving levels from the level editor.
   *
   * @param pathToLevels the level output file path.
   */
  public static void pathToLevels(String pathToLevels) {
    String normalizedPath = normalizePath(pathToLevels);
    LevelEditorSystem.pathToLevels = normalizedPath;
    if (settingsLevelPath != null) persistSettings();
  }

  /**
   * Gets whether the level editor should save after modifying the level.
   *
   * @return true if auto-save is enabled.
   */
  public static boolean autoSave() {
    return autoSave;
  }

  /**
   * Sets whether the level editor should save after modifying the level.
   *
   * @param autoSave true to enable auto-save.
   */
  public static void autoSave(boolean autoSave) {
    LevelEditorSystem.autoSave = autoSave;
    if (settingsLevelPath == null) {
      defaultAutoSave = autoSave;
    } else {
      persistSettings();
    }
  }

  /** Saves the current level to the configured level editor path. */
  public static void saveLevel() {
    DungeonSaver.saveCurrentDungeon(pathToLevels);
  }

  /** Saves the current level when auto-save is enabled. */
  public static void levelChanged() {
    if (autoSave) DungeonSaver.saveCurrentDungeon(pathToLevels, false);
  }

  /**
   * Refreshes the runtime prefab entities for the given level from its authored instances.
   *
   * @param level level whose prefab entities should be refreshed
   */
  public static void refreshPrefabs(ILevel level) {
    PrefabSide[] sides = PrefabSide.localSides();

    PrefabSpawner.batch(
        () -> {
          for (PrefabSide side : sides) PrefabSpawner.clear(level, side);
          for (PrefabSide side : sides) PrefabSpawner.spawn(level, side);
        });
  }

  /**
   * Moves the free editor camera so that it centers the given world position.
   *
   * @param position world position to look at
   */
  public static void focusCamera(Point position) {
    if (!active) return;
    editorCameraEntity()
        .fetch(PositionComponent.class)
        .ifPresent(component -> component.position(position));
  }

  /**
   * Switches to the given editor mode.
   *
   * <p>Exits the previous mode, creates a new instance of the given mode and enters it. Also
   * updates the mode selection panel of the {@link LevelEditorUI}.
   *
   * @param mode the mode to switch to.
   */
  public static void currentMode(Mode mode) {
    if (mode == null || mode == currentMode) return;
    currentMode = mode;
    currentModeInstance.onExit();
    currentModeInstance = mode.getModeInstance(LevelEditorSystem::levelChanged);
    currentModeInstance.onEnter();
    if (ui != null) {
      ui.modePanel().selected(mode);
      ui.detailsPanel().mode(currentModeInstance);
      ui.secondaryDetailsPanel().mode(currentModeInstance);
    }
  }

  /**
   * Called by the {@link LevelEditorUI} once it removed itself from the stage, so a new group is
   * created if the system is added to the game again.
   */
  public static void uiDetached() {
    ui = null;
  }

  /**
   * Makes sure the {@link LevelEditorUI} group is part of the stage and only visible while the
   * editor is active.
   */
  private static void updateUI() {
    if (ui == null) {
      Game.stage()
          .ifPresent(
              stage -> {
                ui = new LevelEditorUI();
                ui.setSize(stage.getWidth(), stage.getHeight());
                stage.addActor(ui);
                ui.detailsPanel().mode(currentModeInstance);
                ui.secondaryDetailsPanel().mode(currentModeInstance);
              });
    }
    if (ui != null) {
      ui.setVisible(active);
      ui.modePanel().selected(currentMode);
    }
  }

  /** Activates the editor on the first game tick after a local player has been created. */
  public static void activateOnStart() {
    activateOnStart = true;
  }

  @Override
  public void render(float delta) {
    if (!active) return;

    DebugDrawSystem.drawText(
        FONT,
        "( "
            + GameInput.label(EditorActions.DEBUG_SHADER)
            + " to toggle layer debug shader ["
            + DrawSystem.shadersActiveLastFrame()
            + "] )",
        new Point(10.0f, Game.windowHeight() - 10.0f));

    // Draw feedback message if timer > 0
    if (feedbackMessageTimer > 0.0f && !feedbackMessage.isEmpty()) {
      GlyphLayout layout = new GlyphLayout(FONT, feedbackMessage);
      float x = 10;
      float y = 10 + layout.height;
      DebugDrawSystem.drawText(FONT, feedbackMessage, new Point(x, y), feedbackMessageColor);
      feedbackMessageTimer -= Gdx.graphics.getDeltaTime();
      if (feedbackMessageTimer <= 0.0f) {
        feedbackMessage = "";
      }
    }

    // Draw level boundaries in green with alpha 0.3f
    Tile[][] layout = Game.currentLevel().orElseThrow().layout();
    DebugDrawSystem.drawRectangleOutline(
        0, 0, layout[0].length, layout.length, new Color(0, 1, 0, 0.3f));

    currentModeInstance.render();
  }

  @Override
  public void execute() {
    if (activateOnStart && Game.player().isPresent()) {
      activateOnStart = false;
      active(true);
    }

    GameInput.activate(TOGGLE_INPUT);
    if (GameInput.justPressed(EditorActions.TOGGLE)) {
      active(!active);
    }

    if (active) loadSettingsForCurrentLevel();
    updateUI();

    if (!active) return;
    Entity editorCamera = editorCameraEntity();

    // Dialogs and text fields above the editor own the input.
    if (GameInput.covered(EDITOR_INPUT)) {
      currentModeInstance.onCursorLeaveWorld();
      return;
    }

    if (GameInput.justPressed(EditorActions.DEBUG_SHADER)) {
      toggleDebugShader();
    }

    boolean cursorCaptured = uiCapturesCursor();
    if (cursorCaptured) {
      currentModeInstance.onCursorLeaveWorld();
    }
    updateFreeCamera(editorCamera, !cursorCaptured);

    Mode previousMode = currentMode;
    for (int mode = 0; mode < EditorActions.MODES.length; mode++) {
      if (GameInput.justPressed(EditorActions.MODES[mode])) currentMode(Mode.getMode(mode));
    }

    if (!internalStopped || previousMode != currentMode) {
      if (!cursorCaptured) {
        currentModeInstance.execute();
      }
    }
  }

  private static void updateFreeCamera(Entity editorCamera, boolean allowZoom) {
    PositionComponent positionComponent = editorCamera.fetch(PositionComponent.class).orElseThrow();

    int horizontal =
        (GameInput.pressed(EditorActions.CAMERA_RIGHT) ? 1 : 0)
            - (GameInput.pressed(EditorActions.CAMERA_LEFT) ? 1 : 0);
    int vertical =
        (GameInput.pressed(EditorActions.CAMERA_UP) ? 1 : 0)
            - (GameInput.pressed(EditorActions.CAMERA_DOWN) ? 1 : 0);
    engine.utils.Vector2 direction = engine.utils.Vector2.of(horizontal, vertical);
    if (!direction.isZero()) {
      float speedMultiplier =
          GameInput.pressed(EditorActions.CAMERA_FAST) ? FREE_CAMERA_SHIFT_SPEED_MULTIPLIER : 1f;
      float distance =
          FREE_CAMERA_SPEED
              * speedMultiplier
              * CameraSystem.camera().zoom
              * Math.min(Gdx.graphics.getDeltaTime(), 0.1f);
      positionComponent.position(
          positionComponent.position().translate(direction.normalize().scale(distance)));
    }

    float scrollAmount = GameInput.axis(EditorActions.ZOOM);
    if (allowZoom && scrollAmount != 0f) {
      CameraSystem.camera().zoom =
          Math.max(
              MIN_FREE_CAMERA_ZOOM,
              CameraSystem.camera().zoom + scrollAmount * FREE_CAMERA_ZOOM_STEP);
    }
  }

  private static Entity editorCameraEntity() {
    if (editorCameraEntity == null
        || Game.findEntityById(editorCameraEntity.id())
            .filter(entity -> entity == editorCameraEntity)
            .isEmpty()) {
      if (editorCameraEntity != null) {
        editorCameraEntity.remove(CameraComponent.class);
        editorCameraEntity.remove(PositionComponent.class);
      }
      editorCameraEntity = Entity.createLocalEntity("Level Editor Camera");
      Game.add(editorCameraEntity);
      if (active) {
        editorCameraEntity.add(
            new PositionComponent(
                CameraSystem.camera().position.x, CameraSystem.camera().position.y));
        editorCameraEntity.add(editorCameraComponent);
      }
    }
    return editorCameraEntity;
  }

  /**
   * Checks whether the level editor UI currently owns the mouse cursor, so the active mode must not
   * act on it. A drag that started in the level keeps going over the panels; a press on a panel
   * belongs to the UI and never starts an editor action.
   *
   * @return true if the active mode must not act on the cursor, false otherwise.
   */
  private static boolean uiCapturesCursor() {
    // Pick acts on its press only, so only place and alt drag.
    boolean dragging =
        GameInput.pressed(EditorActions.PLACE) || GameInput.pressed(EditorActions.ALT);
    return !dragging && ui != null && ui.isCursorOverUI();
  }

  private void toggleDebugShader() {
    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);
    if (debugShaderActive) {
      ds.levelShaders().remove(DEBUG_SHADER_KEY);
      ds.entityDepthShaders(DepthLayer.Player.depth()).remove(DEBUG_SHADER_KEY);
      ds.entityDepthShaders(DepthLayer.BackgroundDeco.depth()).remove(DEBUG_SHADER_KEY);
      ds.entityDepthShaders(DepthLayer.Normal.depth()).remove(DEBUG_SHADER_KEY);
      ds.sceneShaders().remove(DEBUG_SHADER_KEY);
    } else {
      ds.levelShaders().add(DEBUG_SHADER_KEY, new OutlineShader(3).color(Color.BLUE));
      ds.entityDepthShaders(DepthLayer.Player.depth())
          .add(DEBUG_SHADER_KEY, new OutlineShader(3).color(Color.RED));
      ds.entityDepthShaders(DepthLayer.BackgroundDeco.depth())
          .add(DEBUG_SHADER_KEY, new OutlineShader(3).color(Color.GREEN));
      ds.entityDepthShaders(DepthLayer.Normal.depth())
          .add(DEBUG_SHADER_KEY, new OutlineShader(3).color(Color.WHITE));
      ds.sceneShaders().add(DEBUG_SHADER_KEY, new PassthroughShader().debugPMA(true));
    }
    debugShaderActive = !debugShaderActive;
  }

  /**
   * Shows a feedback message on the screen for a short duration. Also logs the message as INFO,
   * WARN or ERROR depending on the color (red = error, yellow = warn, else = info).
   *
   * @param message the message to show
   * @param color the color of the message.
   */
  public static void showFeedback(String message, Color color) {
    feedbackMessage = message;
    feedbackMessageColor = color;
    feedbackMessageTimer = FEEDBACK_MESSAGE_DURATION;
    if (color == Color.RED) {
      LOGGER.error(message);
    } else if (color == Color.YELLOW) {
      LOGGER.warn(message);
    } else {
      LOGGER.info(message);
    }
  }

  @Override
  public void stop() {
    internalStopped = true;
  }

  @Override
  public void run() {
    internalStopped = false;
  }

  /** The editor reads input and moves its camera with the frame rate. */
  @Override
  public boolean runsPerFrame() {
    return true;
  }

  /** The available modes of the level editor. */
  public enum Mode {
    /** Mode to place and remove level tiles. */
    Tiles,
    /** Mode to place and remove decorations. */
    Decos,
    /** Mode to place and remove named points. */
    Points,
    /** Mode to author registered prefab instances. */
    Prefabs,
    /** Mode to define the start (spawn) tiles. */
    StartTiles,
    /** Mode to resize, shift, and save the current level. */
    Settings;

    /**
     * Gets the mode with the given index.
     *
     * @param number the index of the mode.
     * @return the mode with the given index.
     */
    public static Mode getMode(int number) {
      if (number < 0 || number >= values().length) {
        throw new IllegalArgumentException("Invalid mode number: " + number);
      }
      return values()[number];
    }

    /**
     * Creates a new instance of the {@link LevelEditorMode} belonging to this mode.
     *
     * @param onLevelChanged invoked after the mode changes the level.
     * @return a new mode instance.
     */
    public LevelEditorMode getModeInstance(Runnable onLevelChanged) {
      return switch (this) {
        case Tiles -> new TilesMode(onLevelChanged);
        case Decos -> new DecoMode(onLevelChanged);
        case Points -> new PointMode(onLevelChanged);
        case Prefabs -> new PrefabMode(onLevelChanged);
        case StartTiles -> new StartTilesMode(onLevelChanged);
        case Settings -> new SettingsMode(onLevelChanged);
      };
    }
  }
}
