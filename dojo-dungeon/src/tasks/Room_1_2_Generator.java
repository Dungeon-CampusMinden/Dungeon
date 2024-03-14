package tasks;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Room_1_2_Generator extends TaskRoomGenerator {

  public Room_1_2_Generator(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) {
    super(gen, room, nextNeighbour);
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.MEDIUM, getRoom().neighbours()),
                DesignLabel.randomDesign()));

    // Create tasks
    String fileName = "../dungeon/assets/dojo/FehlerhafteKlasse.java";
    String fileName2 = "../dungeon/assets/dojo/FehlerhafteKlasse2.java";
    String className = "FehlerhafteKlasse";
    String text =
        "Die Datei "
            + fileName
            + " enthält 4 Fehler (Syntax und Semantik). Öffne sie, korrigiere die Fehler, speichere die Datei und laufe dann zur Truhe. :)";
    String text2 =
        "Öffne/Kopiere die Datei, korrigiere die Fehler und speichere die Datei unter: "
            + fileName2;
    String text3 = "Laufe dann zur Truhe und lasse die Datei überprüfen.";
    Consumer<Task> openDialog1 =
        (t) -> {
          OkDialogUtil.showOkDialog(text, "Aufgabe 1:");
          OkDialogUtil.showOkDialog(text2, "Aufgabe 1:");
          OkDialogUtil.showOkDialog(text3, "Aufgabe 1:");
        };
    Consumer<Task> openDialog2 =
        (t) -> {
          OkDialogUtil.showOkDialog(
              "Arrr, Sie kenne ich schon. Hier noch mal die Aufgabe.", "Aufgabe 1:");
          openDialog1.accept(t);
        };
    Function<Task, Boolean> openDialog3 =
        (t) -> {
          DojoCompiler.TestResult results = new DojoCompiler(fileName2, className).test1();
          if (results.passed()) {
            OkDialogUtil.showOkDialog("Danke ... gelöst: " + results.messages(), "Lösung 1:");
            return true;
          }
          OkDialogUtil.showOkDialog("Fehler: " + results.messages(), "Lösung 1:");
          return false;
        };
    Consumer<Task> openDialog4 =
        (t) -> {
          OkDialogUtil.showOkDialog("Gehe zuerst zum Questioner für Aufgabe 1.", "Lösung 1:");
        };

    addTask(new Task(this, openDialog2, openDialog1, openDialog3, openDialog4));

    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    // add questioner
    Entity talkToMe = new Entity();
    talkToMe.add(new PositionComponent());
    talkToMe.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    talkToMe.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::question,
                        () ->
                            OkDialogUtil.showOkDialog(
                                "Sie haben schon alle Aufgaben gelöst, die Tür ist auf.",
                                "Aufgabe(n):"))));

    // add a solver chest
    Entity solver = EntityFactory.newChest();
    solver.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::solve,
                        () ->
                            OkDialogUtil.showOkDialog(
                                "Sie haben schon alle Aufgaben gelöst, die Tür ist auf.",
                                "Lösung(en):"))));

    roomEntities.add(talkToMe);
    roomEntities.add(solver);

    addRoomEntities(roomEntities);
  }
}
