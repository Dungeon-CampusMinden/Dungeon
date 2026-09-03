package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.draw.state.CharacterStateFactory;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import rooms.programming.ProgrammingRoomController;
import rooms.programming.PuzzleSubmissionResult;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopType;
import rooms.programming.state.ProgrammingStateComponent;
import rooms.programming.state.ProgrammingStateStore;

/** Spawns the movable placeholder stations used while the room layout is being built. */
final class ProgrammingRoomElements {

  private static final float GOLEM_SCALE = 2f;
  private static final float GOLEM_MAX_SPEED = 2.5f;
  private static final float GOLEM_MASS = 8f;
  private static final float GOLEM_INTERACTION_RANGE = 3.5f;
  private static final Vector2 GOLEM_HITBOX_OFFSET = Vector2.of(0.3f, 0.05f);
  private static final Vector2 GOLEM_HITBOX_SIZE = Vector2.of(0.55f, 0.45f);

  private static final List<ChoiceOption> LOOP_OPTIONS =
      List.of(
          ChoiceOption.of("while", LoopType.WHILE.name()),
          ChoiceOption.of("do-while", LoopType.DO_WHILE.name()),
          ChoiceOption.of("for", LoopType.FOR.name()));

  private static final List<Station> STATIONS =
      List.of(
          station(
              "variables-golem",
              Visual.GOLEM,
              "Golem",
              "Der zentrale Golem wartet auf seine Zuordnungen."),
          station(
              "variables-properties",
              Visual.CHEST,
              "Eigenschaftsrunen",
              "Name, Lebensenergie, Mana, Aktiviert, Blickrichtung und Schritte."),
          station(
              "variables-vessels",
              Visual.CHEST,
              "Seelengefäße",
              "Die Gefäße tragen Fantasienamen. Ihre Java-Datentypen werden erst nach der Lösung offenbart."));

  private static final List<LoopStation> LOOP_STATIONS =
      List.of(
          loop("long-corridor", "Langer Gang", "Gehe, solange der Weg vor dir frei ist."),
          loop("wall-left", "Wand links", "Gehe, solange links eine Wand ist."),
          loop(
              "pressure-plate",
              "Druckschalter",
              "Betritt zuerst den Druckschalter und prüfe danach, ob er aktiviert wurde."),
          loop(
              "five-movement-crystals", "Fünf Bewegungskristalle", "Gehe höchstens fünf Schritte."),
          loop("wall-right", "Wand rechts", "Gehe, solange rechts eine Wand ist."),
          loop(
              "magic-bridge",
              "Magische Brücke",
              "Betritt zuerst die Brücke und prüfe danach ihren Zustand."),
          loop("burning-torches", "Brennende Fackeln", "Gehe, solange die Fackeln brennen."),
          loop("three-rune-stones", "Drei Runensteine", "Aktiviere drei Runensteine."),
          loop(
              "fog-corridor",
              "Nebelgang",
              "Gehe zuerst in den Nebel und prüfe danach den weiteren Weg."),
          loop("seven-crystals", "Sieben Kristalle", "Sammle sieben Kristalle."),
          loop("locked-gate", "Verschlossenes Tor", "Handle, solange das Tor verschlossen ist."),
          loop(
              "magic-rune",
              "Magische Rune",
              "Betritt zuerst die Rune und prüfe danach ihre Wirkung."),
          loop("four-guardians", "Vier Wächter", "Passiere vier Wächter."),
          loop("exit-in-fog", "Ausgang im Nebel", "Gehe, solange der Ausgang nicht sichtbar ist."),
          loop(
              "final-passage",
              "Letzte Passage",
              "Gehe höchstens fünf Schritte, solange der Weg frei ist."));

  private ProgrammingRoomElements() {}

  static void spawn(DungeonLevel level) {
    STATIONS.forEach(station -> spawnStation(level, station));
    for (int index = 0; index < LOOP_STATIONS.size(); index++) {
      spawnLoopStation(level, LOOP_STATIONS.get(index), index);
    }
  }

  private static void spawnStation(DungeonLevel level, Station station) {
    if (!level.namedPoints().containsKey(station.pointName())) {
      return;
    }
    Entity entity = createEntity(level, station.pointName(), station.visual(), 0);
    float interactionRange =
        station.visual() == Visual.GOLEM
            ? GOLEM_INTERACTION_RANGE
            : Interaction.DEFAULT_INTERACTION_RADIUS;
    entity.add(
        new InteractionComponent(
            new Interaction((interacted, who) -> showStation(station, who), interactionRange)));
    Game.add(entity);
  }

  private static void spawnLoopStation(DungeonLevel level, LoopStation station, int runeIndex) {
    String pointName = "loop-" + station.challengeId();
    if (!level.namedPoints().containsKey(pointName)) {
      return;
    }
    Entity entity = createEntity(level, pointName, Visual.RUNE, runeIndex);
    entity.add(
        new InteractionComponent(
            new Interaction((interacted, who) -> showLoopQuestion(station, who))));
    Game.add(entity);
  }

