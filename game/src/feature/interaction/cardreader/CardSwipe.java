package feature.interaction.cardreader;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.TimeUtils;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The card in the slot of a card reader dialog: where it is and how long the swipe took.
 *
 * <p>The player drags the card through the slot; what counts is the time until it reaches the end.
 * Without a mouse the player holds space or the right arrow key while the card runs through on its
 * own; then the time until the key is let go counts. Each swipe is rated with the reader's {@link
 * Pace} right away and sent to the server, which rates it again.
 *
 * <p>A dialog of a card reader passes its input on to this class and only draws the card at {@link
 * #position()} and the rating of {@link #result()}. {@link CardReaderUI} is the engine's look; a
 * room replaces it by registering a builder of its own for {@link
 * feature.hud.dialogs.DialogType.DefaultTypes#CARD_READER} with {@link
 * feature.hud.dialogs.DialogFactory#register}.
 */
public final class CardSwipe {

  /** Without a mouse the card needs this long for the slot, so letting go at the end fits. */
  private static final float KEY_RUN_SECONDS = 0.45f;

  /** A rejected card slides back to the start in this time. */
  private static final float RETURN_SECONDS = 0.25f;

  private final Pace pace;
  private final Consumer<DialogResponseMessage.Payload> server;
  private float position;
  private float pull;
  private long startedAt = -1;
  private long arrivedAt = -1;
  private boolean keyRun;
  private float returnFrom;
  private float returned = RETURN_SECONDS;
  private Pace.Result result;

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
    server =
        DialogCallbackResolver.createButtonCallback(context.dialogId(), CardReaderFactory.SWIPE);
    if (context.find(CardReaderFactory.ACCEPTED, Boolean.class).orElse(false)) {
      position = 1;
      result = Pace.Result.ACCEPTED;
    }
  }

  /**
   * Returns the listener for swipes without a mouse: space or the right arrow key starts a swipe
   * and letting go ends it. A dialog adds it and takes the keyboard focus.
   *
   * @return the key listener
   */
  public InputListener keys() {
    return new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        return CardSwipe.this.keyDown(keycode);
      }

      @Override
      public boolean keyUp(InputEvent event, int keycode) {
        return CardSwipe.this.keyUp(keycode);
      }
    };
  }

  private boolean keyDown(int keycode) {
    if (!swipeKey(keycode) || accepted() || startedAt >= 0) return false;
    position = 0;
    returned = RETURN_SECONDS;
    result = null;
    keyRun = true;
    startedAt = TimeUtils.millis();
    return true;
  }

  private boolean keyUp(int keycode) {
    if (!swipeKey(keycode) || !keyRun) return false;
    release(TimeUtils.timeSinceMillis(startedAt));
    return true;
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
    release((arrivedAt < 0 ? TimeUtils.millis() : arrivedAt) - startedAt);
  }

  /**
   * Moves the card while a key holds it or while it slides back after a rejected swipe.
   *
   * @param delta seconds since the last frame
   */
  public void act(float delta) {
    if (keyRun) {
      position = Math.min(1, position + delta / KEY_RUN_SECONDS);
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

  private void release(long millis) {
    String swipe = Pace.encode(millis, position >= 1);
    startedAt = -1;
    arrivedAt = -1;
    keyRun = false;
    pull = 0;
    result = pace.rate(swipe);
    server.accept(new DialogResponseMessage.StringValue(swipe));
    if (result != Pace.Result.ACCEPTED) {
      returnFrom = position;
      returned = 0;
    }
  }

  private static boolean swipeKey(int keycode) {
    return keycode == Input.Keys.SPACE || keycode == Input.Keys.RIGHT;
  }
}
