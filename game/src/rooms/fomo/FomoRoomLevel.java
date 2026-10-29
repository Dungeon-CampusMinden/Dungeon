package rooms.fomo;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Server level of the FOMO room. Three terminals ask about regular languages and grammars and one
 * about the steps of modelling, and a code panel next to the door takes the four-digit code. Each
 * solved terminal shows one step of the modelling cycle with one digit of the code. The digits
 * belong in the order of the cycle, which Stachowiak draws on the whiteboard once terminal 3 is
 * solved. After a wrong answer the audience gets a hint and the same question again. Walking
 * through the open door ends the game.
 *
 * <p>The hero starts in a tutorial room. A robot there asks a practice single-choice and a practice
 * multiple-choice question, then tells the audience that the code of the tutorial door is the last
 * three digits of the phone number. The number is on the business card the level file places as a
 * {@code deco-image} prefab.
 *
 * <p>The third terminal is taken apart at first. Stachowiak stands in its place and rebuilds it
 * once the hero brings the blueprint from the chest. A two-digit lock keeps the chest shut. Its
 * code is the length of the shortest path from the anteroom to the server room in the graph model
 * of the laboratory that a computer next to the chest shows. The blueprint is a flag of the level,
 * not an inventory item, so all of it happens in dialogs the audience can vote on.
 *
 * <p>The level file places the terminals on its named points {@code q1} to {@code q4}, the chest on
 * {@code chest} and the code panel on {@code panel}; {@code door} is the door tile and {@code exit}
 * the tile behind it. The robot stands on {@code tutorial_npc}, the tutorial door is {@code
 * tutorial_door} and its code panel {@code tutorial_panel}. The whiteboard hangs on {@code
 * whiteboard} and the computer with the graph model stands on {@code graph_model}.
 */
public final class FomoRoomLevel extends DungeonLevel {
  private static final String CODE = "4826";
  // The last three digits of the phone number on the business card in the tutorial room.
  private static final String TUTORIAL_CODE = "279";
  // Anteroom, hall, laboratory, store and server room take 4 + 2 + 3 + 4 minutes; every other path
  // takes at least 14.
  private static final String CHEST_CODE = "13";
  private static final String MODELLING_CYCLE = "rooms/fomo/modelling_cycle.png";
  private static final String GRAPH_MODEL = "rooms/fomo/graph_model.png";
  private static final SimpleIPath COMPUTER =
      new SimpleIPath("objects/tech/Computer_station_1.png");

  /** Steps of the modelling cycle; the door code lists their digits in this order. */
  private static final List<String> STEPS =
      List.of(
          "Fragestellung festlegen",
          "Modell erstellen oder auswählen",
          "Modell untersuchen und auswerten",
          "Modell bei Bedarf verbessern");

  // The step each terminal shows: terminal 1 shows the third step, terminal 2 the first, and so on.
  private static final int[] TERMINAL_STEPS = {2, 0, 3, 1};
  private static final SimpleIPath TERMINAL = new SimpleIPath("objects/tech/Screen_device.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/tech/Hand_scanner.png");
  private static final SimpleIPath CHEST_TEXTURE = new SimpleIPath("objects/treasurechest");
  private static final SimpleIPath KEYPAD = new SimpleIPath("objects/keypad/off.png");

  /** The model of the laboratory tells the intro; it speaks through one of its terminals. */
  private static final String INTRO_SPEAKER =
      "[speaker img=" + TERMINAL.pathString() + " name=\"Das Modell\"]";

