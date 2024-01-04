package dsl.semanticanalysis.typesystem.extension;

/**
 * A {@link IDSLExtensionProperty} enables accessing some named value of type V in means of an
 * instance of type T.
 *
 * @param <T> type of the instance in which to access the property
 * @param <V> type of the value of the property.
 */
public interface IDSLExtensionProperty<T, V> {
  /**
   * "Set" the value of the property, allows arbitrary logic to be used for that.
   *
   * @param instance the instance, in which to set the value.
   * @param valueToSet the value to set.
   */
  void set(T instance, V valueToSet);

  /**
   * "Get" the value of the property, allows arbitrary logic to be used for that.
   *
   * @param instance the instance, in which to get the value
   * @return the retrieved value.
   */
  V get(T instance);
}
