package tasks;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Room_1_2_Generator extends TaskRoomGenerator {

  public Room_1_2_Generator(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) {
    super(gen, room, nextNeighbour);
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.MEDIUM, getRoom().neighbours()),
                DesignLabel.randomDesign()));

    // Create tasks
    String fileName = "../dungeon/assets/dojo/FehlerhafteKlasse.java";
    String className = "FehlerhafteKlasse";
    String text =
        "Die Datei "
            + fileName
            + " enthält 4 Fehler (Syntax und Semantik). Öffne sie, korrigiere die Fehler, speichere die Datei und laufe dann zur Truhe. :)";

    addTask(
        new Task(
            this,
            (t) ->
                OkDialogUtil.showOkDialog(
                    "Arr, Sie kenne ich schon. Hier noch mal die Aufgabe. " + text, "Aufgabe 1:"),
            (t) -> OkDialogUtil.showOkDialog(text, "Aufgabe 1:"),
            (t) -> {
              List<String> results = compileAndRun(fileName, className);
              OkDialogUtil.showOkDialog(
                  "Ergebnisse:\n\n" + String.join("\n", results), "Lösung 1:");
              return results.getFirst().equals("ok");
            },
            (t) ->
                OkDialogUtil.showOkDialog(
                    "Gehe zuerst zum Questioner für Aufgabe 1.", "Lösung 1:")));

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
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::question,
                        () ->
                            OkDialogUtil.showOkDialog(
                                "Sie haben schon alle Aufgaben gelöst, die Tür ist auf.",
                                "Aufgabe(n):"))));

    // add a solver chest
    Entity solver = EntityFactory.newChest();
    solver.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::solve,
                        () ->
                            OkDialogUtil.showOkDialog(
                                "Sie haben schon alle Aufgaben gelöst, die Tür ist auf.",
                                "Lösung(en):"))));

    roomEntities.add(talkToMe);
    roomEntities.add(solver);

    addRoomEntities(roomEntities);
  }

  private List<String> compileAndRun(String fileName, String className) {
    try {
      Method method = compile(fileName, className);
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      method.invoke(null, new String[] {}, new PrintWriter(buf));
      String actualOutput = buf.toString(Charset.defaultCharset());
      assert actualOutput.equals(
          """
            Die Summe ist: 7
            Die dritte Zahl ist: 7
            """);
    } catch (Exception e) {
      // e.printStackTrace();
      return List.of(e.getMessage());
    }
    return List.of("ok");
  }

  private Method compile(String fileName, String className) throws Exception {
    // Prepare source somehow.
    String source = Files.readString(Paths.get(fileName));
    //    try (InputStream is = this.getClass().getResourceAsStream(fileName)) {
    //      assert is != null;
    //      source = new String(is.readAllBytes(), Charset.defaultCharset());
    //    }

    // Regex replacement ...
    // source = source.replace("\"7\"", "\"777777777777777777777\"");

    // Save source in .java file.
    File root = Files.createTempDirectory("java").toFile();
    File sourceFile = new File(root, className + ".java");
    assert sourceFile.getParentFile().mkdirs();
    Files.writeString(sourceFile.toPath(), source);
    // Compile source file.
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, sourceFile.getPath());
    // Load and instantiate compiled class.
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {root.toURI().toURL()});
    Class<?> cls = Class.forName(className, true, classLoader);
    return cls.getDeclaredMethod("main", String[].class, PrintWriter.class);
  }
}
