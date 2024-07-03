package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.utils.ReadOnlyIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The InstanceAttributeList class represents a list of InstanceAttributes. This class implements the
 * Iterable interface, allowing it to be used in enhanced for loops.
 */
public class InstanceAttributeList implements Iterable<InstanceAttribute> {

  /** An array of InstanceAttributes in the list. */
  private final InstanceAttribute[] attributes;

  /** An iterator for the InstanceAttributes in the list. */
  private Iterator<InstanceAttribute> iterator;

  /** The size of the list in bytes. */
  private int sizeInBytes;

  /**
   * Constructs a InstanceAttributeList with the specified InstanceAttributes.
   *
   * @param attributes the InstanceAttributes to be included in the list
   */
  public InstanceAttributeList(InstanceAttribute... attributes) {
    this.attributes = attributes;
    this.calculateOffsets();
  }

  /** Calculates the offsets of the InstanceAttributes in the list. */
  private void calculateOffsets() {
    int offset = 0;
    for (InstanceAttribute attribute : this.attributes) {
      attribute.offset = offset;
      offset += attribute.getSizeInBytes();
    }
    this.sizeInBytes = offset;
  }

  /**
   * Returns the size of the list in bytes.
   *
   * @return the size of the list in bytes
   */
  public int sizeInBytes() {
    return this.sizeInBytes;
  }

  /**
   * Returns the InstanceAttribute at the specified index in the list.
   *
   * @param index the index of the InstanceAttribute to return
   * @return the InstanceAttribute at the specified index in the list
   */
  public InstanceAttribute get(int index) {
    return this.attributes[index];
  }

  /**
   * Returns a list of InstanceAttributes in the list that have the specified usage.
   *
   * @param usage the usage of the InstanceAttributes to return
   * @return a list of InstanceAttributes in the list that have the specified usage
   */
  public List<InstanceAttribute> getByUsage(int usage) {
    return Arrays.stream(this.attributes).filter(attribute -> attribute.usage == usage).toList();
  }

  /**
   * Returns the count of InstanceAttributes in the list.
   *
   * @return the count of InstanceAttributes in the list
   */
  public int count() {
    return this.attributes.length;
  }

  /**
   * Returns an iterator over the InstanceAttributes in the list.
   *
   * @return an iterator over the InstanceAttributes in the list
   */
  @Override
  public Iterator<InstanceAttribute> iterator() {
    if (this.iterator == null) {
      this.iterator = new ReadOnlyIterator<>(this.attributes);
    }
    return this.iterator;
  }
}
