package rooms.systemRecovery.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.components.PositionComponent;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.OutlineShader;
import feature.entities.deco.Deco;
import feature.interaction.keypad.KeypadComponent;
import feature.systems.LevelEditorSystem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.lasthour.util.shaders.LightingShader;
import rooms.systemRecovery.modules.display.DisplayTextStatusShader;
import rooms.systemRecovery.modules.scanner.ModuleScannerVisualState;

/** Client-side rendering setup for System Recovery. */
public class SystemRecoveryClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String LIGHTING_SHADER_ID = "systemRecoveryLighting";
  private static final String MODULE_SCAN_SHADER_ID = "moduleScannerLocal";
  private static final String[] MODULE_NAMES = {"cpu", "ram", "gpu", "ssd", "network"};

  private int highlightedModuleIndex = -1;

  /**
   * Creates the System Recovery client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   * @param decorations static decorations loaded from the level state
   */
  public SystemRecoveryClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the System Recovery client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   */
  public SystemRecoveryClientLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    setupLightingShader();
  }

  @Override
  protected void onTick() {
    updateLightingShader();
    updateModuleScanHighlight();
  }

  /**
   * Follows the synchronized scanner entity locally so a module highlight changes in the same
   * render frame as the scanner movement. Only the scan lifecycle is network state; the current
   * module is derived from the authoritative position already applied by the default translator.
   */
  private void updateModuleScanHighlight() {
    if (Game.isHeadless()) return;

    Optional<Entity> scanner =
        Game.levelEntities().filter(entity -> "module_scanner".equals(entity.name())).findFirst();
    boolean scanning =
        scanner
            .flatMap(entity -> entity.fetch(ModuleScannerVisualState.class))
            .map(ModuleScannerVisualState::scanning)
            .orElse(false);

    if (!scanning) {
      if (highlightedModuleIndex >= 0) {
        clearModuleScanHighlight();
        highlightedModuleIndex = -1;
      }
      return;
    }

    Optional<Point> scannerPosition = scanner.flatMap(this::positionOfOptional);
    if (scannerPosition.isEmpty()) return;

    int moduleIndex = nearestScannerPoint(scannerPosition.orElseThrow());
    if (moduleIndex < 0 || moduleIndex == highlightedModuleIndex) return;

    clearModuleScanHighlight();
    Game.levelEntities()
        .filter(entity -> ("module_" + MODULE_NAMES[moduleIndex]).equals(entity.name()))
        .findFirst()
        .flatMap(entity -> entity.fetch(DrawComponent.class))
        .ifPresent(
            draw -> draw.shaders().add(MODULE_SCAN_SHADER_ID, new OutlineShader(2, Color.CYAN)));
    highlightedModuleIndex = moduleIndex;
  }

  private int nearestScannerPoint(Point scannerPosition) {
    int nearestIndex = -1;
    float nearestDistance = Float.MAX_VALUE;
    for (int index = 0; index < MODULE_NAMES.length; index++) {
      Point target = getPoint("scanner" + index).translate(-1, 1);
      float distance = Point.calculateDistance(scannerPosition, target);
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearestIndex = index;
      }
    }
    return nearestDistance <= 0.75f ? nearestIndex : -1;
  }

  private void clearModuleScanHighlight() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity.fetch(DrawComponent.class)
                    .ifPresent(draw -> draw.shaders().remove(MODULE_SCAN_SHADER_ID)));
  }

  /** Installs the same global lighting shader used by Last Hour. */
  private void setupLightingShader() {
    if (Game.isHeadless()) return;
    DrawSystem.getInstance()
        .sceneShaders()
        .add(LIGHTING_SHADER_ID, new LightingShader().ambientLight(0));
  }

  /** Updates local light sources without sending shader data through the network. */
  private void updateLightingShader() {
    if (Game.isHeadless()) return;
    if (!(DrawSystem.getInstance().sceneShaders().get(LIGHTING_SHADER_ID)
        instanceof LightingShader lighting)) return;

    lighting.clearLightSources();
    if (LevelEditorSystem.active()) {
      lighting.ambientLight(1.0f);
      return;
    }

    // Match Last Hour while its room lights are switched on: objects remain readable while
    // terminals, keypads, labels and players still provide local light sources.
    lighting.ambientLight(0.5f);
    Game.allPlayers().forEach(player -> lighting.addLightSource(positionOf(player), 1.0f));
    addSystemRecoveryLights(lighting);
  }

  /** Adds local light sources to the important interactive objects in the room. */
  private void addSystemRecoveryLights(LightingShader lighting) {
    Game.levelEntities()
        .filter(this::isTerminal)
        .forEach(entity -> addLight(lighting, entity, 0.45f, Color.CYAN));

    Game.levelEntities()
        .filter(entity -> entity.isPresent(KeypadComponent.class))
        .forEach(
            entity -> {
              KeypadComponent keypad = entity.fetch(KeypadComponent.class).orElseThrow();
              Color color = keypad.isUnlocked() ? Color.GREEN : Color.RED;
              addLight(lighting, entity, 0.3f, color);
            });

    Game.levelEntities()
        .filter(this::isDoorLabel)
        .forEach(
            entity -> {
              DisplayTextStatusShader labelShader = doorLabelShader(entity);
              Color color = labelShader.completed() ? Color.GREEN : Color.RED;
              addLight(lighting, entity, 0.25f, color);
            });
  }

  private boolean isTerminal(Entity entity) {
    String name = entity.name();
    return name != null && (name.equals("terminal") || name.endsWith("_terminal"));
  }

  private boolean isDoorLabel(Entity entity) {
    return doorLabelShader(entity) != null;
  }

  private DisplayTextStatusShader doorLabelShader(Entity entity) {
    return entity
        .fetch(DrawComponent.class)
        .map(draw -> draw.shaders().get("doorLabelStatus"))
        .filter(DisplayTextStatusShader.class::isInstance)
        .map(DisplayTextStatusShader.class::cast)
        .orElse(null);
  }

  private void addLight(LightingShader lighting, Entity entity, float intensity, Color color) {
    lighting.addLightSource(positionOf(entity).translate(0.5f, 0.5f), intensity, color);
  }

  private Point positionOf(Entity entity) {
    return Game.positionOf(entity).orElse(new Point(0, 0));
  }

  private Optional<Point> positionOfOptional(Entity entity) {
    return entity.fetch(PositionComponent.class).map(PositionComponent::position);
  }
}
