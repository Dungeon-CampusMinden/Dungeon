package rooms.fomo;

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
 * Server level of the FOMO room. Three terminals ask about regular languages and grammars and one
 * about the steps of modelling, and a code panel next to the door takes the four-digit code. Each
 * solved terminal shows one step of the modelling cycle with one digit of the code. The digits
 * belong in the order of the cycle, which Stachowiak draws on the whiteboard once terminal 3 is
 * solved.
 *
 * <p>The hero starts in a tutorial room; the number on the business card that the printer shows
 * opens its door. Terminal 3 is taken apart at first. Stachowiak stands in its place and rebuilds
 * it once the hero brings the blueprint from the chest. Its two-digit lock opens with the length of
 * the shortest path from the anteroom to the server room in the graph model of the laboratory that
 * the computer next to the chest shows.
 *
 * <p>Besides the named points of {@link EscapeRoomLevel}, the level file places the computer on
 * {@code graph_model}.
 */
public final class FomoRoomLevel extends EscapeRoomLevel {
  private static final String CODE = "4826";
  // The last three digits of the phone number on the business card in the tutorial room.
  private static final String TUTORIAL_CODE = "279";
  // Anteroom, hall, laboratory, store and server room take 4 + 2 + 3 + 4 minutes; every other path
  // takes at least 14.
  private static final String CHEST_CODE = "13";
  private static final String MODELLING_CYCLE = "rooms/fomo/modelling_cycle.png";
  private static final String GRAPH_MODEL = "rooms/fomo/graph_model.png";
  private static final SimpleIPath TERMINAL = new SimpleIPath("objects/tech/Screen_device.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/tech/Hand_scanner.png");

  /** Steps of the modelling cycle; the door code lists their digits in this order. */
  private static final List<String> STEPS =
      List.of(
          "Fragestellung festlegen",
          "Modell erstellen oder auswählen",
          "Modell untersuchen und auswerten",
          "Modell bei Bedarf verbessern");

  // The step each terminal shows: terminal 1 shows the third step, terminal 2 the first, and so on.
  private static final int[] TERMINAL_STEPS = {2, 0, 3, 1};

  /** The model of the laboratory tells the intro; it speaks through one of its terminals. */
  private static final String INTRO_SPEAKER =
      "[speaker img=" + TERMINAL.pathString() + " name=\"Das Modell\"]";

  private static final List<String> INTRO =
      introPages(
          "Hallo! Ich bin ein vereinfachtes Modell dieses Labors, für meinen Zweck aber genau"
              + " richtig. Leider seid ihr gerade in mir gelandet.",
          4);

  /** Stachowiak looks after the models of the laboratory and took terminal 3 apart. */
  private static final Helper STACHOWIAK =
      new Helper(
          "Stachowiak",
          "[speaker img=rooms/fomo/stachowiak_portrait.png name=\"Stachowiak\"]",
          "character/wizard",
          true,
          2,
          "In der Truhe liegt ein Bauplan. Ihr nehmt ihn mit.",
          List.of(
              "Oh, Besuch! Ich bin Stachowiak, ich pflege die Modelle in diesem Labor.",
              "Beim Aufräumen habe ich Terminal 3 auseinandergenommen und dabei seinen Bauplan"
                  + " verlegt. Jetzt fehlt seine Frage.",
              "Ich kann es wieder aufbauen, mir fehlt nur der Bauplan. Eine Kopie liegt in der"
                  + " Truhe unten rechts. Bringt sie mir!",
              "Die Truhe hat leider ein Zahlenschloss, und die Zahl habe ich vergessen. Ich weiß"
                  + " nur noch, dass sie in meinem Graphmodell des Labors steckt. Der Rechner neben"
                  + " der Truhe zeigt es."),
          List.of(
              "Die Kopie des Bauplans liegt in der Truhe unten rechts. Die Zahl für ihr Schloss"
                  + " steckt in meinem Graphmodell auf dem Rechner daneben. Bringt mir den"
                  + " Bauplan, dann baue ich Terminal 3 wieder auf."),
          List.of(
              "Ist das der Bauplan aus der Truhe? Genau den brauche ich!",
              "Abbilden, verkürzen, zweckmäßig machen ... fertig! Terminal 3 läuft wieder, und es"
                  + " hat eine Frage für euch."),
          List.of(
              "Danke! Ohne den Bauplan hätte ich Terminal 3 nicht wieder aufbauen können. Den"
                  + " Modellierungskreislauf findet ihr auf dem Whiteboard."));

  private static final ChestLock CHEST_LOCK =
      new ChestLock(
          CHEST_CODE,
          "Die Truhe hat ein Zahlenschloss mit zwei Rädchen. Daneben ist eingeritzt: Kürzester"
              + " Weg vom Vorraum zum Serverraum, in Minuten.",
          "Das Schloss bleibt zu. Rechnet die Wege im Graphmodell auf dem Rechner noch einmal"
              + " nach.");

  private static final List<String> STACHOWIAK_WHITEBOARD =
      List.of(
          "Richtig, Modellieren ist ein Kreislauf! Ich zeichne ihn euch an das Whiteboard hier"
              + " an der Wand.",
          "Schaut ihn euch genau an. Vielleicht hilft er euch auch bei der Reihenfolge eurer"
              + " Ziffern.");

  /**
   * One question per terminal. Terminals 1, 2 and 4 ask about regular languages and grammars, as in
   * the automaton room. Terminal 3 asks about the steps of modelling, because its answer is the
   * modelling cycle that orders the digits of the door code.
   */
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
              "Welche Schritte können zum Modellierungsprozess gehören?",
              List.of(
                  ChoiceOption.of("Fragestellung festlegen", "a"),
                  ChoiceOption.of("Modell erstellen oder auswählen", "b"),
                  ChoiceOption.of("Modell untersuchen und Ergebnisse auswerten", "c"),
                  ChoiceOption.of("Modell bei Bedarf verbessern", "d")),
              true,
              Set.of("a", "b", "c", "d"),
              "Modellieren ist ein Kreislauf. Er führt von der Frage über das Modell zur"
                  + " Auswertung und bei Bedarf wieder zurück."),
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
  public FomoRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(
        layout,
        designLabel,
        namedPoints,
        "Modell",
        new Terminals(
            TERMINAL,
            QUESTIONS,
            CODE,
            "Noch nicht ganz",
            "Diese Antwort passt noch nicht zum Modell."));
  }

  /** Shows the intro once play begins. */
  static void intro() {
    intro(INTRO_SPEAKER, INTRO);
  }

  @Override
  protected void setUp() {
    tutorial(TUTORIAL_CODE);
    terminals(STACHOWIAK, CHEST_LOCK);
    computer("graph_model", GRAPH_MODEL);
    whiteboard(STACHOWIAK_WHITEBOARD, hero -> showImage(MODELLING_CYCLE, hero));
    doorLock(
        new DoorLock(
            PANEL,
            "Richtig",
            "Der Code stimmt! Die Tür aus dem Modell springt auf.",
            "Falsch",
            "Der Code stimmt nicht."));
  }

  // Shows the step of the modelling cycle a solved terminal stands for and its digit of the door
  // code.
  @Override
  protected String revealText(int terminal) {
    int step = TERMINAL_STEPS[terminal];
    return "Auf dem Bildschirm leuchtet der Schritt \""
        + STEPS.get(step)
        + "\" und daneben die Ziffer "
        + CODE.charAt(step)
        + ".";
  }
}
