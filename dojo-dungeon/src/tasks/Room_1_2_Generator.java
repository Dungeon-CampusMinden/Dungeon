package tasks;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.TextDialog;
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

    addTask(
        new Task(
            this,
            (t) -> {
              TextDialog.textDialog(
                  "Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\nÖffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 1:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Öffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 1:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Alles ok, Sie können mit Aufgabe 2 weitermachen.", "Ok", "Lösung 1:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog("Gehe zuerst zum Questioner für Aufgabe 1.", "Ok", "Lösung 1:");
              return false;
            }));

    addTask(
        new Task(
            this,
            (t) -> {
              TextDialog.textDialog(
                  "Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\nÖffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 2:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Öffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 2:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Alles ok, Sie können mit Aufgabe 3 weitermachen.", "Ok", "Lösung 2:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog("Gehe zuerst zum Questioner für Aufgabe 2.", "Ok", "Lösung 2:");
              return false;
            }));

    addTask(
        new Task(
            this,
            (t) -> {
              TextDialog.textDialog(
                  "Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\nÖffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 3:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Öffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Aufgabe 3:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog(
                  "Alles ok, Sie können in den nächsten Raum gehen.", "Ok", "Lösung 3:");
              return true;
            },
            (t) -> {
              TextDialog.textDialog("Gehe zuerst zum Questioner für Aufgabe 3.", "Ok", "Lösung 3:");
              return false;
            }));

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
                            TextDialog.textDialog(
                                "Du hast schon alle Aufgaben gelöst!", "Ok", "Ihre Lösung:"))));

    // add a solver chest
    Entity solver = EntityFactory.newChest();
    solver.add(
        new InteractionComponent(
            1,
            true,
            (interacted, interactor) -> {
              getNextUncompletedTask()
                  .ifPresentOrElse(
                      Task::solve,
                      () ->
                          TextDialog.textDialog(
                              "Du hast schon alle Aufgaben gelöst!", "Ok", "Ihre Lösung:"));
            }));

    roomEntities.add(talkToMe);
    roomEntities.add(solver);

    addRoomEntities(roomEntities);
  }
}
