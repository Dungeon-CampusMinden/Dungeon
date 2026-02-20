package core.network.messages.s2c;

import contrib.item.Item;
import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Optional;

/**
 * Shared compact state representation for an entity.
 *
 * <p>This message is used to convey the current state of an entity from the server to the client.
 * It can include various information such as position, health, mana, and other relevant attributes.
 *
 * @see SnapshotMessage
 * @see core.network.SnapshotTranslator
 */
public class EntityState implements NetworkMessage {
  private final int entityId;
  private final String entityName;
  private final Point position;
  private final String viewDirection;
  private final Float rotation;
  private final Vector2 scale;
  private final Integer curHealth;
  private final Integer maxHealth;
  private final Float curMana;
  private final Float maxMana;
  private final String stateName;
  private final Integer tintColor;
  private final Item[] inventory;

  /**
   * Constructs an EntityState object using the provided Builder.
   *
   * @param builder the Builder containing the entity's state data
   */
  private EntityState(Builder builder) {
    this.entityId = builder.entityId;
    this.entityName = builder.entityName;
    this.position = builder.position;
    this.viewDirection = builder.viewDirection;
    this.rotation = builder.rotation;
    this.scale = builder.scale;
    this.curHealth = builder.curHealth;
    this.maxHealth = builder.maxHealth;
    this.curMana = builder.curMana;
    this.maxMana = builder.maxMana;
    this.stateName = builder.stateName;
    this.tintColor = builder.tintColor;
    this.inventory = builder.inventory;
  }

  /**
   * Gets the unique identifier of the entity.
   *
   * @return the entity ID
   */
  public int entityId() {
    return entityId;
  }

  /**
   * Gets the optional position of the entity.
   *
   * @return an Optional containing the entity position if present, otherwise an empty Optional
   */
  public Optional<Point> position() {
    return Optional.ofNullable(position);
  }

  /**
   * Gets the optional name of the entity.
   *
   * @return an Optional containing the entity name if present, otherwise an empty Optional
   */
  public Optional<String> entityName() {
    return Optional.ofNullable(entityName);
  }

  /**
   * Gets the optional view direction of the entity.
   *
   * @return an Optional containing the view direction if present, otherwise an empty Optional
   */
  public Optional<String> viewDirection() {
    return Optional.ofNullable(viewDirection);
  }

  /**
   * Gets the optional rotation of the entity.
   *
   * @return an Optional containing the rotation if present, otherwise an empty Optional
   */
  public Optional<Float> rotation() {
    return Optional.ofNullable(rotation);
  }

  /**
   * Gets the optional scale of the entity.
   *
   * @return an Optional containing the scale if present, otherwise an empty Optional
   */
  public Optional<Vector2> scale() {
    return Optional.ofNullable(scale);
  }

  /**
   * Gets the optional current health of the entity.
   *
   * @return an Optional containing the current health if present, otherwise an empty Optional
   */
  public Optional<Integer> currentHealth() {
    return Optional.ofNullable(curHealth);
  }

  /**
   * Gets the optional maximum health of the entity.
   *
   * @return an Optional containing the maximum health if present, otherwise an empty Optional
   */
  public Optional<Integer> maxHealth() {
    return Optional.ofNullable(maxHealth);
  }

  /**
   * Gets the optional current mana of the entity.
   *
   * @return an Optional containing the current mana if present, otherwise an empty Optional
   */
  public Optional<Float> currentMana() {
    return Optional.ofNullable(curMana);
  }

  /**
   * Gets the optional maximum mana of the entity.
   *
   * @return an Optional containing the maximum mana if present, otherwise an empty Optional
   */
  public Optional<Float> maxMana() {
    return Optional.ofNullable(maxMana);
  }

  /**
   * Gets the optional state name of the entity.
   *
   * @return an Optional containing the state name if present, otherwise an empty Optional
   */
  public Optional<String> stateName() {
    return Optional.ofNullable(stateName);
  }

  /**
   * Gets the optional tint color of the entity.
   *
   * @return an Optional containing the tint color if present, otherwise an empty Optional
   */
  public Optional<Integer> tintColor() {
    return Optional.ofNullable(tintColor);
  }

