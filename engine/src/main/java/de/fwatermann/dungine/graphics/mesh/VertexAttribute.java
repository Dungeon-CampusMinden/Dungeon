package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

/**
 * The VertexAttribute class represents an attribute of a vertex in a 3D mesh. It provides methods
 * for getting the size of the attribute in bytes, and overrides the equals, hashCode, and toString
 * methods.
 */
public class VertexAttribute {

  public static final class Usage {
    public static final int POSITION = 1;
    public static final int NORMAL = 2;
    public static final int COLOR = 4;
    public static final int COLOR_PACKED = 8;
    public static final int TEXTURE_COORDINATES = 16;
    public static final int TANGENT = 32;
    public static final int BITANGENT = 64;

    /**
     * Returns the default name of the specified usage. If the usage is not recognized, the default
     * name is "a_Attribute" followed by the usage.
     *
     * @param usage the usage of the attribute
     * @return the default name of the specified usage
     */
    public static String getDefaultName(int usage) {
      return switch (usage) {
        case POSITION -> "a_Position";
        case NORMAL -> "a_Normal";
        case COLOR -> "a_Color";
        case COLOR_PACKED -> "a_ColorPacked";
        case TEXTURE_COORDINATES -> "a_TexCoords";
        case TANGENT -> "a_Tangent";
        case BITANGENT -> "a_Bitangent";
        default -> "a_Attribute" + usage;
      };
    }
  }

  /** The Usage enum represents the usage of a vertex attribute. */
  public final int usage;

  public final int numComponents;
  public final int glType;
  protected int offset;
  public final String name;

  /**
   * Constructs a VertexAttribute with the specified usage, number of components, GL type, offset,
   * and name.
   *
   * @param usage the usage of the vertex attribute
   * @param numComponents the number of components of the vertex attribute
   * @param glType the GL type of the vertex attribute
   * @param name the name of the vertex attribute
   */
  public VertexAttribute(int usage, int numComponents, int glType, String name) {
    this.usage = usage;
    this.numComponents = numComponents;
    this.glType = glType;
    this.name = name == null ? Usage.getDefaultName(usage) : name;

    if (this.glType != GL33.GL_FLOAT
      && this.glType != GL33.GL_UNSIGNED_BYTE
      && this.glType != GL33.GL_BYTE
      && this.glType != GL33.GL_UNSIGNED_SHORT
      && this.glType != GL33.GL_SHORT
      && this.glType != GL33.GL_INT
      && this.glType != GL33.GL_UNSIGNED_INT) {
      throw new IllegalArgumentException(
          "Invalid GL type: "
              + this.glType
            + ". Must be one of GL_FLOAT, GL_UNSIGNED_BYTE, GL_BYTE, GL_UNSIGNED_SHORT, GL_SHORT, GL_UNSIGNED_INT or GL_INT.");
    }
  }

  /**
   * Constructs a VertexAttribute with the specified usage, number of components, GL type, and
   * offset. The name of the vertex attribute is set to the default name of the usage.
   *
   * @param usage the usage of the vertex attribute
   * @param numComponents the number of components of the vertex attribute
   * @param glType the GL type of the vertex attribute
   */
  public VertexAttribute(int usage, int numComponents, int glType) {
    this(usage, numComponents, glType, null);
  }

  /**
   * Returns the size of the attribute in bytes.
   *
   * @return the size of the attribute in bytes
   */
  public int getSizeInBytes() {
    return switch (this.glType) {
      case GL33.GL_FLOAT, GL33.GL_INT, GL33.GL_UNSIGNED_INT -> this.numComponents * 4;
      case GL33.GL_UNSIGNED_BYTE, GL33.GL_BYTE -> this.numComponents;
      case GL33.GL_UNSIGNED_SHORT, GL33.GL_SHORT -> this.numComponents * 2;
      default -> throw new IllegalArgumentException("Invalid GL type: " + this.glType);
    };
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
    return this.usage == other.usage
        && this.numComponents == other.numComponents
        && this.glType == other.glType
        && this.offset == other.offset
        && this.name.equals(other.name);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + this.numComponents;
    result = 31 * result + this.glType;
    result = 31 * result + this.offset;
    result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "VertexAttribute [Usage: %s, NumComponents: %d, GLType: %d, Offset: %d, Name: %s]",
        this.usage, this.numComponents, this.glType, this.offset, this.name);
  }
}
