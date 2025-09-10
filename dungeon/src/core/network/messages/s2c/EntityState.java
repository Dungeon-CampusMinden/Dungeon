package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.state.State;

import java.io.Serial;
import java.util.Optional;

/**
 * Shared compact state representation for an entity.
 *
 * <p>Expected max size (per entity): ~32-64 bytes.
 */
public final class EntityState implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  private final int entityId;
  private final Point position;
  private final String viewDirection;
  private final Float rotation;
  private final Integer curHealth;
  private final Integer maxHealth;
  private final Float curMana;
  private final Float maxMana;
  private final String stateName;
  private final Integer tintColor;

  private EntityState(Builder builder) {
    this.entityId = builder.entityId;
    this.position = builder.position;
    this.viewDirection = builder.viewDirection;
    this.rotation = builder.rotation;
    this.curHealth = builder.curHealth;
    this.maxHealth = builder.maxHealth;
    this.curMana = builder.curMana;
    this.maxMana = builder.maxMana;
    this.stateName = builder.stateName;
    this.tintColor = builder.tintColor;
  }

  public int entityId() {
    return entityId;
  }

  public Point position() {
    return position;
  }

  public Optional<String> viewDirection() {
    return Optional.ofNullable(viewDirection);
  }

  public Optional<Float> rotation() {
    return Optional.ofNullable(rotation);
  }

  public Optional<Integer> currentHealth() {
    return Optional.ofNullable(curHealth);
  }

  public Optional<Integer> maxHealth() {
    return Optional.ofNullable(maxHealth);
  }

  public Optional<Float> currentMana() {
    return Optional.ofNullable(curMana);
  }

  public Optional<Float> maxMana() {
    return Optional.ofNullable(maxMana);
  }

  public Optional<String> stateName() {
    return Optional.ofNullable(stateName);
  }

  public Optional<Integer> tintColor() {
    return Optional.ofNullable(tintColor);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int entityId;
    private Point position;
    private String viewDirection;
    private Float rotation;
    private Integer curHealth;
    private Integer maxHealth;
    private Float curMana;
    private Float maxMana;
    private String stateName;
    private Integer tintColor;

    public Builder entityId(int entityId) {
      this.entityId = entityId;
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

    public Builder rotation(Float rotation) {
      this.rotation = rotation;
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

    public Builder currentMana(Float mana) {
      this.curMana = mana;
      return this;
    }

    public Builder maxMana(Float maxMana) {
      this.maxMana = maxMana;
      return this;
    }

    public Builder stateName(String stateName) {
      this.stateName = stateName;
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
