package task;

import static org.junit.Assert.assertTrue;

import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.generator.graphBased.RoombasedLevelGenerator;
import core.level.generator.graphBased.levelGraph.Direction;
import core.level.generator.graphBased.levelGraph.GraphGenerator;
import core.level.generator.graphBased.levelGraph.LevelGraph;
import core.level.generator.graphBased.levelGraph.Node;
import core.level.utils.DesignLabel;
import core.level.utils.GeneratorUtils;
import core.utils.Tuple;

import dslToGame.DoorComponent;

import org.junit.Before;
import org.junit.Test;

public class TaskContentDoorOpenerTest {

    private Task taskA;
    private Entity manager;
    private TaskComponent taskComponent;

    @Before
    public void setup() {
        taskA = new DummyTask();
        manager = new Entity();
        taskComponent = new TaskComponent(taskA);
        manager.addComponent(taskComponent);
        taskA.managerEntity(manager);
    }

    @Test
    public void openDoor() {
        LevelGraph taskALevelGraph = GraphGenerator.generate(3);
        LevelGraph nextLevelGraph = GraphGenerator.generate(2);
        Tuple<Node, Direction> tuple =
                taskALevelGraph.add(nextLevelGraph, taskALevelGraph).orElseThrow();
        RoombasedLevelGenerator.level(taskALevelGraph, DesignLabel.DEFAULT);
        DoorTile door = GeneratorUtils.doorAt(tuple.a().level(), tuple.b()).orElseThrow();
        door.close();
        DoorComponent dc = new DoorComponent(door);
        manager.addComponent(dc);
        taskComponent.onActivate(TaskComponent.DOOR_OPENER);
        taskA.state(Task.TaskState.ACTIVE);
        assertTrue(door.isOpen());
    }

    private static class DummyTask extends Task {}
}
