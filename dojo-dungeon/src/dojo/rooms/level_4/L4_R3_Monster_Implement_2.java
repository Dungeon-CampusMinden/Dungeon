package dojo.rooms.level_4;

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
import java.lang.reflect.Constructor;
import java.util.*;
import javax.tools.*;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum erhält man eine Beschreibung des erwarteten Verhaltens, die implementiert
 * werden muss. Der Dämon erteilt die Aufgabe, Methoden zu schreiben, die dieses Verhalten
 * implementieren. Danach muss der Dämon angegriffen werden.
 */
public class L4_R3_Monster_Implement_2 extends Room {

  private static final String IMP_FQC = "dojo.monster.MyImp";
  private static final String IMP_PATH = "src/dojo/monster/MyImp";

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

  public L4_R3_Monster_Implement_2(
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

  public void generate() throws IOException {
    final Entity myImp = new Entity();
    new MyImp(myImp, false);

    final Entity chest = EntityFactory.newChest();
    chest.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) -> {
              try {
                Class<?> cls = compile();
                Constructor<?> tor = cls.getDeclaredConstructor(Entity.class, boolean.class);
                tor.newInstance(myImp, true);
                OkDialog.showOkDialog("Imp wurde erneut geladen.", "Ok:", () -> {});
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
