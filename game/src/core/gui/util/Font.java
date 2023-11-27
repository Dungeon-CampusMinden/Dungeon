package core.gui.util;

import static core.gui.util.Logging.log;

import static org.lwjgl.stb.STBTruetype.*;

import core.Assets;
import core.gui.GUIRoot;
import core.gui.backend.BackendImage;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Vector2f;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTKerningentry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class Font {

    public static final int MAX_ATLAS_SIZE = 1024;
    public static final String DEFAULT_CHARS =
            "abcdefghijklmnopqrstuvwxyzäöüABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜß1234567890!?\"§$%&/()=\\-_.,:;#*+~<>@[]{}'²³|`´^°";
    public static final Map<Integer, Float> WHITESPACE_CHARACTERS = new HashMap<>();
    public static final List<Integer> WRAPPING_CHARACTERS = new ArrayList<>();
    public static final List<Integer> NEWLINE_CHARACTERS = new ArrayList<>();
    public static final int CODEPOINT_TABULATOR = 0x0009;
    public static final int CODEPOINT_SPACE = 0x0020;
    public static final int ADDITIONAL_LINE_GAP = 4;

    static {
        String wrappingCharacters = ".,:;!?-";
        wrappingCharacters.chars().forEach(WRAPPING_CHARACTERS::add);

        String newlineCharacters = "\n";
        newlineCharacters.chars().forEach(NEWLINE_CHARACTERS::add);

        WHITESPACE_CHARACTERS.put(0x0020, 0.25f);
        WHITESPACE_CHARACTERS.put(0x00A0, 0.25f);
        WHITESPACE_CHARACTERS.put(0x2000, 0.50f);
        WHITESPACE_CHARACTERS.put(0x2001, 1.0f);
        WHITESPACE_CHARACTERS.put(0x2002, 1.0f);
        WHITESPACE_CHARACTERS.put(0x2003, 1.0f);
        WHITESPACE_CHARACTERS.put(0x2004, 0.33f);
        WHITESPACE_CHARACTERS.put(0x2005, 0.25f);
        WHITESPACE_CHARACTERS.put(0x2006, 0.166f);
        WHITESPACE_CHARACTERS.put(0x2009, 0.166f);
        WHITESPACE_CHARACTERS.put(0x200A, 0.125f);
    }

    public BackendImage[] fontAtlas;
    public final float fontScale;
    public final int fontSize, ascent, descent, lineGap;
    public final Map<Integer, Glyph> glyphMap = new HashMap<>();
    public final Map<Integer, Map<Integer, Integer>> kerningMap = new HashMap<>();
    public final int spaceWidth;

    private Font(
            float fontScale, int fontSize, int ascent, int descent, int lineGap, int spaceWidth) {
        this.fontScale = fontScale;
        this.fontSize = fontSize;
        this.ascent = ascent;
        this.descent = descent;
        this.lineGap = lineGap;
        this.spaceWidth = spaceWidth;
    }

    /**
     * Load a true type font from a file.
     *
     * <p>Loads a true type font from a file from the specified path using the STBTrueType library.
     * Only the {@link Font#DEFAULT_CHARS default characters} are loaded. If other characters are
     * needed, use {@link #loadFont(String, int, String)}.
     *
     * <p>The initial font size is specified in pixels. The font is loaded as a texture atlas. Using
     * this font with another size than the initial size may result in blurry text.
     *
     * @deprecated This version of loadFont() is marked as deprecated, as it uses a string to
     *     specify the font.
     * @param ttfFilePath The path to the true type font file.
     * @param initialFontSize The initial font size in pixels.
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    @Deprecated
    public static Font[] loadFont(String ttfFilePath, int initialFontSize) throws IOException {
        return loadFont(ttfFilePath, initialFontSize, DEFAULT_CHARS);
    }

    /**
     * Load a true type font from a file.
     *
     * <p>Loads a true type font from a file from the specified path using the STBTrueType library.
     * Only the {@link Font#DEFAULT_CHARS default characters} are loaded. If other characters are
     * needed, use {@link #loadFont(Assets.Fonts, int, String)}.
     *
     * <p>The initial font size is specified in pixels. The font is loaded as a texture atlas. Using
     * this font with another size than the initial size may result in blurry text.
     *
     * @param font The Font Asset to load.
     * @param initialFontSize The initial font size in pixels.
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    public static Font[] loadFont(Assets.Fonts font, int initialFontSize) throws IOException {
        return loadFont(font.path(), initialFontSize, DEFAULT_CHARS);
    }

    /**
     * Load a true type font from a file.
     *
     * <p>Loads a true type font from a file from the specified path using the STBTrueType library.
     * Only the {@link Font#DEFAULT_CHARS default characters} are loaded. If other characters are
     * needed, use {@link #loadFont(Assets.Fonts, int, String)}.
     *
     * <p>The initial font size is specified in pixels. The font is loaded as a texture atlas. Using
     * this font with another size than the initial size may result in blurry text.
     *
     * @param font The Font Asset to load.
     * @param initialFontSize The initial font size in pixels.
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    public static Font[] loadFont(Assets.Fonts font, int initialFontSize, String charactersToLoad)
            throws IOException {
        return loadFont(font.path(), initialFontSize, charactersToLoad);
    }

    /**
     * Load a true type font from a file.
     *
     * <p>Loads a true type font from a file from the specified path using the STBTrueType library.
     * The initial font size is specified in pixels. The font is loaded as a texture atlas. Using
     * this font with another size than the initial size may result in blurry text.
     *
     * @param ttfFilePath The path to the true type font file.
     * @param fontSize The initial font size in pixels.
     * @param charactersToLoad The characters to load. Only these characters will be available while
     *     rendering.
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    private static Font[] loadFont(String ttfFilePath, int fontSize, String charactersToLoad)
            throws IOException {
        InputStream is = Font.class.getResourceAsStream(ttfFilePath);
        if (is == null) {
            throw new IOException("Font not found at: " + ttfFilePath);
        }
        byte[] fontBytes = is.readAllBytes();
        is.close();

        // Check if question mark is in charactersToLoad. If not add it, as it is used as a
        // replacement for unknown characters
        if (!charactersToLoad.contains("?")) {
            charactersToLoad += "?";
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(fontBytes.length);
        buffer.put(fontBytes);
        buffer.position(0);
        STBTTFontinfo fontInfo = STBTTFontinfo.calloc();
        boolean success = stbtt_InitFont(fontInfo, buffer);
        if (!success) {
            throw new RuntimeException("Failed to load font: " + ttfFilePath);
        }

        int fontsInFile = stbtt_GetNumberOfFonts(buffer);
        log(CustomLogLevel.DEBUG, "Fonts in file \"%s\": %d", ttfFilePath, fontsInFile);

        Font[] ret = new Font[fontsInFile];
        for (int fontIndex = 0; fontIndex < fontsInFile; fontIndex++) {
            float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);
            log(
                    CustomLogLevel.DEBUG,
                    "Font[%d] scale: x%f (%d pixels)",
                    fontIndex,
                    scale,
                    fontSize);

            int[] ascent = new int[1];
            int[] descent = new int[1];
            int[] lineGap = new int[1];
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
            Font font =
                    new Font(
                            scale,
                            fontSize,
                            Math.round(ascent[0] * scale),
                            Math.round(descent[0] * scale),
                            Math.round(lineGap[0] * scale) + Font.ADDITIONAL_LINE_GAP,
                            (fontSize) * 2);

            // Map the glyphs to the font atlas

            int maxHeightInRow = 0;
            int currentX = 0;
            int currentY = 0;
            List<ByteBuffer> atlasImages = new ArrayList<>();
            atlasImages.add(ByteBuffer.allocateDirect(MAX_ATLAS_SIZE * MAX_ATLAS_SIZE));

            for (int charIndex = 0; charIndex < charactersToLoad.length(); charIndex++) {
                int unicodeCodepoint = Character.codePointAt(charactersToLoad, charIndex);
                int glyphIndex = stbtt_FindGlyphIndex(fontInfo, unicodeCodepoint);
                if (glyphIndex == 0) {
                    log(
                            CustomLogLevel.WARNING,
                            "Glyph index for character '%c' is 0 (Not Found)",
                            unicodeCodepoint);
                    continue;
                }
                int[] x0 = new int[1];
                int[] y0 = new int[1];
                int[] x1 = new int[1];
                int[] y1 = new int[1];
                int[] xAdvance = new int[1];
                stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, xAdvance, null);
                stbtt_GetGlyphBitmapBox(fontInfo, glyphIndex, scale, scale, x0, y0, x1, y1);
                int width = x1[0] - x0[0];
                int height = y1[0] - y0[0];

                if (currentX + width > MAX_ATLAS_SIZE) { // End of row
                    currentX = 0;
                    currentY = currentY + maxHeightInRow;
                    maxHeightInRow = 0;
                }

                ByteBuffer currentAtlas = atlasImages.get(atlasImages.size() - 1);
                currentAtlas.position(currentX + currentY * MAX_ATLAS_SIZE);
                stbtt_MakeGlyphBitmap(
                        fontInfo,
                        currentAtlas,
                        width,
                        height,
                        MAX_ATLAS_SIZE,
                        scale,
                        scale,
                        glyphIndex);

                Glyph glyph =
                        new Glyph(
                                unicodeCodepoint,
                                atlasImages.size() - 1,
                                width,
                                height,
                                currentX,
                                currentY,
                                x0[0],
                                y0[0],
                                x1[0],
                                y1[0],
                                Math.round(xAdvance[0] * scale));
                font.glyphMap.put(unicodeCodepoint, glyph);

                currentX += glyph.width;
                maxHeightInRow = Math.max(maxHeightInRow, glyph.height);

                if (currentX > MAX_ATLAS_SIZE) { // End of row
                    currentX = 0;
                    currentY = currentY + maxHeightInRow;
                }
                if (currentY > MAX_ATLAS_SIZE) { // End of atlas
                    currentY = 0;
                    atlasImages.add(ByteBuffer.allocateDirect(MAX_ATLAS_SIZE * MAX_ATLAS_SIZE));
                }
            }

            // Load kerning table
            STBTTKerningentry.Buffer kerningTable =
                    STBTTKerningentry.calloc(stbtt_GetKerningTableLength(fontInfo));
            int entries = stbtt_GetKerningTable(fontInfo, kerningTable);
            log(CustomLogLevel.DEBUG, "Loaded %d kerning entries", entries);

            for (int i = 0; i < entries; i++) {
                STBTTKerningentry entry = kerningTable.get(i);
                int g1 = entry.glyph1();
                int g2 = entry.glyph2();
                int kerning = entry.advance();
                if (!font.kerningMap.containsKey(g1)) {
                    font.kerningMap.put(g1, new HashMap<>());
                }
                font.kerningMap.get(g1).put(g2, kerning);
            }

            kerningTable.free();

            font.fontAtlas = new BackendImage[atlasImages.size()];
            for (int i = 0; i < atlasImages.size(); i++) {
                font.fontAtlas[i] =
                        GUIRoot.getInstance()
                                .backend()
                                .loadImageFromBitmap(
                                        atlasImages.get(i), MAX_ATLAS_SIZE, MAX_ATLAS_SIZE, 1);
                atlasImages.get(i).position(0);
            }
            ret[fontIndex] = font;
        }
        fontInfo.free();
        return ret;
    }

    public Vector2f boundingBox(String text, float maxWidth) {

        if (maxWidth <= 0) {
            maxWidth = Float.MAX_VALUE;
        }

        float width = 0;

        int currentX = 0;
        int currentY = 0;

        int lastWrapIndex = 0;
        boolean tried = false;

        for (int i = 0; i < text.length(); i++) {
            int codePoint = Character.codePointAt(text, i);
            Font.Glyph glyph = this.glyphMap.get(codePoint);

            if (Font.WRAPPING_CHARACTERS.contains(codePoint)) {
                lastWrapIndex = i;
                tried = false;
            }
            if (Font.NEWLINE_CHARACTERS.contains(codePoint)) {
                width = Math.max(currentX, width);
                currentX = 0;
                currentY =
                        currentY - (this.lineGap + this.ascent + this.descent); // TODO: Check if ok
                lastWrapIndex = i;
                tried = false;
                continue;
            }
            if (Font.WHITESPACE_CHARACTERS.containsKey(codePoint)) {
                currentX +=
                        Math.round(
                                Font.WHITESPACE_CHARACTERS.getOrDefault(codePoint, 1.0f)
                                        * this.fontSize);
                if (currentX >= maxWidth) {
                    width = Math.max(currentX, width);
                    currentX = 0;
                    currentY =
                            currentY
                                    - (this.lineGap
                                            + this.ascent
                                            + this.descent); // TODO: Check if ok
                }
                lastWrapIndex = i;
                tried = false;
                continue;
            }
            if (codePoint == Font.CODEPOINT_TABULATOR) {
                int tabWith =
                        4
                                * Math.round(
                                        Font.WHITESPACE_CHARACTERS.get(Font.CODEPOINT_SPACE)
                                                * this.fontSize);
                currentX = currentX + tabWith - (currentX % tabWith);
                lastWrapIndex = i;
                tried = false;
                continue;
            }

            if (glyph == null) {
                glyph = this.glyphMap.get(Character.codePointAt("?", 0));
                if (glyph == null) {
                    currentX +=
                            Math.round(
                                    Font.WHITESPACE_CHARACTERS.getOrDefault(
                                                    Font.CODEPOINT_SPACE, 1.0f)
                                            * this.fontSize);
                    continue;
                }
            }

            if (currentX + glyph.width() > maxWidth && !(glyph.width() > maxWidth)) {
                width = Math.max(currentX, width);
                currentX = 0;
                currentY =
                        currentY - (this.lineGap + this.ascent + this.descent); // TODO: Check if ok
                if (!tried) {
                    i = lastWrapIndex;
                    tried = true;
                    continue;
                } else {
                    lastWrapIndex = i;
                    tried = false;
                }
            }

            int kerning = 0;
            int nextCodepoint = Character.codePointAt(text, Math.min(i + 1, text.length() - 1));
            if (this.kerningMap.containsKey(codePoint) && i != text.length() - 1) {
                kerning = this.kerningMap.get(codePoint).getOrDefault(nextCodepoint, 0);
            }
            currentX += glyph.xAdvance() + kerning;
        }

        return new Vector2f(width, Math.abs(currentY) + fontSize + lineGap + ascent + descent);
    }

    public record Glyph(
            int unicode,
            int atlas,
            int width,
            int height,
            int xOffset,
            int yOffset,
            int x1,
            int y1,
            int x2,
            int y2,
            int xAdvance) {}
}
