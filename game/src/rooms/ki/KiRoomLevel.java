package rooms.ki;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.path.SimpleIPath;
import feature.audience.AudienceSystem;
import feature.audience.AudienceTarget;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.components.UIComponent;
import feature.entities.NPCFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.types.DecoImagePrefab;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Server level of the AI room. Five terminals ask about machine learning, CAL2, entropy, ID3 and
 * random forests, and a code panel next to the door takes the five-digit code. Each solved terminal
 * shows an attribute with its information gain and one digit of the code. The digits belong in the
 * order ID3 would pick the attributes, from the largest information gain to the smallest. Quinlan
 * writes that rule on the whiteboard once the audience solves terminal 4. After a wrong answer the
 * audience gets a hint and the same question again. Walking through the open door ends the game.
 *
 * <p>The hero starts in a tutorial room. A robot there asks a practice single-choice and a practice
 * multiple-choice question, then tells the audience that the code of the tutorial door is the last
 * three digits of the phone number. The number is on the contact page of Carsten Gips that the
 * level file places on a printer as a {@code deco-image} prefab.
 *
 * <p>Terminal 4 is taken apart at first. Quinlan stands in its place and rebuilds it once the hero
 * brings the blueprint from the chest. A two-digit lock keeps the chest shut. Its code is a leaf of
 * the decision tree that a computer next to the chest shows. The blueprint is a flag of the level,
 * not an inventory item, so all of it happens in dialogs the audience can vote on.
 *
 * <p>The audience picks targets from a list instead of single steps. The room offers each object
 * once the story reaches it: at first only the robot, after the practice the keypad and the printer
 * with the contact page, behind the tutorial door Quinlan, terminals 1, 2, 3 and 5 and the door
 * lock, after the first talk with Quinlan the computer and the chest, then terminal 4 and the
 * whiteboard. Finished objects leave the list, and once the door is open, the exit joins it.
 *
 * <p>The level file places the terminals on its named points {@code q1} to {@code q5}, the chest on
 * {@code chest} and the code panel on {@code panel}; {@code door} is the door tile and {@code exit}
 * the tile behind it. The robot stands on {@code tutorial_npc}, the tutorial door is {@code
 * tutorial_door} and its code panel {@code tutorial_panel}. The whiteboard hangs on {@code
 * whiteboard} and the computer with the decision tree stands on {@code decision_tree}.
 */
public final class KiRoomLevel extends DungeonLevel {
  private static final String CODE = "73591";
  // The last three digits of the phone number on the contact page in the tutorial room.
  private static final String TUTORIAL_CODE = "268";
  // The leaf of Quinlan's tree for rain and strong wind; the humidity plays no part on that branch.
  private static final String CHEST_CODE = "83";
  private static final String DECISION_TREE = "rooms/ki/decision_tree.png";
  private static final SimpleIPath COMPUTER =
      new SimpleIPath("objects/tech/Computer_station_1.png");

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
  private static final SimpleIPath TERMINAL = new SimpleIPath("objects/tech/Computer_1.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/tech/Hand_scanner.png");
  private static final SimpleIPath CHEST_TEXTURE = new SimpleIPath("objects/treasurechest");
  private static final SimpleIPath KEYPAD = new SimpleIPath("objects/keypad/off.png");

  /** The decision tree tells the intro; it speaks through one of its terminals. */
  private static final String INTRO_SPEAKER =
      "[speaker img=" + TERMINAL.pathString() + " name=\"Der Entscheidungsbaum\"]";

