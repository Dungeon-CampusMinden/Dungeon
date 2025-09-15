package contrib.components;

import core.Component;

public final class BombSkillComponent implements Component {

  public enum BombMode {
    PLACE,
    THROW
  }

  public enum BombType {
    TIMED,
    IMPACT
  }

  private BombMode mode = BombMode.PLACE;
  private BombType type = BombType.TIMED;

  public BombMode mode() {
    return mode;
  }

  public void mode(BombMode m) {
    this.mode = m;
  }

  public BombType type() {
    return type;
  }

  public void type(BombType t) {
    this.type = t;
  }
}
