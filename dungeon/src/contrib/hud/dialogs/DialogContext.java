package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Transport object describing the data needed to construct a dialog.
 *
 * <p>Acts as a flexible container so dialogs can declare the keys they require while callers can
 * supply arbitrary additional metadata (e.g. for multiplayer synchronisation).
 */
public final class DialogContext {

  private final Skin skin;
  private final String title;
  private final String entityName;
  private final Entity entity;
  private final Map<String, Object> attributes;

  private DialogContext(Builder builder) {
    this.skin = builder.skin;
    this.title = builder.title;
    this.entityName = builder.entityName;
    this.entity = builder.entity;
    this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
  }

  public Skin skin() {
    return skin != null ? skin : UIUtils.defaultSkin();
  }

  public Optional<String> title() {
    return Optional.ofNullable(title);
  }

  public String titleOrDefault(String fallback) {
    return title != null ? title : fallback;
  }

  public Optional<String> entityName() {
    return Optional.ofNullable(entityName);
  }

  public Entity requireEntity() {
    return entity()
        .orElseThrow(() -> new DialogCreationException("DialogContext is missing an entity"));
  }

  public Optional<Entity> entity() {
    return Optional.ofNullable(entity);
  }

  public Map<String, Object> attributes() {
    return attributes;
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
              + "' must be of type "
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

  public DialogContext withEntity(Entity newEntity) {
    if (Objects.equals(this.entity, newEntity)) {
      return this;
    }
    return toBuilder().entity(newEntity).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Skin skin;
    private String title;
    private String entityName;
    private Entity entity;
    private final Map<String, Object> attributes = new HashMap<>();

    private Builder() {}

    private Builder(DialogContext base) {
      this.skin = base.skin;
      this.title = base.title;
      this.entityName = base.entityName;
      this.entity = base.entity;
      this.attributes.putAll(base.attributes);
    }

    public Builder skin(Skin value) {
      this.skin = value;
      return this;
    }

    public Builder title(String value) {
      this.title = value;
      return this;
    }

    public Builder entityName(String value) {
      this.entityName = value;
      return this;
    }

    public Builder entity(Entity value) {
      this.entity = value;
      return this;
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
      if (entity == null && entityName == null) {
        entityName = "dialog_" + System.nanoTime();
      }
      return new DialogContext(this);
    }
  }
}
