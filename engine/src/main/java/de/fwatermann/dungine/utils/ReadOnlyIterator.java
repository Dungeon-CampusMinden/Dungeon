package de.fwatermann.dungine.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The `ReadOnlyIterator` class provides an iterator that allows read-only access to an array of elements.
 * It implements the `Iterator` interface and does not support element removal.
 *
 * @param <T> the type of elements returned by this iterator
 */
public class ReadOnlyIterator<T> implements Iterator<T> {

  /** The array of elements to iterate over. */
  private final T[] array;

  /** The current position in the array. */
  private int position = 0;

  /**
   * Constructs a new `ReadOnlyIterator` for the specified array.
   *
   * @param array the array of elements to iterate over
   */
  public ReadOnlyIterator(T[] array) {
    this.array = array;
  }

  /**
   * Returns `true` if the iteration has more elements.
   *
   * @return `true` if the iteration has more elements
   */
  @Override
  public boolean hasNext() {
    return this.position < this.array.length;
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration
   * @throws NoSuchElementException if the iteration has no more elements
   */
  @Override
  public T next() {
    if (!this.hasNext()) throw new NoSuchElementException("No more elements in iterator.");
    return this.array[this.position++];
  }

  /**
   * This operation is not supported by this iterator.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove elements from read-only iterator.");
  }

  /**
   * Resets the iterator to the initial position.
   */
  public void reset() {
    this.position = 0;
  }
}
