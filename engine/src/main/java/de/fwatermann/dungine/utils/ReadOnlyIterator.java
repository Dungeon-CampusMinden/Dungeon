package de.fwatermann.dungine.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReadOnlyIterator<T> implements Iterator<T> {

  private final T[] array;
  private int position = 0;

  public ReadOnlyIterator(T[] array) {
    this.array = array;
  }

  @Override
  public boolean hasNext() {
    return this.position < this.array.length;
  }

  @Override
  public T next() {
    if (!this.hasNext()) throw new NoSuchElementException("No more elements in iterator.");
    return this.array[this.position++];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove elements from read-only iterator.");
  }

  public void reset() {
    this.position = 0;
  }

}
