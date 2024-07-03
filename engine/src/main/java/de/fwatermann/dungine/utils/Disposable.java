package de.fwatermann.dungine.utils;

/** Represents a disposable object. */
public interface Disposable extends AutoCloseable {

  /** Disposes the object. */
  void dispose();

  @Override
  default void close() throws Exception {
    this.dispose();
  }
}
