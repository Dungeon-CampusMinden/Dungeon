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
    assertTrue(Game.hud().topmostUI().isEmpty());
  }

  @Test
  void escapeOpensAndClosesALocalPauseMenuWithoutServerMessages() {
    INetworkHandler network = useNetworkClient();

    pressEscape();

    var pause = Game.hud().topmostUI().orElseThrow();
    assertTrue(pause.a().isLocal());
    assertEquals(DialogType.DefaultTypes.PAUSE_MENU, pause.b().dialogContext().dialogType());
    assertFalse(player.isPresent(UIComponent.class));

    pressEscape();

    assertTrue(Game.hud().topmostUI().isEmpty());
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
    assertTrue(Game.hud().topmostUI().isEmpty());
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
    assertSame(dialog, Game.hud().topmostUI().orElseThrow().b());
  }

  @Test
  void localCloseHandlerConsumesEscapeExactlyOnce() {
    UIComponent dialog = showDialog(true, true);
    AtomicInteger advances = new AtomicInteger();
    AtomicInteger closes = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);
    dialog.registerCallback(DialogContextKeys.ON_CLOSE, ignored -> closes.incrementAndGet());

    pressEscape();

    assertEquals(1, advances.get());
    assertEquals(0, closes.get());
    assertSame(dialog, Game.hud().topmostUI().orElseThrow().b());
  }

  @Test
  void nonCloseableDialogBlocksPauseButCanHandleEscape() {
    UIComponent dialog = showDialog(true, false);
    pressEscape();
    assertSame(dialog, Game.hud().topmostUI().orElseThrow().b());

    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);
    pressEscape();

    assertEquals(1, advances.get());
    assertSame(dialog, Game.hud().topmostUI().orElseThrow().b());
    assertEquals(1, Game.entities().filter(entity -> entity.isPresent(UIComponent.class)).count());
  }

  @Test
  void disabledGameplayControlsStillAllowDialogInputWithoutSendingMovement() {
    INetworkHandler network = useNetworkClient();
    player.fetch(InputComponent.class).orElseThrow().deactivateControls(true);
    UIComponent dialog = showDialog(true, false);
    AtomicInteger advances = new AtomicInteger();
    UIUtils.onCloseRequest(dialog.dialog(), advances::incrementAndGet);

    processor.get().keyDown(Input.Keys.W);
    pressEscape();

    assertEquals(1, advances.get());
    assertSame(dialog, Game.hud().topmostUI().orElseThrow().b());
    verify(network, never()).sendInput(any());
  }

  @Test
  void disabledControlsDoNotOpenPauseWithoutAnExistingDialog() {
    player.fetch(InputComponent.class).orElseThrow().deactivateControls(true);

    pressEscape();

    assertTrue(Game.hud().topmostUI().isEmpty());
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
