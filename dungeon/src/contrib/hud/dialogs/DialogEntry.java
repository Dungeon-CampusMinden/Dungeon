package contrib.hud.dialogs;

import contrib.hud.elements.RichLabel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A single entry in a {@link DialogDialog} sequence, representing one occurrence of a speaker
 * saying something.
 *
 * <p>Each entry consists of:
 *
 * <ul>
 *   <li>{@code speakerName} – the displayed name of the speaker (may contain {@link RichLabel}
 *       markup).
 *   <li>{@code imagePath} – the classpath/internal path to the speaker portrait image.
 *   <li>{@code text} – what the speaker says (may contain {@link RichLabel} markup, including
 *       {@code [tr]} for typewriter rendering).
 * </ul>
 *
 * <p>{@code DialogEntry} is {@link Serializable} so a list of entries can be transported as part of
 * a {@link DialogContext} attribute payload (e.g. across the network, mirroring how {@link
 * ChoiceOption} is used by {@link MultipleChoiceDialog}).
 *
 * @param speakerName Display name of the speaker (may be empty, may contain RichLabel markup).
 * @param imagePath Classpath/internal path to the speaker portrait image.
 * @param text The line of dialogue (may contain RichLabel markup).
 */
public record DialogEntry(String speakerName, String imagePath, String text)
    implements Serializable {

  /**
   * Creates a new dialog entry.
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
