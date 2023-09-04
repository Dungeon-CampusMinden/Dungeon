package task;

import static junit.framework.TestCase.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import core.Entity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import petriNet.Place;

import java.util.List;
import java.util.function.BiFunction;

public class TaskTest {

    private Task task;

    @Before
    public void setup() {
        task = new DummyTask();
    }

    @Test
    public void register_and_notify_place() {
        Place onActive = new Place();
        Place onPerfect = new Place();
        Place onBad = new Place();
        onActive.observe(task, Task.TaskState.ACTIVE);
        onPerfect.observe(task, Task.TaskState.FINISHED_PERFECT);
        onBad.observe(task, Task.TaskState.FINISHED_BAD);

        assertEquals(0, onActive.tokenCount());
        assertEquals(0, onBad.tokenCount());
        assertEquals(0, onPerfect.tokenCount());

        task.state(Task.TaskState.ACTIVE);
        assertEquals(1, onActive.tokenCount());
        assertEquals(0, onBad.tokenCount());
        assertEquals(0, onPerfect.tokenCount());

        task.state(Task.TaskState.FINISHED_BAD);
        assertEquals(1, onActive.tokenCount());
        assertEquals(1, onBad.tokenCount());
        assertEquals(0, onPerfect.tokenCount());
    }

    @Test
    public void state_perfect() {
        assertEquals(Task.TaskState.INACTIVE, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals(Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.FINISHED_PERFECT);
        assertEquals(Task.TaskState.FINISHED_PERFECT, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_PERFECT, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_PERFECT, task.state());
    }

    @Test
    public void state_okay() {
        assertEquals(Task.TaskState.INACTIVE, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals(Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.FINISHED_OKAY);
        assertEquals(Task.TaskState.FINISHED_OKAY, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_OKAY, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_OKAY, task.state());
    }

    @Test
    public void state_bad() {
        assertEquals(Task.TaskState.INACTIVE, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals(Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.ACTIVE, task.state());
        task.state(Task.TaskState.FINISHED_BAD);
        assertEquals(Task.TaskState.FINISHED_BAD, task.state());
        task.state(Task.TaskState.INACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_BAD, task.state());
        task.state(Task.TaskState.ACTIVE);
        assertEquals("Can not change state back.", Task.TaskState.FINISHED_BAD, task.state());
    }

    @Test
    public void taskText() {
        String newTest = "Test 1";
        task.taskText(newTest);
        assertEquals(newTest, task.taskText());
    }

    @Test
    public void managerEntity() {
        assertTrue(task.managerEntity().isEmpty());
        Entity e = new Entity();
        assertFalse(task.managerEntity(e));
        e.addComponent(new TaskComponent(task));
        task.managerEntity(e);
        assertEquals(e, task.managerEntity().get());
    }

    @Test
    public void notify_managerEntity() {
        Entity e = new Entity();
        final int[] c = {0};
        TaskComponent tc = new TaskComponent(task);
        tc.onActivate(entity -> c[0]++);
        e.addComponent(tc);
        task.managerEntity(e);
        task.state(Task.TaskState.ACTIVE);
        assertEquals(1, c[0]);
    }

    @Test
    public void addContent() {
        TaskContent a = Mockito.mock(TaskContent.class);
        TaskContent b = Mockito.mock(TaskContent.class);
        TaskContent c = Mockito.mock(TaskContent.class);
        task.addContent(a);
        task.addContent(b);
        task.addContent(c);
        List<TaskContent> contents = task.contentStream().toList();
        assertTrue(contents.contains(a));
        assertTrue(contents.contains(b));
        assertTrue(contents.contains(c));
    }

    @Test
    public void scoringFunction() {
        BiFunction scoring = Mockito.mock(BiFunction.class);
        task.scoringFunction(scoring);
        Mockito.verifyNoMoreInteractions(scoring);
        assertEquals(scoring, task.scoringFunction());
    }

    private static class DummyTask extends Task {}
}
