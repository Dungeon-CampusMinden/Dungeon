package core.gui.util;

import static core.gui.util.Logging.log;

import static org.lwjgl.stb.STBTruetype.*;

import core.gui.backend.BackendImage;
import core.utils.logging.CustomLogLevel;

import org.lwjgl.stb.STBTTFontinfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Font {

    private BackendImage[] fontAtlas;
    public final float fontScale;
    public final int fontSize, ascent, descent, lineGap;
    private Map<Integer, Glyph> glyphMap = new HashMap<>();

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
            int maxAtlasWidth = 1024;
            int maxAtlasHeight = 1024;

            int maxHeightInRow = 0;
            int currentX = 0;
            int currentY = 0;
            List<ByteBuffer> atlasImages = new ArrayList<>();
            atlasImages.add(ByteBuffer.allocateDirect(maxAtlasWidth * maxAtlasWidth));

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
                currentAtlas.position(currentX + currentY * maxAtlasWidth);
                stbtt_MakeGlyphBitmap(
                        fontInfo,
                        currentAtlas,
                        width,
                        height,
                        maxAtlasWidth,
                        scale,
                        scale,
                        glyphIndex);

                Glyph glyph =
                        new Glyph(unicodeCodepoint, width, height, currentX, currentY, xAdvance[0]);
                font.glyphMap.put(unicodeCodepoint, glyph);
                log(
                        CustomLogLevel.DEBUG,
                        "Loaded glyph for character '%c' with index %d (%dx%d)",
                        unicodeCodepoint,
                        glyphIndex,
                        width,
                        height);

                currentX += glyph.width;
                maxHeightInRow = Math.max(maxHeightInRow, glyph.height);

                if (currentX + glyph.width > maxAtlasWidth) { // End of row
                    currentX = 0;
                    currentY = currentY + maxHeightInRow;
                }
                if (currentY + glyph.height > maxAtlasHeight) { // End of atlas
                    currentX = 0;
                    currentY = 0;
                    atlasImages.add(ByteBuffer.allocateDirect(maxAtlasWidth * maxAtlasWidth));
                }

                // TODO: Kerning pairs
            }
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
        return loadFont(
                ttfFilePath,
                initialFontSize,
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZß1234567890!?\"§$%&/()=\\-_.,:;#*+~<>@[]{}'²³|`´^°");
    }

    private record Glyph(
            int unicode, int width, int height, int xOffset, int yOffset, int xAdvance) {}
}
