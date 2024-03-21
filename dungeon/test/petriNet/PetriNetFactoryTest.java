package petriNet;

import static org.junit.Assert.assertEquals;

import graph.petrinet.PetriNet;
import graph.petrinet.PetriNetFactory;
import org.junit.Test;
import task.Task;

/** A test class for the PetriNetFactory class. */
public class PetriNetFactoryTest {

  /** WTF? . */
  @Test
  public void defaultNet_correct() {
    Task task = new DummyTask();
    PetriNet net = PetriNetFactory.defaultNet(task);
    net.taskNotActivated().placeToken();
    // task processing should be activated by the petri net
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, task.state());
    // place should have a token
    assertEquals(1, net.processingActivated().tokenCount());

    // finish task
    task.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(0, net.processingActivated().tokenCount());
    assertEquals(1, net.end_correct().tokenCount());
    // assertEquals(Task.TaskState.INACTIVE, task.state());
  }

  /**
   * A test for the defaultNet method with false parameter, checking the behavior of the task and
   * petri net.
   */
  @Test
  public void defaultNet_false() {
    Task task = new DummyTask();
    PetriNet net = PetriNetFactory.defaultNet(task);
    net.taskNotActivated().placeToken();
    // task processing should be activated by the petri net
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, task.state());
    // place should have a token
    assertEquals(1, net.processingActivated().tokenCount());

    // finish task
    task.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(0, net.processingActivated().tokenCount());
    assertEquals(1, net.end_wrong().tokenCount());
    // assertEquals(Task.TaskState.INACTIVE, task.state());
  }

  /** WTF? . */
  @Test
  public void connectSubtaskMandatory() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task sub1 = new DummyTask();
    PetriNet sub1Net = PetriNetFactory.defaultNet(sub1);
    Task sub2 = new DummyTask();
    PetriNet sub2Net = PetriNetFactory.defaultNet(sub2);
    PetriNetFactory.connectSubtaskMandatory(mainNet, sub1Net);
    PetriNetFactory.connectSubtaskMandatory(mainNet, sub2Net);

    sub1Net.taskNotActivated().placeToken();
    sub2Net.taskNotActivated().placeToken();

    // sub tasks should not be activated before main task is activated
    assertEquals(Task.TaskState.INACTIVE, sub1.state());
    assertEquals(Task.TaskState.INACTIVE, sub2.state());

    // activate main task should activate processing of sub tasks
    mainNet.taskNotActivated().placeToken();
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, sub1.state());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, sub2.state());
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(1, sub1Net.processingActivated().tokenCount());

    // finish first task, should wait for second
    sub1.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(0, sub1Net.processingActivated().tokenCount());
    assertEquals(0, mainNet.processingActivated().tokenCount());

    // finish second sub task should activate processing of main task
    sub2.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(0, sub2Net.processingActivated().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, mainTask.state());
    // assertEquals(Task.TaskState.INACTIVE, sub1.state());
    // assertEquals(Task.TaskState.INACTIVE, sub2.state());
  }

  /** WTF? . */
  @Test
  public void connectSubtaskOptional_finished() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task sub1 = new DummyTask();
    PetriNet sub1Net = PetriNetFactory.defaultNet(sub1);
    PetriNetFactory.connectSubtaskOptional(mainNet, sub1Net);

    sub1Net.taskNotActivated().placeToken();

    // sub tasks should not be activated before main task is activated
    assertEquals(Task.TaskState.INACTIVE, sub1.state());

    // activate main task should activate processing of sub tasks and main task
    mainNet.taskNotActivated().placeToken();
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, sub1.state());
    assertEquals(1, sub1Net.processingActivated().tokenCount());

    // finish sub-task, main task should still be active
    sub1.state(Task.TaskState.FINISHED_CORRECT);
    // assertEquals(Task.TaskState.INACTIVE, sub1.state());
    assertEquals(0, sub1Net.processingActivated().tokenCount());
    assertEquals(1, sub1Net.end_correct().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
  }

  /** WTF? . */
  @Test
  public void connectSubtaskOptional_notfinished() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task sub1 = new DummyTask();
    PetriNet sub1Net = PetriNetFactory.defaultNet(sub1);
    PetriNetFactory.connectSubtaskOptional(mainNet, sub1Net);

    sub1Net.taskNotActivated().placeToken();

    // sub tasks should not be activated before main task is activated
    assertEquals(Task.TaskState.INACTIVE, sub1.state());

    // activate main task should activate processing of sub tasks and main task
    mainNet.taskNotActivated().placeToken();
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, sub1.state());
    assertEquals(1, sub1Net.processingActivated().tokenCount());

    // finish maintask, sub task should be inactive
    mainTask.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(1, mainNet.end_correct().tokenCount());

    assertEquals(Task.TaskState.INACTIVE, sub1.state());
    assertEquals(0, sub1Net.end_correct().tokenCount());
    assertEquals(0, sub1Net.end_wrong().tokenCount());
    assertEquals(0, sub1Net.end().tokenCount());
  }

  /** A test method to connect sequences and verify task activation based on pre-task completion. */
  @Test
  public void connectSequenceAnd() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    Task pre2 = new DummyTask();
    PetriNet preNet2 = PetriNetFactory.defaultNet(pre2);
    PetriNetFactory.connectSequenceAnd(mainNet, preNet1);
    PetriNetFactory.connectSequenceAnd(mainNet, preNet2);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();
    preNet2.taskNotActivated().placeToken();

    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish first pre task should not change the state of the main task
    pre1.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(1, preNet1.end_correct().tokenCount());
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish second pre task should activate the main task
    pre2.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(1, preNet2.end_wrong().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, mainTask.state());
  }

  /** WTF? . */
  @Test
  public void connectSequenceOr() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    Task pre2 = new DummyTask();
    PetriNet preNet2 = PetriNetFactory.defaultNet(pre2);
    PetriNetFactory.connectSequenceOr(mainNet, preNet1);
    PetriNetFactory.connectSequenceOr(mainNet, preNet2);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();
    preNet2.taskNotActivated().placeToken();

    assertEquals(0, mainNet.or().tokenCount());
    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish second pre task should activate the main task
    pre2.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(1, preNet2.end_correct().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, mainTask.state());
    assertEquals(1, preNet1.processingActivated().tokenCount());
    assertEquals(0, preNet1.end_wrong().tokenCount());
    assertEquals(0, preNet1.end_correct().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, pre1.state());
  }

  /** WTF? . */
  @Test
  public void connectConditionalFalse_successful() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    PetriNetFactory.connectConditionalFalse(mainNet, preNet1);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();

    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish first task correct should activate this task
    pre1.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(1, preNet1.end_wrong().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, mainTask.state());
    // assertEquals(Task.TaskState.INACTIVE, pre1.state());
  }

  /** WTF? . */
  @Test
  public void connectConditionalFalse_unsuccessful() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    PetriNetFactory.connectConditionalFalse(mainNet, preNet1);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();

    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish first task correct should not activate this task
    pre1.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(1, preNet1.end_correct().tokenCount());
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());
    // assertEquals(Task.TaskState.INACTIVE, pre1.state());
  }

  /** Tests the successful connection of a conditional task to the main task. */
  @Test
  public void connectConditionalCorrect_successful() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    PetriNetFactory.connectConditionalCorrect(mainNet, preNet1);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();

    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish first task correct should activate this task
    pre1.state(Task.TaskState.FINISHED_CORRECT);
    assertEquals(1, preNet1.end_correct().tokenCount());
    assertEquals(1, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.PROCESSING_ACTIVE, mainTask.state());
    // assertEquals(Task.TaskState.INACTIVE, pre1.state());
  }

  /** WTF? . */
  @Test
  public void connectConditionalCorrect_unsuccessful() {
    Task mainTask = new DummyTask();
    PetriNet mainNet = PetriNetFactory.defaultNet(mainTask);
    Task pre1 = new DummyTask();
    PetriNet preNet1 = PetriNetFactory.defaultNet(pre1);
    PetriNetFactory.connectConditionalCorrect(mainNet, preNet1);

    mainNet.taskNotActivated().placeToken();
    preNet1.taskNotActivated().placeToken();

    // no preTask done, main task should not be activated
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());

    // finish first task false should not activate this task
    pre1.state(Task.TaskState.FINISHED_WRONG);
    assertEquals(1, preNet1.end_wrong().tokenCount());
    assertEquals(0, mainNet.processingActivated().tokenCount());
    assertEquals(Task.TaskState.INACTIVE, mainTask.state());
    // assertEquals(Task.TaskState.INACTIVE, pre1.state());
  }

  private static class DummyTask extends Task {
    @Override
    public String correctAnswersAsString() {
      return null;
    }
  }
}