  private static final List<String> INTRO =
      List.of(
          "Hallo! Ich bin ein Entscheidungsbaum, und dieses Labor ist mein Inneres. Leider seid ihr"
              + " gerade in mir gelandet.",
          "Ihr spielt alle zusammen und steuert gemeinsam eine Figur. Wer noch nicht dabei ist,"
              + " scannt den QR-Code in der Ecke oder öffnet die Adresse dort und gibt den Code"
              + " ein.",
          "Gesteuert wird per Abstimmung. Alle geben auf ihrem Gerät eine Stimme ab, und die"
              + " Figur macht, was die meisten gewählt haben. Bei Gleichstand entscheidet der"
              + " Zufall.",
          "Ihr wählt aus einer Liste, wohin die Figur geht. Jedes Ziel im Raum hat einen gelben"
              + " Rand und darüber seinen Buchstaben und seinen Namen. Die Figur läuft selbst hin"
              + " und benutzt, was dort steht.",
          "Die erste Stimme startet eine Runde von 15 Sekunden. Bis sie vorbei ist, könnt ihr"
              + " eure Stimme noch ändern, es zählt die letzte. Neue Ziele kommen dazu, sobald ihr"
              + " weiterkommt.",
          "Bei Fragen haben die Antworten Buchstaben. Bei Single-Choice wählt ihr genau eine"
              + " Antwort. Bei Multiple-Choice wählt ihr alle passenden und drückt"
              + " \"Abschicken\", dann zählt die ganze Kombination.",
          "Für eine Frage habt ihr 2 Minuten. Hat die Hälfte abgestimmt, bleiben noch 10"
              + " Sekunden, haben alle abgestimmt, noch 3. Texte wie diesen bestätigt ihr mit"
              + " \"Weiter\".",
          "Codes tippt ihr auf dem Ziffernblock ein. Meine fünf Terminals im Labor verraten je"
              + " eine Ziffer des Türcodes, wenn ihr ihre Frage richtig beantwortet.",
          "In welcher Reihenfolge die Ziffern zum Türcode gehören, müsst ihr selbst"
              + " herausfinden. Nach einer falschen Antwort bekommt ihr einen Tipp und versucht"
              + " es noch einmal.",
          "Ihr startet im Vorraum. Sprecht zuerst mit dem Roboter. Er übt mit euch und verrät,"
              + " wie ihr ins Labor kommt.");

  /** The robot in the tutorial room practises the two kinds of questions with the audience. */
  private static final String ROBOT_SPEAKER =
      "[speaker img=rooms/fomo/robot_portrait.png name=\"Robo\"]";

  private static final List<String> ROBOT_SINGLE_INTRO =
      List.of(
          "Hallo, ich bin Robo! Bevor es ins Labor geht, üben wir die beiden Arten von Fragen.",
          "Zuerst eine Single-Choice-Frage. Dort wählt ihr genau eine Antwort.");

  private static final ChoicePractice SINGLE_PRACTICE =
      new ChoicePractice(
          "Hey, das ist eine Single-Choice-Frage.",
          List.of(
              ChoiceOption.of("Pizza", "pizza"),
              ChoiceOption.of("Pasta", "pasta"),
              ChoiceOption.of("Salat", "salad")),
          false);

  private static final ChoicePractice MULTIPLE_PRACTICE =
      new ChoicePractice(
          "Hey, das ist eine Multiple-Choice-Frage.",
          List.of(
              ChoiceOption.of("Kaffee", "coffee"),
              ChoiceOption.of("Tee", "tea"),
              ChoiceOption.of("Wasser", "water"),
              ChoiceOption.of("Kakao", "cocoa")),
          true);

  private static final String MULTIPLE_INTRO =
      "Jetzt eine Multiple-Choice-Frage. Wählt alle passenden Antworten und drückt dann"
          + " \"Abschicken\". Gezählt wird die ganze Kombination.";

  private static final String DOOR_HINT =
      "Die Tür ins Labor ist verschlossen. Der Code für das Tastenfeld daneben sind die letzten"
          + " drei Ziffern der Telefonnummer. Schaut euch hier im Raum um.";

  /** Quinlan invented ID3 and took terminal 4 apart. */
  private static final String QUINLAN_SPEAKER =
      "[speaker img=rooms/ki/quinlan_portrait.png name=\"Quinlan\"]";

