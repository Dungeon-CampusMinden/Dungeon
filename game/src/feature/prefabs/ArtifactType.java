package feature.prefabs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Type of the values passed between prefabs as artifacts.
 *
 * <p>Values can be passed to a slot of another type if no information is lost: every type can be
 * shown as {@link #TEXT}, and {@link #INTEGER} values can be used as {@link #FLOAT}.
 *
 * @param <T> Java type of the values
 */
public final class ArtifactType<T> {

  /** Truth value. */
  public static final ArtifactType<Boolean> BOOLEAN =
      new ArtifactType<>("boolean", "Boolean", Boolean.class, String::valueOf);

  /** Whole number. */
  public static final ArtifactType<Integer> INTEGER =
      new ArtifactType<>("integer", "Integer", Integer.class, String::valueOf);

  /** Decimal number. */
  public static final ArtifactType<Float> FLOAT =
      new ArtifactType<>("float", "Decimal", Float.class, String::valueOf);

  /** Text, e.g. a code with leading zeros. */
  public static final ArtifactType<String> TEXT =
      new ArtifactType<>("text", "Text", String.class, Function.identity());

  /** A kind of item. */
  public static final ArtifactType<ItemArtifact> ITEM =
      new ArtifactType<>("item", "Item", ItemArtifact.class, ItemArtifact::displayName);

  private static final List<ArtifactType<?>> ALL = List.of(BOOLEAN, INTEGER, FLOAT, TEXT, ITEM);

  private final String id;
  private final String displayName;
  private final Class<T> valueType;
  private final Function<T, String> formatter;

  private ArtifactType(
      String id, String displayName, Class<T> valueType, Function<T, String> formatter) {
    this.id = id;
    this.displayName = displayName;
    this.valueType = valueType;
    this.formatter = formatter;
  }

  /**
   * Returns every artifact type.
   *
   * @return artifact types
   */
  public static List<ArtifactType<?>> all() {
    return ALL;
  }

  /**
   * Returns the stable ID of this type.
   *
   * @return type ID
   */
  public String id() {
    return id;
  }

  /**
   * Returns the editor-facing type name.
   *
   * @return display name
   */
  public String displayName() {
    return displayName;
  }

  /**
   * Returns whether values of this type can be passed to slots of the target type.
   *
   * @param target slot type
   * @return true if the values can be converted without losing information
   */
  public boolean convertibleTo(ArtifactType<?> target) {
    return this == target || target == TEXT || (this == INTEGER && target == FLOAT);
  }

  /**
   * Shows a value of this type as text.
   *
   * @param value value of this type
   * @return text representation
   */
  public String format(Object value) {
    return formatter.apply(valueType.cast(value));
  }

  /**
   * Converts a value of this type to the target type.
   *
   * @param value value of this type
   * @param target target type
   * @param <R> target value type
   * @return the converted value, or empty if the types are not convertible
   */
  public <R> Optional<R> convert(Object value, ArtifactType<R> target) {
    Objects.requireNonNull(target, "target");
    if (value == null || !valueType.isInstance(value) || !convertibleTo(target)) {
      return Optional.empty();
    }
    if (this == target) return Optional.of(target.valueType.cast(value));
    if (target == TEXT) return Optional.of(target.valueType.cast(format(value)));
    return Optional.of(target.valueType.cast(((Integer) value).floatValue()));
  }

  @Override
  public String toString() {
    return id;
  }
}
