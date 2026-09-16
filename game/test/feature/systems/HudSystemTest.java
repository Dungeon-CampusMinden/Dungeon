package feature.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.handler.NettyNetworkHandler;
import engine.network.messages.s2c.DialogCloseMessage;
import engine.network.messages.s2c.DialogShowMessage;
import engine.network.server.ClientState;
import engine.network.server.DialogTracker;
import engine.network.server.ServerRuntime;
import engine.network.server.ServerTransport;
import engine.network.server.Session;
import feature.components.UIComponent;
import feature.entities.CharacterClass;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Tests for {@link HudSystem}. */
public class HudSystemTest {

  private HudSystem hudSystem;

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
    DialogFactory.register(TestDialogType.DISPOSABLE, ignored -> new TestDisposableGroup());
    DialogFactory.register(
        TestDialogType.REJECTED,
        ignored -> {
          throw new AssertionError("A dialog without a present target must not be created");
        });
  }

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    hudSystem = Game.hud();
    Game.add(hudSystem);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    DialogTracker.instance().clear();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /** Replacing a player's UI keeps the HUD query synchronized with the current component. */
  @Test
  public void replacingUiComponentRunsFullLifecycle() {
    Entity player = player();
    UIComponent first = ui(player, player.id());
    player.add(first);

    assertTrue(hudSystem.hasOpenUI(player));
    assertSame(first, hudSystem.topmostCloseableUI().orElseThrow().b());

    UIComponent second = ui(player, false, player.id());
    player.add(second);

    assertTrue(hudSystem.hasOpenUI(player));
    assertFalse(hudSystem.hasOpenPausingUI(player));
    assertSame(second, player.fetch(UIComponent.class).orElseThrow());
    assertSame(second, hudSystem.topmostCloseableUI().orElseThrow().b());

    UIUtils.closeDialog(first);
    assertSame(second, player.fetch(UIComponent.class).orElseThrow());
    assertTrue(hudSystem.hasOpenUI(player));

    UIUtils.closeDialog(second);
    assertFalse(hudSystem.hasOpenUI(player));
  }

  /** Temporarily detaching the HUD keeps a dialog reusable until its component is removed. */
  @Test
  public void readdingHudSystemDoesNotDisposeDialog() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.DISPOSABLE, player, true, player.id());
    player.add(component);
    TestDisposableGroup dialog = (TestDisposableGroup) component.dialog();

    Game.remove(HudSystem.class);

    assertFalse(dialog.disposed);
    assertEquals(-1, DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));

    Game.add(hudSystem);

    assertFalse(dialog.disposed);
    assertTrue(hudSystem.hasOpenUI(player));
    assertEquals(
        player.id(), DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));

    UIUtils.closeDialog(component);
    assertTrue(dialog.disposed);
  }

  /** Removing a UI-owning entity performs terminal dialog cleanup. */
  @Test
  public void removingUiOwnerDisposesDialog() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.DISPOSABLE, player, true, player.id());
    player.add(component);
    TestDisposableGroup dialog = (TestDisposableGroup) component.dialog();

    Game.remove(player);

    assertTrue(dialog.disposed);
    assertFalse(hudSystem.hasOpenUI(player));
    assertEquals(-1, DialogTracker.instance().getEntityId(component.dialogContext().dialogId()));
  }

  /** Empty targets affect every player while explicit targets only affect matching players. */
  @Test
  public void uiTargetsDetermineWhoIsAffected() {
    Entity player = player();
    Entity otherPlayer = player();
    NettyNetworkHandler networkHandler = Mockito.mock(NettyNetworkHandler.class);
    Mockito.when(networkHandler.isServer()).thenReturn(true);
    Mockito.when(networkHandler.serverRuntime()).thenReturn(Optional.empty());
    MockNetworkHandler.useNetworkHandler(networkHandler);

    UIComponent global = show(player);
    assertTrue(hudSystem.hasOpenUI(player));
    assertTrue(hudSystem.hasOpenUI(otherPlayer));

    UIUtils.closeDialog(global);
    assertFalse(hudSystem.hasOpenUI(player));

    UIComponent targeted = show(player, player.id());
    assertTrue(hudSystem.hasOpenUI(player));
    assertFalse(hudSystem.hasOpenUI(otherPlayer));

    UIUtils.closeDialog(targeted);
    assertFalse(hudSystem.hasOpenUI(player));
  }

  /** A targeted UI is not created when none of its target players are present. */
  @Test
  public void uiWithoutPresentTargetIsNotCreated() {
    Entity player = player();
    UIComponent component = ui(TestDialogType.REJECTED, player, true, Integer.MAX_VALUE);

    player.add(component);

    assertFalse(hudSystem.hasOpenUI(player));
    player.remove(UIComponent.class);
  }

  /** Non-suppressible dialogs stay visible while normal HUD dialogs are temporarily hidden. */
  @Test
  public void nonSuppressibleDialogStaysVisibleDuringSuppression() {
    Entity player = player();
    UIComponent normal = show(player, player.id());

    hudSystem.dialogsSuppressed(true);

    assertFalse(normal.isVisible());
    assertFalse(hudSystem.hasOpenUI(player));

    Entity editorDialogOwner = new Entity("editor-dialog");
    Game.add(editorDialogOwner);
    UIComponent editorDialog =
        ui(TestDialogType.TEST, editorDialogOwner, true, true, false, player.id());
    editorDialogOwner.add(editorDialog);

    assertTrue(editorDialog.isVisible());
    assertTrue(hudSystem.hasOpenUI(player));

    hudSystem.dialogsSuppressed(false);

    assertTrue(normal.isVisible());
    assertTrue(editorDialog.isVisible());
  }

  /** Joining clients receive an open dialog only once their world is ready. */
  @Test
  public void dialogWaitsForInitialWorldReady() {
    Entity player = player();
    ClientState state =
        new ClientState((short) 1, "tester", 1, new byte[] {1}, CharacterClass.WIZARD);
    state.playerEntity(player);
    NettyNetworkHandler network = serverNetwork(state);

    UIComponent intro = show(player, player.id());

    Mockito.verify(network, Mockito.never())
        .send(Mockito.anyShort(), Mockito.any(DialogShowMessage.class), Mockito.anyBoolean());
    state.initialWorldReady(true);
    DialogTracker.instance().resyncDialogsToClient(state.clientId());
    Mockito.verify(network)
        .send(Mockito.eq(state.clientId()), Mockito.any(DialogShowMessage.class), Mockito.eq(true));
    UIUtils.closeDialog(intro, true);
    Mockito.verify(network)
        .send(
            Mockito.eq(state.clientId()), Mockito.any(DialogCloseMessage.class), Mockito.eq(true));
    DialogTracker.instance().resyncDialogsToClient(state.clientId());
    Mockito.verify(network)
        .send(Mockito.eq(state.clientId()), Mockito.any(DialogShowMessage.class), Mockito.eq(true));
  }

  /** A dialog closed during bootstrap must never appear when the client finishes joining. */
  @Test
  public void closedBootstrapDialogIsNotDelivered() {
    Entity player = player();
    ClientState state =
        new ClientState((short) 1, "tester", 1, new byte[] {1}, CharacterClass.WIZARD);
    state.playerEntity(player);
    NettyNetworkHandler network = serverNetwork(state);
    UIComponent intro = show(player, player.id());
    UIUtils.closeDialog(intro, true);

    state.initialWorldReady(true);
    DialogTracker.instance().resyncDialogsToClient(state.clientId());

    Mockito.verify(network, Mockito.never())
        .send(Mockito.anyShort(), Mockito.any(DialogShowMessage.class), Mockito.anyBoolean());
  }

  /** Shared dialogs use the same recipients for initial delivery, bootstrap and server close. */
  @Test
  public void sharedDialogTracksReadyAndJoiningRecipients() {
    Entity player = player();
    Entity joiningPlayer = player();
    ClientState ready =
        new ClientState((short) 1, "ready", 1, new byte[] {1}, CharacterClass.WIZARD);
    ready.playerEntity(player);
    ready.initialWorldReady(true);
    ClientState joining =
        new ClientState((short) 2, "joining", 1, new byte[] {2}, CharacterClass.WIZARD);
    joining.playerEntity(joiningPlayer);
    NettyNetworkHandler network = serverNetwork(ready, joining);

    UIComponent shared = show(player);

    Mockito.verify(network)
        .send(Mockito.eq(ready.clientId()), Mockito.any(DialogShowMessage.class), Mockito.eq(true));
    Mockito.verify(network, Mockito.never())
        .send(
            Mockito.eq(joining.clientId()),
            Mockito.any(DialogShowMessage.class),
            Mockito.anyBoolean());
    assertTrue(
        DialogTracker.instance().canRespond(ready.clientId(), shared.dialogContext().dialogId()));
    assertTrue(
        DialogTracker.instance().canRespond(joining.clientId(), shared.dialogContext().dialogId()));
    assertFalse(DialogTracker.instance().canRespond((short) 3, shared.dialogContext().dialogId()));

    joining.initialWorldReady(true);
    DialogTracker.instance().resyncDialogsToClient(joining.clientId());
    Mockito.verify(network)
        .send(
            Mockito.eq(joining.clientId()), Mockito.any(DialogShowMessage.class), Mockito.eq(true));
    UIUtils.closeDialog(shared, true);
    Mockito.verify(network)
        .send(
            Mockito.eq(ready.clientId()), Mockito.any(DialogCloseMessage.class), Mockito.eq(true));
    Mockito.verify(network)
        .send(
            Mockito.eq(joining.clientId()),
            Mockito.any(DialogCloseMessage.class),
            Mockito.eq(true));
  }

  /** A private dialog never reaches a different world-ready player, including during resync. */
  @Test
  public void targetedDialogOnlyReachesItsPlayer() {
    Entity player = player();
    Entity otherPlayer = player();
    ClientState first =
        new ClientState((short) 1, "first", 1, new byte[] {1}, CharacterClass.WIZARD);
    first.playerEntity(player);
    first.initialWorldReady(true);
    ClientState other =
        new ClientState((short) 2, "other", 1, new byte[] {2}, CharacterClass.WIZARD);
    other.playerEntity(otherPlayer);
    other.initialWorldReady(true);
    NettyNetworkHandler network = serverNetwork(first, other);

    UIComponent privateDialog = show(player, player.id());
    DialogTracker.instance().resyncDialogsToClient(other.clientId());
    UIUtils.closeDialog(privateDialog, true);

    Mockito.verify(network)
        .send(Mockito.eq(first.clientId()), Mockito.any(DialogShowMessage.class), Mockito.eq(true));
    Mockito.verify(network, Mockito.never())
        .send(Mockito.eq(other.clientId()), Mockito.any(), Mockito.anyBoolean());
  }

  private static NettyNetworkHandler serverNetwork(ClientState... states) {
    PreRunConfiguration.multiplayerEnabled(true);
    NettyNetworkHandler network = Mockito.mock(NettyNetworkHandler.class);
    ServerRuntime runtime = Mockito.mock(ServerRuntime.class);
    ServerTransport transport = Mockito.mock(ServerTransport.class);
    Mockito.when(network.isServer()).thenReturn(true);
    Mockito.when(network.serverRuntime()).thenReturn(Optional.of(runtime));
    Mockito.when(runtime.transport()).thenReturn(Optional.of(transport));
    Map<Short, Session> sessions = new HashMap<>();
    for (ClientState state : states) {
      Session session = Mockito.mock(Session.class);
      Mockito.when(session.clientState()).thenReturn(Optional.of(state));
      Mockito.when(session.clientId()).thenReturn(state.clientId());
      sessions.put(state.clientId(), session);
    }
    Mockito.when(transport.clientIdToSessionMap()).thenReturn(sessions);
    MockNetworkHandler.useNetworkHandler(network);
    return network;
  }

  private static Entity player() {
    Entity player = new Entity("player");
    player.add(new PlayerComponent());
    Game.add(player);
    return player;
  }

  private static UIComponent show(Entity owner, int... targetEntityIds) {
    UIComponent component = ui(owner, targetEntityIds);
    owner.add(component);
    return component;
  }

  private static UIComponent ui(Entity owner, int... targetEntityIds) {
    return ui(owner, true, targetEntityIds);
  }

  private static UIComponent ui(Entity owner, boolean willPauseGame, int... targetEntityIds) {
    return ui(TestDialogType.TEST, owner, willPauseGame, targetEntityIds);
  }

  private static UIComponent ui(
      DialogType dialogType, Entity owner, boolean willPauseGame, int... targetEntityIds) {
    return ui(dialogType, owner, willPauseGame, true, true, targetEntityIds);
  }

  private static UIComponent ui(
      DialogType dialogType,
      Entity owner,
      boolean willPauseGame,
      boolean canBeClosed,
      boolean suppressible,
      int... targetEntityIds) {
    DialogContext context =
        new DialogContext(
            dialogType,
            true,
            Map.of(feature.hud.dialogs.DialogContextKeys.OWNER_ENTITY, owner.id()));
    return new UIComponent(context, willPauseGame, canBeClosed, suppressible, targetEntityIds);
  }

  private enum TestDialogType implements DialogType {
    TEST,
    DISPOSABLE,
    REJECTED;

    @Override
    public String type() {
      return "HUD_SYSTEM_TEST";
    }
  }

  private static final class TestDisposableGroup extends Group implements Disposable {
    private boolean disposed;

    @Override
    public void dispose() {
      disposed = true;
    }
  }
}