  private static final List<String> QUINLAN_FIRST_TALK =
      List.of(
          "Oh, Besuch! Ich bin Quinlan. Ich habe ID3 erfunden und pflege die Bäume in diesem"
              + " Labor.",
          "Beim Umbau auf C4.5 habe ich Terminal 4 auseinandergenommen und dabei seinen Bauplan"
              + " verlegt. Jetzt fehlt seine Frage.",
          "Ich kann es wieder aufbauen, mir fehlt nur der Bauplan. Eine Kopie liegt in der Truhe"
              + " unten rechts. Bringt sie mir!",
          "Die Truhe hat leider ein Zahlenschloss. Die Zahl liefert mein Entscheidungsbaum auf"
              + " dem Rechner neben der Truhe.");

  private static final List<String> QUINLAN_REMINDER =
      List.of(
          "Die Kopie des Bauplans liegt in der Truhe unten rechts. Die Zahl für ihr Schloss"
              + " liefert mein Entscheidungsbaum auf dem Rechner daneben. Bringt mir den Bauplan,"
              + " dann baue ich Terminal 4 wieder auf.");

  private static final List<String> QUINLAN_REPAIR =
      List.of(
          "Ist das der Bauplan aus der Truhe? Genau den brauche ich!",
          "Attribut wählen, aufteilen, wiederholen ... fertig! Terminal 4 läuft wieder, und es"
              + " hat eine Frage für euch.");

  private static final List<String> QUINLAN_THANKS =
      List.of(
          "Danke! Ohne den Bauplan hätte ich Terminal 4 nicht wieder aufbauen können. Die Regel"
              + " für eure Ziffern steht auf dem Whiteboard.");

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

  // Quinlan took apart terminal 4, which asks about ID3.
  private static final int BROKEN = 3;

