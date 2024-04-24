package dsl.runtime;

/** Cloneable objects must implement this interface. */
public interface IClonable {
  /**
   * @return cloned Object
   */
  Object clone();
}
