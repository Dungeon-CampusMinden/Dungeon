package rooms.systemRecovery.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.rendering.Atmosphere;
import engine.rendering.CeilingLights;
import engine.rendering.Light;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.components.draw.shader.OutlineShader;
import feature.entities.deco.Deco;
import feature.interaction.keypad.KeypadComponent;
import feature.timer.WorldTimerAlarm;
import feature.utils.EntityUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.modules.computer.content.SystemCoreMetaDraft;
import rooms.systemRecovery.modules.computer.content.TerminalDraftStore;
import rooms.systemRecovery.modules.display.DisplayTextStatusShader;
import rooms.systemRecovery.modules.scanner.ModuleScannerVisualState;

/** Client-side rendering setup for System Recovery. */
public class SystemRecoveryClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";
  private static final Atmosphere DARK_COLD = Atmosphere.COLD.withBrightness(0.6f);
  private static final Color CEILING_LIGHT = new Color(0.8f, 0.92f, 1f, 1f);
  private static final float CEILING_LIGHT_INTENSITY = 0.7f;
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
    SystemCoreMetaDraft.clear();
    TerminalDraftStore.clear();
  }

  @Override
  protected void onTick() {
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
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(draw -> draw.shaders().remove(MODULE_SCAN_SHADER_ID)));
  }

  @Override
  public Atmosphere atmosphere() {
    return lighting();
  }

  @Override
  public void lights(List<Light> lighting) {
    addLights(layout(), lighting);
  }

  /**
   * AXIOM's facility: dim and cool, lit by cool ceiling lamps and its machines; screens, server
   * shelves, terminals, scanners and status lamps glow in their colours. In the last minute of the
   * countdown the light pulses red. Shared by the client and the server level, which renders in
   * single player.
   *
   * @return the current atmosphere of the room
   */
  static Atmosphere lighting() {
    return WorldTimerAlarm.apply(DARK_COLD);
  }

  /**
   * Adds the ceiling lamps and the lights of the important interactive objects in the room. Shared
   * by the client and the server level, which renders in single player.
   *
   * @param layout the level layout, indexed [y][x]
   * @param lighting receives the lights
   */
  static void addLights(Tile[][] layout, List<Light> lighting) {
    CeilingLights.add(layout, CEILING_LIGHT, CEILING_LIGHT_INTENSITY, (x, y) -> true, lighting);
    Game.levelEntities()
        .filter(SystemRecoveryClientLevel::isTerminal)
        .forEach(entity -> addLight(lighting, entity, 0.45f, Color.CYAN));

    Game.levelEntities()
        .filter(SystemRecoveryClientLevel::isScanner)
        .forEach(entity -> addLight(lighting, entity, 0.55f, Color.CYAN));

    Game.levelEntities()
        .filter(entity -> entity.isPresent(KeypadComponent.class))
        .forEach(
            entity -> {
              KeypadComponent keypad = entity.fetch(KeypadComponent.class).orElseThrow();
              Color color = keypad.isUnlocked() ? Color.GREEN : Color.RED;
              addLight(lighting, entity, 0.3f, color);
            });

    Game.levelEntities()
        .filter(SystemRecoveryClientLevel::isDoorLabel)
        .forEach(
            entity -> {
              DisplayTextStatusShader labelShader = doorLabelShader(entity);
              addLight(lighting, entity, 0.5f, doorLabelLightColor(labelShader.completed()));
            });
  }

  private static boolean isTerminal(Entity entity) {
    String name = entity.name();
    return name != null && (name.equals("terminal") || name.endsWith("_terminal"));
  }

  static boolean isScanner(Entity entity) {
    String name = entity.name();
    return name != null && name.endsWith("_scanner");
  }

  private static boolean isDoorLabel(Entity entity) {
    return doorLabelShader(entity) != null;
  }

  private static DisplayTextStatusShader doorLabelShader(Entity entity) {
    return entity
        .fetch(DrawComponent.class)
        .map(draw -> draw.shaders().get("doorLabelStatus"))
        .filter(DisplayTextStatusShader.class::isInstance)
        .map(DisplayTextStatusShader.class::cast)
        .orElse(null);
  }

  private static void addLight(List<Light> lighting, Entity entity, float intensity, Color color) {
    lighting.add(Light.steady(lightSourcePosition(entity), color, intensity));
  }

  static Point lightSourcePosition(Entity entity) {
    return EntityUtils.getPosition(entity);
  }

  static Color doorLabelLightColor(boolean completed) {
    return completed ? Color.GREEN : Color.RED;
  }

  private Optional<Point> positionOfOptional(Entity entity) {
    return entity.fetch(PositionComponent.class).map(PositionComponent::position);
  }
}
