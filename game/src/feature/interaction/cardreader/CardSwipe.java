package feature.interaction.cardreader;

import static engine.input.Binding.key;
import static engine.input.Binding.pad;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.TimeUtils;
import engine.input.ActionId;
import engine.input.Binding.Pad;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.UiActions;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogInput;
import feature.hud.dialogs.DialogType;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The card in the slot of a card reader dialog: where it is and how long the swipe took.
 *
 * <p>The player drags the card through the slot; what counts is the time until it reaches the end.
 * Without a mouse the player holds {@link #SWIPE}; then only the time until it is let go counts.
 * Meanwhile the card runs through the slot in the fastest accepted time, so its arrival shows the
 * earliest moment to let go. Each swipe is rated with the reader's {@link Pace} right away and sent
 * to the server, which rates it again.
 *
 * <p>A dialog of a card reader passes its input on to this class and only draws the card at {@link
 * #position()} and the rating of {@link #result()}. {@link CardReaderUI} is the engine's look; a
 * room replaces it by registering a builder of its own for {@link
 * feature.hud.dialogs.DialogType.DefaultTypes#CARD_READER} with {@link
 * feature.hud.dialogs.DialogFactory#register}.
 */
public final class CardSwipe {

  /** Swipes the card without a mouse while it is held. */
  public static final ActionId SWIPE =
      new ActionId(
          "card_reader",
          "swipe",
          key(Keys.SPACE),
          key(Keys.RIGHT),
          pad(Pad.A),
          pad(Pad.DPAD_RIGHT));

  /** Input context of every card reader dialog. */
  public static final ContextSpec INPUT = UiActions.modal("card_reader", SWIPE);

  /** A rejected card slides back to the start in this time. */
  private static final float RETURN_SECONDS = 0.25f;

  private final Pace pace;
  private final Consumer<DialogResponseMessage.Payload> swipe;
  private final Consumer<DialogResponseMessage.Payload> incomplete;
  private float position;
  private float pull;
  private long startedAt = -1;
  private long arrivedAt = -1;
  private boolean keyRun;
  private float returnFrom;
  private float returned = RETURN_SECONDS;
  private Pace.Result result;

  // Also serves rooms with a dialog of their own look, since they build a CardSwipe as well.
  static {
    DialogInput.register(DialogType.DefaultTypes.CARD_READER, INPUT);
  }

  /**
   * Creates the card of a reader dialog that {@link CardReaderFactory#use(engine.Entity,
   * engine.Entity)} opened. A reader that already accepted a card shows it at the end of the slot.
   *
   * @param context the dialog context with the pace of the reader
   */
  public CardSwipe(DialogContext context) {
    pace =
        new Pace(
            context.require(CardReaderFactory.MIN_MILLIS, Long.class),
            context.require(CardReaderFactory.MAX_MILLIS, Long.class));
    swipe =
        DialogCallbackResolver.createButtonCallback(context.dialogId(), CardReaderFactory.SWIPE);
    incomplete =
        DialogCallbackResolver.createButtonCallback(
            context.dialogId(), CardReaderFactory.INCOMPLETE_SWIPE);
    if (context.find(CardReaderFactory.ACCEPTED, Boolean.class).orElse(false)) {
      position = 1;
      result = Pace.Result.ACCEPTED;
    }
  }

  /**
   * Drags the card along the slot; the first drag starts a swipe.
   *
   * @param distance how far the card was dragged to the right, in lengths of the slot
   */
  public void drag(float distance) {
    if (accepted() || keyRun) return;
    if (startedAt < 0) {
      startedAt = TimeUtils.millis();
      result = null;
    }
    returned = RETURN_SECONDS;
    pull += distance;
    position = Math.clamp(pull, 0, 1);
    if (position >= 1 && arrivedAt < 0) arrivedAt = TimeUtils.millis();
  }

  /** Ends a dragged swipe when the card is let go. */
  public void drop() {
    if (startedAt < 0 || keyRun) return;
    release((arrivedAt < 0 ? TimeUtils.millis() : arrivedAt) - startedAt, position >= 1);
  }

  /**
   * Reads {@link #SWIPE} and moves the card while it is held or while it slides back after a
   * rejected swipe. A dialog calls this every frame.
   *
   * @param delta seconds since the last frame
   */
  public void act(float delta) {
    if (GameInput.justPressed(SWIPE) && !accepted() && startedAt < 0) {
      position = 0;
      returned = RETURN_SECONDS;
      result = null;
      keyRun = true;
      startedAt = TimeUtils.millis();
    }
    if (keyRun) {
      long held = TimeUtils.timeSinceMillis(startedAt);
      position = Math.min(1, (float) held / Math.max(1, pace.minMillis()));
      if (GameInput.justReleased(SWIPE)) {
        release(held, true);
      } else if (!GameInput.pressed(SWIPE)) {
        // A hold cancelled without a release, e.g. when the window loses focus, is no swipe; the
        // card slides back.
        startedAt = -1;
        keyRun = false;
        returnFrom = position;
        returned = 0;
      }
    } else if (returned < RETURN_SECONDS) {
      returned = Math.min(RETURN_SECONDS, returned + delta);
      position = returnFrom * (1 - Interpolation.pow2Out.apply(returned / RETURN_SECONDS));
    }
  }

  /**
   * Returns where the card is.
   *
   * @return 0 at the start of the slot, 1 at its end
   */
  public float position() {
    return position;
  }

  /**
   * Returns the rating of the last swipe.
   *
   * @return the rating, or empty before the first swipe and while the card is swiped
   */
  public Optional<Pace.Result> result() {
    return Optional.ofNullable(result);
  }

  /**
   * Returns whether the reader accepted the card; then the card stays at the end of the slot.
   *
   * @return true after an accepted swipe
   */
  public boolean accepted() {
    return result == Pace.Result.ACCEPTED;
  }

  private void release(long millis, boolean complete) {
    startedAt = -1;
    arrivedAt = -1;
    keyRun = false;
    pull = 0;
    result = pace.rate(millis, complete);
    if (complete) swipe.accept(new DialogResponseMessage.LongValue(millis));
    else incomplete.accept(null);
    if (result != Pace.Result.ACCEPTED) {
      returnFrom = position;
      returned = 0;
    }
  }
}
