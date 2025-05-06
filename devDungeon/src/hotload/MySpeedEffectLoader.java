package hotload;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MySpeedEffectLoader {

  public static Object loadUserSpeedEffectInstance() {
    try {
      // Pfad zur Datei
      Path filePath = Paths.get("MySpeedEffect.java");
      String userCode = Files.readString(filePath);

      System.out.println("Lese Quellcode von: " + filePath);

      // Dynamische Kompilierung und Laden der Klasse mit einem neuen ClassLoader
      Class<?> newClass = DynamicCompiler.compileAndLoad("hotload.MySpeedEffect", userCode);

      // Konstruktor holen
      Constructor<?> ctor = newClass.getConstructor();

      System.out.println("Erstelle Instanz der Klasse: " + newClass.getName());

      // Instanz erstellen
      return ctor.newInstance();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
