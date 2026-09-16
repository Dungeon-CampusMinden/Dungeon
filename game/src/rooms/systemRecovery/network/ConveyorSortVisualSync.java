package rooms.systemRecovery.network;

import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.OutlineShader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Reconstructs conveyor package colors and the active scanner comparison on each client. */
final class ConveyorSortVisualSync {

  private int lastLeftPackage = -1;
  private int lastRightPackage = -1;
  private int lastScanner = -1;
  private String lastPackageMetadata;
  private final Set<Integer> lastColoredPackageIds = new HashSet<>();

  /**
   * Applies the server-selected package pair, scanner and package colors.
   *
   * @param metadata synchronized conveyor metadata
   */
  void apply(Map<String, String> metadata) {
    String leftPackage = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_LEFT_PACKAGE);
    String rightPackage =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_RIGHT_PACKAGE);
    String scanner = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_SCANNER);
    String packageMetadata = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_PACKAGES);
    if (leftPackage == null || rightPackage == null || scanner == null || packageMetadata == null) {
      return;
    }

    int leftPackageId = parseEntityId(leftPackage);
    int rightPackageId = parseEntityId(rightPackage);
    int scannerId = parseEntityId(scanner);
    if (leftPackageId == lastLeftPackage
        && rightPackageId == lastRightPackage
        && scannerId == lastScanner
        && packageMetadata.equals(lastPackageMetadata)) {
      return;
    }

    lastColoredPackageIds.forEach(ConveyorSortVisualSync::resetTint);
    lastColoredPackageIds.clear();
    clear();
    Map<Integer, Integer> packageValues = parsePackageMetadata(packageMetadata);
    packageValues.forEach(this::addPackageColor);
    lastColoredPackageIds.addAll(packageValues.keySet());

    if (leftPackageId >= 0 && rightPackageId >= 0) {
      addHighlight(leftPackageId, "beltSortLeft", Color.YELLOW);
      addHighlight(rightPackageId, "beltSortRight", Color.CYAN);
      addHighlight(scannerId, "beltSortScanner", Color.WHITE);
    }
    lastLeftPackage = leftPackageId;
    lastRightPackage = rightPackageId;
    lastScanner = scannerId;
    lastPackageMetadata = packageMetadata;
  }

  /** Removes conveyor-specific shaders and forgets the previous comparison. */
  void reset() {
    lastColoredPackageIds.forEach(ConveyorSortVisualSync::resetTint);
    lastColoredPackageIds.clear();
    clear();
    lastLeftPackage = -1;
    lastRightPackage = -1;
    lastScanner = -1;
    lastPackageMetadata = null;
  }

  private void clear() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.shaders().remove("beltSortLeft");
                          draw.shaders().remove("beltSortRight");
                          draw.shaders().remove("beltSortScanner");
                          draw.shaders().remove("beltPackageColor");
                        }));
  }

  private void addPackageColor(int entityId, int value) {
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.tintColor(0xFFFFFFFF);
                          draw.shaders()
                              .add(
                                  "beltPackageColor",
                                  new HueRemapShader(0.08f, packageHue(value), 0.12f));
                        }));
  }

  private static void addHighlight(int entityId, String shaderName, Color color) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> draw.shaders().add(shaderName, new OutlineShader(1, color))));
  }

  private static float packageHue(int value) {
    return switch (value) {
      case 15 -> 0.00f;
      case 20 -> 0.14f;
      case 30 -> 0.33f;
      case 40 -> 0.60f;
      case 60 -> 0.85f;
      default -> 0.08f;
    };
  }

  private static Map<Integer, Integer> parsePackageMetadata(String metadata) {
    Map<Integer, Integer> values = new HashMap<>();
    for (String entry : metadata.split(",")) {
      String[] parts = entry.split(":", 2);
      if (parts.length != 2) continue;
      try {
        values.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      } catch (NumberFormatException ignored) {
        // Ignore malformed debug metadata and keep the remaining packages visible.
      }
    }
    return values;
  }

  private static void resetTint(int entityId) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(DrawComponent.class))
        .ifPresent(draw -> draw.tintColor(0xFFFFFFFF));
  }

  private static int parseEntityId(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return -1;
    }
  }
}
