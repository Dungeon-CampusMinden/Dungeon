package feature.interaction.phone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests how a phone answers dialed numbers on the server. */
class PhoneFactoryTest {

  private final PhoneComponent component = new PhoneComponent(4);
  private final Entity phone = new Entity("phone");
  private final Entity player = new Entity("player");
  private final AtomicReference<Entity> mailbox = new AtomicReference<>();
  private final AtomicReference<Entity> hotline = new AtomicReference<>();
  private final AtomicReference<Entity> noConnection = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    component.addLine(new PhoneLine("99", "Mailbox", true), mailbox::set);
    component.addLine(new PhoneLine("42", "Hotline", false), hotline::set);
    component.onNoConnection(noConnection::set);
    phone.add(component);
    Game.add(phone);
    Game.add(player);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  @Test
  void knownNumberCallsOnlyItsLineAndClosesTheDialog() {
    PhoneFactory.use(phone, player);
    dial(dialogs().getFirst(), "42");

    assertSame(player, hotline.get());
    assertNull(mailbox.get());
    assertNull(noConnection.get());
    assertEquals(List.of(), dialogs());
  }

  @Test
  void numberWithoutLineKeepsThePhoneOpen() {
    PhoneFactory.use(phone, player);
    dial(dialogs().getFirst(), "4");

    assertSame(player, noConnection.get());
    assertNull(hotline.get());
    assertEquals(1, dialogs().size());
  }

  private static List<UIComponent> dialogs() {
    return Game.entities(Set.of(UIComponent.class))
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList();
  }

  private static void dial(UIComponent ui, String number) {
    ui.callbacks().get(PhoneFactory.DIAL).accept(new DialogResponseMessage.StringValue(number));
  }
}
