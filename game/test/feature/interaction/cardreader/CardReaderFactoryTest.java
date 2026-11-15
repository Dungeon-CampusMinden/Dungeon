package feature.interaction.cardreader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.inventory.items.ItemKey;
import feature.systems.EventScheduler;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the card a card reader requires on the server. */
class CardReaderFactoryTest {

  private final CardReaderComponent component =
      new CardReaderComponent(CardReaderFactory.DEFAULT_PACE);
  private final InventoryComponent inventory = new InventoryComponent(2);
  private final Entity reader = new Entity("reader");
  private final Entity player = new Entity("player");
  private final AtomicReference<Entity> missing = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    component.requireCard(ItemKey.class::isInstance, true, missing::set);
    reader.add(component);
    player.add(inventory);
    Game.add(reader);
    Game.add(player);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    EventScheduler.clear();
  }

  @Test
  void playerWithoutTheCardGetsNoReader() {
    CardReaderFactory.use(reader, player);

    assertSame(player, missing.get());
    assertTrue(dialogs().isEmpty());
  }

  @Test
  void cardOpensTheReaderAndIsConsumedOnceASwipeIsAccepted() {
    inventory.add(new ItemKey());
    CardReaderFactory.use(reader, player);
    List<UIComponent> dialogs = dialogs();
    assertEquals(1, dialogs.size());

    swipe(dialogs.getFirst(), Pace.encode(100, true));
    assertEquals(1, inventory.count());
    assertFalse(component.accepted());

    swipe(dialogs.getFirst(), Pace.encode(600, true));
    assertEquals(0, inventory.count());
    assertTrue(component.accepted());
  }

  private static List<UIComponent> dialogs() {
    return Game.entities(Set.of(UIComponent.class))
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList();
  }

  private static void swipe(UIComponent ui, String swipe) {
    ui.callbacks()
        .get(CardReaderFactory.SWIPE)
        .accept(new DialogResponseMessage.StringValue(swipe));
  }
}
