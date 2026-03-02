package core.platform.gdx.render.shader;

import core.Component;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * GDX-only component that stores shader state for an entity.
 *
 * <p>This keeps libGDX shader pipeline types out of engine-agnostic core components.
 */
public final class GdxShaderComponent implements Component, Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final ShaderList shaders;

  public GdxShaderComponent() {
    this(new ShaderList());
  }

  public GdxShaderComponent(final ShaderList shaders) {
    this.shaders = Objects.requireNonNull(shaders, "shaders");
  }

  public ShaderList shaders() {
    return shaders;
  }
}
