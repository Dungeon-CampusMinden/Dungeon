package rooms.automaton;

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
 * Server level of the automaton room. Four terminals ask about regular languages and grammars, and
 * a code panel next to the door takes the four-digit code. Each solved terminal reveals one digit
 * of the code. The automaton rejects a wrong answer like an input without a transition, then shows
 * a hint and the same question again. Walking through the open door ends the game.
 *
 * <p>The third terminal is broken at first. Kleene stands in its place and repairs it once the hero
 * brings the transition table from the chest. The table is a flag of the level, not an inventory
 * item, so all of it happens in dialogs the audience can vote on.
 *
 * <p>The level file places the terminals on its named points {@code q1} to {@code q4}, the chest on
 * {@code chest} and the code panel on {@code panel}; {@code door} is the door tile and {@code exit}
 * the tile behind it.
 */
public final class AutomatonRoomLevel extends DungeonLevel {
  private static final String CODE = "7031";
  private static final SimpleIPath TERMINAL =
      new SimpleIPath("objects/tech/Computer_station_1.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/keypad/off.png");
  private static final SimpleIPath CHEST_TEXTURE = new SimpleIPath("objects/treasurechest");

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
  private static final String KLEENE_SPEAKER =
      "[speaker img=rooms/automaton/kleene_portrait.png name=\"Kleene\"]";

  private static final List<String> KLEENE_FIRST_TALK =
      List.of(
          "Oh, Besuch! Ich bin Kleene, ich halte diesen Automaten in Schuss.",
          "Ich wollte gerade Zustand q3 polieren und habe dabei seine Übergangstabelle zerrissen."
              + " Jetzt ist er kaputt, und seine Frage ist weg.",
          "Ich kann ihn reparieren, mir fehlt nur eine neue Übergangstabelle. Eine Ersatzkopie"
              + " liegt in der Truhe da drüben. Bringt sie mir!");

  private static final List<String> KLEENE_REMINDER =
      List.of(
          "Die Ersatzkopie der Übergangstabelle liegt in der Truhe da drüben. Bringt sie mir,"
              + " dann repariere ich Zustand q3.");

  private static final List<String> KLEENE_REPAIR =
      List.of(
          "Ist das die Übergangstabelle aus der Truhe? Genau die brauche ich!",
          "Einsetzen, festschrauben ... fertig! Zustand q3 läuft wieder, und er hat eine Frage für"
              + " euch.");

  private static final List<String> KLEENE_THANKS =
      List.of("Danke für eure Hilfe! Ab jetzt poliere ich vorsichtiger.");

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

  private final boolean[] solved = new boolean[QUESTIONS.size()];
  private boolean ended;
  private Entity kleene;
  private boolean metKleene;
  private boolean hasTable;
  private boolean repaired;

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public AutomatonRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Automat");
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
    door().ifPresent(DoorTile::close);
    station(getPoint("q1"), TERMINAL, "terminal-1", hero -> ask(0, hero));
    station(getPoint("q2"), TERMINAL, "terminal-2", hero -> ask(1, hero));
    kleene = kleene();
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
      AutomatonRoom.finished();
    }
  }

  private Optional<DoorTile> door() {
    return tileAt(getPoint("door")).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
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

  private Entity kleene() {
    Entity npc = NPCFactory.createNPC(getPoint("q3"), "character/char03");
    npc.add(
        new InteractionComponent(new Interaction((self, hero) -> talkToKleene(hero), 1.5f, true)));
    AudienceSystem.standOn(npc, getPoint("q3"));
    Game.add(npc);
    return npc;
  }

  private void talkToKleene(Entity hero) {
    if (repaired) {
      story(KLEENE_SPEAKER, KLEENE_THANKS, 0, () -> {}, hero.id());
    } else if (hasTable) {
      story(KLEENE_SPEAKER, KLEENE_REPAIR, 0, () -> repair(hero), hero.id());
    } else if (metKleene) {
      story(KLEENE_SPEAKER, KLEENE_REMINDER, 0, () -> {}, hero.id());
    } else {
      metKleene = true;
      story(KLEENE_SPEAKER, KLEENE_FIRST_TALK, 0, () -> {}, hero.id());
    }
  }

  // Kleene steps onto a free tile next to q3, terminal 3 is back, and its question opens right
  // away. The hero stays where it is, next to the terminal.
  private void repair(Entity hero) {
    repaired = true;
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
        .ifPresent(aside -> AudienceSystem.standOn(kleene, aside));
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
    if (hasTable || repaired) {
      DialogUtils.showTextPopup("Die Truhe ist leer.", "Truhe", hero.id());
      return;
    }
    hasTable = true;
    chest.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("open_empty", null);
    DialogUtils.showTextPopup(
        "In der Truhe liegt eine Übergangstabelle. Ihr nehmt sie mit.", "Truhe", hero.id());
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
        payload -> {
          Set<String> chosen =
              switch (payload) {
                case DialogResponseMessage.StringValue(String value) -> Set.of(value);
                case DialogResponseMessage.StringList list ->
                    Set.copyOf(Arrays.asList(list.values()));
                default -> Set.of();
              };
          answered(terminal, chosen.equals(question.correct()), hero);
        },
        null,
        hero.id());
  }

  private void codePanel(Entity hero) {
    if (door().map(DoorTile::isOpen).orElse(false)) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.FREE_INPUT)
                .put(DialogContextKeys.TITLE, "Tastenfeld")
                .put(DialogContextKeys.QUESTION, "Gebt den vierstelligen Türcode ein.")
                .put(DialogContextKeys.INPUT_PLACEHOLDER, "Code")
                .put(DialogContextKeys.CONFIRM_LABEL, "Öffnen")
                .put(DialogContextKeys.CANCEL_LABEL, "Abbrechen")
                .put(AudienceSystem.CODE_DIGITS, CODE.length())
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
                  && value.strip().equals(CODE);
          if (correct) {
            door().ifPresent(DoorTile::open);
            DialogUtils.showTextPopup(
                "Wort akzeptiert! Die Tür zum Endzustand springt auf.", "Akzeptiert", hero.id());
          } else {
            DialogUtils.showTextPopup(
                "Dieses Wort akzeptiert der Automat nicht. Der Code stimmt nicht.",
                "Verworfen",
                hero.id());
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

  // Rejects a wrong answer with the hint of the terminal. Once the audience has read it, the same
  // question opens again.
  private void reject(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        "Für diese Antwort kennt der Automat keinen Übergang. Versucht es noch einmal. Tipp: "
            + QUESTIONS.get(terminal).hint(),
        "Eingabe verworfen",
        () -> ask(terminal, hero),
        hero.id());
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
}
