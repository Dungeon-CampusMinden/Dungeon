package rooms.systemRecovery.util.shaders;

import feature.shader.ShaderSystem;
import rooms.systemRecovery.util.SystemRecoverySounds;

/** Controls the global System Recovery alarm scene shader. */
public final class SystemRecoveryAlarm {
  private static final String SHADER_ID = "systemRecoveryAlarm";
  private static final int SHADER_ORDER = 0;

  private SystemRecoveryAlarm() {}

  /**
   * Enables the red alarm filter for the complete rendered scene.
   *
   * <p>The assignment goes through {@link ShaderSystem}, so the server remains the source of truth
   * and the same scene shader is synchronized to every connected client.
   */
  public static void activate() {
    setActive(true);
  }

  /**
   * Restores the alarm scene state and looped audio without replaying its one-shot trigger sting.
   */
  public static void restoreActive() {
    setActive(false);
  }

  private static void setActive(boolean playTrigger) {
    ShaderSystem shaderSystem = ShaderSystem.getInstance();
    boolean alreadyActive =
        shaderSystem.sceneShaders().shaders().stream()
            .anyMatch(entry -> entry.identifier().equals(SHADER_ID));
    if (alreadyActive) return;

    shaderSystem.addSceneShader(SHADER_ID, SHADER_ORDER, new SystemRecoveryAlarmShader());
    if (playTrigger) SystemRecoverySounds.alarmTriggered();
    SystemRecoverySounds.syncAlarmLayer(true);
  }

  /** Removes the global alarm scene shader when the level is reset or completed. */
  public static void deactivate() {
    ShaderSystem.getInstance().removeSceneShader(SHADER_ID);
    SystemRecoverySounds.syncAlarmLayer(false);
  }

  /**
   * Applies the client-side alarm layer when synchronized metadata arrives.
   *
   * <p>The scene shader itself is already synchronized by {@link ShaderSystem}. Metadata can arrive
   * independently while a client joins, so this is also the recovery path for a late join or
   * reconnect. The loop is local to each client and never rebroadcast through the server.
   *
   * @param active whether the authoritative alarm is active
   */
  public static void syncClientAudioLayer(boolean active) {
    SystemRecoverySounds.syncAlarmLayer(active);
  }
}
