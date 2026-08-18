package feature.leveleditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.System;
import engine.input.CursorUtils;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.systems.LevelSystem;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.systems.LevelEditorSystem;
import java.util.Collections;
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

  /** Render call for this mode. */
  public void render() {
    // Default: do nothing
  }

  /** Called when entering this mode. */
  public abstract void onEnter();

  /** Called when exiting this mode. */
  public abstract void onExit();

  /**
   * Gets the header text shown at the top of the details panel of the level editor.
   *
   * @return the header text.
   */
  public String getHeader() {
    return getName();
  }

  /**
   * Builds the mode specific part of the details panel.
   *
   * <p>Called once when this mode becomes the active mode. Implementations should add their
   * controls and displays to the given table. The default implementation adds nothing.
   *
   * @param content the table the mode specific content should be added to.
   */
  public void buildDetailsUI(Table content) {
    // Default: no mode specific content
  }

  /**
   * Refreshes the dynamic parts of the mode specific content built in {@link
   * #buildDetailsUI(Table)}.
   *
   * <p>Called every frame while this mode is active and the details panel is visible.
   */
  public void updateDetailsUI() {
    // Default: nothing to refresh
  }

  /**
   * Gets additional information this mode wants to display to the user in the details panel.
   *
   * <p>Called every frame, so the returned text may be dynamic. If the returned text is {@code
   * null} or blank, the section is hidden.
   *
   * @return the additional information text.
   */
  public String additionalInformation() {
    return "";
  }

  /**
   * Gets the controls for this mode.
   *
   * @return A map of key codes to their action descriptions.
   */
  public abstract Map<Integer, String> getControls();

  /**
   * Gets the controls of this mode as they were resolved when this mode was created.
   *
   * @return an unmodifiable map of key codes to their action descriptions.
   */
  public Map<Integer, String> controls() {
    return Collections.unmodifiableMap(controls);
  }

  /**
   * Converts a key or mouse button code into a displayable name.
   *
   * <p>Also contains a quick and dirty fix for the german keyboard layout where Y and Z are
   * swapped.
   *
   * @param key the key code, or one of {@link Input.Buttons#LEFT} / {@link Input.Buttons#RIGHT}
   *     when {@code mouse} is set.
   * @return the key as string.
   */
  public static String keyName(int key) {
    if (key == Input.Buttons.LEFT) {
      return "LMB";
    } else if (key == Input.Buttons.RIGHT) {
      return "RMB";
    } else if (key == Input.Keys.Y) {
      return "Z";
    } else if (key == Input.Keys.Z) {
      return "Y";
    }
    return Input.Keys.toString(key);
  }

  protected DungeonLevel getLevel() {
    if (level == null) {
      Game.currentLevel()
          .filter(DungeonLevel.class::isInstance)
          .map(DungeonLevel.class::cast)
          .ifPresent(currentLevel -> level = currentLevel);
    }
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
    return CursorUtils.positionInWorld();
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
