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
        + "Stellt seine Seelenbindung wieder her. Er soll die Torwinde freiräumen, "
        + "damit sich der Ausgang wieder öffnen lässt.\n\nValerius";
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

  static String maintenance() {
    return "Wartung, Torwinde\n\nDie untere Halterung ist stark verrostet. "
        + "Unter Belastung gibt sie nach.\n\nValerius";
  }

  static String workshop() {
    return "Die Torwinde ist zerstört. Der Nebenausgang der Werkstatt bleibt uns. "
        + "Aktiviere die Runensteine, sammle ihre Kristalle und versorge beide Altäre."
        + "\n\nValerius' langes Programm erledigt das bereits. Doch die Steuerung des Ausgangs "
        + "fasst nur acht Befehle im Hauptprogramm. Lagere wiederholte Abläufe in eigene Methoden aus. "
        + "Ihre Aufrufe sparen Platz. Parameter und Rückgaben machen dieselbe Methode mehrfach nutzbar."
        + "\n\nDie Werkstatt ist ein Prüfstand. Jeder Start setzt Nox und alle Objekte zurück. "
        + "Öffne das Steuerbuch auf der Werkbank und probiere deine Änderungen aus.";
  }

  static String workshopJournal() {
    return "Valerius' Werkstattjournal\n\n"
        + "Das vollständige Programm liegt im Steuerbuch. Zwei Tore, zweimal derselbe Handgriff. "
        + "Die Runen verlangen unterschiedliche Richtungen. Die Felder liefern drei und fünf Kristalle. "
        + "Die Altäre brauchen genau diese Mengen."
        + "\n\nDie Ausgangssteuerung hat Platz für acht Befehle. Eigene Methoden haben ihren eigenen "
        + "Speicher. Ich muss die Wiederholungen zusammenfassen und ihre Unterschiede als Parameter "
        + "übergeben. Eine Sammlung liefert ihre tatsächliche Anzahl als Rückgabe."
        + "\n\nDer Prüfstand setzt vor jedem Lauf alles zurück. Fehler bleiben sichtbar, "
        + "bis der nächste Versuch beginnt. Die Hilfe im Buch erklärt Parameter und Rückgaben."
        + "\n\nValerius";
  }

  static String workshopArrival() {
    return "Das Steuerbuch enthält meinen vollständigen Arbeitsablauf. Er funktioniert, "
        + "aber die Ausgangssteuerung fasst nur acht Befehle. Fasse Wiederholungen in eigenen "
        + "Methoden zusammen. Bei jedem Start setzt der Prüfstand alles zurück.";
  }
}
