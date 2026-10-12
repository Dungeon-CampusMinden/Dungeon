package feature.input.systems;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.server.DialogTracker;
import engine.systems.input.InputManager;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.input.configuration.KeyboardConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Tests controls dialog lifetime independently of the completion callback and visibility. */
class ControlsDialogSystemTest {

  private final AtomicInteger scriptRequests = new AtomicInteger();
  private ControlsDialogSystem system;
  private Entity player;

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);

    player = new Entity("player");
    player.add(new PlayerComponent());
    Game.add(player);
    system =
        new ControlsDialogSystem(
            () -> {
              scriptRequests.incrementAndGet();
              return "Controls";
            });
    Game.add(system);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    DialogTracker.instance().clear();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  @Test
  void repeatedOpenRequestsKeepTheExistingDialog() {
    UIComponent first = openDialog();

    system.showControlsFor(player);

    assertSame(first, onlyControlsDialog());
    assertEquals(1, scriptRequests.get());
    assertArrayEquals(new int[] {player.id()}, first.targetEntityIds());
  }

  @Test
  void controlsKeyReopensAfterRemovalWithoutACompletionCallback() {
    Input originalInput = Gdx.input;
    AtomicReference<InputProcessor> processor = new AtomicReference<>();
    int controlsKey = KeyboardConfig.SHOW_CONTROLS.value();
    Gdx.input = mock(Input.class);
    when(Gdx.input.getInputProcessor()).thenAnswer(ignored -> processor.get());
    doAnswer(
            invocation -> {
              processor.set(invocation.getArgument(0));
              return null;
            })
        .when(Gdx.input)
        .setInputProcessor(any(InputProcessor.class));
    InputManager.init();

    try (MockedStatic<Game> game = mockStatic(Game.class, Mockito.CALLS_REAL_METHODS)) {
      game.when(Game::isHeadless).thenReturn(false);
      processor.get().keyDown(controlsKey);
      system.execute();
      UIComponent first = onlyControlsDialog();
      processor.get().keyUp(controlsKey);
      InputManager.update();
      Game.remove(first.dialogContext().ownerEntity());

      processor.get().keyDown(controlsKey);
      system.execute();

      assertNotSame(first, onlyControlsDialog());
      assertEquals(2, scriptRequests.get());
    } finally {
      processor.get().keyUp(controlsKey);
      InputManager.update();
      Gdx.input = originalInput;
    }
  }

  @Test
  void normalCompletionAllowsReopening() {
    UIComponent first = openDialog();

    first.callbacks().get(DialogContextKeys.ON_CONFIRM).accept(null);

    assertTrue(controlsDialogs().isEmpty());
    assertNotSame(first, openDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void closingWithoutTheCompletionCallbackAllowsReopening() {
    UIComponent first = openDialog();

    UIUtils.closeDialog(first);

    assertNotSame(first, openDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void removingTheComponentWithoutCallbacksAllowsReopening() {
    UIComponent first = openDialog();

    first.dialogContext().ownerEntity().remove(UIComponent.class);

    assertNotSame(first, openDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void removingTheOwnerWithoutCallbacksAllowsReopening() {
    UIComponent first = openDialog();

    Game.remove(first.dialogContext().ownerEntity());

    assertNotSame(first, openDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void replacementAndLateCompletionDoNotAffectTheNewDialog() {
    UIComponent first = openDialog();
    Entity owner = first.dialogContext().ownerEntity();
    UIComponent replacement =
        new UIComponent(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.OK)
                .put(DialogContextKeys.OWNER_ENTITY, owner.id())
                .build(),
            false);
    owner.add(replacement);

    UIComponent second = openDialog();
    first.callbacks().get(DialogContextKeys.ON_CONFIRM).accept(null);
    system.showControlsFor(player);

    assertSame(replacement, owner.fetch(UIComponent.class).orElseThrow());
    assertSame(second, onlyControlsDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void temporarilyHiddenDialogPreventsDuplicates() {
    UIComponent first = openDialog();
    first.dialog().setVisible(false);

    system.showControlsFor(player);

    assertSame(first, onlyControlsDialog());
    assertEquals(1, scriptRequests.get());
  }

  @Test
  void clearingTheSceneAllowsReopeningForTheSamePlayer() {
    UIComponent first = openDialog();
    Game.removeAllEntities();
    Game.add(player);

    assertNotSame(first, openDialog());
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void invalidScriptDoesNotLeaveThePlayerBlocked() {
    system =
        new ControlsDialogSystem(() -> scriptRequests.incrementAndGet() == 1 ? "" : "Controls");

    assertThrows(IllegalArgumentException.class, () -> system.showControlsFor(player));

    openDialog();
    assertEquals(2, scriptRequests.get());
  }

  @Test
  void failedSupplierDoesNotLeaveThePlayerBlocked() {
    system =
        new ControlsDialogSystem(
            () -> {
              if (scriptRequests.incrementAndGet() == 1) {
                throw new IllegalStateException("Controls are not ready");
              }
              return "Controls";
            });

    assertThrows(IllegalStateException.class, () -> system.showControlsFor(player));

    openDialog();
    assertEquals(2, scriptRequests.get());
  }

  private UIComponent openDialog() {
    system.showControlsFor(player);
    return onlyControlsDialog();
  }

  private UIComponent onlyControlsDialog() {
    var dialogs = controlsDialogs();
    assertEquals(1, dialogs.size());
    return dialogs.getFirst();
  }

  private List<UIComponent> controlsDialogs() {
    return Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.DIALOG_DIALOG)
        .toList();
  }
}
