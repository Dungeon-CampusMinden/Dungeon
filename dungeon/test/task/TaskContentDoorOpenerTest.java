package task;

import static org.junit.Assert.assertTrue;

import contrib.level.generator.graphBased.LevelGraphGenerator;
import contrib.level.generator.graphBased.RoombasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;

import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import task.components.DoorComponent;
import task.components.TaskComponent;

import java.util.Set;

public class TaskContentDoorOpenerTest {

    private Task task;
    private Entity manager;
    private TaskComponent taskComponent;

    @Before
    public void setup() {
        task = new DummyTask();
        manager = new Entity();
        taskComponent = new TaskComponent(task, manager);
    }

    @Test
    @Ignore
    public void openDoor() {
        // will be fixed in #1030 because there a new way to find doors will be implemented
        LevelGraph taskLevelGraph = LevelGraphGenerator.generate(3);
        LevelGraph nextLevelGraph = LevelGraphGenerator.generate(2);
        taskLevelGraph.add(nextLevelGraph, taskLevelGraph);
        RoombasedLevelGenerator.level(taskLevelGraph, DesignLabel.DEFAULT);
        DoorTile door =
                null; // = GeneratorUtils.doorAt(tuple.a().level(), tuple.b()).orElseThrow();
        door.close();
        DoorComponent dc = new DoorComponent(Set.of(door));
        manager.addComponent(dc);
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
