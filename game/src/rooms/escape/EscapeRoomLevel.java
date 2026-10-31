package rooms.escape;

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
import feature.prefabs.types.DecorationInteractionPrefab;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Server level of an escape room in audience mode. Each terminal asks one question, and each solved
 * terminal reveals one digit of the door code. A code panel next to the door takes the code, and
 * walking through the open door ends the game. After a wrong answer the audience gets the hint of
 * the question and the same question again.
 *
 * <p>One terminal is taken apart at first. A helper stands in its place and rebuilds it once the
 * hero brings an item from the chest. The item is a flag of the level, not an inventory item, so
 * all of it happens in dialogs the audience can vote on.
 *
 * <p>A room can add a tutorial, a lock on the chest, a computer that shows an image, and a
 * whiteboard. In the tutorial a robot practises a single-choice and a multiple-choice question and
 * then tells the audience that the code of the tutorial door is the last three digits of the phone
 * number. The level file places a printer as a {@code deco-interaction} prefab, and a {@code
 * show-image} prefab shows the number when the hero uses it. The whiteboard stays empty until the
 * audience solves the rebuilt terminal; then the helper writes on it.
 *
 * <p>Subclasses build their objects in {@link #setUp()} and describe a revealed digit in {@link
 * #revealText(int)}. The audience picks targets from a list unless {@link #offersTargets()} says
 * no; then it votes on single steps. A room offers each object once the story reaches it, finished
 * objects leave the list, and once the door is open, the exit joins it.
 *
 * <p>The level file places the terminals on its named points {@code q1}, {@code q2} and so on, the
 * chest on {@code chest} and the code panel on {@code panel}; {@code door} is the door tile and
 * {@code exit} the tile behind it. The robot stands on {@code tutorial_npc}, the tutorial door is
 * {@code tutorial_door} and its code panel {@code tutorial_panel}. The whiteboard hangs on {@code
 * whiteboard}.
 */
public abstract class EscapeRoomLevel extends DungeonLevel {
  private static final SimpleIPath CHEST_TEXTURE = new SimpleIPath("objects/treasurechest");
  private static final SimpleIPath KEYPAD = new SimpleIPath("objects/keypad/off.png");
  private static final SimpleIPath COMPUTER =
      new SimpleIPath("objects/tech/Computer_station_1.png");
  // Number words for counts and code lengths, such as "vier" for "vierstelligen".
  private static final List<String> NUMBERS =
      List.of("null", "ein", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun");

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

  private final Terminals terminals;
  private final boolean[] solved;
  private boolean ended;

  // Set by tutorial(); null in a room without a tutorial.
  private String tutorialCode;
  private boolean practised;
  // Whether the printer of the level file is a target yet; the level spawns it on its own.
  private boolean printerOffered;

  // Set by terminals().
  private Helper helper;
  private Entity helperNpc;
  private ChestLock chestLock;
  private boolean metHelper;
  private boolean chestUnlocked;
  private boolean hasItem;
  private boolean rebuilt;

  // Set by doorLock().
  private DoorLock doorLock;

  // Set by whiteboard(); null in a room without a whiteboard.
  private List<String> boardPages;
  private Consumer<Entity> boardContent;
  private boolean boardWritten;

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   * @param name name of the level
   * @param terminals what the terminals ask and the code they reveal
   */
  protected EscapeRoomLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      String name,
      Terminals terminals) {
    super(layout, designLabel, namedPoints, name);
    this.terminals = terminals;
    solved = new boolean[terminals.questions().size()];
  }

  /**
   * Builds the objects of the room. The level calls it on its first tick, after it closed the door.
   * Use {@link #terminals}, {@link #doorLock} and the optional parts.
   */
  protected abstract void setUp();

  /**
   * Describes what a solved terminal shows, including its digit of the door code.
   *
   * @param terminal index of the terminal, starting at 0
   * @return text of the popup after the right answer
   */
  protected abstract String revealText(int terminal);

  /**
   * Whether the audience picks targets from a list. Otherwise it votes on single steps.
   *
   * @return true by default
   */
  protected boolean offersTargets() {
    return true;
  }

  /**
   * Shows the pages of the intro to everyone once play begins.
   *
   * @param speaker speaker tag with portrait and name
   * @param pages one dialog per page
   */
  protected static void intro(String speaker, List<String> pages) {
    story(speaker, pages, 0, () -> {}, Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  /**
   * Intro of a room with targets, a tutorial, and digits whose order the audience finds out on its
   * own. After the greeting it explains joining, voting, questions and codes.
   *
   * @param greeting first page, in which the room introduces itself
   * @param terminals number of terminals
   * @return the pages of the intro
   */
  protected static List<String> introPages(String greeting, int terminals) {
    return List.of(
        greeting,
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
        "Codes tippt ihr auf dem Ziffernblock ein. Meine "
            + NUMBERS.get(terminals)
            + " Terminals im Labor verraten je eine Ziffer des Türcodes, wenn ihr ihre Frage"
            + " richtig beantwortet.",
        "In welcher Reihenfolge die Ziffern zum Türcode gehören, müsst ihr selbst"
            + " herausfinden. Nach einer falschen Antwort bekommt ihr einen Tipp und versucht"
            + " es noch einmal.",
        "Ihr startet im Vorraum. Sprecht zuerst mit dem Roboter. Er übt mit euch und verrät,"
            + " wie ihr ins Labor kommt.");
  }

  /**
   * Shows the pages of a speaker one after another and runs the callback after the last one. Each
   * page is a dialog of its own, because the audience's "Weiter" confirms a whole dialog.
   *
   * @param speaker speaker tag with portrait and name
   * @param pages one dialog per page
   * @param then runs after the last page
   * @param targetIds clients that see the dialogs
   */
  protected static void story(String speaker, List<String> pages, Runnable then, int... targetIds) {
    story(speaker, pages, 0, then, targetIds);
  }

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

  /**
   * Shows an image to the audience.
   *
   * @param image path of the image
   * @param hero the hero that looks at it
   */
  protected static void showImage(String image, Entity hero) {
    DialogFactory.show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, image)
            .build(),
        hero.id());
  }

  @Override
  protected final void onFirstTick() {
    door("door").ifPresent(DoorTile::close);
    setUp();
  }

  @Override
  protected final void onTick() {
    if (tutorialCode != null && !printerOffered) {
      prefabs(DecorationInteractionPrefab.class).stream()
          .findFirst()
          .flatMap(DecorationInteractionPrefab::decoEntity)
          .ifPresent(
              printer -> {
                offer(printer, "Drucker", () -> practised && inTutorial());
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
      EscapeRoom.finished();
    }
  }

  /**
   * Places a terminal on {@code q1}, {@code q2} and so on for each question, except the one the
   * helper took apart. The helper stands there instead, and the chest holds the item it needs.
   *
   * @param helper the helper
   */
  protected final void terminals(Helper helper) {
    terminals(helper, null);
  }

  /**
   * Places the terminals, the helper and the chest like {@link #terminals(Helper)}, and locks the
   * chest. The chest joins the targets once the audience has talked to the helper.
   *
   * @param helper the helper
   * @param lock the lock of the chest
   */
  protected final void terminals(Helper helper, ChestLock lock) {
    this.helper = helper;
    chestLock = lock;
    for (int terminal = 0; terminal < solved.length; terminal++) {
      if (terminal == helper.terminal()) helperNpc = offer(helperNpc(), helper.name(), () -> true);
      else terminal(terminal);
    }
    offer(chest(), "Truhe", () -> metHelper && !hasItem && !rebuilt);
  }

  /**
   * Places the code panel of the door. Once the door is open, the exit behind it joins the targets.
   *
   * @param lock look of the panel and its answers
   */
  protected final void doorLock(DoorLock lock) {
    doorLock = lock;
    offer(
        station(getPoint("panel"), lock.texture(), "code-panel", this::codePanel),
        "Türschloss",
        () -> !isOpen("door"));
    if (offersTargets())
      Game.add(AudienceTarget.tile(getPoint("exit"), "Ausgang", () -> isOpen("door")));
  }

  /**
   * Sets up the tutorial room: closes its door and places the robot and the keypad. The keypad and
   * the printer join the targets after the practice, and all three leave once the door is open.
   *
   * @param code code of the tutorial door
   */
  protected final void tutorial(String code) {
    tutorialCode = code;
    door("tutorial_door").ifPresent(DoorTile::close);
    offer(
        npc("tutorial_npc", "character/roboter_cleaner", true, this::talkToRobot),
        "Robo",
        this::inTutorial);
    offer(
        station(getPoint("tutorial_panel"), KEYPAD, "tutorial-panel", this::tutorialPanel),
        "Tastenfeld",
        () -> practised && inTutorial());
  }

  /**
   * Places a computer that shows an image. It joins the targets once the audience has talked to the
   * helper.
   *
   * @param point named point of the computer; also the name of its entity
   * @param image path of the image
   */
  protected final void computer(String point, String image) {
    offer(
        station(getPoint(point), COMPUTER, point.replace('_', '-'), hero -> showImage(image, hero)),
        "Rechner",
        () -> metHelper);
  }

  /**
   * Hangs the whiteboard on the wall. Its hitbox spans the two tiles the board covers, so the hero
   * can use it from in front of either; the board is drawn two tiles wide to match. It is not a
   * decoration of the level file, so the level editor leaves it alone.
   *
   * <p>The board stays empty, so it does not give away the rebuilt terminal. Once the audience
   * solves that terminal, the helper says the given pages and the board joins the targets.
   *
   * @param helperPages what the helper says when it writes on the board
   * @param content shows the board to the hero once it is written on
   */
  protected final void whiteboard(List<String> helperPages, Consumer<Entity> content) {
    boardPages = helperPages;
    boardContent = content;
    Deco deco = Deco.LabWhiteboard;
    // The board image is 50 by 37 pixels and its height is the scale, so this makes it 2 tiles
    // wide.
    AnimationConfig look = new AnimationConfig().scaleX(2f * 37 / 50);
    Entity board =
        DecoFactory.createDeco(
            getPoint("whiteboard"), deco, deco.defaultDepth(), look, new Rectangle(2f, 1f, 0, 0));
    board.remove(DecoComponent.class);
    board.add(
        new InteractionComponent(new Interaction((self, hero) -> readBoard(hero), 1.5f, true)));
    Game.add(board);
    offer(board, "Whiteboard", () -> boardWritten);
  }

  private void readBoard(Entity hero) {
    if (!boardWritten) {
      DialogUtils.showTextPopup("Das Whiteboard ist noch leer.", "Whiteboard", hero.id());
      return;
    }
    boardContent.accept(hero);
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
  private Entity offer(Entity entity, String label, BooleanSupplier available) {
    if (offersTargets()) entity.add(new AudienceTarget(label, available));
    return entity;
  }

  private void terminal(int terminal) {
    offer(
        station(
            getPoint("q" + (terminal + 1)),
            terminals.texture(),
            "terminal-" + (terminal + 1),
            hero -> ask(terminal, hero)),
        "Terminal " + (terminal + 1),
        () -> true);
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

  private Entity helperNpc() {
    return npc(
        "q" + (helper.terminal() + 1), helper.sprite(), helper.oneTileWide(), this::talkToHelper);
  }

  private Entity npc(String point, String sprite, boolean oneTileWide, Consumer<Entity> talk) {
    Entity npc = NPCFactory.createNPC(getPoint(point), sprite);
    if (oneTileWide) {
      // The factory's hitbox fits two-tile sprites.
      npc.remove(CollideComponent.class);
      CollideComponent hitbox =
          new CollideComponent(Vector2.of(0.1f, 0.1f), Vector2.of(0.8f, 0.8f));
      hitbox.isSolid(true);
      npc.add(hitbox);
    }
    npc.add(
        new InteractionComponent(new Interaction((self, hero) -> talk.accept(hero), 1.5f, true)));
    AudienceSystem.standOn(npc, getPoint(point));
    Game.add(npc);
    return npc;
  }

  // The robot practises a single-choice and a multiple-choice question, says what the audience
  // chose, and then gives the hint for the tutorial door. Afterwards it only repeats the hint.
  private void talkToRobot(Entity hero) {
    if (practised) {
      story(ROBOT_SPEAKER, List.of(DOOR_HINT), () -> {}, hero.id());
      return;
    }
    story(
        ROBOT_SPEAKER,
        ROBOT_SINGLE_INTRO,
        () -> practise(SINGLE_PRACTICE, hero, chosen -> afterSinglePractice(hero, chosen)),
        hero.id());
  }

  private void afterSinglePractice(Entity hero, String chosen) {
    story(
        ROBOT_SPEAKER,
        List.of("Ihr habt " + chosen + " gewählt. Gut gemacht!", MULTIPLE_INTRO),
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

  private void talkToHelper(Entity hero) {
    if (rebuilt) {
      story(helper.speaker(), helper.thanks(), () -> {}, hero.id());
    } else if (hasItem) {
      story(helper.speaker(), helper.repair(), () -> rebuild(hero), hero.id());
    } else if (metHelper) {
      story(helper.speaker(), helper.reminder(), () -> {}, hero.id());
    } else {
      metHelper = true;
      story(helper.speaker(), helper.firstTalk(), () -> {}, hero.id());
    }
  }

  // The helper steps onto a free tile next to its terminal, the terminal is back, and its question
  // opens right away. The hero stays where it is, next to the terminal.
  private void rebuild(Entity hero) {
    rebuilt = true;
    Point terminal = getPoint("q" + (helper.terminal() + 1));
    Point heroTile = Game.tileAtEntity(hero).map(Tile::position).orElse(null);
    Stream.of(
            new Point(terminal.x() - 1, terminal.y()),
            new Point(terminal.x() + 1, terminal.y()),
            new Point(terminal.x(), terminal.y() + 1),
            new Point(terminal.x(), terminal.y() - 1))
        .filter(tile -> !tile.equals(heroTile))
        .filter(tile -> tileAt(tile).map(Tile::isAccessible).orElse(false))
        .findFirst()
        .ifPresent(aside -> AudienceSystem.standOn(helperNpc, aside));
    terminal(helper.terminal());
    ask(helper.terminal(), hero);
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
    if (hasItem || rebuilt) {
      DialogUtils.showTextPopup("Die Truhe ist leer.", "Truhe", hero.id());
      return;
    }
    if (chestLock != null && !chestUnlocked) {
      askCode(
          hero,
          "Zahlenschloss",
          chestLock.question(),
          chestLock.code().length(),
          code -> {
            if (code.equals(chestLock.code())) {
              chestUnlocked = true;
              openChest(chest, hero);
            } else {
              DialogUtils.showTextPopup(chestLock.wrong(), "Falsch", hero.id());
            }
          });
      return;
    }
    hasItem = true;
    chest.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("open_empty", null);
    DialogUtils.showTextPopup(helper.found(), "Truhe", hero.id());
  }

  private void ask(int terminal, Entity hero) {
    if (solved[terminal]) {
      reveal(terminal, hero, () -> {});
      return;
    }
    Question question = terminals.questions().get(terminal);
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
    String code = terminals.code();
    askCode(
        hero,
        "Tastenfeld",
        "Gebt den " + NUMBERS.get(code.length()) + "stelligen Türcode ein.",
        code.length(),
        entered -> {
          if (entered.equals(code)) {
            door("door").ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(doorLock.opened(), doorLock.openedTitle(), hero.id());
          } else if (sorted(entered).equals(sorted(code))) {
            DialogUtils.showTextPopup(
                "Die Ziffern stimmen, aber ihre Reihenfolge nicht.", "Fast", hero.id());
          } else {
            DialogUtils.showTextPopup(doorLock.wrong(), doorLock.wrongTitle(), hero.id());
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
        "Gebt den " + NUMBERS.get(tutorialCode.length()) + "stelligen Code ein.",
        tutorialCode.length(),
        code -> {
          if (code.equals(tutorialCode)) {
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
    // The rebuilt terminal asks for what the whiteboard shows. Once the audience solves it, the
    // helper writes it on the board.
    if (boardPages != null && terminal == helper.terminal() && !boardWritten) {
      boardWritten = true;
      reveal(terminal, hero, () -> story(helper.speaker(), boardPages, () -> {}, hero.id()));
      return;
    }
    reveal(terminal, hero, () -> {});
  }

  // Rejects a wrong answer with the hint of the terminal. Once the audience has read it, the same
  // question opens again.
  private void reject(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        terminals.rejectText()
            + " Versucht es noch einmal. Tipp: "
            + terminals.questions().get(terminal).hint(),
        terminals.rejectTitle(),
        () -> ask(terminal, hero),
        hero.id());
  }

  private void reveal(int terminal, Entity hero, Runnable then) {
    DialogUtils.showTextPopup(revealText(terminal), "Richtig", then::run, hero.id());
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
   * Question of a terminal.
   *
   * @param text question text
   * @param options answer options; their values identify the answers
   * @param multiple whether several options may be chosen
   * @param correct values of the correct options; the chosen set must match exactly
   * @param hint hint shown after a wrong answer
   */
  public record Question(
      String text,
      List<ChoiceOption> options,
      boolean multiple,
      Set<String> correct,
      String hint) {}

  /**
   * What the terminals of a room ask and the code they reveal.
   *
   * @param texture look of a terminal
   * @param questions one question per terminal, starting with terminal 1
   * @param code the door code; each terminal reveals one digit
   * @param rejectTitle title of the popup after a wrong answer
   * @param rejectText first sentence of that popup; the request to try again and the hint follow
   */
  public record Terminals(
      SimpleIPath texture,
      List<Question> questions,
      String code,
      String rejectTitle,
      String rejectText) {}

  /**
   * The helper who took a terminal apart and rebuilds it with the item from the chest.
   *
   * @param name name in the list of targets
   * @param speaker speaker tag of the dialogs with portrait and name
   * @param sprite sprite of the NPC
   * @param oneTileWide whether the sprite is one tile wide; the hitbox of the NPC factory fits
   *     two-tile sprites
   * @param terminal index of the terminal the helper took apart, starting at 0
   * @param found popup when the audience takes the item from the chest
   * @param firstTalk what the helper says the first time
   * @param reminder what the helper says until the audience brings the item
   * @param repair what the helper says before rebuilding the terminal
   * @param thanks what the helper says afterwards
   */
  public record Helper(
      String name,
      String speaker,
      String sprite,
      boolean oneTileWide,
      int terminal,
      String found,
      List<String> firstTalk,
      List<String> reminder,
      List<String> repair,
      List<String> thanks) {}

  /**
   * Number lock of the chest.
   *
   * @param code the code
   * @param question what the lock says about its code
   * @param wrong popup after a wrong code
   */
  public record ChestLock(String code, String question, String wrong) {}

  /**
   * Code panel of the door.
   *
   * @param texture look of the panel
   * @param openedTitle title of the popup after the right code
   * @param opened popup after the right code
   * @param wrongTitle title of the popup after a wrong code
   * @param wrong popup after a wrong code
   */
  public record DoorLock(
      SimpleIPath texture, String openedTitle, String opened, String wrongTitle, String wrong) {}

  /**
   * Practice question of the robot; every answer counts.
   *
   * @param text question text
   * @param options answer options
   * @param multiple whether several options may be chosen
   */
  private record ChoicePractice(String text, List<ChoiceOption> options, boolean multiple) {}
}
