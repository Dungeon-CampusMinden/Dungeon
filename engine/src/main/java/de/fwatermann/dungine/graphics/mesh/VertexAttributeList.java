package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.ReadOnlyIterator;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * The VertexAttributeList class represents a list of VertexAttributes. This class implements the
 * Iterable interface, allowing it to be used in enhanced for loops.
 */
public class VertexAttributeList implements Iterable<VertexAttribute> {

  private static final Logger LOGGER = LogManager.getFormatterLogger();

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
      offset += attribute.sizeInBytes();
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
    ThreadUtils.checkMainThread();
    GL33.glBindVertexArray(vao);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
    this.forEach(
        attrib -> {
          int loc = shaderProgram.getAttributeLocation(attrib.name);
          if (loc != -1) {
            int remaining = attrib.numComponents;
            while (remaining > 0) {
              GL33.glEnableVertexAttribArray(loc);
              LOGGER.trace("Binding vertex attribute '%s' at location %d", attrib.name, loc);
              if(attrib.dataType.isInteger()) {
                GL33.glVertexAttribIPointer(
                  loc,
                  Math.min(remaining, 4),
                  attrib.dataType.glType,
                  this.sizeInBytes(),
                  attrib.offset
                    + (long) (attrib.numComponents - remaining) * attrib.sizeInBytes());
              } else {
                GL33.glVertexAttribPointer(
                  loc,
                  Math.min(remaining, 4),
                  attrib.dataType.glType,
                  false,
                  this.sizeInBytes(),
                  attrib.offset
                    + (long) (attrib.numComponents - remaining) * attrib.sizeInBytes());
              }
              remaining -= Math.min(remaining, 4);
              loc++;
            }
          }
        });
    GL33.glBindVertexArray(0);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
  }

}
