package rooms.programming.state;

import java.util.Objects;

/** Values carried by the golem after the variable puzzle has been solved. */
public record GolemState(
    String name, int lifeEnergy, double mana, boolean activated, char viewDirection, int steps) {

  /** Creates a validated golem state. */
  public GolemState {
    name = Objects.requireNonNull(name, "name").trim();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (!Double.isFinite(mana)) {
      throw new IllegalArgumentException("mana must be finite");
    }
  }
}
