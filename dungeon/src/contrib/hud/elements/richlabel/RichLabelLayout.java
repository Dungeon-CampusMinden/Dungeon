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
import java.util.List;
import java.util.Map;

/**
 * Handles the flow layout of parsed {@link Run} tokens into Scene2d actors. Creates and positions
 * labels and images with baseline alignment and block-level image support.
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
   * Result of a layout pass.
   *
   * @param prefHeight the computed preferred height
   * @param shakeTargets actors that should receive shake animation
   */
  public record LayoutResult(float prefHeight, List<ShakeTarget> shakeTargets) {}

  /**
   * Computes the preferred inline width of the given runs (ignoring block images).
   *
   * @param runs the parsed runs
   * @param fontSpec the default font specification
   * @return the preferred width in pixels
   */
  public float computePrefWidth(List<Run> runs, FontSpec fontSpec) {
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
        BitmapFont runFont = fontForRun(tr, fontSpec);
        glyphLayout.setText(runFont, trimmed);
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
    BitmapFont defaultFont = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float defaultLineHeight = defaultFont.getLineHeight();
    float baseSpaceWidth = computeSpaceWidth(defaultFont, glyphLayout);
    float wordSpaceMul = 1f;
    float lineSpaceMul = 1f;

    float x = 0;
    List<Float> lineMaxHeight = new ArrayList<>();
    List<Float> lineSpaceMuls = new ArrayList<>();
    lineMaxHeight.add(defaultLineHeight);
    lineSpaceMuls.add(1f);

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);

      if (run instanceof SpacingRun sr) {
        if (sr.wordSpaceMultiplier() >= 0) wordSpaceMul = sr.wordSpaceMultiplier();
        if (sr.lineSpaceMultiplier() >= 0) lineSpaceMul = sr.lineSpaceMultiplier();
        continue;
      }

      float spaceWidth = baseSpaceWidth * wordSpaceMul;

      if (run instanceof LineBreakRun) {
        x = 0;
        lineMaxHeight.add(defaultLineHeight);
        lineSpaceMuls.add(lineSpaceMul);
        continue;
      }

      if (run instanceof TextRun tr) {
        BitmapFont runFont = fontForRun(tr, fontSpec);
        float runLineHeight = runFont.getLineHeight();

        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && x > 0) {
          x += spaceWidth;
        }
        glyphLayout.setText(runFont, trimmed);
        float runWidth = glyphLayout.width;

        if (wrap && x > 0 && x + runWidth > availableWidth) {
          x = 0;
          lineMaxHeight.add(defaultLineHeight);
          lineSpaceMuls.add(lineSpaceMul);
        }

        int currentLine = lineMaxHeight.size() - 1;
        if (runLineHeight > lineMaxHeight.get(currentLine)) {
          lineMaxHeight.set(currentLine, runLineHeight);
        }
        // Track the highest multiplier active on this line
        if (lineSpaceMul > lineSpaceMuls.get(currentLine)) {
          lineSpaceMuls.set(currentLine, lineSpaceMul);
        }
        x += runWidth;

      } else if (run instanceof ImageRun ir) {
        float imgHeight = defaultFont.getCapHeight() * IMAGE_SCALE;
        TextureRegion region = getTextureRegion(ir.path());
        float imgWidth =
            region != null
                ? (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight
                : 0;
        float leadGap = i > 0 ? IMAGE_GAP : 0;
        float trailGap = i < runs.size() - 1 ? IMAGE_GAP : 0;
        float totalImgWidth = imgWidth + leadGap + trailGap;

        if (wrap && x > 0 && x + totalImgWidth > availableWidth) {
          x = 0;
          lineMaxHeight.add(defaultLineHeight);
          lineSpaceMuls.add(lineSpaceMul);
        }
        x += totalImgWidth;

      } else if (run instanceof ImageBlockRun ibr) {
        if (x > 0) {
          x = 0;
          lineMaxHeight.add(0f);
          lineSpaceMuls.add(1f);
        }

        float imgWidth = resolveBlockImageWidth(ibr.widthSpec(), availableWidth);
        imgWidth = Math.min(imgWidth, availableWidth);

        TextureRegion region = getTextureRegion(ibr.path());
        float imgHeight = 0;
        if (region != null && region.getRegionWidth() > 0) {
          imgHeight = imgWidth * region.getRegionHeight() / region.getRegionWidth();
        }

        int currentLine = lineMaxHeight.size() - 1;
        lineMaxHeight.set(currentLine, imgHeight);

        if (i + 1 < runs.size()) {
          lineMaxHeight.add(defaultLineHeight);
          lineSpaceMuls.add(lineSpaceMul);
        }
        x = 0;
      }
    }

    // Apply line-space multiplier between lines (not after the last line)
    float totalHeight = 0;
    for (int i = 0; i < lineMaxHeight.size(); i++) {
      float mul = (i < lineMaxHeight.size() - 1) ? lineSpaceMuls.get(i) : 1f;
      totalHeight += lineMaxHeight.get(i) * mul;
    }
    return totalHeight;
  }

  /**
   * Performs flow layout of all runs, creating actors and adding them to the given group.
   *
   * @param group the widget group to add actors to
   * @param runs the parsed runs
   * @param fontSpec the default font specification
   * @param availableWidth the maximum width
   * @param wrap whether to wrap lines
   * @return the layout result with preferred height and shake targets
   */
  public LayoutResult layoutRuns(
      WidgetGroup group, List<Run> runs, FontSpec fontSpec, float availableWidth, boolean wrap) {
    BitmapFont defaultFont = FontHelper.getFont(fontSpec);
    GlyphLayout glyphLayout = new GlyphLayout();
    float defaultLineHeight = defaultFont.getLineHeight();
    float baseSpaceWidth = computeSpaceWidth(defaultFont, glyphLayout);
    float wordSpaceMul = 1f;
    float lineSpaceMul = 1f;

    float x = 0;
    float y = 0;

    List<PlacedRun> placed = new ArrayList<>();
    List<Float> lineMaxHeight = new ArrayList<>();
    List<Float> lineMaxAscent = new ArrayList<>();
    List<Float> lineMaxTopDist = new ArrayList<>();
    List<Float> lineSpaceMuls = new ArrayList<>();
    int currentLine = 0;
    lineMaxHeight.add(defaultLineHeight);
    lineMaxAscent.add(defaultFont.getCapHeight());
    lineMaxTopDist.add(defaultFont.getCapHeight() - defaultFont.getDescent());
    lineSpaceMuls.add(1f);

    for (int i = 0; i < runs.size(); i++) {
      Run run = runs.get(i);

      if (run instanceof SpacingRun sr) {
        if (sr.wordSpaceMultiplier() >= 0) wordSpaceMul = sr.wordSpaceMultiplier();
        if (sr.lineSpaceMultiplier() >= 0) lineSpaceMul = sr.lineSpaceMultiplier();
        continue;
      }

      float spaceWidth = baseSpaceWidth * wordSpaceMul;

      if (run instanceof LineBreakRun) {
        x = 0;
        y += lineMaxHeight.get(currentLine);
        currentLine++;
        lineMaxHeight.add(defaultLineHeight);
        lineMaxAscent.add(defaultFont.getCapHeight());
        lineMaxTopDist.add(defaultFont.getCapHeight() - defaultFont.getDescent());
        lineSpaceMuls.add(lineSpaceMul);
        continue;
      }

      if (run instanceof TextRun tr) {
        BitmapFont runFont = fontForRun(tr, fontSpec);
        float runLineHeight = runFont.getLineHeight();
        float runAscent = runFont.getCapHeight();

        String trimmed = tr.word().stripLeading();
        boolean hasLeadingSpace = tr.word().length() > trimmed.length();
        if (hasLeadingSpace && x > 0) {
          x += spaceWidth;
        }
        glyphLayout.setText(runFont, trimmed);
        float runWidth = glyphLayout.width;

        if (wrap && x > 0 && x + runWidth > availableWidth) {
          x = 0;
          y += lineMaxHeight.get(currentLine);
          currentLine++;
          lineMaxHeight.add(defaultLineHeight);
          lineMaxAscent.add(defaultFont.getCapHeight());
          lineMaxTopDist.add(defaultFont.getCapHeight() - defaultFont.getDescent());
          lineSpaceMuls.add(lineSpaceMul);
        }

        if (runLineHeight > lineMaxHeight.get(currentLine)) {
          lineMaxHeight.set(currentLine, runLineHeight);
        }
        if (runAscent > lineMaxAscent.get(currentLine)) {
          lineMaxAscent.set(currentLine, runAscent);
        }
        float runTopDist = runAscent - runFont.getDescent();
        if (runTopDist > lineMaxTopDist.get(currentLine)) {
          lineMaxTopDist.set(currentLine, runTopDist);
        }
        if (lineSpaceMul > lineSpaceMuls.get(currentLine)) {
          lineSpaceMuls.set(currentLine, lineSpaceMul);
        }

        placed.add(new PlacedRun(run, x, currentLine, runWidth, runLineHeight));
        x += runWidth;

      } else if (run instanceof ImageRun ir) {
        float imgHeight = defaultFont.getCapHeight() * IMAGE_SCALE;
        TextureRegion region = getTextureRegion(ir.path());
        float imgWidth =
            region != null
                ? (float) region.getRegionWidth() / region.getRegionHeight() * imgHeight
                : 0;
        float leadGap = i > 0 ? IMAGE_GAP : 0;
        float trailGap = i < runs.size() - 1 ? IMAGE_GAP : 0;
        float totalImgWidth = imgWidth + leadGap + trailGap;

        if (wrap && x > 0 && x + totalImgWidth > availableWidth) {
          x = 0;
          y += lineMaxHeight.get(currentLine);
          currentLine++;
          lineMaxHeight.add(defaultLineHeight);
          lineMaxAscent.add(defaultFont.getCapHeight());
          lineMaxTopDist.add(defaultFont.getCapHeight() - defaultFont.getDescent());
          lineSpaceMuls.add(lineSpaceMul);
        }

        x += leadGap;
        placed.add(new PlacedRun(run, x, currentLine, imgWidth, imgHeight));
        x += imgWidth + trailGap;

      } else if (run instanceof ImageBlockRun ibr) {
        if (x > 0) {
          x = 0;
          y += lineMaxHeight.get(currentLine);
          currentLine++;
          lineMaxHeight.add(0f);
          lineMaxAscent.add(0f);
          lineMaxTopDist.add(0f);
          lineSpaceMuls.add(1f);
        }

        float imgWidth = resolveBlockImageWidth(ibr.widthSpec(), availableWidth);
        imgWidth = Math.min(imgWidth, availableWidth);

        TextureRegion region = getTextureRegion(ibr.path());
        float imgHeight = 0;
        if (region != null && region.getRegionWidth() > 0) {
          imgHeight = imgWidth * region.getRegionHeight() / region.getRegionWidth();
        }

        float blockX = (availableWidth - imgWidth) / 2f;
        lineMaxHeight.set(currentLine, imgHeight);
        placed.add(new PlacedRun(run, blockX, currentLine, imgWidth, imgHeight));

        // Advance past the block image line. Only create the next line if there are
        // more runs to lay out; otherwise the empty trailing line inflates the height.
        if (i + 1 < runs.size()) {
          y += imgHeight;
          currentLine++;
          lineMaxHeight.add(defaultLineHeight);
          lineMaxAscent.add(defaultFont.getCapHeight());
          lineMaxTopDist.add(defaultFont.getCapHeight() - defaultFont.getDescent());
          lineSpaceMuls.add(lineSpaceMul);
        }
        x = 0;
      }
    }

    // Apply line-space multiplier between lines (not after the last line)
    float totalHeight = 0;
    for (int i = 0; i < lineMaxHeight.size(); i++) {
      float mul = (i < lineMaxHeight.size() - 1) ? lineSpaceMuls.get(i) : 1f;
      totalHeight += lineMaxHeight.get(i) * mul;
    }

    // Compute y-offset for each line using scaled heights
    float[] lineY = new float[lineMaxHeight.size()];
    lineY[0] = 0;
    for (int i = 1; i < lineY.length; i++) {
      float mul = (i - 1 < lineMaxHeight.size() - 1) ? lineSpaceMuls.get(i - 1) : 1f;
      lineY[i] = lineY[i - 1] + lineMaxHeight.get(i - 1) * mul;
    }

    List<ShakeTarget> shakeTargets = new ArrayList<>();

    for (PlacedRun pr : placed) {
      int line = pr.line();
      float lineH = lineMaxHeight.get(line);
      float lineTopDist = lineMaxTopDist.get(line);

      if (pr.run() instanceof TextRun tr) {
        String trimmed = tr.word().stripLeading();
        if (trimmed.isEmpty()) continue;
        FontSpec runFontSpec = fontSpecForRun(tr, fontSpec);
        BitmapFont runFont = FontHelper.getFont(runFontSpec);
        float runCapHeight = runFont.getCapHeight();
        float runDescent = runFont.getDescent();

        Label label = new Label(trimmed, new Label.LabelStyle(runFont, null));
        label.setAlignment(Align.topLeft);
        float lineTop = totalHeight - lineY[line];
        float actorY = lineTop - lineTopDist - pr.height() - runDescent + runCapHeight;
        label.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(label);

        if (tr.shake() != null) {
          shakeTargets.add(new ShakeTarget(label, pr.x(), actorY, tr.shake()));
        }

      } else if (pr.run() instanceof ImageRun ir) {
        TextureRegion region = getTextureRegion(ir.path());
        if (region == null) continue;
        Image image = new Image(new TextureRegionDrawable(region));
        float yOffset = (lineH - pr.height()) / 2f;
        float actorY = totalHeight - lineY[line] - lineH + yOffset;
        image.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(image);

        if (ir.shake() != null) {
          shakeTargets.add(new ShakeTarget(image, pr.x(), actorY, ir.shake()));
        }

      } else if (pr.run() instanceof ImageBlockRun ibr) {
        TextureRegion region = getTextureRegion(ibr.path());
        if (region == null) continue;
        Image image = new Image(new TextureRegionDrawable(region));
        float actorY = totalHeight - lineY[line] - pr.height();
        image.setBounds(pr.x(), actorY, pr.width(), pr.height());
        group.addActor(image);
      }
    }

    return new LayoutResult(totalHeight, shakeTargets);
  }

  /** Clears the texture cache. */
  public void clearTextureCache() {
    textureCache.clear();
  }

  // -- Private helpers --

  private record PlacedRun(Run run, float x, int line, float width, float height) {}

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
    if (tr.sizeOverride() > 0) {
      return FontHelper.getFont(fontSpec.withSize(tr.sizeOverride()));
    }
    return FontHelper.getFont(fontSpec);
  }

  static FontSpec fontSpecForRun(TextRun tr, FontSpec fontSpec) {
    Color color = tr.color() != null ? tr.color() : fontSpec.color();
    FontSpec spec = fontSpec.withColor(color);
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
}
