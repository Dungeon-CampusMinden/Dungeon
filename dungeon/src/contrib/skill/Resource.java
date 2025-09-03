package contrib.skill;

import contrib.components.HealthComponent;
import core.Entity;
import core.components.DrawComponent;

import java.util.function.Function;
import java.util.function.Supplier;

public enum Resource {
HP(entity -> 1);

  private final Function<Entity,Integer> supplier;

  Resource(Function<Entity,Integer> supplier) {
    this.supplier = supplier;
  }


  //if MANA => gucke im ManaComponent nach Mana
  //if Arrow => gucke im InventoryComponent
}
