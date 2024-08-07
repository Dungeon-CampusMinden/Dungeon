package de.fwatermann.dungine.graphics.text;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.resource.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.util.freetype.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.*;

public class Font {

  private static final Logger LOGGER = LogManager.getLogger(Font.class);
  private static final int PAGE_SIZE_X = 512;
  private static final int PAGE_SIZE_Y = 512;

  private GlyphInfo[] glyphs;
  private final ArrayList<Texture> pages = new ArrayList<Texture>();


  public static Font load(Resource resource, int fontSize) throws IOException {
    ByteBuffer fontFile = resource.readBytes();
    Font font = new Font();

    PointerBuffer libPtr = BufferUtils.createPointerBuffer(1);
    int error = FT_Init_FreeType(libPtr);
    if (error != 0) {
      throw new RuntimeException("Failed to initialize FreeType library: " + error);
    }
    long library = libPtr.get(0);

    PointerBuffer facePtr = BufferUtils.createPointerBuffer(1);
    error = FT_New_Memory_Face(library, fontFile, 0, facePtr);
    if (error == FT_Err_Unknown_File_Format) {
      throw new RuntimeException("Failed to load font face: Unknown file format");
    } else if (error != 0) {
      throw new RuntimeException("Failed to load font face: " + error);
    }
    FT_Face face = FT_Face.create(facePtr.get(0));
    FT_Set_Char_Size(face, (long) fontSize * 64, 0, 96, 0);

    LOGGER.debug("FontFace loaded: \"{}\" with {} faces", face.family_nameString(), face.num_faces());

    StringBuilder chars = new StringBuilder();
    IntBuffer charIndex = BufferUtils.createIntBuffer(1);
    long charCode = FT_Get_First_Char(face, charIndex);
    while (charIndex.get(0) != 0) {
      chars.append(Character.toChars((int) charCode));
      charCode = FT_Get_Next_Char(face, charCode, charIndex);
    }

    font.pages.add(new Texture(512, 512, GL33.GL_RED, null));
    int currentX = 0;
    int currentY = 0;
    int rowMaxHeight = 0;

    font.glyphs = new GlyphInfo[chars.length()];
    for (int i = 0; i < chars.length(); i++) {
      char c = chars.charAt(i);
      int glyphIndex = FT_Get_Char_Index(face, c);
      error = FT_Load_Glyph(face, glyphIndex, FT_LOAD_DEFAULT);
      if (error != 0) {
        throw new RuntimeException("Failed to load glyph for character '" + c + "': " + error);
      }

      PointerBuffer glyphPtr = BufferUtils.createPointerBuffer(1);
      error = FT_Get_Glyph(face.glyph(), glyphPtr);
      if (error != 0) {
        throw new RuntimeException("Failed to get glyph for character '" + c + "': " + error);
      }
      FT_Glyph glyph = FT_Glyph.create(glyphPtr.get(0));

      if(face.glyph().format() != FT_GLYPH_FORMAT_BITMAP) {
        error = FT_Render_Glyph(face.glyph(), FT_RENDER_MODE_NORMAL);
        if(error != 0) {
          throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
        }
      }

      FT_BBox bbox = FT_BBox.create();
      FT_Glyph_Get_CBox(glyph, FT_GLYPH_BBOX_PIXELS, bbox);

      float width = (bbox.xMax() - bbox.xMin());
      float height = (bbox.yMax() - bbox.yMin());

      error = FT_Glyph_To_Bitmap(glyphPtr, FT_RENDER_MODE_NORMAL, null, false);
      if (error != 0) {
        throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
      }

      FT_Bitmap bitmap = face.glyph().bitmap();
      FT_Glyph_Metrics metrics = face.glyph().metrics();

      //check if fits on current page
      if (currentX + bitmap.width() >= PAGE_SIZE_X) {
        currentX = 0;
        currentY += rowMaxHeight;
        rowMaxHeight = 0;
        if (currentY >= PAGE_SIZE_Y) {
          font.pages.add(new Texture(512, 512, GL33.GL_RED, null));
          currentY = 0;

          Texture page = font.pages.get(font.pages.size() - 2);
          ByteBuffer buffer = BufferUtils.createByteBuffer(PAGE_SIZE_X * PAGE_SIZE_Y);
          GL33.glBindTexture(GL33.GL_TEXTURE_2D, page.glHandle());
          GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RED, GL33.GL_UNSIGNED_BYTE, buffer);
          GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
          STBImageWrite.stbi_write_png("font_" + (font.pages.size() - 2) + ".png", PAGE_SIZE_X, PAGE_SIZE_Y, 1, buffer, PAGE_SIZE_X);
        }
      }

      ByteBuffer bitmapPixels = bitmap.buffer(bitmap.width() * bitmap.rows());
      if (bitmapPixels == null) {
        continue;
      }

      GL33.glBindTexture(GL33.GL_TEXTURE_2D, font.pages.get(font.pages.size() - 1).glHandle());
      GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, currentX, currentY, bitmap.width(), bitmap.rows(), GL33.GL_RED, GL33.GL_UNSIGNED_BYTE, bitmapPixels);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

      LOGGER.debug("Glyph: {} [{}x{}] at {}x{} on page {}", c, bitmap.width(), bitmap.rows(), currentX, currentY, font.pages.size() - 1);

      currentX += bitmap.width();
      rowMaxHeight = Math.max(rowMaxHeight, bitmap.rows());


    }

    LOGGER.debug("Font has {} [{}] characters", chars.toString(), chars.length());


    return null;
  }


  private float scale = 1.0f;
  private float ascent = 0.0f;
  private float descent = 0.0f;
  private float lineGap = 0.0f;
  List<ByteBuffer> glyphBitmaps = new ArrayList<>();

  private Font() {
  }

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
