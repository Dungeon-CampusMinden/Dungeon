package engine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** Tests for the {@link Res} handle. */
public class ResTest {

  /** A handle created with {@code of} resolves to the given value. */
  @Test
  public void ofResolvesToValue() {
    Res<String> res = Res.of("level");

    assertEquals(Optional.of("level"), res.get());
  }

  /** A handle created with {@code of(null)} behaves like an absent resource. */
  @Test
  public void ofNullIsEmpty() {
    Res<String> res = Res.of(null);

    assertTrue(res.get().isEmpty());
  }

  /** A handle created with {@code empty} never resolves to a value. */
  @Test
  public void emptyIsEmpty() {
    Res<String> res = Res.empty();

    assertTrue(res.get().isEmpty());
  }

  /** Handles that are not backed by the registry never change their version. */
  @Test
  public void fixedHandlesHaveVersionZero() {
    assertEquals(0L, Res.of("level").version());
    assertEquals(0L, Res.empty().version());
  }

  /** A handle resolves the resource on every call and therefore sees replaced values. */
  @Test
  public void getResolvesOnEveryCall() {
    AtomicReference<String> current = new AtomicReference<>("first");
    Res<String> res = () -> Optional.ofNullable(current.get());

    assertEquals(Optional.of("first"), res.get());
    current.set("second");
    assertEquals(Optional.of("second"), res.get());
  }
}
