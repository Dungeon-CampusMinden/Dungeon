package dojo.rooms.level_2;

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
public class L2_R2_Monster_Implement_1 extends TaskRoom {
  private final String FILENAME1 = "../dojo-dungeon/todo-assets/lvl2r2/Monster.java";
  private final String FILENAME2 = "../dojo-dungeon/todo-assets/lvl2r2/MyMonster.java";
  private final String CLASS_NAME = "MyMonster";
  private final String title = "Monster besiegen";

  public L2_R2_Monster_Implement_1(
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
                            "Implementiere die Datei %s, nach der Vorgabe in %s. Wenn das Monster besiegt ist, soll sich die Tür zum nächsten Raum öffnen.",
                            FILENAME2, FILENAME1),
                        title,
                        empty),
                (t1) -> {
                  DojoCompiler.TestResult results =
                      new DojoCompiler().spawnMonsterToOpenTheDoor(FILENAME2, CLASS_NAME, this);
                  if (results.passed()) {
                    OkDialog.showOkDialog(
                        "Ok! " + results.messages(),
                        title,
                        () -> OkDialog.showOkDialog("Das Monster ist gespawnt!", title, empty));
                    return true;
                  }
                  OkDialog.showOkDialog("Fehler: " + results.messages(), title, empty);
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
                                "Das Monster ist bereits gespawnt!", title, empty))));

    // Add questioner to room
    addRoomEntities(Set.of(questioner));
  }
}
