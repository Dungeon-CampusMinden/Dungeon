package contrib.hud.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
import contrib.hud.elements.richlabel.RichLabelLayout;
import contrib.hud.elements.richlabel.RichLabelParser;
import contrib.hud.elements.richlabel.Run;
import core.utils.FontSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * A rich-text label for libGDX Scene2d that supports inline images, block images, color changes,
 * font size overrides, and text effects.
 *
 * <p>Markup tags supported in the input text:
 *
 * <ul>
 *   <li>{@code [img=path/to/image.png]} - renders an inline image scaled to the font line height.
 *   <li>{@code [img-block path=image.png]} or {@code [img-block path=image.png width=300]} or
 *       {@code [img-block path=image.png width=50%]} - renders a block-level image on its own line,
 *       centered horizontally. Width can be pixels or a percentage of the container width. Aspect
 *       ratio is always preserved. Defaults to full container width.
 *   <li>{@code [color=red]} or {@code [color=#ff0000]} - changes the text color for subsequent
 *       text.
 *   <li>{@code [/color]} - resets the text color to the default.
 *   <li>{@code [size=24]} - changes the font size for subsequent text.
 *   <li>{@code [/size]} - resets the font size to the default.
 *   <li>{@code [shake]} or {@code [shake strength=1.5]} or {@code [shake strength=1.5 speed=0.5]} -
 *       applies a shake effect to subsequent runs. Named parameters: {@code strength} (multiplier
 *       of default 1.3px) and {@code speed} (multiplier of default 10). Boolean parameters can be
 *       specified by name alone (presence = true, absence = false).
 *   <li>{@code [/shake]} - ends the shake effect.
 *   <li>{@code [word-space=1.5]} - multiplies the default word spacing by the given factor for all
 *       subsequent text. 1.0 is default spacing.
 *   <li>{@code [line-space=1.5]} - multiplies the default line height by the given factor for all
 *       subsequent lines. 1.0 is default spacing.
 *   <li>{@code [n]} - forces a line break at the current position.
 * </ul>
 *
 * <p>Tags with named parameters use space-separated {@code key=value} pairs. Boolean parameters are
 * specified by name alone (presence = true, absence = false). This convention applies to all tags
 * that accept named parameters ({@code [shake ...]}, {@code [img-block ...]}, and any future tags).
 *
 * <p>The label performs word-level flow layout: each word and each image is a "run" that is placed
 * left-to-right and wrapped to the next line when it would exceed the available width. Runs with
 * different font sizes are baseline-aligned within each line.
 */
public class RichLabel extends WidgetGroup implements Disposable {

  /** Suppresses invalidation while layout is in progress to prevent re-layout every frame. */
  private boolean layoutInProgress = false;

  private FontSpec fontSpec;
  private String text;
  private float computedPrefHeight;
  private float lastPrefHeightWidth;
  private boolean wrap = true;

  private final RichLabelParser parser = new RichLabelParser();
  private final RichLabelLayout layoutEngine = new RichLabelLayout();
  private List<Run> runs = new ArrayList<>();

  /** Accumulated time for shake effects, persists across layout calls. */
  private float shakeElapsed;

  /** Frame counter for choppy shake updates. */
  private int shakeFrameCounter;

  /** Only update shake positions every N frames for a more intense, choppy feel. */
  private static final int SHAKE_UPDATE_INTERVAL = 3;

  /** Actors that should be shaken each frame, rebuilt during layout. */
  private final List<RichLabelLayout.ShakeTarget> shakeTargets = new ArrayList<>();

  @Override
  protected void childrenChanged() {
    if (!layoutInProgress) {
      super.childrenChanged();
    }
  }

  /**
   * Creates a new RichLabel with the given text and font specification.
   *
   * @param text the markup text to display
   * @param fontSpec the font specification for default text rendering
   */
  public RichLabel(String text, FontSpec fontSpec) {
    this.fontSpec = fontSpec;
    this.text = text;
    runs = parser.parse(text);
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
   * Replaces the font specification used for default text rendering and triggers a re-layout. Does
   * not re-parse the text, preserving shake effect continuity.
   *
   * @param fontSpec the new font specification
   */
  public void setFontSpec(FontSpec fontSpec) {
    this.fontSpec = fontSpec;
    invalidateHierarchy();
  }

  /**
   * Replaces the displayed text with new markup text and re-parses it.
   *
   * @param text the new markup text
   */
  public void setText(String text) {
    this.text = text;
    parser.clearShakeCache();
    runs = parser.parse(text);
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
    return layoutEngine.computePrefWidth(runs, fontSpec);
  }

  @Override
  public void invalidate() {
    super.invalidate();
    computedPrefHeight = 0;
  }

  @Override
  protected void sizeChanged() {
    super.sizeChanged();
    // Width affects wrapping which affects height. If the parent assigned a new width,
    // invalidate so getPrefHeight is recomputed and the parent can re-allocate space.
    if (wrap && getWidth() > 0 && getWidth() != lastPrefHeightWidth) {
      computedPrefHeight = 0;
      invalidateHierarchy();
    }
  }

  @Override
  public float getPrefHeight() {
    float w = getWidth() > 0 ? getWidth() : getPrefWidth();
    if (computedPrefHeight <= 0 || w != lastPrefHeightWidth) {
      lastPrefHeightWidth = w;
      computedPrefHeight = layoutEngine.computePrefHeight(runs, fontSpec, w, wrap);
    }
    return computedPrefHeight;
  }

  @Override
  public void layout() {
    layoutInProgress = true;
    clearChildren();
    clearActions();
    shakeTargets.clear();
    float availableWidth = getWidth();
    if (availableWidth <= 0) {
      availableWidth = getPrefWidth();
    }
    var result = layoutEngine.layoutRuns(this, runs, fontSpec, availableWidth, wrap);
    computedPrefHeight = result.prefHeight();
    shakeTargets.addAll(result.shakeTargets());
    applyShakeOffsets();
    layoutInProgress = false;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (shakeTargets.isEmpty()) return;
    shakeElapsed += delta;
    shakeFrameCounter++;
    if (shakeFrameCounter % SHAKE_UPDATE_INTERVAL != 0) return;
    applyShakeOffsets();
  }

  /** Applies the current shake offsets to all shake targets based on {@link #shakeElapsed}. */
  private void applyShakeOffsets() {
    for (RichLabelLayout.ShakeTarget st : shakeTargets) {
      float t = (shakeElapsed + st.shake().phase()) * st.shake().speed();
      float offsetX = MathUtils.sin(t * 7.3f) * st.shake().strength();
      float offsetY = MathUtils.cos(t * 5.9f) * st.shake().strength();
      st.actor().setPosition(st.baseX() + offsetX, st.baseY() + offsetY);
    }
  }

  @Override
  public void dispose() {
    layoutEngine.clearTextureCache();
  }
}
