package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.loader.DungeonSaver;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import engine.utils.Tuple;
import feature.leveleditor.ui.ActionSetting;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.NumberSetting;
import feature.leveleditor.ui.StringSetting;
import feature.systems.LevelEditorSystem;
import feature.systems.PositionSync;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The settings mode for resizing, shifting, and saving the level. */
public class SettingsMode extends LevelEditorMode {

  private static final int MIN_LEVEL_SIZE = 1;
  private static final int MAX_LEVEL_SIZE = 1000;

  private static boolean autoSave = true;

  private NumberSetting heightSetting;
  private NumberSetting widthSetting;
  private StringSetting savePathSetting;
  private BooleanSetting autoSaveSetting;

  /** Constructs a new settings mode. */
  public SettingsMode() {
    super("Settings");
  }

  @Override
  public void buildDetailsUI(Table content) {
    content.clearChildren();
    heightSetting =
        new NumberSetting(
            "Level Height",
            MIN_LEVEL_SIZE,
            MAX_LEVEL_SIZE,
            () -> getLevel().layout().length,
            height -> resizeLevel(getLevel().layout()[0].length, height));
    widthSetting =
        new NumberSetting(
            "Level Width",
            MIN_LEVEL_SIZE,
            MAX_LEVEL_SIZE,
            () -> getLevel().layout()[0].length,
            width -> resizeLevel(width, getLevel().layout().length));
    savePathSetting =
        new StringSetting(
            "Save Level To",
            LevelEditorSystem::pathToLevels,
            LevelEditorSystem::pathToLevels);
    autoSaveSetting = new BooleanSetting("Auto-Save", () -> autoSave, value -> autoSave = value);

    content.add(heightSetting).growX().row();
    content.add(widthSetting).growX().padTop(4f).row();
    content.add(new ActionSetting("Shift Level Up", () -> shiftLevel(0, 1)))
        .growX()
        .padTop(8f)
        .row();
    content.add(new ActionSetting("Shift Level Down", () -> shiftLevel(0, -1)))
        .growX()
        .padTop(4f)
        .row();
    content.add(new ActionSetting("Shift Level Right", () -> shiftLevel(1, 0)))
        .growX()
        .padTop(4f)
        .row();
    content.add(new ActionSetting("Shift Level Left", () -> shiftLevel(-1, 0)))
        .growX()
        .padTop(4f)
        .row();
    content.add(Scene2dElementFactory.createHorizontalDivider())
        .growX()
        .padTop(8f)
        .padBottom(8f)
        .row();
    content.add(savePathSetting).growX().row();
    content.add(autoSaveSetting).growX().padTop(8f).row();
    content.add(new ActionSetting("Save Level", this::saveLevel))
        .growX()
        .padTop(8f)
        .row();
  }

  @Override
  public void updateDetailsUI() {
    if (heightSetting != null) heightSetting.refresh();
    if (widthSetting != null) widthSetting.refresh();
    if (savePathSetting != null) savePathSetting.refresh();
    if (autoSaveSetting != null) autoSaveSetting.refresh();
  }

  @Override
  public void execute() {}

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String additionalInformation() {
    return "Level Editor v3";
  }

  @Override
  public Map<Integer, String> getControls() {
    return Map.of();
  }

  private void resizeLevel(int width, int height) {
    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();
    int oldHeight = layout.length;
    int oldWidth = layout[0].length;
    if (oldWidth == width && oldHeight == height) return;

    List<Point> startPositions = level.startTiles().stream().map(Tile::position).toList();
    LevelElement[][] newLayout = new LevelElement[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        newLayout[y][x] =
            y < oldHeight && x < oldWidth ? layout[y][x].levelElement() : LevelElement.SKIP;
      }
    }

    level.startTiles().clear();
    level.setLayout(newLayout);
    startPositions.stream()
        .map(level::tileAt)
        .flatMap(Optional::stream)
        .forEach(level.startTiles()::add);
    LevelEditorSystem.showFeedback(
        "Resized level to: (" + width + ", " + height + ")", Color.WHITE);
    saveIfEnabled();
  }

  private void shiftLevel(int x, int y) {
    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();
    if (!canShift(layout, x, y)) {
      LevelEditorSystem.showFeedback("Cannot shift level: overwriting non-SKIP tiles!", Color.RED);
      return;
    }

    List<Point> startPositions =
        level.startTiles().stream().map(tile -> tile.position().translate(x, y)).toList();
    LevelElement[][] newLayout = new LevelElement[layout.length][layout[0].length];
    for (int row = 0; row < layout.length; row++) {
      for (int column = 0; column < layout[0].length; column++) {
        int oldRow = row - y;
        int oldColumn = column - x;
        newLayout[row][column] =
            oldRow >= 0
                    && oldRow < layout.length
                    && oldColumn >= 0
                    && oldColumn < layout[0].length
                ? layout[oldRow][oldColumn].levelElement()
                : LevelElement.SKIP;
      }
    }

    level.startTiles().clear();
    level.setLayout(newLayout);
    startPositions.stream()
        .map(level::tileAt)
        .flatMap(Optional::stream)
        .forEach(level.startTiles()::add);

    level.namedPoints().replaceAll((name, point) -> point.translate(x, y));
    level.decorations()
        .replaceAll(
            decoration ->
                new Tuple<>(decoration.a(), decoration.b().translate(x, y)));
    Game.levelEntities(Set.of(PositionComponent.class))
        .forEach(
            entity -> {
              PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();
              Point oldPosition = position.position();
              position.position(oldPosition.translate(x, y));
              PositionSync.syncPosition(entity);
            });

    String directionName = x == 1 ? "RIGHT" : x == -1 ? "LEFT" : y == 1 ? "UP" : "DOWN";
    LevelEditorSystem.showFeedback("Shifted level " + directionName, Color.WHITE);
    saveIfEnabled();
  }

  private boolean canShift(Tile[][] layout, int x, int y) {
    int rows = layout.length;
    int columns = layout[0].length;
    if (x == 1) {
      for (int row = 0; row < rows; row++) {
        if (layout[row][columns - 1].levelElement() != LevelElement.SKIP) return false;
      }
    } else if (x == -1) {
      for (int row = 0; row < rows; row++) {
        if (layout[row][0].levelElement() != LevelElement.SKIP) return false;
      }
    }
    if (y == 1) {
      for (int column = 0; column < columns; column++) {
        if (layout[rows - 1][column].levelElement() != LevelElement.SKIP) return false;
      }
    } else if (y == -1) {
      for (int column = 0; column < columns; column++) {
        if (layout[0][column].levelElement() != LevelElement.SKIP) return false;
      }
    }
    return true;
  }

  private void saveLevel() {
    DungeonSaver.saveCurrentDungeon(LevelEditorSystem.pathToLevels());
    LevelEditorSystem.showFeedback("Exported level to clipboard!", Color.GREEN);
  }

  private void saveIfEnabled() {
    if (autoSave) saveLevel();
  }
}
