package contrib.hud.dialogs;

import contrib.hud.elements.RichLabel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A single entry in a {@link DialogScriptView} sequence, representing one occurrence of a speaker
 * saying something (or just a plain block of text without a speaker).
 *
 * <p>Each entry consists of:
 *
 * <ul>
 *   <li>{@code speakerName} – the displayed name of the speaker (may contain {@link RichLabel}
 *       markup). May be {@code null} when this entry has no speaker (see {@link #hasSpeaker()}).
 *   <li>{@code imagePath} – the classpath/internal path to the speaker portrait image. May be
 *       {@code null} when this entry has no speaker.
 *   <li>{@code text} – what the speaker says (may contain {@link RichLabel} markup, including
 *       {@code [tr]} for typewriter rendering).
 * </ul>
 *
 * <p>An entry without a speaker (both {@code speakerName} and {@code imagePath} are {@code null})
 * is rendered without the portrait/name column; the text takes up the full content width.
 *
 * <p>{@code DialogEntry} is {@link Serializable} so a list of entries can be transported as part of
 * a {@link DialogContext} attribute payload (e.g. across the network, mirroring how {@link
 * ChoiceOption} is used by {@link MultipleChoiceDialog}).
 *
 * @param speakerName Display name of the speaker, or {@code null} for no speaker.
 * @param imagePath Classpath/internal path to the speaker portrait image, or {@code null} for no
 *     speaker.
 * @param text The line of dialogue (may contain RichLabel markup).
 */
public record DialogEntry(String speakerName, String imagePath, String text)
    implements Serializable {

  /**
   * Creates a new dialog entry with a speaker.
   *
   * @param speakerName Display name of the speaker (may be empty, may contain RichLabel markup).
   * @param imagePath Classpath/internal path to the speaker portrait image.
   * @param text The line of dialogue (may contain RichLabel markup).
   * @return A new {@link DialogEntry}.
   */
  public static DialogEntry of(String speakerName, String imagePath, String text) {
    return new DialogEntry(speakerName, imagePath, text);
  }

  /**
   * Creates a new dialog entry without a speaker. The portrait/name column will be omitted when
   * this entry is rendered.
   *
   * @param text The line of dialogue (may contain RichLabel markup).
   * @return A new speaker-less {@link DialogEntry}.
   */
  public static DialogEntry noSpeaker(String text) {
    return new DialogEntry(null, null, text);
  }

  /**
   * Returns whether this entry has a speaker (and should therefore render the portrait/name
   * column).
   *
   * @return {@code true} if a speaker is set, {@code false} for plain text-only entries.
   */
  public boolean hasSpeaker() {
    return imagePath != null;
  }

  /**
   * Convenience factory for creating a list of dialog entries.
   *
   * @param entries The entries to include.
   * @return An unmodifiable list containing the given entries.
   */
  public static List<DialogEntry> ofList(DialogEntry... entries) {
    return Arrays.asList(entries);
  }

  /**
   * Convenience factory for creating a list of dialog entries from a collection.
   *
   * @param entries The entries to include.
   * @return A list containing the given entries.
   */
  public static List<DialogEntry> ofList(Collection<DialogEntry> entries) {
    return List.copyOf(entries);
  }
}
