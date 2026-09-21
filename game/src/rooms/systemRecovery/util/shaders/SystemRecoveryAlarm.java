package rooms.systemRecovery.util.shaders;

import engine.Game;
import engine.sound.SoundSpec;
import feature.shader.ShaderSystem;

/** Controls the global System Recovery alarm scene shader. */
public final class SystemRecoveryAlarm {
  private static final String SHADER_ID = "systemRecoveryAlarm";
  private static final int SHADER_ORDER = 0;
  private static boolean clientAlarmSoundActive;

  private SystemRecoveryAlarm() {}

  /**
   * Enables the red alarm filter for the complete rendered scene.
   *
   * <p>The assignment goes through {@link ShaderSystem}, so the server remains the source of
   * truth and the same scene shader is synchronized to every connected client.
   */
  public static void activate() {
    ShaderSystem shaderSystem = ShaderSystem.getInstance();
    boolean alreadyActive =
        shaderSystem.sceneShaders().shaders().stream()
            .anyMatch(entry -> entry.identifier().equals(SHADER_ID));
    if (alreadyActive) return;

    shaderSystem.addSceneShader(SHADER_ID, SHADER_ORDER, new SystemRecoveryAlarmShader());
    playClientAlarmSoundOnce();
  }

  /** Removes the global alarm scene shader when the level is reset or completed. */
  public static void deactivate() {
    ShaderSystem.getInstance().removeSceneShader(SHADER_ID);
    clientAlarmSoundActive = false;
  }

  /**
   * Plays the client-side alarm sound when synchronized metadata arrives.
   *
   * <p>The scene shader itself is already synchronized by {@link ShaderSystem}. Metadata can
   * arrive independently while a client joins, so it is only used here to keep the one-shot sound
   * feedback intact without adding the shader a second time locally.
   *
   * @param active whether the authoritative alarm is active
   */
  public static void syncClientSound(boolean active) {
    if (!active) {
      clientAlarmSoundActive = false;
      return;
    }
    playClientAlarmSoundOnce();
  }

  private static void playClientAlarmSoundOnce() {
    if (clientAlarmSoundActive || Game.isHeadless()) return;
    clientAlarmSoundActive = true;
    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01"));
  }
}
