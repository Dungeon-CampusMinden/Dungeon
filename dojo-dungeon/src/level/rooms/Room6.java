package level.rooms;

import contrib.components.InteractionComponent;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.IVoidFunction;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Set;
import level.tasks.Task;

public class Room6 extends TaskRoom {
  private final String FILENAME1 = "Room5ToRefactor.java";
  private final String title = "Refactoring";

  Room6(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);

    try {
      generate();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to generate: " + getClass().getName() + ": " + e.getMessage(), e);
    }
  }

  private void generate() throws IOException {
    // Create task 1
    IVoidFunction empty = () -> {};
    addTask(
        new Task(
            this,
            "task1",
            empty,
            () ->
                OkDialog.showOkDialog(
                    String.format(
                        "Refactor die Datei %s und lasse die korrigierte Datei von einem Tutor überprüfen.",
                        FILENAME1),
                    title,
                    empty),
            (t1) -> {
              OkDialog.showOkDialog(
                  "Das war schon alles, die Tür zum nächsten Level ist geöffnet.", title, empty);
              return true;
            },
            empty));

    // Create questioner
    Entity questioner = new Entity();
    questioner.add(new PositionComponent());
    questioner.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    questioner.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        (t) -> {
                          if (!t.isActivated()) {
                            t.question();
                          } else {
                            t.solve();
                          }
                        },
                        () ->
                            OkDialog.showOkDialog(
                                "Die Aufgabe ist abgeschlossen!", title, empty))));

    // Add questioner to room
    addRoomEntities(Set.of(questioner));
  }
}
