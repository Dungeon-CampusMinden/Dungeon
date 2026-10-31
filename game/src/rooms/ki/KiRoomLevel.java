package rooms.ki;

import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.escape.EscapeRoomLevel;

/**
 * Server level of the AI room. Five terminals ask about machine learning, CAL2, entropy, ID3 and
 * random forests, and a code panel next to the door takes the five-digit code. Each solved terminal
 * shows an attribute with its information gain and one digit of the code. The digits belong in the
 * order ID3 would pick the attributes, from the largest information gain to the smallest. Quinlan
 * writes that rule on the whiteboard once the audience solves terminal 4.
 *
 * <p>The hero starts in a tutorial room; the number on the contact page of Carsten Gips that the
 * printer shows opens its door. Terminal 4 is taken apart at first. Quinlan stands in its place and
 * rebuilds it once the hero brings the blueprint from the chest. Its two-digit lock opens with a
 * leaf of the decision tree that the computer next to the chest shows.
 *
 * <p>Besides the named points of {@link EscapeRoomLevel}, the level file places the computer on
 * {@code decision_tree}.
 */
public final class KiRoomLevel extends EscapeRoomLevel {
  private static final String CODE = "73591";
  // The last three digits of the phone number on the contact page in the tutorial room.
  private static final String TUTORIAL_CODE = "268";
  // The leaf of Quinlan's tree for rain and strong wind; the humidity plays no part on that branch.
  private static final String CHEST_CODE = "83";
  private static final String DECISION_TREE = "rooms/ki/decision_tree.png";
  private static final SimpleIPath TERMINAL = new SimpleIPath("objects/tech/Computer_1.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/tech/Hand_scanner.png");

  /**
   * Attributes the terminals show, from the largest information gain to the smallest. The door code
   * lists their digits in this order.
   */
  private static final List<Attribute> ATTRIBUTES =
      List.of(
          new Attribute("Wetter", "0,25"),
          new Attribute("Luftfeuchte", "0,15"),
          new Attribute("Tageszeit", "0,10"),
          new Attribute("Wind", "0,05"),
          new Attribute("Temperatur", "0,03"));

  // The attribute each terminal shows: terminal 1 shows the fourth attribute, terminal 2 the
  // first, and so on.
  private static final int[] TERMINAL_ATTRIBUTES = {3, 0, 4, 2, 1};

  /** The decision tree tells the intro; it speaks through one of its terminals. */
  private static final String INTRO_SPEAKER =
      "[speaker img=" + TERMINAL.pathString() + " name=\"Der Entscheidungsbaum\"]";

  private static final List<String> INTRO =
      introPages(
          "Hallo! Ich bin ein Entscheidungsbaum, und dieses Labor ist mein Inneres. Leider seid ihr"
              + " gerade in mir gelandet.",
          5);

  /** Quinlan invented ID3 and took terminal 4, which asks about ID3, apart. */
  private static final Helper QUINLAN =
      new Helper(
          "Quinlan",
          "[speaker img=rooms/ki/quinlan_portrait.png name=\"Quinlan\"]",
          "character/char03",
          false,
          3,
          "In der Truhe liegt ein Bauplan. Ihr nehmt ihn mit.",
          List.of(
              "Oh, Besuch! Ich bin Quinlan. Ich habe ID3 erfunden und pflege die Bäume in diesem"
                  + " Labor.",
              "Beim Umbau auf C4.5 habe ich Terminal 4 auseinandergenommen und dabei seinen"
                  + " Bauplan verlegt. Jetzt fehlt seine Frage.",
              "Ich kann es wieder aufbauen, mir fehlt nur der Bauplan. Eine Kopie liegt in der"
                  + " Truhe unten rechts. Bringt sie mir!",
              "Die Truhe hat leider ein Zahlenschloss. Die Zahl liefert mein Entscheidungsbaum auf"
                  + " dem Rechner neben der Truhe."),
          List.of(
              "Die Kopie des Bauplans liegt in der Truhe unten rechts. Die Zahl für ihr Schloss"
                  + " liefert mein Entscheidungsbaum auf dem Rechner daneben. Bringt mir den"
                  + " Bauplan, dann baue ich Terminal 4 wieder auf."),
          List.of(
              "Ist das der Bauplan aus der Truhe? Genau den brauche ich!",
              "Attribut wählen, aufteilen, wiederholen ... fertig! Terminal 4 läuft wieder, und es"
                  + " hat eine Frage für euch."),
          List.of(
              "Danke! Ohne den Bauplan hätte ich Terminal 4 nicht wieder aufbauen können. Die"
                  + " Regel für eure Ziffern steht auf dem Whiteboard."));

  private static final ChestLock CHEST_LOCK =
      new ChestLock(
          CHEST_CODE,
          "Die Truhe hat ein Zahlenschloss mit zwei Rädchen. Daneben ist eingeritzt: Wetter"
              + " Regen, Wind stark, Luftfeuchte hoch. Was sagt der Baum?",
          "Das Schloss bleibt zu. Geht den Baum auf dem Rechner noch einmal von oben durch.");

  private static final List<String> QUINLAN_WHITEBOARD =
      List.of(
          "Richtig, ID3 nimmt immer zuerst das Attribut mit dem größten Informationsgewinn! Ich"
              + " schreibe euch das an das Whiteboard hier an der Wand.",
          "Schaut es euch genau an. Vielleicht hilft es euch auch bei der Reihenfolge eurer"
              + " Ziffern.");

  private static final String WHITEBOARD_RULE =
      "Auf dem Whiteboard steht: Wähle immer zuerst das Attribut mit dem größten"
          + " Informationsgewinn.";

