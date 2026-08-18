package feature.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.InputComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.systems.DrawSystem;
import engine.systems.input.InputManager;
import engine.utils.FontHelper;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.PassthroughShader;
import engine.utils.logging.DungeonLogger;
import feature.components.HealthComponent;
import feature.leveleditor.DecoMode;
import feature.leveleditor.LevelBoundsMode;
import feature.leveleditor.LevelEditorMode;
import feature.leveleditor.PointMode;
import feature.leveleditor.SaveMode;
import feature.leveleditor.ShiftLevelMode;
import feature.leveleditor.StartTilesMode;
import feature.leveleditor.TilesMode;
import feature.leveleditor.ui.LevelEditorUI;
import java.util.Map;

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
  private static final int TOGGLE_ACTIVE = Input.Keys.F4;
  private static String pathToLevels = "";

  private static final int TOGGLE_DEBUG_SHADER = Input.Keys.SPACE;
  private boolean debugShaderActive = false;
  private static final String DEBUG_SHADER_KEY = "LevelEditorSystem_debug";

  private static Mode currentMode = Mode.Tiles;
  private static LevelEditorMode currentModeInstance = currentMode.getModeInstance();
  private static final int MODE_1 = Input.Keys.NUM_1;
  private static final int MODE_2 = Input.Keys.NUM_2;
  private static final int MODE_3 = Input.Keys.NUM_3;
  private static final int MODE_4 = Input.Keys.NUM_4;
  private static final int MODE_5 = Input.Keys.NUM_5;
  private static final int MODE_6 = Input.Keys.NUM_6;
  private static final int MODE_7 = Input.Keys.NUM_7;

  private static String feedbackMessage = "";
  private static Color feedbackMessageColor = Color.WHITE;
  private static float feedbackMessageTimer = 0.0f;
  private static final float FEEDBACK_MESSAGE_DURATION = 3.0f; // seconds

  private static Map<Integer, InputComponent.InputData> playerClallbacks = null;

  private static LevelEditorUI ui = null;

  /**
   * Creates a new LevelEditorSystem.
   *
   * @param pathToLevels The folder in which the file is placed this system.
   */
  public LevelEditorSystem(String pathToLevels) {
    super();
    LevelEditorSystem.pathToLevels = pathToLevels;
  }

  /** Creates a new LevelEditorSystem. */
  public LevelEditorSystem() {
    super();
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
    LevelEditorSystem.active = active;
    Entity player = Game.player().orElseThrow();
    if (active) {
      player
          .fetch(InputComponent.class)
          .ifPresent(
              pc -> {
                playerClallbacks = pc.callbacks();
                pc.removeCallback(LevelEditorMode.PRIMARY_UP);
                pc.removeCallback(LevelEditorMode.PRIMARY_DOWN);
                pc.removeCallback(LevelEditorMode.SECONDARY_UP);
                pc.removeCallback(LevelEditorMode.SECONDARY_DOWN);
                pc.removeCallback(LevelEditorMode.TERTIARY);
                pc.removeCallback(Input.Buttons.LEFT);
                pc.removeCallback(Input.Buttons.RIGHT);
              });
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
      if (playerClallbacks != null) {
        player
            .fetch(InputComponent.class)
            .ifPresent(
                pc -> {
                  playerClallbacks.forEach(
                      ((key, value) ->
                          pc.registerCallback(
                              key, value.callback(), value.repeat(), value.pauseable())));
                });
        playerClallbacks = null;
        player
            .fetch(HealthComponent.class)
            .ifPresent(
                hc -> {
                  hc.godMode(false);
                });
      }
      currentModeInstance.onExit();
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
    currentModeInstance = mode.getModeInstance();
    currentModeInstance.onEnter();
    if (ui != null) {
      ui.modePanel().selected(mode);
      ui.detailsPanel().mode(currentModeInstance);
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
        "( SPACE to toggle layer debug shader [" + DrawSystem.shadersActiveLastFrame() + "] )",
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

    if (InputManager.isKeyJustPressed(TOGGLE_ACTIVE)) {
      active(!active);
    }

    updateUI();

    if (!active) return;

    if (Game.player().map(Game.hud()::hasOpenUI).orElse(false)) {
      return;
    }

    // Do not react to editor keys while the user types into a text field of the editor UI
    if (Game.stage().map(stage -> stage.getKeyboardFocus() instanceof TextField).orElse(false)) {
      return;
    }

    if (InputManager.isKeyJustPressed(TOGGLE_DEBUG_SHADER)) {
      toggleDebugShader();
    }

    Mode previousMode = currentMode;
    if (InputManager.isKeyPressed(MODE_1)) {
      currentMode(Mode.getMode(0));
    } else if (InputManager.isKeyPressed(MODE_2)) {
      currentMode(Mode.getMode(1));
    } else if (InputManager.isKeyPressed(MODE_3)) {
      currentMode(Mode.getMode(2));
    } else if (InputManager.isKeyPressed(MODE_4)) {
      currentMode(Mode.getMode(3));
    } else if (InputManager.isKeyPressed(MODE_5)) {
      currentMode(Mode.getMode(4));
    } else if (InputManager.isKeyPressed(MODE_6)) {
      currentMode(Mode.getMode(5));
    } else if (InputManager.isKeyPressed(MODE_7)) {
      currentMode(Mode.getMode(6));
    }

    if (!internalStopped || previousMode != currentMode) {
      currentModeInstance.doExecute();
    }
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

  /** The available modes of the level editor. */
  public enum Mode {
    /** Mode to place and remove level tiles. */
    Tiles('T'),
    /** Mode to place and remove decorations. */
    Decos('D'),
    /** Mode to place and remove named points. */
    Points('P'),
    /** Mode to change the boundaries of the level. */
    LevelBounds('B'),
    /** Mode to shift the whole level layout. */
    ShiftLevel('M'),
    /** Mode to define the start (spawn) tiles. */
    StartTiles('A'),
    /** Mode to save the current level. */
    SaveLevel('S');

    private final char letter;

    Mode(char letter) {
      this.letter = letter;
    }

    /**
     * Gets the single letter representing this mode in the mode selection panel.
     *
     * @return the letter of this mode.
     */
    public char letter() {
      return letter;
    }

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
     * @return a new mode instance.
     */
    public LevelEditorMode getModeInstance() {
      return switch (this) {
        case Tiles -> new TilesMode();
        case Decos -> new DecoMode();
        case Points -> new PointMode();
        case LevelBounds -> new LevelBoundsMode();
        case ShiftLevel -> new ShiftLevelMode();
        case StartTiles -> new StartTilesMode();
        case SaveLevel -> new SaveMode(pathToLevels);
      };
    }
  }
}
