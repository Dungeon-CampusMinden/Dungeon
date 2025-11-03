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

public abstract class LevelEditorMode {

  private static DungeonLevel level = null;
  public static final int SECONDARY_UP = Input.Keys.C;
  public static final int SECONDARY_DOWN = Input.Keys.Z;
  public static final int PRIMARY_UP = Input.Keys.E;
  public static final int PRIMARY_DOWN = Input.Keys.Q;
  public static final int TERTIARY = Input.Keys.X;

  private final String name;
  private final Map<Integer, String> controls = new LinkedHashMap<>();

  public LevelEditorMode(String name) {
    this.name = name;
    Map<Integer, String> controls = getControls();
    if (controls != null) {
      this.controls.putAll(controls);
    }
  }

  public String getName() {
    return name;
  }

  public void doExecute() {
    LevelSystem.level().ifPresent(level -> LevelEditorMode.level = (DungeonLevel) level);
    execute();
  }

  public abstract void execute();

  public abstract void onEnter();

  public abstract void onExit();

  public String getFullStatusText() {
    StringBuilder status = new StringBuilder("--- " + getName() + " ---");
    addControlsToStatus(status, controls);
    status.append("\n\nSettings:\n").append(getStatusText());
    return status.toString();
  }

  public abstract String getStatusText();

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
    OffGrid,
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
        default -> position;
      };
    }
  }
}
