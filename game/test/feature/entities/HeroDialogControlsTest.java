package feature.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.input.Binding;
import engine.input.ContextLayer;
import engine.input.GameInput;
import engine.input.InputDevice;
import engine.input.InputResolver;
import engine.network.handler.INetworkHandler;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.server.DialogTracker;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.PauseDialog;
import feature.input.systems.GameplayInputSystem;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testingUtils.MockNetworkHandler;

/** Tests the real player close binding through the resolver, the HUD and the gameplay context. */
class HeroDialogControlsTest {

  private GameplayInputSystem gameplayInput;
  private InputResolver resolver;
  private Entity player;

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
  }

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    GameInput.reset();
    resolver = GameInput.resolver();
    Game.add(Game.hud());
    gameplayInput = new GameplayInputSystem();
    Game.add(gameplayInput);

    player = new Entity("player");
    player.add(new PlayerComponent());
    Game.add(player);
    gameplayInput.execute();
  }

  @AfterEach
  void tearDown() {
    GameInput.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    DialogTracker.instance().clear();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  @Test
  void escapeClosesAnOpenDialogWithoutOpeningPause() {
    UIComponent dialog = showDialog(true, true);
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    pressEscape();

    assertEquals(1, closes.get());
    assertEquals(0, uiContexts());
  }

  @Test
  void escapeOpensAndClosesALocalPauseMenuWithoutServerMessages() {
    INetworkHandler network = useNetworkClient();

    pressEscape();

    assertTrue(pauseMenu().orElseThrow().dialogContext().ownerEntity().isLocal());
    assertFalse(player.isPresent(UIComponent.class));

    pressEscape();

    assertEquals(0, uiContexts());
    verify(network, never()).send(anyShort(), any(), anyBoolean());
  }

  @Test
  void serverOwnedDialogSendsCloseRequestInsteadOfClosingLocally() {
    INetworkHandler network = useNetworkClient();
    UIComponent dialog = showDialog(false, true);

    pressEscape();

    verify(network)
        .send(
            eq((short) 0),
            eq(
                new DialogResponseMessage(
                    dialog.dialogContext().dialogId(), DialogContextKeys.ON_CLOSE, null)),
            eq(true));
    assertStillOpen(dialog);
  }

  @Test
  void escapeOpensPauseWhenOnlyPassiveUIIsVisible() {
    UIComponent bar = showPassiveUI();
    assertTrue(Game.hud().hasOpenUI(player));
    assertFalse(Game.hud().hasOpenPausingUI(player));

    pressEscape();

    assertTrue(pauseMenu().isPresent());
    assertTrue(bar.isVisible());
    pressEscape();
    assertEquals(0, uiContexts());
    assertTrue(bar.isVisible());
  }

  @Test
  void passiveUIAddedAboveADialogDoesNotInterceptEscape() {
    UIComponent dialog = showDialog(true, true);
    UIComponent bar = showPassiveUI();
    Group stage = new Group();
    stage.addActor(dialog.dialog());
    stage.addActor(bar.dialog());
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    pressEscape();

    assertEquals(1, closes.get());
    assertTrue(bar.isVisible());
    assertEquals(0, uiContexts());
  }

  @Test
  void serverDialogWithALocalProxyWaitsForTheServerToCloseIt() {
    INetworkHandler network = useNetworkClient();
    DialogContext context = DialogContext.builder().type(TestDialogType.TEST).build();
    context.owner(Integer.MAX_VALUE);
    UIComponent dialog = DialogFactory.show(context, false, true, player.id());
    assertTrue(dialog.dialogContext().ownerEntity().isLocal());
    assertTrue(dialog.callbacks().isEmpty());

    pressEscape();

    verify(network)
        .send(
            eq((short) 0),
            eq(new DialogResponseMessage(context.dialogId(), DialogContextKeys.ON_CLOSE, null)),
            eq(true));
    assertStillOpen(dialog);
  }

  @Test
  void disabledGameplayControlsAllowClosingAPausingDialogWithoutSendingMovement() {
    INetworkHandler network = useNetworkClient();
    player.fetch(PlayerComponent.class).orElseThrow().controlsLocked(true);
    gameplayInput.execute();
    UIComponent dialog = showDialog(true, true);
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    resolver.keyDown(Input.Keys.W);
    pressEscape();
    gameplayInput.execute();

    assertEquals(1, closes.get());
    assertEquals(0, uiContexts());
    verify(network, never()).sendInput(any());
  }

  @Test
  void disabledControlsWithOnlyPassiveUIDoNotOpenMenus() {
    INetworkHandler network = useNetworkClient();
    player.fetch(PlayerComponent.class).orElseThrow().controlsLocked(true);
    gameplayInput.execute();
    showPassiveUI();

    pressEscape();

    assertEquals(0, uiContexts());
    verify(network, never()).sendInput(any());
  }

  @Test
  void localCloseHandlerConsumesEscapeExactlyOnceWithoutSendingAServerResponse() {
    INetworkHandler network = useNetworkClient();
    UIComponent dialog = showDialog(false, true);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);

    pressEscape();

    assertEquals(1, advances.get());
    assertStillOpen(dialog);
    verify(network, never()).send(anyShort(), any(), anyBoolean());
  }

  @Test
  void nonCloseableDialogueReceivesEscapeEvenWithPassiveUIAboveIt() {
    UIComponent dialog = showDialog(true, false);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);
    UIComponent bar = showPassiveUI();
    Group stage = new Group();
    stage.addActor(dialog.dialog());
    stage.addActor(bar.dialog());

    pressEscape();

    assertEquals(1, advances.get());
    assertStillOpen(dialog);
    assertTrue(bar.isVisible());
    assertTrue(pauseMenu().isEmpty());
  }

  @Test
  void disabledGameplayControlsStillAllowTheNonCloseableDialogHandler() {
    INetworkHandler network = useNetworkClient();
    player.fetch(PlayerComponent.class).orElseThrow().controlsLocked(true);
    gameplayInput.execute();
    UIComponent dialog = showDialog(true, false);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);

    resolver.keyDown(Input.Keys.W);
    pressEscape();
    gameplayInput.execute();

    assertEquals(1, advances.get());
    assertStillOpen(dialog);
    verify(network, never()).sendInput(any());
  }

  @Test
  void escapeRejectsYesNoInsteadOfConfirming() {
    AtomicInteger yes = new AtomicInteger();
    AtomicInteger no = new AtomicInteger();
    DialogFactory.showYesNoDialog(
        "Insert USB stick?", "", yes::incrementAndGet, no::incrementAndGet, player.id());

    pressEscape();

    assertEquals(0, yes.get());
    assertEquals(1, no.get());
    assertEquals(0, uiContexts());
  }

  @Test
  void pauseResumeAndToggleLeaveThePlayersExistingUiUntouched() {
    DialogContext context = DialogContext.builder().type(TestDialogType.TEST).build();
    context.owner(player.id());
    UIComponent existing = DialogFactory.show(context, player.id());

    UIComponent pause = PauseDialog.showPauseDialog(player);
    assertNotNull(pause);
    assertSame(existing, player.fetch(UIComponent.class).orElseThrow());
    pause.callbacks().get(DialogContextKeys.ON_RESUME).accept(null);
    assertSame(existing, player.fetch(UIComponent.class).orElseThrow());

    assertNotNull(PauseDialog.showPauseDialog(player));
    PauseDialog.showPauseDialog(player);

    assertSame(existing, player.fetch(UIComponent.class).orElseThrow());
    assertTrue(pauseMenu().isEmpty());
  }

  private UIComponent showPassiveUI() {
    Entity owner = Entity.createLocalEntity("passive-ui");
    DialogContext context = DialogContext.builder().type(TestDialogType.TEST).build();
    context.owner(owner.id());
    UIComponent bar = new UIComponent(context, false, false);
    owner.add(bar);
    Game.add(owner);
    return bar;
  }

  @Test
  void padCloseLeavesANonCloseableDialogWithoutOpeningPause() {
    UIComponent dialog = showDialog(true, false);

    resolver.device(InputDevice.PAD);
    resolver.padDown(Binding.pad(Binding.Pad.B));
    resolver.advance(0);

    assertStillOpen(dialog);
    assertTrue(pauseMenu().isEmpty());
  }

  private UIComponent showDialog(boolean local, boolean closeable) {
    Entity owner = local ? Entity.createLocalEntity("dialog") : new Entity("dialog");
    Game.add(owner);
    DialogContext context = DialogContext.builder().type(TestDialogType.TEST).build();
    context.owner(owner.id());
    return DialogFactory.show(context, true, closeable, player.id());
  }

  private INetworkHandler useNetworkClient() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    INetworkHandler network = mock(INetworkHandler.class);
    when(network.isConnected()).thenReturn(true);
    MockNetworkHandler.useNetworkHandler(network);
    return network;
  }

  private void pressEscape() {
    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.keyUp(Input.Keys.ESCAPE);
    resolver.advance(0);
  }

  private Optional<UIComponent> pauseMenu() {
    return Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.PAUSE_MENU)
        .findFirst();
  }

  private long uiContexts() {
    return resolver.contexts().stream()
        .filter(context -> context.spec().layer() == ContextLayer.UI)
        .count();
  }

  private void assertStillOpen(UIComponent dialog) {
    assertTrue(dialog.isVisible());
    assertEquals(1, uiContexts());
  }

  private enum TestDialogType implements DialogType {
    TEST;

    @Override
    public String type() {
      return "hero-close-test";
    }
  }
}
