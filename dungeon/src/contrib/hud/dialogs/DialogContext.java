package contrib.hud.dialogs;

import core.Entity;
import core.Game;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Immutable context object that encapsulates all configuration data needed to create a dialog.
 *
 * <p>DialogContext uses a type-safe key-value store pattern to pass arbitrary parameters to dialog
 * creators. This allows flexible dialog configuration without requiring method signature changes
 * when adding new parameters.
 *
 * <p>This class is fully {@link Serializable}. Callbacks are stored separately in {@link
 * contrib.components.UIComponent} and are not part of this context.
 *
 * <p>Instances are created using the {@link Builder} class obtained via {@link #builder()}.
 *
 * @see DialogFactory
 * @see DialogType
 */
public final class DialogContext implements Serializable {
  @Serial private static final long serialVersionUID = 2L;
  private final DialogType dialogType;
  private final boolean center;
  private final Map<String, Serializable> attributes;
  private final String dialogId;

  /**
   * Constructs a new DialogContext with the specified parameters.
   *
   * @param dialogType The type of dialog to create
   * @param center Whether the dialog should be centered on screen
   * @param attributes Map of serializable attributes for dialog configuration (if null, an empty
   *     map is used)
   */
  public DialogContext(
      DialogType dialogType, boolean center, Map<String, Serializable> attributes) {
    this(dialogType, center, attributes, null);
  }

  /**
   * Constructs a new DialogContext with the specified parameters and dialog ID.
   *
   * @param dialogType The type of dialog to create
   * @param center Whether the dialog should be centered on screen
   * @param attributes Map of serializable attributes for dialog configuration (if null, an empty
   *     map is used)
   * @param dialogId The dialog ID to use (if null, a new ID is generated)
   */
  public DialogContext(
      DialogType dialogType,
      boolean center,
      Map<String, Serializable> attributes,
      String dialogId) {
    attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    this.dialogType = dialogType;
    this.center = center;
    this.attributes = attributes;
    this.dialogId = dialogId == null ? "dialog-" + UUID.randomUUID() : dialogId;
  }

  /**
   * Retrieves an attribute value from the context by its key and expected type.
   *
   * @param key The key to look up
   * @param type The expected class type of the value
   * @param <T> The expected type of the value, must be Serializable
   * @return An Optional containing the value if present and of the correct type, empty otherwise
   * @throws DialogCreationException if the value exists but is not of the expected type
   */
  public <T extends Serializable> Optional<T> find(String key, Class<T> type) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(type, "type");
    Object value = attributes.get(key);
    if (value == null) {
      return Optional.empty();
    }
    if (!type.isInstance(value)) {
      throw new DialogCreationException(
          "Attribute '"
              + key
              + "' must be of type "
              + type.getSimpleName()
              + ", got "
              + value.getClass().getSimpleName());
    }
    return Optional.of(type.cast(value));
  }

  /**
   * Retrieves a required attribute value, throwing an exception if not present.
   *
   * @param key The key to look up
   * @param type The expected class type of the value
   * @param <T> The expected type of the value, must be Serializable
   * @return The attribute value
   * @throws DialogCreationException if the attribute is missing or of wrong type
   */
  public <T extends Serializable> T require(String key, Class<T> type) {
    return find(key, type)
        .orElseThrow(() -> new DialogCreationException("Missing required attribute '" + key + "'"));
  }

  /**
   * Retrieves a required entity by looking up its ID from the attributes.
   *
   * @param key The key storing the entity ID
   * @return The Entity instance
   * @throws DialogCreationException if the attribute is missing or the entity is not found
   */
  public Entity requireEntity(String key) {
    int entityId = require(key, Integer.class);
    return Game.findEntityById(entityId)
        .orElseThrow(
            () -> new DialogCreationException("Entity with ID " + entityId + " not found"));
  }

  /**
   * Finds an entity by looking up its ID from the attributes.
   *
   * @param key The key storing the entity ID
   * @return Optional containing the Entity if found
   */
  public Optional<Entity> findEntity(String key) {
    return find(key, Integer.class).flatMap(Game::findEntityById);
  }

  /**
   * Creates a new empty Builder for constructing a DialogContext.
   *
   * @return A new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the dialog type for this context.
   *
   * @return The dialog type
   */
  public DialogType dialogType() {
    return dialogType;
  }

  /**
   * Returns whether the dialog should be centered on screen.
   *
   * @return True if the dialog should be centered, false otherwise
   */
  public boolean center() {
    return center;
  }

  /**
   * Returns an unmodifiable view of the attributes map.
   *
   * @return The attributes map
   */
  public Map<String, Serializable> attributes() {
    return attributes;
  }

  /**
   * Returns the unique identifier for this dialog.
   *
   * @return the dialog ID, or null for local-only dialogs
   */
  public String dialogId() {
    return dialogId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (DialogContext) obj;
    return Objects.equals(this.dialogType, that.dialogType)
        && this.center == that.center
        && Objects.equals(this.attributes, that.attributes)
        && Objects.equals(this.dialogId, that.dialogId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dialogType, center, attributes, dialogId);
  }

  @Override
  public String toString() {
    return "DialogContext["
        + "dialogType="
        + dialogType
        + ", center="
        + center
        + ", attributes="
        + attributes
        + ", dialogId="
        + dialogId
        + ']';
  }

  /**
   * Sets the owner attribute to the given entity ID.
   *
   * @param id The entity ID of the owner
   */
  public void owner(int id) {
    this.attributes.put(DialogContextKeys.OWNER_ENTITY, id);
  }

  /**
   * Retrieves the owner entity from the context.
   *
   * @return The owner Entity
   * @throws DialogCreationException if the owner entity is not set or not found
   */
  public Entity ownerEntity() {
    return requireEntity(DialogContextKeys.OWNER_ENTITY);
  }

  /**
   * Builder class for constructing {@link DialogContext} instances.
   *
   * <p>The builder provides a fluent API for setting dialog parameters. At minimum, a dialog type
   * must be specified using {@link #type(DialogType)}.
   */
  public static final class Builder {
    private DialogType type;
    private boolean center = true;
    private final Map<String, Serializable> attributes = new HashMap<>();
    private String dialogId;

    private Builder() {}

    /**
     * Creates a builder from an existing DialogContext.
     *
     * @param from The context to copy from
     */
    public Builder(DialogContext from) {
      this.type = from.dialogType();
      this.center = from.center();
      this.attributes.putAll(from.attributes());
      this.dialogId = from.dialogId();
    }

    /**
     * Sets the dialog type for this context.
     *
     * @param value The dialog type (must be registered with {@link DialogFactory})
     * @return This builder for method chaining
     * @throws NullPointerException if value is null
     */
    public Builder type(DialogType value) {
      this.type = Objects.requireNonNull(value, "type");
      return this;
    }

    /**
     * Sets whether the dialog should be centered on screen when displayed.
     *
     * @param value True to center the dialog, false otherwise (default is true)
     * @return This builder for method chaining
     */
    public Builder center(boolean value) {
      this.center = value;
      return this;
    }

    /**
     * Sets the dialog ID to use.
     *
     * @param value the dialog ID to use (null to generate a new ID)
     * @return This builder for method chaining
     */
    public Builder dialogId(String value) {
      this.dialogId = value;
      return this;
    }

    /**
     * Stores a key-value pair in the context attributes.
     *
     * @param key The key to store the value under
     * @param value The serializable value to store, or null to remove the key
     * @return This builder for method chaining
     * @throws NullPointerException if key is null
     */
    public Builder put(String key, Serializable value) {
      Objects.requireNonNull(key, "key");
      if (value == null) {
        attributes.remove(key);
      } else {
        attributes.put(key, value);
      }
      return this;
    }

    /**
     * Builds an immutable DialogContext from the current builder state.
     *
     * @return A new DialogContext instance
     * @throws DialogCreationException if no dialog type has been set
     */
    public DialogContext build() {
      if (type == null) {
        throw new DialogCreationException("Dialog type must be set");
      }
      return new DialogContext(type, center, attributes, dialogId);
    }
  }
}
