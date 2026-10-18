package feature.timer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import engine.Game;
import engine.rendering.Atmosphere;

/**
 * The alarm of a world timer's last minute: the ambient light of a room pulses red once a second
 * while a running countdown is about to run out.
 */
public final class WorldTimerAlarm {

  private static final Color ALARM = new Color(1f, 0.1f, 0.06f, 1f);
  private static final long LAST_MINUTE_MS = 60_000L;
  private static final float PULSE_MS = 1000f;
  // Share of the alarm colour in the ambient light at the peak of a pulse.
  private static final float MAX_TINT = 0.25f;

  private WorldTimerAlarm() {}

  /**
   * Lays the alarm over an atmosphere while a running world timer is in its last minute.
   *
   * @param atmosphere the room's atmosphere without alarm
   * @return the atmosphere with the current alarm pulse, or the given one without alarm
   */
  public static Atmosphere apply(Atmosphere atmosphere) {
    float pulse = pulse();
    if (pulse == 0) return atmosphere;
    return atmosphere.withAmbient(atmosphere.ambient().cpy().lerp(ALARM, MAX_TINT * pulse));
  }

  private static float pulse() {
    long remaining =
        Game.entities()
            .flatMap(entity -> entity.fetch(WorldTimerComponent.class).stream())
            .filter(timer -> timer.stoppedAtActiveMs() == Long.MAX_VALUE)
            .mapToLong(WorldTimerComponent::remainingMs)
            .filter(ms -> ms > 0 && ms <= LAST_MINUTE_MS)
            .min()
            .orElse(0);
    if (remaining == 0) return 0;
    float phase = (remaining % (long) PULSE_MS) / PULSE_MS;
    return 0.5f - 0.5f * MathUtils.cos(MathUtils.PI2 * phase);
  }
}
