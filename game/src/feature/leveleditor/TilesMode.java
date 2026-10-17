package feature.leveleditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.level.DungeonLevel;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.rendering.Atmosphere;
import engine.rendering.TileTextures;
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.leveleditor.ui.ColorSetting;
import feature.leveleditor.ui.IntegerSetting;
import feature.leveleditor.ui.LevelElementGrid;
import feature.leveleditor.ui.SelectSetting;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import feature.utils.CheckPatternPainter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The TilesMode allows the user to place different types of tiles in the level editor. */
public class TilesMode extends LevelEditorMode {

  private static int selectedTileIndexL = 1;
  private static int selectedTileIndexR = 2;
  private static int brushSize = 1;
  private static final int MAX_BRUSH_SIZE = 7;
  private static final int GRID_COLUMNS = 4;

  /** Preview of the void, which has no texture of its own. */
  private static final String SKIP_TEXTURE = "hud/cross.png";

  private LevelElementGrid grid = null;
  private IntegerSetting brushSizeSetting = null;
  private SelectSetting<DesignLabel> designSetting = null;
  private SelectSetting<Atmosphere.Preset> atmosphereSetting = null;
  private ColorSetting ambientSetting = null;
  private DesignLabel previewDesign = null;
  private boolean mouseLevelChangePending = false;

  /**
   * Gets the preview texture for an element in the selected level design.
   *
   * @param element the level element
   * @param designLabel the selected level design
   * @return the texture path used by the element button
   */
  public static String texturePath(LevelElement element, DesignLabel designLabel) {
    if (element == LevelElement.SKIP) return SKIP_TEXTURE;
    return TileTextures.preview(element, designLabel).pathString();
  }

  /**
   * Constructs a new TilesMode.
   *
   * @param levelChangedCallback invoked after tiles are changed.
   */
  public TilesMode(Runnable levelChangedCallback) {
    super("Tiles Mode", levelChangedCallback);
  }

  @Override
  public String getHeader() {
    return "Level Tiles";
  }

  @Override
  public void buildDetailsUI(Table content) {
    content.clearChildren();
    previewDesign = designLabel();
    grid =
        new LevelElementGrid(
            GRID_COLUMNS,
            element -> texturePath(element, designLabel()),
            () -> selectedTileIndexL,
            () -> selectedTileIndexR,
            ordinal -> selectedTileIndexL = ordinal,
            ordinal -> selectedTileIndexR = ordinal);
    designSetting =
        new SelectSetting<>(
            "Level Design",
            DesignLabel.values(),
            this::designLabel,
            this::designLabel,
            DesignLabel::name,
            true);
    atmosphereSetting =
        new SelectSetting<>(
            "Atmosphere",
            Atmosphere.Preset.values(),
            () -> getLevel().atmospherePreset(),
            this::atmospherePreset,
            Atmosphere.Preset::name,
            true);
    ambientSetting =
        new ColorSetting(
            "Ambient Light",
            () -> getLevel().atmosphere().ambient(),
            this::ambient,
            message -> LevelEditorSystem.showFeedback("Ambient Light: " + message, Color.YELLOW));
    brushSizeSetting =
        new IntegerSetting(
            "Brush Size", 1, MAX_BRUSH_SIZE, () -> brushSize, size -> brushSize = size);

    content.add(designSetting).growX().padBottom(10f).row();
    content.add(atmosphereSetting).growX().padBottom(10f).row();
    content.add(ambientSetting).growX().padBottom(10f).row();
    content.add(grid).growX().row();
    content.add(brushSizeSetting).growX().padTop(10f).row();
  }

  @Override
  public void updateDetailsUI() {
    if (designSetting != null) designSetting.refresh();
    if (atmosphereSetting != null) atmosphereSetting.refresh();
    if (ambientSetting != null) ambientSetting.refresh();
    DesignLabel currentDesign = designLabel();
    if (grid != null && currentDesign != previewDesign) {
      grid.refreshTextures();
      previewDesign = currentDesign;
    }
    if (grid != null) grid.refresh();
    if (brushSizeSetting != null) brushSizeSetting.refresh();
  }

  @Override
  public void render() {
    // Draw squares on all affected tiles via the DebugDrawSystem
    Point cursorPos = getCursorPosition();
    cursorPos = new Point((float) Math.floor(cursorPos.x()), (float) Math.floor(cursorPos.y()));
    for (int dx = -brushSize + 1; dx < brushSize; dx++) {
      for (int dy = -brushSize + 1; dy < brushSize; dy++) {
        // Ignore corners
        if (Math.abs(dx) + Math.abs(dy) >= brushSize) {
          continue;
        }
        Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
        DebugDrawSystem.drawRectangleOutline(
            targetPos.x(), targetPos.y(), 1.0f, 1.0f, new Color(1, 1, 1, 0.2f));
      }
    }
  }

