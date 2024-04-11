package level.rooms;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.tools.*;
import level.monster.MyImp;

public class Room12 extends Room {

  public static class ReplacingClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if (name.equals("level.monster.MyImp")) {
        try (InputStream is = new FileInputStream("src/level/monster/MyImp.class")) {
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

  Room12(
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
    compiler.run(null, null, null, new File("src/level/monster/MyImp.java").getPath());

    // Load compiled class, and replace existing MyImp class with new one
    return new ReplacingClassLoader().loadClass("level.monster.MyImp");
  }
}
