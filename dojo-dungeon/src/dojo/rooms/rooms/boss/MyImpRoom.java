package dojo.rooms.rooms.boss;

import contrib.components.*;
import contrib.entities.AIFactory;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.utils.studentTasks.modifyEntities.ModifyEntities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.tools.*;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum erhält man eine Beschreibung des erwarteten Verhaltens, die implementiert
 * werden muss. Der Dämon erteilt die Aufgabe, Methoden zu schreiben, die dieses Verhalten
 * implementieren. Danach muss der Dämon angegriffen werden.
 */
public class MyImpRoom extends Room {
  private static final String PATH_FOR_UI =
      "dojo-dungeon/src/dojo/utils/studentTasks/modifyEntities/ModifyEntities.java";
  private static final String IMP_FQC = "dojo.utils.studentTasks.modifyEntities.ModifyEntities";
  private static final String IMP_PATH =
      "src/dojo/utils/studentTasks/modifyEntities/ModifyEntities";

  /** A class loader that replaces the ModifyEntities class with a new implementation. */
  public static class ReplacingClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if (name.equals(IMP_FQC)) {
        try (InputStream is = new FileInputStream(IMP_PATH + ".class")) {
          byte[] buf = new byte[10000];
          int len = is.read(buf);
          return defineClass(name, buf, 0, len);
        } catch (IOException e) {
          throw new ClassNotFoundException("", e);
        }
      }
      return getParent().loadClass(name);
    }
  }

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public MyImpRoom(
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
    final Entity myImp = createEntityMyImp(this);

    final Entity chest = EntityFactory.newChest();
    chest.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) -> {
              try {
                Class<?> cls = compile();
                cls.getDeclaredMethod("disableGodMode", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("setHealthTo25", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("increaseSpeed", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("addNewHealthPotionToInventory", Entity.class)
                    .invoke(null, Game.hero().orElseThrow());
                OkDialog.showOkDialog("Die Entitäten wurden angepasst.", "Ok:", () -> {});
              } catch (Exception e) {
                OkDialog.showOkDialog(e.getMessage(), "Fehler:", () -> {});
              }
            }));

    addRoomEntities(Set.of(myImp, chest));
  }

  private Class<?> compile() throws Exception {
    // Compile source file
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, new File(IMP_PATH + ".java").getPath());

    // Load compiled class, and replace existing ModifyEntities class with new one
    return new ReplacingClassLoader().loadClass(IMP_FQC);
  }

  private static Entity createEntityMyImp(Room currentRoom) {
    Entity entity = new Entity();

    entity.name("MyImp");

    InventoryComponent ic = new InventoryComponent(5);
    entity.add(ic);

    entity.add(
        new HealthComponent(
            1000,
            (e) ->
                OkDialog.showOkDialog(
                    "Danke, du hast die Aufgabe in diesem Raum gelöst!",
                    "Aufgabe gelöst:",
                    currentRoom::openDoors)));
    entity.fetch(HealthComponent.class).orElseThrow().godMode(true);

    entity.add(new PositionComponent());

    entity.add(new AIComponent(AIFactory.randomFightAI(), AIFactory.randomIdleAI(), e -> false));

    try {
      entity.add(new DrawComponent(new SimpleIPath("character/monster/imp")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    entity.add(new VelocityComponent(0, 0));

    entity.add(new CollideComponent());

    // Tell the tasks of this room ...
    entity.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                OkDialog.showOkDialog(
                    "Du findest eine Implementierung in \""
                        + ModifyEntities.class.getName()
                        + "\" (\""
                        + PATH_FOR_UI
                        + "\").",
                    "Aufgabe in diesem Raum:",
                    () ->
                        OkDialog.showOkDialog(
                            "Implementiere die Methoden disableGodMode(), setHealthTo25(), increaseSpeed() und addNewHealthPotionToInventory().",
                            "Aufgabe in diesem Raum:",
                            () ->
                                OkDialog.showOkDialog(
                                    "Sprich dann mit der Truhe, um mich erneut zu laden, und greife an!",
                                    "Aufgabe in diesem Raum:",
                                    () -> {})))));

    return entity;
  }
}
