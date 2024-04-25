package dojo.rooms;

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
import dojo.tasks.Task;
import java.io.IOException;
import java.util.Set;

public class ClsToRefactor extends TaskRoom {
  public final String title = "Title";
  public String FILENAME2 = "../dojo-dungeon/todo-assets/lvl2r2/MyMonster.java";

  Room5ToRefactor(
      LevelRoom a, RoomGenerator b, Room c, LevelSize d, DesignLabel e, IVoidFunction empty) {
    super(a, b, c, d, e);

    try {
      ge();
    } catch (IOException exc) {
      throw new RuntimeException("Failed to generate something: " + exc.getMessage(), exc);
    }
  }

  private void ge() throws IOException {
    // Create?
    IVoidFunction f = () -> {};
    addTask(
        new Task(
                this,
                "task1",
                f,
                () ->
                    OkDialog.showOkDialog(
                        String.format(
                            "Implementiere die Datei %s, nach der Vorgabe in %s. Wenn das Monster besiegt ist, soll sich die Tür zum nächsten Raum öffnen.",
                            "../dojo-dungeon/todo-assets/lvl2r2/MyMonster.java",
                            "../dojo-dungeon/todo-assets/lvl2r2/Monster.java"),
                        title,
                        f),
                (t1) -> {
                  DojoCompiler.TestResult results =
                      new DojoCompiler(
                              "../dojo-dungeon/todo-assets/lvl2r2/MyMonster.java", "MyMonster")
                          .spawnMonsterToOpenTheDoor(this);
                  if (results.passed()) {
                    OkDialog.showOkDialog(
                        "Ok! " + results.messages(),
                        title,
                        () -> OkDialog.showOkDialog("Das Monster ist gespawnt!", title, f));
                    return true;
                  }
                  OkDialog.showOkDialog("Fehler: " + results.messages(), title, f);
                  return false;
                },
                f)
            .setShouldOpenDoors(false));

    // Create q?
    Entity q = new Entity();
    q.add(new PositionComponent());
    q.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    q.add(
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
                            OkDialog.showOkDialog("Das Monster ist bereits gespawnt!", title, f))));

    // Add q to r
    addRoomEntities(Set.of(q));
  }
}
