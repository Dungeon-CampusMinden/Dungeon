package contrib.utils;

import java.util.function.Supplier;

public abstract class Predicate implements Supplier<Boolean> {

  private final Supplier<Boolean> a;
  private final Supplier<Boolean> b;

  public Predicate(Supplier<Boolean> a, Supplier<Boolean> b) {
    this.a = a;
    this.b = b;
  }
}
