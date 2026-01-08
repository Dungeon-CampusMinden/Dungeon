package portal.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Die Klasse Timer verwaltet mehrere einfache Zeitmesser (Timer).
 *
 * <p>Jeder Timer wird über einen Namen angesprochen und läuft für eine festgelegte Zeit in
 * Millisekunden.
 *
 * <p>Typischer Anwendungsfall: - Einen Timer starten - Regelmäßig prüfen, ob die Zeit abgelaufen
 * ist
 */
public class Timer {

  /**
   * Diese Map speichert für jeden Timer-Namen den Zeitpunkt, zu dem der Timer enden soll.
   *
   * <p>Key: Name des Timers Value: Endzeitpunkt in Millisekunden (Systemzeit)
   */
  private static Map<String, Long> timers = new HashMap<>();

  /**
   * Startet (oder überschreibt) einen Timer.
   *
   * @param name Der eindeutige Name des Timers
   * @param timeInMs Die Laufzeit des Timers in Millisekunden
   */
  public static void startTimer(String name, int timeInMs) {
    // Aktuelle Systemzeit in Millisekunden holen
    long currentTime = System.currentTimeMillis();

    // Endzeit berechnen
    long endTime = currentTime + timeInMs;

    // Timer speichern oder überschreiben
    timers.put(name, endTime);
  }

  /**
   * Prüft, ob ein bestimmter Timer abgelaufen ist.
   *
   * @param name Der Name des Timers
   * @return true, wenn der Timer abgelaufen ist oder nicht existiert, false, wenn der Timer noch
   *     läuft
   */
  public static boolean isTimerEnd(String name) {
    // Prüfen, ob es den Timer überhaupt gibt
    if (!timers.containsKey(name)) {
      return true; // Nicht vorhandener Timer gilt als beendet
    }

    // Aktuelle Zeit holen
    long currentTime = System.currentTimeMillis();

    // Gespeicherte Endzeit holen
    long endTime = timers.get(name);

    // Prüfen, ob die aktuelle Zeit größer oder gleich der Endzeit ist
    if (currentTime >= endTime) {
      timers.remove(name);
      return true;
    }
    return false;
  }
}
