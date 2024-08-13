package de.fwatermann.dungine.graphics.text;

import static org.lwjgl.util.freetype.FreeType.*;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.util.freetype.*;

public class Font {

  private static final Logger LOGGER = LogManager.getLogger(Font.class);
  private static final int PAGE_SIZE_X = 1024;
  private static final int PAGE_SIZE_Y = 1024;
  private static final ByteBuffer BLACK;
  private static long FT_LIBRARY = 0;

  static {
    BLACK = BufferUtils.createByteBuffer(PAGE_SIZE_X * PAGE_SIZE_Y * 4);
    BLACK.position(0);
    for (int i = 0; i < BLACK.capacity(); i++) {
      BLACK.put(i, (byte) 0);
    }
    BLACK.flip();
  }

  private final Map<Character, GlyphInfo> glyphs = new HashMap<>();
  private final ArrayList<Texture> pages = new ArrayList<Texture>();

  private static void initFT() {
    if (FT_LIBRARY == 0) {
      PointerBuffer libPtr = BufferUtils.createPointerBuffer(1);
      int error = FT_Init_FreeType(libPtr);
      if (error != 0) {
        throw new RuntimeException("Failed to initialize FreeType library: " + error);
      }
      FT_LIBRARY = libPtr.get(0);
    }
  }

