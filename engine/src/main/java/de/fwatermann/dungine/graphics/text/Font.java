package de.fwatermann.dungine.graphics.text;

import static org.lwjgl.util.freetype.FreeType.*;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.util.freetype.*;

public class Font {

  /* Default charset. These chars are loaded by default */
  private static final String DEFAULT_CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:?!-_~#\"'&()[]{}<>|/@\\^$€%*+=`´";
  private static final int[] DEFAULT_SIZES = {8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64};
  private static final int DEFAULT_RENDER_MODE = FT_RENDER_MODE_NORMAL;

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

  private final Map<Integer, Map<Character, GlyphInfo>> glyphs = new HashMap<>();
  private final ArrayList<Texture> pages = new ArrayList<Texture>();
  private boolean color = false;

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

  private static FT_Face loadFace(Resource resource) throws IOException {
    ByteBuffer fontFile = resource.readBytes();
    PointerBuffer facePtr = BufferUtils.createPointerBuffer(1);
    int error = FT_New_Memory_Face(FT_LIBRARY, fontFile, 0, facePtr);
    if (error == FT_Err_Unknown_File_Format) {
      throw new RuntimeException("Failed to load font face: Unknown file format");
    } else if (error != 0) {
      throw new RuntimeException("Failed to load font face: " + error);
    }
    return FT_Face.create(facePtr.get(0));
  }

  private static void selectSize(FT_Face face, int preferredSize) {
    int error = FT_Select_Charmap(face, FT_ENCODING_UNICODE);
    if (error != 0) {
      throw new RuntimeException("Failed to select Unicode charmap: " + error);
    }

    if((face.face_flags() & FT_FACE_FLAG_SCALABLE) != 0) { //Face is scalable
      if(face.num_fixed_sizes() > 0) {
        //Check if the preferred size is available
        FT_Bitmap_Size.Buffer buffer = face.available_sizes();
        if(buffer != null) {
          for (int i = 0; i < face.num_fixed_sizes(); i++) {
            if(buffer.get(i).height() == preferredSize) {
              FT_Select_Size(face, i);
              return;
            }
          }
        }
      }
    }
    FT_Set_Char_Size(face, 0, (long) preferredSize * 64, 96, 0); //TODO: Check DPI
  }

  /**
   * Loads a font from the specified resource.
   *
   * <p>The {@link #DEFAULT_CHARSET default charset} and {@link #DEFAULT_SIZES sizes} are used.
   *
   * @param resource
   * @return the font object representing the loaded font
   * @throws IOException
   */
  public static Font load(Resource resource) throws IOException {
    return load(resource, DEFAULT_CHARSET, DEFAULT_SIZES, DEFAULT_RENDER_MODE);
  }

