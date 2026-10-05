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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.handler.INetworkHandler;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.server.DialogTracker;
import engine.systems.input.InputManager;
import engine.systems.input.InputSystem;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.PauseDialog;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Tests the real player close binding through InputManager, InputSystem and the HUD. */
class HeroDialogControlsTest {

  private final AtomicReference<InputProcessor> processor = new AtomicReference<>();
  private Input originalInput;
  private InputSystem inputSystem;
  private Entity player;

  @BeforeAll
  static void registerDialogType() {
    DialogFactory.register(TestDialogType.TEST, ignored -> new Group());
  }

  @BeforeEach
  void setUp() throws ReflectiveOperationException {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    Game.add(Game.hud());
    inputSystem = new InputSystem();
    Game.add(inputSystem);

    player = new Entity("player");
    player.add(new PlayerComponent());
    InputComponent controls = new InputComponent();
    Method setupControls =
        HeroBuilder.class.getDeclaredMethod(
            "setupControls", InputComponent.class, CharacterClass.class);
    setupControls.setAccessible(true);
    setupControls.invoke(null, controls, CharacterClass.WIZARD);
    player.add(controls);
    Game.add(player);

    originalInput = Gdx.input;
    Gdx.input = mock(Input.class);
    processor.set(new InputAdapter());
    when(Gdx.input.getInputProcessor()).thenAnswer(ignored -> processor.get());
    doAnswer(
            invocation -> {
              processor.set(invocation.getArgument(0));
              return null;
            })
        .when(Gdx.input)
        .setInputProcessor(any(InputProcessor.class));
    InputManager.init();
  }

  @AfterEach
  void tearDown() {
    processor.get().keyUp(Input.Keys.ESCAPE);
    processor.get().keyUp(Input.Keys.W);
    InputManager.update();
    Gdx.input = originalInput;
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
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
  }

  @Test
  void escapeOpensAndClosesALocalPauseMenuWithoutServerMessages() {
    INetworkHandler network = useNetworkClient();

    pressEscape();

    var pause = Game.hud().topmostCloseRequestUI().orElseThrow();
    assertTrue(pause.a().isLocal());
    assertEquals(DialogType.DefaultTypes.PAUSE_MENU, pause.b().dialogContext().dialogType());
    assertFalse(player.isPresent(UIComponent.class));

    pressEscape();

    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
    verify(network, never()).send(anyShort(), any(), anyBoolean());
  }

  @Test
  void escapeRemainsAvailableWhenATextFieldConsumesTheKey() {
    UIComponent dialog = showDialog(true, true);
    Stage stage = mock(Stage.class);
    when(stage.getKeyboardFocus()).thenReturn(mock(TextField.class));
    Gdx.input.setInputProcessor(
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            return true;
          }
        });
    InputManager.init();

    try (MockedStatic<Game> game = mockStatic(Game.class, Mockito.CALLS_REAL_METHODS)) {
      game.when(Game::stage).thenReturn(Optional.of(stage));
      pressEscape();
    }

    assertFalse(
        dialog
            .dialogContext()
            .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
            .flatMap(Game::findEntityById)
            .isPresent());
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
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
    assertSame(dialog, Game.hud().topmostCloseRequestUI().orElseThrow().b());
  }

  @Test
  void escapeOpensPauseWhenOnlyAttributeBarsAreVisible() {
    UIComponent bar = showAttributeBar();
    assertTrue(Game.hud().hasOpenUI(player));
    assertFalse(Game.hud().hasOpenPausingUI(player));

    pressEscape();

    assertEquals(
        DialogType.DefaultTypes.PAUSE_MENU,
        Game.hud().topmostCloseRequestUI().orElseThrow().b().dialogContext().dialogType());
    assertTrue(bar.isVisible());
    pressEscape();
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
    assertTrue(bar.isVisible());
  }

  @Test
  void attributeBarsAddedAboveADialogDoNotInterceptEscape() {
    UIComponent dialog = showDialog(true, true);
    UIComponent bar = showAttributeBar();
    Group stage = new Group();
    stage.addActor(dialog.dialog());
    stage.addActor(bar.dialog());
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    pressEscape();

    assertEquals(1, closes.get());
    assertTrue(bar.isVisible());
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
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
    assertSame(dialog, Game.hud().topmostCloseRequestUI().orElseThrow().b());
  }

  @Test
  void disabledGameplayControlsAllowClosingAPausingDialogWithoutSendingMovement() {
    INetworkHandler network = useNetworkClient();
    player.fetch(InputComponent.class).orElseThrow().deactivateControls(true);
    UIComponent dialog = showDialog(true, true);
    AtomicInteger closes = new AtomicInteger();
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    processor.get().keyDown(Input.Keys.W);
    pressEscape();

    assertEquals(1, closes.get());
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
    verify(network, never()).sendInput(any());
  }

  @Test
  void disabledControlsWithOnlyAttributeBarsDoNotOpenMenus() {
    INetworkHandler network = useNetworkClient();
    player.fetch(InputComponent.class).orElseThrow().deactivateControls(true);
    showAttributeBar();

    pressEscape();

    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
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
    assertSame(dialog, Game.hud().topmostCloseRequestUI().orElseThrow().b());
    verify(network, never()).send(anyShort(), any(), anyBoolean());
  }

  @Test
  void nonCloseableDialogueReceivesEscapeEvenWithAnAttributeBarAboveIt() {
    UIComponent dialog = showDialog(true, false);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);
    UIComponent bar = showAttributeBar();
    Group stage = new Group();
    stage.addActor(dialog.dialog());
    stage.addActor(bar.dialog());

    pressEscape();

    assertEquals(1, advances.get());
    assertSame(dialog, Game.hud().topmostCloseRequestUI().orElseThrow().b());
    assertTrue(bar.isVisible());
    assertFalse(
        Game.entities()
            .flatMap(entity -> entity.fetch(UIComponent.class).stream())
            .anyMatch(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.PAUSE_MENU));
  }

  @Test
  void disabledGameplayControlsStillAllowTheNonCloseableDialogHandler() {
    INetworkHandler network = useNetworkClient();
    player.fetch(InputComponent.class).orElseThrow().deactivateControls(true);
    UIComponent dialog = showDialog(true, false);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);

    processor.get().keyDown(Input.Keys.W);
    pressEscape();

    assertEquals(1, advances.get());
    assertSame(dialog, Game.hud().topmostCloseRequestUI().orElseThrow().b());
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
    assertTrue(Game.hud().topmostCloseRequestUI().isEmpty());
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
    assertFalse(
        Game.entities()
            .flatMap(entity -> entity.fetch(UIComponent.class).stream())
            .anyMatch(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.PAUSE_MENU));
  }

  private UIComponent showAttributeBar() {
    Entity owner = Entity.createLocalEntity("health-bar");
    DialogContext context =
        DialogContext.builder().type(DialogType.DefaultTypes.PROGRESS_BAR).build();
    context.owner(owner.id());
    UIComponent bar = new UIComponent(context, false, false);
    owner.add(bar);
    Game.add(owner);
    return bar;
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
    processor.get().keyDown(Input.Keys.ESCAPE);
    inputSystem.execute();
    processor.get().keyUp(Input.Keys.ESCAPE);
    InputManager.update();
  }

  private enum TestDialogType implements DialogType {
    TEST;

    @Override
    public String type() {
      return "hero-close-test";
    }
  }
}
