package contrib.skill;

import core.Entity;
import java.util.function.Function;

public enum Resource {
  HP(entity -> 1);

  private final Function<Entity, Integer> supplier;

  Resource(Function<Entity, Integer> supplier) {
    this.supplier = supplier;
  }

  // if MANA => gucke im ManaComponent nach Mana
  // if Arrow => gucke im InventoryComponent
}
