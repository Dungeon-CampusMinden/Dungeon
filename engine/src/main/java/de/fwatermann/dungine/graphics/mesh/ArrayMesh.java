package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.ShaderProgram;
import de.fwatermann.dungine.utils.BoundingBox;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class ArrayMesh extends Mesh {

  private int glVBO;
  private FloatBuffer vertices;

  private boolean dirty = false;

  public ArrayMesh(float[] vertices) {

  }


  @Override
  public int getVertexCount() {
    return 0;
  }

  @Override
  public FloatBuffer getVertexBuffer() {
    return null;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return null;
  }

  @Override
  public void translate(float x, float y, float z) {

  }

  @Override
  public void setTranslation(float x, float y, float z) {

  }

  @Override
  public void rotate(float x, float y, float z, float angle) {

  }

  @Override
  public void scale(float x, float y, float z) {

  }

  @Override
  public void setScale(float x, float y, float z) {

  }

  @Override
  public void setTransformMatrix(Matrix4f matrix) {

  }

  @Override
  public Matrix4f getTransformMatrix() {
    return null;
  }

  @Override
  public void render(ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {

  }

  @Override
  public void dispose() {

  }
}
