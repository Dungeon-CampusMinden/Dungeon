package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.petrinet.PetriNetSystem;
import feature.petrinet.PlaceComponent;
import feature.petrinet.TransitionComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.programming.modules.loops.LoopExecution;
import rooms.programming.modules.loops.LoopPuzzle;
import tools.jackson.databind.json.JsonMapper;

/** Room-owned, requested Petri-net transitions release hints without publishing future answers. */
public final class ProgrammingHelp {
  public static final String ID = "programming.help";
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static State received;
  private final ProgrammingGolemRuntime runtime;
  private final Map<String, Progress> puzzles = new LinkedHashMap<>();
  private final List<ReleasedHint> history = new ArrayList<>();

  /** A previously requested hint remains readable after its puzzle is complete. */
  public record ReleasedHint(String puzzleId, String title, int step, String text) {}

  /**
   * Immutable snapshot; eligibility is room-wide, with player permission checked on each action.
   */
  public record State(
      String puzzleId,
      String title,
      String goal,
      List<ReleasedHint> history,
      int level,
      boolean canRequest,
      boolean canSolve,
      String status) {
    public State {
      history = List.copyOf(history);
    }
  }

  private static final class Progress {
    final PetriNetSystem net = new PetriNetSystem();
    final PlaceComponent request = new PlaceComponent();
    final List<PlaceComponent> steps = new ArrayList<>();

    Progress() {
      for (int i = 0; i <= 3; i++) steps.add(new PlaceComponent());
      steps.getFirst().produce();
      for (int i = 0; i < 3; i++) {
        var transition = new TransitionComponent();
        net.addInputArc(transition, steps.get(i));
        net.addInputArc(transition, request);
        net.addOutputArc(transition, steps.get(i + 1));
      }
    }

    int level() {
      for (int i = 3; i >= 0; i--) if (steps.get(i).tokenCount() > 0) return i;
      throw new IllegalStateException("Missing help progression token");
    }

    void next() {
      request.produce();
      net.execute();
    }
  }

  ProgrammingHelp(ProgrammingGolemRuntime runtime) {
    this.runtime = runtime;
    for (String id :
        List.of(
            "vessels",
            "essences",
            "cellar-0",
            "cellar-1",
            "cellar-2",
            "cellar-3",
            "cellar-4",
            "methods")) puzzles.put(id, new Progress());
  }

  State snapshot() {
    String id = runtime.helpPuzzle();
    int level = puzzles.get(id).level();
    boolean ready = runtime.helpReady();
    String status = runtime.helpStatus();
    if (id.startsWith("cellar-") && level == 3 && ready) {
      int checkpoint = Integer.parseInt(id.substring(7));
      String family = LoopPuzzle.challenges().get(checkpoint);
      if (runtime.terminalState().collectedRunes().stream()
          .noneMatch(rune -> rune.startsWith(family + "-")))
        status +=
            " Noch keine Rune für diesen Abschnitt gesammelt. "
                + switch (checkpoint) {
                  case 0 ->
                      "Eine passende orange for-Rune liegt im Runenarchiv auf der unteren Tischreihe ganz links.";
                  case 1 ->
                      "Eine passende blaue while-Rune liegt im Runenarchiv auf der unteren Tischreihe, an zweiter Stelle von links.";
                  case 2 ->
                      "Eine passende blaue while-Rune liegt im Runenarchiv auf dem mittleren Tisch der unteren Reihe, an dessen linkem Ende.";
                  case 3 ->
                      "Eine passende violette do-while-Rune liegt im Runenarchiv auf der mittleren Tischreihe ganz links.";
                  default ->
                      "Eine passende orange for-Rune liegt im Runenarchiv auf dem rechten Tisch der unteren Reihe, links neben der letzten Rune.";
                };
    }
    return new State(
        id,
        title(id),
        goal(id),
        history,
        level,
        ready && level < 3,
        ready && level == 3 && runtime.helpSolvable(),
        status);
  }

