package de.fwatermann.dungine.graphics.text;

import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.freetype.FreeType;

public class Font {

  private static final Logger LOGGER = LogManager.getLogger(Font.class);

  public static Font load(Resource resource, int fontSize) throws IOException {
    ByteBuffer fontFile = resource.readBytes();
    Font font = new Font();

    PointerBuffer libPtr = BufferUtils.createPointerBuffer(1);
    int error = FreeType.FT_Init_FreeType(libPtr);
    if(error != 0) {
      throw new RuntimeException("Failed to initialize FreeType library: " + error);
    }
    long library = libPtr.get(0);

    PointerBuffer facePtr = BufferUtils.createPointerBuffer(1);
    error = FreeType.FT_New_Memory_Face(library, fontFile, 0, facePtr);
    if(error != 0) {
      throw new RuntimeException("Failed to load font face: " + error);
    }

    



    return null;
  }

  private float scale = 1.0f;
  private float ascent = 0.0f;
  private float descent = 0.0f;
  private float lineGap = 0.0f;
  List<ByteBuffer> glyphBitmaps = new ArrayList<>();

  private Font() {}

  public GlyphInfo[] shapeText(String text) {
    return new GlyphInfo[0];
  }

  public static class GlyphInfo {
    public int codepoint;
    public float xAdvance, yAdvance, xOffset, yOffset;

    public GlyphInfo(int codepoint, float xAdvance, float yAdvance, float xOffset, float yOffset) {
      this.codepoint = codepoint;
      this.xAdvance = xAdvance;
      this.yAdvance = yAdvance;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
    }
  }
}
