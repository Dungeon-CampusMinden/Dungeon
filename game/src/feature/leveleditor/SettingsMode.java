package feature.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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
import feature.hud.elements.RichLabel;
import feature.leveleditor.ui.ActionSetting;
import feature.leveleditor.ui.BooleanSetting;
import feature.leveleditor.ui.ModeDetailsPanel;
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
  private static final String EXISTING_FILE_WARNING =
      "File already exists, will be overwritten";

  private NumberSetting heightSetting;
  private NumberSetting widthSetting;
  private StringSetting savePathSetting;
  private RichLabel savePathStatusLabel;
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
    savePathStatusLabel = new RichLabel("", 12, Color.RED, false);
    savePathStatusLabel.setWrap(false);
    savePathStatusLabel.setVisible(false);
    autoSaveSetting =
        new BooleanSetting(
            "Auto-Save", LevelEditorSystem::autoSave, LevelEditorSystem::autoSave);

    content.add(heightSetting).growX().row();
    content.add(widthSetting).growX().padTop(4f).row();
    content.add(
            Scene2dElementFactory.createLabel(
                "Shift Level", 16, ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left()
        .padTop(8f)
        .row();

    Table shiftGrid = new Table();
    shiftGrid.top();
    shiftGrid.defaults().growX().uniformX().height(40f).pad(2f);
    shiftGrid.add();
    shiftGrid.add(new ActionSetting("Up", () -> shiftLevel(0, 1)));
    shiftGrid.add().row();
    shiftGrid.add(new ActionSetting("Left", () -> shiftLevel(-1, 0)));
    shiftGrid.add();
    shiftGrid.add(new ActionSetting("Right", () -> shiftLevel(1, 0))).row();
    shiftGrid.add();
    shiftGrid.add(new ActionSetting("Down", () -> shiftLevel(0, -1)));
    shiftGrid.add();
    content.add(shiftGrid).growX().row();

    content.add(Scene2dElementFactory.createHorizontalDivider())
        .growX()
        .padTop(8f)
        .padBottom(8f)
        .row();
    content.add(savePathSetting).growX().row();
    content.add(savePathStatusLabel).growX().left().padTop(2f).row();
    content.add(autoSaveSetting).growX().padTop(8f).row();
    content.add(new ActionSetting("Save Level", this::saveLevel))
        .growX()
        .padTop(8f)
        .row();
    updateSavePathStatus();
  }

  @Override
  public void updateDetailsUI() {
    if (heightSetting != null) heightSetting.refresh();
    if (widthSetting != null) widthSetting.refresh();
    if (savePathSetting != null) savePathSetting.refresh();
    updateSavePathStatus();
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
    saveIfEnabled();
  }

  private void shiftLevel(int x, int y) {
    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();
    String directionName = x == 1 ? "RIGHT" : x == -1 ? "LEFT" : y == 1 ? "UP" : "DOWN";
    if (!canShift(layout, x, y)) {
      LevelEditorSystem.showFeedback("Cannot shift level " + directionName + ": overwriting non-SKIP tiles!", Color.RED);
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
  }

  private void saveIfEnabled() {
    if (LevelEditorSystem.autoSave()) saveLevel();
  }

  private void updateSavePathStatus() {
    if (savePathStatusLabel == null) return;

    String savePath = LevelEditorSystem.pathToLevels();
    FileHandle saveFile =
        savePath == null || savePath.isBlank()
            ? null
            : Gdx.files.local(DungeonSaver.normalizeLevelFilePath(savePath));
    boolean fileExists =
        saveFile != null && saveFile.exists() && !saveFile.isDirectory();
    savePathStatusLabel.setVisible(fileExists);
    savePathStatusLabel.setText(fileExists ? EXISTING_FILE_WARNING : "");
  }
}
