package dojo.rooms.rooms.boss;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import dojo.monster.MyImp;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
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
public class Implement_MyImp extends Room {

  private static final String IMP_FQC = "dojo.monster.MyImp";
  private static final String IMP_PATH = "src/dojo/monster/MyImp";

  /** A class loader that replaces the MyImp class with a new implementation. */
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
  public Implement_MyImp(
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
    final Entity myImp = MyImp.createEntity(this);

    final Entity chest = EntityFactory.newChest();
    chest.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) -> {
              try {
                Class<?> cls = compile();
                cls.getDeclaredMethod("modifyMyImp", Entity.class).invoke(null, myImp);
                OkDialog.showOkDialog("Die Entität \"MyImp\" wurde angepasst.", "Ok:", () -> {});
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

    // Load compiled class, and replace existing MyImp class with new one
    return new ReplacingClassLoader().loadClass(IMP_FQC);
  }
}
