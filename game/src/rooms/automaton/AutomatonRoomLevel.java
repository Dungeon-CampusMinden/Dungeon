package rooms.automaton;

import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.ChoiceOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.escape.EscapeRoomLevel;

/**
 * Server level of the automaton room. Four terminals ask about regular languages and grammars, and
 * a code panel next to the door takes the four-digit code. Each solved terminal reveals one digit
 * of the code. The automaton rejects a wrong answer like an input without a transition. The
 * audience steers the hero with single steps.
 *
 * <p>The third terminal is broken at first. Kleene stands in its place and repairs it once the hero
 * brings the transition table from the chest.
 */
public final class AutomatonRoomLevel extends EscapeRoomLevel {
  private static final String CODE = "7031";
  private static final SimpleIPath TERMINAL =
      new SimpleIPath("objects/tech/Computer_station_1.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/keypad/off.png");

  /** The automaton tells the intro; it speaks through one of its terminals. */
  private static final String INTRO_SPEAKER =
      "[speaker img=" + TERMINAL.pathString() + " name=\"Der Automat\"]";

  private static final List<String> INTRO =
      List.of(
          "Eingabe erkannt. Ich bin ein endlicher Automat, und ihr seid gerade in meinem"
              + " Startzustand q0 gelandet.",
          "Hinaus kommt ihr nur durch die Tür zu meinem Endzustand. Sie akzeptiert genau ein"
              + " Wort, den vierstelligen Türcode.",
          "Meine vier Terminals verraten je eine Ziffer, wenn ihr ihre Frage richtig beantwortet."
              + " Eine falsche Antwort verwerfe ich, aber ihr bekommt einen Tipp und dürft es"
              + " gleich noch einmal versuchen.",
          "Ihr steuert die Figur mit eurem Gerät. Die Pfeile bewegen sie ein Feld weit, und"
              + " \"Benutzen\" bedient, was direkt vor ihr steht.");

  /** Kleene keeps the automaton running and broke state q3. */
  private static final Helper KLEENE =
      new Helper(
          "Kleene",
          "[speaker img=rooms/automaton/kleene_portrait.png name=\"Kleene\"]",
          "character/char03",
          false,
          2,
          "In der Truhe liegt eine Übergangstabelle. Ihr nehmt sie mit.",
          List.of(
              "Oh, Besuch! Ich bin Kleene, ich halte diesen Automaten in Schuss.",
              "Ich wollte gerade Zustand q3 polieren und habe dabei seine Übergangstabelle"
                  + " zerrissen. Jetzt ist er kaputt, und seine Frage ist weg.",
              "Ich kann ihn reparieren, mir fehlt nur eine neue Übergangstabelle. Eine Ersatzkopie"
                  + " liegt in der Truhe da drüben. Bringt sie mir!"),
          List.of(
              "Die Ersatzkopie der Übergangstabelle liegt in der Truhe da drüben. Bringt sie mir,"
                  + " dann repariere ich Zustand q3."),
          List.of(
              "Ist das die Übergangstabelle aus der Truhe? Genau die brauche ich!",
              "Einsetzen, festschrauben ... fertig! Zustand q3 läuft wieder, und er hat eine Frage"
                  + " für euch."),
          List.of("Danke für eure Hilfe! Ab jetzt poliere ich vorsichtiger."));

  /** One question per terminal, in the order of the code digits. */
  private static final List<Question> QUESTIONS =
      List.of(
          new Question(
              "Welche der folgenden Sprachen sind regulär?",
              List.of(
                  ChoiceOption.of("a^i b^j mit i, j <= 3", "a"),
                  ChoiceOption.of("a^i b^j mit i, j >= 2", "b"),
                  ChoiceOption.of("a^i b^j mit j = 2*i", "c")),
              true,
              Set.of("a", "b"),
              "Ein endlicher Automat hat nur endlich viele Zustände. Muss er sich eine beliebig"
                  + " große Zahl merken?"),
          new Question(
              "Wie muss eine reguläre Grammatik aussehen, damit die erzeugte Sprache das leere"
                  + " Wort enthält?",
              List.of(
                  ChoiceOption.of(
                      "A -> epsilon muss für irgendein Nichtterminal enthalten sein", "a"),
                  ChoiceOption.of("S -> epsilon muss enthalten sein", "b"),
                  ChoiceOption.of("Reguläre Sprachen können das leere Wort nicht enthalten.", "c"),
                  ChoiceOption.of("Es geht auch ohne epsilon-Regel.", "d")),
              false,
              Set.of("b"),
              "Jede Ableitung beginnt beim Startsymbol."),
          new Question(
              "Welche der folgenden Aussagen ist/sind richtig?",
              List.of(
                  ChoiceOption.of("Endliche Sprachen sind immer regulär.", "a"),
                  ChoiceOption.of(
                      "Für eine reguläre Sprache gibt es immer genau einen regulären Ausdruck.",
                      "b"),
                  ChoiceOption.of(
                      "Für manche reguläre Ausdrücke gibt es keine reguläre Grammatik.", "c"),
                  ChoiceOption.of(
                      "In einem DFA kann der Anfangszustand nicht gleichzeitig Endzustand sein.",
                      "d")),
              true,
              Set.of("a"),
              "Nur eine Aussage stimmt. Überlegt bei D, wie ein DFA das leere Wort akzeptieren"
                  + " kann."),
          new Question(
              "Wie viele Wörter enthält die Sprache, die von der Grammatik S -> a | epsilon"
                  + " erzeugt wird?",
              List.of(
                  ChoiceOption.of("kein Wort", "a"),
                  ChoiceOption.of("ein Wort", "b"),
                  ChoiceOption.of("zwei Wörter", "c"),
                  ChoiceOption.of("unendlich viele Wörter", "d")),
              false,
              Set.of("c"),
              "Was lässt sich aus S alles ableiten? Auch das leere Wort ist ein Wort."));

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public AutomatonRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(
        layout,
        designLabel,
        namedPoints,
        "Automat",
        new Terminals(
            TERMINAL,
            QUESTIONS,
            CODE,
            "Eingabe verworfen",
            "Für diese Antwort kennt der Automat keinen Übergang."));
  }

  /** Shows the intro once play begins. */
  static void intro() {
    intro(INTRO_SPEAKER, INTRO);
  }

  @Override
  protected void setUp() {
    terminals(KLEENE);
    doorLock(
        new DoorLock(
            PANEL,
            "Akzeptiert",
            "Wort akzeptiert! Die Tür zum Endzustand springt auf.",
            "Verworfen",
            "Dieses Wort akzeptiert der Automat nicht. Der Code stimmt nicht."));
  }

  // The digits come in the order of the terminals.
  @Override
  protected String revealText(int terminal) {
    return "Ziffer " + (terminal + 1) + " des Türcodes: " + CODE.charAt(terminal);
  }

  @Override
  protected boolean offersTargets() {
    return false;
  }
}
