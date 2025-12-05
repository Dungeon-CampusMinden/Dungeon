package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Immutable context object that encapsulates all configuration data needed to create a dialog.
 *
 * <p>DialogContext uses a type-safe key-value store pattern to pass arbitrary parameters to dialog
 * creators. This allows flexible dialog configuration without requiring method signature changes
 * when adding new parameters.
 *
 * <p>This class implements {@link Serializable}, but note that {@link Skin} and callbacks are
 * transient and will not be serialized.
 *
 * <p>Instances are created using the {@link Builder} class obtained via {@link #builder()}.
 *
 * @see DialogFactory
 * @see DialogType
 */
public final class DialogContext implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
  private final DialogType dialogType;
  private final boolean center;
  private final Map<String, Serializable> attributes;

  // Skin and callbacks are transient as they are not serializable
  private final transient Skin skin;
  private final transient Map<String, Object> callbacks;

  /**
   * Constructs a new DialogContext with the specified parameters.
   *
   * @param dialogType The type of dialog to create
   * @param skin The UI skin to use for rendering, or null to use the default skin
   * @param center Whether the dialog should be centered on screen
   * @param attributes Map of serializable attributes for dialog configuration
   * @param callbacks Map of callback functions for dialog interactions
   */
  public DialogContext(
      DialogType dialogType,
      Skin skin,
      boolean center,
      Map<String, Serializable> attributes,
      Map<String, Object> callbacks) {
    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    callbacks = callbacks == null ? Map.of() : Map.copyOf(callbacks);
    this.dialogType = dialogType;
    this.skin = skin;
    this.center = center;
    this.attributes = attributes;
    this.callbacks = callbacks;
  }

  /**
   * Returns the UI skin for this dialog.
   *
   * @return The configured skin, or the default skin if none was specified
   */
  public Skin skin() {
    return skin != null ? skin : UIUtils.defaultSkin();
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
              + "' must be of dialogType "
              + type.getSimpleName()
              + ", got "
              + value.getClass().getSimpleName());
    }
    return Optional.of(type.cast(value));
  }

  /**
   * Retrieves a callback function from the context by its key and expected type.
   *
   * @param callbackKey The key to look up
   * @param type The expected class type of the callback
   * @param <T> The expected type of the callback
   * @return An Optional containing the callback if present and of the correct type, empty otherwise
   * @throws DialogCreationException if the callback exists but is not of the expected type
   */
  public <T> Optional<T> findCallback(String callbackKey, Class<T> type) {
    Objects.requireNonNull(callbackKey, "callbackKey");
    Objects.requireNonNull(type, "type");
    Object value = callbacks.get(callbackKey);
    if (value == null) {
      return Optional.empty();
    }
    if (!type.isInstance(value)) {
      throw new DialogCreationException(
          "Callback '"
              + callbackKey
              + "' must be of dialogType "
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
   * Retrieves a required callback, throwing an exception if not present.
   *
   * @param callbackKey The key to look up
   * @param type The expected class type of the callback
   * @param <T> The expected type of the callback
   * @return The callback function
   * @throws DialogCreationException if the callback is missing or of wrong type
   */
  public <T> T requireCallback(String callbackKey, Class<T> type) {
    return findCallback(callbackKey, type)
        .orElseThrow(
            () -> new DialogCreationException("Missing required callback '" + callbackKey + "'"));
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
   * Creates a new Builder pre-populated with the values from this context.
   *
   * @return A new Builder instance with this context's values
   */
  public Builder toBuilder() {
    return new Builder(this);
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
   * Returns an unmodifiable view of the callbacks map.
   *
   * @return The callbacks map
   */
  public Map<String, Object> callbacks() {
    return callbacks;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (DialogContext) obj;
    return Objects.equals(this.dialogType, that.dialogType)
        && Objects.equals(this.skin, that.skin)
        && this.center == that.center
        && Objects.equals(this.attributes, that.attributes)
        && Objects.equals(this.callbacks, that.callbacks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dialogType, skin, center, attributes, callbacks);
  }

  @Override
  public String toString() {
    return "DialogContext["
        + "dialogType="
        + dialogType
        + ", "
        + "skin="
        + skin
        + ", "
        + "center="
        + center
        + ", "
        + "attributes="
        + attributes
        + ", "
        + "callbacks="
        + callbacks
        + ']';
  }

  /**
   * Builder class for constructing {@link DialogContext} instances.
   *
   * <p>The builder provides a fluent API for setting dialog parameters and callbacks. At minimum, a
   * dialog type must be specified using {@link #type(DialogType)}.
   */
  public static final class Builder {
    private Skin skin;
    private DialogType type;
    private boolean center = true;
    private final Map<String, Serializable> attributes = new HashMap<>();
    private final Map<String, Object> callbacks = new HashMap<>();

    private Builder() {}

    private Builder(DialogContext from) {
      this.type = from.dialogType();
      this.skin = from.skin(); // Use accessor to get potentially defaulted skin
      this.attributes.putAll(from.attributes());
      this.callbacks.putAll(from.callbacks());
    }

    /**
     * Sets the UI skin for the dialog.
     *
     * @param value The skin to use, or null to use the default skin
     * @return This builder for method chaining
     */
    public Builder skin(Skin value) {
      this.skin = value;
      return this;
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
     * Stores an entity-based callback function in the context.
     *
     * @param callbackKey The key to store the callback under
     * @param callback The callback function that receives an Entity parameter
     * @return This builder for method chaining
     */
    public Builder putCallback(String callbackKey, Function<Entity, Object> callback) {
      return putCallback(callbackKey, (Object) callback);
    }

    /**
     * Stores a callback function in the context.
     *
     * @param callbackKey The key to store the callback under
     * @param callback The callback to store, or null to remove the key
     * @return This builder for method chaining
     * @throws NullPointerException if callbackKey is null
     */
    public Builder putCallback(String callbackKey, Object callback) {
      Objects.requireNonNull(callbackKey, "callbackKey");
      if (callback == null) {
        callbacks.remove(callbackKey);
      } else {
        callbacks.put(callbackKey, callback);
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
        throw new DialogCreationException("Dialog dialogType must be set");
      }
      return new DialogContext(type, skin, center, attributes, callbacks);
    }
  }
}