  private static Entity createEntity(
      DungeonLevel level, String pointName, Visual visual, int runeIndex) {
    Entity entity = new Entity("programming-" + pointName);
    PositionComponent position = new PositionComponent(level.getPoint(pointName));
    if (visual == Visual.GOLEM) {
      position.scale(GOLEM_SCALE);
      entity.add(new VelocityComponent(GOLEM_MAX_SPEED, GOLEM_MASS));
      entity.add(new CollideComponent(GOLEM_HITBOX_OFFSET, GOLEM_HITBOX_SIZE));
    }
    entity.add(position);
    entity.add(visual.drawComponent(runeIndex));
    return entity;
  }

  private static void showStation(Station station, Entity who) {
    String text = station.description();
    if (station.pointName().equals("variables-golem")) {
      text = golemStatus();
    }
    DialogFactory.showTextDialog(text, station.title(), () -> {}, "Schließen", who.id());
  }

  private static String golemStatus() {
    return ProgrammingStateStore.current()
        .map(ProgrammingRoomElements::golemStatus)
        .orElse("Der Raumzustand wurde noch nicht initialisiert.");
  }

  private static String golemStatus(ProgrammingStateComponent state) {
    return "Aktuelle Phase: "
        + state.phase()
        + "\nZuordnungsstufe: "
        + state.variableStage()
        + "\nGelöste Schleifenrunen: "
        + state.completedLoopChallenges().size()
        + "/"
        + LoopPuzzle.challenges().size();
  }

  private static void showLoopQuestion(LoopStation station, Entity who) {
    DialogFactory.showMultipleChoiceDialog(
        station.prompt(),
        station.title(),
        LOOP_OPTIONS,
        false,
        answer -> showLoopResult(station, answer, who),
        () -> {},
        who.id());
  }

  private static void showLoopResult(
      LoopStation station, DialogResponseMessage.Payload answer, Entity who) {
    if (!(answer instanceof DialogResponseMessage.StringValue(String value))) {
      DialogUtils.showTextPopup(
          "Die Auswahl konnte nicht gelesen werden.", "Loopen-Test", who.id());
      return;
    }

    try {
      LoopType selected = LoopType.valueOf(value);
      PuzzleSubmissionResult result =
          ProgrammingRoomController.submitLoopAnswer(station.challengeId(), selected);
      String feedback = loopFeedback(result, selected);
      DialogUtils.showTextPopup(feedback, station.title(), who.id());
    } catch (IllegalArgumentException exception) {
      DialogUtils.showTextPopup("Unbekannte Schleifenart: " + value, "Loopen-Test", who.id());
    }
  }

  private static String loopFeedback(PuzzleSubmissionResult result, LoopType selected) {
    return switch (result) {
      case ACCEPTED ->
          "Richtig. "
              + loopExplanation(selected)
              + "\n\nGelöste Runen: "
              + completedLoopCount()
              + "/"
              + LoopPuzzle.challenges().size();
      case INCORRECT ->
          "Noch nicht. Prüfe, wann die Bedingung ausgewertet wird und ob die Anzahl bekannt ist.";
      case INACTIVE -> "Diese Rune wird erst in Akt 2 aktiv.";
    };
  }

  private static int completedLoopCount() {
    return ProgrammingStateStore.current()
        .map(state -> state.completedLoopChallenges().size())
        .orElse(0);
  }

  private static String loopExplanation(LoopType type) {
    return switch (type) {
      case WHILE -> "Die Bedingung wird vor der ersten Handlung geprüft.";
      case DO_WHILE -> "Die Handlung findet mindestens einmal vor der Prüfung statt.";
      case FOR -> "Die Wiederholung wird über eine bekannte Anzahl oder einen Zähler begrenzt.";
    };
  }

  private static Station station(
      String pointName, Visual visual, String title, String description) {
    return new Station(pointName, visual, title, description);
  }

  private static LoopStation loop(String challengeId, String title, String prompt) {
    return new LoopStation(challengeId, title, prompt);
  }

  private record Station(String pointName, Visual visual, String title, String description) {}

  private record LoopStation(String challengeId, String title, String prompt) {}

  private enum Visual {
    RUNE,
    CHEST,
    GOLEM;

    private DrawComponent drawComponent(int runeIndex) {
      DrawComponent drawComponent =
          switch (this) {
            case RUNE ->
                new DrawComponent(
                    new SimpleIPath("spritesheets/runes.png"),
                    new SpritesheetConfig(runeIndex % 8 * 16, runeIndex % 24 / 8 * 16, 1, 1));
            case CHEST -> new DrawComponent(new SimpleIPath("objects/treasurechest"), "closed");
            case GOLEM ->
                new DrawComponent(
                    CharacterStateFactory.createStateMachine(
                        new SimpleIPath("character/monster/big_zombie")));
          };
      drawComponent.depth(DepthLayer.Normal.depth());
      return drawComponent;
    }
  }
}
