package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.utils.annotations.NotNull;
import org.lwjgl.opengl.GL33;

/**
 * The InstanceAttribute class represents an attribute of an instance in a 3D mesh. It provides
 * methods for getting the size of the attribute in bytes, and overrides the equals, hashCode, and
 * toString methods.
 */
public class InstanceAttribute {

  public final int numComponents;
  public final int glType;
  protected int offset;
  public final String name;

  /**
   * Constructs a InstanceAttribute with the specified usage, number of components, GL type, offset,
   * and name.
   *
   * @param numComponents the number of components of the instance attribute
   * @param glType the GL type of the instance attribute
   * @param name the name of the instance attribute
   */
  public InstanceAttribute(int numComponents, int glType, @NotNull String name) {
    this.numComponents = numComponents;
    this.glType = glType;
    this.name = name;

    if (this.glType != GL33.GL_FLOAT
        && this.glType != GL33.GL_UNSIGNED_BYTE
        && this.glType != GL33.GL_BYTE
        && this.glType != GL33.GL_UNSIGNED_SHORT
        && this.glType != GL33.GL_SHORT) {
      throw new IllegalArgumentException(
          "Invalid GL type: "
              + this.glType
              + ". Must be one of GL_FLOAT, GL_UNSIGNED_BYTE, GL_BYTE, GL_UNSIGNED_SHORT, or GL_SHORT.");
    }
  }

  /**
   * Returns the size of the attribute in bytes.
   *
   * @return the size of the attribute in bytes
   */
  public int getSizeInBytes() {
    return switch (this.glType) {
      case GL33.GL_FLOAT -> this.numComponents * 4;
      case GL33.GL_UNSIGNED_BYTE, GL33.GL_BYTE -> this.numComponents;
      case GL33.GL_UNSIGNED_SHORT, GL33.GL_SHORT -> this.numComponents * 2;
      default -> throw new IllegalArgumentException("Invalid GL type: " + this.glType);
    };
  }

  /**
   * Get the offset of the attributes in bytes.
   *
   * @return the offset of the attributes in bytes
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
    if (!(obj instanceof InstanceAttribute other)) {
      return false;
    }
    return this.numComponents == other.numComponents
        && this.glType == other.glType
        && this.offset == other.offset
        && this.name.equals(other.name);
  }

  @Override
  public int hashCode() {
    int result = 43;
    result = 31 * result + this.numComponents;
    result = 31 * result + this.glType;
    result = 31 * result + this.offset;
    result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "InstanceAttribute [NumComponents: %d, GLType: %d, Offset: %d, Name: %s]",
        this.numComponents, this.glType, this.offset, this.name);
  }
}
