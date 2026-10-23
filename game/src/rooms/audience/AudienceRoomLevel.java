package rooms.audience;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.Coordinate;
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
import java.util.Set;
import java.util.function.Consumer;

/**
 * Server level of the audience room: three terminals with placeholder questions and a code panel
 * next to the door. Each solved terminal reveals one digit of the door code; the code keeps its
 * leading zero. Walking through the open door ends the game.
 */
public final class AudienceRoomLevel extends DungeonLevel {
  private static final String CODE = "047";
  private static final Coordinate DOOR = new Coordinate(6, 6);
  private static final Coordinate EXIT = new Coordinate(6, 7);
  private static final SimpleIPath TERMINAL =
      new SimpleIPath("objects/tech/Computer_station_1.png");
  private static final SimpleIPath PANEL = new SimpleIPath("objects/keypad/off.png");

  private final boolean[] solved = new boolean[3];
  private boolean ended;

  /**
   * Creates the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   */
  public AudienceRoomLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Publikum");
  }

  /** Shows the intro once play begins. */
  static void intro() {
    DialogFactory.showTextDialog(
        "Ihr seid im Rechenzentrum eingeschlossen. Drei Terminals verraten je eine Ziffer des"
            + " Türcodes. Steuert die Figur mit eurem Gerät: Pfeile bewegen sie ein Feld weit,"
            + " „Benutzen“ bedient, was direkt vor ihr steht.",
        "Raus aus dem Rechenzentrum",
        () -> {},
        "Los",
        Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  @Override
  protected void onFirstTick() {
    tileAt(DOOR)
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast)
        .ifPresent(DoorTile::close);
    station(
        new Point(2, 5),
        TERMINAL,
        "terminal-1",
        hero ->
            choice(
                0,
                "Welche Datenstruktur arbeitet nach dem LIFO-Prinzip?",
                List.of(
                    ChoiceOption.of("Queue", "queue"),
                    ChoiceOption.of("Stack", "stack"),
                    ChoiceOption.of("Heap", "heap"),
                    ChoiceOption.of("Hashtabelle", "hashtable")),
                false,
                Set.of("stack"),
                hero));
    station(
        new Point(10, 5),
        TERMINAL,
        "terminal-2",
        hero ->
            choice(
                1,
                "Welche Sortierverfahren sind stabil?",
                List.of(
                    ChoiceOption.of("Mergesort", "mergesort"),
                    ChoiceOption.of("Quicksort", "quicksort"),
                    ChoiceOption.of("Insertionsort", "insertionsort"),
                    ChoiceOption.of("Heapsort", "heapsort")),
                true,
                Set.of("mergesort", "insertionsort"),
                hero));
    station(new Point(10, 1), TERMINAL, "terminal-3", this::freeText);
    station(new Point(7, 5), PANEL, "code-panel", this::codePanel);
  }

  @Override
  protected void onTick() {
    if (ended) return;
    boolean atExit =
        Game.allPlayers()
            .anyMatch(
                hero ->
                    Game.tileAtEntity(hero)
                        .map(tile -> tile.coordinate().equals(EXIT))
                        .orElse(false));
    if (atExit) {
      ended = true;
      AudienceRoom.finished();
    }
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

  private void choice(
      int terminal,
      String question,
      List<ChoiceOption> options,
      boolean multiple,
      Set<String> correct,
      Entity hero) {
    if (solved[terminal]) {
      revealDigit(terminal, hero);
      return;
    }
    DialogFactory.showMultipleChoiceDialog(
        question,
        "Terminal " + (terminal + 1),
        options,
        multiple,
        false,
        payload -> {
          Set<String> chosen =
              switch (payload) {
                case DialogResponseMessage.StringValue(String value) -> Set.of(value);
                case DialogResponseMessage.StringList list ->
                    Set.copyOf(Arrays.asList(list.values()));
                default -> Set.of();
              };
          answered(terminal, chosen.equals(correct), hero);
        },
        null,
        hero.id());
  }

  private void freeText(Entity hero) {
    DialogFactory.showInputDialog(
        "Welche Laufzeit hat die binäre Suche im schlechtesten Fall? Schreibt sie als O(...).",
        "Terminal 3",
        "",
        "O(...)",
        "Senden",
        "Abbrechen",
        payload -> {
          // Audience mode sends the answer without whitespace and in lower case.
          boolean correct =
              payload instanceof DialogResponseMessage.StringValue(String value)
                  && Set.of("o(logn)", "o(log(n))", "logn", "log(n)").contains(value);
          answered(2, correct, hero);
        },
        () -> {},
        hero.id());
  }

  private void codePanel(Entity hero) {
    if (tileAt(DOOR)
        .filter(DoorTile.class::isInstance)
        .map(t -> ((DoorTile) t).isOpen())
        .orElse(false)) {
      DialogUtils.showTextPopup("Die Tür ist schon offen.", "Tastenfeld", hero.id());
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.FREE_INPUT)
                .put(DialogContextKeys.TITLE, "Tastenfeld")
                .put(DialogContextKeys.QUESTION, "Gebt den dreistelligen Türcode ein.")
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
            tileAt(DOOR)
                .filter(DoorTile.class::isInstance)
                .map(DoorTile.class::cast)
                .ifPresent(DoorTile::open);
            DialogUtils.showTextPopup("Die Tür springt auf.", "Richtig", hero.id());
          } else {
            DialogUtils.showTextPopup("Der Code stimmt nicht.", "Falsch", hero.id());
          }
        });
    ui.registerCallback(DialogContextKeys.ON_CANCEL, payload -> UIUtils.closeDialog(ui));
  }

  private void answered(int terminal, boolean correct, Entity hero) {
    if (!correct) {
      DialogUtils.showTextPopup("Leider falsch. Versucht es noch einmal.", "Falsch", hero.id());
      return;
    }
    solved[terminal] = true;
    revealDigit(terminal, hero);
  }

  private static void revealDigit(int terminal, Entity hero) {
    DialogUtils.showTextPopup(
        "Ziffer " + (terminal + 1) + " des Türcodes: " + CODE.charAt(terminal),
        "Richtig",
        hero.id());
  }
}
