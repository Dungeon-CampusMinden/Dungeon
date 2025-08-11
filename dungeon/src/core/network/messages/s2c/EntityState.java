package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
import java.io.Serial;
import java.util.Optional;

/**
 * Shared compact state representation for an entity.
 *
 * <p>Expected max size (per entity): ~32-64 bytes.
 */
public final class EntityState implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  private final String entityName;
  private final Point position;
  private final String viewDirection;
  private final Integer curHealth;
  private final Integer maxHealth;
  private final String animation;
  private final Integer tintColor;

  private EntityState(Builder builder) {
    this.entityName = builder.entityName;
    this.position = builder.position;
    this.viewDirection = builder.viewDirection;
    this.curHealth = builder.curHealth;
    this.maxHealth = builder.maxHealth;
    this.animation = builder.animation;
    this.tintColor = builder.tintColor;
  }

  public String entityName() {
    return entityName;
  }

  public Point position() {
    return position;
  }

  public Optional<String> viewDirection() {
    return Optional.ofNullable(viewDirection);
  }

  public Optional<Integer> currentHealth() {
    return Optional.ofNullable(curHealth);
  }

  public Optional<Integer> maxHealth() {
    return Optional.ofNullable(maxHealth);
  }

  public Optional<String> animation() {
    return Optional.ofNullable(animation);
  }

  public Optional<Integer> tintColor() {
    return Optional.ofNullable(tintColor);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String entityName;
    private Point position;
    private String viewDirection;
    private Integer curHealth;
    private Integer maxHealth;
    private String animation;
    private Integer tintColor;

    public Builder entityName(String entityName) {
      this.entityName = entityName;
      return this;
    }

    public Builder position(Point position) {
      this.position = position;
      return this;
    }

    public Builder viewDirection(Direction viewDirection) {
      this.viewDirection = viewDirection == null ? null : viewDirection.name();
      return this;
    }

    public Builder viewDirection(String viewDirection) {
      this.viewDirection = viewDirection;
      return this;
    }

    public Builder currentHealth(Integer health) {
      this.curHealth = health;
      return this;
    }

    public Builder maxHealth(Integer maxHealth) {
      this.maxHealth = maxHealth;
      return this;
    }

    public Builder animation(String animation) {
      this.animation = animation;
      return this;
    }

    public Builder tintColor(Integer tintColor) {
      this.tintColor = tintColor;
      return this;
    }

    public EntityState build() {
      if (position == null) {
        throw new IllegalStateException("Position is required");
      }
      return new EntityState(this);
    }
  }
}
