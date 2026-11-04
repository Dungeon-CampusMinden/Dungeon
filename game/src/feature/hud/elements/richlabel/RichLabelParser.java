package feature.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.CoreActions;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.input.KeyNames;
import engine.utils.logging.DungeonLogger;
import feature.input.ui.InputMethod;
import feature.input.ui.InputPromptHelper;
import feature.input.ui.InputPromptHelper.InputPromptRegion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses rich-text markup into a flat list of {@link Run} tokens. Handles all supported tags:
 * {@code [img]}, {@code [img-block]}, {@code [key]}, {@code [color]}, {@code [size]}, {@code
 * [shake]}, and their closing variants.
 */
public class RichLabelParser {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(RichLabelParser.class);

  static final Pattern TAG_PATTERN =
      Pattern.compile(
          "\\[img=([^]]+)]"
              + "|\\[img((?:\\s+[^]]*))]"
              + "|\\[img-block((?:\\s+[^]]*))]"
              + "|\\[action((?:\\s+[^]]*))]"
              + "|\\[color=([^]]+)]|\\[/color]"
              + "|\\[size=([^]]+)]|\\[/size]"
              + "|\\[shake((?:\\s+[^]]*)?)]|\\[/shake]"
              + "|\\[word-space=([^]]+)]"
              + "|\\[line-space=([^]]+)]"
              + "|\\[tr((?:\\s+[^]]*)?)]"
              + "|\\[pause=([^]]+)]"
              + "|\\[align=([^]]+)]"
              + "|\\[font=([^]]+)]|\\[/font]"
              + "|\\[n]");

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

  /** Default shake strength in pixels. */
  static final float SHAKE_DEFAULT_STRENGTH = 1.3f;

  /** Default shake speed (frequency multiplier). */
  static final float SHAKE_DEFAULT_SPEED = 10f;

  /** Default typewriter speed in characters per second. */
  static final float TYPEWRITER_DEFAULT_SPEED = 35f;

  /** Default size multiplier applied to all {@code [key]}-derived images. */
  static final float KEY_DEFAULT_SCALE = 1.5f;

  /** Counter used to assign a unique phase to each [shake] block during parsing. */
  private int shakeBlockCounter;

  /** Cached ShakeEffect instances by block index, preserved across re-parses. */
  private final List<ShakeEffect> shakeEffectCache = new ArrayList<>();

  /**
   * Parses the markup text into a flat list of runs, with the typewriter implicitly enabled at
   * default speed. Equivalent to {@code parse(text, true)}.
   *
   * @param text the markup text to parse
   * @return the list of parsed runs
   */
  public List<Run> parse(String text) {
    return parse(text, true);
  }