  private static final List<String> INTRO =
      List.of(
          "Hallo! Ich bin ein vereinfachtes Modell dieses Labors, für meinen Zweck aber genau"
              + " richtig. Leider seid ihr gerade in mir gelandet.",
          "Ihr spielt alle zusammen und steuert gemeinsam eine Figur. Wer noch nicht dabei ist,"
              + " scannt den QR-Code in der Ecke oder öffnet die Adresse dort und gibt den Code"
              + " ein.",
          "Gesteuert wird per Abstimmung. Alle geben auf ihrem Gerät eine Stimme ab, und die"
              + " Figur macht, was die meisten gewählt haben. Bei Gleichstand entscheidet der"
              + " Zufall.",
          "Die Pfeile bewegen die Figur ein Feld weit. Die erste Stimme startet eine Runde von 5"
              + " Sekunden. Bis sie vorbei ist, könnt ihr eure Stimme noch ändern, es zählt die"
              + " letzte.",
          "Alles, was ihr benutzen könnt, hat einen gelben Rand. Rot umrandet ist, was"
              + " \"Benutzen\" gerade auslösen würde. Dafür muss die Figur direkt davor stehen"
              + " und hinschauen.",
          "Bei Fragen haben die Antworten Nummern. Bei Single-Choice wählt ihr genau eine"
              + " Antwort. Bei Multiple-Choice wählt ihr alle passenden und drückt"
              + " \"Abschicken\", dann zählt die ganze Kombination.",
          "Für eine Frage habt ihr 2 Minuten. Hat die Hälfte abgestimmt, bleiben noch 10"
              + " Sekunden, haben alle abgestimmt, noch 3. Texte wie diesen bestätigt ihr mit"
              + " \"Weiter\".",
          "Codes tippt ihr auf dem Ziffernblock ein. Meine vier Terminals im Labor verraten je"
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

  /** Stachowiak looks after the models of the laboratory and took terminal 3 apart. */
  private static final String STACHOWIAK_SPEAKER =
      "[speaker img=rooms/fomo/stachowiak_portrait.png name=\"Stachowiak\"]";

  private static final List<String> STACHOWIAK_FIRST_TALK =
      List.of(
          "Oh, Besuch! Ich bin Stachowiak, ich pflege die Modelle in diesem Labor.",
          "Beim Aufräumen habe ich Terminal 3 auseinandergenommen und dabei seinen Bauplan"
              + " verlegt. Jetzt fehlt seine Frage.",
          "Ich kann es wieder aufbauen, mir fehlt nur der Bauplan. Eine Kopie liegt in der Truhe"
              + " unten rechts. Bringt sie mir!",
          "Die Truhe hat leider ein Zahlenschloss, und die Zahl habe ich vergessen. Ich weiß nur"
              + " noch, dass sie in meinem Graphmodell des Labors steckt. Der Rechner neben der"
              + " Truhe zeigt es.");

  private static final List<String> STACHOWIAK_REMINDER =
      List.of(
          "Die Kopie des Bauplans liegt in der Truhe unten rechts. Die Zahl für ihr Schloss"
              + " steckt in meinem Graphmodell auf dem Rechner daneben. Bringt mir den Bauplan, dann"
              + " baue ich Terminal 3 wieder auf.");

  private static final List<String> STACHOWIAK_REPAIR =
      List.of(
          "Ist das der Bauplan aus der Truhe? Genau den brauche ich!",
          "Abbilden, verkürzen, zweckmäßig machen ... fertig! Terminal 3 läuft wieder, und es hat"
              + " eine Frage für euch.");

  private static final List<String> STACHOWIAK_THANKS =
      List.of(
          "Danke! Ohne den Bauplan hätte ich Terminal 3 nicht wieder aufbauen können. Den"
              + " Modellierungskreislauf findet ihr auf dem Whiteboard.");

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

  private final boolean[] solved = new boolean[QUESTIONS.size()];
  private boolean ended;
  private boolean practised;
  private Entity stachowiak;
  private boolean metStachowiak;
  private boolean chestUnlocked;
  private boolean hasBlueprint;
  private boolean rebuilt;
  private boolean cycleDrawn;

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public FomoRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Modell");
  }

