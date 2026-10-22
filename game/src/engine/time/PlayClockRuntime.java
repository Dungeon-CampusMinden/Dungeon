package engine.time;

import engine.Game;
import engine.network.messages.c2s.PauseStateMessage;
import engine.network.messages.s2c.PlayClockMessage;
import feature.systems.EventScheduler;
import feature.systems.HudSystem;
import java.util.function.BooleanSupplier;

/** Game-thread bridge for local pause intent, authoritative samples, and play-time actions. */
public final class PlayClockRuntime {
  private static Boolean reportedPause;
  private static Boolean lastBroadcastRunning;
  private static BooleanSupplier roomPause = () -> false;

  private PlayClockRuntime() {}

  /**
   * Pauses play like the pause menu while the condition holds, for a room's own pause screen.
   *
   * @param condition whether the room's pause screen is open on this client
   */
  public static void pauseWhile(BooleanSupplier condition) {
    roomPause = condition;
  }

  /** Reports local pause intent even while the simulation is stopped by a dialog. */
  public static void updateLocalPause() {
    Game.player()
        .ifPresent(
            player -> {
              boolean paused =
                  HudSystem.getInstance().hasOpenPlayPauseUI(player) || roomPause.getAsBoolean();
              if (Game.isSingleplayer()) {
                Game.playClock().paused((short) 0, paused);
              } else if (Game.network().isConnected()
                  && (reportedPause == null || reportedPause != paused)) {
                reportedPause = paused;
                Game.network().send((short) 0, new PauseStateMessage(paused), true);
              }
            });
  }

  /** Forgets the reported pause state so the next connection reports it again. */
  public static void resetClient() {
    reportedPause = null;
  }

  /**
   * Advances authoritative timers and broadcasts the clock when it starts or stops running, the
   * only moments clients cannot extrapolate. Joining clients receive the state with the world sync.
   */
  public static void tick() {
    if (Game.isMultiplayerClient()) return;
    EventScheduler.executePlayActions();
    if (Game.isSingleplayer()) return;
    boolean running = Game.playClock().running();
    if (lastBroadcastRunning != null && lastBroadcastRunning == running) return;
    lastBroadcastRunning = running;
    Game.network().broadcast(new PlayClockMessage(Game.playClock().activeMs(), running), true);
  }
}
