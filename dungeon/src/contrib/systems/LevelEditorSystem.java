package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import contrib.utils.systems.levelEditor.*;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.Tile;
import core.utils.*;

/**
 * The LevelEditorSystem is responsible for handling the level editor. It allows the user to change
 * the {@link DungeonLevel} layout by setting different tiles. The user can set the following tiles:
 * skip, pit, floor, wall, hole, exit, door, and custom points. The user can also fill an area with
 * floor tiles and save the current dungeon.
 */
public class LevelEditorSystem extends System {

  public static final BitmapFont FONT = FontHelper.getDefaultFont(24);
  public static final BitmapFont FONT_SMALL = FontHelper.getDefaultFont(16);

  private static boolean internalStopped = false;
  private static boolean active = false;
  private static final int TOGGLE_ACTIVE = Input.Keys.F4;

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
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(TOGGLE_ACTIVE)) {
      active = !active;
    }

    if (!active) {
      return;
    }

    Mode previousMode = currentMode;
    if (Gdx.input.isKeyPressed(MODE_1)) {
      currentMode = Mode.getMode(0);
    } else if (Gdx.input.isKeyPressed(MODE_2)) {
      currentMode = Mode.getMode(1);
    } else if (Gdx.input.isKeyPressed(MODE_3)) {
      currentMode = Mode.getMode(2);
    } else if (Gdx.input.isKeyPressed(MODE_4)) {
      currentMode = Mode.getMode(3);
    } else if (Gdx.input.isKeyPressed(MODE_5)) {
      currentMode = Mode.getMode(4);
    } else if (Gdx.input.isKeyPressed(MODE_6)) {
      currentMode = Mode.getMode(5);
    } else if (Gdx.input.isKeyPressed(MODE_7)) {
      currentMode = Mode.getMode(6);
    }

    if (!internalStopped || previousMode != currentMode) {
      if (previousMode != currentMode) {
        currentModeInstance.onExit();
        currentModeInstance = currentMode.getModeInstance();
        currentModeInstance.onEnter();
      }
      currentModeInstance.execute();
    }

    String status = currentModeInstance.getStatusText();
    // Prepend to status: mode selection info. a horizontal list of all mode numbers, separated by
    // |. active mode is in [brackets]
    StringBuilder modeSelection = new StringBuilder("Level Editor v2 | Modes: ");
    for (int i = 0; i < Mode.values().length; i++) {
      if (i > 0) {
        modeSelection.append(" | ");
      }
      if (i == currentMode.ordinal()) {
        modeSelection.append("[").append(i + 1).append("]");
      } else {
        modeSelection.append(i + 1);
      }
    }
    modeSelection.append("\n");
    status = modeSelection + status;
    DebugDrawSystem.drawText(FONT, status, new Point(10.0f, Game.windowHeight() - 10.0f));

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
  }

  public static void showFeedback(String message, Color color) {
    feedbackMessage = message;
    feedbackMessageColor = color;
    feedbackMessageTimer = FEEDBACK_MESSAGE_DURATION;
    if (color == Color.RED) {
      LOGGER.warning(message);
    } else if (color == Color.YELLOW) {
      LOGGER.warning(message);
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

  private enum Mode {
    Tiles,
    Decos,
    Points,
    LevelBounds,
    ShiftLevel,
    StartTiles,
    SaveLevel;

    public static Mode getMode(int number) {
      if (number < 0 || number >= values().length) {
        throw new IllegalArgumentException("Invalid mode number: " + number);
      }
      return values()[number];
    }

    public LevelEditorMode getModeInstance() {
      return switch (this) {
        case Tiles -> new TilesMode();
        case Decos -> new DecoMode();
        case Points -> new PointMode();
        case LevelBounds -> new LevelBoundsMode();
        case ShiftLevel -> new ShiftLevelMode();
        case StartTiles -> new StartTilesMode();
        case SaveLevel -> new SaveMode();
      };
    }
  }
}