  void accept(Entity who, String event, String expected) {
    State state = snapshot();
    if (!state.puzzleId().equals(expected) || !runtime.helpAuthorized(who)) return;
    if (event.equals("help.next")) {
      if (!state.canRequest()) return;
      Progress progress = puzzles.get(expected);
      progress.next();
      int step = progress.level();
      String hint = hints(expected).get(step - 1);
      history.add(new ReleasedHint(expected, state.title(), step, hint));
      ProgrammingProgress.hint(
          expected, "step-" + step, state.title() + " · Tipp " + step + "\n\n" + hint, who);
      if (step == 3) ProgrammingProgress.interaction(expected, "simplification", who);
    } else if (event.equals("help.solve")) {
      if (!state.canSolve() || !runtime.helpSolveAuthorized(who)) return;
      ProgrammingProgress.interaction(expected, "solve-confirm", who);
      runtime.solveHelp(who);
    } else {
      String action =
          switch (event) {
            case "help.open" -> "help-open";
            case "help.return" -> "help-return";
            case "help.cancel" -> "solve-cancel";
            case "help.solve.request" -> "solve-request";
            default -> "";
          };
      if (!action.isEmpty()) ProgrammingProgress.interaction(expected, action, who);
    }
  }

  static feature.hud.dialogs.DialogContext.Builder context(
      feature.hud.dialogs.DialogContext.Builder builder) {
    state().ifPresent(state -> builder.put(ID, encode(state)));
    return builder;
  }

