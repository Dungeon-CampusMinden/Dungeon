package de.fwatermann.dungine.graphics.mesh;

/**
 * The VertexAttribute class represents an attribute of a vertex in a 3D mesh. It provides methods
 * for getting the size of the attribute in bytes, and overrides the equals, hashCode, and toString
 * methods.
 */
public class VertexAttribute {

  /** The number of Components this attribute has. */
  public final int numComponents;

  /** The data type of this attribute. */
  public final DataType dataType;

  /** The name of this attribute. */
  public final String name;

  /** The offset of this attribute. */
  protected int offset;

  /**
   * Constructs a VertexAttribute with the specified usage, number of components, GL type, offset,
   * and name.
   *
   * @param numComponents the number of components of the vertex attribute
   * @param dataType the data type of the vertex attribute
   * @param name the name of the vertex attribute
   */
  public VertexAttribute(int numComponents, DataType dataType, String name) {
    this.numComponents = numComponents;
    this.dataType = dataType;
    this.name = name;
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
   * Sets the offset of the attribute.
   *
   * @return the offset of the attribute
   */
  public int offset() {
    return this.offset;
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
    if (!(obj instanceof VertexAttribute other)) {
      return false;
    }
    return this.dataType == other.dataType
        && this.numComponents == other.numComponents
        && this.offset == other.offset
        && this.name.equals(other.name);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + this.numComponents;
    result = 31 * result + this.dataType.hashCode();
    result = 31 * result + this.offset;
    result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "VertexAttribute [NumComponents: %d, DataType: %s, Offset: %d, Name: %s]",
        this.numComponents, this.dataType.name(), this.offset, this.name);
  }
}
