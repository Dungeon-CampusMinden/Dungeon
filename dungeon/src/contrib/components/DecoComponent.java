package contrib.components;

import contrib.entities.deco.Deco;
import core.Component;

public class DecoComponent implements Component {

  private final Deco type;

  public DecoComponent(Deco type) {
    this.type = type;
  }

  public Deco type() {
    return type;
  }
}
