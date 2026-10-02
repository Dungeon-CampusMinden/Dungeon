package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Tests callback resolution for dialogs created only on a multiplayer client. */
class DialogCallbackResolverTest {

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

  private DialogContext dialogContext(String dialogId) {
    return DialogContext.builder()
        .type(DialogType.DefaultTypes.DIALOG_DIALOG)
        .dialogId(dialogId)
        .build();
  }
}