  @Override
  public void execute() {
    if (InputManager.isButtonJustReleased(Input.Buttons.LEFT)
        || InputManager.isButtonJustReleased(Input.Buttons.RIGHT)) {
      flushMouseLevelChange();
    }

    if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    } else if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR += 1;
      } else {
        selectedTileIndexL += 1;
      }
    }
    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      brushSize = Math.min(MAX_BRUSH_SIZE, brushSize + 1);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      brushSize = Math.max(1, brushSize - 1);
    }

    if (InputManager.isKeyJustPressed(QUARTERNARY)
        || InputManager.isButtonJustPressed(Input.Buttons.MIDDLE)) {
      // Pick tile under cursor to LMB
      Point cursorPos = getCursorPosition();
      getLevel()
          .tileAt(cursorPos)
          .ifPresent(
              t -> {
                LevelElement element = t.levelElement();
                selectedTileIndexL = element.ordinal();
              });
    }

    /* Mouse interactions:
     * - LMB place tile on cursor
     * - RMB place SKIP tile on cursor
     */
    Optional<LevelElement> levelElement = Optional.empty();
    int targetBrushSize;
    if (InputManager.isButtonPressed(Input.Buttons.LEFT)) {
      levelElement =
          Optional.of(
              LevelElement.values()[
                  Math.floorMod(selectedTileIndexL, LevelElement.values().length)]);
      targetBrushSize = brushSize;
    } else {
      targetBrushSize = 1;
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
        levelElement =
            Optional.of(
                LevelElement.values()[
                    Math.floorMod(selectedTileIndexR, LevelElement.values().length)]);
      } else if (InputManager.isKeyPressed(TERTIARY)) {
        levelElement = Optional.of(LevelElement.SKIP);
      }
    }
    levelElement.ifPresent(
        element -> {
          // Set tiles in a distance brush area. 1 = 1 tile, 2 = 1 tile + surrounding
          // tiles in x and
          // y independently (not a square), etc.
          Point cursorPos = getCursorPosition();
          boolean changed = false;
          for (int dx = -targetBrushSize + 1; dx < targetBrushSize; dx++) {
            for (int dy = -targetBrushSize + 1; dy < targetBrushSize; dy++) {
              // Ignore corners
              if (Math.abs(dx) + Math.abs(dy) >= targetBrushSize) {
                continue;
              }
              Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
              changed |= setTile(targetPos, element);
            }
          }
          CheckPatternPainter.paintCheckerPattern(getLevel().layout());
          if (changed) {
            if (InputManager.isButtonPressed(Input.Buttons.LEFT)
                || InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
              mouseLevelChangePending = true;
            } else {
              levelChanged();
            }
          }
        });
  }

  private void flushMouseLevelChange() {
    if (!mouseLevelChangePending) return;
    mouseLevelChangePending = false;
    levelChanged();
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {
    flushMouseLevelChange();
  }

  @Override
  public String additionalInformation() {
    StringBuilder status = new StringBuilder();
    LevelElement[] elements = LevelElement.values();
    status
        .append("Selected [L]: ")
        .append(elements[Math.floorMod(selectedTileIndexL, elements.length)].name())
        .append("\nSelected [R]: ")
        .append(elements[Math.floorMod(selectedTileIndexR, elements.length)].name());
    Point cursorPos = getCursorPosition();
    if (getLevel() == null) return status.toString();
    getLevel()
        .tileAt(cursorPos)
        .ifPresent(
            tile -> {
              status.append("\n");
              Coordinate c = cursorPos.toCoordinate();
              status
                  .append("Tile under cursor (")
                  .append(c.x())
                  .append(",")
                  .append(c.y())
                  .append("):");
              status
                  .append("\n")
                  .append(tile.levelElement().name())
                  .append("  |  ")
                  .append(TileTextures.texture(tile, tile.level().layout()).pathString());
              if (tile.tintColor() == -1) {
                status.append("\nTint: (---)");
              } else {
                Color tintColor = new Color(tile.tintColor());
                status.append(
                    String.format(
                        "\nTint: (%.1f, %.1f, %.1f, %.1f)",
                        tintColor.r, tintColor.g, tintColor.b, tintColor.a));
              }
            });

    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Place Tile [L]");
    controls.put(Input.Buttons.RIGHT, "Place Tile [R]");
    controls.put(TERTIARY, "Place SKIP Tile");
    controls.put(QUARTERNARY, "Pick tile from cursor");
    controls.put(Input.Buttons.MIDDLE, "Pick tile from cursor");
    return controls;
  }

  private DesignLabel designLabel() {
    return getLevel().designLabel().orElse(DesignLabel.DEFAULT);
  }

  private void designLabel(DesignLabel designLabel) {
    DungeonLevel level = getLevel();
    if (level.designLabel().filter(designLabel::equals).isPresent()) return;
    level.designLabel(designLabel);
    levelChanged();
  }

  private void atmospherePreset(Atmosphere.Preset preset) {
    // Choosing a preset also brings back its own ambient light.
    DungeonLevel level = getLevel();
    if (level.atmospherePreset() == preset) return;
    level.atmosphere(preset, Optional.empty());
    levelChanged();
  }

  private void ambient(Color ambient) {
    DungeonLevel level = getLevel();
    Color opaque = new Color(ambient.r, ambient.g, ambient.b, 1f);
    if (opaque.equals(level.atmosphere().ambient())) return;
    level.atmosphere(level.atmospherePreset(), Optional.of(opaque));
    levelChanged();
  }
}
