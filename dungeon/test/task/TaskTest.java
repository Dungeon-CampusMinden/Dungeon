package task;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import core.Entity;
import graph.petrinet.Place;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import task.game.components.TaskComponent;

/** WTF? . */
public class TaskTest {

  private Task task;

  /** WTF? . */
  @Before
  public void setup() {
    task = new DummyTask();
  }

  /** WTF? . */
  @Test
  public void register_and_notify_place() {
    Place onActive = new Place();
    Place onPerfect = new Place();
    Place onBad = new Place();
    onActive.observe(task, Task.TaskState.ACTIVE);
    onPerfect.observe(task, Task.TaskState.FINISHED_CORRECT);
    onBad.observe(task, Task.TaskState.FINISHED_WRONG);

    assertEquals(0, onActive.tokenCount());
    assertEquals(0, onBad.tokenCount());
    assertEquals(0, onPerfect.tokenCount());

    task.state(Task.TaskState.ACTIVE);
    assertEquals(1, onActive.tokenCount());
    assertEquals(0, onBad.tokenCount());
    assertEquals(0, onPerfect.tokenCount());

    task.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(1, onActive.tokenCount());
    assertEquals(1, onBad.tokenCount());
    assertEquals(0, onPerfect.tokenCount());
  }

  /** WTF? . */
  @Test
  public void state_perfect() {
    assertEquals(Task.TaskState.INACTIVE, task.state());
    task.state(Task.TaskState.ACTIVE);
    assertEquals(Task.TaskState.ACTIVE, task.state());
    task.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(Task.TaskState.FINISHED_CORRECT, task.state());
  }

  /** WTF? . */
  @Test
  public void state_bad() {
    assertEquals(Task.TaskState.INACTIVE, task.state());
    task.state(Task.TaskState.ACTIVE);
    assertEquals(Task.TaskState.ACTIVE, task.state());
    task.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(Task.TaskState.FINISHED_WRONG, task.state());
  }

  /** WTF? . */
  @Test
  public void taskText() {
    String newTest = "Test 1";
    task.taskText(newTest);
    assertEquals(newTest, task.taskText());
  }

  /** WTF? . */
  @Test
  public void managerEntity() {
    assertTrue(task.managerEntity().isEmpty());
    Entity e = new Entity();
    assertFalse(task.managerEntity(e));
    new TaskComponent(task, e);
    assertEquals(e, task.managerEntity().get());
  }

  /** WTF? . */
  @Test
  public void notify_managerEntity() {
    Entity e = new Entity();
    final int[] c = {0};
    TaskComponent tc = new TaskComponent(task, e);
    tc.onActivate(entity -> c[0]++);
    task.state(Task.TaskState.ACTIVE);
    assertEquals(1, c[0]);
  }

  /** WTF? . */
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

  /** WTF? . */
  @Test
  public void scoringFunction() {
    BiFunction scoring = Mockito.mock(BiFunction.class);
    task.scoringFunction(scoring);
    Mockito.verifyNoMoreInteractions(scoring);
    assertEquals(scoring, task.scoringFunction());
  }

  /** WTF? . */
  @Test
  public void id() {
    Task t1 = new DummyTask();
    Task t2 = new DummyTask();
    assertTrue(t1.id() != t2.id());
  }

  private static class DummyTask extends Task {
    @Override
    public String correctAnswersAsString() {
      return null;
    }
  }
}
