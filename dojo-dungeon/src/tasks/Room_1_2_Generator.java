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
    String fileName = "Task_1_2.java";
    String text =
        "Öffne die Datei "
            + fileName
            + " und verbessere die Fehler, speichere sie und öffne dann die Truhe, um zu überprüfen.";

    addTask(
        new Task(
            this,
            (t) ->
                OkDialogUtil.showOkDialog(
                    "Arr, Sie kenne ich schon. Hier noch mal die Aufgabe. " + text, "Aufgabe 1:"),
            (t) -> OkDialogUtil.showOkDialog(text, "Aufgabe 1:"),
            (t) -> {
              OkDialogUtil.showOkDialog(
                  "Alles ok, Sie können mit der nächsten Aufgabe weitermachen.", "Lösung 1:");
              return true;
            },
            (t) ->
                OkDialogUtil.showOkDialog(
                    "Gehe zuerst zum Questioner für Aufgabe 1.", "Lösung 1:")));

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
