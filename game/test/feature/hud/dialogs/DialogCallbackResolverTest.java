package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.network.handler.INetworkHandler;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testingUtils.MockNetworkHandler;

/** Tests callback resolution for dialogs created only on a multiplayer client. */
class DialogCallbackResolverTest {

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
  }

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    INetworkHandler network = mock(INetworkHandler.class);
    when(network.isConnected()).thenReturn(true);
    MockNetworkHandler.useNetworkHandler(network);
    Game.add(Game.hud());
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    MockNetworkHandler.useLocalNetworkHandler();
  }

  @Test
  void resolvesCallbackFromLocalDialogWithoutUsingServerDialog() {
    String dialogId = "local-controls-dialog";
    AtomicReference<DialogResponseMessage.Payload> localPayload = new AtomicReference<>();
    Entity localOwner = Entity.createLocalEntity("local-dialog");
    localOwner.add(
        new UIComponent(dialogContext(dialogId), false)
            .registerCallback("onConfirm", localPayload::set));

    Entity remoteOwner = new Entity("server-dialog");
    remoteOwner.add(
        new UIComponent(dialogContext(dialogId), false)
            .registerCallback("onConfirm", payload -> {}));

    DialogResponseMessage.Payload payload = new DialogResponseMessage.BoolValue(true);
    DialogCallbackResolver.findLocalCallback(
            Stream.of(remoteOwner, localOwner), dialogId, "onConfirm")
        .orElseThrow()
        .accept(payload);

    assertEquals(payload, localPayload.get());
  }

  @Test
  void doesNotResolveCallbackFromServerOwnedDialog() {
    String dialogId = "server-dialog";
    Entity remoteOwner = new Entity("server-dialog");
    remoteOwner.add(
        new UIComponent(dialogContext(dialogId), false)
            .registerCallback("onConfirm", payload -> {}));

    assertTrue(
        DialogCallbackResolver.findLocalCallback(Stream.of(remoteOwner), dialogId, "onConfirm")
            .isEmpty());
  }

  @Test
  void localCloseRunsItsCallbackAndThenClosesTheDialog() {
    Entity owner = localOwner();
    UIComponent dialog = owner.fetch(UIComponent.class).orElseThrow();
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    DialogCallbackResolver.createButtonCallback(
            dialog.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE)
        .accept(null);

    assertEquals(1, closes.get());
    assertFalse(owner.isPresent(UIComponent.class));
  }

  @Test
  void localCloseWithoutAnExplicitCloseCallbackDoesNotConfirmAnotherAction() {
    Entity owner = localOwner();
    UIComponent dialog = owner.fetch(UIComponent.class).orElseThrow();
    AtomicInteger confirmations = new AtomicInteger();
    dialog.registerCallback(
        DialogContextKeys.ON_CONFIRM, ignored -> confirmations.incrementAndGet());

    DialogCallbackResolver.createButtonCallback(
            dialog.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE)
        .accept(null);

    assertEquals(0, confirmations.get());
    assertFalse(owner.isPresent(UIComponent.class));
  }

  @Test
  void localCloseAlsoCleansUpWhenTheCallbackThrows() {
    Entity owner = localOwner();
    UIComponent dialog = owner.fetch(UIComponent.class).orElseThrow();
    dialog.registerCallback(
        DialogContextKeys.ON_CLOSE,
        ignored -> {
          throw new IllegalStateException("test callback failure");
        });

    var close =
        DialogCallbackResolver.createButtonCallback(
            dialog.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE);
    assertThrows(IllegalStateException.class, () -> close.accept(null));
    assertFalse(owner.isPresent(UIComponent.class));
  }

  @Test
  void localCloseDoesNotRemoveAReplacementDialogOnTheSameOwner() {
    Entity owner = localOwner();
    UIComponent first = owner.fetch(UIComponent.class).orElseThrow();
    UIComponent replacement = new UIComponent(dialogContext("replacement"), false);
    replacement.dialogContext().owner(owner.id());
    first.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> owner.add(replacement));

    DialogCallbackResolver.createButtonCallback(
            first.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE)
        .accept(null);

    assertSame(replacement, owner.fetch(UIComponent.class).orElseThrow());
  }

  @Test
  void aLocalProxyWithoutCallbacksDoesNotBecomeAClientOwnedDialog() {
    Entity proxy = localOwner();
    UIComponent dialog = proxy.fetch(UIComponent.class).orElseThrow();

    assertTrue(
        DialogCallbackResolver.findLocalCallback(
                Stream.of(proxy), dialog.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE)
            .isEmpty());
    assertSame(dialog, proxy.fetch(UIComponent.class).orElseThrow());
  }

  private Entity localOwner() {
    Entity owner = Entity.createLocalEntity("local-dialog");
    DialogContext context = dialogContext("dialog-" + owner.id());
    context.owner(owner.id());
    owner.add(new UIComponent(context, false));
    Game.add(owner);
    return owner;
  }

  private DialogContext dialogContext(String dialogId) {
    return DialogContext.builder().type(TestDialogType.TEST).dialogId(dialogId).build();
  }

  private enum TestDialogType implements DialogType {
    TEST;

    @Override
    public String type() {
      return "callback-resolver-test";
    }
  }
}
