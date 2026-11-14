package feature.interaction.cables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the server's check of the wiring and of wrong plugs that the cable dialogs send. */
class CablesTest {

  // Cable 0 belongs in socket 2, cable 1 into socket 0, cable 2 into socket 1.
  private static final CablePuzzle PUZZLE =
      new CablePuzzle(
          List.of(
              new CablePuzzle.Cable("int", Color.RED, 2),
              new CablePuzzle.Cable("double", Color.BLUE, 0),
              new CablePuzzle.Cable("String", Color.YELLOW, 1)),
          List.of("3.14", "\"Hi\"", "42"));

  private final Entity box = new Entity("box");
  private final Entity alice = new Entity("alice");
  private final Entity bob = new Entity("bob");
  private final List<Entity> reactions = new ArrayList<>();

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.add(box);
    Game.add(alice);
    Game.add(bob);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  @Test
  void onlyTheRightWiringConnectsOnceAndClosesTheOtherDialogsOfTheBox() {
    CablesComponent cables = cables(true);
    cables.onConnected(reactions::add);
    Entity otherBox = new Entity("other box");
    otherBox.add(new CablesComponent(PUZZLE, true));
    Game.add(otherBox);
    UIComponent own = Cables.show(box, alice, "", "");
    Cables.show(box, bob, "", "");
    UIComponent elsewhere = Cables.show(otherBox, bob, "", "");
    assertEquals(PUZZLE, CablePuzzle.from(own.dialogContext()));

    send(own, Cables.CONNECT, 0, 1, 2);
    assertTrue(reactions.isEmpty());
    assertEquals(3, openDialogs().size());

    send(own, Cables.CONNECT, 2, 0, 1);
    send(own, Cables.CONNECT, 2, 0, 1);
    assertEquals(List.of(alice), reactions);
    assertTrue(cables.connected());
    assertEquals(Set.of(own, elsewhere), Set.copyOf(openDialogs()));
  }

  @Test
  void withoutOnceEveryRightWiringCounts() {
    CablesComponent cables = cables(false);
    cables.onConnected(reactions::add);
    UIComponent ui = Cables.show(box, alice, "", "");

    send(ui, Cables.CONNECT, 2, 0, 1);
    send(ui, Cables.CONNECT, 2, 0, 1);
    assertEquals(List.of(alice, alice), reactions);
    assertFalse(cables.connected());
  }

  @Test
  void onlyRealWrongPlugsReachTheReaction() {
    cables(true).onWrongPlug(reactions::add);
    UIComponent ui = Cables.show(box, alice, "", "");

    send(ui, Cables.WRONG_PLUG, 0, 2);
    send(ui, Cables.WRONG_PLUG, 0, 3);
    send(ui, Cables.WRONG_PLUG, 3, 0);
    assertTrue(reactions.isEmpty());
    send(ui, Cables.WRONG_PLUG, 0, 1);
    assertEquals(List.of(alice), reactions);
  }

  private CablesComponent cables(boolean once) {
    CablesComponent cables = new CablesComponent(PUZZLE, once);
    box.add(cables);
    return cables;
  }

  private static void send(UIComponent ui, String key, int... values) {
    ui.callbacks().get(key).accept(new DialogResponseMessage.IntList(values));
  }

  private static List<UIComponent> openDialogs() {
    return Game.entities(Set.of(UIComponent.class))
        .map(owner -> owner.fetch(UIComponent.class).orElseThrow())
        .toList();
  }
}
