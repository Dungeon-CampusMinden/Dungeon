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
  /** Plugging the cables of the fuse box in the control room back in. */
  FUSE_BOX("morphCoFuseBox", FuseBoxUI::build),
  /** Word classes for the crates next to the inflection machine. */
  WORD_CLASSES("morphCoWordClasses", WordClassUI::build),
  /** Digging through the waste paper basket in F-04 for the defect report. */
  TRASH("morphCoTrash", TrashUI::build),
  /** Close-up of the affix sorter A-12 with display, hatch lock, hatch and lever. */
  AFFIX_SORTER("morphCoAffixSorter", AffixSorterUI::build),
  /** Cutting words and sorting their affixes in A-12. */
  AFFIXES("morphCoAffixes", AffixUI::build),
  /** Pushing the freed affixes into the input shafts of A-12. */
  SHAFTS("morphCoShafts", ShaftUI::build),
  /** The circumfix assembly station. */
  STATION("morphCoStation", StationUI::build),
  /** The production control system in the control room. */
  COMPUTER("morphCoComputer", ComputerUI::build),
  /** The phone in the control room with its keypad and the note of important extensions. */
  PHONE("morphCoPhone", PhoneUI::build),
  /** The book of hamster jokes on the shelf of A-12. */
  HAMSTER_BOOK("morphCoHamsterBook", HamsterBookUI::build),
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

  /** Context key: number of the joke the hamster book falls open at. */
  public static final String JOKE = "morphCoJoke";

  /** Number of jokes in the hamster book. */
  public static final int HAMSTER_JOKES = 100;

  /** Context key: whether F-04 has been repaired. */
  public static final String F04_OK = "morphCoF04Ok";

  /** Context key: whether the defect report in the waste paper basket of F-04 has been found. */
  public static final String COMPLAINT_FOUND = "morphCoComplaintFound";

  /** Context key: whether the mailbox of the phone still holds the unheard message. */
  public static final String MESSAGE_WAITING = "morphCoMessageWaiting";

  /** Context key: whether A-12 has been repaired. */
  public static final String A12_OK = "morphCoA12Ok";

  /** Context key: whether the hatch lock of A-12 is open. */
  public static final String HATCH_OK = "morphCoHatchOk";

  /** Context key: whether the affixes of A-12 have been cut and sorted. */
  public static final String AFFIXES_OK = "morphCoAffixesOk";

  /** Context key: whether the affixes lie in the input shafts of A-12. */
  public static final String SHAFTS_OK = "morphCoShaftsOk";

  /** Context key: part of A-12 that was used in the world; the close-up highlights it. */
  public static final String A12_PART = "morphCoA12Part";

  /** Part of A-12: the keypad of the hatch lock, see {@link A12Answer}. */
  public static final String A12_LOCK = "lock";

  /** Part of A-12: the maintenance hatch, see {@link A12Answer}. */
  public static final String A12_HATCH = "hatch";

  /** Part of A-12: the lever, see {@link A12Answer}. */
  public static final String A12_LEVER = "lever";

  /** Part of A-12: the display. */
  public static final String A12_DISPLAY = "display";

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

  /**
   * Registers all dialogs and the room's look of the engine's card reader at the entrance; safe to
   * call several times.
   */
  public static void register() {
    for (MorphCoDialogs dialog : values()) DialogFactory.register(dialog, dialog.builder);
    DialogFactory.register(DialogType.DefaultTypes.CARD_READER, CardReaderUI::build);
  }

  /**
   * An answer of the A-12 close-up: the part that was used and what was done with it.
   *
   * @param part {@link MorphCoDialogs#A12_LOCK}, {@link MorphCoDialogs#A12_HATCH} or {@link
   *     MorphCoDialogs#A12_LEVER}
   * @param value the typed digits, the encoded pull of the lever or empty for the hatch
   */
  public record A12Answer(String part, String value) {

    /**
     * Encodes an answer for {@link MorphCoDialogs#SUBMIT}.
     *
     * @param part part of A-12
     * @param value what was done with it
     * @return the encoded answer
     */
    public static String encode(String part, String value) {
      return part + ":" + value;
    }

    /**
     * Splits an encoded answer.
     *
     * @param answer answer from {@link #encode(String, String)}
     * @return part and value; without a value the value is empty
     */
    public static A12Answer parse(String answer) {
      String[] parts = answer.split(":", 2);
      return new A12Answer(parts[0], parts.length == 2 ? parts[1] : "");
    }
  }
}
