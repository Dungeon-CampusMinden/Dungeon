package rooms.programming.level;

import engine.utils.Tuple;
import java.util.List;

/** Notes in Valerius' workshop explain the guardian and the route out. */
final class ProgrammingStory {
  private ProgrammingStory() {}

  static List<Tuple<String, Integer>> intro() {
    return List.of(Tuple.of("Aethelgard\nDie Schmiede des vermissten Meisters Valerius", 30));
  }

  static String letter() {
    return "Falls jemand nach mir sucht: Ich bin zum Herzfeuer gegangen. "
        + "Die Entropie hat die alten Maschinen erreicht.\n\n"
        + "Hinter der zugemauerten Öffnung liegt der Versorgungsgang. Er führt zum Herzfeuer "
        + "und zum zweiten Ausgang der Schmiede. Nox ist stark genug, das Mauerwerk aufzubrechen. "
        + "Ich habe ihn als Wächter des Herzfeuers gebaut, aber seine Seelenbindung ist erloschen.\n\n"
        + "Die Runen und Gefäße für seine Bindung habe ich in den Werkstattkisten verwahrt.\n\nValerius";
  }

  static String golem() {
    return "Seelenbindung unvollständig.";
  }

  static String archive() {
    return "Fernsteuerung des Wächters\n\n"
        + "Nox fährt durch die Schleuse in die tiefen Versorgungsgänge. Menschen bleiben hier. "
        + "Das aufgeschlagene Buch am Tor steuert ihn. Der blaue Sehstein daneben zeigt seinen Weg.\n\n"
        + "24 Runen liegen im Archiv und in der Werkstatt. Sammle sie ein und prüfe ihren Code. "
        + "Einige sind Versuche, andere führen Nox weiter. Das Terminal nimmt eine Rune zugleich an.\n\n"
        + "Die Karte meldet Hindernisse. Ihr Aussehen erkennst du nur durch den Sehstein. "
        + "Nach einem Fehler kehrt Nox zurück; die Rune wird wieder frei. "
        + "Erst am Ende der Versorgungsgänge öffnet er unseren Weg zum Herzfeuer.\n\nValerius";
  }

  static String inspect(String id) {
    return switch (id) {
      case "inspect-forge-ledger" ->
          "Nox, Steuerung\n\nDie Seelenbindung versorgt den Körper. Für gesteuerte Schritte "
              + "wird zusätzlich eine Rhythmusrune benötigt. Die Programme werden am Terminal im Archiv eingesetzt.\n\n"
              + "Die Runensammlung liegt auf den Lesetischen hinter der zugemauerten Öffnung.\n\nValerius";
      case "inspect-discarded-vessel" ->
          "Am Boden haftet eingetrocknete Essenz. Sie lässt sich nicht mehr lösen.";
      case "inspect-herb-notes" ->
          "Prüfvermerk\n\nDie Fassungen sind unbeschädigt. "
              + "Bei ausbleibender Bewegung zuerst das Programm prüfen, nicht den Seelenkern ersetzen.\n\nValerius";
      case "inspect-broken-compass" -> "Gravur auf der Unterseite: 'Nox · Blickrichtung O'.";
      case "inspect-sealed-cache" -> "Leere Ersatzfassungen. Ihre Kontakte sind stark korrodiert.";
      case "inspect-old-tools" -> "Der Kopf sitzt locker. Im Stiel verläuft ein tiefer Riss.";
      default -> throw new IllegalArgumentException("Unknown forge inscription: " + id);
    };
  }
}
