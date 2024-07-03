package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.ReadOnlyIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.opengl.GL33;

/**
 * The VertexAttributeList class represents a list of VertexAttributes. This class implements the
 * Iterable interface, allowing it to be used in enhanced for loops.
 */
public class VertexAttributeList implements Iterable<VertexAttribute> {

  /** An array of VertexAttributes in the list. */
  private final VertexAttribute[] attributes;

  /** An iterator for the VertexAttributes in the list. */
  private Iterator<VertexAttribute> iterator;

  /** The size of the list in bytes. */
  private int sizeInBytes;

  /**
   * Constructs a VertexAttributeList with the specified VertexAttributes.
   *
   * @param attributes the VertexAttributes to be included in the list
   */
  public VertexAttributeList(VertexAttribute... attributes) {
    this.attributes = attributes;
    this.calculateOffsets();
  }

  /** Calculates the offsets of the VertexAttributes in the list. */
  private void calculateOffsets() {
    int offset = 0;
    for (VertexAttribute attribute : this.attributes) {
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
   * Returns the VertexAttribute at the specified index in the list.
   *
   * @param index the index of the VertexAttribute to return
   * @return the VertexAttribute at the specified index in the list
   */
  public VertexAttribute get(int index) {
    return this.attributes[index];
  }

  /**
   * Returns a list of VertexAttributes in the list that have the specified usage.
   *
   * @param usage the usage of the VertexAttributes to return
   * @return a list of VertexAttributes in the list that have the specified usage
   */
  public List<VertexAttribute> getByUsage(int usage) {
    return Arrays.stream(this.attributes).filter(attribute -> attribute.usage == usage).toList();
  }

  /**
   * Returns the count of VertexAttributes in the list.
   *
   * @return the count of VertexAttributes in the list
   */
  public int count() {
    return this.attributes.length;
  }

  /**
   * Returns an iterator over the VertexAttributes in the list.
   *
   * @return an iterator over the VertexAttributes in the list
   */
  @Override
  public Iterator<VertexAttribute> iterator() {
    if (this.iterator == null) {
      this.iterator = new ReadOnlyIterator<>(this.attributes);
    }
    return this.iterator;
  }

  public void bindAttribPointers(ShaderProgram shaderProgram, int vao, int vbo) {
    GL33.glBindVertexArray(vao);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
    this.forEach(
        attrib -> {
          int loc = shaderProgram.getAttributeLocation(attrib.name);
          if (loc != -1) {
            GL33.glEnableVertexAttribArray(loc);
            int remaining = attrib.numComponents;
            while (remaining > 0) {
              GL33.glVertexAttribPointer(
                  loc,
                  Math.min(remaining, 4),
                  attrib.glType,
                  false,
                  this.sizeInBytes(),
                  attrib.offset
                      + (long) (attrib.numComponents - remaining) * attrib.getSizeInBytes());
              remaining -= Math.min(remaining, 4);
              loc++;
            }
          }
        });
    GL33.glBindVertexArray(0);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
  }
}
