package rooms.systemRecovery.util.shaders;

import engine.Game;
import engine.sound.SoundSpec;
import engine.systems.DrawSystem;

/** Client-side controller for the System Recovery alarm state. */
public final class SystemRecoveryAlarm {
  private static final String SHADER_ID = "systemRecoveryAlarm";

  private SystemRecoveryAlarm() {}

  /** Enables the red alarm filter and plays the warning tone once. */
  public static void activate() {
    if (Game.isHeadless()) return;
    if (DrawSystem.getInstance().sceneShaders().get(SHADER_ID) != null) return;
    DrawSystem.getInstance().sceneShaders().add(SHADER_ID, new SystemRecoveryAlarmShader());
    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01"));
  }

  /** Removes the alarm filter when the level is reset. */
  public static void deactivate() {
    if (!Game.isHeadless()) {
      DrawSystem.getInstance().sceneShaders().remove(SHADER_ID);
    }
  }
}
