package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.utils.annotations.NotNull;

/**
 * The InstanceAttribute class represents an attribute of an instance in a 3D mesh. It provides
 * methods for getting the size of the attribute in bytes, and overrides the equals, hashCode, and
 * toString methods.
 */
public class InstanceAttribute {

  public final int bufferIndex;
  public final int numComponents;
  public final DataType dataType;
  public final String name;

  /**
   * Constructs a InstanceAttribute with the specified usage, number of components, GL type, offset,
   * bufferIndex and name.
   *
   * @param bufferIndex the index of the buffer containing the instance attribute
   * @param numComponents the number of components of the instance attribute
   * @param glType the GL type of the instance attribute
   * @param name the name of the instance attribute
   */
  public InstanceAttribute(int bufferIndex, int numComponents, DataType dataType, @NotNull String name) {
    this.bufferIndex = bufferIndex;
    this.numComponents = numComponents;
    this.dataType = dataType;
    this.name = name;
  }

  /**
   * Constructs a InstanceAttribute with the specified usage, number of components, GL type, offset,
   * and name.
   *
   * @param numComponents the number of components of the instance attribute
   * @param glType the GL type of the instance attribute
   * @param name the name of the instance attribute
   */
  public InstanceAttribute(int numComponents, DataType dataType, @NotNull String name) {
    this(0, numComponents, dataType, name);
  }

  /**
   * Returns the size of the attribute in bytes.
   *
   * @return the size of the attribute in bytes
   */
  public int sizeInBytes() {
    return this.dataType.bytes * this.numComponents;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param obj the reference object with which to compare
   * @return true if this object is the same as the obj argument based on the usage, number of
   *     components, GL type, offset, and name; false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InstanceAttribute other)) {
      return false;
    }
    return this.numComponents == other.numComponents
        && this.bufferIndex == other.bufferIndex
        && this.dataType == other.dataType
        && this.name.equals(other.name);
  }

  @Override
  public int hashCode() {
    int result = 43;
    result = 31 * result + this.numComponents;
    result = 31 * result + this.dataType.hashCode();
    result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "InstanceAttribute [NumComponents: %d, DataType: %s, Buffer: %d, Name: %s]",
        this.numComponents, this.dataType.name(), this.bufferIndex, this.name);
  }
}
