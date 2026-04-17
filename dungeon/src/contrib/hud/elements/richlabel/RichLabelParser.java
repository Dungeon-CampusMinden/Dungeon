package contrib.hud.elements.richlabel;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses rich-text markup into a flat list of {@link Run} tokens. Handles all supported tags:
 * {@code [img]}, {@code [img-block]}, {@code [color]}, {@code [size]}, {@code [shake]}, and their
 * closing variants.
 */
public class RichLabelParser {

  static final Pattern TAG_PATTERN =
      Pattern.compile(
          "\\[img=([^]]+)]"
              + "|\\[img-block((?:\\s+[^]]*))]"
              + "|\\[color=([^]]+)]|\\[/color]"
              + "|\\[size=([^]]+)]|\\[/size]"
              + "|\\[shake((?:\\s+[^]]*)?)]|\\[/shake]"
              + "|\\[word-space=([^]]+)]"
              + "|\\[line-space=([^]]+)]"
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

  /** Counter used to assign a unique phase to each [shake] block during parsing. */
  private int shakeBlockCounter;

  /** Cached ShakeEffect instances by block index, preserved across re-parses. */
  private final List<ShakeEffect> shakeEffectCache = new ArrayList<>();

  /**
   * Parses the markup text into a flat list of runs.
   *
   * @param text the markup text to parse
   * @return the list of parsed runs
   */
  public List<Run> parse(String text) {
    List<Run> runs = new ArrayList<>();
    shakeBlockCounter = 0;
    if (text == null || text.isEmpty()) return runs;

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
        runs.add(new ImageRun(matcher.group(1), currentShake));
      } else if (matcher.group(2) != null) {
        TagParams params = TagParams.parse(matcher.group(2));
        String path = params.getString("path", null);
        if (path != null) {
          String widthSpec = params.getString("width", null);
          runs.add(new ImageBlockRun(path, widthSpec));
        }
      } else if (matcher.group(3) != null) {
        currentColor = parseColor(matcher.group(3));
      } else if (matcher.group(4) != null) {
        currentSize = parseSizeValue(matcher.group(4));
      } else if (matcher.group(5) != null) {
        currentShake = parseShake(matcher.group(5));
      } else if (matcher.group(6) != null) {
        // [word-space=N]
        float val = parseFloatValue(matcher.group(6), 1f);
        runs.add(new SpacingRun(val, -1f));
      } else if (matcher.group(7) != null) {
        // [line-space=N]
        float val = parseFloatValue(matcher.group(7), 1f);
        runs.add(new SpacingRun(-1f, val));
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

    // Remove purely whitespace text runs immediately before an image run
    for (int i = runs.size() - 1; i >= 0; i--) {
      if (runs.get(i) instanceof TextRun tr
          && tr.word().isBlank()
          && i + 1 < runs.size()
          && (runs.get(i + 1) instanceof ImageRun || runs.get(i + 1) instanceof ImageBlockRun)) {
        runs.remove(i);
      }
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
