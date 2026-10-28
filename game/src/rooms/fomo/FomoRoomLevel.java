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
import engine.utils.Vector2;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.audience.AudienceSystem;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import feature.entities.NPCFactory;
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
 * Server level of the FOMO room. Four terminals ask about choosing, building and examining models,
 * and a code panel next to the door takes the four-digit code. Each solved terminal reveals one
 * digit of the code. After a wrong answer the audience gets a hint and the same question again.
 * Walking through the open door ends the game.
 *
 * <p>The hero starts in a tutorial room. A robot there asks a practice single-choice and a practice
 * multiple-choice question, then tells the audience that the code of the tutorial door is the last
 * three digits of the phone number. The number is on the business card the level file places as a
 * {@code deco-image} prefab.
 *
 * <p>The third terminal is taken apart at first. Stachowiak stands in its place and rebuilds it
 * once the hero brings the blueprint from the chest. The blueprint is a flag of the level, not an
 * inventory item, so all of it happens in dialogs the audience can vote on.
 *
 * <p>The level file places the terminals on its named points {@code q1} to {@code q4}, the chest on
 * {@code chest} and the code panel on {@code panel}; {@code door} is the door tile and {@code exit}
 * the tile behind it. The robot stands on {@code tutorial_npc}, the tutorial door is {@code
 * tutorial_door} and its code panel {@code tutorial_panel}.
 */
public final class FomoRoomLevel extends DungeonLevel {
  private static final String CODE = "4826";
  // The last three digits of the phone number on the business card in the tutorial room.
  private static final String TUTORIAL_CODE = "279";
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
              + " eine Ziffer des Türcodes, wenn ihr ihre Frage richtig beantwortet. Nach einer"
              + " falschen Antwort bekommt ihr einen Tipp und versucht es noch einmal.",
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
              + " da drüben. Bringt sie mir!");

  private static final List<String> STACHOWIAK_REMINDER =
      List.of(
          "Die Kopie des Bauplans liegt in der Truhe da drüben. Bringt sie mir, dann baue ich"
              + " Terminal 3 wieder auf.");

  private static final List<String> STACHOWIAK_REPAIR =
      List.of(
          "Ist das der Bauplan aus der Truhe? Genau den brauche ich!",
          "Abbilden, verkürzen, zweckmäßig machen ... fertig! Terminal 3 läuft wieder, und es hat"
              + " eine Frage für euch.");

  private static final List<String> STACHOWIAK_THANKS =
      List.of("Danke! Ohne den Bauplan hätte ich Terminal 3 nicht wieder aufbauen können.");

  /** One question per terminal, in the order of the code digits. */
  private static final List<Question> QUESTIONS =
      List.of(
          new Question(
              "Was gehört zur Auswahl eines Modells?",
              List.of(
                  ChoiceOption.of("Eine passende Modellart wählen.", "a"),
                  ChoiceOption.of("Die Fragestellung berücksichtigen.", "b"),
                  ChoiceOption.of("Prüfen, welche Eigenschaften des Systems wichtig sind.", "c"),
                  ChoiceOption.of(
                      "Überprüfen, ob es eine graphische Darstellung des Modells gibt.", "d")),
              true,
              Set.of("a", "b", "c"),
              "Ein Modell wählt man für einen Zweck. Hilft die Antwort diesem Zweck, oder geht es"
                  + " nur um das Aussehen?"),
          new Question(
              "Was kann beim Erstellen eines Modells nötig sein?",
              List.of(
                  ChoiceOption.of("Wichtige Merkmale auswählen.", "a"),
                  ChoiceOption.of("Vereinfachungen und Annahmen festlegen.", "b"),
                  ChoiceOption.of("Das Modell passend zur Fragestellung gestalten.", "c"),
                  ChoiceOption.of(
                      "Alle Eigenschaften des zugrundeliegenden Systems vollständig übernehmen.",
                      "d")),
              true,
              Set.of("a", "b", "c"),
              "Ein Modell ist immer eine Vereinfachung. Muss es alles vom System enthalten?"),
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
              "Was gehört zur Modellierung?",
              List.of(
                  ChoiceOption.of("Ein reales oder gedachtes System untersuchen", "a"),
                  ChoiceOption.of("Wichtige Eigenschaften auswählen", "b"),
                  ChoiceOption.of("Das Modell mit dem System vergleichen", "c"),
                  ChoiceOption.of("Möglichst viele Details des Systems übernehmen", "d")),
              true,
              Set.of("a", "b", "c"),
              "Ein gutes Modell lässt bewusst etwas weg, und man vergleicht es mit seinem"
                  + " Original."));

  private final boolean[] solved = new boolean[QUESTIONS.size()];
  private boolean ended;
  private boolean practised;
  private Entity stachowiak;
  private boolean metStachowiak;
  private boolean hasBlueprint;
  private boolean rebuilt;

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
    hasBlueprint = true;
    chest.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("open_empty", null);
    DialogUtils.showTextPopup(
        "In der Truhe liegt ein Bauplan. Ihr nehmt ihn mit.", "Truhe", hero.id());
  }

  private void ask(int terminal, Entity hero) {
    if (solved[terminal]) {
      revealDigit(terminal, hero);
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
    codePanel(
        hero,
        "door",
        CODE,
        "Gebt den vierstelligen Türcode ein.",
        "Der Code stimmt! Die Tür aus dem Modell springt auf.");
  }

  private void tutorialPanel(Entity hero) {
    codePanel(
        hero,
        "tutorial_door",
        TUTORIAL_CODE,
        "Gebt den dreistelligen Code ein.",
        "Der Code stimmt! Die Tür ins Labor geht auf.");
  }

  /**
   * Opens the code entry for a door.
   *
   * @param hero the hero using the panel
   * @param door named point of the door tile
   * @param code the code that opens the door
   * @param question what the dialog asks for
   * @param opened message after the right code
   */
  private void codePanel(Entity hero, String door, String code, String question, String opened) {
    if (door(door).map(DoorTile::isOpen).orElse(false)) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.FREE_INPUT)
                .put(DialogContextKeys.TITLE, "Tastenfeld")
                .put(DialogContextKeys.QUESTION, question)
                .put(DialogContextKeys.INPUT_PLACEHOLDER, "Code")
                .put(DialogContextKeys.CONFIRM_LABEL, "Öffnen")
                .put(DialogContextKeys.CANCEL_LABEL, "Abbrechen")
                .put(AudienceSystem.CODE_DIGITS, code.length())
                .build(),
            true,
            true,
            hero.id());
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        payload -> {
          UIUtils.closeDialog(ui);
          boolean correct =
              payload instanceof DialogResponseMessage.StringValue(String value)
                  && value.strip().equals(code);
          if (correct) {
            door(door).ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(opened, "Richtig", hero.id());
          } else {
            DialogUtils.showTextPopup("Der Code stimmt nicht.", "Falsch", hero.id());
          }
        });
    ui.registerCallback(DialogContextKeys.ON_CANCEL, payload -> UIUtils.closeDialog(ui));
  }

  private void answered(int terminal, boolean correct, Entity hero) {
    if (!correct) {
      reject(terminal, hero);
      return;
    }
    solved[terminal] = true;
    revealDigit(terminal, hero);
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

  private static void revealDigit(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        "Ziffer " + (terminal + 1) + " des Türcodes: " + CODE.charAt(terminal),
        "Richtig",
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