  private final boolean[] solved = new boolean[QUESTIONS.size()];
  private boolean ended;
  private boolean practised;
  private Entity quinlan;
  private boolean metQuinlan;
  private boolean chestUnlocked;
  private boolean hasBlueprint;
  private boolean rebuilt;
  private boolean ruleWritten;
  // Whether the printer of the level file is a target yet; the level spawns it on its own.
  private boolean printerOffered;

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public KiRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Entscheidungsbaum");
  }

  /** Shows the intro once play begins. */
  static void intro() {
    story(INTRO_SPEAKER, INTRO, 0, () -> {}, Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  // Shows the pages of a speaker one after another and runs the callback after the last one. Each
  // page is a dialog of its own, because the audience's "Weiter" confirms a whole dialog.
  private static void story(
      String speaker, List<String> pages, int page, Runnable then, int... targetIds) {
    if (page >= pages.size()) {
      then.run();
      return;
    }
    DialogFactory.showDialogDialog(
        speaker + pages.get(page),
        () -> story(speaker, pages, page + 1, then, targetIds),
        targetIds);
  }

  @Override
  protected void onFirstTick() {
    door("door").ifPresent(DoorTile::close);
    door("tutorial_door").ifPresent(DoorTile::close);
    BooleanSupplier always = () -> true;
    target(robot(), "Robo", this::inTutorial);
    target(
        station(getPoint("tutorial_panel"), KEYPAD, "tutorial-panel", this::tutorialPanel),
        "Tastenfeld",
        () -> practised && inTutorial());
    for (int terminal = 0; terminal < QUESTIONS.size(); terminal++) {
      if (terminal == BROKEN) continue;
      int asked = terminal;
      target(
          station(
              getPoint("q" + (terminal + 1)),
              TERMINAL,
              "terminal-" + (terminal + 1),
              hero -> ask(asked, hero)),
          "Terminal " + (terminal + 1),
          always);
    }
    quinlan = target(quinlan(), "Quinlan", always);
    target(chest(), "Truhe", () -> metQuinlan && !hasBlueprint && !rebuilt);
    target(
        station(
            getPoint("decision_tree"),
            COMPUTER,
            "decision-tree",
            hero -> showImage(DECISION_TREE, hero)),
        "Rechner",
        () -> metQuinlan);
    target(whiteboard(), "Whiteboard", () -> ruleWritten);
    target(
        station(getPoint("panel"), PANEL, "code-panel", this::codePanel),
        "Türschloss",
        () -> !isOpen("door"));
    Game.add(AudienceTarget.tile(getPoint("exit"), "Ausgang", () -> isOpen("door")));
  }

  @Override
  protected void onTick() {
    if (!printerOffered) {
      prefabs(DecoImagePrefab.class).stream()
          .findFirst()
          .flatMap(DecoImagePrefab::decoEntity)
          .ifPresent(
              printer -> {
                target(printer, "Drucker", () -> practised && inTutorial());
                printerOffered = true;
              });
    }
    if (ended) return;
    boolean atExit =
        Game.allPlayers()
            .anyMatch(
                hero ->
                    Game.tileAtEntity(hero)
                        .map(tile -> tile.position().equals(getPoint("exit")))
                        .orElse(false));
    if (atExit) {
      ended = true;
      KiRoom.finished();
    }
  }

  private Optional<DoorTile> door(String point) {
    return tileAt(getPoint(point)).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }

  private boolean isOpen(String door) {
    return door(door).map(DoorTile::isOpen).orElse(false);
  }

  // The robot, the keypad and the printer are of no more use once the tutorial door is open.
  private boolean inTutorial() {
    return !isOpen("tutorial_door");
  }

  // Offers an entity in the audience's list of targets while available says so.
  private static Entity target(Entity entity, String label, BooleanSupplier available) {
    entity.add(new AudienceTarget(label, available));
    return entity;
  }

  private Entity station(Point position, SimpleIPath texture, String name, Consumer<Entity> use) {
    Entity station = new Entity(name);
    station.add(new PositionComponent(position));
    station.add(new DrawComponent(new Animation(texture)));
    station.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    station.add(
        new InteractionComponent(new Interaction((self, hero) -> use.accept(hero), 1.5f, true)));
    Game.add(station);
    return station;
  }

  // Hangs the whiteboard on the wall. Its hitbox spans the two tiles the board covers, so the hero
  // can use it from in front of either; the board is drawn two tiles wide to match. It is not a
  // decoration of the level file, so the level editor leaves it alone.
  private Entity whiteboard() {
    Deco deco = Deco.LabWhiteboard;
    // The board image is 50 by 37 pixels and its height is the scale, so this makes it 2 tiles
    // wide.
    AnimationConfig look = new AnimationConfig().scaleX(2f * 37 / 50);
    Entity board =
        DecoFactory.createDeco(
            getPoint("whiteboard"), deco, deco.defaultDepth(), look, new Rectangle(2f, 1f, 0, 0));
    board.remove(DecoComponent.class);
    board.add(
        new InteractionComponent(new Interaction((self, hero) -> whiteboard(hero), 1.5f, true)));
    Game.add(board);
    return board;
  }

  // Empty until Quinlan writes the rule, so it does not give away terminal 4.
  private void whiteboard(Entity hero) {
    DialogUtils.showTextPopup(
        ruleWritten ? WHITEBOARD_RULE : "Das Whiteboard ist noch leer.", "Whiteboard", hero.id());
  }

  private static void showImage(String image, Entity hero) {
    DialogFactory.show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, image)
            .build(),
        hero.id());
  }

  // Places Quinlan on terminal 4. His sprite is two tiles wide, and the hitbox of the NPC factory
  // fits it, so he keeps that hitbox.
  private Entity quinlan() {
    Point point = getPoint("q" + (BROKEN + 1));
    Entity npc = NPCFactory.createNPC(point, "character/char03");
    npc.add(
        new InteractionComponent(new Interaction((self, hero) -> talkToQuinlan(hero), 1.5f, true)));
    AudienceSystem.standOn(npc, point);
    Game.add(npc);
    return npc;
  }

  private Entity robot() {
    Entity npc = NPCFactory.createNPC(getPoint("tutorial_npc"), "character/roboter_cleaner");
    // This sprite is one tile wide; the factory's hitbox fits two-tile sprites.
    npc.remove(CollideComponent.class);
    CollideComponent hitbox = new CollideComponent(Vector2.of(0.1f, 0.1f), Vector2.of(0.8f, 0.8f));
    hitbox.isSolid(true);
    npc.add(hitbox);
    npc.add(
        new InteractionComponent(new Interaction((self, hero) -> talkToRobot(hero), 1.5f, true)));
    AudienceSystem.standOn(npc, getPoint("tutorial_npc"));
    Game.add(npc);
    return npc;
  }

  // The robot practises a single-choice and a multiple-choice question, says what the audience
  // chose, and then gives the hint for the tutorial door. Afterwards it only repeats the hint.
  private void talkToRobot(Entity hero) {
    if (practised) {
      story(ROBOT_SPEAKER, List.of(DOOR_HINT), 0, () -> {}, hero.id());
      return;
    }
    story(
        ROBOT_SPEAKER,
        ROBOT_SINGLE_INTRO,
        0,
        () -> practise(SINGLE_PRACTICE, hero, chosen -> afterSinglePractice(hero, chosen)),
        hero.id());
  }

  private void afterSinglePractice(Entity hero, String chosen) {
    story(
        ROBOT_SPEAKER,
        List.of("Ihr habt " + chosen + " gewählt. Gut gemacht!", MULTIPLE_INTRO),
        0,
        () ->
            practise(
                MULTIPLE_PRACTICE, hero, combination -> afterMultiplePractice(hero, combination)),
        hero.id());
  }

  private void afterMultiplePractice(Entity hero, String chosen) {
    story(
        ROBOT_SPEAKER,
        List.of(
            "Ihr habt " + chosen + " gewählt. Genau so geht es auch bei den Terminals im Labor.",
            DOOR_HINT),
        0,
        () -> practised = true,
        hero.id());
  }

  // Asks a practice question. Every answer counts, and the callback receives the chosen options as
  // text, such as "Kaffee und Tee".
  private static void practise(ChoicePractice practice, Entity hero, Consumer<String> then) {
    DialogFactory.showMultipleChoiceDialog(
        practice.text(),
        "Übungsfrage",
        practice.options(),
        practice.multiple(),
        false,
        payload -> then.accept(labels(practice.options(), chosen(payload))),
        null,
        hero.id());
  }

  // Labels of the chosen options in display order, joined like "Kaffee, Tee und Wasser".
  private static String labels(List<ChoiceOption> options, Set<String> values) {
    List<String> labels =
        options.stream()
            .filter(option -> values.contains(option.value()))
            .map(ChoiceOption::label)
            .toList();
    if (labels.size() <= 1) return String.join("", labels);
    return String.join(", ", labels.subList(0, labels.size() - 1)) + " und " + labels.getLast();
  }

  private void talkToQuinlan(Entity hero) {
    if (rebuilt) {
      story(QUINLAN_SPEAKER, QUINLAN_THANKS, 0, () -> {}, hero.id());
    } else if (hasBlueprint) {
      story(QUINLAN_SPEAKER, QUINLAN_REPAIR, 0, () -> rebuild(hero), hero.id());
    } else if (metQuinlan) {
      story(QUINLAN_SPEAKER, QUINLAN_REMINDER, 0, () -> {}, hero.id());
    } else {
      metQuinlan = true;
      story(QUINLAN_SPEAKER, QUINLAN_FIRST_TALK, 0, () -> {}, hero.id());
    }
  }

  // Quinlan steps onto a free tile next to terminal 4, the terminal is back, and its question opens
  // right away. The hero stays where it is, next to the terminal.
  private void rebuild(Entity hero) {
    rebuilt = true;
    Point terminal = getPoint("q" + (BROKEN + 1));
    Point heroTile = Game.tileAtEntity(hero).map(Tile::position).orElse(null);
    Stream.of(
            new Point(terminal.x() - 1, terminal.y()),
            new Point(terminal.x() + 1, terminal.y()),
            new Point(terminal.x(), terminal.y() + 1),
            new Point(terminal.x(), terminal.y() - 1))
        .filter(tile -> !tile.equals(heroTile))
        .filter(tile -> tileAt(tile).map(Tile::isAccessible).orElse(false))
        .findFirst()
        .ifPresent(aside -> AudienceSystem.standOn(quinlan, aside));
    target(
        station(terminal, TERMINAL, "terminal-" + (BROKEN + 1), other -> ask(BROKEN, other)),
        "Terminal " + (BROKEN + 1),
        () -> true);
    ask(BROKEN, hero);
  }

  private Entity chest() {
    Entity chest = new Entity("chest");
    chest.add(new PositionComponent(getPoint("chest")));
    chest.add(new DrawComponent(CHEST_TEXTURE, "closed"));
    chest.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    chest.add(
        new InteractionComponent(
            new Interaction((self, hero) -> openChest(self, hero), 1.5f, true)));
    Game.add(chest);
    return chest;
  }

  private void openChest(Entity chest, Entity hero) {
    if (hasBlueprint || rebuilt) {
      DialogUtils.showTextPopup("Die Truhe ist leer.", "Truhe", hero.id());
      return;
    }
    if (!chestUnlocked) {
      askCode(
          hero,
          "Zahlenschloss",
          "Die Truhe hat ein Zahlenschloss mit zwei Rädchen. Daneben ist eingeritzt: Wetter"
              + " Regen, Wind stark, Luftfeuchte hoch. Was sagt der Baum?",
          CHEST_CODE.length(),
          code -> {
            if (code.equals(CHEST_CODE)) {
              chestUnlocked = true;
              openChest(chest, hero);
            } else {
              DialogUtils.showTextPopup(
                  "Das Schloss bleibt zu. Geht den Baum auf dem Rechner noch einmal von oben"
                      + " durch.",
                  "Falsch",
                  hero.id());
            }
          });
      return;
    }
    hasBlueprint = true;
    chest.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("open_empty", null);
    DialogUtils.showTextPopup(
        "In der Truhe liegt ein Bauplan. Ihr nehmt ihn mit.", "Truhe", hero.id());
  }

  private void ask(int terminal, Entity hero) {
    if (solved[terminal]) {
      revealDigit(terminal, hero, () -> {});
      return;
    }
    Question question = QUESTIONS.get(terminal);
    DialogFactory.showMultipleChoiceDialog(
        question.text(),
        "Terminal " + (terminal + 1),
        question.options(),
        question.multiple(),
        false,
        payload -> answered(terminal, chosen(payload).equals(question.correct()), hero),
        null,
        hero.id());
  }

  private void codePanel(Entity hero) {
    if (isOpen("door")) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    askCode(
        hero,
        "Tastenfeld",
        "Gebt den fünfstelligen Türcode ein.",
        CODE.length(),
        code -> {
          if (code.equals(CODE)) {
            door("door").ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(
                "Der Code stimmt! Die Tür aus dem Baum springt auf.", "Richtig", hero.id());
          } else if (sorted(code).equals(sorted(CODE))) {
            DialogUtils.showTextPopup(
                "Die Ziffern stimmen, aber ihre Reihenfolge nicht.", "Fast", hero.id());
          } else {
            DialogUtils.showTextPopup("Der Code stimmt nicht.", "Falsch", hero.id());
          }
        });
  }

  private void tutorialPanel(Entity hero) {
    if (isOpen("tutorial_door")) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    askCode(
        hero,
        "Tastenfeld",
        "Gebt den dreistelligen Code ein.",
        TUTORIAL_CODE.length(),
        code -> {
          if (code.equals(TUTORIAL_CODE)) {
            door("tutorial_door").ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(
                "Der Code stimmt! Die Tür ins Labor geht auf.", "Richtig", hero.id());
          } else {
            DialogUtils.showTextPopup("Der Code stimmt nicht.", "Falsch", hero.id());
          }
        });
  }

  /**
   * Asks for a code on the digit keypad of the devices.
   *
   * @param hero the hero using the lock
   * @param title title of the dialog
   * @param question what the dialog asks for
   * @param digits number of digits of the code
   * @param onCode receives the entered code
   */
  private static void askCode(
      Entity hero, String title, String question, int digits, Consumer<String> onCode) {
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.FREE_INPUT)
                .put(DialogContextKeys.TITLE, title)
                .put(DialogContextKeys.QUESTION, question)
                .put(DialogContextKeys.INPUT_PLACEHOLDER, "Code")
                .put(DialogContextKeys.CONFIRM_LABEL, "Öffnen")
                .put(DialogContextKeys.CANCEL_LABEL, "Abbrechen")
                .put(AudienceSystem.CODE_DIGITS, digits)
                .build(),
            true,
            true,
            hero.id());
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        payload -> {
          UIUtils.closeDialog(ui);
          onCode.accept(
              payload instanceof DialogResponseMessage.StringValue(String value)
                  ? value.strip()
                  : "");
        });
    ui.registerCallback(DialogContextKeys.ON_CANCEL, payload -> UIUtils.closeDialog(ui));
  }

  // The digits of a code in ascending order, to tell a wrong order from wrong digits.
  private static String sorted(String code) {
    return code.chars()
        .sorted()
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  private void answered(int terminal, boolean correct, Entity hero) {
    if (!correct) {
      reject(terminal, hero);
      return;
    }
    solved[terminal] = true;
    // Terminal 4 asks about ID3. Once the audience solves it, Quinlan writes the rule on the
    // whiteboard.
    if (terminal == BROKEN && !ruleWritten) {
      ruleWritten = true;
      revealDigit(
          terminal, hero, () -> story(QUINLAN_SPEAKER, QUINLAN_WHITEBOARD, 0, () -> {}, hero.id()));
      return;
    }
    revealDigit(terminal, hero, () -> {});
  }

  // Rejects a wrong answer with the hint of the terminal. Once the audience has read it, the same
  // question opens again.
  private void reject(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        "Diese Antwort passt noch nicht. Versucht es noch einmal. Tipp: "
            + QUESTIONS.get(terminal).hint(),
        "Noch nicht ganz",
        () -> ask(terminal, hero),
        hero.id());
  }

  // The values of the options the audience chose.
  private static Set<String> chosen(DialogResponseMessage.Payload payload) {
    return switch (payload) {
      case DialogResponseMessage.StringValue(String value) -> Set.of(value);
      case DialogResponseMessage.StringList list -> Set.copyOf(Arrays.asList(list.values()));
      default -> Set.of();
    };
  }

  // Shows the attribute a solved terminal stands for, its information gain and its digit of the
  // door code, then runs the callback.
  private static void revealDigit(int terminal, Entity hero, Runnable then) {
    int index = TERMINAL_ATTRIBUTES[terminal];
    Attribute attribute = ATTRIBUTES.get(index);
    DialogUtils.showTextPopup(
        "Auf dem Bildschirm leuchtet das Attribut \""
            + attribute.name()
            + "\" mit dem Informationsgewinn "
            + attribute.gain()
            + " und daneben die Ziffer "
            + CODE.charAt(index)
            + ".",
        "Richtig",
        then::run,
        hero.id());
  }

  /**
   * Attribute a terminal shows.
   *
   * @param name name of the attribute
   * @param gain information gain as shown, with a decimal comma
   */
  private record Attribute(String name, String gain) {}

  /**
   * Question of a terminal.
   *
   * @param text question text
   * @param options answer options; their values identify the answers
   * @param multiple whether several options may be chosen
   * @param correct values of the correct options; the chosen set must match exactly
   * @param hint hint shown after a wrong answer
   */
  private record Question(
      String text,
      List<ChoiceOption> options,
      boolean multiple,
      Set<String> correct,
      String hint) {}

  /**
   * Practice question of the robot; every answer counts.
   *
   * @param text question text
   * @param options answer options
   * @param multiple whether several options may be chosen
   */
  private record ChoicePractice(String text, List<ChoiceOption> options, boolean multiple) {}
}
