package contrib.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import core.utils.FontHelper;
import core.utils.FontSpec;
import core.utils.components.draw.TextureMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the flow layout of parsed {@link Run} tokens into Scene2d actors.
 *
 * <h2>Baseline-anchored line model</h2>
 *
 * <p>Each line owns a single shared <em>baseline</em>. All text runs on a line are placed so their
 * font baselines coincide with that baseline regardless of font size. Inline images are anchored
 * <em>to</em> the baseline:
 *
 * <ul>
 *   <li>An image whose render height is &le; the line's text band (cap-height + |descent| of the
 *       largest text run) sits with its bottom edge on the baseline.
 *   <li>An image taller than the text band keeps its bottom anchored as if it were the size of the
 *       text band, then spreads the surplus height equally above and below - visually centering it
 *       around the text band while still appearing baseline-anchored.
 * </ul>
 *
 * <p>Per line we therefore track {@code textAbove} / {@code textBelow} (the text band) and the list
 * of image heights placed on the line. The actual {@code aboveBaseline} / {@code belowBaseline}
 * distances are derived once the line is sealed (i.e. after the next wrap / line break / end of
 * input), so an image can wrap onto a line whose final text band is not yet known.
 */
public class RichLabelLayout {

  /** Scale factor for inline images relative to font cap height. */
  static final float IMAGE_SCALE = 1.5f;

  /** Gap in pixels between an inline image and adjacent runs. */
  static final float IMAGE_GAP = 3f;

  private final Map<String, TextureRegion> textureCache = new HashMap<>();

  /**
   * A record pairing an actor with its base position and shake parameters.
   *
   * @param actor the Scene2d actor to shake
   * @param baseX the resting x position
   * @param baseY the resting y position
   * @param shake the shake effect parameters (includes phase)
   */
  public record ShakeTarget(Actor actor, float baseX, float baseY, ShakeEffect shake) {}

  /**
   * A record pairing a laid-out actor with its source run and full text content.
   *
   * @param actor the Scene2d actor
   * @param run the source run that produced this actor
   * @param fullText the full text content for TextRuns, or null for non-text runs
   */
  public record PlacedActor(Actor actor, Run run, String fullText) {}

  /**
   * Result of a layout pass.
   *
   * @param prefHeight the computed preferred height
   * @param shakeTargets actors that should receive shake animation
   * @param placedActors ordered list of actors paired with their source runs
   */
  public record LayoutResult(
      float prefHeight, List<ShakeTarget> shakeTargets, List<PlacedActor> placedActors) {}

