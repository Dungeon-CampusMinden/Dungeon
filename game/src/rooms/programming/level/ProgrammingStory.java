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

  static String passageNote(String id) {
    String entry =
        switch (id) {
          case "forge-press" ->
              "Wartung: Nox muss genau auf Höhe des Wegzeichens halten. "
                  + "amWegzeichen() meldet nur dort true.";
          case "bellows" ->
              "Probelauf: Schon am Start war die Bedingung falsch. "
                  + "Eine Prüfung vor dem ersten Schritt ließ Nox stehen.";
          case "chain-lift" ->
              "Vermessung: Zwischen Ausgangsposition und Wegzeichen liegen fünf Schritte. "
                  + "Hinter dem Zeichen ist der Gang noch frei.";
          case "cooling-channel" ->
              "Steuerung: schritt() bewegt Nox um ein Feld in Blickrichtung. "
                  + "Das Wegzeichen ist der Haltepunkt, nicht die Wand dahinter.";
          case "heart-gate" ->
              "Rücklaufprüfung: Ein fehlgeschlagener Lauf setzt Nox zur Ausgangsposition zurück. "
                  + "Die eingesetzte Rune bleibt verwendbar.";
          default -> throw new IllegalArgumentException("Unknown passage note: " + id);
        };
    return ProgrammingGolemRuntime.passageName(id) + "\n\n" + entry + "\n\nValerius";
  }

  static String inspect(String id) {
    return switch (id) {
      case "inspect-forge-ledger" ->
          "Nox, Steuerung\n\nDie Seelenbindung versorgt den Körper. Für gesteuerte Schritte "
              + "wird zusätzlich eine Rhythmusrune benötigt. Die Programme werden am Seelenkern eingesetzt.\n\n"
              + "Ich habe die Runensätze bei den jeweiligen Wartungsplätzen hinterlegt.\n\nValerius";
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