  static void callbacks(UIComponent ui, Entity who) {
    ui.registerCallback(
        "help.questlog",
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue value)) return;
          Game.currentLevel()
              .filter(ProgrammingLevel.class::isInstance)
              .map(ProgrammingLevel.class::cast)
              .ifPresent(
                  level -> {
                    var help = level.runtime().help();
                    if (!help.snapshot().puzzleId().equals(value.value())
                        || !level.runtime().helpAuthorized(who)) return;
                    ProgrammingProgress.interaction(value.value(), "questlog-open", who);
                    feature.questlog.QuestLogUI.showQuestLogForPlayers(who.id());
                  });
        });
    for (String event :
        List.of(
            "help.open",
            "help.next",
            "help.solve",
            "help.cancel",
            "help.solve.request",
            "help.return"))
      ui.registerCallback(
          event,
          payload -> {
            if (payload instanceof DialogResponseMessage.StringValue value)
              Game.currentLevel()
                  .filter(ProgrammingLevel.class::isInstance)
                  .map(ProgrammingLevel.class::cast)
                  .ifPresent(level -> level.runtime().help().accept(who, event, value.value()));
          });
  }

  /** Current authoritative state on the host, or the most recent client snapshot. */
  public static Optional<State> state() {
    return Game.currentLevel()
        .filter(ProgrammingLevel.class::isInstance)
        .map(ProgrammingLevel.class::cast)
        .map(ProgrammingLevel::runtime)
        .map(ProgrammingGolemRuntime::help)
        .map(ProgrammingHelp::snapshot)
        .or(() -> Optional.ofNullable(received));
  }

  public static String encode(State state) {
    return JSON.writeValueAsString(state);
  }

  public static void receive(String value) {
    received = JSON.readValue(value, State.class);
  }

  public static void reset() {
    received = null;
  }

  /** Used only for an explicitly released simplification or a confirmed solve. */
  private static final Map<Integer, String> RECOMMENDED_RUNES = new java.util.HashMap<>();

  public static String recommendedRune(int checkpoint) {
    return RECOMMENDED_RUNES.computeIfAbsent(checkpoint, ProgrammingHelp::findRecommendedRune);
  }

  private static String findRecommendedRune(int checkpoint) {
    return LoopPuzzle.runes().stream()
        .filter(
            rune -> {
              var execution = new LoopExecution(checkpoint, rune, checkpoint <= 2);
              while (execution.next().isPresent()) execution.complete();
              return execution.success();
            })
        .findFirst()
        .orElseThrow()
        .id();
  }

  private static String title(String id) {
    return switch (id) {
      case "vessels" -> "Gefäße zuordnen";
      case "essences" -> "Essenzen einsetzen";
      case "methods" -> "Werkstattprogramm kürzen";
      default -> "Kellerauftrag " + (Integer.parseInt(id.substring(7)) + 1);
    };
  }

  private static String goal(String id) {
    return switch (id) {
      case "vessels" -> "Gib jeder Eigenschaft ein Gefäß für ihren Datentyp.";
      case "essences" -> "Fülle die Gefäße mit den Werten aus Valerius' Bindungsplan.";
      case "methods" ->
          "Erledige alle Arbeitsstellen mit höchstens acht Hauptblöcken und wiederverwendbaren Methoden.";
      default -> "Bringe Nox zur Zielmarke und richte ihn für den Räumauftrag aus.";
    };
  }

  private static List<String> hints(String id) {
    return switch (id) {
      case "vessels" ->
          List.of(
              "Öffne beide Vorratskisten beim Golem. Lies dann Valerius' Bindungsplan und ordne die Gefäße an der Bindungsfläche zu.",
              "Achte auf die Art des Wertes: Text, ganze Zahl, Bruchzahl, Wahrheitswert oder einzelnes Zeichen. Die Gefäße tragen passende Prägungen.",
              "Die nächste offene Fassung ist markiert. Nur passende Gefäße bleiben sichtbar. Ordne ein passendes Gefäß zu.");
      case "essences" ->
          List.of(
              "Die Gefäße stehen bereit. Lies die gewünschten Werte in Valerius' Bindungsplan und setze Essenzen in die Fassungen.",
              "Ein passender Datentyp allein reicht nicht. Jeder Wert muss auch dem Bindungsplan entsprechen. Eine neue Essenz überschreibt den alten Wert.",
              "Die nächste offene Fassung ist markiert. Nur passende Essenzen bleiben sichtbar. Setze den Wert aus dem Bindungsplan ein.");
      case "methods" ->
          List.of(
              "Nox arbeitet die verbundenen Hauptblöcke ab. Vergleiche das lange Ausgangsprogramm mit den Arbeitsstellen und fasse wiederholte Abschnitte zusammen.",
              "Höchstens acht Hauptblöcke und sechs Blöcke je Methode. Rufe eine Methode mit Parametern mehrfach auf und verwende einen Rückgabewert. Beide Altäre müssen gefüllt sein; Nox und kristalle müssen am Ende 0 haben.",
              "Die Werkbank markiert jetzt wiederholte Anweisungen und veränderliche Eingaben in Gold. Nutze Richtung und Menge als Parameter. Sammeln gibt die Menge zurück; der Aufrufer addiert sie zu kristalle und zieht abgelegte Mengen wieder ab.");
      default ->
          List.of(
              "Beobachte Nox und die Zielmarke am Sehstein. Sammle Programmrunen im Archiv und lege in der Kellersteuerung eine Rune in den Executor.",
              switch (id) {
                case "cellar-0" ->
                    "Prüfe die Zahl der Schritte und die abschließende Linksdrehung. Eine vorab geprüfte Bedingung kann schon am Start falsch sein.";
                case "cellar-1" ->
                    "Prüfe, wann die Bedingung ausgewertet wird und wie weit die Schleife läuft. Boden voraus und Zielmarke sind verschiedene Bedingungen. Prüfe auch die Schlussdrehung.";
                case "cellar-2" ->
                    "Ein Eindringling versperrt den Weg. Der Angriff muss vor dem Schritt auf sein Feld erfolgen. Prüfe auch die Schlussdrehung.";
                case "cellar-3" ->
                    "Vor Nox liegt eine Grube. Springe darüber, bevor du weitergehst. Wiederhole den passenden Bewegungsblock und prüfe die Schlussdrehung.";
                default ->
                    "Der Weg hat wiederholte Knicke. Verfolge einen ganzen Schleifenrumpf aus Schritten und Drehungen, dann die Anzahl der Wiederholungen.";
              },
              "Die Kellersteuerung zeigt jetzt die Runenfamilie für diesen Abschnitt und markiert den Weg. Vergleiche ihre Schleifen. Falls keine Rune angezeigt wird, sammle diese Familie im Runenarchiv.");
    };
  }
}
