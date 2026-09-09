package rooms.programming.level;

import engine.utils.Tuple;
import java.util.List;

/** Workshop records explain Nox's work and the blocked exit without spoiling the accident. */
final class ProgrammingStory {
  private ProgrammingStory() {}

  static List<Tuple<String, Integer>> intro() {
    return List.of(Tuple.of("Aethelgard\nDie Schmiede des vermissten Meisters Valerius", 30));
  }

  static String letter() {
    return "Falls jemand nach mir sucht: Ich bin zum Herzfeuer gegangen.\n\n"
        + "Der zweite Ausgang klemmt. Schutt blockiert die Torwinde im Keller, "
        + "und aus den alten Leitungen tritt heißer Dampf aus. Geht nicht selbst hinunter.\n\n"
        + "Ich habe Nox für solche Arbeiten gebaut. Er trägt Lasten und hält die Maschinen frei. "
        + "Seine Seelenbindung ist allerdings erloschen. Runen und Gefäße liegen in den Werkstattkisten.\n\n"
        + "Hinter der dünnen Trennwand liegt sein Steuerplatz. Nox kann sie aufbrechen. "
        + "Schickt ihn von dort zur Torwinde.\n\nValerius";
  }

  static String archive() {
    return "Arbeitsauftrag · Maschinenkeller\n\n"
        + "Nox: Förderbahn, Pumpenzugang und Kettenzug freiräumen. "
        + "Den beschädigten Kühlkanal überqueren, dann den Schutt an der Torwinde entfernen.\n\n"
        + "Die Messingmarken sind seine Arbeitspositionen. Die Pfeile geben die Ausrichtung an. "
        + "Das Steuerprogramm muss dort vollständig enden. Erst danach beginnt der "
        + "separate Räumauftrag. Das Berühren einer Marke genügt nicht.\n\n"
        + "Steuerprogramme liegen im Archiv, darunter alte Probeläufe. "
        + "Das Steuerbuch nimmt jeweils eine Rhythmusrune an. "
        + "Passt der Lauf nicht zum Auftrag, kehrt Nox zur letzten Arbeitsposition zurück. "
        + "Die Rune bleibt erhalten. Während er läuft oder zurückkehrt, nimmt er kein neues Programm an.\n\n"
        + "Der Sehstein überträgt sein Blickfeld aus dem Keller. "
        + "Die Karte meldet nur, wo ein Hindernis liegt.\n\nValerius";
  }

  static String inspect(String id) {
    return switch (id) {
      case "inspect-forge-ledger" ->
          "Nox · Bindungsplan\n\nJede Eigenschaft erhält ein eigenes Gefäß. "
              + "Für ganze Zahlen verwende ich Eisenkisten, für Bruchteile Kristallflaschen. "
              + "Die Sollwerte sind in die Eigenschaftsrunen eingeritzt.\n\n"
              + "Ein Gefäß behält seinen Inhalt, bis ein neuer Wert ihn ersetzt. "
              + "Bei falscher Füllung nicht die ganze Bindung lösen.\n\n"
              + "Für gesteuerte Schritte braucht Nox anschließend eine Rhythmusrune. "
              + "Die Sammlung liegt im Archiv hinter der Trennwand.\n\nValerius";
      case "inspect-discarded-vessel" ->
          "Am Boden haftet eingetrocknete Essenz. Sie lässt sich nicht mehr lösen.";
      case "inspect-herb-notes" ->
          "Wartung, Torwinde\n\nDie untere Halterung ist stark verrostet. "
              + "Ersatz liegt im Materiallager. Bis zum Austausch keine Last auflegen.\n\nValerius";
      case "inspect-broken-compass" -> "Gravur auf der Unterseite: 'Nox · Blickrichtung O'.";
      case "inspect-sealed-cache" -> "Leere Ersatzfassungen. Ihre Kontakte sind stark korrodiert.";
      case "inspect-old-tools" -> "Der Kopf sitzt locker. Im Stiel verläuft ein tiefer Riss.";
      default -> throw new IllegalArgumentException("Unknown forge inscription: " + id);
    };
  }
}
