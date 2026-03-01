package core.platform.gdx.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import core.utils.components.draw.TextureMap;
import core.platform.gdx.render.shader.ShaderList;
import core.utils.components.path.SimpleIPath;

public final class GdxShaderTextureRenderer {
  private GdxShaderTextureRenderer() {}

  // Caller must dispose Pixmap.
  public static Pixmap renderToPixmap(String baseTexturePath, ShaderList shaders) {
    Texture base = TextureMap.instance().textureAt(new SimpleIPath(baseTexturePath));
    if (base == null) return null;

    TextureRegion region = new TextureRegion(base);
    FrameBuffer fbo = DrawSystem.getInstance().processShaders(region, shaders);
    if (fbo == null) return null;

    fbo.begin();
    try {
      return Pixmap.createFromFrameBuffer(0, 0, fbo.getWidth(), fbo.getHeight());
    } finally {
      fbo.end();
      // Return FBO to pool after rendering
      FrameBufferPool.getInstance().free(fbo);
    }
  }
}