  /**
   * Parses the markup text into a flat list of runs.
   *
   * @param text the markup text to parse
   * @param implicitTypewriter if {@code true}, an implicit leading {@code [tr speed=1.0]} is
   *     prepended so the typewriter is enabled by default. If {@code false}, no implicit typewriter
   *     run is added; the typewriter is then only active if the input text contains an explicit
   *     {@code [tr]} tag.
   * @return the list of parsed runs
   */
  public List<Run> parse(String text, boolean implicitTypewriter) {
    List<Run> runs = new ArrayList<>();
    shakeBlockCounter = 0;
    if (text == null || text.isEmpty()) return runs;

    if (implicitTypewriter) {
      // Default behavior is equivalent to an implicit leading [tr speed=1.0].
      runs.add(new TypewriterRun(TYPEWRITER_DEFAULT_SPEED));
    }

    Color currentColor = null;
    int currentSize = -1;
    ShakeEffect currentShake = null;
    String currentFontPath = null;
    Matcher matcher = TAG_PATTERN.matcher(text);
    int lastEnd = 0;

    while (matcher.find()) {
      if (matcher.start() > lastEnd) {
        String segment = text.substring(lastEnd, matcher.start());
        addTextRuns(runs, segment, currentColor, currentSize, currentShake, currentFontPath);
      }

      if (matcher.group(1) != null) {
        runs.add(ImageRun.fullTexture(matcher.group(1), currentShake, currentSize));
      } else if (matcher.group(2) != null) {
        TagParams params = TagParams.parse(matcher.group(2));
        String path = params.getString("path", null);
        if (path != null) {
          int rx = (int) params.getFloat("x", -1f);
          int ry = (int) params.getFloat("y", -1f);
          int rw = (int) params.getFloat("w", -1f);
          int rh = (int) params.getFloat("h", -1f);
          float scale = params.getFloat("scale", 1f);
          boolean noGapLeft = params.getBoolean("noGapLeft");
          boolean noGapRight = params.getBoolean("noGapRight");
          runs.add(
              new ImageRun(
                  path,
                  currentShake,
                  currentSize,
                  scale,
                  rx,
                  ry,
                  rw,
                  rh,
                  noGapLeft,
                  noGapRight,
                  Color.WHITE));
        }
      } else if (matcher.group(3) != null) {
        TagParams params = TagParams.parse(matcher.group(3));
        String path = params.getString("path", null);
        if (path != null) {
          String widthSpec = params.getString("width", null);
          runs.add(new ImageBlockRun(path, widthSpec));
        }
      } else if (matcher.group(4) != null) {
        parseActionTag(matcher.group(4), currentColor, currentShake, currentSize, runs);
      } else if (matcher.group(5) != null) {
        currentColor = parseColor(matcher.group(5));
      } else if (matcher.group(6) != null) {
        currentSize = parseSizeValue(matcher.group(6));
      } else if (matcher.group(7) != null) {
        currentShake = parseShake(matcher.group(7));
      } else if (matcher.group(8) != null) {
        // [word-space=N]
        float val = parseFloatValue(matcher.group(8), 1f);
        runs.add(new SpacingRun(val, -1f));
      } else if (matcher.group(9) != null) {
        // [line-space=N]
        float val = parseFloatValue(matcher.group(9), 1f);
        runs.add(new SpacingRun(-1f, val));
      } else if (matcher.group(10) != null) {
        // [tr] or [tr speed=N]
        TagParams tp = TagParams.parse(matcher.group(10));
        float speedMul = tp.getFloat("speed", 1f);
        runs.add(new TypewriterRun(TYPEWRITER_DEFAULT_SPEED * speedMul));
      } else if (matcher.group(11) != null) {
        // [pause=N]
        float duration = parseFloatValue(matcher.group(11), 0f);
        runs.add(new PauseRun(duration));
      } else if (matcher.group(12) != null) {
        // [align=left|center|right]
        runs.add(new AlignRun(parseAlignValue(matcher.group(12))));
      } else if (matcher.group(13) != null) {
        // [font=path/to/font] (defaults to .ttf when no extension is provided)
        String path = matcher.group(13).trim();
        currentFontPath = path.isEmpty() ? null : normalizeFontPath(path);
      } else {
        String matched = matcher.group();
        if (matched.equals("[/color]")) {
          currentColor = null;
        } else if (matched.equals("[/size]")) {
          currentSize = -1;
        } else if (matched.equals("[/shake]")) {
          currentShake = null;
        } else if (matched.equals("[/font]")) {
          currentFontPath = null;
        } else if (matched.equals("[n]")) {
          runs.add(new LineBreakRun());
        }
      }
      lastEnd = matcher.end();
    }

    if (lastEnd < text.length()) {
      addTextRuns(
          runs, text.substring(lastEnd), currentColor, currentSize, currentShake, currentFontPath);
    }

    return runs;
  }

  /** Clears the shake effect cache (call when the text itself changes). */
  public void clearShakeCache() {
    shakeEffectCache.clear();
  }

  /**
   * Strips all markup tags from the text, returning plain text only.
   *
   * @param input the markup text to strip
   * @return the plain text with all tags removed
   */
  public static String stripTags(String input) {
    return TAG_PATTERN.matcher(input).replaceAll("");
  }

  // -- Private helpers --

  private static void addTextRuns(
      List<Run> runs,
      String segment,
      Color color,
      int sizeOverride,
      ShakeEffect shake,
      String fontPath) {
    String[] parts = segment.split("(?<=\\s)|(?=\\s)");
    StringBuilder current = new StringBuilder();
    for (String part : parts) {
      if (part.isBlank() && !current.isEmpty()) {
        runs.add(new TextRun(current.toString(), color, sizeOverride, shake, fontPath));
        current.setLength(0);
        current.append(part);
      } else if (part.isBlank()) {
        current.append(part);
      } else {
        current.append(part);
        runs.add(new TextRun(current.toString(), color, sizeOverride, shake, fontPath));
        current.setLength(0);
      }
    }
    if (!current.isEmpty()) {
      runs.add(new TextRun(current.toString(), color, sizeOverride, shake, fontPath));
    }
  }

