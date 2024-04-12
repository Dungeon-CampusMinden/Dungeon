package dojo.rooms.level_1;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.draw.ChestAnimations;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum soll eine fehlerhafte Java-Klasse korrigiert werden, während man spielt. Erst,
 * wenn man alle Fehler gefunden und alle Überprüfungen geschafft hat, kann man in den nächsten Raum
 * weitergehen. Es gibt dabei drei "Prüfstufen".
 */
public class L1_R2_Fehler_Syntax extends TaskRoom {
  private final String FILENAME1 = "../dojo-dungeon/todo-assets/lvl1r2/FehlerhafteKlasse.java";
  private final String FILENAME2 = "../dojo-dungeon/todo-assets/lvl1r2/Klasse.java";
  private final String CLASS_NAME = "Klasse";
  private final String[] TEXT = {
    // 0
    "Die Datei " + FILENAME1 + " enthält 4 Fehler (Syntax und Semantik).",
    // 1
    "Öffne/Kopiere die Datei, korrigiere die Fehler und speichere die Datei unter: " + FILENAME2,
    // 2
    "Laufe dann zur Truhe 1 und lasse die Datei überprüfen.",
    // 3
    "Arrrr, Sie kenne ich schon. Hier noch mal die Aufgabe.",
    // 4
    "Gehe zuerst zum Questioner für Aufgabe 1.",
    // 5
    "Du hast schon alle Aufgaben gelöst, die Tür ist offen.",
    // 6
    "Du hast Aufgabe 1 gelöst. Gehe nun zu Truhe 2 und lasse deine Datei zusätzlich überprüfen.",
    // 7
    "Noch einmal die aktuelle Aufgabe:",
    // 8
    "Du hast Aufgabe 1 gelöst. Gehe zuerst zum Questioner für Aufgabe 2.",
    // 9
    "Ich bin nicht dran.",
    // 10
    "Du hast Aufgabe 2 gelöst. Gehe nun zu Truhe 3 und lasse deine Datei noch einmal zusätzlich überprüfen.",
    // 11
    "Noch einmal die aktuelle Aufgabe:",
    // 12
    "Du hast Aufgabe 2 gelöst. Gehe zuerst zum Questioner für Aufgabe 3.",
  };
  private final String[] TASK_NAMES = {"task_1_easy", "task_2_medium", "task_3_hard"};
  private final Entity[] CHESTS = new Entity[3];

