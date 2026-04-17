package contrib.hud.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
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
 *   <li>{@code [tr]} or {@code [tr speed=0.5]} - enables typewriter mode. The {@code speed}
 *       parameter is a multiplier of the default rate (10 characters per second), so {@code
 *       speed=0.5} yields 5 chars/s. A speed of 0 disables typewriter mode (text appears
 *       instantly). Typewriter mode is disabled by default until a {@code [tr]} tag is encountered.
 *   <li>{@code [pause=0.5]} - pauses the typewriter for the specified duration in seconds. Has no
 *       effect when typewriter mode is disabled.
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

  /** Optional callback invoked once when the typewriter finishes revealing all content. */
  private Runnable onTypewriterFinished;

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
    return layoutEngine.computePrefWidth(runs, fontSpec);
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
    var result = layoutEngine.layoutRuns(this, runs, fontSpec, availableWidth, wrap);
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

  /** Instantly reveals all remaining typewriter content. */
  public void skipTypewriter() {
    if (typewriterFinished) return;
    typewriterFinished = true;
    typewriterPauseRemaining = 0;
    suppressInvalidation = true;
    // Reveal everything
    for (RevealEntry entry : revealSchedule) {
      if (entry instanceof TextReveal tr) {
        tr.label.setText(tr.fullText);
        tr.label.setVisible(true);
      } else if (entry instanceof ActorReveal ar) {
        ar.actor.setVisible(true);
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
   * beginning. Typewriter speed resets to 0 (disabled) until a [tr] tag is encountered.
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
      if (pa.run() instanceof TypewriterRun tr) {
        revealSchedule.add(new SpeedChange(tr.speed()));
        if (tr.speed() > 0) hasTypewriter = true;
      } else if (pa.run() instanceof PauseRun pr) {
        revealSchedule.add(new PauseEntry(pr.duration()));
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
        if (entry instanceof SpeedChange sc) speed = sc.speed;
        continue;
      }
      if (i == revealIndex) {
        // Currently being revealed
        if (entry instanceof TextReveal tr) {
          if (speed > 0) {
            if (revealCharOffset <= 0) {
              tr.label.setText("");
              tr.label.setVisible(true);
            } else {
              tr.label.setText(tr.fullText.substring(0, revealCharOffset));
              tr.label.setVisible(true);
            }
          }
          // If speed is 0, label stays fully visible (instant)
        } else if (entry instanceof SpeedChange sc) {
          speed = sc.speed;
        }
        continue;
      }
      // Future entries: hide if typewriter is active at this point
      if (speed > 0) {
        if (entry instanceof TextReveal tr) {
          tr.label.setText("");
          tr.label.setVisible(true);
        } else if (entry instanceof ActorReveal ar) {
          ar.actor.setVisible(false);
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

      if (entry instanceof SpeedChange sc) {
        typewriterSpeed = sc.speed;
        revealIndex++;
        revealCharOffset = 0;

        // If speed changed to 0, instantly reveal everything until next SpeedChange
        if (typewriterSpeed <= 0) {
          revealInstantUntilNextSpeedChange();
        }
        continue;
      }

      if (entry instanceof PauseEntry pe) {
        if (typewriterSpeed > 0) {
          typewriterPauseRemaining = pe.duration;
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

      if (entry instanceof TextReveal tr) {
        int totalChars = tr.fullText.length();
        while (revealCharOffset < totalChars && revealCharAccum >= charTime) {
          revealCharAccum -= charTime;
          revealCharOffset++;
        }
        tr.label.setText(tr.fullText.substring(0, revealCharOffset));
        tr.label.setVisible(true);
        if (revealCharOffset >= totalChars) {
          revealIndex++;
          revealCharOffset = 0;
          continue;
        }
        suppressInvalidation = false;
        return; // Waiting for more time
      }

      if (entry instanceof ActorReveal ar) {
        if (revealCharAccum >= charTime) {
          revealCharAccum -= charTime;
          ar.actor.setVisible(true);
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
      if (entry instanceof TextReveal tr) {
        tr.label.setText(tr.fullText);
        tr.label.setVisible(true);
      } else if (entry instanceof ActorReveal ar) {
        ar.actor.setVisible(true);
      }
      // PauseEntry is skipped when speed is 0
      revealIndex++;
      revealCharOffset = 0;
    }
  }
}
