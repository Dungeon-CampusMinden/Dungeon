package petriNet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import graph.petrinet.Place;
import graph.petrinet.Transition;
import org.junit.Test;
import org.mockito.Mockito;
import task.Task;

public class PlaceTest {

  @Test
  public void placeToken() {
    Place place = new Place();
    assertEquals(0, place.tokenCount());
    place.placeToken();
    assertEquals(1, place.tokenCount());
    place.placeToken();
    assertEquals(2, place.tokenCount());
  }

  @Test
  public void removeToken() {
    Place place = new Place();
    place.placeToken();
    place.placeToken();
    assertEquals(2, place.tokenCount());
    place.removeToken();
    assertEquals(1, place.tokenCount());
    place.removeToken();
    assertEquals(0, place.tokenCount());
    place.removeToken();
    assertEquals("Token-count should not be negative", 0, place.tokenCount());
  }

  @Test
  public void changeStateOnTokenAdd() {
    Task taskA = new DummyTask();
    Task taskB = new DummyTask();
    Task.TaskState changeATo = Task.TaskState.ACTIVE;
    Task.TaskState changeBTo = Task.TaskState.FINISHED_WRONG;
    Place place = new Place();
    place.changeStateOnTokenAdd(taskA, changeATo);
    place.changeStateOnTokenAdd(taskB, changeBTo);
    assertEquals(Task.TaskState.INACTIVE, taskA.state());
    assertEquals(Task.TaskState.INACTIVE, taskB.state());
    place.placeToken();
    assertEquals(changeATo, taskA.state());
    assertEquals(changeBTo, taskB.state());
  }

  @Test
  public void observe_and_notify() {
    Task taskA = new DummyTask();
    Task taskB = new DummyTask();
    Task.TaskState aAt = Task.TaskState.ACTIVE;
    Task.TaskState bAt = Task.TaskState.FINISHED_WRONG;
    Place place = new Place();
    place.observe(taskA, aAt);
    place.observe(taskB, bAt);
    assertEquals(0, place.tokenCount());
    taskA.state(aAt);
    assertEquals(1, place.tokenCount());
    taskB.state(aAt);
    assertEquals(1, place.tokenCount());
    taskB.state(bAt);
    assertEquals(2, place.tokenCount());
  }

  @Test
  public void cant_observe_and_change() {
    Place place = new Place();
    place.observe(new DummyTask(), Task.TaskState.FINISHED_CORRECT);

    // Use assertThrows to check if RuntimeException is thrown
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              place.changeStateOnTokenAdd(new DummyTask(), Task.TaskState.FINISHED_CORRECT);
            });

    // Optionally, you can check the exception message if needed
    String expectedMessage = "A Place cannot observe and activate Tasks at the same time.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void cant_change_and_observe() {
    Place place = new Place();
    place.changeStateOnTokenAdd(new DummyTask(), Task.TaskState.FINISHED_CORRECT);

    // Use assertThrows to check if RuntimeException is thrown
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              place.observe(new DummyTask(), Task.TaskState.FINISHED_CORRECT);
            });

    // Optionally, you can check the exception message if needed
    String expectedMessage = "A Place cannot observe and activate Tasks at the same time.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void register_and_notify_transition() {
    Place place = new Place();
    Transition transitionA = Mockito.mock(Transition.class);
    Transition transitionB = Mockito.mock(Transition.class);
    place.register(transitionA);
    place.register(transitionB);
    place.placeToken();
    Mockito.verify(transitionA).notify(place);
    Mockito.verify(transitionB).notify(place);
  }

  private static class DummyTask extends Task {
    @Override
    public String correctAnswersAsString() {
      return null;
    }
  }
}
