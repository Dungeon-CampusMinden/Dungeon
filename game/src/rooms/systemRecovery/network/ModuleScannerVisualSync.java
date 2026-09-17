package rooms.systemRecovery.network;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.OutlineShader;
import java.util.Map;
import rooms.systemRecovery.modules.scanner.ModuleScannerVisualState;

/** Reconstructs the module scanner animation state and persistent GPU fault highlight. */
final class ModuleScannerVisualSync {

  private boolean lastScanFault;
  private int lastScannerEntityId = -1;

  /**
   * Applies scanner metadata to the scanner entity and the defective GPU.
   *
   * @param entity scanner entity receiving the animation state
   * @param metadata synchronized scanner metadata
   */
  void apply(Entity entity, Map<String, String> metadata) {
    if (!"module_scanner".equals(entity.name())) return;

    String scanRunningValue =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_RUNNING);
    String scanFaultValue =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_FAULT);
    if (scanRunningValue != null) {
      ModuleScannerVisualState state =
          entity
              .fetch(ModuleScannerVisualState.class)
              .orElseGet(
                  () -> {
                    ModuleScannerVisualState created = new ModuleScannerVisualState();
                    entity.add(created);
                    return created;
                  });
      state.scanning(Boolean.parseBoolean(scanRunningValue));
    }

    if (scanFaultValue == null) return;
    boolean scanFault = Boolean.parseBoolean(scanFaultValue);
    if (entity.id() == lastScannerEntityId && scanFault == lastScanFault) return;

    clearFaultHighlight();
    if (scanFault) addFaultHighlight();
    lastScanFault = scanFault;
    lastScannerEntityId = entity.id();
  }

  /** Removes the persistent GPU fault shader and forgets the previous scan state. */
  void reset() {
    clearFaultHighlight();
    lastScanFault = false;
    lastScannerEntityId = -1;
  }

  private void clearFaultHighlight() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(draw -> draw.shaders().remove("moduleScannerFault")));
  }

  private void addFaultHighlight() {
    Game.levelEntities()
        .filter(entity -> "module_gpu".equals(entity.name()))
        .findFirst()
        .flatMap(entity -> entity.fetch(DrawComponent.class))
        .ifPresent(
            draw -> draw.shaders().add("moduleScannerFault", new OutlineShader(2, Color.RED)));
  }
}