  /** Shows the intro once play begins. */
  static void intro() {
    story(INTRO_SPEAKER, INTRO, 0, () -> {}, Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  /**
   * Shows the pages of a speaker one after another and runs {@code then} after the last. Each page
   * is a dialog of its own, because the audience's "Weiter" confirms a whole dialog.
   */
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
    robot();
    station(getPoint("tutorial_panel"), KEYPAD, "tutorial-panel", this::tutorialPanel);
    station(getPoint("q1"), TERMINAL, "terminal-1", hero -> ask(0, hero));
    station(getPoint("q2"), TERMINAL, "terminal-2", hero -> ask(1, hero));
    stachowiak = stachowiak();
    station(getPoint("q4"), TERMINAL, "terminal-4", hero -> ask(3, hero));
    chest();
    station(getPoint("graph_model"), COMPUTER, "graph-model", hero -> showImage(GRAPH_MODEL, hero));
    whiteboard();
    station(getPoint("panel"), PANEL, "code-panel", this::codePanel);
  }

  @Override
  protected void onTick() {
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
      FomoRoom.finished();
    }
  }

  private Optional<DoorTile> door(String point) {
    return tileAt(getPoint(point)).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }

  private void station(Point position, SimpleIPath texture, String name, Consumer<Entity> use) {
    Entity station = new Entity(name);
    station.add(new PositionComponent(position));
    station.add(new DrawComponent(new Animation(texture)));
    station.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    station.add(
        new InteractionComponent(new Interaction((self, hero) -> use.accept(hero), 1.5f, true)));
    Game.add(station);
  }

  /**
   * Hangs the whiteboard on the wall. Its hitbox spans the two tiles the board covers, so the hero
   * can use it from in front of either; the board is drawn two tiles wide to match. It is not a
   * decoration of the level file, so the level editor leaves it alone.
   */
  private void whiteboard() {
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
  }

  // Empty until Stachowiak draws the modelling cycle, so it does not give away terminal 3.
  private void whiteboard(Entity hero) {
    if (!cycleDrawn) {
      DialogUtils.showTextPopup("Das Whiteboard ist noch leer.", "Whiteboard", hero.id());
      return;
    }
    showImage(MODELLING_CYCLE, hero);
  }

