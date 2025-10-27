package contrib.utils.systems;

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
import java.util.Map;

public abstract class LevelEditorMode {

  private static DungeonLevel level = null;
  protected static final int SECONDARY_UP = Input.Keys.C;
  protected static final int SECONDARY_DOWN = Input.Keys.Y;
  protected static final int PRIMARY_UP = Input.Keys.E;
  protected static final int PRIMARY_DOWN = Input.Keys.Q;

  private final String name;

  public LevelEditorMode(String name) {
    this.name = name;
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

  public abstract String getStatusText();

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
      status
          .append("\n- ")
          .append(Input.Keys.toString(entry.getKey()))
          .append(": ")
          .append(entry.getValue());
    }
  }

  protected enum SnapMode {
    OnGrid,
    OneTenthGrid,
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
        case OneTenthGrid ->
            new Point(
                (float) Math.floor(position.x() * 10) / 10.0f,
                (float) Math.floor(position.y() * 10) / 10.0f);
        default -> position;
      };
    }
  }
}
