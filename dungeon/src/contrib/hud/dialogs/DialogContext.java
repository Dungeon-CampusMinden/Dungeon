package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record DialogContext(
    DialogType dialogType, Skin skin, boolean center, Map<String, Object> attributes)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** Compact Constructor ensures immutability and default logic. */
  public DialogContext {
    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }

  @Override
  public Skin skin() {
    return skin != null ? skin : UIUtils.defaultSkin();
  }

  public <T> Optional<T> find(String key, Class<T> type) {
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

  public <T> T require(String key, Class<T> type) {
    return find(key, type)
        .orElseThrow(() -> new DialogCreationException("Missing required attribute '" + key + "'"));
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static final class Builder {
    private Skin skin;
    private DialogType type;
    private boolean center = true;
    private final Map<String, Object> attributes = new HashMap<>();

    private Builder() {}

    private Builder(DialogContext from) {
      this.type = from.dialogType();
      this.skin = from.skin(); // Use accessor to get potentially defaulted skin
      this.attributes.putAll(from.attributes());
    }

    public Builder skin(Skin value) {
      this.skin = value;
      return this;
    }

    public Builder type(DialogType value) {
      this.type = Objects.requireNonNull(value, "type");
      return this;
    }

    public boolean center() {
      return center;
    }

    public Builder put(String key, Object value) {
      Objects.requireNonNull(key, "key");
      if (value == null) {
        attributes.remove(key);
      } else {
        attributes.put(key, value);
      }
      return this;
    }

    public DialogContext build() {
      if (type == null) {
        throw new DialogCreationException("Dialog dialogType must be set");
      }
      return new DialogContext(type, skin, center, attributes);
    }
  }
}