  /**
   * Creates a new Builder instance for constructing an EntityState.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the optional inventory of the entity.
   *
   * @return an Optional containing the inventory if present, otherwise an empty Optional
   */
  public Optional<Item[]> inventory() {
    return Optional.ofNullable(inventory);
  }

  /** Builder class for constructing EntityState objects. */
  public static class Builder {
    private int entityId;
    private String entityName;
    private Point position;
    private String viewDirection;
    private Float rotation;
    private Vector2 scale;
    private Integer curHealth;
    private Integer maxHealth;
    private Float curMana;
    private Float maxMana;
    private String stateName;
    private Integer tintColor;
    private Item[] inventory;

    /**
     * Sets the unique identifier for the entity.
     *
     * @param entityId the entity ID
     * @return the Builder instance
     */
    public Builder entityId(int entityId) {
      this.entityId = entityId;
      return this;
    }

    /**
     * Sets the position of the entity in the game world.
     *
     * @param position the entity's position
     * @return the Builder instance
     */
    public Builder position(Point position) {
      this.position = position;
      return this;
    }

    /**
     * Sets the name of the entity.
     *
     * @param name the entity name
     * @return the Builder instance
     */
    public Builder entityName(String name) {
      this.entityName = name;
      return this;
    }

    /**
     * Sets the direction the entity is facing.
     *
     * @param viewDirection the view direction as a Direction enum
     * @return the Builder instance
     */
    public Builder viewDirection(Direction viewDirection) {
      this.viewDirection = viewDirection == null ? null : viewDirection.name();
      return this;
    }

    /**
     * Sets the rotation of the entity in degrees.
     *
     * @param rotation the rotation value
     * @return the Builder instance
     */
    public Builder rotation(Float rotation) {
      this.rotation = rotation;
      return this;
    }

    /**
     * Sets the scale of the entity.
     *
     * @param scale the scale value
     * @return the Builder instance
     */
    public Builder scale(Vector2 scale) {
      this.scale = scale;
      return this;
    }

    /**
     * Sets the direction the entity is facing as a string.
     *
     * @param viewDirection the view direction as a string
     * @return the Builder instance
     */
    public Builder viewDirection(String viewDirection) {
      this.viewDirection = viewDirection;
      return this;
    }

    /**
     * Sets the current health of the entity.
     *
     * @param health the current health value
     * @return the Builder instance
     */
    public Builder currentHealth(Integer health) {
      this.curHealth = health;
      return this;
    }

    /**
     * Sets the maximum health of the entity.
     *
     * @param maxHealth the maximum health value
     * @return the Builder instance
     */
    public Builder maxHealth(Integer maxHealth) {
      this.maxHealth = maxHealth;
      return this;
    }

    /**
     * Sets the current mana of the entity.
     *
     * @param mana the current mana value
     * @return the Builder instance
     */
    public Builder currentMana(Float mana) {
      this.curMana = mana;
      return this;
    }

    /**
     * Sets the maximum mana of the entity.
     *
     * @param maxMana the maximum mana value
     * @return the Builder instance
     */
    public Builder maxMana(Float maxMana) {
      this.maxMana = maxMana;
      return this;
    }

    /**
     * Sets the name of the entity's current state.
     *
     * @param stateName the state name
     * @return the Builder instance
     */
    public Builder stateName(String stateName) {
      this.stateName = stateName;
      return this;
    }

    /**
     * Sets the tint color applied to the entity.
     *
     * @param tintColor the tint color value
     * @return the Builder instance
     */
    public Builder tintColor(Integer tintColor) {
      this.tintColor = tintColor;
      return this;
    }

    /**
     * Sets the inventory items for the entity.
     *
     * @param inventory the array of Item instances
     * @return the Builder instance
     */
    public Builder inventory(Item[] inventory) {
      this.inventory = inventory;
      return this;
    }

    /**
     * Builds and returns an EntityState object.
     *
     * @return the constructed EntityState object
     * @throws IllegalStateException if the position is not set
     */
    public EntityState build() {
      return new EntityState(this);
    }
  }
}
