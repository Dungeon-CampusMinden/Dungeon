package core.network.messages.s2c;

import contrib.item.ItemSnapshot;
import core.network.messages.NetworkMessage;
import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared compact state representation for an entity.
 *
 * <p>This message is used to convey the current state of an entity from the server to the client.
 * It can include various information such as position, health, mana, and other relevant attributes.
 *
 * <p>Subclasses can extend this class and the {@link Builder} to add custom fields for
 * subproject-specific components.
 *
 * @see SnapshotMessage
 * @see core.network.SnapshotTranslator
 */
// TODO: Refactor Builder to use a self-referencing generic pattern (Builder<T extends Builder<T>>)
//       for cleaner inheritance and method chaining in subclasses.
public class EntityState implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  private final int entityId;
  private final String entityName;
  private final Point position;
  private final String viewDirection;
  private final Float rotation;
  private final Integer curHealth;
  private final Integer maxHealth;
  private final Float curMana;
  private final Float maxMana;
  private final Float curStamina;
  private final Float maxStamina;
  private final String stateName;
  private final Integer tintColor;
  private final List<SoundSpec> sounds;
  private final ItemSnapshot[] inventory;

  /**
   * Constructs an EntityState object using the provided Builder.
   *
   * @param builder the Builder containing the entity's state data
   */
  protected EntityState(Builder builder) {
    this.entityId = builder.entityId;
    this.entityName = builder.entityName;
    this.position = builder.position;
    this.viewDirection = builder.viewDirection;
    this.rotation = builder.rotation;
    this.curHealth = builder.curHealth;
    this.maxHealth = builder.maxHealth;
    this.curMana = builder.curMana;
    this.maxMana = builder.maxMana;
    this.curStamina = builder.curStamina;
    this.maxStamina = builder.maxStamina;
    this.stateName = builder.stateName;
    this.tintColor = builder.tintColor;
    this.sounds = builder.sounds;
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
   * Gets the optional current stamina of the entity.
   *
   * @return an Optional containing the current stamina if present, otherwise an empty Optional
   */
  public Optional<Float> currentStamina() {
    return Optional.ofNullable(curStamina);
  }

  /**
   * Gets the optional maximum stamina of the entity.
   *
   * @return an Optional containing the maximum stamina if present, otherwise an empty Optional
   */
  public Optional<Float> maxStamina() {
    return Optional.ofNullable(maxStamina);
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
   * Gets the optional audio list of the entity.
   *
   * @return an Optional containing the audio list if present, otherwise an empty Optional
   */
  public Optional<List<SoundSpec>> sounds() {
    return Optional.ofNullable(sounds);
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
  public Optional<ItemSnapshot[]> inventory() {
    return Optional.ofNullable(inventory);
  }

  /** Builder class for constructing EntityState objects. */
  public static class Builder {
    protected int entityId;
    protected String entityName;
    protected Point position;
    protected String viewDirection;
    protected Float rotation;
    protected Integer curHealth;
    protected Integer maxHealth;
    protected Float curMana;
    protected Float maxMana;
    protected Float curStamina;
    protected Float maxStamina;
    protected String stateName;
    protected Integer tintColor;
    protected List<SoundSpec> sounds;
    protected ItemSnapshot[] inventory;

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
     * Sets the current stamina of the entity.
     *
     * @param stamina the current stamina value
     * @return the Builder instance
     */
    public Builder currentStamina(Float stamina) {
      this.curStamina = stamina;
      return this;
    }

    /**
     * Sets the maximum stamina of the entity.
     *
     * @param maxStamina the maximum stamina value
     * @return the Builder instance
     */
    public Builder maxStamina(Float maxStamina) {
      this.maxStamina = maxStamina;
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
     * Sets the audio list for the entity.
     *
     * @param sounds the list of SoundSpec instances
     * @return the Builder instance
     */
    public Builder sounds(List<SoundSpec> sounds) {
      this.sounds = sounds;
      return this;
    }

    /**
     * Sets the inventory of the entity.
     *
     * @param inventory the array of ItemSnapshot instances
     * @return the Builder instance
     */
    public Builder inventory(ItemSnapshot[] inventory) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityState that = (EntityState) o;
    return entityId == that.entityId
        && Objects.equals(entityName, that.entityName)
        && Objects.equals(position, that.position)
        && Objects.equals(viewDirection, that.viewDirection)
        && Objects.equals(rotation, that.rotation)
        && Objects.equals(curHealth, that.curHealth)
        && Objects.equals(maxHealth, that.maxHealth)
        && Objects.equals(curMana, that.curMana)
        && Objects.equals(maxMana, that.maxMana)
        && Objects.equals(curStamina, that.curStamina)
        && Objects.equals(maxStamina, that.maxStamina)
        && Objects.equals(stateName, that.stateName)
        && Objects.equals(tintColor, that.tintColor)
        && Objects.equals(sounds, that.sounds)
        && inventoriesEqual(inventory, that.inventory);
  }

  /**
   * Compares two inventory arrays for semantic equality.
   *
   * <p>Two ItemSnapshots are considered equal if they have the same item class and stack size.
   *
   * @param inv1 first inventory array
   * @param inv2 second inventory array
   * @return true if the inventories are semantically equal
   */
  private static boolean inventoriesEqual(ItemSnapshot[] inv1, ItemSnapshot[] inv2) {
    if (inv1 == inv2) return true;
    if (inv1 == null || inv2 == null) return false;
    if (inv1.length != inv2.length) return false;

    for (int i = 0; i < inv1.length; i++) {
      ItemSnapshot a = inv1[i];
      ItemSnapshot b = inv2[i];
      if (a == b) continue;
      if (a == null || b == null) return false;
      if (!Objects.equals(a.itemClass(), b.itemClass())) return false;
      if (a.stackSize() != b.stackSize()) return false;
    }
    return true;
  }

  /**
   * Computes a hash code for an inventory array based on semantic content.
   *
   * @param inv the inventory array
   * @return hash code based on item classes and stack sizes
   */
  private static int inventoryHashCode(ItemSnapshot[] inv) {
    if (inv == null) return 0;
    int result = 1;
    for (ItemSnapshot item : inv) {
      if (item == null) {
        result = 31 * result;
      } else {
        result = 31 * result + (item.itemClass() != null ? item.itemClass().hashCode() : 0);
        result = 31 * result + item.stackSize();
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            entityId,
            entityName,
            position,
            viewDirection,
            rotation,
            curHealth,
            maxHealth,
            curMana,
            maxMana,
            curStamina,
            maxStamina,
            stateName,
            tintColor,
            sounds);
    result = 31 * result + inventoryHashCode(inventory);
    return result;
  }
}
