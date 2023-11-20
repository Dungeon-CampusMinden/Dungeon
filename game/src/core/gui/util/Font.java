package core.gui.util;

import static core.gui.util.Logging.log;

import static org.lwjgl.stb.STBTruetype.*;

import core.gui.GUIRoot;
import core.gui.backend.BackendImage;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Vector2i;

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
    private static final List<Integer> WRAPPING_CHARACTERS = new ArrayList<>();
    private static final List<Integer> NEWLINE_CHARACTERS = new ArrayList<>();

    static {
        String wrappingCharacters = " .,:;!?-";
        wrappingCharacters.chars().forEach(WRAPPING_CHARACTERS::add);

        String newlineCharacters = "\n";
        newlineCharacters.chars().forEach(NEWLINE_CHARACTERS::add);
    }

    public BackendImage[] fontAtlas;
    public final float fontScale;
    public final int fontSize, ascent, descent, lineGap;
    public final Map<Integer, Glyph> glyphMap = new HashMap<>();
    public final Map<Integer, Map<Integer, Integer>> kerningMap = new HashMap<>();

    private Font(float fontScale, int fontSize, int ascent, int descent, int lineGap) {
        this.fontScale = fontScale;
        this.fontSize = fontSize;
        this.ascent = ascent;
        this.descent = descent;
        this.lineGap = lineGap;
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
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    public static Font[] loadFont(String ttfFilePath, int fontSize, String charactersToLoad)
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

        float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);
        log(CustomLogLevel.DEBUG, "Font scale: x%f (%d pixels)", scale, fontSize);

        Font[] ret = new Font[fontsInFile];
        for (int fontIndex = 0; fontIndex < fontsInFile; fontIndex++) {
            int[] ascent = new int[1];
            int[] descent = new int[1];
            int[] lineGap = new int[1];
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
            Font font = new Font(scale, fontSize, ascent[0], descent[0], lineGap[0]);

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
                                xAdvance[0]);
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
            }
            ret[fontIndex] = font;
        }
        fontInfo.free();
        return ret;
    }

    /**
     * Load a true type font from a file.
     *
     * <p>Loads a true type font from a file from the specified path using the STBTrueType library.
     * Only the default characters () are loaded. If other characters are needed, use {@link
     * #loadFont(String, int, String)}.
     *
     * <p>The initial font size is specified in pixels. The font is loaded as a texture atlas. Using
     * this font with another size than the initial size may result in blurry text.
     *
     * @param ttfFilePath The path to the true type font file.
     * @param initialFontSize The initial font size in pixels.
     * @return The loaded font.
     * @throws IOException If the font could not be loaded.
     */
    public static Font[] loadFont(String ttfFilePath, int initialFontSize) throws IOException {
        return loadFont(ttfFilePath, initialFontSize, DEFAULT_CHARS);
    }

    public Vector2i boundingBox(String text, int maxWidth, boolean considerKernings) {
        // TODO: Handle Tabulator
        int width = 0;
        int height = 0;

        int lastWrapIndex = 0;
        int xOnLastWrapChar = 0;

        int currentX = 0;
        int currentY = 0;

        for (int i = 0; i < text.length(); i++) {
            int unicodeCodepoint = Character.codePointAt(text, i);
            Glyph glyph = glyphMap.get(unicodeCodepoint);
            if (glyph == null) {
                glyph = glyphMap.get(Character.codePointOf("?"));
                if (glyph == null) {
                    continue; // Skip unknown character if question mark is not available
                }
            }

            if (NEWLINE_CHARACTERS.contains(unicodeCodepoint)) {
                width = Math.max(width, currentX);
                currentX = 0;
                currentY += this.fontSize + this.lineGap;
                lastWrapIndex = i;
                xOnLastWrapChar = 0;
                continue;
            }
            if (WRAPPING_CHARACTERS.contains(unicodeCodepoint)) {
                lastWrapIndex = i;
                xOnLastWrapChar = currentX;
            }

            if (considerKernings) {

            } else {

            }

            if (currentX + glyph.width > maxWidth) {
                width = Math.max(width, xOnLastWrapChar);
                currentX = xOnLastWrapChar;
                currentY += fontSize + lineGap;
                i = lastWrapIndex + 1;
                continue;
            }
            currentX += glyphMap.get(unicodeCodepoint).xAdvance;
        }
        return Vector2i.zero();
    }

    public record Glyph(
            int unicode,
            int atlas,
            int width,
            int height,
            int xOffset,
            int yOffset,
            int xAdvance) {}
}
