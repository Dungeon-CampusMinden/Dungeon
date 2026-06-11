package contrib.hud.dialogs;

import contrib.hud.elements.richlabel.TagParams;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Parser for sequenced speaker dialog scripts shared by {@link DialogDialog} and {@link
 * MultipleChoiceDialog} (via {@link DialogScriptView}).
 *
 * <p>Scripts use two custom tags:
 *
 * <ul>
 *   <li>{@code [p]} — page break: splits the script into individual {@link DialogEntry} pages.
 *   <li>{@code [speaker img=<path> name="<displayName>"]} — optional leading tag on a page that
 *       sets the speaker portrait/name for that page.
 * </ul>
 *
 * <p>Speaker resolution rules:
 *
 * <ul>
 *   <li>If a page omits the {@code [speaker]} tag entirely, it inherits the previous page's
 *       speaker. The very first page without a tag has no speaker (its portrait/name column is
 *       omitted).
 *   <li>If a page contains a {@code [speaker]} tag, that tag starts a fresh speaker context for the
 *       page: omitted {@code img}/{@code name} fields fall back to the placeholder defaults ({@link
 *       #DEFAULT_SPEAKER_IMAGE} / {@link #DEFAULT_SPEAKER_NAME}).
 *   <li>{@code [speaker clear]} resets the speaker to "no speaker" for this and subsequent pages
 *       (until another {@code [speaker]} tag is seen). When {@code clear} is set, {@code img} and
 *       {@code name} are ignored.
 * </ul>
 */
final class DialogScript {

  /** Default speaker portrait used when {@code [speaker]} is present without an {@code img}. */
  static final String DEFAULT_SPEAKER_IMAGE = "other/unknown.png";

  /** Default speaker name used when {@code [speaker]} is present without a {@code name}. */
  static final String DEFAULT_SPEAKER_NAME = "";

  /** Tag used inside the dialog script to split into multiple pages. */
  private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile("\\s*\\[p]\\s*");

  private DialogScript() {}

  /**
   * Parses the script string into dialog pages with resolved speaker metadata.
   *
   * @param script the raw script text (must be non-null)
   * @return a list of resolved {@link DialogEntry} pages (may be empty if the script contains only
   *     whitespace / page breaks)
   */
  static List<DialogEntry> parse(String script) {
    String[] parts = PAGE_BREAK_PATTERN.split(script, -1);
    List<DialogEntry> out = new ArrayList<>(parts.length);
    // Initial state: no speaker (until a [speaker] tag is encountered).
    String currentImage = null;
    String currentName = null;
    for (String rawPart : parts) {
      String part = rawPart;
      SpeakerTagParse parsed = parseLeadingSpeakerTag(part);
      if (parsed != null) {
        TagParams tp = TagParams.parse(parsed.params());
        if (tp.getBoolean("clear")) {
          currentImage = null;
          currentName = null;
        } else {
          // A speaker tag starts a fresh speaker context: omitted fields fall back to defaults
          // instead of inheriting from previous pages.
          String img = tp.getString("img", null);
          String name = tp.getString("name", null);
          currentImage = img != null ? img : DEFAULT_SPEAKER_IMAGE;
          currentName = name != null ? name : DEFAULT_SPEAKER_NAME;
        }
        part = parsed.remainingText();
      }
      String text = part.strip();
      if (text.isEmpty()) continue;
      if (currentImage == null) {
        out.add(DialogEntry.noSpeaker(text));
      } else {
        out.add(DialogEntry.of(currentName, currentImage, text));
      }
    }
    return out;
  }

  /**
   * Parses a script and throws a dialog creation exception if it does not produce any pages.
   *
   * @param script the script to parse
   * @param emptyScriptError supplier for an error message when parsing yields no pages
   * @return non-empty parsed entries
   */
  static List<DialogEntry> parseNonEmpty(String script, Supplier<String> emptyScriptError) {
    List<DialogEntry> entries = parse(script);
    if (entries.isEmpty()) {
      throw new DialogCreationException(emptyScriptError.get());
    }
    return entries;
  }

  /**
   * Formats parsed entries into a single plain-text payload for headless logging/forwarding.
   *
   * <p>Speaker pages are formatted as {@code <name>: <text>}; speaker-less pages are emitted as
   * plain {@code <text>} lines.
   *
   * @param entries parsed dialog entries
   * @return newline-separated headless payload
   */
  static String toHeadlessText(List<DialogEntry> entries) {
    StringBuilder combined = new StringBuilder();
    for (DialogEntry e : entries) {
      if (!combined.isEmpty()) combined.append('\n');
      if (e.hasSpeaker()) {
        combined.append(e.speakerName()).append(": ");
      }
      combined.append(e.text());
    }
    return combined.toString();
  }

  /**
   * Result of parsing a leading {@code [speaker ...]} tag from a page.
   *
   * @param params raw speaker-tag parameter string (without surrounding brackets)
   * @param remainingText page text after removing the leading speaker tag and trailing whitespace
   */
  private record SpeakerTagParse(String params, String remainingText) {}

  private static SpeakerTagParse parseLeadingSpeakerTag(String page) {
    int len = page.length();
    int i = 0;
    while (i < len && Character.isWhitespace(page.charAt(i))) i++;

    if (i >= len || page.charAt(i) != '[') return null;
    if (!page.regionMatches(true, i + 1, "speaker", 0, "speaker".length())) return null;

    int afterKeyword = i + 1 + "speaker".length();
    if (afterKeyword >= len) return null;
    char next = page.charAt(afterKeyword);
    if (!(Character.isWhitespace(next) || next == ']')) return null;

    boolean inQuotes = false;
    boolean escaped = false;
    for (int pos = afterKeyword; pos < len; pos++) {
      char c = page.charAt(pos);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (inQuotes && c == '\\') {
        escaped = true;
        continue;
      }
      if (c == '"') {
        inQuotes = !inQuotes;
        continue;
      }
      if (c == ']' && !inQuotes) {
        int textStart = pos + 1;
        while (textStart < len && Character.isWhitespace(page.charAt(textStart))) textStart++;
        return new SpeakerTagParse(page.substring(afterKeyword, pos), page.substring(textStart));
      }
    }

    return null;
  }
}
