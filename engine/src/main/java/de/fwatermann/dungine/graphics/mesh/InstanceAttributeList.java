package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.utils.ReadOnlyIterator;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * The InstanceAttributeList class represents a list of InstanceAttributes. This class implements
 * the Iterable interface, allowing it to be used in enhanced for loops.
 */
public class InstanceAttributeList implements Iterable<InstanceAttribute> {

  private static final Logger LOGGER = LogManager.getFormatterLogger();

  /** An array of InstanceAttributes in the list. */
  private final InstanceAttribute[] attributes;

  /** An iterator for the InstanceAttributes in the list. */
  private ReadOnlyIterator<InstanceAttribute> iterator;

  /** The size of the list in bytes. */
  private int sizeInBytes;

  /**
   * Constructs a InstanceAttributeList with the specified InstanceAttributes.
   *
   * @param attributes the InstanceAttributes to be included in the list
   */
  public InstanceAttributeList(InstanceAttribute... attributes) {
    this.attributes = attributes;
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

  /**
   * Binds the attribute pointers of the InstanceAttributes in the list to the specified shader
   * program, vertex array object, and instance data buffer object.
   *
   * @param shaderProgram the shader program to bind the attribute pointers to
   * @param vao the vertex array object to bind the attribute pointers to
   * @param buffers the instance data buffers to bind the attribute pointers to
   */
  public void bindAttribPointers(
      ShaderProgram shaderProgram, int vao, List<InstancedMesh.InstanceDataBuffer> buffers) {
    ThreadUtils.checkMainThread();
    Map<Integer, Integer> offsets = new HashMap<>();
    Map<Integer, Integer> strides = new HashMap<>();
    this.forEach(
        attrib ->
            strides.compute(
                attrib.bufferIndex,
                (k, v) -> v == null ? attrib.sizeInBytes() : v + attrib.sizeInBytes()));
    this.iterator.reset();
    GL33.glBindVertexArray(vao);
    this.forEach(
        attrib -> {
          GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, buffers.get(attrib.bufferIndex).glIBO);
          int loc = shaderProgram.getAttributeLocation(attrib.name);
          if (loc != -1) {
            int remaining = attrib.numComponents;
            int offset = offsets.getOrDefault(attrib.bufferIndex, 0);
            while (remaining > 0) {
              GL33.glEnableVertexAttribArray(loc);
              GL33.glVertexAttribDivisor(loc, 1);
              LOGGER.trace(
                  "Binding instance attribute '%s' (%d) in buffer %d at location %d",
                  attrib.name, attrib.numComponents - remaining, attrib.bufferIndex, loc);
              if (attrib.dataType.isInteger()) {
                GL33.glVertexAttribIPointer(
                    loc,
                    Math.min(remaining, 4),
                    attrib.dataType.glType,
                    strides.get(attrib.bufferIndex),
                    offset + (long) (attrib.numComponents - remaining) * 4);
              } else {
                GL33.glVertexAttribPointer(
                    loc,
                    Math.min(remaining, 4),
                    attrib.dataType.glType,
                    false,
                    strides.get(attrib.bufferIndex),
                    offset + (long) (attrib.numComponents - remaining) * 4);
              }
              remaining -= Math.min(remaining, 4);
              loc++;
            }
            offsets.put(attrib.bufferIndex, offset + attrib.sizeInBytes());
          }
        });
    GL33.glBindVertexArray(0);
    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
  }
}
