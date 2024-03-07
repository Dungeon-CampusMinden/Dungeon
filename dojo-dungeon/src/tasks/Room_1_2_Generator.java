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
    String text =
        "Öffne die Datei "
            + fileName
            + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.";
    String[][] taskText1 = {
      {"Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\n" + text, "Ok", "Aufgabe 1:"},
      {text, "Ok", "Aufgabe 1:"},
      {"Alles ok, Sie können mit Aufgabe 2 weitermachen.", "Ok", "Lösung 1:"},
      {"Gehe zuerst zum Questioner für Aufgabe 1.", "Ok", "Lösung 1:"},
      {"Alles ok, Sie können in den nächsten Raum gehen.", "Sie haben schon alle Aufgaben gelöst!"}
    };
    String[][] taskText2 = {
      {"Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\n" + text, "Ok", "Aufgabe 2:"},
      {text, "Ok", "Aufgabe 2:"},
      {"Alles ok, Sie können mit Aufgabe 3 weitermachen.", "Ok", "Lösung 2:"},
      {"Gehe zuerst zum Questioner für Aufgabe 2.", "Ok", "Lösung 2:"},
      {"Alles ok, Sie können in den nächsten Raum gehen.", "Sie haben schon alle Aufgaben gelöst!"}
    };
    String[][] taskText3 = {
      {"Arr, Sie kenne ich.\nHier noch mal die Aufgabe.\n" + text, "Ok", "Aufgabe 3:"},
      {text, "Ok", "Aufgabe 3:"},
      {"Alles ok, Sie können in den nächsten Raum gehen.", "Ok", "Lösung 3:"},
      {"Gehe zuerst zum Questioner für Aufgabe 3.", "Ok", "Lösung 3:"},
      {"Alles ok, Sie können in den nächsten Raum gehen.", "Sie haben schon alle Aufgaben gelöst!"}
    };

    addTask(
        new Task(
            this,
            (t) -> TextDialog.textDialog(taskText1[0][0], taskText1[0][1], taskText1[0][2]),
            (t) -> TextDialog.textDialog(taskText1[1][0], taskText1[1][1], taskText1[1][2]),
            (t) -> {
              TextDialog.textDialog(taskText1[2][0], taskText1[2][1], taskText1[2][2]);
              return true;
            },
            (t) -> TextDialog.textDialog(taskText1[3][0], taskText1[3][1], taskText1[3][2])));

    addTask(
        new Task(
            this,
            (t) -> TextDialog.textDialog(taskText2[0][0], taskText2[0][1], taskText2[0][2]),
            (t) -> TextDialog.textDialog(taskText2[1][0], taskText2[1][1], taskText2[1][2]),
            (t) -> {
              TextDialog.textDialog(taskText2[2][0], taskText2[2][1], taskText2[2][2]);
              return true;
            },
            (t) -> TextDialog.textDialog(taskText2[3][0], taskText2[3][1], taskText2[3][2])));

    addTask(
        new Task(
            this,
            (t) -> TextDialog.textDialog(taskText3[0][0], taskText3[0][1], taskText3[0][2]),
            (t) -> TextDialog.textDialog(taskText3[1][0], taskText3[1][1], taskText3[1][2]),
            (t) -> {
              TextDialog.textDialog(taskText3[2][0], taskText3[2][1], taskText3[2][2]);
              return true;
            },
            (t) -> TextDialog.textDialog(taskText3[3][0], taskText3[3][1], taskText3[3][2])));

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
                        () -> TextDialog.textDialog(taskText1[4][1], "Ok", "Ihre Aufgabe:"))));

    // add a solver chest
    Entity solver = EntityFactory.newChest();
    solver.add(
        new InteractionComponent(
            1,
            true,
            (interacted, interactor) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::solve,
                        () -> TextDialog.textDialog(taskText1[4][1], "Ok", "Ihre Lösung:"))));

    roomEntities.add(talkToMe);
    roomEntities.add(solver);

    addRoomEntities(roomEntities);
  }
}