  /**
   * Loads a font from the specified resource.
   * @param resource the resource to load the font from
   * @param charset the chars to load initially
   * @param sizes the sizes to load initially
   * @param ftRenderMode the render mode to use (e.g. {@link FreeType#FT_RENDER_MODE_NORMAL})
   * @return the font object representing the loaded font
   * @throws IOException if an I/O error occurs
   */
  public static Font load(Resource resource, String charset, int[] sizes, int ftRenderMode) throws IOException {

    long start = System.currentTimeMillis();

    initFT();
    Font font = new Font();

    FT_Face face = loadFace(resource);

    // Check if the font is a color font
    if (FT_HAS_COLOR(face)) {
      LOGGER.debug("Font \"{}\" is a color font [Sizes: {}]", face.family_nameString(), face.num_fixed_sizes());
      font.color = true;
    } else {
      LOGGER.debug("Font \"{}\" is a monochrome font", face.family_nameString());
      font.color = false;
    }

    LOGGER.debug(
        "FontFace loaded: \"{}\" with {} faces [color: {}]",
        face.family_nameString(),
        face.num_faces(),
        font.color);

    font.pages.add(createPage());

    int currentX = 0;
    int currentY = 0;
    int rowMaxHeight = 0;

    for(int si = 0; si < sizes.length; si++) {
      selectSize(face, sizes[si]);

      for (int i = 0; i < charset.length(); i++) {
        char c = charset.charAt(i);

        FT_Glyph glyph = loadGlyph(face, font.color, ftRenderMode, c);

        FT_BBox bbox = FT_BBox.create();
        FT_Glyph_Get_CBox(glyph, FT_GLYPH_BBOX_PIXELS, bbox);

        float width = (bbox.xMax() - bbox.xMin());
        float height = (bbox.yMax() - bbox.yMin());

        if (width == 0 || height == 0) {
          continue;
        }

        FT_Bitmap bitmap = face.glyph().bitmap();
        FT_Glyph_Metrics metrics = face.glyph().metrics();

        int pixelWidth = (int) metrics.width() / 64;
        int pixelHeight = (int) metrics.height() / 64;

        switch(bitmap.pixel_mode()) {
          case FT_PIXEL_MODE_LCD:
            pixelWidth = Objects.requireNonNull(bitmap, "Bitmap is null!").width() / 3;
            pixelHeight = bitmap.rows();
            break;
          case FT_PIXEL_MODE_BGRA:
            pixelWidth = Objects.requireNonNull(bitmap, "Bitmap is null!").width() / 4;
            pixelHeight = bitmap.rows();
            break;
          case FT_PIXEL_MODE_GRAY:
            pixelWidth = Objects.requireNonNull(bitmap, "Bitmap is null!").width();
            pixelHeight = bitmap.rows();
            break;
          default:
            throw new RuntimeException("Unsupported pixel mode: " + bitmap.pixel_mode());
        }

        // check if fits on current page
        if (currentX + pixelWidth >= PAGE_SIZE_X) {
          currentX = 0;
          currentY += rowMaxHeight;
          rowMaxHeight = 0;
        }
        if (currentY + pixelHeight >= PAGE_SIZE_Y) {
          currentX = 0;
          currentY = 0;
          rowMaxHeight = 0;
          font.pages.add(createPage());
        }

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, font.pages.getLast().glHandle());

        ByteBuffer targetBuffer = BufferUtils.createByteBuffer(pixelWidth * pixelHeight * 4);

        if(bitmap.pixel_mode() == FT_PIXEL_MODE_LCD) { //RGB //TODO: Fix alignment issues
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if(bmpBuffer != null) {
            LOGGER.debug("Glyph '{}' Mode: LCD", c);
            for(int p = 0; p < bmpBuffer.limit(); p += 3) {
              byte r = bmpBuffer.get(p);
              byte g = bmpBuffer.get(p + 1);
              byte b = bmpBuffer.get(p + 2);
              targetBuffer.put(r);
              targetBuffer.put(g);
              targetBuffer.put(b);
              targetBuffer.put((byte) (r | g | b));
            }
            targetBuffer.flip();
          }
        } else if(bitmap.pixel_mode() == FT_PIXEL_MODE_BGRA) { //BGRA
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if(bmpBuffer != null) {
            LOGGER.debug("Glyph '{}' Mode: BGRA", c);
            for(int p = 0; p < bmpBuffer.limit(); p += 4) {
              byte b = bmpBuffer.get(p);
              byte g = bmpBuffer.get(p + 1);
              byte r = bmpBuffer.get(p + 2);
              byte a = bmpBuffer.get(p + 3);
              targetBuffer.put(r);
              targetBuffer.put(g);
              targetBuffer.put(b);
              targetBuffer.put(a);
            }
            targetBuffer.flip();
          }
        } else if(bitmap.pixel_mode() == FT_PIXEL_MODE_GRAY) { //A
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if(bmpBuffer != null) {
            LOGGER.debug("Glyph '{}' Mode: GRAY", c);
            for(int p = 0; p < bmpBuffer.limit(); p++) {
              byte a = bmpBuffer.get(p);
              targetBuffer.put((byte)255);
              targetBuffer.put((byte)255);
              targetBuffer.put((byte)255);
              targetBuffer.put(a);
            }
            targetBuffer.flip();
          }
        } else {
          throw new RuntimeException("Unsupported pixel mode: " + bitmap.pixel_mode());
        }

        GL33.glTexSubImage2D(
          GL33.GL_TEXTURE_2D,
          0,
          currentX,
          currentY,
          pixelWidth,
          pixelHeight,
          GL33.GL_RGBA,
          GL33.GL_UNSIGNED_BYTE,
          targetBuffer);

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

        GlyphInfo glyphInfo =
            new GlyphInfo(
                c,
                metrics.horiAdvance() / 64.0f,
                metrics.vertAdvance() / 64.0f,
                font.pages.size() - 1,
                currentX,
                currentY,
                pixelWidth,
                pixelHeight,
                false);

        font.glyphs.computeIfAbsent(DEFAULT_SIZES[si], k -> new HashMap<>()).put(c, glyphInfo);

        currentX += pixelWidth;
        rowMaxHeight = Math.max(rowMaxHeight, pixelHeight);
      }
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

    long end = System.currentTimeMillis();
    LOGGER.debug("Font loaded in {}ms", end - start);

    return font;
  }

  private static FT_Glyph loadGlyph(FT_Face face, boolean loadColor, int ftRenderMode, char c) {
    int glyphIndex = FT_Get_Char_Index(face, c);
    int error = FT_Load_Glyph(face, glyphIndex, loadColor ? FT_LOAD_COLOR : FT_LOAD_DEFAULT);
    if (error != 0) {
      throw new RuntimeException("Failed to load glyph for character '" + c + "': " + error);
    }
    if(face.glyph() == null) {
      throw new RuntimeException("Failed to load glyph for character '" + c + "': Glyph is null");
    }

    PointerBuffer glyphPtr = BufferUtils.createPointerBuffer(1);
    error = FT_Get_Glyph(Objects.requireNonNull(face.glyph(), "GlyphSlot is null!"), glyphPtr);
    if (error != 0) {
      throw new RuntimeException("Failed to get glyph for character '" + c + "': " + error);
    }
    FT_Glyph glyph = FT_Glyph.create(glyphPtr.get(0));

    if(glyph.format() == FT_GLYPH_FORMAT_SVG) {
      throw new RuntimeException("SVG glyphs are currently not supported!"); //TODO: Implement SVG support
    }

    if(glyph.format() != FT_GLYPH_FORMAT_BITMAP) {
      error = FT_Glyph_To_Bitmap(glyphPtr, ftRenderMode, null, false);
      if (error != 0) {
        throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
      }
    }

    error = FT_Render_Glyph(face.glyph(), ftRenderMode);
    if (error != 0) {
      throw new RuntimeException("Failed to render glyph for character '" + c + "': " + error);
    }

    return glyph;
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

  private Font() {}

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