  public L1_R2_Fehler_Syntax(
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
    // Create tasks 1
    IVoidFunction empty = () -> {};
    IVoidFunction openDialog2 =
        () ->
            OkDialog.showOkDialog(
                TEXT[0],
                "Aufgabe 1:",
                () ->
                    OkDialog.showOkDialog(
                        TEXT[1],
                        "Aufgabe 1:",
                        () -> OkDialog.showOkDialog(TEXT[2], "Aufgabe 1:", empty)));
    IVoidFunction openDialog1 = () -> OkDialog.showOkDialog(TEXT[3], "Aufgabe 1:", openDialog2);
    Function<Task, Boolean> openDialog3 =
        (t) -> {
          DojoCompiler.TestResult results =
              new DojoCompiler().testWrongClass1(FILENAME2, CLASS_NAME);
          if (results.passed()) {
            OkDialog.showOkDialog(
                "Danke ... gelöst: " + results.messages(), "Lösung 1:", this::openOrCloseChests);
            return true;
          }
          OkDialog.showOkDialog("Fehler: " + results.messages(), "Lösung 1:", empty);
          return false;
        };
    IVoidFunction openDialog4 = () -> OkDialog.showOkDialog(TEXT[4], "Lösung 1:", empty);

    // Create tasks 2
    IVoidFunction openDialog6 = () -> OkDialog.showOkDialog(TEXT[6], "Aufgabe 2:", empty);
    IVoidFunction openDialog5 = () -> OkDialog.showOkDialog(TEXT[7], "Aufgabe 2:", openDialog6);
    Function<Task, Boolean> openDialog7 =
        (t) -> {
          DojoCompiler.TestResult results =
              new DojoCompiler().testWrongClass2(FILENAME2, CLASS_NAME);
          if (results.passed()) {
            OkDialog.showOkDialog(
                "Danke ... gelöst: " + results.messages(), "Lösung 2:", this::openOrCloseChests);
            return true;
          }
          OkDialog.showOkDialog("Fehler: " + results.messages(), "Lösung 2:", empty);
          return false;
        };
    IVoidFunction openDialog8 = () -> OkDialog.showOkDialog(TEXT[8], "Lösung 2:", empty);

    // Create tasks 3
    IVoidFunction openDialog10 = () -> OkDialog.showOkDialog(TEXT[10], "Aufgabe 3:", empty);
    IVoidFunction openDialog9 = () -> OkDialog.showOkDialog(TEXT[11], "Aufgabe 3:", openDialog10);
    Function<Task, Boolean> openDialog11 =
        (t) -> {
          DojoCompiler.TestResult results =
              new DojoCompiler().testWrongClass3(FILENAME2, CLASS_NAME);
          if (results.passed()) {
            OkDialog.showOkDialog(
                "Danke ... gelöst: " + results.messages(), "Lösung 3:", this::openOrCloseChests);
            return true;
          }
          OkDialog.showOkDialog("Fehler: " + results.messages(), "Lösung 3:", empty);
          return false;
        };
    IVoidFunction openDialog12 = () -> OkDialog.showOkDialog(TEXT[12], "Lösung 3:", empty);

    // add tasks
    addTask(new Task(this, TASK_NAMES[0], openDialog1, openDialog2, openDialog3, openDialog4));
    addTask(new Task(this, TASK_NAMES[1], openDialog5, openDialog6, openDialog7, openDialog8));
    addTask(new Task(this, TASK_NAMES[2], openDialog9, openDialog10, openDialog11, openDialog12));

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
                        () -> OkDialog.showOkDialog(TEXT[5], "Aufgabe(n):", empty))));

    // add solver chest 1
    CHESTS[0] = EntityFactory.newChest();
    CHESTS[0].add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        (t1) ->
                            getNextUncompletedTaskByName(TASK_NAMES[0])
                                .ifPresentOrElse(
                                    Task::solve,
                                    () -> OkDialog.showOkDialog(TEXT[9], "Lösung(en):", empty)),
                        () -> OkDialog.showOkDialog(TEXT[5], "Lösung(en):", empty))));

    // add solver chest 2
    CHESTS[1] = EntityFactory.newChest();
    CHESTS[1].add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        (t1) ->
                            getNextUncompletedTaskByName(TASK_NAMES[1])
                                .ifPresentOrElse(
                                    Task::solve,
                                    () -> OkDialog.showOkDialog(TEXT[9], "Lösung(en):", empty)),
                        () -> OkDialog.showOkDialog(TEXT[5], "Lösung(en):", empty))));

    // add solver chest 3
    CHESTS[2] = EntityFactory.newChest();
    CHESTS[2].add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        (t1) ->
                            getNextUncompletedTaskByName(TASK_NAMES[2])
                                .ifPresentOrElse(
                                    Task::solve,
                                    () -> OkDialog.showOkDialog(TEXT[9], "Lösung(en):", empty)),
                        () -> OkDialog.showOkDialog(TEXT[5], "Lösung(en):", empty))));

    // open the first chest after initialization of all tasks and chests in this room
    openOrCloseChests();

    roomEntities.add(talkToMe);
    roomEntities.addAll(Arrays.asList(CHESTS));

    addRoomEntities(roomEntities);
  }

  private void openOrCloseChests() {
    // open or close all chests, depending on the next uncompleted task with name...
    for (int i = 0; i < TASK_NAMES.length; i++) {
      final Entity chest = CHESTS[i];
      getNextUncompletedTaskByName(TASK_NAMES[i])
          .ifPresentOrElse(
              (t) ->
                  chest
                      .fetch(DrawComponent.class)
                      .orElseThrow()
                      .currentAnimation(ChestAnimations.OPEN_FULL),
              () ->
                  chest
                      .fetch(DrawComponent.class)
                      .orElseThrow()
                      .currentAnimation(ChestAnimations.CLOSED));
    }
  }
}
