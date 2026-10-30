package feature.prefabs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Value a prefab type consumes from the {@link ArtifactOutput artifact outputs} of other instances.
 *
 * <p>Artifact slots are input ports: each instance stores the outputs linked to its slots in {@link
 * PrefabInstance#inputs()}. A slot either takes a single value, or any number of values that are
 * told apart by an alias, e.g. for dialog placeholders.
 *
 * <p>A slot can replace a property: while such a slot is linked, the linked value is used instead
 * of the authored property value, which is kept as a fallback for when the link is removed.
 *
 * @param <T> value type
 */
public final class ArtifactSlot<T> implements PrefabPort {

  private final String key;
  private final String displayName;
  private final ArtifactType<T> type;
  private final boolean multiple;
  private final PrefabProperty<T> property;
  private final Predicate<T> validator;
  private final String validationMessage;
  private final PrefabProperty<String> placeholderProperty;

  private ArtifactSlot(
      String key,
      String displayName,
      ArtifactType<T> type,
      boolean multiple,
      PrefabProperty<T> property,
      Predicate<T> validator,
      String validationMessage,
      PrefabProperty<String> placeholderProperty) {
    this.key = PrefabPorts.requireKey(key);
    this.displayName = PrefabPorts.requireText(displayName, "displayName");
    this.type = Objects.requireNonNull(type, "type");
    this.multiple = multiple;
    this.property = property;
    this.validator = Objects.requireNonNull(validator, "validator");
    this.validationMessage = validationMessage;
    this.placeholderProperty = placeholderProperty;
  }

  /**
   * Creates a slot taking one value.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param type accepted value type
   * @param <T> value type
   * @return new slot
   */
  public static <T> ArtifactSlot<T> single(String key, String displayName, ArtifactType<T> type) {
    return new ArtifactSlot<>(key, displayName, type, false, null, value -> true, null, null);
  }

  /**
   * Creates a slot taking any number of values, each named by an alias.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param type accepted value type
   * @param <T> value type
   * @return new slot
   */
  public static <T> ArtifactSlot<T> multiple(String key, String displayName, ArtifactType<T> type) {
    return new ArtifactSlot<>(key, displayName, type, true, null, value -> true, null, null);
  }

  /**
   * Creates a slot that can replace a property value. The slot uses the key and name of the
   * property.
   *
   * @param property replaced property
   * @param type accepted value type
   * @param <T> value type
   * @return new slot
   */
  public static <T> ArtifactSlot<T> forProperty(PrefabProperty<T> property, ArtifactType<T> type) {
    Objects.requireNonNull(property, "property");
    return new ArtifactSlot<>(
        property.key(), property.displayName(), type, false, property, value -> true, null, null);
  }

  /**
   * Returns a copy of this slot that only accepts values passing a check. Rejected values are
   * treated as missing and logged.
   *
   * @param check value check
   * @param message describes accepted values, e.g. "only digits"
   * @return validated slot
   */
  public ArtifactSlot<T> validatedBy(Predicate<T> check, String message) {
    return new ArtifactSlot<>(
        key, displayName, type, multiple, property, check, message, placeholderProperty);
  }

  /**
   * Returns a copy of this multi-value slot whose aliases are used as {@code {alias}} placeholders
   * in a text property. The editor lists the available placeholders below the property.
   *
   * @param text text property containing the placeholders
   * @return slot feeding placeholders
   * @see Placeholders
   */
  public ArtifactSlot<T> withPlaceholdersIn(PrefabProperty<String> text) {
    if (!multiple) throw new IllegalStateException("Only multi-value slots feed placeholders");
    return new ArtifactSlot<>(
        key, displayName, type, multiple, property, validator, validationMessage, text);
  }

  /**
   * Returns the text property using this slot's aliases as placeholders.
   *
   * @return text property, if any
   */
  public Optional<PrefabProperty<String>> placeholderProperty() {
    return Optional.ofNullable(placeholderProperty);
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public String displayName() {
    return displayName;
  }

  @Override
  public Kind kind() {
    return Kind.ARTIFACT_SLOT;
  }

  /**
   * Returns the accepted value type.
   *
   * @return artifact type
   */
  public ArtifactType<T> type() {
    return type;
  }

  /**
   * Returns whether the slot takes any number of aliased values instead of one.
   *
   * @return true for multi-value slots
   */
  public boolean multiple() {
    return multiple;
  }

  /**
   * Returns the property this slot replaces while it is linked.
   *
   * @return replaced property, if any
   */
  public Optional<PrefabProperty<T>> property() {
    return Optional.ofNullable(property);
  }

  /**
   * Returns whether a value is accepted by this slot.
   *
   * @param value converted value
   * @return true if accepted
   */
  public boolean accepts(T value) {
    return value != null && validator.test(value);
  }

  /**
   * Describes the accepted values, if the slot restricts them.
   *
   * @return validation message
   */
  public Optional<String> validationMessage() {
    return Optional.ofNullable(validationMessage);
  }

  @Override
  public String toString() {
    return key;
  }
}
