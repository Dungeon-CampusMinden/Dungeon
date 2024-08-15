package task;

import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.level.generator.graphBased.LevelGraphGenerator;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import task.game.components.DoorComponent;
import task.game.components.TaskComponent;

/** WTF? . */
public class TaskContentDoorOpenerTest {

  private Task task;
  private Entity manager;
  private TaskComponent taskComponent;

  /** Setup method to initialize task, manager, and taskComponent. */
  @BeforeEach
  public void setup() {
    task = new DummyTask();
    manager = new Entity();
    taskComponent = new TaskComponent(task, manager);
  }

  /** WTF? . */
  @Test
  @Disabled
  public void openDoor() {
    // will be fixed in #1030 because there a new way to find doors will be implemented
    LevelGraph taskLevelGraph = LevelGraphGenerator.generate(3);
    LevelGraph nextLevelGraph = LevelGraphGenerator.generate(2);
    LevelGraph.add(nextLevelGraph, taskLevelGraph);
    RoomBasedLevelGenerator.level(taskLevelGraph, DesignLabel.DEFAULT);
    DoorTile door = null; // = GeneratorUtils.doorAt(tuple.a().level(), tuple.b()).orElseThrow();
    door.close();
    DoorComponent dc = new DoorComponent(Set.of(door));
    manager.add(dc);
    taskComponent.onActivate(TaskComponent.DOOR_OPENER);
    task.state(Task.TaskState.ACTIVE);
    assertTrue(door.isOpen());
  }

  private static class DummyTask extends Task {
    @Override
    public String correctAnswersAsString() {
      return null;
    }
  }
}
