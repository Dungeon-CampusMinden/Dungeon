package interpreter;

import dungeonFiles.DSLEntryPoint;
import dungeonFiles.DungeonConfig;

import org.junit.Assert;
import org.junit.Test;

import task.Task;

import taskdependencygraph.TaskNode;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TetsDSLEntryPointFinder {

    @Test
    public void testReadEntrtyPoints() {
        List<DSLEntryPoint> entryPoints = new ArrayList<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        URL resource1 = getClass().getClassLoader().getResource("config1.dng");
        assert resource1 != null;
        Path firstPath = null;
        try {
            firstPath = Path.of(resource1.toURI());
            var entryPointsFromFile = finder.getEntryPoints(firstPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        URL resource2 = getClass().getClassLoader().getResource("config2.dng");
        assert resource2 != null;
        Path secondPath = null;
        try {
            secondPath = Path.of(resource2.toURI());
            var entryPointsFromFile = finder.getEntryPoints(secondPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(4, entryPoints.size());

        // TODO: test stored AST-Nodes

        DSLEntryPoint firstEntryPoint = entryPoints.get(0);
        Assert.assertEquals("This is my config 1", firstEntryPoint.displayName());
        Assert.assertEquals(firstPath, firstEntryPoint.file().filePath());

        DSLEntryPoint secondEntryPoint = entryPoints.get(1);
        Assert.assertEquals("my_other_config", secondEntryPoint.displayName());
        Assert.assertEquals(firstPath, secondEntryPoint.file().filePath());

        DSLEntryPoint thirdEntryPoint = entryPoints.get(2);
        Assert.assertEquals("This is my config 2", thirdEntryPoint.displayName());
        Assert.assertEquals(secondPath, thirdEntryPoint.file().filePath());

        DSLEntryPoint forthEntryPoint = entryPoints.get(3);
        Assert.assertEquals("my_completely_other_config", forthEntryPoint.displayName());
        Assert.assertEquals(secondPath, forthEntryPoint.file().filePath());
    }

    @Test
    public void testEntryPointToDungeonConfig() {
        List<DSLEntryPoint> entryPoints = new ArrayList<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        URL resource1 = getClass().getClassLoader().getResource("config1.dng");
        assert resource1 != null;
        Path firstPath = null;
        try {
            firstPath = Path.of(resource1.toURI());
            var entryPointsFromFile = finder.getEntryPoints(firstPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        URL resource2 = getClass().getClassLoader().getResource("config2.dng");
        assert resource2 != null;
        Path secondPath = null;
        try {
            secondPath = Path.of(resource2.toURI());
            var entryPointsFromFile = finder.getEntryPoints(secondPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        DSLInterpreter interpreter = new DSLInterpreter();

        DSLEntryPoint firstEntryPoint = entryPoints.get(0);
        DungeonConfig config = interpreter.interpretEntryPoint(firstEntryPoint);
        Assert.assertEquals("This is my config 1", config.displayName());
        TaskNode taskNode = config.dependencyGraph().nodeIterator().next();
        Task task = taskNode.task();
        Assert.assertEquals("Task1", task.taskText());

        DSLEntryPoint secondEntryPoint = entryPoints.get(1);
        config = interpreter.interpretEntryPoint(secondEntryPoint);
        Assert.assertEquals("my_other_config", config.displayName());
        taskNode = config.dependencyGraph().nodeIterator().next();
        task = taskNode.task();
        Assert.assertEquals("Task2", task.taskText());

        DSLEntryPoint thirdEntryPoint = entryPoints.get(2);
        config = interpreter.interpretEntryPoint(thirdEntryPoint);
        Assert.assertEquals("This is my config 2", config.displayName());
        taskNode = config.dependencyGraph().nodeIterator().next();
        task = taskNode.task();
        Assert.assertEquals("Kuckuck1", task.taskText());

        DSLEntryPoint forthEntryPoint = entryPoints.get(3);
        config = interpreter.interpretEntryPoint(forthEntryPoint);
        Assert.assertEquals("my_completely_other_config", config.displayName());
        taskNode = config.dependencyGraph().nodeIterator().next();
        task = taskNode.task();
        Assert.assertEquals("Kuckuck2", task.taskText());
    }
}