  public static Font load(Resource resource, int fontSize) throws IOException {
    initFT();

    ByteBuffer fontFile = resource.readBytes();
    Font font = new Font(fontSize);

    PointerBuffer facePtr = BufferUtils.createPointerBuffer(1);
    int error = FT_New_Memory_Face(FT_LIBRARY, fontFile, 0, facePtr);
    if (error == FT_Err_Unknown_File_Format) {
      throw new RuntimeException("Failed to load font face: Unknown file format");
    } else if (error != 0) {
      throw new RuntimeException("Failed to load font face: " + error);
    }
    FT_Face face = FT_Face.create(facePtr.get(0));

    // Check if the font is a color font
    if (FT_HAS_COLOR(face)) {
      LOGGER.debug("Font \"{}\" is a color font [Sizes: {}]", face.family_nameString(), face.num_fixed_sizes());
      font.color = true;
    } else {
      LOGGER.debug("Font \"{}\" is a monochrome font", face.family_nameString());
      font.color = false;
    }

    error = FT_Select_Charmap(face, FT_ENCODING_UNICODE);
    if (error != 0) {
      throw new RuntimeException("Failed to select Unicode charmap: " + error);
    }
    if (font.color) {
      if(face.num_fixed_sizes() > 0) {
        int best = 0;
        int diff = Integer.MAX_VALUE;
        for (int i = 0; i < face.num_fixed_sizes(); i++) {
          int newDiff = Math.abs(font.pixelSize - face.available_sizes().get(i).height());
          if (newDiff < diff) {
            best = i;
            diff = newDiff;
          }
          LOGGER.debug(
            "Available size: {}x{}",
            face.available_sizes().get(i).width(),
            face.available_sizes().get(i).height());
        }
        error = FT_Select_Size(face, best);
      } else {
        //error = FT_Set_Pixel_Sizes(face, 0, font.pixelSize);
        FT_Set_Char_Size(face, (long) fontSize * 64, 0, 96, 0);
      }
    } else {
      //error = FT_Set_Pixel_Sizes(face, 0, font.pixelSize);
      FT_Set_Char_Size(face, (long) fontSize * 64, 0, 96, 0);
    }

    LOGGER.debug(
        "FontFace loaded: \"{}\" with {} faces [color: {}]",
        face.family_nameString(),
        face.num_faces(),
        font.color);

    StringBuilder chars = new StringBuilder();
    IntBuffer charIndex = BufferUtils.createIntBuffer(1);
    long charCode = FT_Get_First_Char(face, charIndex);
    while (charIndex.get(0) != 0) {
      chars.append(Character.toChars((int) charCode));
      charCode = FT_Get_Next_Char(face, charCode, charIndex);
    }

    font.pages.add(createPage());

    int c_color = 0;
    int c_mono = 0;
    int c_skip_size = 0;
    int c_skip_no_bitmap_c = 0;
    int c_skip_no_bitmap_m = 0;

    int currentX = 0;
    int currentY = 0;
    int rowMaxHeight = 0;

    for (int i = 0; i < chars.length(); i++) {
      char c = chars.charAt(i);
      int glyphIndex = FT_Get_Char_Index(face, c);
      error = FT_Load_Glyph(face, glyphIndex, font.color ? FT_LOAD_COLOR : FT_LOAD_DEFAULT);
      if (error != 0) {
        throw new RuntimeException("Failed to load glyph for character '" + c + "': " + error);
      }

      PointerBuffer glyphPtr = BufferUtils.createPointerBuffer(1);
      error = FT_Get_Glyph(face.glyph(), glyphPtr);
      if (error != 0) {
        throw new RuntimeException("Failed to get glyph for character '" + c + "': " + error);
      }
      FT_Glyph glyph = FT_Glyph.create(glyphPtr.get(0));

      if (face.glyph().format() != FT_GLYPH_FORMAT_BITMAP) {
        error = FT_Render_Glyph(face.glyph(), FT_RENDER_MODE_NORMAL);
        if (error != 0) {
          throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
        }
      }

      if(face.glyph().format() == FT_GLYPH_FORMAT_SVG) {
        throw new RuntimeException("SVG fonts are not supported");
      }

      FT_BBox bbox = FT_BBox.create();
      FT_Glyph_Get_CBox(glyph, FT_GLYPH_BBOX_PIXELS, bbox);

      float width = (bbox.xMax() - bbox.xMin());
      float height = (bbox.yMax() - bbox.yMin());

      if (width == 0 || height == 0) {
        c_skip_size ++;
        continue;
      }

      error = FT_Glyph_To_Bitmap(glyphPtr, FT_RENDER_MODE_NORMAL, null, false);
      if (error != 0) {
        throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
      }

      FT_Bitmap bitmap = face.glyph().bitmap();
      FT_Glyph_Metrics metrics = face.glyph().metrics();

      // check if fits on current page
      if (currentX + bitmap.width() >= PAGE_SIZE_X) {
        currentX = 0;
        currentY += rowMaxHeight;
        rowMaxHeight = 0;
      }
      if (currentY + bitmap.rows() >= PAGE_SIZE_Y) {
        currentX = 0;
        currentY = 0;
        rowMaxHeight = 0;
        font.pages.add(createPage());
      }

      GL33.glBindTexture(GL33.GL_TEXTURE_2D, font.pages.getLast().glHandle());
      boolean colored = false;
      if (bitmap.pixel_mode() == FT_PIXEL_MODE_BGRA) { // Colored font
        ByteBuffer bitmapPixels = bitmap.buffer(bitmap.width() * bitmap.rows() * 4);
        if (bitmapPixels == null) {
          c_skip_no_bitmap_c ++;
          continue;
        }
        for (int j = 0; j < bitmap.width() * bitmap.rows(); j += 4) {
          byte b = bitmapPixels.get(j);
          byte g = bitmapPixels.get(j + 1);
          byte r = bitmapPixels.get(j + 2);
          byte a = bitmapPixels.get(j + 3);
          bitmapPixels.put(j, r);
          bitmapPixels.put(j + 1, g);
          bitmapPixels.put(j + 2, b);
          bitmapPixels.put(j + 3, a);
        }
        GL33.glPixelStorei(GL33.GL_UNPACK_ALIGNMENT, 4);
        GL33.glTexSubImage2D(
            GL33.GL_TEXTURE_2D,
            0,
            currentX,
            currentY,
            bitmap.width(),
            bitmap.rows(),
            GL33.GL_RGBA,
            GL33.GL_UNSIGNED_BYTE,
            bitmapPixels);
        colored = true;
        c_color ++;
      } else {
        ByteBuffer bitmapPixels = bitmap.buffer(bitmap.width() * bitmap.rows());
        ByteBuffer pixels = BufferUtils.createByteBuffer(bitmap.width() * bitmap.rows() * 4);
        if (bitmapPixels == null) {
          c_skip_no_bitmap_m ++;
          continue;
        }
        for (int j = 0; j < bitmap.width() * bitmap.rows(); j++) {
          byte b = bitmapPixels.get(j);
          pixels.put((byte) 255);
          pixels.put((byte) 255);
          pixels.put((byte) 255);
          pixels.put(b);
        }
        pixels.flip();
        GL33.glPixelStorei(GL33.GL_UNPACK_ALIGNMENT, 4);
        GL33.glTexSubImage2D(
            GL33.GL_TEXTURE_2D,
            0,
            currentX,
            currentY,
            bitmap.width(),
            bitmap.rows(),
            GL33.GL_RGBA,
            GL33.GL_UNSIGNED_BYTE,
            pixels);
        c_mono ++;
      }
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

      GlyphInfo glyphInfo =
          new GlyphInfo(
              c,
              metrics.horiAdvance() / 64.0f,
              metrics.vertAdvance() / 64.0f,
              font.pages.size() - 1,
              currentX,
              currentY,
              bitmap.width(),
              bitmap.rows(),
              colored);
      font.glyphs.put(c, glyphInfo);

      currentX += bitmap.width();
      rowMaxHeight = Math.max(rowMaxHeight, bitmap.rows());
    }

    int c = 0;
    for (Texture page : font.pages) {
      ByteBuffer buffer = BufferUtils.createByteBuffer(PAGE_SIZE_X * PAGE_SIZE_Y * 4);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, page.glHandle());
      GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
      STBImageWrite.stbi_write_png(
          String.format("%s_%d.png", face.family_nameString().toLowerCase(), c++),
          PAGE_SIZE_X,
          PAGE_SIZE_Y,
          4,
          buffer,
          PAGE_SIZE_X * 4);
    }