  private static int parseSizeValue(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private static float parseFloatValue(String value, float defaultValue) {
    try {
      return Float.parseFloat(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Parses an alignment keyword ({@code left}, {@code center}, {@code right}) into the
   * corresponding libGDX {@link Align} bits. Unknown / blank values reset to {@code -1}, which the
   * layout engine interprets as "use the programmatic default".
   *
   * @param value the raw alignment keyword
   * @return the matching {@link Align} constant or {@code -1} for "reset to default"
   */
  private static int parseAlignValue(String value) {
    if (value == null) return -1;
    switch (value.trim().toLowerCase()) {
      case "left":
        return Align.left;
      case "center":
      case "centre":
        return Align.center;
      case "right":
        return Align.right;
      default:
        return -1;
    }
  }

  /**
   * Resolves an {@code [action]} tag to the current binding of the action on the device the player
   * used last. The required {@code id} parameter names the action, the optional {@code all} flag
   * shows every binding of that device instead of the first. {@code core:move} shows the stick on
   * the pad and the four movement keys otherwise. Keys, mouse buttons and pad inputs show the
   * outlined variant of their prompt graphic in the color of the text, so prompts look the same on
   * light and dark backgrounds; a combo shows one graphic per key. Inputs without a graphic and
   * unbound actions show their label.
   *
   * @param params the raw parameter string of the tag (everything after {@code [action} and before
   *     the closing {@code ]}).
   * @param color the current text color, also used for the prompt graphics.
   * @param shake the current shake effect to attach to the resulting runs, or {@code null}.
   * @param sizeOverride the current font size override, or {@code -1}.
   * @param runs receives the resolved runs.
   */
  private static void parseActionTag(
      String params, Color color, ShakeEffect shake, int sizeOverride, List<Run> runs) {
    TagParams tp = TagParams.parse(params);
    String id = tp.getString("id", null);
    if (id == null) {
      LOGGER.warn("[action] tag is missing required 'id' parameter; ignoring");
      return;
    }
    ActionId action;
    try {
      action = ActionId.of(id);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("[action] tag has invalid id '" + id + "'; ignoring");
      return;
    }
    if (action.equals(CoreActions.MOVE) && GameInput.device() != InputDevice.PAD) {
      for (ActionId key : CoreActions.MOVE_KEYS) {
        GameInput.displayBindings(key).stream()
            .findFirst()
            .ifPresent(binding -> addBinding(binding, color, shake, sizeOverride, runs));
      }
      return;
    }
    List<Binding> bindings = GameInput.displayBindings(action);
    if (bindings.isEmpty()) {
      runs.add(new TextRun("-", color, sizeOverride, shake));
      return;
    }
    int shown = tp.getBoolean("all") ? bindings.size() : 1;
    for (int i = 0; i < shown; i++) {
      if (i > 0) runs.add(new TextRun("/ ", color, sizeOverride, shake));
      addBinding(bindings.get(i), color, shake, sizeOverride, runs);
    }
  }

  private static void addBinding(
      Binding binding, Color color, ShakeEffect shake, int sizeOverride, List<Run> runs) {
    List<InputPromptRegion> regions = regions(binding);
    if (regions.isEmpty() || regions.contains(null)) {
      runs.add(new TextRun(KeyNames.label(binding), color, sizeOverride, shake));
      return;
    }
    for (InputPromptRegion region : regions) runs.add(imageRun(region, color, shake, sizeOverride));
  }

  private static List<InputPromptRegion> regions(Binding binding) {
    if (binding instanceof Binding.MouseButton mouse) {
      return Collections.singletonList(InputPromptHelper.lookupMouseButton(mouse.button()));
    }
    if (binding.isPad()) {
      InputMethod method = InputMethod.ofController(GameInput.padName());
      return Collections.singletonList(InputPromptHelper.lookupPad(method, binding));
    }
    if (!(binding instanceof Binding.Key key)) return List.of();
    List<InputPromptRegion> regions = new ArrayList<>();
    key.prefixes().stream().sorted().forEach(prefix -> regions.add(keyRegion(prefix)));
    regions.add(keyRegion(key.keycode()));
    return regions;
  }

  private static InputPromptRegion keyRegion(int keycode) {
    return InputPromptHelper.lookupKey(KeyNames.glyphKeycode(keycode));
  }

  private static ImageRun imageRun(
      InputPromptRegion region, Color color, ShakeEffect shake, int sizeOverride) {
    return new ImageRun(
        region.texturePath(),
        shake,
        sizeOverride,
        KEY_DEFAULT_SCALE,
        region.x(),
        region.y(),
        region.width(),
        region.height(),
        false,
        false,
        color);
  }

  private ShakeEffect parseShake(String params) {
    TagParams tp = TagParams.parse(params);
    float strengthMul = tp.getFloat("strength", 1f);
    float speedMul = tp.getFloat("speed", 1f);
    int blockIdx = shakeBlockCounter++;
    if (blockIdx < shakeEffectCache.size()) {
      return shakeEffectCache.get(blockIdx);
    }
    float phase = (blockIdx * 97.31f) % 100f;
    ShakeEffect effect =
        new ShakeEffect(
            SHAKE_DEFAULT_STRENGTH * strengthMul, SHAKE_DEFAULT_SPEED * speedMul, phase);
    shakeEffectCache.add(effect);
    return effect;
  }

  static Color parseColor(String value) {
    String lower = value.toLowerCase().trim();
    Color named = NAMED_COLORS.get(lower);
    if (named != null) return named;

    try {
      String hex = lower.startsWith("#") ? lower.substring(1) : lower;
      if (hex.length() == 6) hex += "ff";
      return Color.valueOf(hex);
    } catch (Exception e) {
      return Color.WHITE;
    }
  }

  /**
   * Normalizes a font path from a {@code [font=...]} tag.
   *
   * <p>If no file extension is present in the last path segment, {@code .ttf} is appended. Existing
   * extensions are preserved.
   *
   * @param rawPath the raw font path value from markup
   * @return the normalized font path
   */
  private static String normalizeFontPath(String rawPath) {
    int lastSlash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
    int lastDot = rawPath.lastIndexOf('.');
    boolean hasExtension = lastDot > lastSlash && lastDot < rawPath.length() - 1;
    return hasExtension ? rawPath : rawPath + ".ttf";
  }
}
