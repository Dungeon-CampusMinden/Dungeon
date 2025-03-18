package contrib.utils;

import contrib.utils.Predicate;
import java.util.function.Supplier;

public class PredicateFactory {

  public static Predicate and(Supplier<Boolean> a, Supplier<Boolean> b) {
    return new Predicate(a, b) {
      @Override
      public Boolean get() {
        return a.get() && b.get();
      }
    };
  }

  public static Predicate or(Supplier<Boolean> a, Supplier<Boolean> b) {
    return new Predicate(a, b) {
      @Override
      public Boolean get() {
        return a.get() || b.get();
      }
    };
  }

  public static Predicate xor(Supplier<Boolean> a, Supplier<Boolean> b) {
    return new Predicate(a, b) {
      @Override
      public Boolean get() {
        return a.get() ^ b.get();
      }
    };
  }

  public static Predicate is(Supplier<Boolean> a) {
    return new Predicate(a, a) {
      @Override
      public Boolean get() {
        return a.get();
      }
    };
  }

  public static Predicate not(Supplier<Boolean> a) {
    return new Predicate(a, a) {
      @Override
      public Boolean get() {
        return !a.get();
      }
    };
  }
}
