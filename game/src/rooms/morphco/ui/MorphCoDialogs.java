package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.function.Function;

/**
 * Custom dialogs of the Morph & Co. room.
 *
 * <p>Every dialog is registered on the server and on each client. The server opens it with {@link
 * DialogFactory#show(DialogContext, int...)}, the client renders it and sends the players' answers
 * back through the callback {@link #SUBMIT}.
 */
public enum MorphCoDialogs implements DialogType {
  /** Word classes for the crates next to the inflection machine. */
  WORD_CLASSES("morphCoWordClasses", WordClassUI::build),
  /** Cutting words and sorting their affixes in A-12. */
  AFFIXES("morphCoAffixes", AffixUI::build),
  /** Pushing the freed affixes into the input shafts of A-12. */
  SHAFTS("morphCoShafts", ShaftUI::build),
  /** The circumfix assembly station. */
  STATION("morphCoStation", StationUI::build),
  /** The production control system in the control room. */
  COMPUTER("morphCoComputer", ComputerUI::build),
  /** Handbook and morpheme classification. */
  DOCUMENT("morphCoDocument", DocumentUI::build);

  /** Callback key for an answer; the payload is a string value. */
  public static final String SUBMIT = "morphCoSubmit";

  /** Context key: name of the document, see {@link DocumentUI}. */
  public static final String DOCUMENT_ID = "morphCoDocumentId";

  /** Document: handbook of the inflection machine FL3-10-9001. */
  public static final String HANDBOOK = "handbook";

  /** Document: "Morphemtypen und wo sie zu finden sind", part 1 (free morphemes). */
  public static final String MORPH_1 = "morph1";

  /** Document: "Morphemtypen und wo sie zu finden sind", part 2 (bound morphemes). */
  public static final String MORPH_2 = "morph2";

  /** Context key: whether F-04 has been repaired. */
  public static final String F04_OK = "morphCoF04Ok";

  /** Context key: whether A-12 has been repaired. */
  public static final String A12_OK = "morphCoA12Ok";

  /** Context key: fragments in the inventory of the player at the station. */
  public static final String FRAGMENTS = "morphCoFragments";

  /** Context key: circumfixes that have already been assembled. */
  public static final String ASSEMBLED = "morphCoAssembled";

  private final String type;
  private final Function<DialogContext, Group> builder;

  MorphCoDialogs(String type, Function<DialogContext, Group> builder) {
    this.type = type;
    this.builder = builder;
  }

  @Override
  public String type() {
    return type;
  }

  /** Registers all dialogs; safe to call several times. */
  public static void register() {
    for (MorphCoDialogs dialog : values()) DialogFactory.register(dialog, dialog.builder);
  }
}