  /**
   * One question per terminal, in the order of the lecture. Terminal 4 asks about ID3, because its
   * answer is the rule that orders the digits of the door code.
   */
  private static final List<Question> QUESTIONS =
      List.of(
          new Question(
              "Was trifft auf unüberwachtes Lernen zu?",
              List.of(
                  ChoiceOption.of("Es gibt keine Trainingsdaten.", "a"),
                  ChoiceOption.of("Die Trainingsdaten werden während des Trainings gelabelt.", "b"),
                  ChoiceOption.of("Die Trainingsdaten sind gelabelt.", "c"),
                  ChoiceOption.of("Die Trainingsdaten sind nicht gelabelt.", "d")),
              false,
              Set.of("d"),
              "Beim unüberwachten Lernen verrät kein Lehrer die Klasse. Daten gibt es trotzdem."),
          new Question(
              "Bei CAL2: Was passiert, wenn der Baum auf ein Trainingsbeispiel einen * ausgibt?",
              List.of(
                  ChoiceOption.of(
                      "Man merkt sich das Beispiel und kommt später darauf zurück.", "a"),
                  ChoiceOption.of("Der * wird durch die Klasse des Beispiels ersetzt.", "b"),
                  ChoiceOption.of("Man macht mit dem nächsten Beispiel weiter.", "c"),
                  ChoiceOption.of("Der * wird durch den nächsten Test ersetzt.", "d")),
              false,
              Set.of("b"),
              "Der * steht für Nichtwissen. Was weiß der Baum nach diesem Beispiel?"),
          new Question(
              "Welche Aussage über Entropie stimmt?",
              List.of(
                  ChoiceOption.of("Entropie ist wichtig, damit CAL3 funktioniert.", "a"),
                  ChoiceOption.of("Entropie ist der Informationsgehalt einer Variable.", "b"),
                  ChoiceOption.of(
                      "Die Entropie der Trainingsmenge gibt an, wie viele Daten vorhanden sind.",
                      "c"),
                  ChoiceOption.of(
                      "Die Entropie wird für die statistische Auswertung von CAL3 verwendet.",
                      "d")),
              false,
              Set.of("b"),
              "CAL3 entscheidet mit den Schwellen S1 und S2. Und eine faire Münze hat 1 Bit, egal"
                  + " wie oft man wirft."),
          new Question(
              "Welche Aussagen über ID3 stimmen?",
              List.of(
                  ChoiceOption.of(
                      "ID3 wählt das Attribut mit dem größten Informationsgewinn.", "a"),
                  ChoiceOption.of(
                      "ID3 betrachtet alle Beispiele, die an einem Knoten ankommen, auf einmal.",
                      "b"),
                  ChoiceOption.of(
                      "ID3 geht wie CAL2 Beispiel für Beispiel durch die Trainingsmenge.", "c"),
                  ChoiceOption.of("ID3 bevorzugt Attribute mit wenigen Ausprägungen.", "d")),
              true,
              Set.of("a", "b"),
              "ID3 behebt eine Schwäche von CAL2. C4.5 musste ID3 später bei Attributen mit vielen"
                  + " Ausprägungen bremsen."),
          new Question(
              "Wodurch werden die Bäume in einem Random Forest unterschiedlich?",
              List.of(
                  ChoiceOption.of(
                      "Jeder Baum lernt auf einer zufälligen Stichprobe mit Zurücklegen.", "a"),
                  ChoiceOption.of(
                      "An jedem Knoten wird nur eine zufällige Auswahl der Merkmale betrachtet.",
                      "b"),
                  ChoiceOption.of("Jeder Baum nutzt einen anderen Lernalgorithmus.", "c"),
                  ChoiceOption.of(
                      "Jeder Baum bekommt andere Klassen für dieselben Beispiele.", "d")),
              true,
              Set.of("a", "b"),
              "Der Lernalgorithmus bleibt gleich. Der Zufall steckt an zwei Stellen, in den Daten"
                  + " und in den Merkmalen."));

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public KiRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(
        layout,
        designLabel,
        namedPoints,
        "Entscheidungsbaum",
        new Terminals(
            TERMINAL, QUESTIONS, CODE, "Noch nicht ganz", "Diese Antwort passt noch nicht."));
  }

  /** Shows the intro once play begins. */
  static void intro() {
    intro(INTRO_SPEAKER, INTRO);
  }

  @Override
  protected void setUp() {
    tutorial(TUTORIAL_CODE);
    terminals(QUINLAN, CHEST_LOCK);
    computer("decision_tree", DECISION_TREE);
    whiteboard(
        QUINLAN_WHITEBOARD,
        hero -> DialogUtils.showTextPopup(WHITEBOARD_RULE, "Whiteboard", hero.id()));
    doorLock(
        new DoorLock(
            PANEL,
            "Richtig",
            "Der Code stimmt! Die Tür aus dem Baum springt auf.",
            "Falsch",
            "Der Code stimmt nicht."));
  }

  // Shows the attribute a solved terminal stands for, its information gain and its digit of the
  // door code.
  @Override
  protected String revealText(int terminal) {
    int index = TERMINAL_ATTRIBUTES[terminal];
    Attribute attribute = ATTRIBUTES.get(index);
    return "Auf dem Bildschirm leuchtet das Attribut \""
        + attribute.name()
        + "\" mit dem Informationsgewinn "
        + attribute.gain()
        + " und daneben die Ziffer "
        + CODE.charAt(index)
        + ".";
  }

  /**
   * Attribute a terminal shows.
   *
   * @param name name of the attribute
   * @param gain information gain as shown, with a decimal comma
   */
  private record Attribute(String name, String gain) {}
}
