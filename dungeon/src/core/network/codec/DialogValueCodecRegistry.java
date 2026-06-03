package core.network.codec;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for {@link DialogValueCodec} instances.
 *
 * <p>Provides encode-side lookup by Java class and decode-side lookup by type ID string. Access the
 * shared instance via {@link #global()}.
 */
public final class DialogValueCodecRegistry {
  private static volatile DialogValueCodecRegistry globalInstance;

  private final Map<Class<?>, DialogValueCodec<?>> byType = new ConcurrentHashMap<>();
  private final Map<String, DialogValueCodec<?>> byTypeId = new ConcurrentHashMap<>();

  /** Creates an empty registry. */
  public DialogValueCodecRegistry() {}

  /**
   * Returns the shared global registry instance.
   *
   * <p>Built-in codecs are registered automatically during first initialization.
   *
   * @return the global registry
   */
  public static DialogValueCodecRegistry global() {
    DialogValueCodecRegistry instance = globalInstance;
    if (instance != null) {
      return instance;
    }
    synchronized (DialogValueCodecRegistry.class) {
      instance = globalInstance;
      if (instance == null) {
        instance = new DialogValueCodecRegistry();
        instance.registerDefaults();
        globalInstance = instance;
      }
    }
    return instance;
  }

  /**
   * Registers a codec.
   *
   * @param codec the codec to register
   * @throws IllegalStateException if a codec for the same type or type ID is already registered
   */
  public void register(DialogValueCodec<?> codec) {
    Objects.requireNonNull(codec, "codec");
    Objects.requireNonNull(codec.typeId(), "codec.typeId()");
    Objects.requireNonNull(codec.type(), "codec.type()");

    DialogValueCodec<?> existingType = byType.putIfAbsent(codec.type(), codec);
    if (existingType != null) {
      throw new IllegalStateException(
          "Duplicate DialogValueCodec for type "
              + codec.type().getName()
              + " (existing: "
              + existingType.getClass().getName()
              + ")");
    }

    DialogValueCodec<?> existingTypeId = byTypeId.putIfAbsent(codec.typeId(), codec);
    if (existingTypeId != null) {
      byType.remove(codec.type(), codec);
      throw new IllegalStateException(
          "Duplicate DialogValueCodec for typeId '"
              + codec.typeId()
              + "' (existing: "
              + existingTypeId.getClass().getName()
              + ")");
    }
  }

  /**
   * Looks up a codec by exact Java class.
   *
   * @param type the class to look up
   * @return the codec if one is registered for the class
   */
  public Optional<DialogValueCodec<?>> byType(Class<?> type) {
    return Optional.ofNullable(byType.get(type));
  }

  /**
   * Looks up a codec by type ID string.
   *
   * @param typeId the type discriminator
   * @return the codec if one is registered for the type ID
   */
  public Optional<DialogValueCodec<?>> byTypeId(String typeId) {
    return Optional.ofNullable(byTypeId.get(typeId));
  }

  private void registerDefaults() {
    register(new core.network.codec.codecs.TransitionSpeedCodec());
  }
}
