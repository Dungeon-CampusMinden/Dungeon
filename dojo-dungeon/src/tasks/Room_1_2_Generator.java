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

public class Room_1_2_Generator implements TaskRoomGenerator {

  @Override
  public void generateRoom(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour)
      throws IOException {
    // generate the room
    room.level(
        new TileLevel(gen.layout(LevelSize.MEDIUM, room.neighbours()), DesignLabel.randomDesign()));

    // Create tasks
    String fileName = "Task_1_2.java";

    Task task =
        new Task(
            this,
            room,
            nextNeighbour,
            (t) -> {
              Entity dialog =
                  TextDialog.textDialog("Alles ok, Sie können weitergehen.", "Ok", "Ihre Lösung:");
              // todo: resize the text dialog (to fit the text)
              return true;
            },
            (t) -> {
              TextDialog.textDialog("Gehe zuerst zum Questioner.", "Ok", "Ihre Lösung:");
              return false;
            });
    addTask(task);

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
            (entity1, entity2) -> {
              TextDialog.textDialog(
                  "Öffne die Datei "
                      + fileName
                      + ", verbessere die Fehler, speichere sie und öffne dann die Truhe, um weiterzugehen.",
                  "Ok",
                  "Ihre Aufgabe:");
              task.setActivated(true);
            }));

    // add a solver chest
    Entity solver = EntityFactory.newChest();
    solver.add(new InteractionComponent(1, true, (interacted, interactor) -> task.check()));

    roomEntities.add(talkToMe);
    roomEntities.add(solver);

    addRoomEntities(room, roomEntities);
  }
}
