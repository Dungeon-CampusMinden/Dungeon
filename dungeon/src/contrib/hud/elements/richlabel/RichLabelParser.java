package contrib.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;
import contrib.hud.input.InputMethod;
import contrib.hud.input.InputPromptHelper;
import contrib.hud.input.InputPromptHelper.InputPromptRegion;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
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
              + "|\\[key((?:\\s+[^]]*))]"
              + "|\\[color=([^]]+)]|\\[/color]"
              + "|\\[size=([^]]+)]|\\[/size]"
              + "|\\[shake((?:\\s+[^]]*)?)]|\\[/shake]"
              + "|\\[word-space=([^]]+)]"
              + "|\\[line-space=([^]]+)]"
              + "|\\[tr((?:\\s+[^]]*)?)]"
              + "|\\[pause=([^]]+)]"
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
    Matcher matcher = TAG_PATTERN.matcher(text);
    int lastEnd = 0;

    while (matcher.find()) {
      if (matcher.start() > lastEnd) {
        String segment = text.substring(lastEnd, matcher.start());
        addTextRuns(runs, segment, currentColor, currentSize, currentShake);
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
                  path, currentShake, currentSize, scale, rx, ry, rw, rh, noGapLeft, noGapRight));
        }
      } else if (matcher.group(3) != null) {
        TagParams params = TagParams.parse(matcher.group(3));
        String path = params.getString("path", null);
        if (path != null) {
          String widthSpec = params.getString("width", null);
          runs.add(new ImageBlockRun(path, widthSpec));
        }
      } else if (matcher.group(4) != null) {
        ImageRun keyImage = parseKeyTag(matcher.group(4), currentShake, currentSize);
        if (keyImage != null) runs.add(keyImage);
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
      } else {
        String matched = matcher.group();
        if (matched.equals("[/color]")) {
          currentColor = null;
        } else if (matched.equals("[/size]")) {
          currentSize = -1;
        } else if (matched.equals("[/shake]")) {
          currentShake = null;
        } else if (matched.equals("[n]")) {
          runs.add(new LineBreakRun());
        }
      }
      lastEnd = matcher.end();
    }

    if (lastEnd < text.length()) {
      addTextRuns(runs, text.substring(lastEnd), currentColor, currentSize, currentShake);
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
      List<Run> runs, String segment, Color color, int sizeOverride, ShakeEffect shake) {
    String[] parts = segment.split("(?<=\\s)|(?=\\s)");
    StringBuilder current = new StringBuilder();
    for (String part : parts) {
      if (part.isBlank() && !current.isEmpty()) {
        runs.add(new TextRun(current.toString(), color, sizeOverride, shake));
        current.setLength(0);
        current.append(part);
      } else if (part.isBlank()) {
        current.append(part);
      } else {
        current.append(part);
        runs.add(new TextRun(current.toString(), color, sizeOverride, shake));
        current.setLength(0);
      }
    }
    if (!current.isEmpty()) {
      runs.add(new TextRun(current.toString(), color, sizeOverride, shake));
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
   * Parses a {@code [key]} tag's parameter string and resolves it to an {@link ImageRun} that
   * references the matching cell of an input-prompt spritesheet. The {@code code} parameter is
   * required and holds the libGDX key or button integer; the optional {@code type} parameter
   * selects the lookup space ({@code keyboard} (default), {@code mouse}, {@code playstation},
   * {@code xbox}, {@code touch}). The optional {@code outline} boolean flag selects the outlined
   * spritesheet variant. Returns {@code null} (and logs a warning) when the tag cannot be resolved,
   * so the markup is silently dropped at render time instead of throwing.
   *
   * @param params the raw parameter string of the tag (everything after {@code [key} and before the
   *     closing {@code ]}).
   * @param shake the current shake effect to attach to the resulting image run, or {@code null}.
   * @param sizeOverride the current font size override to scale the image with, or {@code -1}.
   * @return the resolved {@link ImageRun}, or {@code null} when the tag cannot be resolved.
   */
  private static ImageRun parseKeyTag(String params, ShakeEffect shake, int sizeOverride) {
    TagParams tp = TagParams.parse(params);
    String codeStr = tp.getString("code", null);
    if (codeStr == null) {
      LOGGER.warn("[key] tag is missing required 'code' parameter; ignoring");
      return null;
    }
    int code;
    try {
      code = Integer.parseInt(codeStr);
    } catch (NumberFormatException e) {
      LOGGER.warn("[key] tag has non-integer code '" + codeStr + "'; ignoring");
      return null;
    }
    String type = tp.getString("type", "keyboard").toLowerCase();
    boolean outline = tp.getBoolean("outline");

    InputPromptRegion region;
    try {
      if ("mouse".equals(type)) {
        region = InputPromptHelper.lookupMouseButton(code, outline);
      } else {
        InputMethod method;
        try {
          method = InputMethod.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
          LOGGER.warn("[key] tag has unknown type '" + type + "'; ignoring");
          return null;
        }
        region = InputPromptHelper.lookupKey(method, code, outline);
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.warn("[key] tag uses unsupported input method '" + type + "'; ignoring");
      return null;
    }

    if (region == null) {
      LOGGER.warn(
          "[key] tag has no prompt mapping for code=" + code + " type=" + type + "; ignoring");
      return null;
    }
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
        false);
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
}