  private static void showImage(String image, Entity hero) {
    DialogFactory.show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, image)
            .build(),
        hero.id());
  }

  private Entity stachowiak() {
    return npc("q3", "character/wizard", this::talkToStachowiak);
  }

  private void robot() {
    npc("tutorial_npc", "character/roboter_cleaner", this::talkToRobot);
  }

  private Entity npc(String point, String sprite, Consumer<Entity> talk) {
    Entity npc = NPCFactory.createNPC(getPoint(point), sprite);
    // These sprites are one tile wide; the factory's hitbox fits two-tile sprites.
    npc.remove(CollideComponent.class);
    CollideComponent hitbox = new CollideComponent(Vector2.of(0.1f, 0.1f), Vector2.of(0.8f, 0.8f));
    hitbox.isSolid(true);
    npc.add(hitbox);
    npc.add(
        new InteractionComponent(new Interaction((self, hero) -> talk.accept(hero), 1.5f, true)));
    AudienceSystem.standOn(npc, getPoint(point));
    Game.add(npc);
    return npc;
  }

  /**
   * The robot practises a single-choice and a multiple-choice question, says what the audience
   * chose, and then gives the hint for the tutorial door. Afterwards it only repeats the hint.
   */
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

  /**
   * Asks a practice question. Every answer counts; {@code then} receives the chosen options as
   * text, such as "Kaffee und Tee".
   */
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

  private void talkToStachowiak(Entity hero) {
    if (rebuilt) {
      story(STACHOWIAK_SPEAKER, STACHOWIAK_THANKS, 0, () -> {}, hero.id());
    } else if (hasBlueprint) {
      story(STACHOWIAK_SPEAKER, STACHOWIAK_REPAIR, 0, () -> rebuild(hero), hero.id());
    } else if (metStachowiak) {
      story(STACHOWIAK_SPEAKER, STACHOWIAK_REMINDER, 0, () -> {}, hero.id());
    } else {
      metStachowiak = true;
      story(STACHOWIAK_SPEAKER, STACHOWIAK_FIRST_TALK, 0, () -> {}, hero.id());
    }
  }

  /**
   * Stachowiak steps onto a free tile next to q3, terminal 3 is back, and its question opens right
   * away. The hero stays where it is, next to the terminal.
   */
  private void rebuild(Entity hero) {
    rebuilt = true;
    Point terminal = getPoint("q3");
    Point heroTile = Game.tileAtEntity(hero).map(Tile::position).orElse(null);
    Stream.of(
            new Point(terminal.x() - 1, terminal.y()),
            new Point(terminal.x() + 1, terminal.y()),
            new Point(terminal.x(), terminal.y() + 1),
            new Point(terminal.x(), terminal.y() - 1))
        .filter(tile -> !tile.equals(heroTile))
        .filter(tile -> tileAt(tile).map(Tile::isAccessible).orElse(false))
        .findFirst()
        .ifPresent(aside -> AudienceSystem.standOn(stachowiak, aside));
    station(terminal, TERMINAL, "terminal-3", other -> ask(2, other));
    ask(2, hero);
  }

  private void chest() {
    Entity chest = new Entity("chest");
    chest.add(new PositionComponent(getPoint("chest")));
    chest.add(new DrawComponent(CHEST_TEXTURE, "closed"));
    chest.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    chest.add(
        new InteractionComponent(
            new Interaction((self, hero) -> openChest(self, hero), 1.5f, true)));
    Game.add(chest);
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
          "Die Truhe hat ein Zahlenschloss mit zwei Rädchen. Daneben ist eingeritzt: Kürzester"
              + " Weg vom Vorraum zum Serverraum, in Minuten.",
          CHEST_CODE.length(),
          code -> {
            if (code.equals(CHEST_CODE)) {
              chestUnlocked = true;
              openChest(chest, hero);
            } else {
              DialogUtils.showTextPopup(
                  "Das Schloss bleibt zu. Rechnet die Wege im Graphmodell auf dem Rechner noch"
                      + " einmal nach.",
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
    if (door("door").map(DoorTile::isOpen).orElse(false)) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    askCode(
        hero,
        "Tastenfeld",
        "Gebt den vierstelligen Türcode ein.",
        CODE.length(),
        code -> {
          if (code.equals(CODE)) {
            door("door").ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(
                "Der Code stimmt! Die Tür aus dem Modell springt auf.", "Richtig", hero.id());
          } else if (sorted(code).equals(sorted(CODE))) {
            DialogUtils.showTextPopup(
                "Die Ziffern stimmen, aber ihre Reihenfolge nicht.", "Fast", hero.id());
          } else {
            DialogUtils.showTextPopup("Der Code stimmt nicht.", "Falsch", hero.id());
          }
        });
  }

  private void tutorialPanel(Entity hero) {
    if (door("tutorial_door").map(DoorTile::isOpen).orElse(false)) {
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
    // Terminal 3 asks for the steps of modelling; once it is solved, the cycle may be shown.
    if (terminal == 2 && !cycleDrawn) {
      cycleDrawn = true;
      revealDigit(
          terminal,
          hero,
          () -> story(STACHOWIAK_SPEAKER, STACHOWIAK_WHITEBOARD, 0, () -> {}, hero.id()));
      return;
    }
    revealDigit(terminal, hero, () -> {});
  }

  /**
   * Rejects a wrong answer with the hint of the terminal. Once the audience has read it, the same
   * question opens again.
   */
  private void reject(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        "Diese Antwort passt noch nicht zum Modell. Versucht es noch einmal. Tipp: "
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

  /**
   * Shows the step of the modelling cycle a solved terminal stands for and its digit of the door
   * code, then runs {@code then}.
   */
  private static void revealDigit(int terminal, Entity hero, Runnable then) {
    int step = TERMINAL_STEPS[terminal];
    DialogUtils.showTextPopup(
        "Auf dem Bildschirm leuchtet der Schritt \""
            + STEPS.get(step)
            + "\" und daneben die Ziffer "
            + CODE.charAt(step)
            + ".",
        "Richtig",
        then::run,
        hero.id());
  }

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
