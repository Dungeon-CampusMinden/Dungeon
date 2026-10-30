package feature.interaction.keypad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import feature.tasks.NumericCodeTask;
import feature.tasks.TaskComponent;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class KeypadFactoryTest {

  private final KeypadComponent component = new KeypadComponent(() -> 3, true);
  private final TaskComponent<String> task = new TaskComponent<>(new NumericCodeTask("123"));
  private final Entity keypad = new Entity();

  KeypadFactoryTest() {
    keypad.add(component);
    keypad.add(task);
  }

  @Test
  void correctCodeCallbackReceivesCaller() {
    Entity caller = new Entity();
    AtomicReference<Entity> receivedCaller = new AtomicReference<>();
    task.onCorrect(receivedCaller::set);

    component.add("123");
    KeypadFactory.submit(keypad, task, caller);

    assertSame(caller, receivedCaller.get());
    assertTrue(component.unlocked());
  }

  @Test
  void wrongCodeCallbackReceivesCaller() {
    Entity caller = new Entity();
    AtomicReference<Entity> receivedCaller = new AtomicReference<>();
    task.onWrong(receivedCaller::set);

    component.add("124");
    KeypadFactory.submit(keypad, task, caller);

    assertSame(caller, receivedCaller.get());
  }

  @Test
  void wrongSubmissionClearsTextAfterWrongCodeCallback() {
    AtomicReference<String> submittedText = new AtomicReference<>();
    task.onWrong(player -> submittedText.set(component.enteredText()));
    component.add("321");

    KeypadFactory.submit(keypad, task, new Entity());

    assertEquals("321", submittedText.get());
    assertTrue(component.enteredText().isEmpty());
  }

  @Test
  void incompleteSubmissionAlsoClearsText() {
    component.add("1");

    KeypadFactory.submit(keypad, task, new Entity());

    assertTrue(component.enteredText().isEmpty());
  }
}
