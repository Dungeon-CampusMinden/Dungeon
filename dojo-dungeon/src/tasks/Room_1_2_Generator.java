package tasks;

import contrib.components.InteractionComponent;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import contrib.utils.components.draw.ChestAnimations;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.IVoidFunction;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Room_1_2_Generator extends TaskRoomGenerator {
  private static final String FILENAME1 = "../dungeon/assets/dojo/FehlerhafteKlasse.java";
  private static final String FILENAME2 = "../dungeon/assets/dojo/FehlerhafteKlasse2.java";
  private static final String CLASS_NAME = "FehlerhafteKlasse";
  private static final String[] TEXT = {
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
  };

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

    // Create tasks 1
    IVoidFunction openDialog2 =
        () ->
            OkDialogUtil.showOkDialog(
                TEXT[0],
                "Aufgabe 1:",
                () ->
                    OkDialogUtil.showOkDialog(
                        TEXT[1],
                        "Aufgabe 1:",
                        () -> OkDialogUtil.showOkDialog(TEXT[2], "Aufgabe 1:")));
    IVoidFunction openDialog1 = () -> OkDialogUtil.showOkDialog(TEXT[3], "Aufgabe 1:", openDialog2);
    Function<Task, Boolean> openDialog3 =
        (t) -> {
          DojoCompiler.TestResult results = new DojoCompiler(FILENAME2, CLASS_NAME).test1();
          if (results.passed()) {
            OkDialogUtil.showOkDialog("Danke ... gelöst: " + results.messages(), "Lösung 1:");
            return true;
          }
          OkDialogUtil.showOkDialog("Fehler: " + results.messages(), "Lösung 1:");
          return false;
        };
    IVoidFunction openDialog4 = () -> OkDialogUtil.showOkDialog(TEXT[4], "Lösung 1:");

    // Create tasks 2
    IVoidFunction openDialog6 = () -> OkDialogUtil.showOkDialog(TEXT[6], "Aufgabe 2:");
    IVoidFunction openDialog5 = () -> OkDialogUtil.showOkDialog(TEXT[7], "Aufgabe 2:", openDialog6);
    Function<Task, Boolean> openDialog7 =
        (t) -> {
          DojoCompiler.TestResult results = new DojoCompiler(FILENAME2, CLASS_NAME).test2();
          if (results.passed()) {
            OkDialogUtil.showOkDialog("Danke ... gelöst: " + results.messages(), "Lösung 2:");
            return true;
          }
          OkDialogUtil.showOkDialog("Fehler: " + results.messages(), "Lösung 2:");
          return false;
        };
    IVoidFunction openDialog8 = () -> OkDialogUtil.showOkDialog(TEXT[8], "Lösung 2:");

    // add tasks
    addTask(new Task(this, openDialog1, openDialog2, openDialog3, openDialog4));
    addTask(new Task(this, openDialog5, openDialog6, openDialog7, openDialog8));

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
                        Task::question, () -> OkDialogUtil.showOkDialog(TEXT[5], "Aufgabe(n):"))));

    // add solver chest 1
    Entity solver1 = EntityFactory.newChest();
    solver1.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::solve, () -> OkDialogUtil.showOkDialog(TEXT[5], "Lösung(en):"))));

    solver1.fetch(DrawComponent.class).orElseThrow().queueAnimation(ChestAnimations.OPEN_FULL);

    // add solver chest 2
    Entity solver2 = EntityFactory.newChest();
    solver2.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                getNextUncompletedTask()
                    .ifPresentOrElse(
                        Task::solve, () -> OkDialogUtil.showOkDialog(TEXT[5], "Lösung(en):"))));

    roomEntities.add(talkToMe);
    roomEntities.add(solver1);
    roomEntities.add(solver2);

    addRoomEntities(roomEntities);
  }
}
