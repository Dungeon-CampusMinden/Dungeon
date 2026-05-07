package contrib.hud.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import contrib.hud.dialogs.DialogDesign;
import contrib.hud.elements.richlabel.PauseRun;
import contrib.hud.elements.richlabel.RichLabelLayout;
import contrib.hud.elements.richlabel.RichLabelParser;
import contrib.hud.elements.richlabel.Run;
import contrib.hud.elements.richlabel.TypewriterRun;
import core.utils.FontSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * A rich-text label for libGDX Scene2d that supports inline images, block images, color changes,
 * font size overrides, text effects, and typewriter mode.
 *
 * <p>Markup tags supported in the input text:
 *
 * <ul>
 *   <li>{@code [img=path/to/image.png]} - renders an inline image scaled to the font line height.
 *   <li>{@code [img path=image.png]}, {@code [img path=image.png scale=1.5]} or {@code [img
 *       path=sheet.png x=64 y=0 w=64 h=64 scale=1.5]} - the named-parameter form of the inline
 *       image tag. The optional {@code x}, {@code y}, {@code w}, {@code h} parameters select a
 *       sub-region of the texture for use in spritesheets; all four must be supplied together. The
 *       optional {@code scale} parameter is a multiplier on top of the font-derived image height,
 *       letting an image render larger (or smaller) than the surrounding text while preserving its
 *       aspect ratio.
 *   <li>{@code [img-block path=image.png]} or {@code [img-block path=image.png width=300]} or
 *       {@code [img-block path=image.png width=50%]} - renders a block-level image on its own line,
 *       centered horizontally. Width can be pixels or a percentage of the container width. Aspect
 *       ratio is always preserved. Defaults to full container width.
 *   <li>{@code [key code=<int>]} or {@code [key code=<int> type=<inputMethod>]} or {@code [key
 *       code=<int> outline]} - renders an input-prompt graphic (keyboard key, mouse button, ...)
 *       inline. Behaves exactly like {@code [img]} regarding sizing, spacing and shake. The
 *       required {@code code} parameter holds the libGDX {@code Input.Keys} or {@code
 *       Input.Buttons} integer. The optional {@code type} parameter selects the lookup space:
 *       {@code keyboard} (default), {@code mouse}, {@code playstation}, {@code xbox} or {@code
 *       touch}. Unknown codes / unsupported input methods log a warning and render nothing.
 *   <li>{@code [color=red]} or {@code [color=#ff0000]} - changes the text color for subsequent
 *       text.
 *   <li>{@code [/color]} - resets the text color to the default.
 *   <li>{@code [size=24]} - changes the font size for subsequent text.
 *   <li>{@code [/size]} - resets the font size to the default.
 *   <li>{@code [shake]} or {@code [shake strength=1.5]} or {@code [shake strength=1.5 speed=0.5]} -
 *       applies a shake effect to subsequent runs. Named parameters: {@code strength} and {@code
 *       speed}.
 *   <li>{@code [/shake]} - ends the shake effect.
 *   <li>{@code [word-space=1.5]} - multiplies the default word spacing by the given factor for all
 *       subsequent text. 1.0 is default spacing.
 *   <li>{@code [line-space=1.5]} - multiplies the default line height by the given factor for all
 *       subsequent lines. 1.0 is default spacing.
 *   <li>{@code [tr]} or {@code [tr speed=0.5]} - sets typewriter mode speed. The {@code speed}
 *       parameter is a multiplier of the default rate. A speed of 0 disables typewriter mode (text
 *       appears instantly).
 *   <li>{@code [pause=0.5]} - pauses the typewriter for the specified duration in seconds. Has no
 *       effect when typewriter mode is disabled.
 *   <li>{@code [align=left|center|right]} - sets the horizontal alignment of all subsequent lines
 *       until another {@code [align=...]} tag overrides it. Lines without any align tag use the
 *       alignment programmatically assigned via {@link #setAlignment(int)} (default: {@link
 *       Align#left}). Block images ({@code [img-block ...]}) remain centered regardless of this
 *       setting.
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

  /** Suppresses invalidation during typewriter text updates to prevent re-layout loops. */
  private boolean suppressInvalidation = false;

  private FontSpec fontSpec;
  private String text;
  private float computedPrefHeight;
  private float lastPrefHeightWidth;
  private boolean wrap = true;

  /**
   * Optional upper bound on the value returned by {@link #getPrefWidth()}. {@code 0} (the default)
   * disables the clamp. Used to let parent layouts (e.g. a {@code Window}/{@code Dialog} that packs
   * to its content's preferred size) shrink to fit short text while still capping the width for
   * long text so it wraps instead of stretching the parent across the whole stage.
   */
  private float maxPrefWidth = 0f;

  /**
   * Horizontal alignment of each text line. One of {@link Align#left} (default), {@link
   * Align#center}, or {@link Align#right}; only the horizontal bits are inspected. Block images
   * ({@code [img-block ...]}) are always centered horizontally regardless of this setting.
   */
  private int alignment = Align.left;

  /**
   * Whether an implicit leading {@code [tr]} (typewriter at default speed) is prepended when
   * parsing the text. The input text can always override this with an explicit {@code [tr]} tag.
   */
  private final boolean typewriterEnabledByDefault;

  private final RichLabelParser parser = new RichLabelParser();
  private final RichLabelLayout layoutEngine = new RichLabelLayout();
  private List<Run> runs;

  /** Accumulated time for shake effects, persists across layout calls. */
  private float shakeElapsed;

  /** Frame counter for choppy shake updates. */
  private int shakeFrameCounter;

  /** Only update shake positions every N frames for a more intense, choppy feel. */
  private static final int SHAKE_UPDATE_INTERVAL = 3;

  /** Actors that should be shaken each frame, rebuilt during layout. */
  private final List<RichLabelLayout.ShakeTarget> shakeTargets = new ArrayList<>();

  // -- Typewriter state --

  /** Ordered reveal schedule built during layout. */
  private final List<RevealEntry> revealSchedule = new ArrayList<>();

  /** Current index into {@link #revealSchedule}. */
  private int revealIndex;

  /** For the current TextReveal entry, how many characters have been revealed so far. */
  private int revealCharOffset;

  /** Fractional character accumulator for sub-character timing precision. */
  private float revealCharAccum;

  /** Current typewriter speed in characters per second. 0 means disabled (instant). */
  private float typewriterSpeed;

  /** Remaining pause time in seconds before the typewriter resumes. */
  private float typewriterPauseRemaining;

  /** Whether the typewriter has finished revealing all content. */
  private boolean typewriterFinished = true;

  /**
   * If true, the typewriter advancement is paused (no characters are revealed and pause timers do
   * not tick down). Used to gate the start of a typewriter reveal until an external chain (e.g. a
   * previous label finishing) completes.
   */
  private boolean typewriterPaused;

  /** Optional callback invoked once when the typewriter finishes revealing all content. */
  private Runnable onTypewriterFinished;

  @Override
  protected void childrenChanged() {
    if (!layoutInProgress) {
      super.childrenChanged();
    }
  }

  /**
   * Creates a new RichLabel with the given text.
   *
   * @param text the markup text to display
   */
  public RichLabel(String text) {
    this(text, DialogDesign.DIALOG_FONT_SPEC_NORMAL, false);
  }

  /**
   * Creates a new RichLabel with the given text and font specification. The typewriter is disabled
   * by default.
   *
   * @param text the markup text to display
   * @param fontSpec the font specification for default text rendering
   */
  public RichLabel(String text, FontSpec fontSpec) {
    this(text, fontSpec, false);
  }

  /**
   * Creates a new RichLabel with the given text, font specification, and a flag controlling whether
   * the typewriter is enabled by default.
   *
   * <p>When {@code typewriterEnabledByDefault} is {@code false}, the parser will not prepend an
   * implicit leading {@code [tr]} tag, so text appears instantly unless the input text itself
   * contains an explicit {@code [tr]} tag (which always overrides this default).
   *
   * @param text the markup text to display
   * @param fontSpec the font specification for default text rendering
   * @param typewriterEnabledByDefault whether the typewriter is implicitly enabled at default speed
   *     for this label
   */
  public RichLabel(String text, FontSpec fontSpec, boolean typewriterEnabledByDefault) {
    this.fontSpec = fontSpec;
    this.text = text;
    this.typewriterEnabledByDefault = typewriterEnabledByDefault;
    runs = parser.parse(text, typewriterEnabledByDefault);
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
   * Creates a new RichLabel with the given text, font size, color, and a flag controlling whether
   * the typewriter is enabled by default. See {@link #RichLabel(String, FontSpec, boolean)}.
   *
   * @param text the markup text to display
   * @param fontSize the font size
   * @param fontColor the default font color
   * @param typewriterEnabledByDefault whether the typewriter is implicitly enabled at default speed
   *     for this label
   */
  public RichLabel(String text, int fontSize, Color fontColor, boolean typewriterEnabledByDefault) {
    this(text, FontSpec.of(fontSize, fontColor), typewriterEnabledByDefault);
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
   * Creates a new RichLabel with the given text and font size, using white as the default color,
   * and a flag controlling whether the typewriter is enabled by default. See {@link
   * #RichLabel(String, FontSpec, boolean)}.
   *
   * @param text the markup text to display
   * @param fontSize the font size
   * @param typewriterEnabledByDefault whether the typewriter is implicitly enabled at default speed
   *     for this label
   */
  public RichLabel(String text, int fontSize, boolean typewriterEnabledByDefault) {
    this(text, fontSize, Color.WHITE, typewriterEnabledByDefault);
  }

  /**
   * Converts a plain text string to RichLabel-compatible markup by replacing newline characters
   * ({@code \n}) with the {@code [n]} line-break tag understood by {@link RichLabel}.
   *
   * <p>Useful for adapting legacy text intended for plain {@link Label} usage so it renders with
   * the same line breaks inside a {@link RichLabel}.
   *
   * @param text input text, may contain {@code \n}; may be {@code null}
   * @return text with {@code \n} replaced by {@code [n]}, or {@code null} if {@code text} is null
   */
  public static String toRichText(String text) {
    return text == null ? null : text.replace("\n", "[n]");
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
    runs = parser.parse(text, typewriterEnabledByDefault);
    resetTypewriter();
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
    float pref = layoutEngine.computePrefWidth(runs, fontSpec);
    if (maxPrefWidth > 0f && pref > maxPrefWidth) return maxPrefWidth;
    return pref;
  }

  /**
   * Sets an upper bound on the value returned by {@link #getPrefWidth()}.
   *
   * <p>This lets the label report a smaller preferred width to its parent layout when its natural
   * (unwrapped) width would exceed the given limit, without forcing a fixed width. The label still
   * shrinks below the limit for short text. When the parent assigns the clamped width back to the
   * label, normal {@link #setWrap(boolean) wrap} behaviour kicks in and the text wraps to multiple
   * lines.
   *
   * @param maxPrefWidth the maximum preferred width in pixels, or {@code 0} to disable the clamp
   */
  public void setMaxPrefWidth(float maxPrefWidth) {
    if (this.maxPrefWidth == maxPrefWidth) return;
    this.maxPrefWidth = maxPrefWidth;
    invalidateHierarchy();
  }

  /**
   * Returns the current upper bound on {@link #getPrefWidth()}, or {@code 0} if no clamp is set.
   *
   * @return the maximum preferred width in pixels, or {@code 0}
   */
  public float getMaxPrefWidth() {
    return maxPrefWidth;
  }

  /**
   * Sets the horizontal alignment of each text line, mirroring {@link
   * com.badlogic.gdx.scenes.scene2d.ui.Label#setAlignment(int)}.
   *
   * <p>Only the horizontal bits ({@link Align#left}, {@link Align#center}, {@link Align#right}) of
   * the value are inspected; vertical bits are ignored. Lines shorter than the available width are
   * shifted accordingly. Block images ({@code [img-block ...]}) remain centered regardless of the
   * alignment.
   *
   * @param alignment the alignment, e.g. {@link Align#left}, {@link Align#center}, {@link
   *     Align#right}
   */
  public void setAlignment(int alignment) {
    if (this.alignment == alignment) return;
    this.alignment = alignment;
    invalidateHierarchy();
  }

  /**
   * Returns the current horizontal alignment.
   *
   * @return the alignment value
   */
  public int getAlignment() {
    return alignment;
  }

  @Override
  public void invalidate() {
    if (suppressInvalidation) return;
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
    suppressInvalidation = true;
    clearChildren();
    clearActions();
    shakeTargets.clear();
    float availableWidth = getWidth();
    if (availableWidth <= 0) {
      availableWidth = getPrefWidth();
    }
    var result = layoutEngine.layoutRuns(this, runs, fontSpec, availableWidth, wrap, alignment);
    computedPrefHeight = result.prefHeight();
    shakeTargets.addAll(result.shakeTargets());
    applyShakeOffsets();
    buildRevealSchedule(result.placedActors());
    applyTypewriterState();
    suppressInvalidation = false;
    layoutInProgress = false;
  }

  @Override
  public void act(float delta) {
    super.act(delta);

    // Advance typewriter
    if (!typewriterFinished) {
      advanceTypewriter(delta);
    }

    // Shake effect
    if (!shakeTargets.isEmpty()) {
      shakeElapsed += delta;
      shakeFrameCounter++;
      if (shakeFrameCounter % SHAKE_UPDATE_INTERVAL == 0) {
        applyShakeOffsets();
      }
    }
  }

  /**
   * Returns whether the typewriter has finished revealing all content (or was never active).
   *
   * @return true if all content is visible
   */
  public boolean isTypewriterFinished() {
    return typewriterFinished;
  }

  /**
   * Sets a callback that is invoked once when the typewriter finishes revealing all content. The
   * callback is also invoked immediately by {@link #skipTypewriter()}. It is not invoked if
   * typewriter mode is not active. The callback is cleared after invocation.
   *
   * @param callback the callback to invoke on completion, or null to clear
   */
  public void onTypewriterFinished(Runnable callback) {
    this.onTypewriterFinished = callback;
  }

  /**
   * Pauses typewriter advancement. While paused, no further characters are revealed and pause
   * timers ({@code [pause=...]} tags) do not tick down. Use this to defer the start of the reveal
   * until an external trigger (e.g. another RichLabel finishing) occurs.
   *
   * <p>This is independent of {@code [pause=...]} tags inside the text: those represent in-stream
   * pauses while the reveal is otherwise running.
   */
  public void pauseTypewriter() {
    this.typewriterPaused = true;
  }

  /** Resumes typewriter advancement after {@link #pauseTypewriter()}. */
  public void resumeTypewriter() {
    this.typewriterPaused = false;
  }

  /**
   * Returns whether the typewriter is currently externally paused via {@link #pauseTypewriter()}.
   *
   * @return true if paused
   */
  public boolean isTypewriterPaused() {
    return typewriterPaused;
  }

  /** Instantly reveals all remaining typewriter content. */
  public void skipTypewriter() {
    if (typewriterFinished) return;
    typewriterFinished = true;
    typewriterPauseRemaining = 0;
    suppressInvalidation = true;
    // Reveal everything
    for (RevealEntry entry : revealSchedule) {
      if (entry instanceof TextReveal(Label label, String fullText)) {
        label.setText(fullText);
        label.setVisible(true);
      } else if (entry instanceof ActorReveal(Actor actor)) {
        actor.setVisible(true);
      }
    }
    suppressInvalidation = false;
    revealIndex = revealSchedule.size();
    revealCharOffset = 0;
    revealCharAccum = 0;
    fireTypewriterFinished();
  }

  /**
   * Resets the typewriter state so that the next layout pass will start the reveal from the
   * beginning. Typewriter speed starts from the implicit default on first reveal processing.
   */
  public void resetTypewriter() {
    revealIndex = 0;
    revealCharOffset = 0;
    revealCharAccum = 0;
    typewriterSpeed = 0;
    typewriterPauseRemaining = 0;
    typewriterFinished = true;
    revealSchedule.clear();
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

  // -- Typewriter internals --

  /** A single entry in the typewriter reveal schedule. */
  private sealed interface RevealEntry {}

  /**
   * A text label to reveal character by character.
   *
   * @param label the Scene2d label actor
   * @param fullText the complete text content
   */
  private record TextReveal(Label label, String fullText) implements RevealEntry {}

  /**
   * A non-text actor (image) to reveal as a whole.
   *
   * @param actor the Scene2d actor
   */
  private record ActorReveal(Actor actor) implements RevealEntry {}

  /**
   * A change in typewriter speed.
   *
   * @param speed characters per second, or 0 to disable
   */
  private record SpeedChange(float speed) implements RevealEntry {}

  /**
   * A pause in the typewriter reveal.
   *
   * @param duration pause duration in seconds
   */
  private record PauseEntry(float duration) implements RevealEntry {}

  /**
   * Builds the reveal schedule from the layout result's placed actors. Also determines whether
   * typewriter mode is active and sets initial visibility. Preserves typewriter progress across
   * re-layout calls so the reveal is not reset when the widget is re-laid-out.
   *
   * @param placedActors the ordered list of placed actors from the layout pass
   */
  private void buildRevealSchedule(List<RichLabelLayout.PlacedActor> placedActors) {
    // Save progress before rebuilding
    int savedRevealIndex = revealIndex;
    int savedCharOffset = revealCharOffset;
    float savedCharAccum = revealCharAccum;
    float savedSpeed = typewriterSpeed;
    float savedPause = typewriterPauseRemaining;
    boolean savedFinished = typewriterFinished;
    boolean hadSchedule = !revealSchedule.isEmpty();

    revealSchedule.clear();

    boolean hasTypewriter = false;
    for (RichLabelLayout.PlacedActor pa : placedActors) {
      if (pa.run() instanceof TypewriterRun(float speed)) {
        revealSchedule.add(new SpeedChange(speed));
        if (speed > 0) hasTypewriter = true;
      } else if (pa.run() instanceof PauseRun(float duration)) {
        revealSchedule.add(new PauseEntry(duration));
      } else if (pa.actor() != null) {
        if (pa.fullText() != null) {
          revealSchedule.add(new TextReveal((Label) pa.actor(), pa.fullText()));
        } else {
          revealSchedule.add(new ActorReveal(pa.actor()));
        }
      }
    }

    if (!hasTypewriter) {
      typewriterFinished = true;
      revealIndex = 0;
      revealCharOffset = 0;
      revealCharAccum = 0;
      typewriterSpeed = 0;
      typewriterPauseRemaining = 0;
    } else if (hadSchedule) {
      // Re-layout: restore progress from before
      typewriterFinished = savedFinished;
      revealIndex = Math.min(savedRevealIndex, revealSchedule.size());
      revealCharOffset = savedCharOffset;
      revealCharAccum = savedCharAccum;
      typewriterSpeed = savedSpeed;
      typewriterPauseRemaining = savedPause;
    } else {
      // First layout with typewriter tags: start fresh
      typewriterFinished = false;
      revealIndex = 0;
      revealCharOffset = 0;
      revealCharAccum = 0;
      typewriterSpeed = 0;
      typewriterPauseRemaining = 0;
    }
  }

  /** Applies the current typewriter visibility state to all actors in the reveal schedule. */
  private void applyTypewriterState() {
    if (typewriterFinished) return;

    // Process all entries up to revealIndex to apply speed changes, then hide the rest
    float speed = 0;
    for (int i = 0; i < revealSchedule.size(); i++) {
      RevealEntry entry = revealSchedule.get(i);
      if (i < revealIndex) {
        // Already fully revealed
        if (entry instanceof SpeedChange(float speed1)) speed = speed1;
        continue;
      }
      if (i == revealIndex) {
        // Currently being revealed
        if (entry instanceof TextReveal(Label label, String fullText)) {
          if (speed > 0) {
            if (revealCharOffset <= 0) {
              label.setText("");
              label.setVisible(true);
            } else {
              label.setText(fullText.substring(0, revealCharOffset));
              label.setVisible(true);
            }
          }
          // If speed is 0, label stays fully visible (instant)
        } else if (entry instanceof SpeedChange(float speed1)) {
          speed = speed1;
        }
        continue;
      }
      if (entry instanceof SpeedChange(float speed1)) {
        speed = speed1;
        continue;
      }
      if (speed > 0) {
        if (entry instanceof TextReveal tr) {
          tr.label.setText("");
          tr.label.setVisible(true);
        } else if (entry instanceof ActorReveal(Actor actor)) {
          actor.setVisible(false);
        }
      }
    }
  }

  /**
   * Advances the typewriter by the given delta time.
   *
   * @param delta time elapsed since the last frame in seconds
   */
  private void advanceTypewriter(float delta) {
    if (typewriterFinished) return;
    if (typewriterPaused) return;

    suppressInvalidation = true;

    // Handle pause
    if (typewriterPauseRemaining > 0) {
      typewriterPauseRemaining -= delta;
      if (typewriterPauseRemaining > 0) {
        suppressInvalidation = false;
        return;
      }
      delta = -typewriterPauseRemaining;
      typewriterPauseRemaining = 0;
    }

    revealCharAccum += delta;

    while (revealIndex < revealSchedule.size()) {
      RevealEntry entry = revealSchedule.get(revealIndex);

      if (entry instanceof SpeedChange(float speed)) {
        typewriterSpeed = speed;
        revealIndex++;
        revealCharOffset = 0;

        // If speed changed to 0, instantly reveal everything until next SpeedChange
        if (typewriterSpeed <= 0) {
          revealInstantUntilNextSpeedChange();
        }
        continue;
      }

      if (entry instanceof PauseEntry(float duration)) {
        if (typewriterSpeed > 0) {
          typewriterPauseRemaining = duration;
          revealIndex++;
          revealCharOffset = 0;
          if (typewriterPauseRemaining > 0) {
            suppressInvalidation = false;
            return;
          }
        } else {
          revealIndex++;
          revealCharOffset = 0;
        }
        continue;
      }

      if (typewriterSpeed <= 0) {
        // Instant mode: should have been handled by revealInstantUntilNextSpeedChange
        revealIndex++;
        revealCharOffset = 0;
        continue;
      }

      // Typewriter active: consume characters
      float charTime = 1f / typewriterSpeed;

      if (entry instanceof TextReveal(Label label, String fullText)) {
        int totalChars = fullText.length();
        while (revealCharOffset < totalChars && revealCharAccum >= charTime) {
          revealCharAccum -= charTime;
          revealCharOffset++;
        }
        label.setText(fullText.substring(0, revealCharOffset));
        label.setVisible(true);
        if (revealCharOffset >= totalChars) {
          revealIndex++;
          revealCharOffset = 0;
          continue;
        }
        suppressInvalidation = false;
        return; // Waiting for more time
      }

      if (entry instanceof ActorReveal(Actor actor)) {
        if (revealCharAccum >= charTime) {
          revealCharAccum -= charTime;
          actor.setVisible(true);
          revealIndex++;
          revealCharOffset = 0;
          continue;
        }
        suppressInvalidation = false;
        return; // Waiting for more time
      }

      revealIndex++;
      revealCharOffset = 0;
    }

    typewriterFinished = true;
    suppressInvalidation = false;
    fireTypewriterFinished();
  }

  /** Fires the onTypewriterFinished callback if set, then clears it. */
  private void fireTypewriterFinished() {
    Runnable cb = onTypewriterFinished;
    onTypewriterFinished = null;
    if (cb != null) cb.run();
  }

  /** Instantly reveals all entries from the current position until the next SpeedChange entry. */
  private void revealInstantUntilNextSpeedChange() {
    while (revealIndex < revealSchedule.size()) {
      RevealEntry entry = revealSchedule.get(revealIndex);
      if (entry instanceof SpeedChange) break;
      if (entry instanceof TextReveal(Label label, String fullText)) {
        label.setText(fullText);
        label.setVisible(true);
      } else if (entry instanceof ActorReveal(Actor actor)) {
        actor.setVisible(true);
      }
      // PauseEntry is skipped when speed is 0
      revealIndex++;
      revealCharOffset = 0;
    }
  }
}
