package contrib.modules.keypad;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.Entity;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class KeypadComponentTest {

  @Test
  void checkUnlockRejectsMissingCaller() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});

    assertThrows(NullPointerException.class, () -> component.checkUnlock(null));
  }

  @Test
  void onCorrectCodeRejectsNullRunnable() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});

    assertThrows(NullPointerException.class, () -> component.onCorrectCode((Runnable) null));
  }

  @Test
  void onCorrectCodeRejectsNullConsumer() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});

    assertThrows(
        NullPointerException.class, () -> component.onCorrectCode((Consumer<Entity>) null));
  }

  @Test
  void onWrongCodeRejectsNullRunnable() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});

    assertThrows(NullPointerException.class, () -> component.onWrongCode((Runnable) null));
  }

  @Test
  void onWrongCodeRejectsNullConsumer() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});

    assertThrows(NullPointerException.class, () -> component.onWrongCode((Consumer<Entity>) null));
  }

  @Test
  void correctCodeCallbackReceivesCaller() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});
    Entity caller = new Entity();
    AtomicReference<Entity> receivedCaller = new AtomicReference<>();
    component.onCorrectCode(receivedCaller::set);

    component.addDigit(1);
    component.addDigit(2);
    component.addDigit(3);
    component.checkUnlock(caller);

    assertSame(caller, receivedCaller.get());
  }

  @Test
  void wrongCodeCallbackReceivesCaller() {
    KeypadComponent component = new KeypadComponent(List.of(1, 2, 3), () -> {});
    Entity caller = new Entity();
    AtomicReference<Entity> receivedCaller = new AtomicReference<>();
    component.onWrongCode(receivedCaller::set);

    component.addDigit(1);
    component.addDigit(2);
    component.addDigit(4);
    component.checkUnlock(caller);

    assertSame(caller, receivedCaller.get());
  }
}
