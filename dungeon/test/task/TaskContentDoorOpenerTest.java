package task;

import static org.junit.Assert.assertTrue;

import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.RoombasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.GraphGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.Node;

import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.utils.Tuple;

import org.junit.Before;
import org.junit.Test;

import task.components.DoorComponent;
import task.components.TaskComponent;

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
    public void openDoor() {
        LevelGraph taskLevelGraph = GraphGenerator.generate(3);
        LevelGraph nextLevelGraph = GraphGenerator.generate(2);
        Tuple<Node, Direction> tuple =
                taskLevelGraph.add(nextLevelGraph, taskLevelGraph).orElseThrow();
        RoombasedLevelGenerator.level(taskLevelGraph, DesignLabel.DEFAULT);
        DoorTile door = GeneratorUtils.doorAt(tuple.a().level(), tuple.b()).orElseThrow();
        door.close();
        DoorComponent dc = new DoorComponent(door);
        manager.addComponent(dc);
        taskComponent.onActivate(TaskComponent.DOOR_OPENER);
        task.state(Task.TaskState.ACTIVE);
        assertTrue(door.isOpen());
    }

    private static class DummyTask extends Task {}
}
