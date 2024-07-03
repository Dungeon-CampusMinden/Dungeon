package de.fwatermann.dungine.graphics.mesh;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.nio.FloatBuffer;

public class InstancedIndexedMesh extends Mesh {

  public InstancedIndexedMesh(GLUsageHint usageHint, VertexAttribute... attributes) {
    super(usageHint, attributes);
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
  public void setVertexBuffer(FloatBuffer buffer) {}

  @Override
  protected void calcBoundingBox() {}

  @Override
  protected void calcTransformMatrix() {}

  @Override
  public void render(
      ShaderProgram shaderProgram, int primitiveType, int offset, int count, boolean bindShader) {}

  @Override
  public void dispose() {}
}
