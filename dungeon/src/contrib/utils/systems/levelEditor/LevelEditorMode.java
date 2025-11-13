package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Input;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;
import core.utils.Vector2;
import java.util.LinkedHashMap;
import java.util.Map;

/** Abstract base class for different modes in the Level Editor. */
public abstract class LevelEditorMode {

  private static DungeonLevel level = null;

  /** Primary action button. Direction UP */
  public static final int PRIMARY_UP = Input.Keys.E;

  /** Primary action button. Direction DOWN */
  public static final int PRIMARY_DOWN = Input.Keys.Q;

  /** Secondary action button. Direction UP */
  public static final int SECONDARY_UP = Input.Keys.C;

  /** Secondary action button. Direction DOWN. */
  public static final int SECONDARY_DOWN = Input.Keys.Z;

  /** Tertiary action button. */
  public static final int TERTIARY = Input.Keys.X;

  /** Quaternary action button. */
  public static final int QUARTERNARY = Input.Keys.V;

  private final String name;
  private final Map<Integer, String> controls = new LinkedHashMap<>();

  /**
   * Constructs a new LevelEditorMode with the given name.
   *
   * @param name The name of this mode.
   */
  public LevelEditorMode(String name) {
    this.name = name;
    Map<Integer, String> controls = getControls();
    if (controls != null) {
      this.controls.putAll(controls);
    }
  }

  /**
   * Gets the name of this mode.
   *
   * @return The name of this mode.
   */
  public String getName() {
    return name;
  }

  /** Decorator method to assign the level reference before executing the mode logic. */
  public void doExecute() {
    LevelSystem.level().ifPresent(level -> LevelEditorMode.level = (DungeonLevel) level);
    execute();
  }

  /** Executes the logic for this mode. Called every frame. */
  public abstract void execute();

  /** Called when entering this mode. */
  public abstract void onEnter();

  /** Called when exiting this mode. */
  public abstract void onExit();

  /**
   * Decorator method to get the full status text including the mode name and controls.
   *
   * @return The full status text.
   */
  public String getFullStatusText() {
    StringBuilder status = new StringBuilder("--- " + getName() + " ---");
    addControlsToStatus(status, controls);
    status.append("\n\nSettings:\n").append(getStatusText());
    return status.toString();
  }

  /**
   * Gets the status text for this mode.
   *
   * @return The status text.
   */
  public abstract String getStatusText();

  /**
   * Gets the controls for this mode.
   *
   * @return A map of key codes to their action descriptions.
   */
  public abstract Map<Integer, String> getControls();

  protected DungeonLevel getLevel() {
    return level;
  }

  protected LevelEditorSystem getSystem() {
    System s = Game.systems().get(LevelEditorSystem.class);
    if (s instanceof LevelEditorSystem les) {
      return les;
    }
    throw new IllegalStateException("LevelEditorSystem not found in Game systems.");
  }

  protected Point getCursorPosition() {
    return SkillTools.cursorPositionAsPoint();
  }

  protected void setTile(Point position, LevelElement element) {
    Tile tile = LevelSystem.level().orElse(null).tileAt(position).orElse(null);
    if (tile == null) {
      return;
    }
    LevelSystem.level().orElse(null).changeTileElementType(tile, element);
    // Also set the tiles around the position, to update their sprites for the new neighboring tile
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        Point neighborPos = position.translate(Vector2.of(dx, dy));
        Tile neighborTile = LevelSystem.level().orElse(null).tileAt(neighborPos).orElse(null);
        if (neighborTile != null) {
          LevelSystem.level()
              .orElse(null)
              .changeTileElementType(neighborTile, neighborTile.levelElement());
        }
      }
    }
  }

  protected void addControlsToStatus(StringBuilder status, Map<Integer, String> controls) {
    status.append("\nControls:");
    for (Map.Entry<Integer, String> entry : controls.entrySet()) {
      // Special handling for mouse buttons
      if (entry.getKey() == Input.Buttons.LEFT || entry.getKey() == Input.Buttons.RIGHT) {
        status
            .append("\n- ")
            .append(entry.getKey() == Input.Buttons.LEFT ? "LMB" : "RMB")
            .append(": ")
            .append(entry.getValue());
        continue;
      }
      status
          .append("\n- ")
          .append(keyToString(entry.getKey()))
          .append(": ")
          .append(entry.getValue());
    }
  }

  /**
   * Quick and dirty fix for the german keyboard layout where Y and Z are swapped.
   *
   * @param key the key code
   * @return the key as string, with Y and Z swapped for german layout
   */
  private String keyToString(int key) {
    if (key == Input.Keys.Y) {
      return "Z";
    } else if (key == Input.Keys.Z) {
      return "Y";
    }
    return Input.Keys.toString(key);
  }

  protected enum SnapMode {
    OnGrid,
    QuarterGrid,
    PixelGrid,
    OffGrid,
    CheckerGridEven,
    CheckerGridOdd,
    ;

    SnapMode previousMode() {
      return values()[(this.ordinal() - 1 + values().length) % values().length];
    }

    SnapMode nextMode() {
      return values()[(this.ordinal() + 1) % values().length];
    }

    Point getPosition(Point position) {
      return switch (this) {
        case OnGrid ->
            new Point((float) Math.floor(position.x()), (float) Math.floor(position.y()));
        case QuarterGrid ->
            new Point(
                (float) Math.floor(position.x() * 4) / 4.0f,
                (float) Math.floor(position.y() * 4) / 4.0f);
        case PixelGrid ->
            new Point(
                (float) Math.floor(position.x() * 16) / 16.0f,
                (float) Math.floor(position.y() * 16) / 16.0f);
        case CheckerGridEven, CheckerGridOdd -> {
          int parity = (this == CheckerGridEven) ? 0 : 1;

          // Input represents tile center → shift down-left by 0.5 to get corner-based position
          float px = position.x() - 0.5f;
          float py = position.y() - 0.5f;

          float gx = (float) Math.floor(px);
          float gy = (float) Math.floor(py);

          float bestX = gx;
          float bestY = gy;
          float bestDist = Float.MAX_VALUE;

          // Evaluate 4 nearest grid corners and choose the closest valid checker cell
          for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
              float cx = gx + dx;
              float cy = gy + dy;
              if (((int) (cx + cy)) % 2 == parity) {
                float dist = (px - cx) * (px - cx) + (py - cy) * (py - cy);
                if (dist < bestDist) {
                  bestDist = dist;
                  bestX = cx;
                  bestY = cy;
                }
              }
            }
          }

          // Output should be the bottom-left *corner* of the snapped tile
          yield new Point(bestX, bestY);
        }
        default -> position;
      };
    }

    boolean checkBlocked() {
      return this == OnGrid
          || this == QuarterGrid
          || this == CheckerGridEven
          || this == CheckerGridOdd;
    }
  }
}
