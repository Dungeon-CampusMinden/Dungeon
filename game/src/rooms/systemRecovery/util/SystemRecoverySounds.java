package rooms.systemRecovery.util;

import engine.Game;
import engine.sound.SoundSpec;

/** Shared sound cues for authoritative System Recovery gameplay events. */
public final class SystemRecoverySounds {
  private static final String DOOR_OPEN_SOUND = "enterDoor";
  private SystemRecoverySounds() {}

  /** Plays a globally synchronized sound from an authoritative gameplay event. */
  public static void play(String asset, float volume) {
    Game.audio().playGlobal(SoundSpec.builder(asset).volume(volume));
  }

  /** Plays the game's plain door-opening sound for System Recovery doors. */
  public static void doorOpened() {
    play(DOOR_OPEN_SOUND, 0.72f);
  }

  /** Plays the accepted power-relay lever activation cue. */
  public static void relayActivated() {
    play("system_recovery_relay_activate", 0.58f);
  }

  /** Plays the battery locking into its socket. */
  public static void batteryInserted() {
    play("system_recovery_battery_insert", 0.66f);
  }

  /** Plays a compact diagnostic sweep pulse. */
  public static void scannerPulse() {
    play("system_recovery_scanner_scan", 0.40f);
  }

  /** Plays the conveyor motor spinning up. */
  public static void conveyorStarted() {
    play("system_recovery_conveyor_start", 0.58f);
  }

  /** Plays the conveyor brake and stop cue. */
  public static void conveyorStopped() {
    play("system_recovery_conveyor_stop", 0.52f);
  }

  /** Plays one autonomous search-robot scan pulse. */
  public static void robotScan() {
    play("system_recovery_robot_scan", 0.43f);
  }

  /** Plays the chip recovery confirmation. */
  public static void chipRetrieved() {
    play("system_recovery_chip_retrieve", 0.66f);
  }

  /** Plays the alarm engagement sting. */
  public static void alarmTriggered() {
    play("system_recovery_alarm_trigger", 0.72f);
  }

  /** Plays the final level completion cue. */
  public static void completed() {
    play("system_recovery_complete", 0.72f);
  }

  /**
   * Starts or stops the locally mixed alarm layer and siren from the synchronized alarm state.
   *
   * <p>The looping audio is intentionally local on each client. One-shot gameplay sounds use the
   * server's global sound broadcast, but state-based alarm audio must also begin for clients who
   * join after the alarm has started or load a checkpoint with the alarm active.
   *
   * @param active whether the authoritative system-core alarm is active
   */
  public static synchronized void syncAlarmLayer(boolean active) {
    SystemRecoveryMusic.syncAlarmLayer(active);
  }

  /** Stops alarm audio after disconnect so a reconnected client can resync from metadata. */
  public static synchronized void resetClientAudio() {
    SystemRecoveryMusic.resetClientAudio();
  }
}
