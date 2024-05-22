package dojo.rooms.rooms.riddle;

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
import dojo.compiler.DojoCompiler;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.TaskRoom;
import dojo.tasks.Task;
import java.io.IOException;
import java.util.Set;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum muss ein Monster mit verschiedenen Eigenschaften erstellt und danach besiegt
 * werden, um in den nächsten Raum zu gelangen.
 */
public class Implement_MyMonster extends TaskRoom {
  private static final String PATH_TO_SOURCE_FILES =
      "dojo-dungeon/todo-assets/Implement_MyMonster/";
  private static final String CLASS_NAME = "MyMonster";
  private static final String FILE_NAME = PATH_TO_SOURCE_FILES + CLASS_NAME + ".java";

  private final String TITLE = "Monster besiegen";

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public Implement_MyMonster(
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
                            "Implementiere die Datei %s. Wenn das Monster besiegt ist, soll sich die Tür zum nächsten Raum öffnen.",
                            FILE_NAME),
                        TITLE,
                        empty),
                (t1) -> {
                  DojoCompiler.TestResult results =
                      new DojoCompiler()
                          .spawnMonsterToOpenTheDoor(PATH_TO_SOURCE_FILES, CLASS_NAME, this);
                  if (results.passed()) {
                    OkDialog.showOkDialog(
                        "Ok! " + results.messages(),
                        TITLE,
                        () -> OkDialog.showOkDialog("Das Monster ist gespawnt!", TITLE, empty));
                    return true;
                  }
                  OkDialog.showOkDialog("Fehler: " + results.messages(), TITLE, empty);
                  return false;
                },
                empty)
            .setShouldOpenDoors(false));

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
                                "Das Monster ist bereits gespawnt!", TITLE, empty))));

    // Add questioner to room
    addRoomEntities(Set.of(questioner));
  }
}