    LOGGER.debug("Font has [c: {} m: {}, c_ss: {}, c_sbc: {} c_sbm: {}] [{}] characters", c_color, c_mono, c_skip_size, c_skip_no_bitmap_c, c_skip_no_bitmap_m, chars.length());

    return font;
  }

  private static Font loadSVGFont(Resource resource, FT_Face face) {



    return null;
  }

  private static Texture createPage() {
    Texture page =
        new Texture(
            PAGE_SIZE_X,
            PAGE_SIZE_Y,
            GL33.GL_RGBA,
            GL33.GL_LINEAR,
            GL33.GL_LINEAR,
            GL33.GL_CLAMP_TO_EDGE,
            GL33.GL_CLAMP_TO_EDGE,
            BLACK);
    BLACK.position(0);
    return page;
  }

  private boolean color = false;
  private int pixelSize = 0;

  private Font(int pixelSize) {
    this.pixelSize = pixelSize;
  }

  public GlyphInfo[] shapeText(String text) {
    return new GlyphInfo[0];
  }

  public static class GlyphInfo {

    public final int codepoint;
    public final float xAdvance, yAdvance;
    public final int page;
    public final int pageX, pageY;
    public final int width, height;
    public final boolean colored;

    public GlyphInfo(
        int codepoint,
        float xAdvance,
        float yAdvance,
        int page,
        int x,
        int y,
        int width,
        int height,
        boolean colored) {
      this.codepoint = codepoint;
      this.xAdvance = xAdvance;
      this.yAdvance = yAdvance;
      this.pageX = x;
      this.pageY = y;
      this.width = width;
      this.height = height;
      this.page = page;
      this.colored = colored;
    }
  }
}
