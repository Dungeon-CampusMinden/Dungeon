package feature.interaction.cardreader;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.systems.EventScheduler;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Factory class for creating card reader entities, and the server side of their swipe dialog.
 *
 * <p>A card reader entity has a {@link CardReaderComponent}. Using the reader shows its swipe
 * dialog {@link DialogType.DefaultTypes#CARD_READER}, see {@link #use(Entity, Entity)}. The dialog
 * rates every swipe with the reader's {@link Pace} right away and sends it to the server, which
 * rates it again. After an accepted swipe, the swipe dialogs of all players close and the {@link
 * CardReaderComponent#onAccepted} callback runs.
 */
public final class CardReaderFactory {

  /** Between 0.3 and 1.2 s. Among Us asks for about 0.5 s; the reader is more forgiving. */
  public static final Pace DEFAULT_PACE = new Pace(300, 1200);

  /** Callback key of a swipe; the payload is a string value from {@link Pace#encode}. */
  public static final String SWIPE = "cardReaderSwipe";

  // Dialog attributes read by CardSwipe: the pace and whether the reader already accepted a card.
  static final String MIN_MILLIS = "cardReaderMinMillis";
  static final String MAX_MILLIS = "cardReaderMaxMillis";
  static final String ACCEPTED = "cardReaderAccepted";

  private static final float DEFAULT_INTERACTION_RADIUS = 1.5f;
  // The green light stays on for a moment before the reader closes.
  private static final long CLOSE_DELAY_MS = 700;
  private static final IPath TEXTURE_CLOSED = new SimpleIPath("objects/card_reader/closed.png");
  private static final IPath TEXTURE_OPEN = new SimpleIPath("objects/card_reader/open.png");

  private CardReaderFactory() {}

  /**
   * Configures a supplied entity as a card reader with the engine's swipe dialog. Its light turns
   * green once a single-use reader accepted a card.
   *
   * @param entity entity to configure
   * @param pos position where the reader is created
   * @param pace how fast a swipe has to be
   * @return the supplied configured entity; the card and the callbacks are set on its {@link
   *     CardReaderComponent}
   */
  public static Entity createCardReader(Entity entity, Point pos, Pace pace) {
    entity.add(new PositionComponent(pos));
    State closed = new State("closed", TEXTURE_CLOSED);
    State open = new State("open", TEXTURE_OPEN);
    StateMachine sm = new StateMachine(Arrays.asList(closed, open));
    sm.addTransition(closed, "open", open);
    sm.addTransition(open, "close", closed);
    entity.add(new DrawComponent(sm));
    entity.add(new CollideComponent());
    entity.add(new CardReaderComponent(pace));
    entity.add(
        new InteractionComponent(
            new Interaction(CardReaderFactory::use, DEFAULT_INTERACTION_RADIUS)));
    return entity;
  }

  /**
   * Lets a player use a card reader on the server. A player without the required card only gets the
   * missing-card callback of the reader, see {@link CardReaderComponent#requireCard}; anybody else
   * sees the swipe dialog. A single-use reader that already accepted a card shows it in the dialog
   * and takes no more swipes.
   *
   * @param reader entity with a {@link CardReaderComponent}
   * @param user the player using the reader
   */
  public static void use(Entity reader, Entity user) {
    CardReaderComponent component = reader.fetch(CardReaderComponent.class).orElseThrow();
    if (!component.accepted()
        && component.card != null
        && heldCard(user, component.card).isEmpty()) {
      component.onMissingCard.accept(user);
      return;
    }
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.CARD_READER)
            .put(DialogContextKeys.ENTITY, reader.id())
            .put(MIN_MILLIS, component.pace().minMillis())
            .put(MAX_MILLIS, component.pace().maxMillis())
            .put(ACCEPTED, component.accepted())
            .build();
    UIComponent ui = DialogFactory.show(context, user.id());
    ui.registerCallback(
        SWIPE,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String swipe)) {
            swipe(reader, user, ui, swipe);
          }
        });
  }

  /**
   * Rates a swipe on the server, see {@link Pace#encode(long, boolean)}.
   *
   * <p>A swipe at the reader's pace by a player who still holds the required card is accepted: a
   * single-use reader locks, the card is consumed if the reader is set to, and once the light was
   * green for a moment, the swipe dialogs close and the accepted callback runs. Any other swipe
   * runs the rejected callback; the player may try again, unless the card is gone, which closes the
   * dialog.
   *
   * @param reader entity with a {@link CardReaderComponent}
   * @param user the player who swiped
   * @param ui the swipe dialog of the player
   * @param swipe the encoded swipe
   */
  static void swipe(Entity reader, Entity user, UIComponent ui, String swipe) {
    CardReaderComponent component = reader.fetch(CardReaderComponent.class).orElseThrow();
    // Another player may have swiped a moment earlier.
    if (component.accepted()) return;
    Optional<Item> card =
        component.card == null ? Optional.empty() : heldCard(user, component.card);
    // The card may have been dropped while the dialog was open; the dialog shows it accepted.
    boolean cardGone = component.card != null && card.isEmpty();
    if (cardGone || component.pace().rate(swipe) != Pace.Result.ACCEPTED) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(user.id()));
      if (cardGone) UIUtils.closeDialog(ui, true);
      component.onRejected.accept(user);
      return;
    }
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_select_001"));
    if (component.singleUse()) {
      component.accepted(true);
      reader.fetch(DrawComponent.class).ifPresent(draw -> draw.sendSignal("open"));
    }
    if (component.consumeCard) {
      card.ifPresent(
          item -> user.fetch(InventoryComponent.class).ifPresent(items -> items.remove(item)));
    }
    EventScheduler.scheduleAction(
        () -> {
          UIUtils.closeDialogs(DialogType.DefaultTypes.CARD_READER, reader);
          // A reader reset or removed meanwhile raises nothing.
          if (Game.findEntityById(reader.id()).isEmpty()) return;
          component.onAccepted.accept(user);
        },
        CLOSE_DELAY_MS);
  }

  private static Optional<Item> heldCard(Entity user, Predicate<Item> card) {
    return user.fetch(InventoryComponent.class).stream()
        .flatMap(inventory -> Arrays.stream(inventory.items()))
        .filter(Objects::nonNull)
        .filter(card)
        .findFirst();
  }
}
