package de.fwatermann.dungine.graphics.text;

import static org.lwjgl.util.freetype.FreeType.*;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.BoundingBox2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  public static boolean WRITE_GLYPHS_TO_PNG = true;
  public static final int PAGE_SIZE_X = 1024;
  public static final int PAGE_SIZE_Y = 1024;

  /* Default charset. These chars are loaded by default */
  private static final String DEFAULT_CHARSET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:?!-_~#\"'&()[]{}<>|/@\\^$€%*+=`´";
  private static final int[] DEFAULT_SIZES = {
    8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64
  };
  private static final int DEFAULT_LINE_PADDING = 2;
  private static final int DEFAULT_RENDER_MODE = FT_RENDER_MODE_NORMAL;
  private static final Map<Resource, Font> CACHE = new HashMap<>();
  private static final List<Character> WRAPPING_CHARS =
      new ArrayList<>(List.of('.', ',', ':', ';', '!', '?', '-'));
  private static final List<Character> NEWLINE_CHARS = new ArrayList<>(List.of('\n'));
  private static final Map<Character, Float> WHITESPACE_CHARS = new HashMap<>();

  private static final Logger LOGGER = LogManager.getLogger(Font.class);
  private static final ByteBuffer EMPTY;
  private static long FT_LIBRARY = 0;

  static {
    EMPTY = BufferUtils.createByteBuffer(PAGE_SIZE_X * PAGE_SIZE_Y * 4);
    EMPTY.position(0);
    for (int i = 0; i < EMPTY.capacity(); i++) {
      EMPTY.put(i, (byte) 0);
    }
    EMPTY.flip();

    WHITESPACE_CHARS.put(Character.toChars(0x0020)[0], 0.25f);
    WHITESPACE_CHARS.put(Character.toChars(0x00A0)[0], 0.25f);
    WHITESPACE_CHARS.put(Character.toChars(0x2000)[0], 0.50f);
    WHITESPACE_CHARS.put(Character.toChars(0x2001)[0], 1.0f);
    WHITESPACE_CHARS.put(Character.toChars(0x2002)[0], 1.0f);
    WHITESPACE_CHARS.put(Character.toChars(0x2003)[0], 1.0f);
    WHITESPACE_CHARS.put(Character.toChars(0x2004)[0], 0.33f);
    WHITESPACE_CHARS.put(Character.toChars(0x2005)[0], 0.25f);
    WHITESPACE_CHARS.put(Character.toChars(0x2006)[0], 0.166f);
    WHITESPACE_CHARS.put(Character.toChars(0x2009)[0], 0.166f);
    WHITESPACE_CHARS.put(Character.toChars(0x200A)[0], 0.125f);
  }

  private final Map<Integer, Map<Character, GlyphInfo>> glyphs = new HashMap<>();
  private final ArrayList<Texture> pages = new ArrayList<Texture>();
  private boolean color = false;
  private FT_Face face;
  private int renderMode = DEFAULT_RENDER_MODE;
  private int currentX = 0;
  private int currentY = 0;
  private int rowMaxHeight = 0;

  /** Initializes the FreeType library. */
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

  /**
   * Selects the size for the given FreeType face.
   *
   * @param face the FreeType face
   * @param preferredSize the preferred size
   */
  private static void selectSize(FT_Face face, int preferredSize) {
    int error = FT_Select_Charmap(face, FT_ENCODING_UNICODE);
    if (error != 0) {
      throw new RuntimeException("Failed to select Unicode charmap: " + error);
    }

    if ((face.face_flags() & FT_FACE_FLAG_SCALABLE) != 0) { // Face is scalable
      if (face.num_fixed_sizes() > 0) {
        // Check if the preferred size is available
        FT_Bitmap_Size.Buffer buffer = face.available_sizes();
        if (buffer != null) {
          for (int i = 0; i < face.num_fixed_sizes(); i++) {
            if (buffer.get(i).height() == preferredSize) {
              FT_Select_Size(face, i);
              return;
            }
          }
        }
      }
    }
    FT_Set_Char_Size(face, 0, (long) preferredSize * 64, 96, 0); // TODO: Check DPI
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
   *
   * @param resource the resource to load the font from
   * @param charset the chars to load initially
   * @param sizes the sizes to load initially
   * @param ftRenderMode the render mode to use (e.g. {@link FreeType#FT_RENDER_MODE_NORMAL})
   * @return the font object representing the loaded font
   * @throws IOException if an I/O error occurs
   */
  public static Font load(Resource resource, String charset, int[] sizes, int ftRenderMode)
      throws IOException {

    if (CACHE.containsKey(resource)) {
      return CACHE.get(resource);
    }

    long start = System.currentTimeMillis();

    initFT();
    Font font = new Font();

    FT_Face face = loadFace(resource);
    font.face = face;
    font.renderMode = ftRenderMode;

    // Check if the font is a color font
    if (FT_HAS_COLOR(face)) {
      LOGGER.debug(
          "Font \"{}\" is a color font [Sizes: {}]",
          face.family_nameString(),
          face.num_fixed_sizes());
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

    for (int si = 0; si < sizes.length; si++) {
      loadGlyphs(font, face, charset, sizes[si], ftRenderMode);
    }

    if (WRITE_GLYPHS_TO_PNG) {
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
    }

    long end = System.currentTimeMillis();
    LOGGER.debug("Font loaded in {}ms", end - start);

    CACHE.put(resource, font);

    return font;
  }

  /**
   * Loads glyphs for the specified font, face, charset, size, and render mode.
   *
   * @param font the font object to load glyphs into
   * @param face the FreeType face object
   * @param charset the set of characters to load glyphs for
   * @param size the size of the glyphs to load
   * @param ftRenderMode the render mode to use for the glyphs
   */
  private static void loadGlyphs(
      Font font, FT_Face face, String charset, int size, int ftRenderMode) {
    selectSize(face, size);
    if (font.pages.isEmpty()) font.pages.add(createPage());
    for (int i = 0; i < charset.length(); i++) {
      char c = charset.charAt(i);

      FT_Glyph glyph = loadGlyph(face, font.color, ftRenderMode, c);
      if (glyph == null) {
        LOGGER.debug("Glyph for character '{}' not found, skip.", c);
        continue;
      }

      FT_Glyph_Metrics metrics = face.glyph().metrics();

      FT_BBox bbox = FT_BBox.create();
      FT_Glyph_Get_CBox(glyph, FT_GLYPH_BBOX_PIXELS, bbox);

      float width = (bbox.xMax() - bbox.xMin());
      float height = (bbox.yMax() - bbox.yMin());

      if (width > 0 && height > 0) {
        FT_Bitmap bitmap = face.glyph().bitmap();

        int pixelWidth = 0;
        int pixelHeight = 0;

        switch (bitmap.pixel_mode()) {
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
        if (font.currentX + pixelWidth >= PAGE_SIZE_X) {
          font.currentX = 0;
          font.currentY += font.rowMaxHeight;
          font.rowMaxHeight = 0;
        }
        if (font.currentY + pixelHeight >= PAGE_SIZE_Y) {
          font.currentX = 0;
          font.currentY = 0;
          font.rowMaxHeight = 0;
          font.pages.add(createPage());
        }

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, font.pages.getLast().glHandle());

        ByteBuffer targetBuffer = BufferUtils.createByteBuffer(pixelWidth * pixelHeight * 4);

        if (bitmap.pixel_mode() == FT_PIXEL_MODE_LCD) { // RGB //TODO: Fix alignment issues
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if (bmpBuffer != null) {
            for (int p = 0; p < bmpBuffer.limit(); p += 3) {
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
        } else if (bitmap.pixel_mode() == FT_PIXEL_MODE_BGRA) { // BGRA
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if (bmpBuffer != null) {
            for (int p = 0; p < bmpBuffer.limit(); p += 4) {
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
        } else if (bitmap.pixel_mode() == FT_PIXEL_MODE_GRAY) { // A
          ByteBuffer bmpBuffer = bitmap.buffer(bitmap.width() * bitmap.rows());
          if (bmpBuffer != null) {
            for (int p = 0; p < bmpBuffer.limit(); p++) {
              byte a = bmpBuffer.get(p);
              targetBuffer.put((byte) 255);
              targetBuffer.put((byte) 255);
              targetBuffer.put((byte) 255);
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
            font.currentX,
            font.currentY,
            pixelWidth,
            pixelHeight,
            GL33.GL_RGBA,
            GL33.GL_UNSIGNED_BYTE,
            targetBuffer);

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

        GlyphInfo glyphInfo =
            new GlyphInfo(
                font,
                c,
                metrics.horiAdvance() / 64.0f,
                metrics.vertAdvance() / 64.0f,
                font.pages.size() - 1,
                font.currentX,
                font.currentY,
                pixelWidth,
                pixelHeight,
                false);
        font.glyphs.computeIfAbsent(size, k -> new HashMap<>()).put(c, glyphInfo);

        font.currentX += pixelWidth;
        font.rowMaxHeight = Math.max(font.rowMaxHeight, pixelHeight);

      } else {
        GlyphInfo glyphInfo =
            new GlyphInfo(
                font,
                c,
                metrics.horiAdvance() / 64.0f,
                metrics.vertAdvance() / 64.0f,
                0,
                0,
                0,
                0,
                0,
                false);
        font.glyphs.computeIfAbsent(size, k -> new HashMap<>()).put(c, glyphInfo);
      }

      FT_Done_Glyph(glyph);
    }
  }

  /**
   * Loads a FreeType face from the specified resource.
   *
   * @param resource the resource to load the font from
   * @return the FreeType face object representing the loaded font
   * @throws IOException if an I/O error occurs or if the font face cannot be loaded
   */
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

  /**
   * Loads a glyph for the specified character from the given FreeType face.
   *
   * @param face the FreeType face object
   * @param loadColor whether to load the glyph with color
   * @param ftRenderMode the render mode to use for the glyph
   * @param c the character to load the glyph for
   * @return the loaded glyph, or null if the glyph could not be found
   * @throws RuntimeException if an error occurs while loading or rendering the glyph
   */
  private static FT_Glyph loadGlyph(FT_Face face, boolean loadColor, int ftRenderMode, char c) {
    int glyphIndex = FT_Get_Char_Index(face, c);
    if (glyphIndex == 0) {
      return null;
    }
    int error = FT_Load_Glyph(face, glyphIndex, loadColor ? FT_LOAD_COLOR : FT_LOAD_DEFAULT);
    if (error != 0) {
      throw new RuntimeException("Failed to load glyph for character '" + c + "': " + error);
    }
    if (face.glyph() == null) {
      throw new RuntimeException("Failed to load glyph for character '" + c + "': Glyph is null");
    }

    PointerBuffer glyphPtr = BufferUtils.createPointerBuffer(1);
    error = FT_Get_Glyph(Objects.requireNonNull(face.glyph(), "GlyphSlot is null!"), glyphPtr);
    if (error != 0) {
      throw new RuntimeException("Failed to get glyph for character '" + c + "': " + error);
    }
    FT_Glyph glyph = FT_Glyph.create(glyphPtr.get(0));

    if (glyph.format() == FT_GLYPH_FORMAT_SVG) {
      throw new RuntimeException(
          "SVG glyphs are currently not supported!"); // TODO: Implement SVG support
    }

    if (glyph.format() != FT_GLYPH_FORMAT_BITMAP) {
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

  /**
   * Creates a new texture page.
   *
   * <p>This method initializes a new texture with predefined dimensions and settings. The texture
   * is created with the following properties: - Width: PAGE_SIZE_X - Height: PAGE_SIZE_Y - Internal
   * format: GL33.GL_RGBA - Minification filter: GL33.GL_LINEAR - Magnification filter:
   * GL33.GL_LINEAR - Wrap mode for S coordinate: GL33.GL_CLAMP_TO_EDGE - Wrap mode for T
   * coordinate: GL33.GL_CLAMP_TO_EDGE
   *
   * <p>The texture is filled with the contents of the EMPTY ByteBuffer.
   *
   * @return a new Texture object representing the created texture page
   */
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
            EMPTY);
    EMPTY.position(0);
    return page;
  }

  private Font() {}

  /**
   * Lays out the text into an array of TextLayoutElement objects.
   *
   * <p>This method processes the input text and arranges it into lines and positions based on the
   * specified font size, line padding, and maximum line width. It handles wrapping characters,
   * newline characters, and whitespace characters appropriately to ensure the text fits within the
   * given constraints.
   *
   * @param text the text to layout
   * @param fontSize the size of the font to use
   * @param linePadding the padding between lines
   * @param maxLineWidth the maximum width of a line before wrapping
   * @return an array of TextLayoutElement objects representing the laid out text
   */
  public TextLayoutElement[] layoutText(
      String text, int fontSize, int linePadding, int maxLineWidth) {

    int maxWidth = 0;
    int currentX = 0;
    int currentY = 0;
    int rowMaxHeight = 0;
    int lastWrapIndex = 0;
    int lastWrapAt = 0;

    TextLayoutElement[] elements = new TextLayoutElement[text.length()];

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      // Is Wrap Char
      if (WRAPPING_CHARS.contains(c)) {
        lastWrapIndex = i;
      }

      // Is NewLine Char
      if (NEWLINE_CHARS.contains(c)) {
        currentX = 0;
        currentY += fontSize + linePadding;
        continue;
      }

      // Is WhiteSpace
      if (WHITESPACE_CHARS.containsKey(c)) {
        currentX += Math.round(WHITESPACE_CHARS.get(c) * fontSize);
        continue;
      }

      GlyphInfo glyph = this.getOrLoadGlyph(c, fontSize);
      if (glyph == null) {
        glyph = this.getOrLoadGlyph('?', fontSize); // Replace unknown glyph with ?
      }

      if (currentX + glyph.width >= maxLineWidth) {
        if(lastWrapAt == lastWrapIndex) {
          lastWrapAt = i;
          lastWrapIndex = i;
        } else {
          i = lastWrapIndex;
          lastWrapAt = lastWrapIndex;
        }
        currentX = 0;
        currentY += fontSize + linePadding;
        continue;
      }


      elements[i] =
          new TextLayoutElement(this, glyph, currentX, -currentY, glyph.width, glyph.height);

      currentX += Math.round(glyph.xAdvance);
      rowMaxHeight = Math.max(rowMaxHeight, glyph.height);
      maxWidth = Math.max(maxWidth, currentX + glyph.width);
    }

    return elements;
  }

  public TextLayoutElement[] layoutText(String text, int fontSize, int maxLineWidth) {
    return this.layoutText(text, fontSize, DEFAULT_LINE_PADDING, maxLineWidth);
  }

  /**
   * Calculates the bounding box for an array of TextLayoutElement objects.
   *
   * <p>This method iterates through the provided elements and determines the maximum width and
   * height to create a bounding box that encompasses all the elements.
   *
   * @param elements an array of TextLayoutElement objects to calculate the bounding box for
   * @return a BoundingBox2D object representing the calculated bounding box
   */
  public BoundingBox2D calculateBoundingBox(TextLayoutElement[] elements) {
    int width = 0;
    int height = 0;
    for (TextLayoutElement element : elements) {
      if (element != null) {
        width = Math.max(width, element.x + element.width);
        height = Math.max(height, element.y + element.height);
      }
    }
    return new BoundingBox2D(0, 0, width, height);
  }

  /**
   * Calculates the bounding box for a given text with specified font size, line padding, and
   * maximum line width.
   *
   * <p>This method lays out the text and then calculates the bounding box based on the laid out
   * text.
   *
   * @param text the text to calculate the bounding box for
   * @param fontSize the size of the font to use
   * @param linePadding the padding between lines
   * @param maxWidth the maximum width of a line before wrapping
   * @return a BoundingBox2D object representing the calculated bounding box
   */
  public BoundingBox2D calculateBoundingBox(
      String text, int fontSize, int linePadding, int maxWidth) {
    return this.calculateBoundingBox(this.layoutText(text, fontSize, linePadding, maxWidth));
  }

  /**
   * Calculates the bounding box for a given text with specified font size and maximum line width.
   *
   * <p>This method uses the default line padding.
   *
   * @param text the text to calculate the bounding box for
   * @param fontSize the size of the font to use
   * @param maxWidth the maximum width of a line before wrapping
   * @return a BoundingBox2D object representing the calculated bounding box
   */
  public BoundingBox2D calculateBoundingBox(String text, int fontSize, int maxWidth) {
    return this.calculateBoundingBox(text, fontSize, DEFAULT_LINE_PADDING, maxWidth);
  }

  /**
   * Calculates the bounding box for a given text with specified font size.
   *
   * <p>This method uses the default line padding and assumes no maximum line width.
   *
   * @param text the text to calculate the bounding box for
   * @param fontSize the size of the font to use
   * @return a BoundingBox2D object representing the calculated bounding box
   */
  public BoundingBox2D calculateBoundingBox(String text, int fontSize) {
    return this.calculateBoundingBox(text, fontSize, DEFAULT_LINE_PADDING, Integer.MAX_VALUE);
  }

  /**
   * Checks if the font has a glyph for the specified character and size.
   *
   * @param c the character to check for
   * @param size the size of the glyph to check for
   * @return true if the glyph exists, false otherwise
   */
  public boolean hasGlyph(char c, int size) {
    return this.glyphs.containsKey(size) && this.glyphs.get(size).containsKey(c);
  }

  /**
   * Checks if the font has a glyph for the specified character in any size.
   *
   * @param c the character to check for
   * @return true if the glyph exists, false otherwise
   */
  public boolean hasGlyph(char c) {
    return this.glyphs.values().stream().anyMatch(m -> m.containsKey(c));
  }

  /**
   * Retrieves or loads a glyph for the specified character and size.
   *
   * <p>If the glyph is not already loaded, it will be loaded from the FreeType face.
   *
   * @param c the character to retrieve or load the glyph for
   * @param size the size of the glyph to retrieve or load
   * @return the GlyphInfo object representing the glyph, or null if the glyph could not be loaded
   */
  private GlyphInfo getOrLoadGlyph(char c, int size) {
    if (this.glyphs.containsKey(size)) {
      if (this.glyphs.get(size).containsKey(c)) {
        return this.glyphs.get(size).get(c);
      } else {
        loadGlyphs(this, this.face, String.valueOf(c), size, this.renderMode);
        return this.glyphs.get(size).get(c);
      }
    } else {
      loadGlyphs(this, this.face, DEFAULT_CHARSET, size, this.renderMode);
      if (!DEFAULT_CHARSET.contains(String.valueOf(c))) {
        loadGlyphs(this, this.face, String.valueOf(c), size, this.renderMode);
      }
      return this.getGlyph(c, size);
    }
  }

  /**
   * Retrieves a glyph for the specified character and size.
   *
   * @param c the character to retrieve the glyph for
   * @param size the size of the glyph to retrieve
   * @return the GlyphInfo object representing the glyph, or null if the glyph does not exist
   */
  private GlyphInfo getGlyph(char c, int size) {
    if (this.glyphs.containsKey(size)) {
      return this.glyphs.get(size).get(c);
    }

    return null;
  }

  /**
   * Retrieves a texture page by index.
   * @param pageIndex the index of the texture page to retrieve
   * @return the Texture object representing the texture page
   */
  public Texture getPage(int pageIndex) {
    return this.pages.get(pageIndex);
  }

  /** Represents information about a glyph. */
  public static class GlyphInfo {

    public final int codepoint;
    public final float xAdvance, yAdvance;
    public final int page;
    public final int pageX, pageY;
    public final int width, height;
    public final boolean colored;
    public final Font font;

    /**
     * Constructs a new GlyphInfo object.
     *
     * @param font the font object that this glyph belongs to
     * @param codepoint the Unicode codepoint of the glyph
     * @param xAdvance the horizontal advance of the glyph
     * @param yAdvance the vertical advance of the glyph
     * @param page the texture page index where the glyph is stored
     * @param x the x-coordinate of the glyph on the texture page
     * @param y the y-coordinate of the glyph on the texture page
     * @param width the width of the glyph
     * @param height the height of the glyph
     * @param colored whether the glyph is colored
     */
    public GlyphInfo(
        Font font,
        int codepoint,
        float xAdvance,
        float yAdvance,
        int page,
        int x,
        int y,
        int width,
        int height,
        boolean colored) {
      this.font = font;
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

  /** Represents an element of text layout. */
  public static class TextLayoutElement {

    public final Font font;
    public final GlyphInfo glyph;
    public final int x, y;
    public final int width, height;

    /**
     * Constructs a new TextLayoutElement object.
     *
     * @param font the font object that this element belongs to
     * @param glyph the glyph information for this element
     * @param x the x-coordinate of the element
     * @param y the y-coordinate of the element
     * @param width the width of the element
     * @param height the height of the element
     */
    public TextLayoutElement(Font font, GlyphInfo glyph, int x, int y, int width, int height) {
      this.font = font;
      this.glyph = glyph;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
  }
}
