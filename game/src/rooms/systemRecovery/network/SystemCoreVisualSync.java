package rooms.systemRecovery.network;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.OutlineShader;
import java.util.Map;
import rooms.systemRecovery.util.shaders.SystemRecoveryAlarm;

/** Reconstructs the system-core alarm and per-area completion visuals on clients. */
public final class SystemCoreVisualSync {

  private SystemCoreVisualSync() {}

  /**
   * Applies the authoritative system-core alarm state.
   *
   * @param metadata synchronized system-core metadata
   */
  public static void applyAlarm(Map<String, String> metadata) {
    String alarm = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ALARM);
    boolean active =
        alarm != null
            ? Boolean.parseBoolean(alarm)
            : Boolean.parseBoolean(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ACCESS, "false"));

    // The red filter is a managed scene shader and arrives through ShaderSyncSystem. Metadata is
    // deliberately used only for the one-shot audio feedback and late-join compatibility.
    SystemRecoveryAlarm.syncClientSound(active);
  }

  /**
   * Projects the synchronized system-core stage into the local completion shader.
   *
   * @param entity system-core visual entity
   * @param metadata synchronized System Recovery metadata
   */
  public static void applyCompletionMetadata(Entity entity, Map<String, String> metadata) {
    String stageValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_STAGE);
    if (stageValue == null || entity.name() == null) return;

    int stage;
    try {
      stage = Integer.parseInt(stageValue);
    } catch (NumberFormatException ignored) {
      return;
    }

    int requiredStage = requiredStage(entity.name());
    if (requiredStage < 0) return;
    boolean completed = stage >= requiredStage;
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw -> {
              if (completed) {
                draw.tintColor(0x66FF66FF);
                if (draw.shaders().get("systemCoreComplete") == null) {
                  draw.shaders()
                      .add("systemCoreComplete", new OutlineShader(2, Color.GREEN, 1.8f, 0.25f));
                }
              } else {
                draw.shaders().remove("systemCoreComplete");
              }
            });
  }

  private static int requiredStage(String entityName) {
    if (entityName.startsWith("system_core_sort_")) return 1;
    if (entityName.startsWith("system_core_module_")) return 2;
    if (entityName.startsWith("system_core_map_")) return 3;
    return -1;
  }
}
