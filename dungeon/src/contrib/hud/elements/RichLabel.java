package contrib.hud.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import core.utils.FontHelper;
import core.utils.FontSpec;
import core.utils.components.draw.TextureMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rich-text label for libGDX Scene2d that supports inline images and color changes.
 *
 * <p>Markup tags supported in the input text:
 *
 * <ul>
 *   <li>{@code [img=path/to/image.png]} - renders an inline image scaled to the font line height.
 *   <li>{@code [color=red]} or {@code [color=#ff0000]} - changes the text color for subsequent
 *       text.
 *   <li>{@code [/color]} - resets the text color to the default.
 * </ul>
 *
 * <p>The label performs word-level flow layout: each word and each image is a "run" that is placed
 * left-to-right and wrapped to the next line when it would exceed the available width.
 */
public class RichLabel extends WidgetGroup implements Disposable {

  private static final Pattern TAG_PATTERN =
      Pattern.compile("\\[img=([^]]+)]|\\[color=([^]]+)]|\\[/color]");

  private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

  static {
    NAMED_COLORS.put("white", Color.WHITE);
    NAMED_COLORS.put("black", Color.BLACK);
    NAMED_COLORS.put("red", Color.RED);
    NAMED_COLORS.put("green", Color.GREEN);
    NAMED_COLORS.put("blue", Color.BLUE);
    NAMED_COLORS.put("yellow", Color.YELLOW);
    NAMED_COLORS.put("cyan", Color.CYAN);
    NAMED_COLORS.put("magenta", Color.MAGENTA);
    NAMED_COLORS.put("orange", Color.ORANGE);
    NAMED_COLORS.put("gray", Color.GRAY);
    NAMED_COLORS.put("grey", Color.GRAY);
    NAMED_COLORS.put("light_gray", Color.LIGHT_GRAY);
    NAMED_COLORS.put("dark_gray", Color.DARK_GRAY);
  }

  private static final float IMAGE_SCALE = 1.5f;
  private static final float IMAGE_GAP = 3f;

  private FontSpec fontSpec;
  private final Map<String, TextureRegion> textureCache = new HashMap<>();
  private final List<Run> runs = new ArrayList<>();

  private String text;
  private float computedPrefHeight;
  private boolean wrap = true;

  /**
   * Creates a new RichLabel with the given text and font specification.
   *
   * @param text the markup text to display
   * @param fontSpec the font specification for default text rendering
   */
  public RichLabel(String text, FontSpec fontSpec) {
    this.fontSpec = fontSpec;
    this.text = text;
    parseText();
  }

  /**
   * Creates a new RichLabel with the given text, font size, and color.
   *
   * @param text the markup text to display
   * @param fontSize the font size
   * @param fontColor the default font color
   */
  public RichLabel(String text, int fontSize, Color fontColor) {
    this(text, FontSpec.of(fontSize, fontColor));
  }

  /**
   * Creates a new RichLabel with the given text and font size, using white as the default color.
   *
   * @param text the markup text to display
   * @param fontSize the font size
   */
  public RichLabel(String text, int fontSize) {
    this(text, fontSize, Color.WHITE);
  }

  /**
   * Sets whether the label should wrap text when it exceeds the available width.
   *
   * @param wrap true to enable wrapping, false to lay out in a single line
   */
  public void setWrap(boolean wrap) {
    this.wrap = wrap;
    invalidateHierarchy();
  }

  /**
   * Replaces the font specification used for default text rendering and re-parses the text.
   *
   * @param fontSpec the new font specification
   */
  public void setFontSpec(FontSpec fontSpec) {
    this.fontSpec = fontSpec;
    parseText();
    invalidateHierarchy();
  }

  /**
   * Replaces the displayed text with new markup text and re-parses it.
   *
   * @param text the new markup text
   */
  public void setText(String text) {
    this.text = text;
    parseText();
    invalidateHierarchy();
  }

  /**
   * Returns the current markup text.
   *
   * @return the raw markup text string
   */
  public String getText() {
    return text;
  }

  @Override
  public float getPrefWidth() {
    BitmapFont font = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float spaceWidth = computeSpaceWidth(font, glyphLayout);
    float width = 0;

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);
      if (run instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && width > 0) {
          width += spaceWidth;
        }
        glyphLayout.setText(font, trimmed);
        width += glyphLayout.width;
      } else if (run instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir.path());
        if (region != null) {
          float imgHeight = font.getCapHeight() * IMAGE_SCALE;
          float imgWidth = (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight;
          if (i > 0) imgWidth += IMAGE_GAP;
          if (i < runs.size() - 1) imgWidth += IMAGE_GAP;
          width += imgWidth;
        }
      }
    }
    return width;
  }

  @Override
  public float getPrefHeight() {
    // Trigger a dry layout to compute height if needed
    if (computedPrefHeight <= 0) {
      layoutRuns(getWidth() > 0 ? getWidth() : getPrefWidth());
    }
    return computedPrefHeight;
  }

  @Override
  public void layout() {
    clearChildren();
    float availableWidth = getWidth();
    if (availableWidth <= 0) {
      availableWidth = getPrefWidth();
    }
    layoutRuns(availableWidth);
  }

  @Override
  public void dispose() {
    textureCache.clear();
  }

  // -- Internal types --

  /** A sealed interface representing a single layout token (word or image). */
  private sealed interface Run permits TextRun, ImageRun {}

  /**
   * A single word of text with an associated color.
   *
   * @param word the text content (single word, may include trailing space)
   * @param color the color to render this word in
   */
  private record TextRun(String word, Color color) implements Run {}

  /**
   * An inline image reference.
   *
   * @param path the asset path of the image
   */
  private record ImageRun(String path) implements Run {}

  // -- Parsing --

  /** Parses the markup text into a flat list of runs (words and images). */
  private void parseText() {
    runs.clear();
    if (text == null || text.isEmpty()) return;

    Color currentColor = fontSpec.color();
    Matcher matcher = TAG_PATTERN.matcher(text);
    int lastEnd = 0;

    while (matcher.find()) {
      // Text before this tag
      if (matcher.start() > lastEnd) {
        String segment = text.substring(lastEnd, matcher.start());
        addTextRuns(segment, currentColor);
      }

      if (matcher.group(1) != null) {
        // [img=path]
        runs.add(new ImageRun(matcher.group(1)));
      } else if (matcher.group(2) != null) {
        // [color=value]
        currentColor = parseColor(matcher.group(2));
      } else {
        // [/color]
        currentColor = fontSpec.color();
      }
      lastEnd = matcher.end();
    }

    // Remaining text after the last tag
    if (lastEnd < text.length()) {
      addTextRuns(text.substring(lastEnd), currentColor);
    }
  }

  /**
   * Splits a plain text segment into individual word runs, preserving whitespace as trailing space
   * on words.
   *
   * @param segment the plain text segment to split
   * @param color the color to apply to the resulting text runs
   */
  private void addTextRuns(String segment, Color color) {
    // Split on whitespace boundaries, keeping the words and spaces
    // We split by spaces but keep each word as a run. Spaces between words are attached as
    // a leading space on the next word to ensure correct spacing during layout.
    String[] parts = segment.split("(?<=\\s)|(?=\\s)");
    StringBuilder current = new StringBuilder();
    for (String part : parts) {
      if (part.isBlank() && !current.isEmpty()) {
        // Flush the current word, attach trailing space
        runs.add(new TextRun(current.toString(), color));
        current.setLength(0);
        current.append(part);
      } else if (part.isBlank()) {
        current.append(part);
      } else {
        current.append(part);
        runs.add(new TextRun(current.toString(), color));
        current.setLength(0);
      }
    }
    if (!current.isEmpty()) {
      runs.add(new TextRun(current.toString(), color));
    }
  }

  /**
   * Parses a color string that is either a named color (e.g. "red") or a hex value (e.g.
   * "#ff0000").
   *
   * @param value the color string to parse
   * @return the parsed Color, or white if the value is not recognized
   */
  private static Color parseColor(String value) {
    String lower = value.toLowerCase().trim();
    Color named = NAMED_COLORS.get(lower);
    if (named != null) return named;

    // Try hex (with or without leading #)
    try {
      String hex = lower.startsWith("#") ? lower.substring(1) : lower;
      if (hex.length() == 6) hex += "ff"; // add alpha
      return Color.valueOf(hex);
    } catch (Exception e) {
      return Color.WHITE;
    }
  }

  /**
   * Strips all markup tags from the text, returning plain text only.
   *
   * @param input the markup text to strip
   * @return the plain text with all tags removed
   */
  private String stripTags(String input) {
    return TAG_PATTERN.matcher(input).replaceAll("");
  }

  // -- Layout --

  /**
   * Performs flow layout of all runs within the given width, creating Scene2d actors and
   * positioning them. Also computes {@link #computedPrefHeight}.
   *
   * @param availableWidth the maximum width available for laying out runs
   */
  private void layoutRuns(float availableWidth) {
    BitmapFont font = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float lineHeight = font.getLineHeight();
    float spaceWidth = computeSpaceWidth(font, glyphLayout);

    float x = 0;
    float y = 0;

    List<PlacedRun> placed = new ArrayList<>();

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);
      float runWidth;
      float runHeight;

      if (run instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && x > 0) {
          x += spaceWidth;
        }
        glyphLayout.setText(font, trimmed);
        runWidth = glyphLayout.width;
        runHeight = lineHeight;

        if (wrap && x > 0 && x + runWidth > availableWidth) {
          x = 0;
          y += lineHeight;
        }

        placed.add(new PlacedRun(run, x, y, runWidth, runHeight));
        x += runWidth;

      } else if (run instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir.path());
        float imgHeight = font.getCapHeight() * IMAGE_SCALE;
        float imgWidth =
            region != null
                ? (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight
                : 0;
        float leadGap = i > 0 ? IMAGE_GAP : 0;
        float trailGap = i < runs.size() - 1 ? IMAGE_GAP : 0;
        float totalImgWidth = imgWidth + leadGap + trailGap;

        if (wrap && x > 0 && x + totalImgWidth > availableWidth) {
          x = 0;
          y += lineHeight;
        }

        x += leadGap;
        placed.add(new PlacedRun(run, x, y, imgWidth, imgHeight));
        x += imgWidth + trailGap;
      }
    }

    float totalHeight = y + lineHeight;
    computedPrefHeight = totalHeight;

    for (PlacedRun pr : placed) {
      if (pr.run() instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        if (trimmed.isEmpty()) continue;
        FontSpec runFontSpec = fontSpec.withColor(tr.color());
        Label label =
            new Label(trimmed, new Label.LabelStyle(FontHelper.getFont(runFontSpec), null));
        float actorY = totalHeight - pr.y() - lineHeight;
        label.setBounds(pr.x(), actorY, pr.width(), pr.height());
        addActor(label);

      } else if (pr.run() instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir.path());
        if (region == null) continue;
        Image image = new Image(new TextureRegionDrawable(region));
        float actorY = totalHeight - pr.y() - lineHeight;
        float yOffset = (FontHelper.getFont(fontSpec).getLineHeight() - pr.height()) / 2f;
        image.setBounds(pr.x(), actorY + yOffset, pr.width(), pr.height());
        addActor(image);
      }
    }
  }

  /**
   * A positioned run used during layout computation.
   *
   * @param run the layout run
   * @param x the x position
   * @param y the y position
   * @param width the width of the run
   * @param height the height of the run
   */
  private record PlacedRun(Run run, float x, float y, float width, float height) {}

  /**
   * Computes the pixel width of a single space character in the given font.
   *
   * @param font the bitmap font to measure
   * @param glyphLayout the glyph layout instance to reuse
   * @return the width of a space character in pixels
   */
  private float computeSpaceWidth(BitmapFont font, GlyphLayout glyphLayout) {
    glyphLayout.setText(font, " ");
    return glyphLayout.width;
  }

  /**
   * Loads or retrieves a cached texture region for the given asset path via TextureMap.
   *
   * @param path the asset path to load
   * @return the loaded texture region, or null if loading failed
   */
  private TextureRegion getTextureRegion(String path) {
    return textureCache.computeIfAbsent(
        path,
        p -> {
          try {
            TextureRegion region = TextureMap.instance().cloneTexture(p);
            region
                .getTexture()
                .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return region;
          } catch (Exception e) {
            return null;
          }
        });
  }
}