  /**
   * Computes the preferred inline width of the given runs (ignoring block images).
   *
   * <p>The returned value is the width of the widest line produced by hard line breaks ({@code [n]}
   * / {@link LineBreakRun}) and block images (which force a line break). Soft wrapping based on an
   * available width is not applied here — that is the caller's responsibility via {@link
   * #computePrefHeight} once a width has been assigned. This means a label whose text contains
   * explicit line breaks will report a preferred width matching its longest line rather than the
   * sum of all words on a single line.
   *
   * @param runs the parsed runs
   * @param fontSpec the default font specification
   * @return the preferred width in pixels
   */
  public float computePrefWidth(List<Run> runs, FontSpec fontSpec) {
    BitmapFont font = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float spaceWidth = computeSpaceWidth(font, glyphLayout);
    float wordSpaceMul = 1f;
    float lineWidth = 0;
    float maxWidth = 0;

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);
      if (run instanceof SpacingRun sr) {
        if (sr.wordSpaceMultiplier() >= 0) wordSpaceMul = sr.wordSpaceMultiplier();
        continue;
      }
      if (run instanceof LineBreakRun || run instanceof ImageBlockRun) {
        if (lineWidth > maxWidth) maxWidth = lineWidth;
        lineWidth = 0;
        continue;
      }
      if (run instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && lineWidth > 0) {
          lineWidth += spaceWidth * wordSpaceMul;
        }
        BitmapFont runFont = fontForRun(tr, fontSpec);
        glyphLayout.setText(runFont, trimmed);
        lineWidth += glyphLayout.width;
      } else if (run instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir);
        if (region != null) {
          BitmapFont imgFont = fontForImage(ir, fontSpec);
          float imgHeight = imgFont.getCapHeight() * IMAGE_SCALE * ir.scale();
          float imgWidth = (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight;
          if (i > 0 && !ir.noGapLeft()) imgWidth += IMAGE_GAP;
          if (i < runs.size() - 1 && !ir.noGapRight()) imgWidth += IMAGE_GAP;
          lineWidth += imgWidth;
        }
      }
    }
    if (lineWidth > maxWidth) maxWidth = lineWidth;
    return maxWidth;
  }

  /**
   * Computes the preferred height without creating any actors (measurement-only pass).
   *
   * @param runs the parsed runs
   * @param fontSpec the default font specification
   * @param availableWidth the maximum width
   * @param wrap whether to wrap lines
   * @return the total height in pixels
   */
  public float computePrefHeight(
      List<Run> runs, FontSpec fontSpec, float availableWidth, boolean wrap) {
    return totalHeight(buildLines(runs, fontSpec, availableWidth, wrap, null));
  }

  /**
   * Performs flow layout of all runs, creating actors and adding them to the given group.
   *
   * @param group the widget group to add actors to
   * @param runs the parsed runs
   * @param fontSpec the default font specification
   * @param availableWidth the maximum width
   * @param wrap whether to wrap lines
   * @param align horizontal alignment of each line, one of {@link Align#left}, {@link
   *     Align#center}, {@link Align#right} (only the horizontal bits are inspected). Block images
   *     are always centered regardless of this setting.
   * @return the layout result with preferred height and shake targets
   */
  public LayoutResult layoutRuns(
      WidgetGroup group,
      List<Run> runs,
      FontSpec fontSpec,
      float availableWidth,
      boolean wrap,
      int align) {
    List<PlacedRun> placed = new ArrayList<>();
    List<LineMetrics> lines = buildLines(runs, fontSpec, availableWidth, wrap, placed);

    // Apply per-line horizontal alignment by shifting placed runs (excluding block images,
    // which are independently centered against availableWidth). Each line may carry its own
    // align (set by [align=...] tags during parsing); lines without an explicit override use
    // the {@code align} value passed in here.
    int lineCount = lines.size();
    int[] perLineAlign = new int[lineCount];
    boolean anyNonLeft = false;
    for (int i = 0; i < lineCount; i++) {
      int la = lines.get(i).align;
      perLineAlign[i] = (la >= 0) ? la : align;
      if ((perLineAlign[i] & (Align.center | Align.right)) != 0) anyNonLeft = true;
    }
    if (anyNonLeft) {
      float[] lineRight = new float[lineCount];
      for (PlacedRun pr : placed) {
        if (pr.run() instanceof ImageBlockRun) continue;
        float right = pr.x() + pr.width();
        if (right > lineRight[pr.line()]) lineRight[pr.line()] = right;
      }
      float[] offsets = new float[lineCount];
      for (int i = 0; i < lineCount; i++) {
        int la = perLineAlign[i];
        if ((la & (Align.center | Align.right)) == 0) continue;
        float slack = availableWidth - lineRight[i];
        if (slack <= 0) continue;
        if ((la & Align.right) != 0) offsets[i] = slack;
        else if ((la & Align.center) != 0) offsets[i] = slack / 2f;
      }
      for (int i = 0; i < placed.size(); i++) {
        PlacedRun pr = placed.get(i);
        if (pr.run() instanceof ImageBlockRun) continue;
        float off = offsets[pr.line()];
        if (off != 0f) {
          placed.set(i, new PlacedRun(pr.run(), pr.x() + off, pr.line(), pr.width(), pr.height()));
        }
      }
    }

    // Seal: derive baseline metrics for every line.
    int n = lines.size();
    float[] above = new float[n];
    float[] textAbove = new float[n];
    float[] textBelow = new float[n];
    float[] lineHeights = new float[n];
    for (int i = 0; i < n; i++) {
      LineMetrics lm = lines.get(i);
      above[i] = lm.aboveBaseline();
      textAbove[i] = lm.textAbove;
      textBelow[i] = lm.textBelow;
      lineHeights[i] = lm.lineHeight();
    }

    // Total height with line-space multipliers between lines (not after the last).
    float totalHeight = 0;
    for (int i = 0; i < n; i++) {
      float mul = (i < n - 1) ? lines.get(i).lineSpaceMul : 1f;
      totalHeight += lineHeights[i] * mul;
    }

    // Y of the top edge of each line, measured from the bottom of the widget (Scene2d Y up).
    float[] lineTopY = new float[n];
    float widgetHeight = group.getHeight();
    float verticalSlack = Math.max(0f, widgetHeight - totalHeight);
    float cursorTop = totalHeight + verticalSlack / 2f;
    for (int i = 0; i < n; i++) {
      lineTopY[i] = cursorTop;
      float mul = (i < n - 1) ? lines.get(i).lineSpaceMul : 1f;
      cursorTop -= lineHeights[i] * mul;
    }

    List<ShakeTarget> shakeTargets = new ArrayList<>();
    List<PlacedActor> placedActors = new ArrayList<>();
    Map<Run, Actor> runToActor = new IdentityHashMap<>();
    Map<Run, String> runToFullText = new IdentityHashMap<>();

    for (PlacedRun pr : placed) {
      int line = pr.line();
      float lineTop = lineTopY[line];
      float baseline = lineTop - above[line];

      if (pr.run() instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        if (trimmed.isEmpty()) continue;
        FontSpec runFontSpec = fontSpecForRun(tr, fontSpec);
        BitmapFont runFont = FontHelper.getFont(runFontSpec);

        Label label = new Label(trimmed, new Label.LabelStyle(runFont, null));
        label.setAlignment(Align.topLeft);

        // libGDX Label (Align.topLeft, labelHeight = font.lineHeight) renders the visible glyph
        // baseline exactly at the label's bottom edge: cache.y = height + descent and
        // cache.addText treats y as text-top, so baseline = y - ascent = height + descent -
        // (lineHeight - |descent|) = 0 in label-local coords. Anchoring actorY = baseline puts
        // every run's visible baseline on the shared line baseline regardless of font size,
        // while keeping labelTop = baseline + lh ≤ lineTop. The widget bounds therefore fully
        // contain every label — only glyph descenders fall outside the bounds (matching the
        // legacy layout behaviour).
        float actorY = baseline;
        label.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(label);
        runToActor.put(tr, label);
        runToFullText.put(tr, trimmed);

        if (tr.shake() != null) {
          shakeTargets.add(new ShakeTarget(label, pr.x(), actorY, tr.shake()));
        }

      } else if (pr.run() instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir);
        if (region == null) continue;
        Image image = new Image(new TextureRegionDrawable(region));
        float imgH = pr.height();
        float band = textAbove[line] + textBelow[line];
        // Bottom edge sits on the baseline for small images; tall images split the overflow
        // equally above and below the text band so they remain visually centered on the text.
        float imgBelow = (imgH <= band) ? 0f : (imgH - band) / 2f;
        float actorY = baseline - imgBelow;
        image.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(image);
        runToActor.put(ir, image);

        if (ir.shake() != null) {
          shakeTargets.add(new ShakeTarget(image, pr.x(), actorY, ir.shake()));
        }

      } else if (pr.run() instanceof ImageBlockRun ibr) {
        TextureRegion region = getTextureRegion(ibr.path());
        if (region == null) continue;
        Image image = new Image(new TextureRegionDrawable(region));
        float actorY = lineTop - pr.height();
        image.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(image);
        runToActor.put(ibr, image);
      }
    }

    // Build placedActors in original run order, including control runs
    for (Run run : runs) {
      if (run instanceof TypewriterRun || run instanceof PauseRun) {
        placedActors.add(new PlacedActor(null, run, null));
      } else {
        Actor actor = runToActor.get(run);
        if (actor != null) {
          placedActors.add(new PlacedActor(actor, run, runToFullText.get(run)));
        }
      }
    }

    return new LayoutResult(totalHeight, shakeTargets, placedActors);
  }

  /** Clears the texture cache. */
  public void clearTextureCache() {
    textureCache.forEach((k, v) -> v.getTexture().dispose());
    textureCache.clear();
  }

  // -- Private helpers --

  private record PlacedRun(Run run, float x, int line, float width, float height) {}

  /** Mutable per-line accumulator used by the flow pass; sealed during the placement pass. */
  private static final class LineMetrics {
    float textAbove;
    float textBelow;
    float lineSpaceMul = 1f;

    /**
     * Per-line horizontal alignment override. {@code -1} means "use the alignment passed to the
     * layout call" (i.e. the programmatic default of the {@link contrib.hud.elements.RichLabel}).
     */
    int align = -1;

    final List<Float> imageHeights = new ArrayList<>();

    private LineMetrics(float textAbove, float textBelow) {
      this.textAbove = textAbove;
      this.textBelow = textBelow;
    }

    /**
     * Standard text line: seeded with the default font's natural line height as the above-baseline
     * extent. {@code textBelow} stays {@code 0} because libGDX renders the visible baseline at the
     * label's bottom edge - glyph descenders intentionally render below the line bounds (matching
     * the legacy layout). Using the full {@code lineHeight} here keeps baseline-to-baseline
     * distance equal to {@code font.getLineHeight()} for text-only paragraphs, so widget bounds
     * tightly contain every label.
     *
     * @param defaultFont font used as baseline for a fresh text line
     * @return initialized line metrics for a text line
     */
    static LineMetrics forText(BitmapFont defaultFont) {
      return new LineMetrics(defaultFont.getLineHeight(), 0f);
    }

    /**
     * Used only as a forced break before a block image when there is no preceding text on the
     * current line. Contributes nothing to the line height by itself.
     *
     * @return zero-height metrics used as a separator line
     */
    static LineMetrics forBlockSeparator() {
      return new LineMetrics(0f, 0f);
    }

    void addText(BitmapFont runFont) {
      // See forText() for why we use lineHeight here (rather than capHeight + |descent|).
      textAbove = Math.max(textAbove, runFont.getLineHeight());
    }

    /**
     * Marks this line as fully occupied by a block image of the given height. Block images are
     * neither baselined nor split: the line's full height equals the image height.
     *
     * @param imgHeight rendered block image height in pixels
     */
    void makeBlock(float imgHeight) {
      textAbove = imgHeight;
      textBelow = 0f;
      imageHeights.clear();
    }

    /**
     * Computes the above-baseline extent required by the current line.
     *
     * @return maximum above-baseline extent in pixels
     */
    float aboveBaseline() {
      // textAbove already encodes the line's natural above-baseline extent (max font.lineHeight).
      // For images we anchor the bottom to the baseline (small) or split the surplus equally
      // above and below the text band (tall):
      //   small (h <= band): imgAbove = h,            imgBelow = 0
      //   tall  (h >  band): imgAbove = (band + h)/2, imgBelow = (h - band)/2
      float band = textAbove + textBelow;
      float above = textAbove;
      for (float h : imageHeights) {
        float imgAbove = (h <= band) ? h : (band + h) / 2f;
        if (imgAbove > above) above = imgAbove;
      }
      return above;
    }

    /**
     * Computes the below-baseline extent required by the current line.
     *
     * @return maximum below-baseline extent in pixels
     */
    float belowBaseline() {
      // Text-only lines contribute nothing below the baseline (descenders render outside the
      // line bounds, matching the legacy layout). Only tall images push the line bottom down.
      float band = textAbove + textBelow;
      float below = textBelow;
      for (float h : imageHeights) {
        float imgBelow = (h <= band) ? 0f : (h - band) / 2f;
        if (imgBelow > below) below = imgBelow;
      }
      return below;
    }

    /**
     * Returns the full line height as above-baseline plus below-baseline extent.
     *
     * @return line height in pixels
     */
    float lineHeight() {
      return aboveBaseline() + belowBaseline();
    }
  }

  /**
   * Single flow-layout pass shared by {@link #computePrefHeight} and {@link #layoutRuns}. Builds
   * the per-line {@link LineMetrics} list and, when {@code placedOut} is non-null, records the
   * x-position / line / size of each placed run.
   *
   * @param runs parsed rich-text runs to place
   * @param fontSpec default font specification
   * @param availableWidth width available for wrapping
   * @param wrap whether soft wrapping is enabled
   * @param placedOut optional output list for placed run geometry (null to skip recording)
   * @return computed line metrics for the full layout pass
   */
  private List<LineMetrics> buildLines(
      List<Run> runs,
      FontSpec fontSpec,
      float availableWidth,
      boolean wrap,
      List<PlacedRun> placedOut) {
    BitmapFont defaultFont = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float baseSpaceWidth = computeSpaceWidth(defaultFont, glyphLayout);
    float wordSpaceMul = 1f;
    float lineSpaceMul = 1f;
    int currentAlign = -1; // -1 = inherit programmatic default

    float x = 0;
    List<LineMetrics> lines = new ArrayList<>();
    lines.add(LineMetrics.forText(defaultFont));

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);

      if (run instanceof SpacingRun sr) {
        if (sr.wordSpaceMultiplier() >= 0) wordSpaceMul = sr.wordSpaceMultiplier();
        if (sr.lineSpaceMultiplier() >= 0) lineSpaceMul = sr.lineSpaceMultiplier();
        continue;
      }

      if (run instanceof AlignRun ar) {
        currentAlign = ar.align();
        // Apply immediately to the current (in-progress) line so an [align] tag at the start
        // of a line takes effect for that line, not just the next one.
        lines.get(lines.size() - 1).align = currentAlign;
        continue;
      }

      float spaceWidth = baseSpaceWidth * wordSpaceMul;

      if (run instanceof LineBreakRun) {
        x = 0;
        lines.add(LineMetrics.forText(defaultFont));
        LineMetrics nl = lines.get(lines.size() - 1);
        nl.lineSpaceMul = lineSpaceMul;
        nl.align = currentAlign;
        continue;
      }

      if (run instanceof TextRun tr) {
        BitmapFont runFont = fontForRun(tr, fontSpec);
        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && x > 0) {
          x += spaceWidth;
        }
        glyphLayout.setText(runFont, trimmed);
        float runWidth = glyphLayout.width;

        if (wrap && x > 0 && x + runWidth > availableWidth) {
          x = 0;
          lines.add(LineMetrics.forText(defaultFont));
          lines.get(lines.size() - 1).align = currentAlign;
        }

        LineMetrics current = lines.get(lines.size() - 1);
        current.addText(runFont);
        current.lineSpaceMul = Math.max(current.lineSpaceMul, lineSpaceMul);

        if (placedOut != null) {
          placedOut.add(new PlacedRun(run, x, lines.size() - 1, runWidth, runFont.getLineHeight()));
        }
        x += runWidth;

      } else if (run instanceof ImageRun ir) {
        BitmapFont imgFont = fontForImage(ir, fontSpec);
        float imgHeight = imgFont.getCapHeight() * IMAGE_SCALE * ir.scale();
        TextureRegion region = getTextureRegion(ir);
        float imgWidth =
            region != null
                ? (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight
                : 0;
        float leadGap = (i > 0 && !ir.noGapLeft()) ? IMAGE_GAP : 0;
        float trailGap = (i < runs.size() - 1 && !ir.noGapRight()) ? IMAGE_GAP : 0;
        float totalImgWidth = imgWidth + leadGap + trailGap;

        if (wrap && x > 0 && x + totalImgWidth > availableWidth) {
          x = 0;
          lines.add(LineMetrics.forText(defaultFont));
          lines.get(lines.size() - 1).align = currentAlign;
        }

        LineMetrics current = lines.get(lines.size() - 1);
        current.imageHeights.add(imgHeight);
        current.lineSpaceMul = Math.max(current.lineSpaceMul, lineSpaceMul);

        if (placedOut != null) {
          x += leadGap;
          placedOut.add(new PlacedRun(run, x, lines.size() - 1, imgWidth, imgHeight));
          x += imgWidth + trailGap;
        } else {
          x += totalImgWidth;
        }

      } else if (run instanceof ImageBlockRun ibr) {
        if (x > 0) {
          x = 0;
          lines.add(LineMetrics.forBlockSeparator());
        }

        float imgWidth =
            Math.min(resolveBlockImageWidth(ibr.widthSpec(), availableWidth), availableWidth);
        TextureRegion region = getTextureRegion(ibr.path());
        float imgHeight = 0;
        if (region != null && region.getRegionWidth() > 0) {
          imgHeight = imgWidth * region.getRegionHeight() / region.getRegionWidth();
        }

        LineMetrics current = lines.get(lines.size() - 1);
        current.makeBlock(imgHeight);

        if (placedOut != null) {
          float blockX = (availableWidth - imgWidth) / 2f;
          placedOut.add(new PlacedRun(run, blockX, lines.size() - 1, imgWidth, imgHeight));
        }

        // Only create a successor line if more runs follow, to avoid an empty trailing line.
        if (i + 1 < runs.size()) {
          lines.add(LineMetrics.forText(defaultFont));
          LineMetrics nl = lines.get(lines.size() - 1);
          nl.lineSpaceMul = lineSpaceMul;
          nl.align = currentAlign;
        }
        x = 0;
      }
    }

    return lines;
  }

  private static float totalHeight(List<LineMetrics> lines) {
    float total = 0;
    for (int i = 0; i < lines.size(); i++) {
      float mul = (i < lines.size() - 1) ? lines.get(i).lineSpaceMul : 1f;
      total += lines.get(i).lineHeight() * mul;
    }
    return total;
  }

  static float resolveBlockImageWidth(String widthSpec, float availableWidth) {
    if (widthSpec == null || widthSpec.isBlank()) {
      return availableWidth;
    }
    if (widthSpec.endsWith("%")) {
      try {
        float pct = Float.parseFloat(widthSpec.substring(0, widthSpec.length() - 1).trim());
        return availableWidth * (pct / 100f);
      } catch (NumberFormatException e) {
        return availableWidth;
      }
    }
    try {
      return Float.parseFloat(widthSpec.trim());
    } catch (NumberFormatException e) {
      return availableWidth;
    }
  }

  private static float computeSpaceWidth(BitmapFont font, GlyphLayout glyphLayout) {
    glyphLayout.setText(font, " ");
    return glyphLayout.width;
  }

  static BitmapFont fontForRun(TextRun tr, FontSpec fontSpec) {
    return FontHelper.getFont(applyOverrides(tr, fontSpec));
  }

  static BitmapFont fontForImage(ImageRun ir, FontSpec fontSpec) {
    if (ir.sizeOverride() > 0) {
      return FontHelper.getFont(fontSpec.withSize(ir.sizeOverride()));
    }
    return FontHelper.getFont(fontSpec);
  }

  static FontSpec fontSpecForRun(TextRun tr, FontSpec fontSpec) {
    Color color = tr.color() != null ? tr.color() : fontSpec.color();
    return applyOverrides(tr, fontSpec).withColor(color);
  }

  /**
   * Applies the size and font-path overrides from the given {@link TextRun} to the supplied base
   * {@link FontSpec}, leaving color untouched.
   *
   * @param tr the text run providing potential overrides
   * @param fontSpec the base font specification
   * @return a new {@link FontSpec} reflecting the run's overrides
   */
  private static FontSpec applyOverrides(TextRun tr, FontSpec fontSpec) {
    FontSpec spec = fontSpec;
    if (tr.fontPathOverride() != null) {
      spec = spec.withPath(tr.fontPathOverride());
    }
    if (tr.sizeOverride() > 0) {
      spec = spec.withSize(tr.sizeOverride());
    }
    return spec;
  }

  TextureRegion getTextureRegion(String path) {
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

  /**
   * Returns the {@link TextureRegion} described by the given {@link ImageRun}, applying the run's
   * sub-region if one is set. Falls back to the full texture when no sub-region is configured.
   * Cached results are keyed by path plus sub-region coordinates so the same texture can be sliced
   * into many independently cached sub-regions.
   *
   * @param ir the image run to resolve
   * @return the matching texture region, or {@code null} if the texture could not be loaded
   */
  TextureRegion getTextureRegion(ImageRun ir) {
    if (!ir.hasSubRegion()) return getTextureRegion(ir.path());
    TextureRegion base = getTextureRegion(ir.path());
    if (base == null) return null;
    String cacheKey =
        ir.path()
            + "@"
            + ir.regionX()
            + ","
            + ir.regionY()
            + ","
            + ir.regionW()
            + ","
            + ir.regionH();
    return textureCache.computeIfAbsent(
        cacheKey,
        k -> {
          // The base texture is produced by TextureMap#cloneTexture via an FBO. The base
          // TextureRegion compensates for the FBO's V-axis convention via flip(false, true).
          // A freshly-constructed sub-region using the same source-image (top-left) pixel
          // coordinates points at the correct rectangle but inherits the un-flipped V
          // orientation, so it would render upside-down. Re-apply the same vertical flip so
          // the cropped icon renders right-side up at the correct location.
          TextureRegion sub =
              new TextureRegion(
                  base.getTexture(), ir.regionX(), ir.regionY(), ir.regionW(), ir.regionH());
          sub.flip(false, true);
          return sub;
        });
  }
}
